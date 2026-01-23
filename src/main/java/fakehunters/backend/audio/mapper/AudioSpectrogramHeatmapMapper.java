package fakehunters.backend.audio.mapper;

import fakehunters.backend.audio.domain.AudioSpectrogramHeatmap;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AudioSpectrogramHeatmapMapper {

    void insert(AudioSpectrogramHeatmap heatmap);

    void insertBatch(@Param("heatmaps") List<AudioSpectrogramHeatmap> heatmaps);

    List<AudioSpectrogramHeatmap> findByAnalysisResultId(@Param("analysisResultId") Long analysisResultId);
}