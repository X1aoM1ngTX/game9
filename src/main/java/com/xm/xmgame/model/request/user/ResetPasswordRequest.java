package com.xm.xmgame.model.request.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ResetPasswordRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private String email;
    private String verifyCode;
    private String newPassword;
} 