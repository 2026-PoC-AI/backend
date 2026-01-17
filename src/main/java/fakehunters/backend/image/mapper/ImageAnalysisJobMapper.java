package fakehunters.backend.image.mapper;

import fakehunters.backend.image.domain.ImageAnalysisJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface ImageAnalysisJobMapper {
    void insert(ImageAnalysisJob job); //분석 요청 insert
    ImageAnalysisJob findByJobUuid(UUID jobUuid); //UUID 기반 조회
    void updateStatus( //요청 상태 업데이트
            @Param("jobId") Long jobId,
            @Param("jobStatus") String jobStatus
    );
}
