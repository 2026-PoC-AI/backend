package fakehunters.backend.video.mapper;

import fakehunters.backend.video.domain.DetectedArtifact;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DetectedArtifactMapper {
    void insert(DetectedArtifact artifact);

    List<DetectedArtifact> findByResultId(@Param("resultId") Long resultId);

    void deleteByResultId(@Param("resultId") Long resultId);
}