package com.xm.game9.common;

/**
 * 封装返回类
 * 
 * @author X1aoM1ngTX
 * @createDate 2024-11-05
 */
public class ResultUtils {

    /**
     * 返回成功
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok", "成功");
    }

    /**
     * 返回错误码
     *
     * @param errorCode
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * @param code
     * @param message
     * @param description
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse(code, null, message, description);
    }

    /**
     * @param errorCode
     * @param message
     * @param description
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static BaseResponse error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse(errorCode.getErrorCode(), null, message, description);
    }

    /**
     * @param errorCode
     * @param description
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse(errorCode.getErrorCode(), null, errorCode.getMessage(), description);
    }
}
