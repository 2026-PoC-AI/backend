package fakehunters.backend.audio.mapper;

import fakehunters.backend.audio.domain.AudioFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;

@Mapper
public interface AudioFileMapper {

    void insert(AudioFile audioFile);

    Optional<AudioFile> findById(@Param("id") Long id);

    List<AudioFile> findAllOrderByCreatedAtDesc();

    List<AudioFile> findByStatus(@Param("status") String status);

    void updateStatus(@Param("id") Long id, @Param("status") String status);

    void deleteById(@Param("id") Long id);
}