package fakehunters.backend.image.mapper;

import fakehunters.backend.image.domain.ImageAnalysisResult;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageAnalysisResultMapper {
    void insert(ImageAnalysisResult result);
}
