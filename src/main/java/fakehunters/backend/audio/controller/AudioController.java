package fakehunters.backend.audio.controller;

import fakehunters.backend.audio.dto.request.AudioUploadRequest;
import fakehunters.backend.audio.dto.response.*;
import fakehunters.backend.audio.service.AudioAnalysisService;
import fakehunters.backend.audio.service.AudioFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AudioFileService audioFileService;
    private final AudioAnalysisService audioAnalysisService;

    /**
     * Presigned 업로드 완료 후 DB 등록
     */
    @PostMapping("/upload")
    public ResponseEntity<AudioUploadResponse> uploadAudio(
            @RequestBody AudioUploadRequest request
    ) {
        Long audioFileId =
                audioFileService.uploadAudioFile(
                        request.getS3Key(),
                        request.getFileName(),
                        request.getFileSize()
                );

        return ResponseEntity.ok(
                AudioUploadResponse.builder()
                        .success(true)
                        .audioFileId(audioFileId)
                        .fileName(request.getFileName())
                        .message("파일 업로드 성공")
                        .build()
        );
    }

    @PostMapping("/{audioFileId}/analyze")
    public ResponseEntity<AudioAnalysisStartResponse> analyzeAudio(
            @PathVariable Long audioFileId
    ) {
        Long analysisResultId = audioAnalysisService.analyzeAudio(audioFileId);

        return ResponseEntity.ok(
                AudioAnalysisStartResponse.builder()
                        .success(true)
                        .analysisResultId(analysisResultId)
                        .status("processing")
                        .message("분석 시작")
                        .build()
        );
    }

    @GetMapping("/{audioFileId}/result")
    public ResponseEntity<AudioAnalysisResponse> getAnalysisResult(
            @PathVariable Long audioFileId
    ) {
        return ResponseEntity.ok(
                audioAnalysisService.getAnalysisResult(audioFileId)
        );
    }

    @GetMapping("/{audioFileId}")
    public ResponseEntity<AudioFileInfoResponse> getAudioFileInfo(
            @PathVariable Long audioFileId
    ) {
        return ResponseEntity.ok(
                audioFileService.getAudioFileInfo(audioFileId)
        );
    }

    @GetMapping("/list")
    public ResponseEntity<List<AudioFileInfoResponse>> getAllAudioFiles() {
        return ResponseEntity.ok(
                audioFileService.getAllAudioFiles()
        );
    }

    @DeleteMapping("/{audioFileId}")
    public ResponseEntity<AudioDeleteResponse> deleteAudioFile(
            @PathVariable Long audioFileId
    ) {
        audioFileService.deleteAudioFile(audioFileId);

        return ResponseEntity.ok(
                AudioDeleteResponse.builder()
                        .success(true)
                        .message("파일 삭제 성공")
                        .build()
        );
    }
}
