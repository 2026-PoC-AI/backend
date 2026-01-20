package fakehunters.backend.image.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fakehunters.backend.global.exception.custom.CustomSystemException;
import fakehunters.backend.image.domain.ImageAnalysisResult;
import fakehunters.backend.image.dto.response.GeneratedReport;
import fakehunters.backend.image.exception.ImageErrorCode;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 실제 OpenAI API를 사용하여
 * 이미지 분석 결과를 기반으로
 * 최종 신뢰 리포트를 생성하는 Generator
 */
@Component
public class OpenAiImageReportGenerator implements ImageReportGenerator {

    private final ChatClient chatClient;
    private static final ObjectMapper om = new ObjectMapper();

    public OpenAiImageReportGenerator(ChatClient.Builder builder) {
        this.chatClient = builder.build();
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


        String prompt = """
        너는 디지털 포렌식 및 딥페이크 분석 분야의 전문가이다.
        아래에 제공된 이미지 분석 결과를 종합하여
        사용자가 이해하기 쉬운 '최종 이미지 신뢰 리포트'를 작성하라.

        ⚠️ 매우 중요한 규칙:
        - 반드시 JSON 형식으로만 응답하라.
        - JSON 이외의 자연어 설명은 절대 출력하지 마라.
        - 모든 출력 문장은 한국어로 작성하라.

        응답 JSON 형식은 반드시 아래 구조를 따라야 한다:

        {
          "overallRiskLevel": "LOW | MEDIUM | HIGH",
          "summary": "이미지의 신뢰도에 대한 1~2문장 요약 (한국어)",
          "guidance": [
            "사용자가 취해야 할 행동 가이드 1 (한국어)",
            "사용자가 취해야 할 행동 가이드 2 (한국어)"
          ]
        }

        각 필드 작성 가이드:
        - overallRiskLevel:
          전체 분석 결과를 종합하여 LOW, MEDIUM, HIGH 중 하나로 판단하라.
        - summary:
          일반 사용자가 이해할 수 있도록 과도한 기술 용어 없이 요약하라.
        - guidance:
          이미지 사용/공유/검증과 관련된 실질적인 행동 조언을 작성하라.

        아래는 이미지 분석 결과이다:
        %s
        """.formatted(analysisJson);

        try {
            String response =
                    chatClient.prompt(prompt)
                            .call()
                            .content();

            JsonNode json = om.readTree(response);

            return new GeneratedReport(
                    json.get("overallRiskLevel").asText(),
                    json.get("summary").asText(),
                    json.get("guidance")
            );

        } catch (Exception e) {
            throw new CustomSystemException(ImageErrorCode.LLM_RESPONSE_PARSE_FAILED);
        }
    }
}
