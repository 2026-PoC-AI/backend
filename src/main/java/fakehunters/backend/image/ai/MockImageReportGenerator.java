package fakehunters.backend.image.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import fakehunters.backend.image.domain.ImageAnalysisResult;
import fakehunters.backend.image.dto.response.GeneratedReport;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"default", "local", "test"})
public class MockImageReportGenerator implements ImageReportGenerator {

    private static final ObjectMapper om = new ObjectMapper();

    @Override
    public GeneratedReport generate(List<ImageAnalysisResult> results) {

        int maxRisk =
                results.stream()
                        .mapToInt(ImageAnalysisResult::getResultRiskScore)
                        .max()
                        .orElse(0);

        String riskLevel =
                maxRisk >= 70 ? "HIGH" :
                        maxRisk >= 40 ? "MEDIUM" : "LOW";

        String summary =
                "Based on the analysis results, this image shows a "
                        + riskLevel
                        + " likelihood of being manipulated or AI-generated.";

        return new GeneratedReport(
                riskLevel,
                summary,
                om.createArrayNode()
                        .add("Do not trust this image without verification.")
                        .add("Check the source before sharing.")
        );
    }
}
