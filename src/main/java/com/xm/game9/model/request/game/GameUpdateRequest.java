package com.xm.game9.model.request.game;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * 游戏更新请求
 *
 * @author X1aoM1ngTX
 */
@Data
public class GameUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 游戏ID
     */
    private Long gameId;

    /**
     * 游戏名
     */
    private String gameName;

    /**
     * 游戏描述
     */
    private String gameDescription;

    /**
     * 游戏价格
     */
    private String gamePrice;

    /**
     * 游戏库存
     */
    private Integer gameStock;

    /**
     * 游戏发行时间
     */
    private LocalDate gameReleaseDate;

    /**
     * 游戏开发商
     */
    private String gameDev;

    /**
     * 游戏发行商
     */
    private String gamePub;

    /**
     * 是否开启折扣
     */
    private Boolean gameOnSale;

    /**
     * 折扣开始时间
     */
    private Date gameSaleStartTime;

    /**
     * 折扣结束时间
     */
    private Date gameSaleEndTime;

    /**
     * 折扣值
     */
    private BigDecimal gameDiscount;

    /**
     * 游戏封面
     */
    private String gameCover;
}
