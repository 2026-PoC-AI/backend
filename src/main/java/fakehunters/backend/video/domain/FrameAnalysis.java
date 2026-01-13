package fakehunters.backend.video.domain;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class FrameAnalysis {
    private String frameId;
    private String resultId;        // FK
    private Integer frameNumber;
    private BigDecimal timestampSeconds;
    private Boolean isDeepfake;
    private BigDecimal confidenceScore;
    private String anomalyType;
    private String features;        // JSON 문자열
}
