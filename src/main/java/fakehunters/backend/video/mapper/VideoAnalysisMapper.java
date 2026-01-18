package fakehunters.backend.video.mapper;

import fakehunters.backend.video.domain.VideoAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VideoAnalysisMapper {
    void insert(VideoAnalysis videoAnalysis);
    VideoAnalysis findById(@Param("analysisId") Long analysisId);
    void updateStatus(@Param("analysisId") Long analysisId, @Param("status") String status);
    void updateCompletedAt(@Param("analysisId") Long analysisId);
}