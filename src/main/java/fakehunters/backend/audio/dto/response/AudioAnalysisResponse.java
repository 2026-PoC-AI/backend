package fakehunters.backend.audio.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AudioAnalysisResponse {

    private Long audioFileId;
    private String prediction;
    private BigDecimal confidence;
    private BigDecimal realProbability;
    private BigDecimal fakeProbability;
    private String suspectedMethod;
    private BigDecimal methodConfidence;
    private String modelVersion;
    private BigDecimal processingTime;
    private List<ModelVote> modelVotes;
    private List<SuspiciousSegment> suspiciousSegments;
    private List<DetectionReason> reasons;
    private List<HeatmapInfo> heatmaps;

    @Getter
    @Builder
    public static class ModelVote {
        private String modelName;
        private String modelType;
        private BigDecimal realProbability;
        private BigDecimal fakeProbability;
        private String prediction;
    }

    @Getter
    @Builder
    public static class SuspiciousSegment {
        private BigDecimal startTime;
        private BigDecimal endTime;
        private String riskLevel;
        private BigDecimal riskScore;
        private String reason;
        private List<String> indicators;
    }

    @Getter
    @Builder
    public static class DetectionReason {
        private String type;
        private String description;
        private String severity;
        private BigDecimal confidence;
        private BigDecimal timeStart;
        private BigDecimal timeEnd;
    }

    @Getter
    @Builder
    public static class HeatmapInfo {
        private String heatmapUrl;
        private String heatmapType;
    }
}