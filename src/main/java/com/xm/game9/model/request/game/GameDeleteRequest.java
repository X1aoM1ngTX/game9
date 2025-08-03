package com.xm.game9.model.request.game;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 游戏删除请求体
 *
 * @author X1aoM1ngTX
 */
@Schema(description = "游戏删除请求")
@Data
public class GameDeleteRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 515684984113289L;
    private Long gameId;
} 