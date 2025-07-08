package com.xm.game9.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 好友关系表
 *
 * @author X1aoM1ngTX
 * @TableName friendRelationship
 */
@TableName(value = "friendRelationship")
@Data
public class FriendRelationship implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 关系ID
     */
    @TableId(type = IdType.AUTO)
    private Long relationshipId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 好友ID
     */
    private Long friendId;

    /**
     * 关系状态：0-待确认 1-已确认 2-已拒绝 3-已删除
     */
    private Integer friendStatus;

    /**
     * 好友备注
     */
    private String friendRemark;

    /**
     * 创建时间
     */
    private Date friendCreatedTime;

    /**
     * 更新时间
     */
    private Date friendUpdatedTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer friendIsDelete;
}