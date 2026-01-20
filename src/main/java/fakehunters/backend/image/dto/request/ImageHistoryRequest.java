package fakehunters.backend.image.dto.request;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ImageHistoryRequest {
    private List<UUID> jobUuids;
}
