package fakehunters.backend.video.exception;

import fakehunters.backend.global.exception.custom.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VideoErrorCode implements ErrorCode {

    // 비디오 조회 관련
    NOT_FOUND("VIDEO001", "비디오를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DELETED_ACCESS("VIDEO002", "삭제된 비디오입니다.", HttpStatus.GONE),

    // 비디오 권한 관련
    NO_ACCESS_PERMISSION("VIDEO003", "비디오 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NO_DELETE_PERMISSION("VIDEO004", "비디오 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 비디오 업로드 관련
    FILE_REQUIRED("VIDEO005", "파일은 필수입니다.", HttpStatus.BAD_REQUEST),
    INVALID_FILE_FORMAT("VIDEO006", "지원하지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED("VIDEO007", "파일 크기가 제한을 초과했습니다.", HttpStatus.BAD_REQUEST),
    DURATION_EXCEEDED("VIDEO008", "비디오 길이가 제한을 초과했습니다.", HttpStatus.BAD_REQUEST),

    // 딥페이크 탐지 관련
    DETECTION_FAILED("VIDEO009", "딥페이크 탐지에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_VIDEO_DATA("VIDEO010", "비디오 데이터가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    FRAME_EXTRACTION_FAILED("VIDEO011", "프레임 추출에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 비디오 처리 오류
    UPLOAD_ERROR("VIDEO012", "비디오 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PROCESSING_ERROR("VIDEO013", "비디오 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ENCODING_ERROR("VIDEO014", "비디오 인코딩 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DELETE_ERROR("VIDEO015", "비디오 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}