package fakehunters.backend.audio.controller;

import fakehunters.backend.audio.dto.request.AnalyzeRequest;
import fakehunters.backend.audio.dto.response.AnalyzeResponse;
import fakehunters.backend.audio.service.AudioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/audio")
public class AudioController {

    private final AudioService audioService;

    // POST /api/v1/audio/analyze
    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeResponse> analyze(
            @Valid @RequestBody AnalyzeRequest request
    ) {
        return ResponseEntity.ok(audioService.analyze(request));
    }
}
