package fakehunters.backend.audio.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AudioFile {

    private Long id;
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