package com.xm.gamehub.model.request.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class EmailSendToUserRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -3966864978134910L;
    // 收件人邮箱
    private String toEmail;
}
