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
        
        log.info("WebSocket 握手开始: URI={}, Headers={}", request.getURI(), request.getHeaders());
        
        // 从URL参数中获取userId
        String query = request.getURI().getQuery();
        if (query != null && query.contains("userId=")) {
            String userIdStr = query.split("userId=")[1].split("&")[0];
            try {
                Long userId = Long.valueOf(userIdStr);
                log.info("WebSocket 握手中提取到 userId: {}", userId);
                
                // 验证用户是否存在
                User user = userService.getById(userId);
                if (user != null) {
                    log.info("WebSocket 握手验证用户 {} 成功，用户昵称: {}", userId, user.getUserNickname());
                    attributes.put("userId", userId);
                    attributes.put("userNickname", user.getUserNickname());
                    return true;
                } else {
                    log.warn("WebSocket 握手失败: 用户 {} 不存在", userId);
                }
            } catch (NumberFormatException e) {
                log.warn("WebSocket连接失败: 无效的userId格式: {}", userIdStr);
            }
        } else {
            log.warn("WebSocket 握手失败: URL中缺少 userId 参数, query={}", query);
        }
        
        return false;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket 握手后处理异常: {}", exception.getMessage(), exception);
        } else {
            log.info("WebSocket 握手成功完成");
        }
    }
}