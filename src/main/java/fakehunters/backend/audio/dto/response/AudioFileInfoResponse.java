package fakehunters.backend.audio.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AudioFileInfoResponse {

    private Long id;
    private String fileName;
    private Long fileSize;
    private BigDecimal duration;
    private Integer sampleRate;
    private String status;
    private LocalDateTime uploadedAt;
    private boolean hasAnalysis;
}