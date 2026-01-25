package fakehunters.backend.text.service;

import fakehunters.backend.text.dto.external.AiAnalyzeRequest;
import fakehunters.backend.text.dto.external.AiAnalyzeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TextAiClient {

    private final RestClient client;

    public TextAiClient(@Value("${ai.server.url}") String baseUrl) {
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public AiAnalyzeResponse analyze(AiAnalyzeRequest req) {
        return client.post()
                .uri("/text/analyze")
                .body(req)
                .retrieve()
                .body(AiAnalyzeResponse.class);
    }
}
