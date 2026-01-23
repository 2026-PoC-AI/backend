package fakehunters.backend.audio.controller;

import fakehunters.backend.audio.dto.response.*;
import fakehunters.backend.audio.service.AudioAnalysisService;
import fakehunters.backend.audio.service.AudioFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AudioFileService audioFileService;
    private final AudioAnalysisService audioAnalysisService;

    @PostMapping("/upload")
    public ResponseEntity<AudioUploadResponse> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId
    ) {
        Long audioFileId = audioFileService.uploadAudioFile(file, userId);

        AudioUploadResponse response = AudioUploadResponse.builder()
                .success(true)
                .audioFileId(audioFileId)
                .fileName(file.getOriginalFilename())
                .message("파일 업로드 성공")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{audioFileId}/analyze")
    public ResponseEntity<AudioAnalysisStartResponse> analyzeAudio(
            @PathVariable Long audioFileId,
            @RequestParam Long userId
    ) {
        Long analysisResultId = audioAnalysisService.analyzeAudio(audioFileId, userId);

        AudioAnalysisStartResponse response = AudioAnalysisStartResponse.builder()
                .success(true)
                .analysisResultId(analysisResultId)
                .status("processing")
                .message("분석 시작")
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{audioFileId}/result")
    public ResponseEntity<AudioAnalysisResponse> getAnalysisResult(
            @PathVariable Long audioFileId,
            @RequestParam Long userId
    ) {
        AudioAnalysisResponse response = audioAnalysisService.getAnalysisResult(audioFileId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{audioFileId}")
    public ResponseEntity<AudioFileInfoResponse> getAudioFileInfo(
            @PathVariable Long audioFileId,
            @RequestParam Long userId
    ) {
        AudioFileInfoResponse response = audioFileService.getAudioFileInfo(audioFileId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<AudioFileInfoResponse>> getUserAudioFiles(
            @RequestParam Long userId
    ) {
        List<AudioFileInfoResponse> responses = audioFileService.getUserAudioFiles(userId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{audioFileId}")
    public ResponseEntity<AudioDeleteResponse> deleteAudioFile(
            @PathVariable Long audioFileId,
            @RequestParam Long userId
    ) {
        audioFileService.deleteAudioFile(audioFileId, userId);

        AudioDeleteResponse response = AudioDeleteResponse.builder()
                .success(true)
                .message("파일 삭제 성공")
                .build();

        return ResponseEntity.ok(response);
    }
}