package fakehunters.backend.video.controller;

import fakehunters.backend.video.domain.FrameAnalysis;
import fakehunters.backend.video.dto.response.VideoAnalysisResponse;
import fakehunters.backend.video.dto.response.VideoProgressResponse;
import fakehunters.backend.video.mapper.FrameAnalysisMapper;
import fakehunters.backend.video.mapper.VideoFileMapper;
import fakehunters.backend.video.service.VideoAnalysisService;
import fakehunters.backend.video.domain.VideoFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import org.springframework.core.io.ByteArrayResource;
import java.io.RandomAccessFile;

@Slf4j
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoAnalysisService videoAnalysisService;
    private final VideoFileMapper videoFileMapper;
    private final FrameAnalysisMapper frameAnalysisMapper;

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

    // 호환 비디오 파일 스트리밍
    @GetMapping("/files/{analysisId}")
    public ResponseEntity<Resource> getVideoFile(
            @PathVariable Long analysisId,
            @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {

        try {
            log.info("비디오 파일 요청 - Analysis ID: {}, Range: {}", analysisId, rangeHeader);

            VideoFile videoFile = videoFileMapper.findByAnalysisId(analysisId);

            if (videoFile == null) {
                log.error("VideoFile not found for analysisId: {}", analysisId);
                return ResponseEntity.notFound().build();
            }

            String filePath = videoFile.getWebFilePath() != null
                    ? videoFile.getWebFilePath()
                    : videoFile.getFilePath();

            log.info("파일 경로: {}", filePath);

            File file = new File(filePath);

            if (!file.exists()) {
                log.error("파일이 존재하지 않음: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            long fileSize = file.length();

            // Range 헤더가 없으면 전체 파일 반환
            if (rangeHeader == null) {
                Resource resource = new FileSystemResource(file);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("video/mp4"))
                        .contentLength(fileSize)
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                        .body(resource);
            }

            // Range 요청 처리
            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 && !ranges[1].isEmpty()
                    ? Long.parseLong(ranges[1])
                    : fileSize - 1;

            // end가 파일 크기를 초과하지 않도록 보정
            if (end >= fileSize) {
                end = fileSize - 1;
            }

            long contentLength = end - start + 1;

            log.info("Range 요청 처리 - start: {}, end: {}, contentLength: {}, fileSize: {}",
                    start, end, contentLength, fileSize);

            // ByteArrayResource 사용하여 정확한 범위 반환
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(start);
                byte[] buffer = new byte[(int) contentLength];
                raf.readFully(buffer);

                ByteArrayResource resource = new ByteArrayResource(buffer);

                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .contentType(MediaType.parseMediaType("video/mp4"))
                        .contentLength(contentLength)
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CONTENT_RANGE,
                                String.format("bytes %d-%d/%d", start, end, fileSize))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                        .body(resource);
            }

        } catch (Exception e) {
            log.error("비디오 파일 제공 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/progress/{analysisId}")
    public Mono<ResponseEntity<VideoProgressResponse>> getAnalysisProgress(
            @PathVariable Long analysisId) {

        log.info("========================================");
        log.info("진행률 조회 요청 - ID: {}", analysisId);
        log.info("========================================");

        return videoAnalysisService.getAnalysisProgress(analysisId)
                .map(response -> {
                    log.info("✅ 진행률 응답: {}", response);
                    return ResponseEntity.ok(response);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("❌ 진행률 조회 실패 - ID: {}", analysisId, e);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

}