package fakehunters.backend.video.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FrameAnalysisResponse {
    private Long frameId;
    private Integer frameNumber;
    private Double timestampSeconds;
    private Boolean isDeepfake;
    private Double confidenceScore;
    private String anomalyType;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)  // 빈 값이면 응답에서 제외
    private String features;
}