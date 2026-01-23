package fakehunters.backend.audio.domain;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AudioAnalysisResult {

    private Long id;
    private Long audioFileId;
    private String prediction; // real, fake
    private BigDecimal confidence;
    private BigDecimal realProbability;
    private BigDecimal fakeProbability;
    private String suspectedMethod; // TTS, Voice Conversion, Replay Attack
    private BigDecimal methodConfidence;
    private BigDecimal processingTime;
    private LocalDateTime analyzedAt;
    private String modelVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
