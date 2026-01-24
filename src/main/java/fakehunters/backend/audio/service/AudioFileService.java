package fakehunters.backend.audio.service;

import fakehunters.backend.audio.domain.AudioFile;
import fakehunters.backend.audio.dto.response.AudioFileInfoResponse;
import fakehunters.backend.audio.exception.AudioErrorCode;
import fakehunters.backend.audio.mapper.AudioFileMapper;
import fakehunters.backend.audio.mapper.AudioAnalysisResultMapper;
import fakehunters.backend.global.exception.custom.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Transactional
    public Long uploadAudioFile(MultipartFile file) {
        validateAudioFile(file);

        try {
            log.info("=== 파일 업로드 시작: {} ===", file.getOriginalFilename());

            String filePath = audioStorageService.uploadFile(file);
            log.info("1. S3 업로드 완료: {}", filePath);

            AudioStorageService.AudioMetadata metadata = audioStorageService.extractMetadata(file);
            log.info("2. 메타데이터 추출 완료: duration={}, sampleRate={}",
                    metadata.getDuration(), metadata.getSampleRate());

            AudioFile audioFile = AudioFile.builder()
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .duration(metadata.getDuration())
                    .sampleRate(metadata.getSampleRate())
                    .build();

            log.info("3. AudioFile 객체 생성 완료");

            audioFileMapper.insert(audioFile);
            log.info("4. DB INSERT 완료");

            Long generatedId = audioFile.getId();
            log.info("5. 생성된 ID: {}", generatedId);

            if (generatedId == null) {
                log.error("생성된 ID가 null입니다!");
                throw new CustomBusinessException(AudioErrorCode.UPLOAD_ERROR);
            }

            log.info("=== 파일 업로드 성공: audioFileId={} ===", generatedId);
            return generatedId;

        } catch (CustomBusinessException e) {
            log.error("CustomBusinessException 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 에러 발생", e);
            throw new CustomBusinessException(AudioErrorCode.UPLOAD_ERROR);
        }
    }

    public AudioFileInfoResponse getAudioFileInfo(Long audioFileId) {
        AudioFile audioFile = audioFileMapper.findById(audioFileId)
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

    public List<AudioFileInfoResponse> getAllAudioFiles() {
        List<AudioFile> audioFiles = audioFileMapper.findAllOrderByCreatedAtDesc();

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
    public void deleteAudioFile(Long audioFileId) {
        AudioFile audioFile = audioFileMapper.findById(audioFileId)
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