package fakehunters.backend.audio.dto.internal;

import com.fasterxml.jackson.databind.JsonNode;
import fakehunters.backend.audio.domain.Grade;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AudioAnalysisResult(
        long id,
        OffsetDateTime createdAt,
        String inputAudioS3Key,
        String inputAudioFormat,
        BigDecimal durationSec,
        int riskScore,
        Grade grade,
        JsonNode evidence,
        JsonNode warnings,
        JsonNode features,
        String spectrogramS3Key
) {}