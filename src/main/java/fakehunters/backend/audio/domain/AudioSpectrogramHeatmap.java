package fakehunters.backend.audio.domain;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class AudioSpectrogramHeatmap {

    private Long id;
    private Long analysisResultId;
    private String heatmapUrl;
    private String heatmapType; // mel, lfcc, attention
    private LocalDateTime createdAt;
}
