package fakehunters.backend.video.domain;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class VideoFile {
    private Long fileId;
    private Long analysisId;      // FK
    private String originalFilename;
    private String storedFilename;
    private String filePath;
    private Long fileSize;
    private BigDecimal durationSeconds;
    private String resolution;
    private String format;
    private BigDecimal fps;
    private LocalDateTime uploadedAt;
}
