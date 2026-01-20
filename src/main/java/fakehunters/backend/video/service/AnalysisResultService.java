package fakehunters.backend.video.service;

import fakehunters.backend.video.domain.AnalysisResult;
import fakehunters.backend.video.dto.response.AnalysisResultResponse;
import fakehunters.backend.video.exception.VideoErrorCode;
import fakehunters.backend.video.mapper.AnalysisResultMapper;
import fakehunters.backend.global.exception.custom.CustomSystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisResultService {

    private final AnalysisResultMapper analysisResultMapper;

    @Transactional(readOnly = true)
    public AnalysisResultResponse getAnalysisResult(Long resultId) {
        AnalysisResult result = analysisResultMapper.findById(resultId);
        if (result == null) throw new CustomSystemException(VideoErrorCode.NOT_FOUND);
        return convertToResponse(result);
    }

    @Transactional(readOnly = true)
    public AnalysisResultResponse getAnalysisResultByAnalysisId(Long analysisId) {
        AnalysisResult result = analysisResultMapper.findByAnalysisId(analysisId);
        if (result == null) throw new CustomSystemException(VideoErrorCode.NOT_FOUND);
        return convertToResponse(result);
    }

    private AnalysisResultResponse convertToResponse(AnalysisResult result) {
        return AnalysisResultResponse.builder()
                .resultId(result.getResultId())
                .analysisId(result.getAnalysisId())
                // DTO의 createdAt 필드에 도메인의 analyzedAt 값을 넣어줍니다.
                .createdAt(result.getAnalyzedAt())
                .confidenceScore(result.getConfidenceScore())
                .isDeepfake(result.getIsDeepfake())
                .modelVersion(result.getModelVersion())
                .processingTimeMs(result.getProcessingTimeMs())
                .detectedTechniques(result.getDetectedTechniques())
                .summary(result.getSummary())
                .analyzedAt(result.getAnalyzedAt())
                .build();
    }
}