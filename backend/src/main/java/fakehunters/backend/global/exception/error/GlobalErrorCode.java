package fakehunters.backend.global.exception.error;

import fakehunters.backend.global.exception.custom.ErrorCode;
import org.springframework.http.HttpStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    // 400 Bad Request
    BAD_REQUEST("G001", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("G002", "요청 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    TYPE_MISMATCH("G003", "요청 파라미터 타입이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),

    // 401 Unauthorized
    UNAUTHORIZED("G004", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),

    // 403 Forbidden
    FORBIDDEN("G005", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // 404 Not Found
    NOT_FOUND("G006", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED("G007", "허용되지 않은 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),

    // 409 Conflict
    ALREADY_EXISTS("G008", "이미 존재하는 리소스입니다.", HttpStatus.CONFLICT),

    // 500 Internal Server Error
    INTERNAL_ERROR("G009", "서버 내부 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR("G010", "데이터베이스 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // 503 Service Unavailable
    SERVICE_UNAVAILABLE("G011", "현재 서비스를 이용할 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
