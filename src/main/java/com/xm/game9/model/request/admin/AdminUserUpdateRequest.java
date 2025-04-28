package com.xm.game9.model.request.admin;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 管理员用户信息修改请求体
 *
 * @author X1aoM1ngTX
 */
@Data
public class AdminUserUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7984864914864910L;

    private Long userId;
    private String userName;
    private String userNickname;
    private String userEmail;
    private String userPhone;
    private Integer userIsAdmin;
}