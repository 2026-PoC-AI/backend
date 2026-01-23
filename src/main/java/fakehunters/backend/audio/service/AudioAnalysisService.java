package fakehunters.backend.audio.service;

import fakehunters.backend.audio.domain.*;
import fakehunters.backend.audio.dto.response.AudioAnalysisResponse;
import fakehunters.backend.audio.exception.AudioErrorCode;
import fakehunters.backend.audio.mapper.*;
import fakehunters.backend.global.exception.custom.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AudioAnalysisService {

    private final AudioFileMapper audioFileMapper;
    private final AudioAnalysisResultMapper audioAnalysisResultMapper;
    private final AudioModelPredictionMapper audioModelPredictionMapper;
    private final AudioTimeSegmentAnalysisMapper audioTimeSegmentAnalysisMapper;
    private final AudioFrequencyAnalysisMapper audioFrequencyAnalysisMapper;
    private final AudioDetectionIndicatorMapper audioDetectionIndicatorMapper;
    private final AudioSpectrogramHeatmapMapper audioSpectrogramHeatmapMapper;
    private final AudioFileService audioFileService;

    @Transactional
    public Long analyzeAudio(Long audioFileId, Long userId) {
        AudioFile audioFile = audioFileMapper.findByIdAndUserId(audioFileId, userId)
                .orElseThrow(() -> new CustomBusinessException(AudioErrorCode.NOT_FOUND));

        audioFileService.updateStatus(audioFileId, "processing");

        try {
            // TODO: ML 모델 호출

            AudioAnalysisResult result = createDummyAnalysisResult(audioFileId);
            audioAnalysisResultMapper.insert(result);

            Long analysisResultId = result.getId();

            List<AudioModelPrediction> predictions = createDummyModelPredictions(analysisResultId);
            audioModelPredictionMapper.insertBatch(predictions);

            List<AudioTimeSegmentAnalysis> segments = createDummyTimeSegments(analysisResultId);
            audioTimeSegmentAnalysisMapper.insertBatch(segments);

            List<AudioDetectionIndicator> indicators = createDummyIndicators(analysisResultId);
            audioDetectionIndicatorMapper.insertBatch(indicators);

            audioFileService.updateStatus(audioFileId, "completed");

            return analysisResultId;

        } catch (CustomBusinessException e) {
            audioFileService.updateStatus(audioFileId, "failed");
            throw e;
        } catch (Exception e) {
            audioFileService.updateStatus(audioFileId, "failed");
            throw new CustomBusinessException(AudioErrorCode.DETECTION_FAILED);
        }
    }

    public AudioAnalysisResponse getAnalysisResult(Long audioFileId, Long userId) {
        audioFileMapper.findByIdAndUserId(audioFileId, userId)
                .orElseThrow(() -> new CustomBusinessException(AudioErrorCode.NOT_FOUND));

        AudioAnalysisResult result = audioAnalysisResultMapper.findByAudioFileId(audioFileId)
                .orElseThrow(() -> new CustomBusinessException(AudioErrorCode.NOT_FOUND));

        Long analysisResultId = result.getId();

        List<AudioModelPrediction> predictions = audioModelPredictionMapper.findByAnalysisResultId(analysisResultId);
        List<AudioTimeSegmentAnalysis> segments = audioTimeSegmentAnalysisMapper.findByAnalysisResultIdOrderByStartTimeAsc(analysisResultId);
        List<AudioDetectionIndicator> indicators = audioDetectionIndicatorMapper.findByAnalysisResultId(analysisResultId);
        List<AudioSpectrogramHeatmap> heatmaps = audioSpectrogramHeatmapMapper.findByAnalysisResultId(analysisResultId);

        return AudioAnalysisResponse.builder()
                .audioFileId(audioFileId)
                .prediction(result.getPrediction())
                .confidence(result.getConfidence())
                .realProbability(result.getRealProbability())
                .fakeProbability(result.getFakeProbability())
                .suspectedMethod(result.getSuspectedMethod())
                .methodConfidence(result.getMethodConfidence())
                .modelVersion(result.getModelVersion())
                .processingTime(result.getProcessingTime())
                .modelVotes(toModelVotes(predictions))
                .suspiciousSegments(toSuspiciousSegments(segments))
                .reasons(toDetectionReasons(indicators))
                .heatmaps(toHeatmapInfos(heatmaps))
                .build();
    }

    private List<AudioAnalysisResponse.ModelVote> toModelVotes(List<AudioModelPrediction> predictions) {
        return predictions.stream()
                .map(p -> AudioAnalysisResponse.ModelVote.builder()
                        .modelName(p.getModelName())
                        .modelType(p.getModelType())
                        .realProbability(p.getRealProbability())
                        .fakeProbability(p.getFakeProbability())
                        .prediction(p.getPrediction())
                        .build())
                .collect(Collectors.toList());
    }

    private List<AudioAnalysisResponse.SuspiciousSegment> toSuspiciousSegments(List<AudioTimeSegmentAnalysis> segments) {
        return segments.stream()
                .map(s -> AudioAnalysisResponse.SuspiciousSegment.builder()
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .riskLevel(s.getRiskLevel())
                        .riskScore(s.getRiskScore())
                        .reason(s.getReason())
                        .indicators(s.getIndicators())
                        .build())
                .collect(Collectors.toList());
    }

    private List<AudioAnalysisResponse.DetectionReason> toDetectionReasons(List<AudioDetectionIndicator> indicators) {
        return indicators.stream()
                .map(i -> AudioAnalysisResponse.DetectionReason.builder()
                        .type(i.getIndicatorType())
                        .description(i.getDescription())
                        .severity(i.getSeverity())
                        .confidence(i.getConfidence())
                        .timeStart(i.getTimeStart())
                        .timeEnd(i.getTimeEnd())
                        .build())
                .collect(Collectors.toList());
    }

    private List<AudioAnalysisResponse.HeatmapInfo> toHeatmapInfos(List<AudioSpectrogramHeatmap> heatmaps) {
        return heatmaps.stream()
                .map(h -> AudioAnalysisResponse.HeatmapInfo.builder()
                        .heatmapUrl(h.getHeatmapUrl())
                        .heatmapType(h.getHeatmapType())
                        .build())
                .collect(Collectors.toList());
    }

    // 더미 데이터 생성 메서드들
    private AudioAnalysisResult createDummyAnalysisResult(Long audioFileId) {
        return AudioAnalysisResult.builder()
                .audioFileId(audioFileId)
                .prediction("fake")
                .confidence(new java.math.BigDecimal("0.9234"))
                .realProbability(new java.math.BigDecimal("0.0766"))
                .fakeProbability(new java.math.BigDecimal("0.9234"))
                .suspectedMethod("TTS")
                .methodConfidence(new java.math.BigDecimal("0.78"))
                .processingTime(new java.math.BigDecimal("2.5"))
                .modelVersion("v1.0.0")
                .build();
    }

    private List<AudioModelPrediction> createDummyModelPredictions(Long analysisResultId) {
        return List.of(
                AudioModelPrediction.builder()
                        .analysisResultId(analysisResultId)
                        .modelName("mel_cnn")
                        .modelType("spectrogram")
                        .realProbability(new java.math.BigDecimal("0.08"))
                        .fakeProbability(new java.math.BigDecimal("0.92"))
                        .prediction("fake")
                        .confidence(new java.math.BigDecimal("0.92"))
                        .build(),
                AudioModelPrediction.builder()
                        .analysisResultId(analysisResultId)
                        .modelName("lfcc_cnn")
                        .modelType("frequency")
                        .realProbability(new java.math.BigDecimal("0.12"))
                        .fakeProbability(new java.math.BigDecimal("0.88"))
                        .prediction("fake")
                        .confidence(new java.math.BigDecimal("0.88"))
                        .build()
        );
    }

    private List<AudioTimeSegmentAnalysis> createDummyTimeSegments(Long analysisResultId) {
        return List.of(
                AudioTimeSegmentAnalysis.builder()
                        .analysisResultId(analysisResultId)
                        .startTime(new java.math.BigDecimal("0.5"))
                        .endTime(new java.math.BigDecimal("1.2"))
                        .riskLevel("high")
                        .riskScore(new java.math.BigDecimal("0.95"))
                        .reason("합성 음성 특징")
                        .indicators(List.of("비자연스러운 고주파", "위상 불일치"))
                        .build()
        );
    }

    private List<AudioDetectionIndicator> createDummyIndicators(Long analysisResultId) {
        return List.of(
                AudioDetectionIndicator.builder()
                        .analysisResultId(analysisResultId)
                        .indicatorType("frequency")
                        .description("비자연스러운 고주파 패턴 감지")
                        .severity("critical")
                        .confidence(new java.math.BigDecimal("0.89"))
                        .timeStart(new java.math.BigDecimal("0.5"))
                        .timeEnd(new java.math.BigDecimal("1.2"))
                        .build()
        );
    }
}