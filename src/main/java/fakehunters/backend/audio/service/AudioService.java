package fakehunters.backend.audio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fakehunters.backend.audio.domain.Grade;
import fakehunters.backend.audio.dto.external.AiAudioRequest;
import fakehunters.backend.audio.dto.external.AiAudioResponseWrapper;
import fakehunters.backend.audio.dto.request.AnalyzeRequest;
import fakehunters.backend.audio.dto.response.AnalyzeResponse;
import fakehunters.backend.audio.mapper.AudioAnalysisResultInsertParam;
import fakehunters.backend.audio.mapper.AudioAnalysisResultMapper;
import fakehunters.backend.audio.dto.internal.AudioAnalysisResult; //
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AudioService {

    private final AudioAnalysisResultMapper mapper;
    private final ObjectMapper objectMapper;
    private final RestClient aiClient; // AI 서버 통신용

    @Transactional
    public AnalyzeResponse analyze(AnalyzeRequest req) {

        // 1. AI 서버 호출 (FastAPI)
        AiAudioRequest aiRequest = new AiAudioRequest(req.inputAudioS3Key());

        AiAudioResponseWrapper aiResponse = aiClient.post()
                .uri("/audio/analyze")
                .body(aiRequest)
                .retrieve()
                .body(AiAudioResponseWrapper.class);

        if (aiResponse == null || !aiResponse.success() || aiResponse.data() == null) {
            throw new RuntimeException("AI Server Analysis Failed");
        }

        AiAudioResponseWrapper.AiAudioData aiData = aiResponse.data();

        // 2. DB 저장 파라미터 준비
        try {
            // Evidence List -> JSON String 변환
            String evidenceJson = objectMapper.writeValueAsString(aiData.evidence());
            String warningsJson = objectMapper.writeValueAsString(aiData.warnings());

            AudioAnalysisResultInsertParam param = new AudioAnalysisResultInsertParam(
                    req.inputAudioS3Key(),
                    req.inputAudioFormat(),
                    req.durationSec() != null ? BigDecimal.valueOf(req.durationSec()) : null,
                    aiData.risk_score(),              // AI가 준 점수
                    Grade.valueOf(aiData.grade()),    // AI가 준 등급
                    evidenceJson,
                    warningsJson,
                    null,
                    req.spectrogramS3Key()
            );

            // 3. DB Insert
            Long id = mapper.insert(param);
            AudioAnalysisResult saved = mapper.findById(id);

            // 4. 최종 응답 반환
            // (화면 표시용 DTO 변환)
            // 실제 구현시에는 saved.evidence() (JsonNode)를 List<EvidenceItem>으로 변환하는 로직 필요
            return new AnalyzeResponse(
                    saved.id(),
                    saved.createdAt(),
                    saved.riskScore(),
                    saved.grade(),
                    List.of(), // TODO: JsonNode -> List 변환 로직 추가 필요
                    List.of(), // TODO: JsonNode -> List 변환 로직 추가 필요
                    saved.spectrogramS3Key(),
                    null
            );

        } catch (Exception e) {
            throw new RuntimeException("Error during analysis processing", e);
        }
    }
}