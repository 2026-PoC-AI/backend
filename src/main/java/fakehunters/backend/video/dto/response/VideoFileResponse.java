package fakehunters.backend.video.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
public class VideoFileResponse {
    private Long fileId;
    private Long analysisId;     // FK
    private String originalFilename;
    private String storedFilename;
    private String filePath;
    private Long fileSize;
    private BigDecimal durationSeconds;
    private String resolution;
    private String format;
    private BigDecimal fps;
    private OffsetDateTime uploadedAt;
}