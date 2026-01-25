package fakehunters.backend.video.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FrameAnalysis {
    private Long frameId;
    private Long resultId;
    private Integer frameNumber;
    private Double timestampSeconds;
    private Boolean isDeepfake;
    private Double confidenceScore;
    private String anomalyRegions;
    private String features;
}