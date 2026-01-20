package fakehunters.backend.image.controller;


import fakehunters.backend.image.dto.request.DeepfakeResultRequest;
import fakehunters.backend.image.dto.response.DeepfakeResultResponse;
import fakehunters.backend.image.service.ImageAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/images/results")
@RequiredArgsConstructor
public class ImageResultController {
    private final ImageAnalysisService imageAnalysisService;

    @PostMapping("/deepfake")
    public DeepfakeResultResponse saveDeepfakeResult(
            @RequestBody DeepfakeResultRequest request
    ) {
        return imageAnalysisService.saveDeepfakeResult(request);
    }
}
