package fakehunters.backend.audio.domain;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class AudioSystemSettings {

    private String key;
    private String value;
    private String description;
    private LocalDateTime updatedAt;
}

