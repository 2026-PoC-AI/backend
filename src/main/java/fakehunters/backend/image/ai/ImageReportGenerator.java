package fakehunters.backend.image.ai;

import fakehunters.backend.image.domain.ImageAnalysisResult;
import fakehunters.backend.image.dto.response.GeneratedReport;

import java.util.List;
public interface ImageReportGenerator {
    GeneratedReport generate(List<ImageAnalysisResult> results);
}
