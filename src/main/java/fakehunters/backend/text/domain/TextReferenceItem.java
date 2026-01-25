package fakehunters.backend.text.domain;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextReferenceItem {
    private Long id;
    private Long analysisId;
    private String title;
    private String url;
    private String snippet;
    private Integer ord;
}
