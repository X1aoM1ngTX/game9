package com.xm.game9.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.game9.mapper.ChatSessionMapper;
import com.xm.game9.model.domain.ChatSession;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.vo.ChatSessionVO;
import com.xm.game9.service.ChatSessionService;
import com.xm.game9.service.UserService;
import com.xm.game9.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 聊天会话服务实现类
 *
 * @author X1aoM1ngTX
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {
    
    @Autowired
    private ChatSessionMapper chatSessionMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Override
    public List<ChatSessionVO> getUserSessions(Long userId) {
        List<ChatSession> sessions = chatSessionMapper.getUserSessions(userId);
        List<ChatSessionVO> result = new ArrayList<>();
        
        for (ChatSession session : sessions) {
            ChatSessionVO vo = new ChatSessionVO();
            vo.setSessionId(session.getSessionId());
            vo.setLastMessage(session.getLastMessage());
            vo.setLastMessageTime(session.getLastMessageTime());
            
            // 确定对方用户ID
            Long friendId = session.getUser1Id().equals(userId) ? session.getUser2Id() : session.getUser1Id();
            vo.setFriendId(friendId);
            
            // 获取对方用户信息
            User friend = userService.getById(friendId);
            if (friend != null) {
                vo.setFriendNickname(friend.getUserNickname());
                vo.setFriendAvatar(friend.getUserAvatar());
            }
            
            // 使用与好友系统相同的方法检查在线状态
            boolean isOnline = redisUtil.hasKey("user:online:" + friendId);
            vo.setOnline(isOnline);
            
            // 设置未读消息数
            if (session.getUser1Id().equals(userId)) {
                vo.setUnreadCount(session.getUnreadCountUser1());
            } else {
                vo.setUnreadCount(session.getUnreadCountUser2());
            }
            
            result.add(vo);
        }
        
        return result;
    }
    
    @Override
    public Long getOrCreateSession(Long user1Id, Long user2Id) {
        ChatSession session = chatSessionMapper.getSessionByUsers(user1Id, user2Id);
        
        if (session == null) {
            session = new ChatSession();
            session.setUser1Id(Math.min(user1Id, user2Id));
            session.setUser2Id(Math.max(user1Id, user2Id));
            session.setUnreadCountUser1(0);
            session.setUnreadCountUser2(0);
            session.setCreateTime(new Date());
            session.setUpdateTime(new Date());
            
            save(session);
        }
        
        return session.getSessionId();
    }
    
    @Override
    public boolean updateSession(Long sessionId, String lastMessage, Long senderId) {
        ChatSession session = getById(sessionId);
        if (session == null) {
            return false;
        }
        
        Boolean isUser1Sender = session.getUser1Id().equals(senderId);
        int result = chatSessionMapper.updateSession(sessionId, lastMessage, new Date(), isUser1Sender);
        
        return result > 0;
    }
    
    @Override
    public boolean clearUnreadCount(Long sessionId, Long userId) {
        ChatSession session = getById(sessionId);
        if (session == null) {
            return false;
        }
        
        if (session.getUser1Id().equals(userId)) {
            session.setUnreadCountUser1(0);
        } else {
            session.setUnreadCountUser2(0);
        }
        
        return updateById(session);
    }
}