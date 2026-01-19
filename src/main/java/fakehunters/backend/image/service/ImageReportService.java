package fakehunters.backend.image.service;

import fakehunters.backend.image.domain.ImageAnalysisReport;
import java.util.UUID;

public interface ImageReportService {
    ImageAnalysisReport generateFinalReport(UUID jobUuid);
    ImageAnalysisReport getFinalReport(UUID jobUuid);
}
