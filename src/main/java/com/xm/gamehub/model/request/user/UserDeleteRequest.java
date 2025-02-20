package com.xm.gamehub.model.request.user;

import lombok.Data;

import java.io.Serializable;
import java.io.Serial;
@Data
public class UserDeleteRequest implements Serializable {

    private Long userId;

    @Serial
    private static final long serialVersionUID = 6125446164974256L;
} 