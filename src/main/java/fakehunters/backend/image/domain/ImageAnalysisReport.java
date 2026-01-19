package fakehunters.backend.image.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 이미지 분석 결과를 바탕으로 생성된
 * 최종 신뢰 리포트 (LLM 요약 결과)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageAnalysisReport {

    private Long reportId;
    private UUID reportUuid;
    private Long jobId;

    private String reportOverallRiskLevel;
    private String reportSummary;
    private JsonNode reportGuidance;

    private LocalDateTime createdAt;
}
