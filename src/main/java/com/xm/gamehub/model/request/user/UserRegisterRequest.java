package com.xm.gamehub.model.request.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author X1aoM1ngTX
 */
@Data
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7984864914864910L;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String userCheckPassword;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 验证码
     */
    private String verifyCode;
}
