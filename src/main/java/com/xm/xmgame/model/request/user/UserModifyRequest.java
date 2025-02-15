package com.xm.xmgame.model.request.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户信息修改请求体
 *
 * @author X1aoM1ngTX
 */
@Data
public class UserModifyRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7984864914864910L;

    private String userName;
    private String userEmail;
    private String userPhone;
    private String userProfile;
}