package fakehunters.backend.video.domain;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AnalysisResult {
    private Long resultId;          // BIGSERIAL
    private Long analysisId;        // FK
    private Boolean isDeepfake;
    private BigDecimal confidenceScore;
    private String modelVersion;
    private Long processingTimeMs;
    private String detectedTechniques; // MyBatis Handler를 통해 String 혹은 List로 처리
    private String summary;
    private LocalDateTime analyzedAt;
}