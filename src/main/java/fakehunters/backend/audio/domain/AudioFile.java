package fakehunters.backend.audio.domain;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AudioFile {

    private Long id;
    private Long userId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private BigDecimal duration;
    private Integer sampleRate;
    private LocalDateTime uploadedAt;
    private String status; // pending, processing, completed, failed
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}