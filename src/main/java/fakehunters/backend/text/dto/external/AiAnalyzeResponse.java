package fakehunters.backend.text.dto.external;

import java.util.List;

public record AiAnalyzeResponse(
        String analysisId,
        Integer finalScore,
        String verdict,
        InternalData internal,
        List<ExternalArticleData> externalArticles,
        com.fasterxml.jackson.databind.JsonNode contradictions,
        com.fasterxml.jackson.databind.JsonNode gdeltImpact
) {
    public record InternalData(
            Integer linguisticScore,
            Double biasScore,
            Double sensationalScore
    ) {}

    public record ExternalArticleData(
            String title,
            String url,
            String publisher,
            Double similarityScore,
            java.time.LocalDateTime publishedAt
    ) {}
}
