package com.xm.game9.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.game9.model.domain.ChatSession;
import com.xm.game9.model.vo.ChatSessionVO;

import java.util.List;

/**
 * 聊天会话服务接口
 *
 * @author X1aoM1ngTX
 */
public interface ChatSessionService extends IService<ChatSession> {
    
    /**
     * 获取用户会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSessionVO> getUserSessions(Long userId);
    
    /**
     * 获取或创建会话
     *
     * @param user1Id 用户1 ID
     * @param user2Id 用户2 ID
     * @return 会话ID
     */
    Long getOrCreateSession(Long user1Id, Long user2Id);
    
    /**
     * 更新会话信息
     *
     * @param sessionId     会话ID
     * @param lastMessage   最后消息
     * @param senderId      发送者ID
     * @return 是否成功
     */
    boolean updateSession(Long sessionId, String lastMessage, Long senderId);
    
    /**
     * 清除未读消息数
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @return 是否成功
     */
    boolean clearUnreadCount(Long sessionId, Long userId);
}