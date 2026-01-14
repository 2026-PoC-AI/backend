package fakehunters.backend.audio.mapper;

import fakehunters.backend.audio.domain.Grade;

import java.math.BigDecimal;

public record AudioAnalysisResultInsertParam(
        String inputAudioS3Key,
        String inputAudioFormat,
        BigDecimal durationSec,
        int riskScore,
        Grade grade,
        String evidenceJson,
        String warningsJson,
        String featuresJson,
        String spectrogramS3Key
) {}

