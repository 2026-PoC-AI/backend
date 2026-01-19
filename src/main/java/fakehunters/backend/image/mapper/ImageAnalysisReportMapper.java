package fakehunters.backend.image.mapper;

import fakehunters.backend.image.domain.ImageAnalysisReport;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageAnalysisReportMapper {
    void insert(ImageAnalysisReport report);
    ImageAnalysisReport findByJobId(Long jobId);
}
