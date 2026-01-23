package fakehunters.backend.audio.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AudioDeleteResponse {

    private boolean success;
    private String message;
}