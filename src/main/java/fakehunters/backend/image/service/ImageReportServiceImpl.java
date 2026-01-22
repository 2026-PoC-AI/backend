package fakehunters.backend.image.service;

import fakehunters.backend.global.exception.custom.CustomBusinessException;
import fakehunters.backend.global.exception.custom.CustomSystemException;
import fakehunters.backend.image.ai.ImageReportGenerator;
import fakehunters.backend.image.domain.ImageAnalysisJob;
import fakehunters.backend.image.domain.ImageAnalysisReport;
import fakehunters.backend.image.domain.ImageAnalysisResult;
import fakehunters.backend.image.dto.response.GeneratedReport;
import fakehunters.backend.image.exception.ImageErrorCode;
import fakehunters.backend.image.mapper.ImageAnalysisJobMapper;
import fakehunters.backend.image.mapper.ImageAnalysisReportMapper;
import fakehunters.backend.image.mapper.ImageAnalysisResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageReportServiceImpl implements ImageReportService {

    private final ImageAnalysisJobMapper jobMapper;
    private final ImageAnalysisResultMapper resultMapper;
    private final ImageAnalysisReportMapper reportMapper;
    private final ImageReportGenerator reportGenerator;

    @Override
    @Transactional
    public ImageAnalysisReport generateFinalReport(UUID jobUuid) {

        ImageAnalysisJob job = jobMapper.findByJobUuid(jobUuid);
        if (job == null) {
            throw new CustomBusinessException(ImageErrorCode.NOT_FOUND);
        }

        // 이미 리포트가 생성되어 있으면 중복 생성하지 않고 그대로 반환
        ImageAnalysisReport existing = reportMapper.findByJobId(job.getJobId());
        if (existing != null) {
            return existing;
        }

        List<ImageAnalysisResult> results = resultMapper.findByJobId(job.getJobId());
        // 분석 결과 없으면 여기서 차단
        if (results == null || results.isEmpty()) {
            throw new CustomBusinessException(ImageErrorCode.ANALYSIS_NOT_READY);
        }

        if (!"ANALYZED".equals(job.getJobStatus())
                && !"REPORT_READY".equals(job.getJobStatus())) {
            throw new CustomBusinessException(ImageErrorCode.ANALYSIS_NOT_READY);
        }

        GeneratedReport generated;
        try {
            generated = reportGenerator.generate(results);
        } catch (CustomSystemException e) {
            throw e;
        }

        ImageAnalysisReport report = ImageAnalysisReport.builder()
                .reportUuid(UUID.randomUUID())
                .jobId(job.getJobId())
                .reportOverallRiskLevel(generated.getOverallRiskLevel())
                .reportSummary(generated.getSummary())
                .reportGuidance(generated.getGuidance())
                .build();

        reportMapper.insert(report);

        jobMapper.updateStatus(job.getJobId(), "REPORT_READY");

        return reportMapper.findByJobId(job.getJobId());
    }

    @Override
    @Transactional(readOnly = true)
    public ImageAnalysisReport getFinalReport(UUID jobUuid) {

        ImageAnalysisJob job = jobMapper.findByJobUuid(jobUuid);
        if (job == null) {
            throw new CustomBusinessException(ImageErrorCode.NOT_FOUND);
        }

        ImageAnalysisReport report = reportMapper.findByJobId(job.getJobId());
        if (report == null) {
            throw new CustomBusinessException(ImageErrorCode.NOT_FOUND);
        }

        return report;
    }
}
