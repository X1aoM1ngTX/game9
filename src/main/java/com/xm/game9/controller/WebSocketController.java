package com.xm.game9.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xm.game9.model.vo.ChatMessageVO;
import com.xm.game9.service.ChatMessageService;
import com.xm.game9.service.ChatSessionService;
import com.xm.game9.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.List;
import com.xm.game9.model.domain.User;

/**
 * WebSocket消息控制器
 *
 * @author X1aoM1ngTX
 */
@Controller
@Slf4j
public class WebSocketController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ChatMessageService chatMessageService;
    
    @Autowired
    private ChatSessionService chatSessionService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SimpUserRegistry userRegistry; // 注入用户注册表，用于调试
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 处理聊天消息
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload String messagePayload) {
        try {
            log.info("收到WebSocket消息: {}", messagePayload);
            
            // 解析消息
            ChatMessageVO message = objectMapper.readValue(messagePayload, ChatMessageVO.class);
            log.info("解析后的消息: 发送者={}, 接收者={}, 内容={}", 
                message.getSenderId(), message.getReceiverId(), message.getContent());
            
            // 获取发送者信息
            User sender = userService.getById(message.getSenderId());
            if (sender != null) {
                message.setSenderNickname(sender.getUserNickname());
                message.setSenderAvatar(sender.getUserAvatar());
            }
            
            // 保存消息到数据库
            Long messageId = chatMessageService.sendMessage(
                message.getSenderId(),
                message.getReceiverId(),
                message.getContent(),
                message.getMessageType()
            );
            
            // 设置消息ID和创建时间
            message.setMessageId(messageId);
            message.setCreateTime(new Date());
            message.setStatus(0); // 已发送
            
            // 更新会话信息
            Long sessionId = chatSessionService.getOrCreateSession(
                message.getSenderId(), 
                message.getReceiverId()
            );
            chatSessionService.updateSession(sessionId, message.getContent(), message.getSenderId());
            
            // --- 增强日志：准备推送消息 ---
            log.info("准备推送消息给用户 {}: {}", message.getReceiverId(), message.getContent());
            // 检查接收者在线状态
            SimpUser simpUser = userRegistry.getUser(message.getReceiverId().toString());
            log.info("推送前检查用户 {} 在线状态: {}, 会话数: {}", 
                message.getReceiverId(), 
                simpUser != null ? "在线" : "离线",
                simpUser != null ? simpUser.getSessions().size() : 0);
            // --- 增强日志结束 ---
            
            // 尝试发送消息给接收者（如果在线）
            try {
                messagingTemplate.convertAndSendToUser(
                    message.getReceiverId().toString(),
                    "/queue/messages",
                    message
                );
                log.info("消息成功推送给用户 {}", message.getReceiverId());
            } catch (Exception e) {
                // 将日志级别提升到 ERROR，并打印完整堆栈
                log.error("向用户 {} 推送消息失败: {}", message.getReceiverId(), e.getMessage(), e);
            }
            
            // 发送确认消息给发送者
            try {
                messagingTemplate.convertAndSendToUser(
                    message.getSenderId().toString(),
                    "/queue/confirm",
                    message
                );
            } catch (Exception e) {
                log.warn("发送确认消息失败: 发送者={}, 错误={}", 
                    message.getSenderId(), e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("消息发送失败", e);
        }
    }
    
    /**
     * 推送离线消息给用户
     * 
     * @param userId 用户ID
     */
    public void pushOfflineMessages(Long userId) {
        try {
            // 获取用户的离线消息（未读消息）
            List<ChatMessageVO> offlineMessages = chatMessageService.getOfflineMessages(userId);
            
            if (offlineMessages != null && !offlineMessages.isEmpty()) {
                // 逐条推送离线消息
                for (ChatMessageVO message : offlineMessages) {
                    messagingTemplate.convertAndSendToUser(
                        userId.toString(),
                        "/queue/offline",
                        message
                    );
                }
                
                // 标记这些消息为已推送
                chatMessageService.markMessagesAsPushed(userId, offlineMessages.stream()
                    .map(ChatMessageVO::getMessageId)
                    .toArray(Long[]::new));
            }
        } catch (Exception e) {
            log.error("推送离线消息失败: userId={}", userId, e);
        }
    }
    
    /**
     * 处理消息已读回执
     */
    @MessageMapping("/chat.read")
    public void markMessageAsRead(@Payload String readPayload) {
        try {
            // 解析已读回执
            ChatMessageVO message = objectMapper.readValue(readPayload, ChatMessageVO.class);
            
            // 标记消息为已读
            chatMessageService.markMessagesAsRead(
                message.getReceiverId(),
                message.getSenderId()
            );
            
            // 清除未读消息数
            Long sessionId = chatSessionService.getOrCreateSession(
                message.getReceiverId(),
                message.getSenderId()
            );
            chatSessionService.clearUnreadCount(sessionId, message.getReceiverId());
            
            // 发送已读回执给发送者
            messagingTemplate.convertAndSendToUser(
                message.getSenderId().toString(),
                "/queue/read",
                message
            );
            
            log.info("消息已读: 发送者={}, 接收者={}", 
                message.getSenderId(), message.getReceiverId());
            
        } catch (Exception e) {
            log.error("处理消息已读失败", e);
        }
    }
}