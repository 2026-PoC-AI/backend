package fakehunters.backend.text.controller;

import fakehunters.backend.text.dto.request.TextAnalyzeRequest;
import fakehunters.backend.text.dto.response.TextAnalyzeResponse;
import fakehunters.backend.text.service.TextAnalysisService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/text")
public class TextAnalysisController {

    private final TextAnalysisService service;

    public TextAnalysisController(TextAnalysisService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public TextAnalyzeResponse analyze(
            @Valid @RequestBody TextAnalyzeRequest req,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId
    ) {
        return service.analyze(req, requestId);
    }
}
