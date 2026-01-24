package fakehunters.backend.audio.service;

import fakehunters.backend.audio.dto.response.FastApiAudioResponse;
import fakehunters.backend.audio.exception.AudioErrorCode;
import fakehunters.backend.global.exception.custom.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioFastApiClient {

    private final RestTemplate restTemplate;
    private final S3Client s3Client;

    @Value("${ai.audio.api-url:http://localhost:8000}")
    private String fastApiUrl;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public FastApiAudioResponse analyzeAudio(String s3FilePath) {
        try {
            String url = fastApiUrl + "/api/v1/audio/analyze";

            log.info("FastAPI 호출: {}", url);
            log.info("S3 파일 경로: {}", s3FilePath);

            // S3에서 파일 다운로드
            String s3Key = s3FilePath.replace("s3://" + bucketName + "/", "");
            log.info("S3 Key: {}", s3Key);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            byte[] fileBytes = s3Object.readAllBytes();
            String fileName = s3Key.substring(s3Key.lastIndexOf("/") + 1);

            log.info("S3 파일 다운로드 완료: {} bytes, fileName: {}", fileBytes.length, fileName);

            // ByteArrayResource로 변환
            ByteArrayResource resource = new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };

            // Multipart 요청 생성
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // FastAPI 호출
            log.info("FastAPI 요청 전송...");
            ResponseEntity<FastApiAudioResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    FastApiAudioResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("분석 완료: prediction={}, confidence={}",
                        response.getBody().getPrediction(),
                        response.getBody().getConfidence());
                return response.getBody();
            } else {
                log.error("FastAPI 응답 오류: {}", response.getStatusCode());
                throw new CustomBusinessException(AudioErrorCode.DETECTION_FAILED);
            }

        } catch (IOException e) {
            log.error("S3 파일 읽기 실패", e);
            throw new CustomBusinessException(AudioErrorCode.FILE_READ_ERROR);
        } catch (Exception e) {
            log.error("FastAPI 호출 실패", e);
            throw new CustomBusinessException(AudioErrorCode.AI_SERVER_ERROR);
        }
    }
}