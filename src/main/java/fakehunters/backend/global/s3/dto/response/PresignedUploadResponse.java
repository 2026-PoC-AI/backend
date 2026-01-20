package fakehunters.backend.global.s3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUploadResponse {
    private String url; //Presigned PUT URL
    private String key; //S3 object key (DB 저장용)
    private int expiresIn; //URL 만료 시간 (초)
}
