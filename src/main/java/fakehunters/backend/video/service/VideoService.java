package fakehunters.backend.video.service;

import fakehunters.backend.global.exception.custom.CustomBusinessException;
import fakehunters.backend.global.exception.custom.CustomSystemException;
import fakehunters.backend.video.dto.response.VideoResponse;
import fakehunters.backend.video.exception.VideoErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

    // 비디오 조회 - NOT_FOUND 예외
    public VideoResponse getVideo(Long videoId) {
        // 비디오가 존재하지 않는 경우
        if (videoId == 999L) {
            throw new CustomBusinessException(VideoErrorCode.NOT_FOUND);
        }

        // 삭제된 비디오 접근
        boolean isDeleted = false; // 실제로는 DB에서 확인
        if (isDeleted) {
            throw new CustomBusinessException(VideoErrorCode.DELETED_ACCESS);
        }

        // 정상 조회
        return new VideoResponse(videoId, "sample_video.mp4", "비디오 설명", 120, false);
    }

    // 비디오 업로드 - BAD_REQUEST 예외
    public VideoResponse uploadVideo(MultipartFile file, String description) {
        // 파일 필수 검증
        if (file == null || file.isEmpty()) {
            throw new CustomBusinessException(VideoErrorCode.FILE_REQUIRED);
        }

        // 파일 형식 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("mappers/video/")) {
            throw new CustomBusinessException(VideoErrorCode.INVALID_FILE_FORMAT);
        }

        // 파일 크기 검증 (100MB 제한)
        long maxSize = 100 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new CustomBusinessException(VideoErrorCode.FILE_SIZE_EXCEEDED);
        }

        // 비디오 처리 중 시스템 에러 발생 시
        try {
            // 비디오 처리 로직
            processVideo(file);
        } catch (Exception e) {
            log.error("Video processing failed", e);
            throw new CustomSystemException(VideoErrorCode.PROCESSING_ERROR);
        }

        // 정상 업로드
        return new VideoResponse(1L, file.getOriginalFilename(), description, 120, false);
    }

    // 비디오 삭제 - FORBIDDEN 예외
    public void deleteVideo(Long videoId, Long userId) {
        // 비디오 존재 여부 확인
        if (videoId == 999L) {
            throw new CustomBusinessException(VideoErrorCode.NOT_FOUND);
        }

        // 권한 체크
        Long ownerId = 1L; // 실제로는 DB에서 조회
        if (!ownerId.equals(userId)) {
            throw new CustomBusinessException(VideoErrorCode.NO_DELETE_PERMISSION);
        }

        // 정상 삭제
        log.info("Video deleted: {}", videoId);
    }

    // 딥페이크 탐지
    public VideoResponse detectDeepfake(Long videoId) {
        // 비디오 조회
        if (videoId == 999L) {
            throw new CustomBusinessException(VideoErrorCode.NOT_FOUND);
        }

        // 비디오 데이터 유효성 검증
        boolean isValidData = true; // 실제 검증 로직
        if (!isValidData) {
            throw new CustomBusinessException(VideoErrorCode.INVALID_VIDEO_DATA);
        }

        // 딥페이크 탐지 실패 시
        try {
            // AI 모델 호출
            boolean isDeepfake = runDetectionModel(videoId);
            return new VideoResponse(videoId, "sample_video.mp4", "탐지 완료", 120, isDeepfake);
        } catch (Exception e) {
            log.error("Deepfake detection failed", e);
            throw new CustomSystemException(VideoErrorCode.DETECTION_FAILED);
        }
    }

    // 프레임 추출
    public void extractFrames(Long videoId) {
        try {
            // 프레임 추출 로직
            log.info("Extracting frames from video: {}", videoId);
        } catch (Exception e) {
            log.error("Frame extraction failed", e);
            throw new CustomSystemException(VideoErrorCode.FRAME_EXTRACTION_FAILED);
        }
    }

    // private 헬퍼 메서드들
    private void processVideo(MultipartFile file) {
        // 비디오 처리 로직
    }

    private boolean runDetectionModel(Long videoId) {
        // AI 모델 실행
        return false; // 딥페이크 여부
    }
}