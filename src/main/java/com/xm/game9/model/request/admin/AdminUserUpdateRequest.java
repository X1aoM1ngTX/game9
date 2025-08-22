package com.xm.game9.model.request.admin;

import jakarta.validation.constraints.Pattern;
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
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String userPhone;
    
    private Integer userIsAdmin;
}