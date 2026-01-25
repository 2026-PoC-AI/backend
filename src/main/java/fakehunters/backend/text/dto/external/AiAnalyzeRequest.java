package fakehunters.backend.text.dto.external;

public record AiAnalyzeRequest(
        String text,
        int evidence_k,
        boolean include_references
) {}
