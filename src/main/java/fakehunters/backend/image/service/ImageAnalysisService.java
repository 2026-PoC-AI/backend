package fakehunters.backend.image.service;

import fakehunters.backend.image.dto.request.DeepfakeResultRequest;
import fakehunters.backend.image.dto.request.ImageAnalyzeRequest;
import fakehunters.backend.image.dto.response.DeepfakeResultResponse;
import fakehunters.backend.image.dto.response.ImageAnalysisQueryResponse;
import fakehunters.backend.image.dto.response.ImageAnalyzeResponse;

import java.util.UUID;

// 이미지 분석 도메인의 핵심 유스케이스를 정의하는 인터페이스
public interface ImageAnalysisService {
    //이미지 분석 요청 생성
    ImageAnalyzeResponse createAnalysis(ImageAnalyzeRequest request);
    //딥페이크 분석 결과 저장  (FastAPI → Spring 결과 수신)
    DeepfakeResultResponse saveDeepfakeResult(DeepfakeResultRequest request);
    //분석결과 가져오기
    ImageAnalysisQueryResponse getAnalysisResult(UUID jobUuid);
}
