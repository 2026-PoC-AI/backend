package fakehunters.backend.global.exception.custom;

import fakehunters.backend.global.exception.base.BaseBusinessException;
import lombok.Getter;

@Getter
public class CustomBusinessException extends BaseBusinessException {

    public CustomBusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}