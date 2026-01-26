package fakehunters.backend.video.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalysisResultResponse {
    private Long resultId;
    private Long analysisId;
    private OffsetDateTime createdAt;
    private Double confidenceScore;
    private Boolean isDeepfake;
    private String modelVersion;
    private Long processingTimeMs;
    private String detectedTechniques;
    private String summary;
    private OffsetDateTime analyzedAt;

    // 앙상블 정보
    private Double ensembleFakeProbability;
    private Double modelAgreement;
    private String riskLevel;

    // 개별 모델 결과
    private IndividualModelsResponse individualModels;

    // 탐지된 아티팩트
    private DetectedArtifactsResponse detectedArtifacts;

    @Data
    @Builder
    public static class IndividualModelsResponse {
        private ModelPredictionResponse xception;
        private ModelPredictionResponse efficientnet;

        @JsonProperty("cnn_lstm")
        private ModelPredictionResponse cnnLstm;
    }

    @Data
    @Builder
    public static class ModelPredictionResponse {
        private String modelName;
        private String prediction;
        private Double confidence;
        private Double fakeProbability;
        private List<String> detectedPatterns;
        private List<Integer> suspiciousFrames;  // CNN-LSTM only
    }

    @Data
    @Builder
    public static class DetectedArtifactsResponse {
        private ArtifactCategoryResponse spatial;
        private ArtifactCategoryResponse temporal;
        private ArtifactCategoryResponse structural;
    }

    @Data
    @Builder
    public static class ArtifactCategoryResponse {
        private Boolean detected;
        private List<String> sources;
        private List<String> patterns;
    }
}