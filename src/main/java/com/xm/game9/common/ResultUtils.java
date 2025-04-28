package com.xm.game9.common;

/**
 * 封装返回类
 * author: xmcode
 * update: 2024-11-05 22:42:35
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
     * @param Code
     * @param message
     * @param description
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static BaseResponse error(int Code, String message, String description) {
        return new BaseResponse(Code, null, message, description);
    }

    /**
     * @param errorCode
     * @param message
     * @param description
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static BaseResponse error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse(errorCode.getError_code(), null, message, description);
    }

    /**
     * @param errorCode
     * @param description
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse(errorCode.getError_code(), null, errorCode.getMessage(), description);
    }
}
