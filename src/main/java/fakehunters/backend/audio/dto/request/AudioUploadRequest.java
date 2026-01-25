package fakehunters.backend.audio.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AudioUploadRequest {
    private String s3Key;
    private String fileName;
    private Long fileSize;
    private String contentType;
}