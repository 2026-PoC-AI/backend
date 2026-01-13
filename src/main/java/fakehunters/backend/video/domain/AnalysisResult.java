package fakehunters.backend.video.domain;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AnalysisResult {
    private String resultId;
    private String analysisId;      // FK
    private LocalDateTime createdAt;
    private BigDecimal confidenceScore;
    private Boolean isDeepfake;
    private String modelVersion;
    private Long processingTimeMs;
    private String detectedTechniques;
    private String summary;
    private LocalDateTime analyzedAt;
}
