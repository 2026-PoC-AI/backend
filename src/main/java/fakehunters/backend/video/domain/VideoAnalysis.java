package fakehunters.backend.video.domain;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class VideoAnalysis {
    private String analysisId;      // PK, BIGSERIAL → String으로 UUID 사용
    private String title;
    private String status;          // PENDING/PROCESSING/COMPLETED/FAILED
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
