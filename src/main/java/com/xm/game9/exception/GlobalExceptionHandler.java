package com.xm.game9.exception;

import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author X1aoM1ngTX
 * @date 2024-11-05 22:43:21
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @SuppressWarnings("rawtypes")
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e) {
        log.error("BusinessException: " + e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());

    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }
}
