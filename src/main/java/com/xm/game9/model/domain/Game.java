package com.xm.gamehub.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * 游戏表
 *
 * @表名 game
 */
@TableName(value = "game")
@Data
public class Game implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 游戏ID
     */
    @TableId(type = IdType.AUTO)
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
     * 游戏原价
     */
    private BigDecimal gamePrice;
    /**
     * 游戏库存
     */
    private Integer gameStock;
    /**
     * 游戏创建时间
     */
    private Date gameCreatedTime;
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
     * 游戏是否在售
     */
    private Boolean gameIsRemoved;
    /**
     * 游戏打折价格
     */
    private BigDecimal gameDiscountedPrices;
    /**
     * 游戏是否正在打折
     */
    private Integer gameOnSale;
    /**
     * 游戏折扣开始时间
     */
    private Date gameSaleStartTime;
    /**
     * 游戏折扣结束时间
     */
    private Date gameSaleEndTime;
    /**
     * 游戏折扣
     */
    private BigDecimal gameDiscount;
    /**
     * 游戏封面
     */
    private String gameCover;
}