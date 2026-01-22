package fakehunters.backend.image.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageArtifactResponse {
    private String stage;
    private String type;
    private String url;
    private Integer index;
}
