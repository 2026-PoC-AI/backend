package fakehunters.backend.image.controller;

import fakehunters.backend.image.dto.request.ImageAnalyzeRequest;
import fakehunters.backend.image.dto.response.ImageAnalysisQueryResponse;
import fakehunters.backend.image.dto.response.ImageAnalyzeResponse;
import fakehunters.backend.image.service.ImageAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


//이미지 분석 요청을 처리
@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageAnalysisController {
    private final ImageAnalysisService imageAnalysisService;

    @PostMapping("/analyze")
    public ImageAnalyzeResponse analyzeImage(
            @RequestBody ImageAnalyzeRequest request
    ) {
        return imageAnalysisService.createAnalysis(request);
    }

    @GetMapping("/{jobUuid}")
    public ImageAnalysisQueryResponse getImageAnalysisResult(
            @PathVariable UUID jobUuid
    ) {
        return imageAnalysisService.getAnalysisResult(jobUuid);
    }
}
