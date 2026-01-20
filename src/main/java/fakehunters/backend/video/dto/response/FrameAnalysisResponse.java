package fakehunters.backend.video.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class FrameAnalysisResponse {
    private Long frameId;
    private Integer frameNumber;
    private BigDecimal timestampSeconds;
    private Boolean isDeepfake;
    private BigDecimal confidenceScore;
    private String anomalyType;
    private String features;
}