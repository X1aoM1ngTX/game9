package com.xm.game9.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 *
 * @author X1aoM1ngTX
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Autowired
    private WebSocketHandshakeInterceptor handshakeInterceptor;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的消息代理，前缀为 /topic 和 /queue
        config.enableSimpleBroker("/topic", "/queue");
        // 应用程序前缀为 /app
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，允许跨域，并添加握手拦截器
        // 提供两种连接方式：SockJS和原生WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000", "http://localhost:3001")
                .addInterceptors(handshakeInterceptor)
                .withSockJS();
        
        // 同时提供原生WebSocket端点
        registry.addEndpoint("/ws-native")
                .setAllowedOrigins("http://localhost:3000", "http://localhost:3001")
                .addInterceptors(handshakeInterceptor);
    }
}