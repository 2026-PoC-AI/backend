package fakehunters.backend.audio.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AnalyzeRequest(
        @NotBlank String inputAudioS3Key,
        String inputAudioFormat,
        Double durationSec,

        @Min(0) @Max(100) Integer riskScore,
        String grade,              // "LOW" | "MEDIUM" | "HIGH"

        String spectrogramS3Key
) {}
