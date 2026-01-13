package fakehunters.backend.video.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AnalysisResultResponse {
    private String resultId;
    private String analysisId;
    private LocalDateTime createdAt;
    private BigDecimal confidenceScore;
    private Boolean isDeepfake;
    private String modelVersion;
    private Long processingTimeMs;
    private String detectedTechniques;
    private String summary;
    private LocalDateTime analyzedAt;
}
