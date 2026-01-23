package fakehunters.backend.audio.service;

import fakehunters.backend.audio.domain.AudioFile;
import fakehunters.backend.audio.dto.response.AudioFileInfoResponse;
import fakehunters.backend.audio.exception.AudioErrorCode;
import fakehunters.backend.audio.mapper.AudioFileMapper;
import fakehunters.backend.audio.mapper.AudioAnalysisResultMapper;
import fakehunters.backend.global.exception.custom.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AudioFileService {

    private final AudioFileMapper audioFileMapper;
    private final AudioAnalysisResultMapper audioAnalysisResultMapper;
    private final AudioStorageService audioStorageService;

    @Transactional
    public Long uploadAudioFile(MultipartFile file, Long userId) {
        validateAudioFile(file);

        try {
            String filePath = audioStorageService.uploadFile(file, userId);
            AudioStorageService.AudioMetadata metadata = audioStorageService.extractMetadata(file);

            AudioFile audioFile = AudioFile.builder()
                    .userId(userId)
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .duration(metadata.getDuration())
                    .sampleRate(metadata.getSampleRate())
                    .build();

            audioFileMapper.insert(audioFile);

            return audioFile.getId();

        } catch (CustomBusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomBusinessException(AudioErrorCode.UPLOAD_ERROR);
        }
    }

    public AudioFileInfoResponse getAudioFileInfo(Long audioFileId, Long userId) {
        AudioFile audioFile = audioFileMapper.findByIdAndUserId(audioFileId, userId)
                .orElseThrow(() -> new CustomBusinessException(AudioErrorCode.NOT_FOUND));

        boolean hasAnalysis = audioAnalysisResultMapper.existsByAudioFileId(audioFileId);

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

    public List<AudioFileInfoResponse> getUserAudioFiles(Long userId) {
        List<AudioFile> audioFiles = audioFileMapper.findByUserIdOrderByCreatedAtDesc(userId);

        return audioFiles.stream()
                .map(audioFile -> {
                    boolean hasAnalysis = audioAnalysisResultMapper.existsByAudioFileId(audioFile.getId());
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
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateStatus(Long audioFileId, String status) {
        audioFileMapper.updateStatus(audioFileId, status);
    }

    @Transactional
    public void deleteAudioFile(Long audioFileId, Long userId) {
        AudioFile audioFile = audioFileMapper.findByIdAndUserId(audioFileId, userId)
                .orElseThrow(() -> new CustomBusinessException(AudioErrorCode.NOT_FOUND));

        try {
            audioStorageService.deleteFile(audioFile.getFilePath());
            audioFileMapper.deleteById(audioFileId);
        } catch (Exception e) {
            throw new CustomBusinessException(AudioErrorCode.DELETE_ERROR);
        }
    }

    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomBusinessException(AudioErrorCode.FILE_REQUIRED);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new CustomBusinessException(AudioErrorCode.INVALID_FILE_FORMAT);
        }

        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            throw new CustomBusinessException(AudioErrorCode.FILE_SIZE_EXCEEDED);
        }
    }
}