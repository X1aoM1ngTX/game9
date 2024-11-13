package com.xm.xmgame.model.domain;

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
 * @TableName game
 */
@TableName(value ="game")
@Data
public class Game implements Serializable {
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
     * 游戏价格
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
     * 游戏是否下架
     */
    private Boolean gameIsRemoved;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}