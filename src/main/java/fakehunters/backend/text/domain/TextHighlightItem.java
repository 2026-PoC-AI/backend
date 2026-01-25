package fakehunters.backend.text.domain;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextHighlightItem {
    private Long id;
    private Long analysisId;
    private Integer startIdx;
    private Integer endIdx;
    private String text;
    private Double weight;
    private Integer ord;
}
