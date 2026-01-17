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
public class ImageAnalysisInput {
    private Long inputId;
    private UUID inputUuid;
    private Long jobId;

    private String inputFilename;
    private String inputS3Key;
    private Long inputFilesize;
    private String inputMimeType;

    private LocalDateTime createdAt;
}
