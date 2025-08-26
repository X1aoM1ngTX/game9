package com.xm.game9.config;

import com.xm.game9.service.UserService;
import com.xm.game9.utils.RedisUtil;
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
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 从URL参数中获取token
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String token = query.split("token=")[1].split("&")[0];
            
            // 验证token
            Object userIdObj = redisUtil.get("user:token:" + token);
            if (userIdObj != null) {
                Long userId = Long.valueOf(userIdObj.toString());
                attributes.put("userId", userId);
                log.info("WebSocket连接成功: userId={}", userId);
                return true;
            }
        }
        
        log.warn("WebSocket连接失败: token无效或不存在");
        return false;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        // 连接建立后的处理
    }
}