package com.xm.gamehub.model.request.userLibrary;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户游戏库请求体
 *
 * @author X1aoM1ngTX
 */
@Data
public class UserGameRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -81234816125131L;

    private Long gameId;
} 