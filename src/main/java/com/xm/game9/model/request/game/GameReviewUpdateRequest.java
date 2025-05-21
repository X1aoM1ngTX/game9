package com.xm.game9.model.request.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 游戏评价更新请求体
 *
 * @author X1aoM1ngTX
 */
@Schema(description = "游戏评价更新请求")
@Data
public class GameReviewUpdateRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    @Schema(description = "评价ID")
    private Long reviewId;

    /**
     * 评分（1-5分，支持0.5分）
     */
    @Schema(description = "评分（1-5分，支持0.5分）")
    private BigDecimal rating;

    /**
     * 评价内容
     */
    @Schema(description = "评价内容")
    private String content;
} 