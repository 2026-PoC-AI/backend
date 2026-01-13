package fakehunters.backend.video.service;

import fakehunters.backend.video.domain.FrameAnalysis;

import fakehunters.backend.video.dto.response.FrameAnalysisResponse;
import fakehunters.backend.video.exception.VideoErrorCode;
import fakehunters.backend.video.mapper.FrameAnalysisMapper;
import fakehunters.backend.global.exception.custom.CustomSystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FrameAnalysisService {

    private final FrameAnalysisMapper frameAnalysisMapper;

    @Transactional(readOnly = true)
    public FrameAnalysisResponse getFrameAnalysis(String frameId) {
        FrameAnalysis frame = frameAnalysisMapper.findById(frameId);
        if (frame == null) {
            throw new CustomSystemException(VideoErrorCode.NOT_FOUND);
        }

        return convertToResponse(frame);
    }

    @Transactional(readOnly = true)
    public List<FrameAnalysisResponse> getFrameAnalysesByResultId(String resultId) {
        List<FrameAnalysis> frames = frameAnalysisMapper.findByResultId(resultId);

        return frames.stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FrameAnalysisResponse> getSuspiciousFrames(String resultId) {
        List<FrameAnalysis> frames = frameAnalysisMapper.findSuspiciousFramesByResultId(resultId);

        return frames.stream()
                .map(this::convertToResponse)
                .toList();
    }

    private FrameAnalysisResponse convertToResponse(FrameAnalysis frame) {
        return FrameAnalysisResponse.builder()
                .frameId(frame.getFrameId())
                .frameNumber(frame.getFrameNumber())
                .timestampSeconds(frame.getTimestampSeconds())
                .isDeepfake(frame.getIsDeepfake())
                .confidenceScore(frame.getConfidenceScore())
                .anomalyType(frame.getAnomalyType())
                .features(frame.getFeatures())
                .build();
    }
}