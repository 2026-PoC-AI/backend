package fakehunters.backend.audio.domain;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AudioModelPrediction {

    private Long id;
    private Long analysisResultId;
    private String modelName; // mel_cnn, lfcc_cnn, rawnet
    private String modelType; // spectrogram, frequency, raw_waveform
    private BigDecimal realProbability;
    private BigDecimal fakeProbability;
    private String prediction;
    private BigDecimal confidence;
    private LocalDateTime createdAt;
}
