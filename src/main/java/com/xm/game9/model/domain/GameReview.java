package com.xm.game9.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 游戏评价表
 *
 * @表名 gameReview
 */
@TableName(value = "gameReview")
@Data
public class GameReview implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
     * 评价ID
     */
    @TableId(type = IdType.AUTO)
    private Long reviewId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 游戏ID
     */
    private Long gameId;
    
    /**
     * 评分（1-5分，支持0.5分）
     */
    private BigDecimal rating;
    
    /**
     * 评价内容
     */
    private String content;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDeleted;
} 