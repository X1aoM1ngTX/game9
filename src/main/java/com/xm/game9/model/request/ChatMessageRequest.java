package com.xm.game9.model.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发送消息请求
 *
 * @author X1aoM1ngTX
 */
@Data
public class ChatMessageRequest {
    
    /**
     * 接收者ID
     */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;
    
    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    /**
     * 消息类型：1-文本 2-图片 3-文件
     */
    private Integer messageType = 1;
}