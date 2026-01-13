package fakehunters.backend.video.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VideoFileResponse {
    private String fileId;
    private String originalFilename;
    private String storedFilename;
    private String filePath;
    private Long fileSize;
    private BigDecimal durationSeconds;
    private String resolution;
    private String format;
    private BigDecimal fps;
    private LocalDateTime uploadedAt;
    private String analysisId;
}