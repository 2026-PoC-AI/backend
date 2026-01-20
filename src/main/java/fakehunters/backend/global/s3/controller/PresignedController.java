package fakehunters.backend.global.s3.controller;

import fakehunters.backend.global.s3.dto.request.PresignedUploadRequest;
import fakehunters.backend.global.s3.dto.response.PresignedUploadResponse;
import fakehunters.backend.global.s3.dto.response.PresignedDownloadResponse;
import fakehunters.backend.global.s3.service.PresignedUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/s3/presign")
@RequiredArgsConstructor
public class PresignedController {

    private final PresignedUrlService presignedUrlService;

    /**
     * 업로드용 Presigned PUT URL 발급
     */
    @PostMapping("/upload")
    public ResponseEntity<PresignedUploadResponse> presignUpload(
            @RequestBody PresignedUploadRequest request
    ) {
        PresignedUploadResponse response =
                presignedUrlService.generateUploadUrl(
                        request.getDomain(),
                        request.getStage(),
                        request.getExtension(),
                        request.getContentType()
                );

        return ResponseEntity.ok(response);
    }

    /**
     * 다운로드/조회용 Presigned GET URL 발급
     */
    @GetMapping("/download")
    public ResponseEntity<PresignedDownloadResponse> presignDownload(
            @RequestParam("key") String key
    ) {
        PresignedDownloadResponse response =
                presignedUrlService.generateDownloadUrl(key);

        return ResponseEntity.ok(response);
    }
}
