package fakehunters.backend.video.controller;

import fakehunters.backend.video.dto.response.VideoAnalysisResponse;
import fakehunters.backend.video.service.VideoAnalysisService;
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

    @PostMapping("/analyze")
    public Mono<ResponseEntity<VideoAnalysisResponse>> analyzeVideo(
            @RequestParam("file") MultipartFile file) {

        log.info("영상 분석 요청 - 파일명: {}, 크기: {}bytes",
                file.getOriginalFilename(), file.getSize());

        return videoAnalysisService.analyzeVideo(file)
                .map(response -> {
                    log.info("분석 완료 - ID: {}, 상태: {}",
                            response.getAnalysisId(), response.getStatus());
                    return ResponseEntity.ok(response);
                });
    }

    @GetMapping("/analysis/{analysisId}")
    public Mono<ResponseEntity<VideoAnalysisResponse>> getAnalysisResult(
            @PathVariable String analysisId) {

        log.info("분석 결과 조회 - ID: {}", analysisId);

        return videoAnalysisService.getAnalysisResult(analysisId)
                .map(ResponseEntity::ok);
    }
}