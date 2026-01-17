package fakehunters.backend.image.service;


import fakehunters.backend.image.domain.ImageAnalysisInput;
import fakehunters.backend.image.domain.ImageAnalysisJob;
import fakehunters.backend.image.dto.request.ImageAnalyzeRequest;
import fakehunters.backend.image.dto.response.ImageAnalyzeResponse;
import fakehunters.backend.image.mapper.ImageAnalysisInputMapper;
import fakehunters.backend.image.mapper.ImageAnalysisJobMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageAnalysisServiceImpl implements ImageAnalysisService {
    private final ImageAnalysisJobMapper jobMapper;
    private final ImageAnalysisInputMapper inputMapper;

    @Override
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

        // 외부로 노출되는 ID는 UUID만 사용
        return new ImageAnalyzeResponse(job.getJobUuid());
    }
}
