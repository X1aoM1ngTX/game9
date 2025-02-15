package com.xm.gamehub.common;

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
    NO_AUTH(40101, "无权限", "无权限"),
    SYSTEM_ERROR(50000, "系统内部异常", "系统内部异常"),
    NOT_FOUND_ERROR(50001, "未发现", "未发现"),
    GAME_EXIST(50002, "游戏已存在", "游戏已存在");

    private final int error_code;
    private final String message;
    private final String description;

    ErrorCode(int error_code, String message, String description) {
        this.error_code = error_code;
        this.message = message;
        this.description = description;
    }

}
