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
            @RequestParam("file") MultipartFile file
    ) {
        Long audioFileId = audioFileService.uploadAudioFile(file);

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
            @PathVariable Long audioFileId
    ) {
        Long analysisResultId = audioAnalysisService.analyzeAudio(audioFileId);

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
            @PathVariable Long audioFileId
    ) {
        AudioAnalysisResponse response = audioAnalysisService.getAnalysisResult(audioFileId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{audioFileId}")
    public ResponseEntity<AudioFileInfoResponse> getAudioFileInfo(
            @PathVariable Long audioFileId
    ) {
        AudioFileInfoResponse response = audioFileService.getAudioFileInfo(audioFileId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<AudioFileInfoResponse>> getAllAudioFiles() {
        List<AudioFileInfoResponse> responses = audioFileService.getAllAudioFiles();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{audioFileId}")
    public ResponseEntity<AudioDeleteResponse> deleteAudioFile(
            @PathVariable Long audioFileId
    ) {
        audioFileService.deleteAudioFile(audioFileId);

        AudioDeleteResponse response = AudioDeleteResponse.builder()
                .success(true)
                .message("파일 삭제 성공")
                .build();

        return ResponseEntity.ok(response);
    }
}