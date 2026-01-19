package fakehunters.backend.image.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import fakehunters.backend.image.domain.ImageAnalysisReport;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 프론트에서 바로 쓰기 좋은 형태로 내려주는 최종 리포트 응답 DTO
 */
@Data
@AllArgsConstructor
public class ImageFinalReportResponse {

    private UUID jobUuid;
    private UUID reportUuid;

    private String overallRiskLevel;
    private String summary;
    private JsonNode guidance;

    private LocalDateTime createdAt;

    public static ImageFinalReportResponse from(UUID jobUuid, ImageAnalysisReport report) {
        if (report == null) return null;

        return new ImageFinalReportResponse(
                jobUuid,
                report.getReportUuid(),
                report.getReportOverallRiskLevel(),
                report.getReportSummary(),
                report.getReportGuidance(),
                report.getCreatedAt()
        );
    }
}
