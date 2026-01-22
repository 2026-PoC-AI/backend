package fakehunters.backend.image.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class ImageArtifactItemRequest {
    private String artifactStage;
    private String artifactType;
    private String s3Key;
    private JsonNode meta; // meta는 DB에 저장 안 하더라도 추후 확장 가능
}
