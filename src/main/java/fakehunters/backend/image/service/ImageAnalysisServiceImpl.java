package fakehunters.backend.image.service;


import com.fasterxml.jackson.databind.JsonNode;
import fakehunters.backend.image.ai.AiImageClient;
import fakehunters.backend.image.domain.ImageAnalysisInput;
import fakehunters.backend.image.domain.ImageAnalysisJob;
import fakehunters.backend.image.domain.ImageAnalysisResult;
import fakehunters.backend.image.dto.request.DeepfakeResultRequest;
import fakehunters.backend.image.dto.request.ImageAnalyzeRequest;
import fakehunters.backend.image.dto.response.DeepfakeResultResponse;
import fakehunters.backend.image.dto.response.ImageAnalysisQueryResponse;
import fakehunters.backend.image.dto.response.ImageAnalyzeResponse;
import fakehunters.backend.image.dto.response.ImageResultDetailResponse;
import fakehunters.backend.image.mapper.ImageAnalysisInputMapper;
import fakehunters.backend.image.mapper.ImageAnalysisJobMapper;
import fakehunters.backend.image.mapper.ImageAnalysisResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageAnalysisServiceImpl implements ImageAnalysisService {
    private final ImageAnalysisJobMapper jobMapper;
    private final ImageAnalysisInputMapper inputMapper;
    private final ImageAnalysisResultMapper resultMapper;
    private final AiImageClient aiImageClient;

    // 이미지 분석 Job 생성
    @Override
    @Transactional
    public ImageAnalyzeResponse createAnalysis(ImageAnalyzeRequest request) {

        // 분석 Job 생성
        ImageAnalysisJob job = ImageAnalysisJob.builder()
                .jobUuid(UUID.randomUUID())
                .jobStatus("CREATED")
                .build();

        jobMapper.insert(job);

        // 입력 이미지 메타데이터 저장
        ImageAnalysisInput input = ImageAnalysisInput.builder()
                .inputUuid(UUID.randomUUID())
                .jobId(job.getJobId())
                .inputS3Key(request.getS3Key())
                .inputFilename(request.getFilename())
                .inputFilesize(request.getFileSize())
                .inputMimeType(request.getMimeType())
                .build();

        inputMapper.insert(input);


        // AI 서버 딥페이크 분석 요청
        try {
            jobMapper.updateStatus(job.getJobId(), "ANALYZING");
            aiImageClient.requestDeepfakeAnalysis(
                    job.getJobUuid(),
                    request.getS3Key()
            );
        } catch (Exception e) {
            jobMapper.updateStatus(job.getJobId(), "FAILED");
            log.warn("AI image analysis request failed. jobUuid={}", job.getJobUuid(), e);
        }

        // 외부로는 UUID만 반환
        return new ImageAnalyzeResponse(job.getJobUuid());
    }

    //딥페이크 분석 결과 저장
    @Override
    @Transactional
    public DeepfakeResultResponse saveDeepfakeResult(DeepfakeResultRequest request) {

        ImageAnalysisJob job =
                jobMapper.findByJobUuid(request.getJobUuid());

        if (job == null) {
            throw new IllegalArgumentException("Image analysis job not found");
        }

        JsonNode output = request.getRawResult().get("output");

        String label = output.get("decision").asText();
        String riskLevel = output.get("risk_level").asText();
        String interpretation = output.get("message").asText();

        double confidence = request.getConfidence();

        int riskScore = (int) Math.round(confidence * 100);

        ImageAnalysisResult result = ImageAnalysisResult.builder()
                .resultUuid(UUID.randomUUID())
                .jobId(job.getJobId())
                .resultTaskType("deepfake_image")
                .resultLabel(label)
                .resultConfidence(confidence)
                .resultRiskScore(riskScore)
                .resultRiskLevel(riskLevel)
                .resultInterpretation(interpretation)
                .resultEvidence(null)
                .resultWarnings(null)
                .resultJson(request.getRawResult())
                .build();

        resultMapper.insert(result);

        // Job 상태 업데이트
        jobMapper.updateStatus(job.getJobId(), "ANALYZED");

        return new DeepfakeResultResponse("SAVED");
    }

    @Override
    @Transactional(readOnly = true)
    public ImageAnalysisQueryResponse getAnalysisResult(UUID jobUuid) {

        ImageAnalysisJob job = jobMapper.findByJobUuid(jobUuid);
        if (job == null) {
            throw new IllegalArgumentException("Image analysis job not found");
        }

        ImageAnalysisInput input =
                inputMapper.findByJobId(job.getJobId());

        List<ImageAnalysisResult> results =
                resultMapper.findByJobId(job.getJobId());

        return new ImageAnalysisQueryResponse(
                new ImageAnalysisQueryResponse.JobInfo(
                        job.getJobUuid(),
                        job.getJobStatus(),
                        job.getCreatedAt()
                ),
                new ImageAnalysisQueryResponse.InputInfo(
                        input.getInputFilename(),
                        input.getInputS3Key(),
                        input.getInputMimeType(),
                        input.getInputFilesize()
                ),
                results.stream()
                        .map(r -> new ImageResultDetailResponse(
                                r.getResultTaskType(),
                                r.getResultLabel(),
                                r.getResultConfidence(),
                                r.getResultRiskScore(),
                                r.getResultRiskLevel(),
                                r.getResultInterpretation(),
                                r.getResultEvidence(),
                                r.getResultWarnings()
                        ))
                        .toList()
        );
    }

}
