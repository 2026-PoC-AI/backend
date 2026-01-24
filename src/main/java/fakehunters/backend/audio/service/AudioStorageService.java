package fakehunters.backend.audio.service;

import fakehunters.backend.audio.exception.AudioErrorCode;
import fakehunters.backend.global.exception.custom.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    /**
     * S3 PresignedUrl 가져오기
     */
    public String generatePresignedGetUrl(String s3Path) {
        try {
            String key = extractKey(s3Path);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(15))
                            .getObjectRequest(getObjectRequest)
                            .build();

            PresignedGetObjectRequest presignedRequest =
                    s3Presigner.presignGetObject(presignRequest);

            return presignedRequest.url().toString();

        } catch (Exception e) {
            throw new CustomBusinessException(AudioErrorCode.FILE_READ_ERROR);
        }
    }
    /**
     * S3 파일 삭제 (Presigned 업로드 이후에도 서버 권한으로 가능)
     */
    public void deleteFile(String s3Path) {
        try {
            String key = extractKey(s3Path);

            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build()
            );
        } catch (Exception e) {
            throw new CustomBusinessException(AudioErrorCode.DELETE_ERROR);
        }
    }

    /**
     * S3 파일을 임시 파일로 다운로드
     * (메타데이터 추출, ffprobe 용도)
     */
    public Path downloadToTempFile(String s3Path) {
        try {
            String key = extractKey(s3Path);

            Path tempFile = Files.createTempFile("audio-", ".tmp");

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            try (ResponseInputStream<GetObjectResponse> s3Input =
                         s3Client.getObject(request);
                 OutputStream fileOut = Files.newOutputStream(tempFile)) {

                s3Input.transferTo(fileOut);
            }

            log.debug("Downloaded S3 audio to temp file: {}", tempFile);
            return tempFile;

        } catch (Exception e) {
            log.error("Failed to download audio from S3: {}", s3Path, e);
            throw new CustomBusinessException(AudioErrorCode.DOWNLOAD_ERROR);
        }
    }

    /**
     * s3://bucket/key → key 추출
     */
    private String extractKey(String s3Path) {
        if (!s3Path.startsWith("s3://")) {
            throw new IllegalArgumentException("Invalid S3 path: " + s3Path);
        }
        return s3Path.replace("s3://" + bucketName + "/", "");
    }
}
