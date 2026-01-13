package fakehunters.backend.video.mapper;

import fakehunters.backend.video.domain.FrameAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FrameAnalysisMapper {
    void insert(FrameAnalysis frameAnalysis);
    void insertBatch(@Param("list") List<FrameAnalysis> frameAnalyses);
    FrameAnalysis findById(@Param("frameId") String frameId);
    List<FrameAnalysis> findByResultId(@Param("resultId") String resultId);
    List<FrameAnalysis> findSuspiciousFramesByResultId(@Param("resultId") String resultId);
}
