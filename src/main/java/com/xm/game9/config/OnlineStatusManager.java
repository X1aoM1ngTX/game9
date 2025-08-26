package com.xm.game9.config;

import com.xm.game9.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 在线状态管理器
 *
 * @author X1aoM1ngTX
 */
@Component
@Slf4j
public class OnlineStatusManager {
    
    @Autowired
    private RedisUtil redisUtil;
    
    // 本地缓存：用户ID -> WebSocket Session
    private final ConcurrentHashMap<Long, Object> onlineUsers = new ConcurrentHashMap<>();
    
    /**
     * 用户上线
     */
    public void userOnline(Long userId) {
        onlineUsers.put(userId, new Object());
        redisUtil.setWithExpire("user:online:" + userId, "online", 3600, TimeUnit.SECONDS); // 1小时过期
        log.info("用户上线: userId={}", userId);
    }
    
    /**
     * 用户下线
     */
    public void userOffline(Long userId) {
        onlineUsers.remove(userId);
        redisUtil.delete("user:online:" + userId);
        log.info("用户下线: userId={}", userId);
    }
    
    /**
     * 检查用户是否在线
     */
    public boolean isOnline(Long userId) {
        return onlineUsers.containsKey(userId) || redisUtil.hasKey("user:online:" + userId);
    }
    
    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
}