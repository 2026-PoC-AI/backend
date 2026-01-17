package fakehunters.backend.image.mapper;

import fakehunters.backend.image.domain.ImageAnalysisResult;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ImageAnalysisResultMapper {
    void insert(ImageAnalysisResult result);
    List<ImageAnalysisResult> findByJobId(Long jobId);
}
