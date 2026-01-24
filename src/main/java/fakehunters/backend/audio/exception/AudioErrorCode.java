package fakehunters.backend.audio.exception;

import fakehunters.backend.global.exception.custom.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AudioErrorCode implements ErrorCode {

    // 오디오 조회 관련
    NOT_FOUND("AUDIO001", "오디오를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DELETED_ACCESS("AUDIO002", "삭제된 오디오입니다.", HttpStatus.GONE),

    // 오디오 권한 관련
    NO_ACCESS_PERMISSION("AUDIO003", "오디오 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NO_DELETE_PERMISSION("AUDIO004", "오디오 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 오디오 업로드 관련
    FILE_REQUIRED("AUDIO005", "파일은 필수입니다.", HttpStatus.BAD_REQUEST),
    INVALID_FILE_FORMAT("AUDIO006", "지원하지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED("AUDIO007", "파일 크기가 제한을 초과했습니다.", HttpStatus.BAD_REQUEST),

    // 딥페이크 탐지 관련
    DETECTION_FAILED("AUDIO008", "딥페이크 탐지에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_AUDIO_DATA("AUDIO009", "오디오 데이터가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

    // 오디오 처리 오류
    UPLOAD_ERROR("AUDIO010", "오디오 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PROCESSING_ERROR("AUDIO011", "오디오 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DELETE_ERROR("AUDIO012", "오디오 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // AI 서버 연동 관련
    AI_SERVER_ERROR("AUDIO013", "AI 서버 연결에 실패했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    FILE_READ_ERROR("AUDIO014", "파일 읽기에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}