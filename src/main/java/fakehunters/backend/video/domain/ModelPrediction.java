package fakehunters.backend.video.domain;

import lombok.Builder;
import lombok.Getter;
import java.time.OffsetDateTime;

@Getter
@Builder
public class ModelPrediction {
    private Long predictionId;
    private Long resultId;
    private String modelName;  // xception, efficientnet, cnn_lstm
    private String prediction;  // fake, real
    private Double confidence;
    private Double fakeProbability;
    private String detectedPatterns;  // JSON string
    private String suspiciousFrames;  // JSON string (CNN-LSTM only)
    private String attentionWeights;  // JSON string (CNN-LSTM only)
    private OffsetDateTime createdAt;
}