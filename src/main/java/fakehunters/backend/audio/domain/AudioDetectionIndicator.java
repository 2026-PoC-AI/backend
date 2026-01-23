package fakehunters.backend.audio.domain;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AudioDetectionIndicator {

    private Long id;
    private Long analysisResultId;
    private String indicatorType; // frequency, phase, prosody, formant
    private String description;
    private String severity; // critical, warning, info
    private BigDecimal confidence;
    private BigDecimal timeStart;
    private BigDecimal timeEnd;
    private LocalDateTime createdAt;
}