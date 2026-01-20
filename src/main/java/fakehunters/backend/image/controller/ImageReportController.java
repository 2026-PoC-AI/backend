package fakehunters.backend.image.controller;

import fakehunters.backend.image.dto.response.ImageFinalReportResponse;
import fakehunters.backend.image.domain.ImageAnalysisReport;
import fakehunters.backend.image.service.ImageReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/images/{jobUuid}/report")
@RequiredArgsConstructor
public class ImageReportController {

    private final ImageReportService imageReportService;

    /**
     * 최종 리포트 생성 (Mock or OpenAI Profile)
     * - 이미 생성된 리포트가 있으면 그대로 반환(중복 insert 방지)
     */
    @PostMapping
    public ImageFinalReportResponse generateReport(
            @PathVariable UUID jobUuid
    ) {
        ImageAnalysisReport report = imageReportService.generateFinalReport(jobUuid);
        return ImageFinalReportResponse.from(jobUuid, report);
    }

    /**
     * 최종 리포트 조회
     */
    @GetMapping
    public ImageFinalReportResponse getReport(
            @PathVariable UUID jobUuid
    ) {
        ImageAnalysisReport report = imageReportService.getFinalReport(jobUuid);
        return ImageFinalReportResponse.from(jobUuid, report);
    }
}
