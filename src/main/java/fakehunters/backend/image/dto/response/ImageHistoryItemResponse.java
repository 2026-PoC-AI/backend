package fakehunters.backend.image.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ImageHistoryItemResponse {
    private UUID jobUuid;
    private String filename;
    private String jobStatus;
    private String overallRiskLevel;
    private LocalDateTime createdAt;
}
