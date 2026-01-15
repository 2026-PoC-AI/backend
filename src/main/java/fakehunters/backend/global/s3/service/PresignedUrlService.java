package fakehunters.backend.global.s3.service;

import fakehunters.backend.global.s3.path.S3PathGenerator;
import fakehunters.backend.global.s3.dto.response.PresignedUploadResponse;
import fakehunters.backend.global.s3.dto.response.PresignedDownloadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PresignedUrlService {

    private final S3Presigner s3Presigner;
    private final S3PathGenerator pathGenerator;

    @Value("${aws.s3.bucket}")
    private String bucket;

    /**
     * Presigned PUT URL 생성 (업로드용)
     */
    public PresignedUploadResponse generateUploadUrl(
            String domain,
            String stage,
            String extension,
            String contentType
    ) {
        String key = pathGenerator.generate(domain, stage, extension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .putObjectRequest(putObjectRequest)
                        .build();

        String presignedUrl = s3Presigner
                .presignPutObject(presignRequest)
                .url()
                .toString();

        return new PresignedUploadResponse(
                presignedUrl,
                key,
                300
        );
    }

    /**
     * Presigned GET URL 생성 (다운로드/조회용)
     */
    public PresignedDownloadResponse generateDownloadUrl(String key) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .getObjectRequest(getObjectRequest)
                        .build();

        String presignedUrl = s3Presigner
                .presignGetObject(presignRequest)
                .url()
                .toString();

        return new PresignedDownloadResponse(
                presignedUrl,
                300
        );
    }
}
