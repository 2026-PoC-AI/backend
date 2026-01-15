package fakehunters.backend.global.s3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedDownloadResponse {
    private String url; //Presigned GET URL
    private int expiresIn; //URL 만료 시간 (초)
}
