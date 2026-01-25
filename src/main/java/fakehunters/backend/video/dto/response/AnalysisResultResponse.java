package fakehunters.backend.video.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class AnalysisResultResponse {
    private Long resultId;
    private Long analysisId;
    private OffsetDateTime createdAt;
    private Double confidenceScore;
    private Boolean isDeepfake;
    private String modelVersion;
    private Long processingTimeMs;
    private String detectedTechniques;
    private String summary;
    private OffsetDateTime analyzedAt;
}
