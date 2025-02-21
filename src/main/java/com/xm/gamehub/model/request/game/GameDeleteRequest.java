package com.xm.gamehub.model.request.game;

import lombok.Data;
import java.io.Serializable;
import java.io.Serial;

@Data
public class GameDeleteRequest implements Serializable {
    private Long gameId;
    
    @Serial
    private static final long serialVersionUID = 515684984113289L;
} 