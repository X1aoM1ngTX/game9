package com.xm.game9.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 游戏评价视图对象
 */
@Data
public class GameReviewVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    private Long reviewId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 游戏ID
     */
    private Long gameId;

    /**
     * 评分（1-5星）
     */
    private Integer rating;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 创建时间
     */
    private Date createTime;
} 