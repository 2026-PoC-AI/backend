package fakehunters.backend.audio.domain;

public record EvidenceItem(
        String title,
        String detail,
        Integer startMs,
        Integer endMs,
        Double confidence
) {
}
