package fakehunters.backend.image.mapper;

import fakehunters.backend.image.dto.response.ImageHistoryItemResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ImageAnalysisHistoryMapper {
    List<ImageHistoryItemResponse> findHistoryByJobUuids(
            @Param("jobUuids") List<UUID> jobUuids
    );
}
