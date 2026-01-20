package fakehunters.backend.image.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.UUID;

@Data
public class DeepfakeResultRequest {
    private UUID jobUuid;
    private String decision;
    private String riskLevel;
    private String message;
    private Double confidence; //AI confidence (0.0 ~ 1.0)
    private JsonNode rawResult; //FastAPI raw response
}
