package fakehunters.backend.audio.domain;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AudioFrequencyAnalysis {

    private Long id;
    private Long analysisResultId;
    private Integer frequencyBand;
    private BigDecimal anomalyScore;
    private Boolean isSuspicious;
    private LocalDateTime createdAt;
}
