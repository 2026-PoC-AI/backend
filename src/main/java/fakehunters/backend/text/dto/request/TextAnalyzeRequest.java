package fakehunters.backend.text.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TextAnalyzeRequest(
        @NotBlank String text,
        @Min(1) @Max(10) Integer evidence_k,
        Boolean include_references
) {
    public TextAnalyzeRequest {
        if (evidence_k == null) evidence_k = 3;
        if (include_references == null) include_references = true;
    }
}
