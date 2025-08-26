package com.xm.game9.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 聊天会话表
 *
 * @author X1aoM1ngTX
 * @TableName chat_session
 */
@TableName(value = "chat_session")
@Data
public class ChatSession implements Serializable {
    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @TableId(value = "sessionId", type = IdType.AUTO)
    private Long sessionId;

    /**
     * 用户1 ID
     */
    @TableField("user1Id")
    private Long user1Id;

    /**
     * 用户2 ID
     */
    @TableField("user2Id")
    private Long user2Id;

    /**
     * 最后一条消息
     */
    @TableField("lastMessage")
    private String lastMessage;

    /**
     * 最后消息时间
     */
    @TableField("lastMessageTime")
    private Date lastMessageTime;

    /**
     * 用户1未读数
     */
    @TableField("unreadCountUser1")
    private Integer unreadCountUser1;

    /**
     * 用户2未读数
     */
    @TableField("unreadCountUser2")
    private Integer unreadCountUser2;

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
}