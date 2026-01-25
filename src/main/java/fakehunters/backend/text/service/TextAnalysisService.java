package fakehunters.backend.text.service;

import fakehunters.backend.text.dto.external.AiAnalyzeRequest;
import fakehunters.backend.text.dto.external.AiAnalyzeResponse;
import fakehunters.backend.text.dto.request.TextAnalyzeRequest;
import fakehunters.backend.text.dto.response.TextAnalyzeResponse;
import fakehunters.backend.text.mapper.TextMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TextAnalysisService {

    private final TextAiClient aiClient;
    private final TextMapper textMapper;

    public TextAnalysisService(TextAiClient aiClient, TextMapper textMapper) {
        this.aiClient = aiClient;
        this.textMapper = textMapper;
    }

    @Transactional
    public TextAnalyzeResponse analyze(TextAnalyzeRequest req, String xRequestIdHeader) {

        // 1) AI 서버 호출
        AiAnalyzeResponse ai = aiClient.analyze(
                new AiAnalyzeRequest(req.text(), req.evidence_k(), req.include_references())
        );

        // 2) label 정규화: REAL -> TRUE
        String label = ai.label();
        if (label != null && label.equalsIgnoreCase("REAL")) label = "TRUE";
        if (label == null || label.isBlank()) label = "FAKE"; // 방어(default)

        // 3) score 방어
        double score = ai.score() == null ? 0.0 : ai.score();

        // 4) requestId UUID 파싱(없거나 이상하면 null)
        UUID requestId = null;
        if (xRequestIdHeader != null && !xRequestIdHeader.isBlank()) {
            try {
                requestId = UUID.fromString(xRequestIdHeader.trim());
            } catch (Exception ignored) {
                requestId = null;
            }
        }

        // 5) 부모 저장
        textMapper.insertTextAnalysis(
                req.text(),
                req.evidence_k(),
                req.include_references(),
                requestId,
                label,
                score
        );
        long analysisId = textMapper.selectLastInsertId();

        // 6) 자식 저장 (ord 보장 + null-safe)
        List<AiAnalyzeResponse.Evidence> evidences = ai.evidences() == null ? List.of() : ai.evidences();
        for (int i = 0; i < evidences.size(); i++) {
            var e = evidences.get(i);
            if (e == null || e.text() == null || e.text().isBlank()) continue;
            double eScore = e.score() == null ? 0.0 : e.score();
            textMapper.insertEvidence(analysisId, e.text(), eScore, i);
        }

        List<AiAnalyzeResponse.Highlight> highlights = ai.highlights() == null ? List.of() : ai.highlights();
        for (int i = 0; i < highlights.size(); i++) {
            var h = highlights.get(i);
            if (h == null) continue;
            if (h.start() == null || h.end() == null) continue;
            if (h.text() == null || h.text().isBlank()) continue;
            double w = h.weight() == null ? 0.0 : h.weight();
            textMapper.insertHighlight(analysisId, h.start(), h.end(), h.text(), w, i);
        }

        List<AiAnalyzeResponse.Reference> refs = ai.references() == null ? List.of() : ai.references();
        for (int i = 0; i < refs.size(); i++) {
            var r = refs.get(i);
            if (r == null) continue;
            if (r.title() == null || r.title().isBlank()) continue;
            if (r.url() == null || r.url().isBlank()) continue;
            textMapper.insertReference(analysisId, r.title(), r.url(), r.snippet(), i);
        }

        // 7) 프론트 응답(고정 스펙)
        return new TextAnalyzeResponse(
                label,
                score,
                evidences.stream()
                        .filter(e -> e != null && e.text() != null && !e.text().isBlank())
                        .map(e -> new TextAnalyzeResponse.Evidence(e.text(), e.score()))
                        .toList(),
                highlights.stream()
                        .filter(h -> h != null && h.start() != null && h.end() != null && h.text() != null && !h.text().isBlank())
                        .map(h -> new TextAnalyzeResponse.Highlight(h.start(), h.end(), h.text(), h.weight() == null ? 0.0 : h.weight()))
                        .toList(),
                refs.stream()
                        .filter(r -> r != null && r.title() != null && !r.title().isBlank() && r.url() != null && !r.url().isBlank())
                        .map(r -> new TextAnalyzeResponse.Reference(r.title(), r.url(), r.snippet()))
                        .toList()
        );
    }
}
