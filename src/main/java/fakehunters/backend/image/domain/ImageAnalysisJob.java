package fakehunters.backend.image.domain;

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
public class ImageAnalysisJob {
    private Long jobId;
    private UUID jobUuid;
    private String jobStatus;
    private LocalDateTime createdAt;
}
