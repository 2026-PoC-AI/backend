package fakehunters.backend.video.controller;

import fakehunters.backend.video.domain.VideoFile;
import fakehunters.backend.video.dto.response.VideoAnalysisResponse;
import fakehunters.backend.video.dto.response.VideoProgressResponse;
import fakehunters.backend.video.mapper.VideoFileMapper;
import fakehunters.backend.video.service.VideoAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

    @GetMapping("/files/{analysisId}")
    public ResponseEntity<Resource> getVideoFile(
            @PathVariable Long analysisId,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {

        try {
            log.debug("비디오 파일 요청 - Analysis ID: {}, Range: {}", analysisId, rangeHeader);

            VideoFile videoFile = videoFileMapper.findByAnalysisId(analysisId);
            if (videoFile == null) {
                return ResponseEntity.notFound().build();
            }

            String filePath = videoFile.getWebFilePath() != null
                    ? videoFile.getWebFilePath()
                    : videoFile.getFilePath();

            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            long fileSize = file.length();

            // Range 헤더 없으면 전체 파일
            if (rangeHeader == null || rangeHeader.isEmpty()) {
                Resource resource = new FileSystemResource(file);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("video/mp4"))
                        .contentLength(fileSize)
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                        .body(resource);
            }

            // Range 파싱
            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 && !ranges[1].isEmpty()
                    ? Long.parseLong(ranges[1])
                    : fileSize - 1;

            // 범위 검증
            if (end >= fileSize) {
                end = fileSize - 1;
            }

            if (start > end || start >= fileSize) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }

            long contentLength = end - start + 1;

            log.debug("Range 처리 - start: {}, end: {}, length: {}, total: {}",
                    start, end, contentLength, fileSize);

            // InputStream을 사용한 Range 처리 (메모리 효율적)
            InputStream inputStream = new FileInputStream(file);
            inputStream.skip(start);

            // 지정된 범위만큼만 읽을 수 있는 제한된 InputStream
            InputStream limitedStream = new InputStream() {
                private long remaining = contentLength;

                @Override
                public int read() throws IOException {
                    if (remaining <= 0) {
                        return -1;
                    }
                    int result = inputStream.read();
                    if (result != -1) {
                        remaining--;
                    }
                    return result;
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    if (remaining <= 0) {
                        return -1;
                    }
                    int toRead = (int) Math.min(len, remaining);
                    int result = inputStream.read(b, off, toRead);
                    if (result > 0) {
                        remaining -= result;
                    }
                    return result;
                }

                @Override
                public void close() throws IOException {
                    inputStream.close();
                }
            };

            Resource resource = new InputStreamResource(limitedStream);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.parseMediaType("video/mp4"))
                    .contentLength(contentLength)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_RANGE,
                            String.format("bytes %d-%d/%d", start, end, fileSize))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);

        } catch (Exception e) {
            log.error("비디오 파일 제공 중 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/progress/{analysisId}")
    public Mono<ResponseEntity<VideoProgressResponse>> getAnalysisProgress(
            @PathVariable Long analysisId) {

        log.debug("진행률 조회 요청 - ID: {}", analysisId);

        return videoAnalysisService.getAnalysisProgress(analysisId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(e -> {
                    log.error("진행률 조회 실패 - ID: {}", analysisId, e);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}