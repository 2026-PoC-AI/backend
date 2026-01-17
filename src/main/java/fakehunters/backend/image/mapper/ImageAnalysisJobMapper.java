package fakehunters.backend.image.mapper;

import fakehunters.backend.image.domain.ImageAnalysisJob;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageAnalysisJobMapper {
    void insert(ImageAnalysisJob job); //분석 요청 insert
    ImageAnalysisJob findByJobUuid(String jobUuid); //UUID 기반 조회
}
