package fakehunters.backend.audio.service;

import fakehunters.backend.audio.domain.*;
import fakehunters.backend.audio.dto.response.AudioAnalysisResponse;
import fakehunters.backend.audio.dto.response.FastApiAudioResponse;
import fakehunters.backend.audio.exception.AudioErrorCode;
import fakehunters.backend.audio.mapper.*;
import fakehunters.backend.global.exception.custom.CustomBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AudioAnalysisService {

    private final AudioFileMapper audioFileMapper;
    private final AudioAnalysisResultMapper audioAnalysisResultMapper;
    private final AudioModelPredictionMapper audioModelPredictionMapper;
    private final AudioTimeSegmentAnalysisMapper audioTimeSegmentAnalysisMapper;
    private final AudioDetectionIndicatorMapper audioDetectionIndicatorMapper;
    private final AudioSpectrogramHeatmapMapper audioSpectrogramHeatmapMapper;
    private final AudioFileService audioFileService;
    private final AudioFastApiClient fastApiClient;

    @Transactional
    public Long analyzeAudio(Long audioFileId) {
        AudioFile audioFile = audioFileMapper.findById(audioFileId)
                .orElseThrow(() -> new CustomBusinessException(AudioErrorCode.NOT_FOUND));

        audioFileService.updateStatus(audioFileId, "processing");

        try {
            // FastAPI 호출
            log.info("1. FastAPI 호출 시작");
            FastApiAudioResponse fastApiResult = fastApiClient.analyzeAudio(audioFile.getFilePath());
            log.info("2. FastAPI 응답 받음: {}", fastApiResult);

            // 분석 결과 저장
            log.info("3. 분석 결과 생성 시작");
            AudioAnalysisResult result = createAnalysisResultFromFastApi(audioFileId, fastApiResult);
            log.info("4. 분석 결과 생성 완료");

            audioAnalysisResultMapper.insert(result);
            log.info("5. 분석 결과 INSERT 완료: analysisResultId={}", result.getId());

            Long analysisResultId = result.getId();

            // 모델 예측 저장
            log.info("6. 모델 예측 생성 시작");
            List<AudioModelPrediction> predictions = createModelPredictionsFromFastApi(
                    analysisResultId,
                    fastApiResult
            );
            log.info("7. 모델 예측 생성 완료: count={}", predictions.size());

            audioModelPredictionMapper.insertBatch(predictions);
            log.info("8. 모델 예측 INSERT 완료");

            // 시간 구간 분석 저장
            log.info("9. 시간 구간 분석 생성 시작");
            List<AudioTimeSegmentAnalysis> segments = createTimeSegmentsFromFastApi(
                    analysisResultId,
                    fastApiResult
            );
            log.info("10. 시간 구간 분석 생성 완료: count={}", segments.size());

            if (!segments.isEmpty()) {
                log.info("11. 시간 구간 분석 INSERT 시작");
                audioTimeSegmentAnalysisMapper.insertBatch(segments);
                log.info("12. 시간 구간 분석 INSERT 완료");
            }

            // 탐지 지표 저장
            log.info("13. 탐지 지표 생성 시작");
            List<AudioDetectionIndicator> indicators = createIndicatorsFromFastApi(
                    analysisResultId,
                    fastApiResult
            );
            log.info("14. 탐지 지표 생성 완료: count={}", indicators.size());

            if (!indicators.isEmpty()) {
                log.info("15. 탐지 지표 INSERT 시작");
                audioDetectionIndicatorMapper.insertBatch(indicators);
                log.info("16. 탐지 지표 INSERT 완료");
            }

            audioFileService.updateStatus(audioFileId, "completed");
            log.info("17. 분석 완료: analysisResultId={}", analysisResultId);

            return analysisResultId;

        } catch (CustomBusinessException e) {
            log.error("CustomBusinessException 발생", e);
            audioFileService.updateStatus(audioFileId, "failed");
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 에러 발생", e);
            audioFileService.updateStatus(audioFileId, "failed");
            throw new CustomBusinessException(AudioErrorCode.DETECTION_FAILED);
        }
    }

    public AudioAnalysisResponse getAnalysisResult(Long audioFileId) {
        audioFileMapper.findById(audioFileId)
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

    // FastAPI 결과를 DB 엔티티로 변환
    private AudioAnalysisResult createAnalysisResultFromFastApi(
            Long audioFileId,
            FastApiAudioResponse fastApiResult
    ) {
        return AudioAnalysisResult.builder()
                .audioFileId(audioFileId)
                .prediction(fastApiResult.getPrediction())
                .confidence(fastApiResult.getConfidence())
                .realProbability(fastApiResult.getProbabilities().get("real"))
                .fakeProbability(fastApiResult.getProbabilities().get("fake"))
                .suspectedMethod(fastApiResult.getSuspectedMethod())
                .methodConfidence(fastApiResult.getMethodConfidence())
                .processingTime(fastApiResult.getProcessingTime())
                .modelVersion(fastApiResult.getModelVersion())
                .build();
    }

    private List<AudioModelPrediction> createModelPredictionsFromFastApi(
            Long analysisResultId,
            FastApiAudioResponse fastApiResult
    ) {
        List<AudioModelPrediction> predictions = new ArrayList<>();

        Map<String, Map<String, BigDecimal>> modelOutputs = fastApiResult.getModelOutputs();

        if (modelOutputs != null) {
            modelOutputs.forEach((modelName, probs) -> {
                BigDecimal realProb = probs.get("real");
                BigDecimal fakeProb = probs.get("fake");

                predictions.add(AudioModelPrediction.builder()
                        .analysisResultId(analysisResultId)
                        .modelName(modelName)
                        .modelType(modelName.equals("mel") ? "spectrogram" : "frequency")
                        .realProbability(realProb)
                        .fakeProbability(fakeProb)
                        .prediction(fakeProb.compareTo(realProb) > 0 ? "fake" : "real")
                        .confidence(fakeProb.max(realProb))
                        .build());
            });
        }

        return predictions;
    }

    private List<AudioTimeSegmentAnalysis> createTimeSegmentsFromFastApi(
            Long analysisResultId,
            FastApiAudioResponse fastApiResult
    ) {
        List<AudioTimeSegmentAnalysis> segments = new ArrayList<>();

        List<FastApiAudioResponse.TimeSegment> timeSegments = fastApiResult.getTimeSegments();

        if (timeSegments != null) {
            timeSegments.forEach(segment -> {
                segments.add(AudioTimeSegmentAnalysis.builder()
                        .analysisResultId(analysisResultId)
                        .startTime(segment.getStart())
                        .endTime(segment.getEnd())
                        .riskLevel(segment.getRisk())
                        .riskScore(segment.getRisk().equals("high") ?
                                new BigDecimal("0.9") : new BigDecimal("0.5"))
                        .reason(segment.getReason())
                        .indicators(List.of(segment.getReason()))
                        .build());
            });
        }

        return segments;
    }

    private List<AudioDetectionIndicator> createIndicatorsFromFastApi(
            Long analysisResultId,
            FastApiAudioResponse fastApiResult
    ) {
        List<AudioDetectionIndicator> indicators = new ArrayList<>();

        List<String> suspiciousPatterns = fastApiResult.getSuspiciousPatterns();

        if (suspiciousPatterns != null) {
            suspiciousPatterns.forEach(pattern -> {
                indicators.add(AudioDetectionIndicator.builder()
                        .analysisResultId(analysisResultId)
                        .indicatorType("pattern")
                        .description(pattern)
                        .severity("high")
                        .confidence(fastApiResult.getConfidence())
                        .build());
            });
        }

        return indicators;
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
}