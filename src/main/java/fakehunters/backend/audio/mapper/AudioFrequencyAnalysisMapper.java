package fakehunters.backend.audio.mapper;

import fakehunters.backend.audio.domain.AudioFrequencyAnalysis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AudioFrequencyAnalysisMapper {

    void insert(AudioFrequencyAnalysis analysis);

    void insertBatch(@Param("analyses") List<AudioFrequencyAnalysis> analyses);

    List<AudioFrequencyAnalysis> findByAnalysisResultIdOrderByFrequencyBandAsc(
            @Param("analysisResultId") Long analysisResultId
    );
}