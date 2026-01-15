package fakehunters.backend.audio.dto.external;

import java.util.List;

// 응답 DTO (FastAPI -> Spring)
// FastAPI의 ApiResponse<AudioAnalyzeResponse> 구조를 받기 위함
public record AiAudioResponseWrapper(
        boolean success,
        AiAudioData data,
        Object error
) {
    public record AiAudioData(
            int risk_score,
            String grade,
            List<AiEvidence> evidence,
            List<String> warnings
    ) {}

    public record AiEvidence(
            float score,
            String reason
    ) {}
}