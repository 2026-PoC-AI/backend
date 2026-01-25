package fakehunters.backend.audio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fakehunters.backend.audio.domain.AudioFile;
import fakehunters.backend.audio.dto.response.AudioFileInfoResponse;
import fakehunters.backend.audio.exception.AudioErrorCode;
import fakehunters.backend.audio.mapper.AudioAnalysisResultMapper;
import fakehunters.backend.audio.mapper.AudioFileMapper;
import fakehunters.backend.global.exception.custom.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AudioFileService {

    private final AudioFileMapper audioFileMapper;
    private final AudioAnalysisResultMapper audioAnalysisResultMapper;
    private final AudioStorageService audioStorageService;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    /**
     * Presigned 방식 업로드 후 DB 등록
     * (S3 → 메타데이터 추출 → INSERT)
     */
    @Transactional
    public Long uploadAudioFile(
            String s3Key,
            String fileName,
            Long fileSize
    ) {
        if (s3Key == null || s3Key.isBlank()) {
            throw new CustomBusinessException(AudioErrorCode.FILE_REQUIRED);
        }

        String s3Path = "s3://" + bucketName + "/" + s3Key;

        Path tempFile = null;
        try {
            // 1. S3 → temp file
            tempFile = audioStorageService.downloadToTempFile(s3Path);

            // 2. 메타데이터 추출
            AudioMetadata meta = AudioMetadataExtractor.extract(tempFile);

            // 3. DB INSERT (NOT NULL 컬럼 모두 채움)
            AudioFile audioFile = AudioFile.builder()
                    .fileName(fileName)
                    .filePath(s3Path)
                    .fileSize(fileSize)
                    .duration(BigDecimal.valueOf(meta.duration))
                    .sampleRate(meta.sampleRate)
                    .status("pending")
                    .build();

            audioFileMapper.insert(audioFile);
            return audioFile.getId();

        } catch (Exception e) {
            log.error("Audio upload failed", e);
            throw new CustomBusinessException(AudioErrorCode.UPLOAD_ERROR);
        } finally {
            // 4. temp file 정리
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception e) {
                    log.warn("Failed to delete temp audio file", e);
                }
            }
        }
    }

    public AudioFileInfoResponse getAudioFileInfo(Long audioFileId) {
        AudioFile audioFile = audioFileMapper.findById(audioFileId)
                .orElseThrow(() -> new CustomBusinessException(AudioErrorCode.NOT_FOUND));

        boolean hasAnalysis =
                audioAnalysisResultMapper.existsByAudioFileId(audioFileId);

        return AudioFileInfoResponse.builder()
                .id(audioFile.getId())
                .fileName(audioFile.getFileName())
                .fileSize(audioFile.getFileSize())
                .duration(audioFile.getDuration())
                .sampleRate(audioFile.getSampleRate())
                .status(audioFile.getStatus())
                .uploadedAt(audioFile.getUploadedAt())
                .hasAnalysis(hasAnalysis)
                .build();
    }

    public List<AudioFileInfoResponse> getAllAudioFiles() {
        return audioFileMapper.findAllOrderByCreatedAtDesc()
                .stream()
                .map(file -> AudioFileInfoResponse.builder()
                        .id(file.getId())
                        .fileName(file.getFileName())
                        .fileSize(file.getFileSize())
                        .duration(file.getDuration())
                        .sampleRate(file.getSampleRate())
                        .status(file.getStatus())
                        .uploadedAt(file.getUploadedAt())
                        .hasAnalysis(
                                audioAnalysisResultMapper.existsByAudioFileId(file.getId())
                        )
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateStatus(Long audioFileId, String status) {
        audioFileMapper.updateStatus(audioFileId, status);
    }

    @Transactional
    public void deleteAudioFile(Long audioFileId) {
        AudioFile audioFile = audioFileMapper.findById(audioFileId)
                .orElseThrow(() -> new CustomBusinessException(AudioErrorCode.NOT_FOUND));

        audioStorageService.deleteFile(audioFile.getFilePath());
        audioFileMapper.deleteById(audioFileId);
    }

    private static class AudioMetadataExtractor {

        static AudioMetadata extract(Path audioPath) {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "ffprobe",
                        "-v", "error",
                        "-select_streams", "a:0",
                        "-show_entries", "format=duration",
                        "-show_entries", "stream=sample_rate",
                        "-of", "json",
                        audioPath.toAbsolutePath().toString()
                );

                Process process = pb.start();

                String json;
                try (BufferedReader reader =
                             new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    json = reader.lines().collect(Collectors.joining());
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(json);

                double duration =
                        root.path("format").path("duration").asDouble(-1);

                int sampleRate =
                        root.path("streams").get(0).path("sample_rate").asInt(-1);

                if (duration <= 0 || sampleRate <= 0) {
                    throw new IllegalStateException("Invalid audio metadata: " + json);
                }

                return new AudioMetadata(duration, sampleRate);

            } catch (Exception e) {
                throw new IllegalStateException("Audio metadata extraction failed", e);
            }
        }
    }

    private record AudioMetadata(double duration, int sampleRate) {}
}
