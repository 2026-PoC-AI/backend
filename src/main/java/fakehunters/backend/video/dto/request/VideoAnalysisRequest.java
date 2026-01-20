package fakehunters.backend.video.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoAnalysisRequest {
    private String title;
    private String description;
    private Integer maxFrames;
    private Boolean enableDetailedAnalysis;
}
