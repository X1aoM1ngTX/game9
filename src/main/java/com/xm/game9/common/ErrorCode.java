package com.xm.game9.common;

import lombok.Getter;

/**
 * 错误码
 *
 * @author X1aoM1ngTX
 */
@Getter
public enum ErrorCode {
    SUCCESS(0, "ok", "成功"),
    PARAMS_ERROR(40000, "请求参数错误", "请求参数错误"),
    NULL_ERROR(40001, "请求参数为空", "请求参数为空"),
    NOT_LOGIN(40100, "未登录", "未登录"),
    NO_AUTH(40101, "用户无权限", "用户无权限"),
    SYSTEM_ERROR(50000, "系统内部异常", "系统内部异常"),
    NOT_FOUND_ERROR(50001, "未发现", "未发现"),
    GAME_EXIST(50002, "游戏已存在", "游戏已存在"),
    OPERATION_ERROR(50003, "操作失败", "操作失败");

    private final int errorCode;
    private final String message;
    private final String description;

    ErrorCode(int errorCode, String message, String description) {
        this.errorCode = errorCode;
        this.message = message;
        this.description = description;
    }

}
