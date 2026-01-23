package fakehunters.backend.audio.mapper;

import fakehunters.backend.audio.domain.AudioSystemSettings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Optional;

@Mapper
public interface AudioSystemSettingsMapper {

    Optional<AudioSystemSettings> findByKey(@Param("key") String key);

    void updateValue(@Param("key") String key, @Param("value") String value);
}