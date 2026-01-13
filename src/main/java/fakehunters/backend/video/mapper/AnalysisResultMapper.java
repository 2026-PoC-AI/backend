package fakehunters.backend.video.mapper;

import fakehunters.backend.video.domain.AnalysisResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnalysisResultMapper {
    void insert(AnalysisResult analysisResult);
    AnalysisResult findById(@Param("resultId") String resultId);
    AnalysisResult findByAnalysisId(@Param("analysisId") String analysisId);
}