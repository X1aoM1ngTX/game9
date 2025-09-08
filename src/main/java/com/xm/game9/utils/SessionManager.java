package com.xm.game9.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Session管理器 - 用于实现单设备登录功能
 * 
 * @author X1aoM1ngTX
 */
@Component
@Slf4j
public class SessionManager {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SessionRepository<?> sessionRepository;

    /**
     * 用户登录状态存储的key前缀
     */
    private static final String USER_SESSION_PREFIX = "user:session:";
    
    /**
     * 用户Session集合的key
     */
    private static final String USER_SESSIONS_KEY = "user:sessions:";
    
    /**
     * 用户IP地址的key
     */
    private static final String USER_IP_KEY = "user:ip:";
    
    /**
     * Session属性的key
     */
    private static final String USER_LOGIN_STATE = "userLoginState";

    /**
     * 标准化IP地址 - 将IPv6本地地址转换为IPv4地址
     * 
     * @param ip 原始IP地址
     * @return 标准化后的IP地址
     */
    private String normalizeIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return ip;
        }
        
        // 处理IPv6本地地址
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        
        // 处理其他IPv6地址（如果有需要可以扩展）
        // 目前主要解决本地开发环境的IPv6/IPv4问题
        
        return ip;
    }

    /**
     * 处理用户登录 - 实现单设备登录
     * 
     * @param userId 用户ID
     * @param request HTTP请求
     * @param user 用户对象
     */
    public void handleUserLogin(Long userId, HttpServletRequest request, Object user) {
        String sessionId = request.getSession().getId();
        String userIp = request.getRemoteAddr();
        
        // 标准化IP地址 - 将IPv6本地地址转换为IPv4地址
        String normalizedIp = normalizeIpAddress(userIp);
        
        log.info("[单设备登录] 用户ID: {}, 新SessionID: {}, 原始IP: {}, 标准化IP: {}", userId, sessionId, userIp, normalizedIp);
        
        // 1. 检查用户是否已有其他设备登录
        String oldIp = (String) redisTemplate.opsForValue().get(USER_IP_KEY + userId);
        
        if (oldIp != null && !oldIp.equals(normalizedIp)) {
            log.info("[单设备登录] 检测到不同设备登录，用户ID: {}, 旧IP: {}, 新IP: {}, 清除该设备所有Session", 
                     userId, oldIp, normalizedIp);
            // 清除旧设备的所有Session
            clearOldDeviceSessions(userId, oldIp);
        }
        
        // 2. 存储新的Session信息
        // 使用Redis Set存储同一用户的所有SessionID
        redisTemplate.opsForSet().add(USER_SESSIONS_KEY + userId, sessionId);
        redisTemplate.expire(USER_SESSIONS_KEY + userId, 14, TimeUnit.DAYS);
        
        // 存储用户当前标准化IP（用于设备判断）
        redisTemplate.opsForValue().set(USER_IP_KEY + userId, normalizedIp);
        redisTemplate.expire(USER_IP_KEY + userId, 14, TimeUnit.DAYS);
        
        // 4. 在Session中设置用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        
        log.info("[单设备登录] 登录处理完成，用户ID: {}, SessionID: {}, IP: {}", 
                 userId, sessionId, userIp);
    }

    /**
     * 清除旧设备的所有Session
     * 
     * @param userId 用户ID
     * @param oldIp 旧设备的IP地址
     */
    private void clearOldDeviceSessions(Long userId, String oldIp) {
        try {
            // 获取该用户的所有SessionID
            String sessionsKey = USER_SESSIONS_KEY + userId;
            java.util.Set<String> sessionIds = redisTemplate.opsForSet().members(sessionsKey);
            
            if (sessionIds != null && !sessionIds.isEmpty()) {
                for (String sessionId : sessionIds) {
                    try {
                        // 通过Spring Session的Repository删除Session
                        sessionRepository.deleteById(sessionId);
                        log.info("[单设备登录] 已清除旧设备Session，用户ID: {}, SessionID: {}", userId, sessionId);
                    } catch (Exception e) {
                        log.warn("[单设备登录] 清除Session失败，用户ID: {}, SessionID: {}, 错误: {}", 
                                userId, sessionId, e.getMessage());
                    }
                }
                
                // 清除Redis中的Session集合
                redisTemplate.delete(sessionsKey);
            }
        } catch (Exception e) {
            log.error("[单设备登录] 清除旧设备Sessions失败，用户ID: {}, 旧IP: {}, 错误: {}", 
                     userId, oldIp, e.getMessage(), e);
        }
    }

    /**
     * 处理用户登出
     * 
     * @param userId 用户ID
     * @param request HTTP请求
     */
    public void handleUserLogout(Long userId, HttpServletRequest request) {
        if (userId == null) {
            return;
        }
        
        String sessionId = request.getSession().getId();
        log.info("[单设备登录] 用户登出，用户ID: {}, SessionID: {}", userId, sessionId);
        
        // 从Redis Set中移除当前SessionID
        String sessionsKey = USER_SESSIONS_KEY + userId;
        redisTemplate.opsForSet().remove(sessionsKey, sessionId);
        
        // 如果这是最后一个Session，清除用户IP
        Long remainingSessions = redisTemplate.opsForSet().size(sessionsKey);
        if (remainingSessions != null && remainingSessions == 0) {
            redisTemplate.delete(USER_IP_KEY + userId);
            redisTemplate.delete(sessionsKey);
        }
        
        // 清除Session中的用户信息
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        
        log.info("[单设备登录] 登出处理完成，用户ID: {}, SessionID: {}", userId, sessionId);
    }

    /**
     * 验证当前Session是否有效
     * 
     * @param userId 用户ID
     * @param request HTTP请求
     * @return 是否有效
     */
    public boolean validateSession(Long userId, HttpServletRequest request) {
        if (userId == null) {
            return false;
        }
        
        String currentSessionId = request.getSession().getId();
        String sessionsKey = USER_SESSIONS_KEY + userId;
        
        // 检查当前SessionID是否在用户的Session集合中
        Boolean isMember = redisTemplate.opsForSet().isMember(sessionsKey, currentSessionId);
        
        // 如果Redis中没有该用户的Session集合，或者当前Session不在集合中，说明无效
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * 获取用户的所有SessionID
     * 
     * @param userId 用户ID
     * @return SessionID集合
     */
    public java.util.Set<String> getUserSessionIds(Long userId) {
        String sessionsKey = USER_SESSIONS_KEY + userId;
        return redisTemplate.opsForSet().members(sessionsKey);
    }

    /**
     * 获取用户当前的IP地址
     * 
     * @param userId 用户ID
     * @return IP地址
     */
    public String getUserIp(Long userId) {
        return (String) redisTemplate.opsForValue().get(USER_IP_KEY + userId);
    }

    /**
     * 强制用户下线
     * 
     * @param userId 用户ID
     */
    public void forceUserOffline(Long userId) {
        String userIp = getUserIp(userId);
        if (userIp != null) {
            clearOldDeviceSessions(userId, userIp);
            redisTemplate.delete(USER_IP_KEY + userId);
            log.info("[单设备登录] 强制用户下线，用户ID: {}, IP: {}", userId, userIp);
        }
    }
}