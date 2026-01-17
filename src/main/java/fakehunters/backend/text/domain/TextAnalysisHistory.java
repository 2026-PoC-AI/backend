package fakehunters.backend.text.domain;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextAnalysisHistory {
    private String analysisId;
    private UUID requestId;
    private String inputText;
    private Integer finalScore;
    private String verdict;
    private LocalDateTime createdAt;
}