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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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

    @Value("${file.max-size:104857600}")
    private long maxFileSize;

    @Value("${ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    private static final List<String> ALLOWED_FORMATS = Arrays.asList("mp4", "avi", "mov");

    public Mono<VideoAnalysisResponse> analyzeVideo(MultipartFile file) {
        validateFile(file);

        VideoAnalysis videoAnalysis = VideoAnalysis.builder()
                .title(file.getOriginalFilename())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        videoAnalysisMapper.insert(videoAnalysis);
        Long analysisId = videoAnalysis.getAnalysisId();

        String storedFilename;
        String webFilename = null;
        try {
            storedFilename = saveFile(file, analysisId.toString());

            String originalPath = uploadPath + "/" + storedFilename;
            webFilename = convertToWebFormat(originalPath, analysisId.toString());

        } catch (IOException e) {
            log.error("파일 저장 실패", e);
            throw new CustomSystemException(VideoErrorCode.UPLOAD_ERROR);
        }

        VideoFile videoFile = VideoFile.builder()
                .analysisId(analysisId)
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .filePath(uploadPath + "/" + storedFilename)
                .webFilePath(webFilename != null ? uploadPath + "/" + webFilename : uploadPath + "/" + storedFilename)
                .fileSize(file.getSize())
                .format(getFileExtension(file.getOriginalFilename()))
                .uploadedAt(OffsetDateTime.now())
                .build();
        videoFileMapper.insert(videoFile);

        videoAnalysisMapper.updateStatus(analysisId, "PROCESSING");

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", file.getResource());
        builder.part("analysis_id", analysisId);

        return aiServiceWebClient.post()
                .uri("/api/v1/video/analyze")
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(VideoAnalysisResponse.class)
                .doOnNext(response -> {
                    saveAnalysisResultInternal(analysisId, response);
                    videoAnalysisMapper.updateStatus(analysisId, "COMPLETED");
                    videoAnalysisMapper.updateCompletedAt(analysisId);
                })
                .doOnError(e -> {
                    log.error("AI 서비스 호출 실패", e);
                    videoAnalysisMapper.updateStatus(analysisId, "FAILED");
                });
    }

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

    private void saveAnalysisResultInternal(Long analysisId, VideoAnalysisResponse response) {
        try {
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

            if (response.getFrameAnalyses() != null && !response.getFrameAnalyses().isEmpty()) {
                List<FrameAnalysis> frameAnalyses = response.getFrameAnalyses().stream()
                        .map(frame -> FrameAnalysis.builder()
                                .resultId(resultId)
                                .frameNumber(frame.getFrameNumber())
                                .timestampSeconds(frame.getTimestampSeconds())
                                .isDeepfake(frame.getIsDeepfake())
                                .confidenceScore(frame.getConfidenceScore())
                                .anomalyRegions(convertToJsonString(frame.getAnomalyType()))
                                .features(convertToJsonString(frame.getFeatures()))
                                .build())
                        .toList();

                frameAnalysisMapper.insertBatch(frameAnalyses);
            }
        } catch (Exception e) {
            log.error("DB 결과 저장 실패", e);
        }
    }

    private String convertToWebFormat(String originalPath, String analysisId) throws IOException {
        Path uploadDir = Paths.get(uploadPath);
        String webFilename = analysisId + "_web_" + System.currentTimeMillis() + ".mp4";
        String webPath = uploadDir.resolve(webFilename).toString();

        try {
            List<String> command = Arrays.asList(
                    ffmpegPath,
                    "-i", originalPath,
                    "-c:v", "libx264",
                    "-preset", "ultrafast",
                    "-c:a", "aac",
                    "-movflags", "+faststart",
                    "-y",
                    webPath
            );

            log.info("=== FFmpeg 변환 시작 ===");
            log.info("명령어: {}", String.join(" ", command));
            log.info("원본 파일: {}", originalPath);
            log.info("출력 파일: {}", webPath);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // stderr를 stdout으로 합침

            Process process = processBuilder.start();

            // 출력 읽기
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[FFmpeg] {}", line);
                }
            }

            int exitCode = process.waitFor();
            log.info("FFmpeg 종료 코드: {}", exitCode);

            if (exitCode != 0) {
                log.warn("ffmpeg 변환 실패 (exit code: {}), 원본 파일 사용", exitCode);
                return null;
            }

            log.info("ffmpeg 변환 성공: {}", webFilename);
            return webFilename;
        } catch (Exception e) {
            log.error("ffmpeg 변환 중 예외 발생", e);
            return null;
        }
    }

    private String convertToJsonString(String value) {
        if (value == null || value.isEmpty()) {
            return "{}";
        }
        if (value.startsWith("{") || value.startsWith("[")) {
            return value;
        }
        return "{\"type\":\"" + value.replace("\"", "\\\"") + "\"}";
    }

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

    private VideoFileResponse convertToFileResponse(VideoFile file) {
        if (file == null) return null;

        // OffsetDateTime을 LocalDateTime으로 변환하여 DTO에 담기
        LocalDateTime localUploadedAt = (file.getUploadedAt() != null)
                ? file.getUploadedAt().toLocalDateTime()
                : null;

        return VideoFileResponse.builder()
                .fileId(file.getFileId())
                .originalFilename(file.getOriginalFilename())
                .storedFilename(file.getStoredFilename())
                .filePath(file.getWebFilePath() != null ? file.getWebFilePath() : file.getFilePath())
                .fileSize(file.getFileSize())
                .durationSeconds(file.getDurationSeconds())
                .resolution(file.getResolution())
                .format(file.getFormat())
                .fps(file.getFps())
                .uploadedAt(localUploadedAt) // // 변환된 값 세팅
                .analysisId(file.getAnalysisId())
                .build();
    }

    private AnalysisResultResponse convertToResultResponse(AnalysisResult result) {
        if (result == null) return null;
        return AnalysisResultResponse.builder()
                .resultId(result.getResultId())
                .analysisId(result.getAnalysisId())
                .createdAt(result.getAnalyzedAt())
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
                .anomalyType(frame.getAnomalyRegions())
                .features(frame.getFeatures())
                .build();
    }
}