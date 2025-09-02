package com.xm.game9.config;

import com.xm.game9.model.vo.FriendVO;
import com.xm.game9.service.FriendService;
import com.xm.game9.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
    
    @Autowired
    private FriendService friendService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // 本地缓存：用户ID -> WebSocket Session
    private final ConcurrentHashMap<Long, Object> onlineUsers = new ConcurrentHashMap<>();
    
    /**
     * 用户上线
     */
    public void userOnline(Long userId) {
        onlineUsers.put(userId, new Object());
        redisUtil.setWithExpire("user:online:" + userId, "online", 3600, TimeUnit.SECONDS); // 1小时过期
        log.info("用户上线: userId={}", userId);
        
        // 通知好友该用户上线
        notifyFriendsStatusChange(userId, true);
    }
    
    /**
     * 用户下线
     */
    public void userOffline(Long userId) {
        onlineUsers.remove(userId);
        redisUtil.delete("user:online:" + userId);
        log.info("用户下线: userId={}", userId);
        
        // 通知好友该用户下线
        notifyFriendsStatusChange(userId, false);
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
    
    /**
     * 通知好友状态变化
     */
    private void notifyFriendsStatusChange(Long userId, boolean isOnline) {
        try {
            // 获取该用户的好友列表
            List<Long> friendIds = getFriendIds(userId);
            
            for (Long friendId : friendIds) {
                // 检查好友是否在线
                if (isOnline(friendId)) {
                    // 发送状态变化通知
                    UserStatusNotification notification = new UserStatusNotification(
                        userId, isOnline, System.currentTimeMillis()
                    );
                    
                    messagingTemplate.convertAndSendToUser(
                        friendId.toString(),
                        "/queue/friends/status",
                        notification
                    );
                }
            }
        } catch (Exception e) {
            log.error("通知好友状态变化失败: userId={}, isOnline={}", userId, isOnline, e);
        }
    }
    
    /**
     * 获取用户好友ID列表
     */
    private List<Long> getFriendIds(Long userId) {
        try {
            List<FriendVO> friends = friendService.getFriendList(userId);
            List<Long> friendIds = new ArrayList<>();
            for (FriendVO friend : friends) {
                friendIds.add(friend.getFriendId());
            }
            return friendIds;
        } catch (Exception e) {
            log.error("获取用户好友列表失败: userId={}", userId, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 用户状态通知
     */
    public static class UserStatusNotification {
        private final Long userId;
        private final Boolean online;
        private final Long timestamp;
        
        public UserStatusNotification(Long userId, Boolean online, Long timestamp) {
            this.userId = userId;
            this.online = online;
            this.timestamp = timestamp;
        }
        
        // Getters
        public Long getUserId() {
            return userId;
        }
        
        public Boolean getOnline() {
            return online;
        }
        
        public Long getTimestamp() {
            return timestamp;
        }
    }
}