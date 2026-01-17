package fakehunters.backend.image.service;

import fakehunters.backend.image.dto.request.ImageAnalyzeRequest;
import fakehunters.backend.image.dto.response.ImageAnalyzeResponse;

// 이미지 분석 도메인의 핵심 유스케이스를 정의하는 인터페이스
public interface ImageAnalysisService {
    /**
     * 이미지 분석 Job을 생성한다.
     * @param request 이미지 분석 요청 정보 (s3Key 등)
     * @return 생성된 분석 Job UUID
     */
    ImageAnalyzeResponse createAnalysis(ImageAnalyzeRequest request);
}
