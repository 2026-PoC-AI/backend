package fakehunters.backend.audio.mapper;

import fakehunters.backend.audio.dto.internal.AudioAnalysisResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AudioAnalysisResultMapper {

    long insert(AudioAnalysisResultInsertParam param);

    AudioAnalysisResult findById(@Param("id") long id);
}
