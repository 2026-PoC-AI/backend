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
import java.util.UUID;

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

    @Transactional
    public Mono<VideoAnalysisResponse> analyzeVideo(MultipartFile file) {
        // 파일 검증
        validateFile(file);

        // 1. 분석 ID 생성
        String analysisId = UUID.randomUUID().toString();

        // 2. 파일 저장
        String storedFilename;
        try {
            storedFilename = saveFile(file, analysisId);
        } catch (IOException e) {
            log.error("파일 저장 실패", e);
            throw new CustomSystemException(VideoErrorCode.UPLOAD_ERROR);
        }

        // 3. DB에 분석 작업 생성 (PENDING 상태)
        VideoAnalysis videoAnalysis = VideoAnalysis.builder()
                .analysisId(analysisId)
                .title(file.getOriginalFilename())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        videoAnalysisMapper.insert(videoAnalysis);

        // 4. DB에 파일 정보 저장
        VideoFile videoFile = VideoFile.builder()
                .fileId(UUID.randomUUID().toString())
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .filePath(uploadPath + "/" + storedFilename)
                .fileSize(file.getSize())
                .format(getFileExtension(file.getOriginalFilename()))
                .uploadedAt(LocalDateTime.now())
                .analysisId(analysisId)
                .build();
        videoFileMapper.insert(videoFile);

        // 5. 상태를 PROCESSING으로 변경
        videoAnalysisMapper.updateStatus(analysisId, "PROCESSING");

        // 6. FastAPI 호출
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());

        return aiServiceWebClient.post()
                .uri("/api/v1/video/analyze")
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(VideoAnalysisResponse.class)
                .doOnNext(response -> {
                    // 7. FastAPI 결과를 DB에 저장
                    saveAnalysisResult(analysisId, response);

                    // 8. 상태를 COMPLETED로 변경
                    videoAnalysisMapper.updateStatus(analysisId, "COMPLETED");
                    videoAnalysisMapper.updateCompletedAt(analysisId);
                })
                .doOnError(e -> {
                    log.error("FastAPI 호출 실패", e);
                    videoAnalysisMapper.updateStatus(analysisId, "FAILED");
                });
    }

    @Transactional(readOnly = true)
    public Mono<VideoAnalysisResponse> getAnalysisResult(String analysisId) {
        // DB에서 전체 분석 결과 조회
        VideoAnalysis analysis = videoAnalysisMapper.findById(analysisId);
        if (analysis == null) {
            throw new CustomSystemException(VideoErrorCode.NOT_FOUND);
        }

        VideoFile file = videoFileMapper.findByAnalysisId(analysisId);
        AnalysisResult result = analysisResultMapper.findByAnalysisId(analysisId);

        if (result == null) {
            // 아직 분석이 완료되지 않은 경우 (PENDING 또는 PROCESSING 상태)
            VideoAnalysisResponse response = VideoAnalysisResponse.builder()
                    .analysisId(analysis.getAnalysisId())
                    .title(analysis.getTitle())
                    .status(analysis.getStatus())
                    .createdAt(analysis.getCreatedAt())
                    .videoFile(convertToFileResponse(file))
                    .build();
            return Mono.just(response);
        }

        // FrameAnalysis 조회
        List<FrameAnalysis> frames = frameAnalysisMapper.findByResultId(result.getResultId());

        // Response 조합
        VideoAnalysisResponse response = VideoAnalysisResponse.builder()
                .analysisId(analysis.getAnalysisId())
                .title(analysis.getTitle())
                .status(analysis.getStatus())
                .createdAt(analysis.getCreatedAt())
                .completedAt(analysis.getCompletedAt())
                .videoFile(convertToFileResponse(file))
                .analysisResult(convertToResultResponse(result))
                .frameAnalyses(frames.stream()
                        .map(this::convertToFrameResponse)
                        .toList())
                .build();

        return Mono.just(response);
    }

    private void validateFile(MultipartFile file) {
        // 파일 필수 체크
        if (file == null || file.isEmpty()) {
            throw new CustomSystemException(VideoErrorCode.FILE_REQUIRED);
        }

        // 파일 크기 체크
        if (file.getSize() > maxFileSize) {
            throw new CustomSystemException(VideoErrorCode.FILE_SIZE_EXCEEDED);
        }

        // 파일 형식 체크
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        if (!ALLOWED_FORMATS.contains(extension)) {
            throw new CustomSystemException(VideoErrorCode.INVALID_FILE_FORMAT);
        }
    }

    private String saveFile(MultipartFile file, String analysisId) throws IOException {
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String storedFilename = analysisId + "_" + System.currentTimeMillis() + "." + extension;
        Path filePath = uploadDir.resolve(storedFilename);

        Files.copy(file.getInputStream(), filePath);
        return storedFilename;
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    @Transactional
    private void saveAnalysisResult(String analysisId, VideoAnalysisResponse response) {
        try {
            // AnalysisResult 저장
            AnalysisResult result = AnalysisResult.builder()
                    .resultId(UUID.randomUUID().toString())
                    .analysisId(analysisId)
                    .createdAt(LocalDateTime.now())
                    .confidenceScore(response.getAnalysisResult().getConfidenceScore())
                    .isDeepfake(response.getAnalysisResult().getIsDeepfake())
                    .modelVersion(response.getAnalysisResult().getModelVersion())
                    .processingTimeMs(response.getAnalysisResult().getProcessingTimeMs())
                    .detectedTechniques(response.getAnalysisResult().getDetectedTechniques())
                    .summary(response.getAnalysisResult().getSummary())
                    .analyzedAt(LocalDateTime.now())
                    .build();
            analysisResultMapper.insert(result);

            // FrameAnalysis 배치 저장
            if (response.getFrameAnalyses() != null && !response.getFrameAnalyses().isEmpty()) {
                List<FrameAnalysis> frameAnalyses = response.getFrameAnalyses().stream()
                        .map(frame -> FrameAnalysis.builder()
                                .frameId(UUID.randomUUID().toString())
                                .resultId(result.getResultId())
                                .frameNumber(frame.getFrameNumber())
                                .timestampSeconds(frame.getTimestampSeconds())
                                .isDeepfake(frame.getIsDeepfake())
                                .confidenceScore(frame.getConfidenceScore())
                                .anomalyType(frame.getAnomalyType())
                                .features(frame.getFeatures())
                                .build())
                        .toList();

                frameAnalysisMapper.insertBatch(frameAnalyses);
            }
        } catch (Exception e) {
            log.error("분석 결과 저장 실패", e);
            throw new CustomSystemException(VideoErrorCode.PROCESSING_ERROR);
        }
    }

    private VideoFileResponse convertToFileResponse(VideoFile file) {
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
        return AnalysisResultResponse.builder()
                .resultId(result.getResultId())
                .analysisId(result.getAnalysisId())
                .createdAt(result.getCreatedAt())
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
                .anomalyType(frame.getAnomalyType())
                .features(frame.getFeatures())
                .build();
    }
}