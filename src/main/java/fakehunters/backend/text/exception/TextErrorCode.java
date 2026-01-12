package fakehunters.backend.text.exception;

import fakehunters.backend.global.exception.custom.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TextErrorCode implements ErrorCode {

    // 텍스트 조회 관련
    NOT_FOUND("TEXT001", "텍스트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DELETED_ACCESS("TEXT002", "삭제된 텍스트입니다.", HttpStatus.GONE),

    // 텍스트 권한 관련
    NO_ACCESS_PERMISSION("TEXT003", "텍스트 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NO_DELETE_PERMISSION("TEXT004", "텍스트 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 텍스트 입력 관련
    CONTENT_REQUIRED("TEXT005", "내용은 필수입니다.", HttpStatus.BAD_REQUEST),
    CONTENT_TOO_SHORT("TEXT006", "내용이 너무 짧습니다.", HttpStatus.BAD_REQUEST),
    CONTENT_TOO_LONG("TEXT007", "내용이 너무 깁니다.", HttpStatus.BAD_REQUEST),
    INVALID_ENCODING("TEXT008", "지원하지 않는 인코딩입니다.", HttpStatus.BAD_REQUEST),

    // 딥페이크/페이크뉴스 탐지 관련
    DETECTION_FAILED("TEXT009", "가짜 뉴스 탐지에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_TEXT_DATA("TEXT010", "텍스트 데이터가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    LANGUAGE_NOT_SUPPORTED("TEXT011", "지원하지 않는 언어입니다.", HttpStatus.BAD_REQUEST),

    // 텍스트 처리 오류
    SAVE_ERROR("TEXT012", "텍스트 저장 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PROCESSING_ERROR("TEXT013", "텍스트 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ANALYSIS_ERROR("TEXT014", "텍스트 분석 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DELETE_ERROR("TEXT015", "텍스트 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}