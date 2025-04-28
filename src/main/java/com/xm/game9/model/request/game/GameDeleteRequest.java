package com.xm.gamehub.model.request.game;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class GameDeleteRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 515684984113289L;
    private Long gameId;
} 