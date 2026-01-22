package fakehunters.backend.image.mapper;

import fakehunters.backend.image.domain.ImageAnalysisArtifact;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ImageAnalysisArtifactMapper {
    void insert(ImageAnalysisArtifact artifact);
    List<ImageAnalysisArtifact> findByJobId(Long jobId);
}
