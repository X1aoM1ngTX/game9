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
    
    @Override
    public IPage<ChatMessageVO> getChatMessageList(Long userId, Long friendId, int page, int size) {
        Page<ChatMessageVO> pageParam = new Page<>(page, size);
        return chatMessageMapper.getChatMessageList(pageParam, userId, friendId);
    }
    
    @Override
    public Long getUnreadCount(Long userId) {
        return chatMessageMapper.getUnreadCount(userId);
    }
    
    @Override
    public boolean markMessagesAsRead(Long userId, Long friendId) {
        int result = chatMessageMapper.updateMessageStatus(friendId, userId);
        return result > 0;
    }
    
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
        updateEntity.setStatus(2); // 已推送状态
        
        return update(updateEntity, queryWrapper);
    }
}