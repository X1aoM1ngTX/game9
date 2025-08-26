package com.xm.game9.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 聊天消息表
 *
 * @author X1aoM1ngTX
 * @TableName chat_message
 */
@TableName(value = "chat_message")
@Data
public class ChatMessage implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(value = "messageId", type = IdType.AUTO)
    private Long messageId;

    /**
     * 发送者ID
     */
    @TableField("senderId")
    private Long senderId;

    /**
     * 接收者ID
     */
    @TableField("receiverId")
    private Long receiverId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：1-文本 2-图片 3-文件
     */
    @TableField("messageType")
    private Integer messageType;

    /**
     * 消息状态：0-已发送 1-已送达 2-已读
     */
    @TableField("messageStatus")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("createTime")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("updateTime")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableField("isDeleted")
    private Integer isDeleted;
}