package fakehunters.backend.audio.mapper;

import fakehunters.backend.audio.domain.AudioModelPrediction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AudioModelPredictionMapper {

    void insert(AudioModelPrediction prediction);

    void insertBatch(@Param("predictions") List<AudioModelPrediction> predictions);

    List<AudioModelPrediction> findByAnalysisResultId(@Param("analysisResultId") Long analysisResultId);
}