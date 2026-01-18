package fakehunters.backend.video.service;

import fakehunters.backend.global.exception.custom.CustomSystemException;
import fakehunters.backend.video.domain.*;
import fakehunters.backend.video.dto.response.*;
import fakehunters.backend.video.exception.VideoErrorCode;
import fakehunters.backend.video.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoAnalysisService {

    private final WebClient aiServiceWebClient;
    private final VideoAnalysisMapper videoAnalysisMapper;
    private final VideoFileMapper videoFileMapper;
    private final AnalysisResultMapper analysisResultMapper;
    private final FrameAnalysisMapper frameAnalysisMapper;

    @Value("${file.upload.path:/uploads/videos}")
    private String uploadPath;

    @Value("${file.max-size:104857600}") // 100MB
    private long maxFileSize;

    private static final List<String> ALLOWED_FORMATS = Arrays.asList("mp4", "avi", "mov");

    /**
     * 비디오 분석 요청 처리
     */
    @Transactional
    public Mono<VideoAnalysisResponse> analyzeVideo(MultipartFile file) {
        validateFile(file);

        // 1. 분석 작업 생성 (DB에서 Long ID 발급)
        VideoAnalysis videoAnalysis = VideoAnalysis.builder()
                .title(file.getOriginalFilename())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        videoAnalysisMapper.insert(videoAnalysis);
        Long analysisId = videoAnalysis.getAnalysisId(); // 생성된 PK 확보

        // 2. 파일 저장
        String storedFilename;
        try {
            storedFilename = saveFile(file, analysisId.toString());
        } catch (IOException e) {
            log.error("파일 저장 실패", e);
            throw new CustomSystemException(VideoErrorCode.UPLOAD_ERROR);
        }

        // 3. DB에 파일 정보 저장
        VideoFile videoFile = VideoFile.builder()
                .analysisId(analysisId)
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .filePath(uploadPath + "/" + storedFilename)
                .fileSize(file.getSize())
                .format(getFileExtension(file.getOriginalFilename()))
                .uploadedAt(LocalDateTime.now())
                .build();
        videoFileMapper.insert(videoFile);

        // 4. 상태를 PROCESSING으로 변경
        videoAnalysisMapper.updateStatus(analysisId, "PROCESSING");

        // 5. ★ FastAPI 호출 시 analysis_id 전달 ★
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());
        builder.part("analysis_id", analysisId); // FastAPI가 이 ID를 기반으로 응답하게 함

        return aiServiceWebClient.post()
                .uri("/api/v1/video/analyze")
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(VideoAnalysisResponse.class)
                .doOnNext(response -> {
                    // response.getAnalysisId()는 이제 UUID가 아닌 우리가 보낸 Long ID가 들어있음
                    saveAnalysisResultInternal(analysisId, response);
                    videoAnalysisMapper.updateStatus(analysisId, "COMPLETED");
                    videoAnalysisMapper.updateCompletedAt(analysisId);
                })
                .doOnError(e -> {
                    log.error("AI 서비스 호출 실패", e);
                    videoAnalysisMapper.updateStatus(analysisId, "FAILED");
                });
    }

    /**
     * 분석 결과 조회 (Long ID 사용)
     */
    @Transactional(readOnly = true)
    public Mono<VideoAnalysisResponse> getAnalysisResult(Long analysisId) {
        VideoAnalysis analysis = videoAnalysisMapper.findById(analysisId);
        if (analysis == null) {
            throw new CustomSystemException(VideoErrorCode.NOT_FOUND);
        }

        VideoFile file = videoFileMapper.findByAnalysisId(analysisId);
        AnalysisResult result = analysisResultMapper.findByAnalysisId(analysisId);

        if (result == null) {
            return Mono.just(VideoAnalysisResponse.builder()
                    .analysisId(analysis.getAnalysisId())
                    .title(analysis.getTitle())
                    .status(analysis.getStatus())
                    .createdAt(analysis.getCreatedAt())
                    .videoFile(convertToFileResponse(file))
                    .build());
        }

        List<FrameAnalysis> frames = frameAnalysisMapper.findByResultId(result.getResultId());

        return Mono.just(VideoAnalysisResponse.builder()
                .analysisId(analysis.getAnalysisId())
                .title(analysis.getTitle())
                .status(analysis.getStatus())
                .createdAt(analysis.getCreatedAt())
                .completedAt(analysis.getCompletedAt())
                .videoFile(convertToFileResponse(file))
                .analysisResult(convertToResultResponse(result))
                .frameAnalyses(frames.stream().map(this::convertToFrameResponse).toList())
                .build());
    }

    /**
     * 내부 결과 저장 로직 (ID 체인 연결)
     */
    private void saveAnalysisResultInternal(Long analysisId, VideoAnalysisResponse response) {
        try {
            // 1. Result 저장
            AnalysisResult result = AnalysisResult.builder()
                    .analysisId(analysisId)
                    .isDeepfake(response.getAnalysisResult().getIsDeepfake())
                    .confidenceScore(response.getAnalysisResult().getConfidenceScore())
                    .modelVersion(response.getAnalysisResult().getModelVersion())
                    .processingTimeMs(response.getAnalysisResult().getProcessingTimeMs())
                    .detectedTechniques(response.getAnalysisResult().getDetectedTechniques())
                    .summary(response.getAnalysisResult().getSummary())
                    .analyzedAt(LocalDateTime.now())
                    .build();

            analysisResultMapper.insert(result);
            Long resultId = result.getResultId();

            // 2. Frame 상세 배치 저장
            if (response.getFrameAnalyses() != null && !response.getFrameAnalyses().isEmpty()) {
                List<FrameAnalysis> frameAnalyses = response.getFrameAnalyses().stream()
                        .map(frame -> FrameAnalysis.builder()
                                .resultId(resultId)
                                .frameNumber(frame.getFrameNumber())
                                .timestampSeconds(frame.getTimestampSeconds())
                                .isDeepfake(frame.getIsDeepfake())
                                .confidenceScore(frame.getConfidenceScore())
                                .anomalyRegions(frame.getAnomalyType()) // DTO의 anomalyType을 도메인의 anomalyRegions에 매핑
                                .features(frame.getFeatures())
                                .build())
                        .toList();

                frameAnalysisMapper.insertBatch(frameAnalyses);
            }
        } catch (Exception e) {
            log.error("DB 결과 저장 실패", e);
            // 비동기 처리 중 발생한 예외는 별도 로깅
        }
    }

    // --- Helper Methods ---

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new CustomSystemException(VideoErrorCode.FILE_REQUIRED);
        if (file.getSize() > maxFileSize) throw new CustomSystemException(VideoErrorCode.FILE_SIZE_EXCEEDED);
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!ALLOWED_FORMATS.contains(extension)) throw new CustomSystemException(VideoErrorCode.INVALID_FILE_FORMAT);
    }

    private String saveFile(MultipartFile file, String analysisId) throws IOException {
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
        String extension = getFileExtension(file.getOriginalFilename());
        String storedFilename = analysisId + "_" + System.currentTimeMillis() + "." + extension;
        Files.copy(file.getInputStream(), uploadDir.resolve(storedFilename));
        return storedFilename;
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    // --- Conversion Methods ---

    private VideoFileResponse convertToFileResponse(VideoFile file) {
        if (file == null) return null;
        return VideoFileResponse.builder()
                .fileId(file.getFileId())
                .originalFilename(file.getOriginalFilename())
                .storedFilename(file.getStoredFilename())
                .filePath(file.getFilePath())
                .fileSize(file.getFileSize())
                .durationSeconds(file.getDurationSeconds())
                .resolution(file.getResolution())
                .format(file.getFormat())
                .fps(file.getFps())
                .uploadedAt(file.getUploadedAt())
                .analysisId(file.getAnalysisId())
                .build();
    }

    private AnalysisResultResponse convertToResultResponse(AnalysisResult result) {
        if (result == null) return null;
        return AnalysisResultResponse.builder()
                .resultId(result.getResultId())
                .analysisId(result.getAnalysisId())
                .createdAt(result.getAnalyzedAt()) // 도메인의 analyzedAt을 DTO의 createdAt에 매핑
                .confidenceScore(result.getConfidenceScore())
                .isDeepfake(result.getIsDeepfake())
                .modelVersion(result.getModelVersion())
                .processingTimeMs(result.getProcessingTimeMs())
                .detectedTechniques(result.getDetectedTechniques())
                .summary(result.getSummary())
                .analyzedAt(result.getAnalyzedAt())
                .build();
    }

    private FrameAnalysisResponse convertToFrameResponse(FrameAnalysis frame) {
        return FrameAnalysisResponse.builder()
                .frameId(frame.getFrameId())
                .frameNumber(frame.getFrameNumber())
                .timestampSeconds(frame.getTimestampSeconds())
                .isDeepfake(frame.getIsDeepfake())
                .confidenceScore(frame.getConfidenceScore())
                .anomalyType(frame.getAnomalyRegions()) // 도메인 anomalyRegions -> DTO anomalyType
                .features(frame.getFeatures())
                .build();
    }
}