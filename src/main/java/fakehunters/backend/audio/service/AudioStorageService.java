package fakehunters.backend.audio.service;

import fakehunters.backend.audio.exception.AudioErrorCode;
import fakehunters.backend.global.exception.custom.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AudioStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file, Long userId) {
        String fileName = generateFileName(file.getOriginalFilename(), userId);
        String key = "audio/" + userId + "/" + fileName;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return "s3://" + bucketName + "/" + key;

        } catch (IOException e) {
            throw new CustomBusinessException(AudioErrorCode.UPLOAD_ERROR);
        }
    }

    public void deleteFile(String filePath) {
        try {
            String key = filePath.replace("s3://" + bucketName + "/", "");

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new CustomBusinessException(AudioErrorCode.DELETE_ERROR);
        }
    }

    public AudioMetadata extractMetadata(MultipartFile file) {
        // TODO: 실제 오디오 파일에서 메타데이터 추출
        return new AudioMetadata(
                new BigDecimal("5.23"),
                16000
        );
    }

    private String generateFileName(String originalFilename, Long userId) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    public static class AudioMetadata {
        private final BigDecimal duration;
        private final Integer sampleRate;

        public AudioMetadata(BigDecimal duration, Integer sampleRate) {
            this.duration = duration;
            this.sampleRate = sampleRate;
        }

        public BigDecimal getDuration() {
            return duration;
        }

        public Integer getSampleRate() {
            return sampleRate;
        }
    }
}