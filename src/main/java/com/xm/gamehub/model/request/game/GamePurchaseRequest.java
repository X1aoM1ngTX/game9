package com.xm.gamehub.model.request.game;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

@Data
public class GamePurchaseRequest implements Serializable {
    
    private Long gameId;

    @Serial
    private static final long serialVersionUID = -2184316512141956L;
} 