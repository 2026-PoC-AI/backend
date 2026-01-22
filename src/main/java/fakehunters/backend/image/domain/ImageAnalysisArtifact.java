package fakehunters.backend.image.domain;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageAnalysisArtifact {
    private Long artifactId;
    private UUID artifactUuid;
    private Long jobId;
    private String artifactStage;
    private String artifactType;
    private String artifactS3Key;
    private LocalDateTime createdAt;
}