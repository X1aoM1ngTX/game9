package com.xm.game9.model.request.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 游戏评价请求体
 *
 * @author X1aoM1ngTX
 */
@Schema(description = "游戏评价请求")
@Data
public class GameReviewRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 游戏ID
     */
    @Schema(description = "游戏ID")
    private Long gameId;

    /**
     * 评分（1-5星）
     */
    @Schema(description = "评分（1-5星）")
    private Integer rating;

    /**
     * 评价内容
     */
    @Schema(description = "评价内容")
    private String content;
} 