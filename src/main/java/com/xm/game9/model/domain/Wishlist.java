package com.xm.game9.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 愿望单
 *
 * @TableName wishlist
 */
@TableName(value = "wishlist")
@Data
public class Wishlist implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 愿望单id
     */
    @TableId(type = IdType.AUTO)
    private Long wishlistId;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 游戏id
     */
    private Long gameId;
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
    private Integer isDeleted;
}
