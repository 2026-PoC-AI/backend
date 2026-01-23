package fakehunters.backend.audio.mapper;

import fakehunters.backend.audio.domain.AudioDetectionIndicator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AudioDetectionIndicatorMapper {

    void insert(AudioDetectionIndicator indicator);

    void insertBatch(@Param("indicators") List<AudioDetectionIndicator> indicators);

    List<AudioDetectionIndicator> findByAnalysisResultId(@Param("analysisResultId") Long analysisResultId);
}