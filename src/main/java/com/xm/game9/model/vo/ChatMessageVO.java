package com.xm.game9.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 聊天消息VO
 *
 * @author X1aoM1ngTX
 */
@Data
public class ChatMessageVO {
    
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者昵称
     */
    private String senderNickname;
    
    /**
     * 发送者头像
     */
    private String senderAvatar;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型：1-文本 2-图片 3-文件
     */
    private Integer messageType;
    
    /**
     * 消息状态：0-已发送 1-已送达 2-已读
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
}