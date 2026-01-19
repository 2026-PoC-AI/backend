package fakehunters.backend.image.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * LLM 또는 Mock Generator가 생성한
 * 최종 이미지 신뢰 리포트 결과 DTO
 */
@Data
@AllArgsConstructor
public class GeneratedReport {
    private String overallRiskLevel; // 전체 위험도 (LOW / MEDIUM / HIGH)
    private String summary; // 사용자에게 보여줄 요약 문장
    private JsonNode guidance; //행동 가이드 (JSON 배열)
}
