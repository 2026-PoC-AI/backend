package fakehunters.backend.image.dto.request;

import lombok.Data;

@Data
public class ImageAnalyzeRequest {
    private String task;
    private String s3Key;
    private String filename;
    private Long fileSize;
    private String mimeType;
}
