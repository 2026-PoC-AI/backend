package fakehunters.backend.audio.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
public class AudioUploadRequest {

    private MultipartFile file;
    private Long userId;
}