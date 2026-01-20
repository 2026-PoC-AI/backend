package fakehunters.backend.image.mapper;

import fakehunters.backend.image.domain.ImageAnalysisInput;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageAnalysisInputMapper {
    void insert(ImageAnalysisInput input);
    ImageAnalysisInput findByJobId(Long jobId);
}
