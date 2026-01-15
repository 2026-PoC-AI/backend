package fakehunters.backend.global.s3.dto.request;

import lombok.Getter;

@Getter
public class PresignedUploadRequest {
    private String domain; //image | audio | video | text
    private String stage; //inputs | processed | result
    private String extension; //파일 확장자 (png, jpg, mp3, mp4, json ...)
    private String contentType; //MIME type (image/png, audio/mpeg, video/mp4 ...)
}
