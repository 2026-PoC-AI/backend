package fakehunters.backend.text.dto.response;

import java.util.List;

public record TextAnalyzeResponse(
        String label,
        double score,
        List<Evidence> evidences,
        List<Highlight> highlights,
        List<Reference> references
) {
    public record Evidence(String text, Double score) {}
    public record Highlight(int start, int end, String text, double weight) {}
    public record Reference(String title, String url, String snippet) {}
}
