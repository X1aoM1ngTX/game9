package com.xm.xmgame.model.request.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户登录请求体
 *
 * @author xm
 */
@Data
public class UserLoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 6516898732323168965L;

    private String userName;
    private String userPassword;
}
