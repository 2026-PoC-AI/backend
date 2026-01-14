package fakehunters.backend.audio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fakehunters.backend.audio.domain.EvidenceItem;
import fakehunters.backend.audio.domain.Grade;
import fakehunters.backend.audio.dto.internal.AudioAnalysisResult;
import fakehunters.backend.audio.dto.request.AnalyzeRequest;
import fakehunters.backend.audio.dto.response.AnalyzeResponse;
import fakehunters.backend.audio.mapper.AudioAnalysisResultInsertParam;
import fakehunters.backend.audio.mapper.AudioAnalysisResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AudioService {

    private final AudioAnalysisResultMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public AnalyzeResponse insertTest(AnalyzeRequest req) {

        // 1️⃣ grade 변환
        Grade grade = Grade.valueOf(req.grade().toUpperCase());

        // 2️⃣ 더미 evidence / warnings (FastAPI 붙이기 전 임시)
        String evidenceJson;
        String warningsJson;

        try {
            evidenceJson = objectMapper.writeValueAsString(List.of(
                    Map.of(
                            "title", "DB Insert Test",
                            "detail", "Audio analysis result inserted successfully",
                            "startMs", 0,
                            "endMs", 1200,
                            "confidence", 0.85
                    )
            ));

            warningsJson = objectMapper.writeValueAsString(
                    List.of("Temporary warning for insert test")
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build JSON", e);
        }

        // 3️⃣ Insert Param 생성
        AudioAnalysisResultInsertParam param =
                new AudioAnalysisResultInsertParam(
                        req.inputAudioS3Key(),
                        req.inputAudioFormat(),
                        req.durationSec() == null
                                ? null
                                : BigDecimal.valueOf(req.durationSec()),
                        req.riskScore(),
                        grade,
                        evidenceJson,
                        warningsJson,
                        null, // featuresJson
                        req.spectrogramS3Key()
                );

        // 4️⃣ INSERT → id 반환 (PostgreSQL RETURNING)
        long id = mapper.insert(param);

        // 5️⃣ 방금 저장한 row 조회
        AudioAnalysisResult saved = mapper.findById(id);

        // 6️⃣ JsonNode → EvidenceItem 변환 (지금은 단순 더미)
        List<EvidenceItem> evidence = List.of(
                new EvidenceItem(
                        "DB Insert Test",
                        "Audio analysis result inserted successfully",
                        0,
                        1200,
                        0.85
                )
        );

        // 7️⃣ AnalyzeResponse 생성 (네 record에 정확히 맞춤)
        return new AnalyzeResponse(
                saved.id(),
                saved.createdAt() == null ? OffsetDateTime.now() : saved.createdAt(),
                saved.riskScore(),
                saved.grade(),
                evidence,
                List.of("Temporary warning for insert test"),
                saved.spectrogramS3Key(),
                null // spectrogramUrl (지금은 null, 나중에 presigned URL)
        );
    }
}
