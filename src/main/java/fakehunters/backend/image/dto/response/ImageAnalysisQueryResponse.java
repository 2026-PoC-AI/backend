package fakehunters.backend.image.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ImageAnalysisQueryResponse {
    private JobInfo job;
    private InputInfo input;
    private List<ImageResultDetailResponse> results;

    @Data
    @AllArgsConstructor
    public static class JobInfo {
        private UUID jobUuid;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    @AllArgsConstructor
    public static class InputInfo {
        private String filename;
        private String s3Key;
        private String mimeType;
        private Long fileSize;
    }
}
