package fakehunters.backend.video.service;

import fakehunters.backend.video.domain.VideoFile;
import fakehunters.backend.video.dto.response.VideoFileResponse;
import fakehunters.backend.video.exception.VideoErrorCode;
import fakehunters.backend.video.mapper.VideoFileMapper;
import fakehunters.backend.global.exception.custom.CustomSystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoFileService {

    private final VideoFileMapper videoFileMapper;

    /**
     * 파일 ID(PK)로 비디오 파일 정보 조회
     */
    @Transactional(readOnly = true)
    public VideoFileResponse getVideoFile(Long fileId) { // String -> Long 변경
        VideoFile videoFile = videoFileMapper.findById(fileId);
        if (videoFile == null) {
            throw new CustomSystemException(VideoErrorCode.NOT_FOUND);
        }

        return convertToResponse(videoFile);
    }

    /**
     * 분석 작업 ID(FK)로 연결된 비디오 파일 정보 조회
     */
    @Transactional(readOnly = true)
    public VideoFileResponse getVideoFileByAnalysisId(Long analysisId) { // String -> Long 변경
        VideoFile videoFile = videoFileMapper.findByAnalysisId(analysisId);
        if (videoFile == null) {
            throw new CustomSystemException(VideoErrorCode.NOT_FOUND);
        }

        return convertToResponse(videoFile);
    }

    /**
     * 도메인 객체를 응답 DTO로 변환
     */
    private VideoFileResponse convertToResponse(VideoFile file) {
        return VideoFileResponse.builder()
                .fileId(file.getFileId())
                .analysisId(file.getAnalysisId()) // 연관된 분석 ID 포함
                .originalFilename(file.getOriginalFilename())
                .storedFilename(file.getStoredFilename())
                .filePath(file.getFilePath())
                .fileSize(file.getFileSize())
                .durationSeconds(file.getDurationSeconds())
                .resolution(file.getResolution())
                .format(file.getFormat())
                .fps(file.getFps())
                .uploadedAt(file.getUploadedAt())
                .build();
    }
}