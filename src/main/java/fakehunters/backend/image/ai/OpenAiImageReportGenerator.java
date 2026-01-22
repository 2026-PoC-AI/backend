package fakehunters.backend.image.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fakehunters.backend.global.exception.custom.CustomSystemException;
import fakehunters.backend.image.domain.ImageAnalysisResult;
import fakehunters.backend.image.dto.response.GeneratedReport;
import fakehunters.backend.image.exception.ImageErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 실제 OpenAI API를 사용하여
 * 이미지 분석 결과를 기반으로
 * 최종 신뢰 리포트를 생성하는 Generator
 */
@Slf4j
@Component
public class OpenAiImageReportGenerator implements ImageReportGenerator {

    private final ChatClient chatClient;
    private static final ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

    public OpenAiImageReportGenerator(ChatClient.Builder builder) {
        // ✅ PopSpot과 동일한 패턴: defaultSystem은 여기서 고정
        this.chatClient = builder
                .defaultSystem("""
                    너는 JSON 생성기다.
                    반드시 JSON 객체만 출력한다.
                    다른 텍스트는 절대 출력하지 않는다.
                """)
                .build();
    }

    @Override
    public GeneratedReport generate(List<ImageAnalysisResult> results) {

        if (results == null || results.isEmpty()) {
            throw new CustomSystemException(ImageErrorCode.ANALYSIS_NOT_READY);
        }

        String analysisJson;
        try {
            analysisJson = om.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(results);
        } catch (Exception e) {
            throw new CustomSystemException(ImageErrorCode.LLM_RESPONSE_PARSE_FAILED);
        }

        String userPrompt = """
            너는 디지털 포렌식 및 딥페이크 분석 분야의 전문가이다.
            아래에 제공된 이미지 분석 결과를 종합하여
            사용자가 이해하기 쉬운 '최종 이미지 신뢰 리포트'를 작성하라.

            ⚠️ 매우 중요한 규칙:
            - 반드시 JSON 형식으로만 응답하라.
            - JSON 이외의 자연어 설명은 절대 출력하지 마라.
            - 마크다운, 코드블록, 설명 문장 모두 금지한다.
            - 지금 즉시 JSON 객체만 출력하라.

            응답 JSON 형식은 반드시 아래 구조를 따라야 한다:

            {
              "overallRiskLevel": "LOW | MEDIUM | HIGH",
              "summary": "이미지의 신뢰도에 대한 300자 요약 (한국어)",
              "guidance": [
                "사용자가 취해야 할 행동 가이드 (한국어)",
                "사용자가 취해야 할 행동 가이드 (한국어)",
              ]
            }

            작성 가이드:
            - summary는 최대 1000자
            - guidance는 실제 행동 중심으로 작성
            - guidance는 최소 가이드 2개, 최대 가이드 10개

            아래는 이미지 분석 결과이다:
            %s
            """.formatted(analysisJson);

        String response = null;
        log.warn("🔥 About to call OpenAI ChatClient");

        try {
            log.warn("🔥 BEFORE ChatClient.call()");
            response = chatClient
                    .prompt()
                    .user(userPrompt)
                    .call()
                    .content();

            log.info("LLM raw response:\n{}", response);

            String jsonText = extractJsonObject(response);
            JsonNode json = om.readTree(jsonText);

            return new GeneratedReport(
                    json.get("overallRiskLevel").asText(),
                    json.get("summary").asText(),
                    json.get("guidance")
            );

        } catch (Exception e) {
                log.error("🔥 ChatClient FAILED", e);
                log.error("🔥 Exception class = {}", e.getClass().getName());
                log.error("🔥 Exception message = {}", e.getMessage());
                throw new CustomSystemException(ImageErrorCode.LLM_RESPONSE_PARSE_FAILED);
        }
    }

    /**
     * LLM 응답에서 첫 번째 JSON 객체를
     * 중괄호 카운팅 방식으로 안전하게 추출
     */
    private String extractJsonObject(String response) {

        if (response == null || response.isBlank()) {
            throw new CustomSystemException(ImageErrorCode.LLM_RESPONSE_PARSE_FAILED);
        }

        String cleaned = response
                .replaceAll("(?s)```json", "")
                .replaceAll("```", "")
                .trim();

        int braceCount = 0;
        int start = -1;

        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);

            if (c == '{') {
                if (braceCount == 0) start = i;
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0 && start != -1) {
                    return cleaned.substring(start, i + 1);
                }
            }
        }

        log.error("Invalid LLM response (no JSON object found):\n{}", response);
        throw new CustomSystemException(ImageErrorCode.LLM_RESPONSE_PARSE_FAILED);
    }
}
