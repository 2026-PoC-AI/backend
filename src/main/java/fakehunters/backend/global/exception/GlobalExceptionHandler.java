package fakehunters.backend.global.exception;

import fakehunters.backend.global.exception.base.BaseBusinessException;
import fakehunters.backend.global.exception.base.BaseSystemException;
import fakehunters.backend.global.exception.error.GlobalErrorCode;
import fakehunters.backend.global.exception.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 비즈니스 예외 처리 - ErrorCode의 HttpStatus 사용
    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BaseBusinessException e) {
        log.warn("Business Exception: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.from(e.getErrorCode());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(errorResponse);
    }

    // 시스템 예외 처리 - 항상 500 반환
    @ExceptionHandler(BaseSystemException.class)
    public ResponseEntity<ErrorResponse> handleSystemException(BaseSystemException e) {
        log.error("System Exception: {}", e.getMessage(), e);
        ErrorResponse errorResponse = ErrorResponse.from(e.getErrorCode());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    // 유효성 검사 실패 (DTO @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getAllErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("유효성 검사에 실패했습니다.");

        log.warn("Validation Exception: {}", message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_ERROR", message));
    }

    // @RequestParam, @PathVariable 등 타입 불일치
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Type Mismatch Exception: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.from(GlobalErrorCode.TYPE_MISMATCH));
    }

    // 지원하지 않는 HTTP Method 요청
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.warn("Method Not Allowed Exception: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.from(GlobalErrorCode.METHOD_NOT_ALLOWED));
    }

    // IllegalArgumentException 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("IllegalArgumentException: {}", e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("ILLEGAL_ARGUMENT", e.getMessage()));
    }

    // 클라이언트 연결 중단 예외 처리 - 비디오 스트리밍 중 정상적으로 발생 가능
    @ExceptionHandler({
            ClientAbortException.class,
            AsyncRequestNotUsableException.class
    })
    public void handleClientAbortException(Exception e) {
        log.debug("Client disconnected: {}", e.getMessage());
        // 응답하지 않음 - 클라이언트가 이미 연결을 끊었기 때문
    }

    // 예상치 못한 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Exception: {}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.from(GlobalErrorCode.INTERNAL_SERVER_ERROR));
    }
}