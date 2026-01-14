package fakehunters.backend.audio.dto.response;

import fakehunters.backend.audio.domain.EvidenceItem;
import fakehunters.backend.audio.domain.Grade;

import java.time.OffsetDateTime;
import java.util.List;

public record AnalyzeResponse(
        long id,
        OffsetDateTime createdAt,
        int riskScore,
        Grade grade,
        List<EvidenceItem> evidence,
        List<String> warnings,
        String spectrogramS3Key,
        String spectrogramUrl
) {}
