package fakehunters.backend.text.domain;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextAnalysisHistory {
    private Long id;                 // text_analysis.id
    private UUID requestId;          // text_analysis.request_id
    private String requestText;      // text_analysis.request_text
    private Integer evidenceK;       // text_analysis.evidence_k
    private Boolean includeReferences;// text_analysis.include_references
    private TextLabel label;         // text_analysis.label
    private Double score;            // text_analysis.score
    private OffsetDateTime createdAt;// text_analysis.created_at

    // (선택) join 조회 시 같이 담고 싶으면
    private List<TextEvidenceItem> evidences;
    private List<TextHighlightItem> highlights;
    private List<TextReferenceItem> references;
}
