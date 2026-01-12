package fakehunters.backend.image.exception;

import fakehunters.backend.global.exception.custom.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {

    // 이미지 조회 관련
    NOT_FOUND("IMAGE001", "이미지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DELETED_ACCESS("IMAGE002", "삭제된 이미지입니다.", HttpStatus.GONE),

    // 이미지 권한 관련
    NO_ACCESS_PERMISSION("IMAGE003", "이미지 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NO_DELETE_PERMISSION("IMAGE004", "이미지 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 이미지 업로드 관련
    FILE_REQUIRED("IMAGE005", "파일은 필수입니다.", HttpStatus.BAD_REQUEST),
    INVALID_FILE_FORMAT("IMAGE006", "지원하지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED("IMAGE007", "파일 크기가 제한을 초과했습니다.", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_DIMENSION("IMAGE008", "이미지 크기가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

    // 딥페이크 탐지 관련
    DETECTION_FAILED("IMAGE009", "딥페이크 탐지에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_IMAGE_DATA("IMAGE010", "이미지 데이터가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

    // 이미지 처리 오류
    UPLOAD_ERROR("IMAGE011", "이미지 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PROCESSING_ERROR("IMAGE012", "이미지 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    COMPRESSION_ERROR("IMAGE013", "이미지 압축 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DELETE_ERROR("IMAGE014", "이미지 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}