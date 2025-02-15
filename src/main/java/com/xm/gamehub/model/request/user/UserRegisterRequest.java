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

    private String userName;
    private String userPassword;
    private String userEmail;
    private String userPhone;
}
