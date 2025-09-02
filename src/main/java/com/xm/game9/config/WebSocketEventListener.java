package com.xm.game9.config;

import com.xm.game9.controller.WebSocketController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * WebSocket事件监听器
 * 用于处理连接建立和断开事件，更新用户在线状态
 *
 */
@Component
@Slf4j
public class WebSocketEventListener {

    @Autowired
    private OnlineStatusManager onlineStatusManager;
    
    @Autowired
    private WebSocketController webSocketController;
    
    // --- 添加计数器 ---
    private static final AtomicLong connectCounter = new AtomicLong(0);
    private static final AtomicLong disconnectCounter = new AtomicLong(0);

    /**
     * 连接建立事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        long eventId = WebSocketEventListener.connectCounter.incrementAndGet();
        long threadId = Thread.currentThread().getId();
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        log.info(">>> [{}] 连接事件开始 (线程ID: {})", eventId, threadId);
        log.info(">>> [{}] 会话ID: {}", eventId, headerAccessor.getSessionId());

        // 尝试获取 Principal
        java.security.Principal principal = headerAccessor.getUser();
        Long userIdFromPrincipal = null;
        if (principal != null) {
            log.info(">>> [{}] 连接时 Principal: Name={}, Class={}", eventId, principal.getName(), principal.getClass().getName());
            
            // 检查是否是我们的自定义WebSocketPrincipal
            if (principal instanceof com.xm.game9.config.CustomHandshakeHandler.WebSocketPrincipal) {
                com.xm.game9.config.CustomHandshakeHandler.WebSocketPrincipal wsPrincipal = 
                    (com.xm.game9.config.CustomHandshakeHandler.WebSocketPrincipal) principal;
                userIdFromPrincipal = wsPrincipal.getUserId();
                log.info(">>> [{}] 从自定义WebSocketPrincipal获取到userId: {}", eventId, userIdFromPrincipal);
            } else {
                // 尝试从 Principal 的 name 中解析 userId
                try {
                    userIdFromPrincipal = Long.parseLong(principal.getName());
                    log.info(">>> [{}] 从 Principal Name 解析出 userId: {}", eventId, userIdFromPrincipal);
                } catch (NumberFormatException e) {
                    log.warn(">>> [{}] Principal Name '{}' 无法解析为 Long", eventId, principal.getName());
                }
            }
        } else {
            log.warn(">>> [{}] 连接时 Principal 为 null", eventId);
        }

        // --- 关键修改：更可靠地获取 Session Attributes ---
        // 1. 首先尝试从 StompHeaderAccessor 获取
        Map<String, Object> attributes = headerAccessor.getSessionAttributes();
        
        Long userId = null;
        if (attributes != null) {
            log.info(">>> [{}] 从 StompHeaderAccessor 获取到 Session Attributes", eventId);
            userId = (Long) attributes.get("userId");
            if (userId != null) {
                log.info(">>> [{}] 从 Session Attributes 获取到 userId: {}", eventId, userId);
            } else {
                log.warn(">>> [{}] Session Attributes 中未找到 key 'userId'", eventId);
                log.debug(">>> [{}] Session Attributes 内容: {}", eventId, attributes);
            }
        } else {
            log.warn(">>> [{}] StompHeaderAccessor.getSessionAttributes() 返回 null", eventId);
        }
        
        // 如果从 attributes 获取失败，则使用从 principal 解析的 userId
        if (userId == null && userIdFromPrincipal != null) {
            userId = userIdFromPrincipal;
            log.info(">>> [{}] 使用从 Principal 解析的 userId: {}", eventId, userId);
        }

        if (userId != null) {
            log.info(">>> [{}] WebSocket 连接建立，准备将用户 {} 标记为在线", eventId, userId);
            onlineStatusManager.userOnline(userId);
            
            // 推送离线消息
            log.info(">>> [{}] 准备为用户 {} 推送离线消息", eventId, userId);
            webSocketController.pushOfflineMessages(userId);
        } else {
            log.error(">>> [{}] WebSocket 连接建立，但无法获取 userId。用户将无法被标记为在线，消息推送会失败。", eventId);
            if (attributes == null && principal == null) {
                log.error(">>> [{}] attributes 和 principal 均为 null。", eventId);
            }
        }
        
        log.info(">>> [{}] 连接事件结束 (线程ID: {})", eventId, threadId);
    }

    /**
     * 连接断开事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        long eventId = WebSocketEventListener.disconnectCounter.incrementAndGet();
        long threadId = Thread.currentThread().getId();
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        log.info(">>> [{}] 断开事件开始 (线程ID: {})", eventId, threadId);
        log.info(">>> [{}] 会话ID: {}, CloseStatus: {}", eventId, headerAccessor.getSessionId(), event.getCloseStatus());

        // 尝试获取 Principal
        java.security.Principal principal = headerAccessor.getUser();
        Long userId = null;
        
        if (principal != null) {
            log.info(">>> [{}] 断开时 Principal: Name={}, Class={}", eventId, principal.getName(), principal.getClass().getName());
            
            // 检查是否是我们的自定义WebSocketPrincipal
            if (principal instanceof com.xm.game9.config.CustomHandshakeHandler.WebSocketPrincipal) {
                com.xm.game9.config.CustomHandshakeHandler.WebSocketPrincipal wsPrincipal = 
                    (com.xm.game9.config.CustomHandshakeHandler.WebSocketPrincipal) principal;
                userId = wsPrincipal.getUserId();
                log.info(">>> [{}] 从自定义WebSocketPrincipal获取到userId: {}", eventId, userId);
            } else {
                // 尝试从 Principal 的 name 中解析 userId
                try {
                    userId = Long.parseLong(principal.getName());
                    log.info(">>> [{}] 从 Principal Name 解析出 userId: {}", eventId, userId);
                } catch (NumberFormatException e) {
                    log.warn(">>> [{}] Principal Name '{}' 无法解析为 Long", eventId, principal.getName());
                }
            }
        } else {
            log.warn(">>> [{}] 断开时 Principal 为 null", eventId);
        }

        // 如果从Principal获取失败，尝试从session attributes获取
        if (userId == null) {
            Map<String, Object> attributes = headerAccessor.getSessionAttributes();
            if (attributes != null) {
                userId = (Long) attributes.get("userId");
                if (userId != null) {
                    log.info(">>> [{}] 从Session Attributes获取到userId: {}", eventId, userId);
                } else {
                    log.warn(">>> [{}] Session Attributes中未找到userId", eventId);
                    log.warn(">>> [{}] Session Attributes 内容: {}", eventId, attributes.keySet());
                }
            } else {
                log.warn(">>> [{}] Session Attributes为null", eventId);
            }
        }
        
        // 标记用户离线
        if (userId != null) {
            log.info(">>> [{}] WebSocket 连接断开，准备将用户 {} 标记为离线", eventId, userId);
            onlineStatusManager.userOffline(userId);
        } else {
            log.error(">>> [{}] WebSocket 连接断开，但无法获取userId", eventId);
        }
        
        log.info(">>> [{}] 断开事件结束 (线程ID: {})", eventId, threadId);
    }
    
}