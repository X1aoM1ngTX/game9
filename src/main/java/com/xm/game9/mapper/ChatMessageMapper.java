package com.xm.game9.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xm.game9.model.domain.ChatMessage;
import com.xm.game9.model.vo.ChatMessageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息Mapper接口
 *
 * @author X1aoM1ngTX
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    
    /**
     * 获取聊天消息列表
     *
     * @param page       分页对象
     * @param userId     当前用户ID
     * @param friendId   好友ID
     * @return 消息列表
     */
    IPage<ChatMessageVO> getChatMessageList(Page<ChatMessageVO> page, 
                                           @Param("userId") Long userId, 
                                           @Param("friendId") Long friendId);
    
    /**
     * 获取未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Long getUnreadCount(@Param("userId") Long userId);
    
    /**
     * 更新消息状态为已读
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @return 影响行数
     */
    int updateMessageStatus(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}