package fakehunters.backend.audio.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AudioAnalysisStartResponse {

    private boolean success;
    private Long analysisResultId;
    private String status;
    private String message;
}