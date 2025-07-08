package com.xm.game9.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 好友分组表
 *
 * @表名 friendGroup
 */
@TableName(value = "friendGroup")
@Data
public class FriendGroup implements Serializable {

    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 分组ID
     */
    @TableId(type = IdType.AUTO)
    private Long groupId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 分组排序
     */
    private Integer groupOrder;

    /**
     * 创建时间
     */
    private Date groupCreateTime;

    /**
     * 更新时间
     */
    private Date groupUpdateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer groupIsDelete;
}