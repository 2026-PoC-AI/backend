package fakehunters.backend.video.mapper;

import fakehunters.backend.video.domain.VideoFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VideoFileMapper {
    void insert(VideoFile videoFile);
    VideoFile findById(@Param("fileId") Long fileId);
    VideoFile findByAnalysisId(@Param("analysisId") Long analysisId);
    void updateWebFilePath(@Param("analysisId") Long analysisId, @Param("webFilePath") String webFilePath);
}
