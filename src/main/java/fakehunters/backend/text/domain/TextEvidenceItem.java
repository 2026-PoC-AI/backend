package fakehunters.backend.text.domain;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextEvidenceItem {
    private Long id;
    private Long analysisId;
    private String text;
    private Double score;
    private Integer ord;
}
