package fakehunters.backend.audio.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AudioUploadResponse {

    private boolean success;
    private Long audioFileId;
    private String fileName;
    private String message;
}