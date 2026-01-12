package fakehunters.backend.global.exception.response;


import fakehunters.backend.global.exception.custom.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;

    // ErrorCode에서 생성
    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    // 커스텀 코드와 메시지로 생성
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message);
    }
}
