package fakehunters.backend.global.exception.custom;

import fakehunters.backend.global.exception.base.BaseSystemException;
import lombok.Getter;

@Getter
public class CustomSystemException extends BaseSystemException {

    public CustomSystemException(ErrorCode errorCode) {
        super(errorCode);
    }
}