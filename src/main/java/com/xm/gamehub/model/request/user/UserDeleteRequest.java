package com.xm.gamehub.model.request.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserDeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 6125446164974256L;
    private Long userId;
} 