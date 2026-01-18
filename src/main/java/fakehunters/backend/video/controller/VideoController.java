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
@CrossOrigin(origins = "http://localhost:5173") // 프론트엔드 연동 설정
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoAnalysisService videoAnalysisService;

    /**
     * 비디오 딥페이크 분석 요청
     * @param file 분석할 비디오 파일
     * @return 분석 시작 정보 및 상태
     */
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

    /**
     * 비동기 분석 결과 및 상세 내역 조회
     * @param analysisId 조회할 분석 작업 ID (Long)
     */
    @GetMapping("/analysis/{analysisId}")
    public Mono<ResponseEntity<VideoAnalysisResponse>> getAnalysisResult(
            @PathVariable Long analysisId) { // String -> Long으로 변경

        log.info("분석 결과 조회 요청 - ID: {}", analysisId);

        return videoAnalysisService.getAnalysisResult(analysisId)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("분석 결과 조회 중 오류 발생 - ID: {}", analysisId, e);
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }
}