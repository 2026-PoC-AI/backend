package fakehunters.backend.video.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoFile {
    private Long fileId;
    private Long analysisId;
    private String originalFilename;
    private String storedFilename;
    private String filePath;
    private String webFilePath;
    private Long fileSize;
    private BigDecimal durationSeconds;
    private String resolution;
    private String format;
    private BigDecimal fps;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]", timezone = "Asia/Seoul")
    private OffsetDateTime uploadedAt;
}