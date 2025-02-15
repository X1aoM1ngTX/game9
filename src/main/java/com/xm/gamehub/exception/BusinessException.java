package com.xm.gamehub.exception;

import com.xm.gamehub.common.ErrorCode;
import lombok.Getter;

/**
 * 自定义业务异常
 *
 * @author X1aoM1ngTX
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;
    private final String description;

    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getError_code();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getError_code();
        this.description = description;
    }

}
