package fakehunters.backend.global.exception.base;

import fakehunters.backend.global.exception.custom.ErrorCode;
import lombok.Getter;

@Getter
public abstract class BaseBusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    protected BaseBusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}