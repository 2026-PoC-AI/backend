package fakehunters.backend.image.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ImageAnalyzeResponse {
    private UUID analysisId;
}
