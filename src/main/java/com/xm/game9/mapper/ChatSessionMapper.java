package com.xm.game9.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.game9.model.domain.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天会话Mapper接口
 *
 * @author X1aoM1ngTX
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
    
    /**
     * 获取用户会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> getUserSessions(@Param("userId") Long userId);
    
    /**
     * 获取两个用户之间的会话
     *
     * @param user1Id 用户1 ID
     * @param user2Id 用户2 ID
     * @return 会话信息
     */
    ChatSession getSessionByUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
    
    /**
     * 更新会话信息
     *
     * @param sessionId      会话ID
     * @param lastMessage    最后消息
     * @param messageTime    消息时间
     * @param isUser1Sender  是否是用户1发送的
     * @return 影响行数
     */
    int updateSession(@Param("sessionId") Long sessionId, 
                     @Param("lastMessage") String lastMessage,
                     @Param("messageTime") java.util.Date messageTime,
                     @Param("isUser1Sender") Boolean isUser1Sender);
}