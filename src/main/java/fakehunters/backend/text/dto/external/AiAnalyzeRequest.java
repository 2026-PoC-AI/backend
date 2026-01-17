package fakehunters.backend.text.dto.external;

public record AiAnalyzeRequest(
        String analysisId,
        String inputText
) {}