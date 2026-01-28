package fakehunters.backend.audio.controller;

import fakehunters.backend.audio.dto.request.AudioUploadRequest;
import fakehunters.backend.audio.dto.response.*;
import fakehunters.backend.audio.service.AudioAnalysisService;
import fakehunters.backend.audio.service.AudioFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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
        log.info("=== Audio Upload Request ===");
        log.info("S3 Key: {}", request.getS3Key());
        log.info("File Name: {}", request.getFileName());
        log.info("File Size: {}", request.getFileSize());

        Long audioFileId =
                audioFileService.uploadAudioFile(
                        request.getS3Key(),
                        request.getFileName(),
                        request.getFileSize()
                );

        log.info("Audio File ID created: {}", audioFileId);

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
        log.info("=== Audio Analysis Request ===");
        log.info("Audio File ID: {}", audioFileId);

        try {
            Long analysisResultId = audioAnalysisService.analyzeAudio(audioFileId);
            log.info("Analysis Result ID created: {}", analysisResultId);

            return ResponseEntity.ok(
                    AudioAnalysisStartResponse.builder()
                            .success(true)
                            .analysisResultId(analysisResultId)
                            .status("processing")
                            .message("분석 시작")
                            .build()
            );
        } catch (Exception e) {
            log.error("Analysis failed for audioFileId: {}", audioFileId, e);
            throw e;
        }
    }

    @GetMapping("/{audioFileId}/result")
    public ResponseEntity<AudioAnalysisResponse> getAnalysisResult(
            @PathVariable Long audioFileId
    ) {
        log.info("=== Get Analysis Result Request ===");
        log.info("Audio File ID: {}", audioFileId);

        try {
            AudioAnalysisResponse response = audioAnalysisService.getAnalysisResult(audioFileId);
            log.info("Analysis result prediction: {}", response.getPrediction());
            log.info("Analysis result confidence: {}", response.getConfidence());
            log.info("Full response: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get analysis result for audioFileId: {}", audioFileId, e);
            throw e;
        }
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