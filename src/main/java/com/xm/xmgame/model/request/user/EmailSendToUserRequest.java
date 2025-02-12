package com.xm.xmgame.model.request.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class EmailSendToUserRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -3966864978134910L;
    // 发件人组织
    private String organization;
    // 收件人邮箱
    private String email;
    // 邮件标题
    private String title;
    // 邮件正文
    private String content;
}
