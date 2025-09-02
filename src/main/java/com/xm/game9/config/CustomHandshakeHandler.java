package com.xm.game9.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * 自定义握手处理器，用于将用户信息绑定到WebSocket会话
 *
 * @author X1aoM1ngTX
 */
@Component
@Slf4j
public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    
    @Override
    protected Principal determineUser(ServerHttpRequest request, 
                                    WebSocketHandler wsHandler, 
                                    Map<String, Object> attributes) {
        
        log.info("CustomHandshakeHandler 开始处理用户身份确定");
        
        // 从attributes中获取userId（由握手拦截器设置）
        Long userId = (Long) attributes.get("userId");
        if (userId != null) {
            log.info("从attributes获取到userId: {}", userId);
            
            // 创建自定义Principal
            return new WebSocketPrincipal(userId);
        }
        
        log.warn("未能从attributes中获取到userId");
        return null;
    }
    
    /**
     * 自定义WebSocket Principal，包含用户ID
     */
    public static class WebSocketPrincipal implements Principal {
        private final Long userId;
        
        public WebSocketPrincipal(Long userId) {
            this.userId = userId;
        }
        
        @Override
        public String getName() {
            return userId.toString();
        }
        
        public Long getUserId() {
            return userId;
        }
    }
}