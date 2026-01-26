package fakehunters.backend.video.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DetectedArtifact {
    private Long artifactId;
    private Long resultId;
    private String artifactType;  // spatial, temporal, structural
    private Boolean detected;
    private String sources;  // JSON array
    private String patterns;  // JSON array
}