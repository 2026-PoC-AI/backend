package fakehunters.backend.video.controller;

import fakehunters.backend.video.dto.response.VideoResponse;
import fakehunters.backend.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    // 비디오 조회
    @GetMapping("/{videoId}")
    public ResponseEntity<VideoResponse> getVideo(@PathVariable Long videoId) {
        VideoResponse response = videoService.getVideo(videoId);
        return ResponseEntity.ok(response);
    }

    // 비디오 업로드
    @PostMapping
    public ResponseEntity<VideoResponse> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description
    ) {
        VideoResponse response = videoService.uploadVideo(file, description);
        return ResponseEntity.ok(response);
    }

    // 비디오 삭제
    @DeleteMapping("/{videoId}")
    public ResponseEntity<Void> deleteVideo(
            @PathVariable Long videoId,
            @RequestParam Long userId
    ) {
        videoService.deleteVideo(videoId, userId);
        return ResponseEntity.noContent().build();
    }

    // 딥페이크 탐지
    @PostMapping("/{videoId}/detect")
    public ResponseEntity<VideoResponse> detectDeepfake(@PathVariable Long videoId) {
        VideoResponse response = videoService.detectDeepfake(videoId);
        return ResponseEntity.ok(response);
    }

    // 프레임 추출
    @PostMapping("/{videoId}/extract-frames")
    public ResponseEntity<Void> extractFrames(@PathVariable Long videoId) {
        videoService.extractFrames(videoId);
        return ResponseEntity.ok().build();
    }
}