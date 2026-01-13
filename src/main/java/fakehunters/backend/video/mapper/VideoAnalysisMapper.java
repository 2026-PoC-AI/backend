package fakehunters.backend.video.mapper;

import fakehunters.backend.video.domain.VideoAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VideoAnalysisMapper {
    void insert(VideoAnalysis videoAnalysis);
    VideoAnalysis findById(@Param("analysisId") String analysisId);
    void updateStatus(@Param("analysisId") String analysisId, @Param("status") String status);
    void updateCompletedAt(@Param("analysisId") String analysisId);
}