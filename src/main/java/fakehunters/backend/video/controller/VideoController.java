package fakehunters.backend.video.controller;

import fakehunters.backend.video.dto.response.VideoAnalysisResponse;
import fakehunters.backend.video.mapper.VideoFileMapper;
import fakehunters.backend.video.service.VideoAnalysisService;
import fakehunters.backend.video.domain.VideoFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;


@Slf4j
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoAnalysisService videoAnalysisService;
    private final VideoFileMapper videoFileMapper;

    @PostMapping("/analyze")
    public Mono<ResponseEntity<VideoAnalysisResponse>> analyzeVideo(
            @RequestParam("file") MultipartFile file) {

        log.info("영상 분석 요청 - 파일명: {}, 크기: {}bytes",
                file.getOriginalFilename(), file.getSize());

        return videoAnalysisService.analyzeVideo(file)
                .map(response -> {
                    log.info("분석 요청 수락 - ID: {}, 상태: {}",
                            response.getAnalysisId(), response.getStatus());
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    log.error("분석 프로세스 시작 실패", e);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @GetMapping("/analysis/{analysisId}")
    public Mono<ResponseEntity<VideoAnalysisResponse>> getAnalysisResult(
            @PathVariable Long analysisId) {

        log.info("분석 결과 조회 요청 - ID: {}", analysisId);

        return videoAnalysisService.getAnalysisResult(analysisId)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("분석 결과 조회 중 오류 발생 - ID: {}", analysisId, e);
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    /**
     * 브라우저 호환 비디오 파일 제공
     */
    @GetMapping("/files/{analysisId}")
    public ResponseEntity<String> getVideoFile(@PathVariable Long analysisId) {
        try {
            log.info("=== 비디오 파일 요청 시작 - Analysis ID: {} ===", analysisId);

            VideoFile videoFile = videoFileMapper.findByAnalysisId(analysisId);
            log.info("VideoFile 조회 결과: {}", videoFile);

            if (videoFile == null) {
                return ResponseEntity.ok("VideoFile is null");
            }

            return ResponseEntity.ok("File info: " +
                    "fileId=" + videoFile.getFileId() +
                    ", filePath=" + videoFile.getFilePath() +
                    ", webFilePath=" + videoFile.getWebFilePath());

        } catch (Exception e) {
            log.error("=== 에러 발생 ===", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}