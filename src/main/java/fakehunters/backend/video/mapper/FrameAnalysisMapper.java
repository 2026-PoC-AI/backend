package fakehunters.backend.video.mapper;

import fakehunters.backend.video.domain.FrameAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FrameAnalysisMapper {
    void insert(FrameAnalysis frameAnalysis);
    void insertBatch(@Param("list") List<FrameAnalysis> frameAnalyses);
    FrameAnalysis findById(@Param("frameId") Long frameId);
    List<FrameAnalysis> findByResultId(@Param("resultId") Long resultId);
    List<FrameAnalysis> findSuspiciousFramesByResultId(@Param("resultId") Long resultId);

    // 프레임 이미지 조회용 메서드 추가
    FrameAnalysis findByAnalysisIdAndFrameNumber(@Param("analysisId") Long analysisId,
                                                 @Param("frameNumber") Integer frameNumber);
}
