package fakehunters.backend.video.domain;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class FrameAnalysis {
    private Long frameId;
    private Long resultId;        // FK
    private Integer frameNumber;
    private BigDecimal timestampSeconds;
    private Boolean isDeepfake;
    private BigDecimal confidenceScore;
    private String anomalyRegions;
    private String features;
}