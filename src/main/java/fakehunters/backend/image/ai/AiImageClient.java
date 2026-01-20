package fakehunters.backend.image.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AiImageClient {

    private final RestClient aiClient;

    /**
     * 딥페이크 이미지 분석 요청
     * (fire-and-forget)
     */
    public void requestDeepfakeAnalysis(UUID jobUuid, String s3Key) {

        aiClient.post()
                .uri("/api/v1/images/analyze")
                .body(Map.of(
                        "job_uuid", jobUuid.toString(),
                        "s3_key", s3Key
                ))
                .retrieve()
                .toBodilessEntity();
    }
}
