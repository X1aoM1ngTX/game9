package com.xm.game9.model.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * 聊天会话VO
 *
 * @author X1aoM1ngTX
 */
@Data
public class ChatSessionVO {
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 对方用户ID
     */
    private Long friendId;
    
    /**
     * 对方用户昵称
     */
    private String friendNickname;
    
    /**
     * 对方用户头像
     */
    private String friendAvatar;
    
    /**
     * 最后一条消息
     */
    private String lastMessage;
    
    /**
     * 最后消息时间
     */
    private Date lastMessageTime;
    
    /**
     * 未读消息数
     */
    private Integer unreadCount;
    
    /**
     * 对方在线状态
     */
    private Boolean isOnline;
    
    public void setOnline(Boolean online) {
        this.isOnline = online;
    }
}