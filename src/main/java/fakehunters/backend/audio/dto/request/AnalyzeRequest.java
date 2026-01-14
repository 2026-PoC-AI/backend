package fakehunters.backend.audio.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnalyzeRequest(
        @NotBlank String inputAudioS3Key,
        String inputAudioFormat,
        Double durationSec,

        @NotNull @Min(0) @Max(100) Integer riskScore,
        @NotBlank String grade,              // "LOW" | "MEDIUM" | "HIGH"

        String spectrogramS3Key
) {}
