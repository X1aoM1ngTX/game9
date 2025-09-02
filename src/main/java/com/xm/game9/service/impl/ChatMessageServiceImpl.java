package com.xm.game9.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.game9.mapper.ChatMessageMapper;
import com.xm.game9.model.domain.ChatMessage;
import com.xm.game9.model.vo.ChatMessageVO;
import com.xm.game9.service.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 聊天消息服务实现类
 *
 * @author X1aoM1ngTX
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {
    
    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    /**
     * 发送消息
     *
     * @param senderId   发送者ID
     * @param receiverId 接收者ID
     * @param content    消息内容
     * @param messageType 消息类型（0文本，1图片，2文件等）
     * @return 消息ID
     */
    @Override
    public Long sendMessage(Long senderId, Long receiverId, String content, Integer messageType) {
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setMessageType(messageType);
        message.setStatus(0); // 已发送
        message.setCreateTime(new Date());
        message.setUpdateTime(new Date());
        
        save(message);
        return message.getMessageId();
    }
    
    /**
    * 获取聊天消息列表，分页
    *
    * @param userId   用户ID
    * @param friendId 好友ID
    * @param page     页码
    * @param size     每页大小
    * @return 分页结果
    */
    @Override
    public IPage<ChatMessageVO> getChatMessageList(Long userId, Long friendId, int page, int size) {
        Page<ChatMessageVO> pageParam = new Page<>(page, size);
        return chatMessageMapper.getChatMessageList(pageParam, userId, friendId);
    }
    
    /**
     * 获取用户未读消息总数
     *
     * @param userId 用户ID
     * @return 未读消息数
     */
    @Override
    public Long getUnreadCount(Long userId) {
        return chatMessageMapper.getUnreadCount(userId);
    }
    
    /**
     * 标记消息为已读
     *
     * @param readerId  阅读者ID
     * @param senderId  发送者ID
     * @return 更新是否成功
     */
    @Override
    public boolean markMessagesAsRead(Long readerId, Long senderId) {
        int result = chatMessageMapper.updateMessageStatus(senderId, readerId);
        return result > 0;
    }
    
    /**
     * 获取用户的离线消息
     *
     * @param userId 用户ID
     * @return 离线消息列表
     */
    @Override
    public List<ChatMessageVO> getOfflineMessages(Long userId) {
        // 获取用户的所有未读消息
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiver_id", userId)
                   .eq("status", 0) // 未读状态
                   .orderByDesc("create_time");
        
        List<ChatMessage> messages = list(queryWrapper);
        
        // 转换为VO对象
        return messages.stream().map(msg -> {
            ChatMessageVO vo = new ChatMessageVO();
            vo.setMessageId(msg.getMessageId());
            vo.setSenderId(msg.getSenderId());
            vo.setReceiverId(msg.getReceiverId());
            vo.setContent(msg.getContent());
            vo.setMessageType(msg.getMessageType());
            vo.setStatus(msg.getStatus());
            vo.setCreateTime(msg.getCreateTime());
            return vo;
        }).toList();
    }
    
    /**
     * 标记消息为已推送
     *
     * @param userId     用户ID
     * @param messageIds 消息ID数组
     * @return 更新是否成功
     */
    @Override
    public boolean markMessagesAsPushed(Long userId, Long[] messageIds) {
        if (messageIds == null || messageIds.length == 0) {
            return false;
        }
        
        // 批量更新消息状态为已推送
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("receiver_id", userId)
                   .in("message_id", (Object[]) messageIds);
        
        ChatMessage updateEntity = new ChatMessage();
        updateEntity.setStatus(1); // 已推送状态
        
        return update(updateEntity, queryWrapper);
    }
}