package com.xm.gamehub.model.request.game;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class GamePurchaseRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -2184316512141956L;
    private Long gameId;
} 