package com.xm.xmgame.model.request.game;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 游戏状态请求体
 *
 * @author X1aoM1ngTX
 */
@Data
public class GameStatusRequest implements Serializable {
    /**
     * 游戏ID
     */
    private Long gameId;
    
    /**
     * 游戏状态
     */
    private boolean gameIsRemoved;

    @Serial
    private static final long serialVersionUID = -732424195231891767L;
} 