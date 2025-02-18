package com.xm.gamehub.model.request.user;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

@Data
public class SendEmailCodeRequest implements Serializable {
   
    private String userEmail;

    @Serial
    private static final long serialVersionUID = -3966864978134910L;
}
