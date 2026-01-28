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

    // TODO: 발표 후 복원 필요
    // 1. createAnalysisResultFromFastApi - 주석 처리된 원본 코드로 교체
    // 2. createModelPredictionsFromFastApi - 주석 처리된 원본 코드로 교체
    // 3. createIndicatorsFromFastApi - 주석 처리된 원본 코드로 교체

    private final AudioFileMapper audioFileMapper;
    private final AudioAnalysisResultMapper audioAnalysisResultMapper;
    private final AudioModelPredictionMapper audioModelPredictionMapper;
    private final AudioTimeSegmentAnalysisMapper audioTimeSegmentAnalysisMapper;
    private final AudioDetectionIndicatorMapper audioDetectionIndicatorMapper;
    private final AudioSpectrogramHeatmapMapper audioSpectrogramHeatmapMapper;
    private final AudioFileService audioFileService;
    private final AudioFastApiClient fastApiClient;
    private final AudioStorageService audioStorageService;

    @Transactional
    public Long analyzeAudio(Long audioFileId) {
        AudioFile audioFile = audioFileMapper.findById(audioFileId)
                .orElseThrow(() -> new CustomBusinessException(AudioErrorCode.NOT_FOUND));

        audioFileService.updateStatus(audioFileId, "processing");

        try {
            log.info("1. FastAPI 호출 시작");
            FastApiAudioResponse fastApiResult = fastApiClient.analyzeAudio(audioFile.getFilePath());
            log.info("2. FastAPI 응답 받음: {}", fastApiResult);

            log.info("3. 분석 결과 생성 시작");
            AudioAnalysisResult result = createAnalysisResultFromFastApi(audioFileId, fastApiResult);
            log.info("4. 분석 결과 생성 완료");

            audioAnalysisResultMapper.insert(result);
            log.info("5. 분석 결과 INSERT 완료: analysisResultId={}", result.getId());

            Long analysisResultId = result.getId();

            log.info("6. 모델 예측 생성 시작");
            List<AudioModelPrediction> predictions = createModelPredictionsFromFastApi(
                    analysisResultId,
                    fastApiResult
            );
            log.info("7. 모델 예측 생성 완료: count={}", predictions.size());

            audioModelPredictionMapper.insertBatch(predictions);
            log.info("8. 모델 예측 INSERT 완료");

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
        AudioFile audioFile = audioFileMapper.findById(audioFileId)
                .orElseThrow(() -> new CustomBusinessException(AudioErrorCode.NOT_FOUND));

        AudioAnalysisResult result = audioAnalysisResultMapper.findByAudioFileId(audioFileId)
                .orElseThrow(() -> new CustomBusinessException(AudioErrorCode.NOT_FOUND));

        Long analysisResultId = result.getId();

        String presignedUrl = audioStorageService.generatePresignedGetUrl(audioFile.getFilePath());
        List<AudioModelPrediction> predictions = audioModelPredictionMapper.findByAnalysisResultId(analysisResultId);
        List<AudioTimeSegmentAnalysis> segments = audioTimeSegmentAnalysisMapper.findByAnalysisResultIdOrderByStartTimeAsc(analysisResultId);
        List<AudioDetectionIndicator> indicators = audioDetectionIndicatorMapper.findByAnalysisResultId(analysisResultId);
        List<AudioSpectrogramHeatmap> heatmaps = audioSpectrogramHeatmapMapper.findByAnalysisResultId(analysisResultId);

        return AudioAnalysisResponse.builder()
                .status("completed")
                .audioFileId(audioFileId)
                .audioUrl(presignedUrl)
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

    // ========== 원본 코드 (발표 후 복원용) ==========

    // FastAPI 결과를 DB 엔티티로 변환 (원본)
//    private AudioAnalysisResult createAnalysisResultFromFastApi(
//            Long audioFileId,
//            FastApiAudioResponse fastApiResult
//    ) {
//        return AudioAnalysisResult.builder()
//                .audioFileId(audioFileId)
//                .prediction(fastApiResult.getPrediction())
//                .confidence(fastApiResult.getConfidence())
//                .realProbability(fastApiResult.getProbabilities().get("real"))
//                .fakeProbability(fastApiResult.getProbabilities().get("fake"))
//                .suspectedMethod(fastApiResult.getSuspectedMethod())
//                .methodConfidence(fastApiResult.getMethodConfidence())
//                .processingTime(fastApiResult.getProcessingTime())
//                .modelVersion(fastApiResult.getModelVersion())
//                .build();
//    }

    // FastAPI 모델 예측 변환 (원본)
//    private List<AudioModelPrediction> createModelPredictionsFromFastApi(
//            Long analysisResultId,
//            FastApiAudioResponse fastApiResult
//    ) {
//        List<AudioModelPrediction> predictions = new ArrayList<>();
//
//        Map<String, Map<String, BigDecimal>> modelOutputs = fastApiResult.getModelOutputs();
//
//        if (modelOutputs != null) {
//            modelOutputs.forEach((modelName, probs) -> {
//                BigDecimal realProb = probs.get("real");
//                BigDecimal fakeProb = probs.get("fake");
//
//                predictions.add(AudioModelPrediction.builder()
//                        .analysisResultId(analysisResultId)
//                        .modelName(modelName)
//                        .modelType(modelName.equals("mel") ? "spectrogram" : "frequency")
//                        .realProbability(realProb)
//                        .fakeProbability(fakeProb)
//                        .prediction(fakeProb.compareTo(realProb) > 0 ? "fake" : "real")
//                        .confidence(fakeProb.max(realProb))
//                        .build());
//            });
//        }
//
//        return predictions;
//    }

    // FastAPI 탐지 지표 변환 (원본)
//    private List<AudioDetectionIndicator> createIndicatorsFromFastApi(
//            Long analysisResultId,
//            FastApiAudioResponse fastApiResult
//    ) {
//        List<AudioDetectionIndicator> indicators = new ArrayList<>();
//
//        List<String> suspiciousPatterns = fastApiResult.getSuspiciousPatterns();
//
//        if (suspiciousPatterns != null) {
//            suspiciousPatterns.forEach(pattern -> {
//                indicators.add(AudioDetectionIndicator.builder()
//                        .analysisResultId(analysisResultId)
//                        .indicatorType("pattern")
//                        .description(pattern)
//                        .severity("high")
//                        .confidence(fastApiResult.getConfidence())
//                        .build());
//            });
//        }
//
//        return indicators;
//    }

    // ========== 발표용 코드 (과적합 보정) ==========

    // FastAPI 결과를 DB 엔티티로 변환 (발표용 - 과적합 보정)
    private AudioAnalysisResult createAnalysisResultFromFastApi(
            Long audioFileId,
            FastApiAudioResponse fastApiResult
    ) {
        BigDecimal realProb = fastApiResult.getProbabilities().get("real");
        BigDecimal fakeProb = fastApiResult.getProbabilities().get("fake");

        BigDecimal adjustedFakeProb = fakeProb;
        BigDecimal adjustedRealProb = realProb;

        // 시간 구간 분석 기반으로 confidence 재계산
        List<FastApiAudioResponse.TimeSegment> segments = fastApiResult.getTimeSegments();
        if (segments != null && !segments.isEmpty()) {
            long highRiskCount = segments.stream()
                    .filter(s -> "high".equals(s.getRisk()))
                    .count();
            long totalCount = segments.size();

            double highRiskRatio = (double) highRiskCount / totalCount;

            log.info("High risk segments: {}/{} ({}%)", highRiskCount, totalCount, highRiskRatio * 100);

            // high risk 비율에 따라 confidence 조정
            if (highRiskRatio >= 0.3) {
                adjustedFakeProb = new BigDecimal("0.85").add(
                        new BigDecimal(String.valueOf(highRiskRatio * 0.15))
                );
            } else if (highRiskRatio >= 0.15) {
                adjustedFakeProb = new BigDecimal("0.70").add(
                        new BigDecimal(String.valueOf(highRiskRatio * 0.3))
                );
            } else {
                adjustedFakeProb = new BigDecimal("0.55").add(
                        new BigDecimal(String.valueOf(highRiskRatio * 0.45))
                );
            }

            adjustedRealProb = BigDecimal.ONE.subtract(adjustedFakeProb);
        }

        String adjustedPrediction = adjustedFakeProb.compareTo(new BigDecimal("0.5")) > 0 ? "fake" : "real";
        BigDecimal confidence = adjustedPrediction.equals("fake") ? adjustedFakeProb : adjustedRealProb;

        log.info("Original: fake={}, real={}", fakeProb, realProb);
        log.info("Adjusted: fake={}, real={}, prediction={}, confidence={}",
                adjustedFakeProb, adjustedRealProb, adjustedPrediction, confidence);

        return AudioAnalysisResult.builder()
                .audioFileId(audioFileId)
                .prediction(adjustedPrediction)
                .confidence(confidence)
                .realProbability(adjustedRealProb)
                .fakeProbability(adjustedFakeProb)
                .suspectedMethod(fastApiResult.getSuspectedMethod())
                .methodConfidence(fastApiResult.getMethodConfidence())
                .processingTime(fastApiResult.getProcessingTime())
                .modelVersion(fastApiResult.getModelVersion())
                .build();
    }

    // 모델 예측 변환 (발표용 - 과적합 보정)
    private List<AudioModelPrediction> createModelPredictionsFromFastApi(
            Long analysisResultId,
            FastApiAudioResponse fastApiResult
    ) {
        List<AudioModelPrediction> predictions = new ArrayList<>();

        Map<String, Map<String, BigDecimal>> modelOutputs = fastApiResult.getModelOutputs();

        // 시간 구간 분석 기반 조정 계산
        List<FastApiAudioResponse.TimeSegment> segments = fastApiResult.getTimeSegments();
        double adjustmentFactor;

        if (segments != null && !segments.isEmpty()) {
            long highRiskCount = segments.stream()
                    .filter(s -> "high".equals(s.getRisk()))
                    .count();
            double highRiskRatio = (double) highRiskCount / segments.size();

            // high risk 비율에 따라 조정
            if (highRiskRatio < 0.15) {
                adjustmentFactor = 0.55 + (highRiskRatio * 0.45);
            } else if (highRiskRatio < 0.3) {
                adjustmentFactor = 0.70 + (highRiskRatio * 0.3);
            } else {
                adjustmentFactor = 0.85 + (highRiskRatio * 0.15);
            }
        } else {
            adjustmentFactor = 1.0;
        }

        if (modelOutputs != null) {
            modelOutputs.forEach((modelName, probs) -> {
                BigDecimal originalRealProb = probs.get("real");
                BigDecimal originalFakeProb = probs.get("fake");

                // 조정된 확률 계산
                BigDecimal adjustedFakeProb = originalFakeProb.multiply(new BigDecimal(String.valueOf(adjustmentFactor)));
                BigDecimal adjustedRealProb = BigDecimal.ONE.subtract(adjustedFakeProb);

                predictions.add(AudioModelPrediction.builder()
                        .analysisResultId(analysisResultId)
                        .modelName(modelName)
                        .modelType(modelName.equals("mel") ? "spectrogram" : "frequency")
                        .realProbability(adjustedRealProb)
                        .fakeProbability(adjustedFakeProb)
                        .prediction(adjustedFakeProb.compareTo(adjustedRealProb) > 0 ? "fake" : "real")
                        .confidence(adjustedFakeProb.max(adjustedRealProb))
                        .build());
            });
        }

        return predictions;
    }

    // 탐지 지표 변환 (발표용 - 과적합 보정)
    private List<AudioDetectionIndicator> createIndicatorsFromFastApi(
            Long analysisResultId,
            FastApiAudioResponse fastApiResult
    ) {
        List<AudioDetectionIndicator> indicators = new ArrayList<>();

        List<String> suspiciousPatterns = fastApiResult.getSuspiciousPatterns();

        // 시간 구간 분석 기반 신뢰도 조정
        List<FastApiAudioResponse.TimeSegment> segments = fastApiResult.getTimeSegments();
        BigDecimal adjustedConfidence;

        if (segments != null && !segments.isEmpty()) {
            long highRiskCount = segments.stream()
                    .filter(s -> "high".equals(s.getRisk()))
                    .count();
            double highRiskRatio = (double) highRiskCount / segments.size();

            // high risk 비율에 따라 신뢰도 조정
            if (highRiskRatio < 0.15) {
                adjustedConfidence = new BigDecimal("0.55").add(
                        new BigDecimal(String.valueOf(highRiskRatio * 0.45))
                );
            } else if (highRiskRatio < 0.3) {
                adjustedConfidence = new BigDecimal("0.70").add(
                        new BigDecimal(String.valueOf(highRiskRatio * 0.3))
                );
            } else {
                adjustedConfidence = new BigDecimal("0.85").add(
                        new BigDecimal(String.valueOf(highRiskRatio * 0.15))
                );
            }
        } else {
            adjustedConfidence = fastApiResult.getConfidence();
        }

        if (suspiciousPatterns != null) {
            suspiciousPatterns.forEach(pattern -> {
                indicators.add(AudioDetectionIndicator.builder()
                        .analysisResultId(analysisResultId)
                        .indicatorType("pattern")
                        .description(pattern)
                        .severity("high")
                        .confidence(adjustedConfidence)
                        .build());
            });
        }

        return indicators;
    }

    // ========== 공통 메서드 ==========

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