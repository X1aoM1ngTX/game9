package com.xm.game9.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.game9.model.domain.ChatMessage;
import com.xm.game9.model.vo.ChatMessageVO;

import java.util.List;

/**
 * 聊天消息服务接口
 *
 * @author X1aoM1ngTX
 */
public interface ChatMessageService extends IService<ChatMessage> {
    
    /**
     * 发送消息
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @param content    消息内容
     * @param messageType 消息类型
     * @return 消息ID
     */
    Long sendMessage(Long senderId, Long receiverId, String content, Integer messageType);
    
    /**
     * 获取聊天消息列表
     *
     * @param userId   当前用户ID
     * @param friendId 好友ID
     * @param page     页码
     * @param size     页大小
     * @return 消息列表
     */
    IPage<ChatMessageVO> getChatMessageList(Long userId, Long friendId, int page, int size);
    
    /**
     * 获取未读消息数量
     *
     * @param userId 用户ID
     * @return 未读消息数量
     */
    Long getUnreadCount(Long userId);
    
    /**
     * 标记消息为已读
     *
     * @param readerId 读取消息的用户ID (即接收者)
     * @param senderId 发送消息的用户ID
     * @return 是否成功
     */
    boolean markMessagesAsRead(Long readerId, Long senderId);
    
    /**
     * 获取离线消息
     *
     * @param userId 用户ID
     * @return 离线消息列表
     */
    List<ChatMessageVO> getOfflineMessages(Long userId);
    
    /**
     * 标记消息为已推送
     *
     * @param userId     用户ID
     * @param messageIds 消息ID数组
     * @return 是否成功
     */
    boolean markMessagesAsPushed(Long userId, Long[] messageIds);
}