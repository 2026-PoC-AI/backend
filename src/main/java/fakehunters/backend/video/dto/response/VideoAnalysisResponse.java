package fakehunters.backend.video.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VideoAnalysisResponse {
    private Long analysisId;           // 분석ID (PK)
    private String title;                // 제목
    private String status;               // 상태 (PENDING/PROCESSING/COMPLETED/FAILED)
    private LocalDateTime createdAt;         // 분석 요청 시각
    private LocalDateTime completedAt;       // 분석 완료 시각

    // 연관 데이터
    private VideoFileResponse videoFile;         // 영상 파일 정보
    private AnalysisResultResponse analysisResult;  // 분석 결과
    private List<FrameAnalysisResponse> frameAnalyses;  // 프레임별 분석
}
