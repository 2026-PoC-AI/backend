package fakehunters.backend.audio.mapper;

import fakehunters.backend.audio.domain.AudioAnalysisResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Optional;

@Mapper
public interface AudioAnalysisResultMapper {

    void insert(AudioAnalysisResult result);

    Optional<AudioAnalysisResult> findById(@Param("id") Long id);

    Optional<AudioAnalysisResult> findByAudioFileId(@Param("audioFileId") Long audioFileId);

    boolean existsByAudioFileId(@Param("audioFileId") Long audioFileId);
}