package fakehunters.backend.image.domain;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ImageAnalysisResult {
    private Long resultId;
    private UUID resultUuid;
    private Long jobId;

    private String resultTaskType;
    private String resultLabel;
    private Double resultConfidence;

    private Integer resultRiskScore;
    private String resultRiskLevel;
    private String resultInterpretation;

    private JsonNode resultEvidence;
    private JsonNode resultWarnings;
    private JsonNode resultJson;

    private LocalDateTime createdAt;
}
