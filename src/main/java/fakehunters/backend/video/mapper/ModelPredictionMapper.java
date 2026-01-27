package fakehunters.backend.video.mapper;

import fakehunters.backend.video.domain.ModelPrediction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ModelPredictionMapper {
    void insert(ModelPrediction prediction);

    List<ModelPrediction> findByResultId(@Param("resultId") Long resultId);

    void deleteByResultId(@Param("resultId") Long resultId);
}