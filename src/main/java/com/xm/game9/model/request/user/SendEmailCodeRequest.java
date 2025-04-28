package com.xm.game9.model.request.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class SendEmailCodeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -3966864978134910L;
    private String userEmail;
}
