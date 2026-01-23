package fakehunters.backend.audio.domain;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AudioTimeSegmentAnalysis {

    private Long id;
    private Long analysisResultId;
    private BigDecimal startTime;
    private BigDecimal endTime;
    private String riskLevel; // high, medium, low
    private BigDecimal riskScore;
    private String reason;
    private List<String> indicators;
    private LocalDateTime createdAt;
}
