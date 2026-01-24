package fakehunters.backend.audio.mapper;

import fakehunters.backend.audio.domain.AudioTimeSegmentAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AudioTimeSegmentAnalysisMapper {

    void insert(AudioTimeSegmentAnalysis segment);

    void insertBatch(@Param("list") List<AudioTimeSegmentAnalysis> segments);

    List<AudioTimeSegmentAnalysis> findByAnalysisResultIdOrderByStartTimeAsc(
            @Param("analysisResultId") Long analysisResultId
    );
}
