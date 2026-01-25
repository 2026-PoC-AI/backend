package fakehunters.backend.video.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fakehunters.backend.global.exception.custom.CustomSystemException;
import fakehunters.backend.video.domain.*;
import fakehunters.backend.video.dto.response.*;
import fakehunters.backend.video.exception.VideoErrorCode;
import fakehunters.backend.video.mapper.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
                .createdAt(OffsetDateTime.now())
                .build();

        videoAnalysisMapper.insert(videoAnalysis);
        Long analysisId = videoAnalysis.getAnalysisId();

        String storedFilename;
        String webFilename = null;
        Double videoDuration = null;  // duration 저장

        try {
            storedFilename = saveFile(file, analysisId.toString());
            String originalPath = uploadPath + "/" + storedFilename;

            log.info("FFmpeg 변환 시작 - ID: {}", analysisId);
            VideoConversionResult conversionResult = convertToWebFormat(originalPath, analysisId.toString());

            if (conversionResult != null) {
                webFilename = conversionResult.getFilename();
                videoDuration = conversionResult.getDuration();  // duration 가져오기
            }

            log.info("FFmpeg 변환 완료 - ID: {}, webFilename: {}, duration: {}초",
                    analysisId, webFilename, videoDuration);

        } catch (IOException e) {
            log.error("파일 처리 실패", e);
            throw new CustomSystemException(VideoErrorCode.UPLOAD_ERROR);
        }

        VideoFile videoFile = VideoFile.builder()
                .analysisId(analysisId)
                .originalFilename(file.getOriginalFilename())
                .storedFilename(storedFilename)
                .filePath(uploadPath + "/" + storedFilename)
                .webFilePath(webFilename != null ? uploadPath + "/" + webFilename : uploadPath + "/" + storedFilename)
                .fileSize(file.getSize())
                .durationSeconds(videoDuration)  // duration 설정
                .format(getFileExtension(file.getOriginalFilename()))
                .uploadedAt(OffsetDateTime.now())
                .build();
        videoFileMapper.insert(videoFile);

        videoAnalysisMapper.updateStatus(analysisId, "PROCESSING");

        VideoProgressResponse initialProgress = VideoProgressResponse.builder()
                .progress(0)
                .stage("ai_analysis")
                .detail("AI 분석을 시작합니다.")
                .build();

        String json;
        try {
            json = objectMapper.writeValueAsString(initialProgress);
        } catch (Exception e) {
            log.error("JSON 직렬화 실패", e);
            throw new CustomSystemException(VideoErrorCode.INTERNAL_SERVER_ERROR);
        }

        String key = "video_analysis_progress:" + analysisId;

        return redisTemplate.opsForValue()
                .set(key, json, java.time.Duration.ofHours(1))
                .doOnSuccess(result -> {
                    log.info("Redis 초기 상태 저장 완료 - Key: {}", key);
                    processAIAnalysisAsync(analysisId, storedFilename);
                })
                .doOnError(e -> log.error("Redis 초기 상태 저장 실패 - Key: {}", key, e))
                .thenReturn(VideoAnalysisResponse.builder()
                        .analysisId(analysisId)
                        .title(file.getOriginalFilename())
                        .status("PROCESSING")
                        .createdAt(videoAnalysis.getCreatedAt())
                        .videoFile(convertToFileResponse(videoFile))
                        .build());
    }

    @Async
    public void processAIAnalysisAsync(Long analysisId, String storedFilename) {
        log.info("AI 분석 백그라운드 시작 - ID: {}", analysisId);

        try {
            Path filePath = Paths.get(uploadPath, storedFilename);
            byte[] fileBytes = Files.readAllBytes(filePath);

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return storedFilename;
                }
            });
            builder.part("analysis_id", analysisId);

            aiServiceWebClient.post()
                    .uri("/api/v1/video/analyze")
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(VideoAnalysisResponse.class)
                    .doOnNext(response -> {
                        log.info("AI 분석 완료 - ID: {}", analysisId);
                        saveAnalysisResultInternal(analysisId, response);
                        videoAnalysisMapper.updateStatus(analysisId, "COMPLETED");
                        videoAnalysisMapper.updateCompletedAt(analysisId);
                        updateProgressToCompleted(analysisId);
                    })
                    .doOnError(e -> {
                        log.error("AI 서비스 호출 실패 - ID: {}", analysisId, e);
                        videoAnalysisMapper.updateStatus(analysisId, "FAILED");
                        updateProgressToFailed(analysisId);
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("백그라운드 분석 실패 - ID: {}", analysisId, e);
            videoAnalysisMapper.updateStatus(analysisId, "FAILED");
            updateProgressToFailed(analysisId);
        }
    }

    @Getter
    @AllArgsConstructor
    private static class VideoConversionResult {
        private String filename;
        private Double duration;
    }

    private void updateProgressToCompleted(Long analysisId) {
        VideoProgressResponse progress = VideoProgressResponse.builder()
                .progress(100)
                .stage("completed")
                .detail("분석이 완료되었습니다.")
                .build();

        try {
            String json = objectMapper.writeValueAsString(progress);
            String key = "video_analysis_progress:" + analysisId;
            redisTemplate.opsForValue()
                    .set(key, json, java.time.Duration.ofHours(1))
                    .subscribe();
        } catch (Exception e) {
            log.error("Redis 완료 상태 업데이트 실패", e);
        }
    }

    private void updateProgressToFailed(Long analysisId) {
        VideoProgressResponse progress = VideoProgressResponse.builder()
                .progress(0)
                .stage("failed")
                .detail("분석에 실패했습니다.")
                .build();

        try {
            String json = objectMapper.writeValueAsString(progress);
            String key = "video_analysis_progress:" + analysisId;
            redisTemplate.opsForValue()
                    .set(key, json, java.time.Duration.ofHours(1))
                    .subscribe();
        } catch (Exception e) {
            log.error("Redis 실패 상태 업데이트 실패", e);
        }
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

    public Mono<VideoProgressResponse> getAnalysisProgress(Long analysisId) {
        String key = "video_analysis_progress:" + analysisId;

        log.info("Redis 조회 - Key: {}", key);

        return redisTemplate.opsForValue()
                .get(key)
                .doOnNext(json -> {
                    log.info("Redis 원본 데이터: {}", json);
                })
                .map(json -> {
                    try {
                        VideoProgressResponse response = objectMapper.readValue(json, VideoProgressResponse.class);
                        log.info("파싱 성공: progress={}, stage={}",
                                response.getProgress(), response.getStage());
                        return response;
                    } catch (Exception e) {
                        log.error("JSON 파싱 실패: {}", json, e);
                        return null;
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Redis에 데이터 없음 - Key: {}", key);
                    return Mono.empty();
                }));
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
                    .analyzedAt(OffsetDateTime.now())
                    .build();

            analysisResultMapper.insert(result);
            Long resultId = result.getResultId();

            if (response.getFrameAnalyses() != null && !response.getFrameAnalyses().isEmpty()) {
                List<FrameAnalysis> frameAnalyses = response.getFrameAnalyses().stream()
                        .map(frame -> FrameAnalysis.builder()
                                .resultId(resultId)
                                .frameNumber(frame.getFrameNumber())
                                .timestampSeconds(frame.getTimestampSeconds() != null
                                        ? frame.getTimestampSeconds().doubleValue()
                                        : null)
                                .isDeepfake(frame.getIsDeepfake())
                                .confidenceScore(frame.getConfidenceScore() != null
                                        ? frame.getConfidenceScore().doubleValue()
                                        : null)
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

    private VideoConversionResult convertToWebFormat(String originalPath, String analysisId) throws IOException {
        Path uploadDir = Paths.get(uploadPath);
        String webFilename = analysisId + "_web_" + System.currentTimeMillis() + ".mp4";
        String webPath = uploadDir.resolve(webFilename).toString();

        try {
            List<String> command = new ArrayList<>(Arrays.asList(
                    ffmpegPath,
                    "-i", originalPath,
                    "-c:v", "libx264",
                    "-preset", "medium",
                    "-profile:v", "baseline",
                    "-level", "3.0",
                    "-pix_fmt", "yuv420p",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-movflags", "+faststart",
                    "-y",
                    webPath
            ));

            log.info("FFmpeg 변환 명령: {}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            Double duration = null;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Duration 추출
                    if (line.contains("Duration:")) {
                        String durationStr = line.split("Duration:")[1].split(",")[0].trim();
                        String[] parts = durationStr.split(":");
                        if (parts.length == 3) {
                            int hours = Integer.parseInt(parts[0]);
                            int minutes = Integer.parseInt(parts[1]);
                            double seconds = Double.parseDouble(parts[2]);
                            duration = hours * 3600 + minutes * 60 + seconds;
                        }
                    }

                    if (line.contains("Duration:") || line.contains("time=")) {
                        log.info("[FFmpeg] {}", line);
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("FFmpeg 변환 실패 (exit code: {})", exitCode);
                return null;
            }

            File webFile = new File(webPath);
            if (!webFile.exists() || webFile.length() == 0) {
                log.error("변환 파일 생성 실패");
                return null;
            }

            log.info("FFmpeg 변환 성공: {}, 크기: {} bytes, 길이: {}초",
                    webFilename, webFile.length(), duration);

            return new VideoConversionResult(webFilename, duration);

        } catch (Exception e) {
            log.error("FFmpeg 변환 예외", e);
            return null;
        }
    }

    // 우선은 사용하지 않고 테스트
    private VideoInfo getVideoInfo(String filePath) {
        VideoInfo info = new VideoInfo();
        try {
            String ffprobePath = ffmpegPath.replace("ffmpeg.exe", "ffprobe.exe")
                    .replace("ffmpeg", "ffprobe");

            List<String> command = Arrays.asList(
                    ffprobePath,
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries",
                    "stream=r_frame_rate,duration:format=duration",
                    "-of", "json",
                    filePath
            );

            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            p.waitFor();

            String json = output.toString();
            if (json.contains("\"duration\"")) {
                String durStr = json.split("\"duration\":\"")[1].split("\"")[0];
                info.duration = Double.parseDouble(durStr);
            }
            if (json.contains("\"r_frame_rate\"")) {
                String fpsStr = json.split("\"r_frame_rate\":\"")[1].split("\"")[0];
                String[] parts = fpsStr.split("/");
                if (parts.length == 2) {
                    info.fps = Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
                }
            }

            List<String> audioCmd = Arrays.asList(
                    ffprobePath,
                    "-v", "error",
                    "-select_streams", "a",
                    "-show_entries", "stream=codec_type",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    filePath
            );
            pb = new ProcessBuilder(audioCmd);
            p = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {
                info.hasAudio = reader.readLine() != null;
            }
            p.waitFor();

        } catch (Exception e) {
            log.warn("비디오 정보 조회 실패: {}", filePath, e);
        }
        return info;
    }

    private static class VideoInfo {
        Double duration;
        Double fps;
        boolean hasAudio = false;
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
                .uploadedAt(file.getUploadedAt())
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