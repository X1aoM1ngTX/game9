package com.xm.game9.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket事件监听器
 *
 * @author X1aoM1ngTX
 */
@Component
@Slf4j
public class WebSocketEventListener {
    
    @Autowired
    private OnlineStatusManager onlineStatusManager;
    
    @Autowired
    private WebSocketController webSocketController;
    
    /**
     * 连接建立事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        // 安全地获取session attributes
        if (headerAccessor.getSessionAttributes() != null) {
            Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
            
            if (userId != null) {
                onlineStatusManager.userOnline(userId);
                
                // 推送离线消息给用户
                webSocketController.pushOfflineMessages(userId);
            }
        }
    }
    
    /**
     * 连接断开事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        
        if (userId != null) {
            onlineStatusManager.userOffline(userId);
        }
    }
}