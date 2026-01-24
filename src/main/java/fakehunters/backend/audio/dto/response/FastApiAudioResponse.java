package fakehunters.backend.audio.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FastApiAudioResponse {

    @JsonProperty("analysis_id")
    private Long analysisId;

    private String prediction;

    private BigDecimal confidence;

    private Map<String, BigDecimal> probabilities;

    @JsonProperty("model_outputs")
    private Map<String, Map<String, BigDecimal>> modelOutputs;

    @JsonProperty("model_version")
    private String modelVersion;

    @JsonProperty("processing_time")
    private BigDecimal processingTime;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("file_size")
    private Long fileSize;

    private String status;

    @JsonProperty("suspected_method")
    private String suspectedMethod;

    @JsonProperty("method_confidence")
    private BigDecimal methodConfidence;

    @JsonProperty("detailed_analysis")
    private Map<String, BigDecimal> detailedAnalysis;

    @JsonProperty("suspicious_patterns")
    private List<String> suspiciousPatterns;

    @JsonProperty("time_segments")
    private List<TimeSegment> timeSegments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSegment {
        private BigDecimal start;
        private BigDecimal end;
        private String risk;
        private String reason;
    }
}