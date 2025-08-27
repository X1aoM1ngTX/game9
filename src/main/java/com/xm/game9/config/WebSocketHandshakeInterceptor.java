package com.xm.game9.config;

import com.xm.game9.model.domain.User;
import com.xm.game9.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器
 *
 * @author X1aoM1ngTX
 */
@Component
@Slf4j
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
    
    @Autowired
    private UserService userService;
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        // 从URL参数中获取userId
        String query = request.getURI().getQuery();
        if (query != null && query.contains("userId=")) {
            String userIdStr = query.split("userId=")[1].split("&")[0];
            try {
                Long userId = Long.valueOf(userIdStr);
                
                // 验证用户是否存在
                User user = userService.getById(userId);
                if (user != null) {
                    attributes.put("userId", userId);
                    return true;
                }
            } catch (NumberFormatException e) {
                log.warn("WebSocket连接失败: 无效的userId格式: {}", userIdStr);
            }
        }
        
        return false;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        // 连接建立后的处理
    }
}