package fakehunters.backend.image.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageResultDetailResponse {
    private String taskType;
    private String label;
    private Double confidence;
    private Integer riskScore;
    private String riskLevel;
    private String interpretation;
    private JsonNode evidence;
    private JsonNode warnings;
}
