package fakehunters.backend.video.mapper;

import fakehunters.backend.video.domain.VideoFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VideoFileMapper {
    void insert(VideoFile videoFile);
    VideoFile findById(@Param("fileId") String fileId);
    VideoFile findByAnalysisId(@Param("analysisId") String analysisId);
}
