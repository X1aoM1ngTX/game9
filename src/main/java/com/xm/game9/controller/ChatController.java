package com.xm.game9.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.ChatMessageRequest;
import com.xm.game9.model.vo.ChatMessageVO;
import com.xm.game9.model.vo.ChatSessionVO;
import com.xm.game9.service.ChatMessageService;
import com.xm.game9.service.ChatSessionService;
import com.xm.game9.service.FriendService;
import com.xm.game9.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 聊天控制器
 *
 * @author X1aoM1ngTX
 */
@RestController
@RequestMapping("/chat")
@Tag(name = "聊天接口", description = "聊天系统相关的所有接口")
public class ChatController {
    
    @Autowired
    private ChatMessageService chatMessageService;
    
    @Autowired
    private ChatSessionService chatSessionService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private FriendService friendService;
    
    /**
     * 发送消息
     * 
     * @param request 消息请求体
     * @param httpRequest HTTP请求
     * @return 消息ID
     */
    @PostMapping("/message/send")
    public BaseResponse<Long> sendMessage(@RequestBody ChatMessageRequest request, 
                                         HttpServletRequest httpRequest) {
        User currentUser = userService.getLoginUser(httpRequest);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        
        // 检查是否是好友关系
        if (!friendService.isFriend(currentUser.getUserId(), request.getReceiverId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只能给好友发送消息");
        }
        
        Long messageId = chatMessageService.sendMessage(
            currentUser.getUserId(), 
            request.getReceiverId(), 
            request.getContent(), 
            request.getMessageType()
        );
        
        // 更新会话信息
        Long sessionId = chatSessionService.getOrCreateSession(currentUser.getUserId(), request.getReceiverId());
        chatSessionService.updateSession(sessionId, request.getContent(), currentUser.getUserId());
        
        return ResultUtils.success(messageId);
    }
    
    /**
     * 获取聊天消息列表
     * 
     * @param friendId 好友ID
     * @param page 页码
     * @param size 每页大小
     * @param httpRequest HTTP请求
     * @return 分页的聊天消息列表
     */
    @GetMapping("/message/list")
    public BaseResponse<IPage<ChatMessageVO>> getChatMessageList(@RequestParam Long friendId,
                                                                 @RequestParam(defaultValue = "1") int page,
                                                                 @RequestParam(defaultValue = "20") int size,
                                                                 HttpServletRequest httpRequest) {
        User currentUser = userService.getLoginUser(httpRequest);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        
        IPage<ChatMessageVO> messageList = chatMessageService.getChatMessageList(
            currentUser.getUserId(), friendId, page, size
        );
        
        // 标记消息为已读
        chatMessageService.markMessagesAsRead(currentUser.getUserId(), friendId);
        
        // 清除未读消息数
        Long sessionId = chatSessionService.getOrCreateSession(currentUser.getUserId(), friendId);
        chatSessionService.clearUnreadCount(sessionId, currentUser.getUserId());
        
        return ResultUtils.success(messageList);
    }
    
    /**
     * 获取会话列表
     * 
     * @return 会话列表
     */
    @GetMapping("/session/list")
    public BaseResponse<List<ChatSessionVO>> getSessionList(HttpServletRequest httpRequest) {
        User currentUser = userService.getLoginUser(httpRequest);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        
        List<ChatSessionVO> sessionList = chatSessionService.getUserSessions(currentUser.getUserId());
        return ResultUtils.success(sessionList);
    }
    
    /**
     * 测试端点
     * 
     * @return 测试消息
     */
    @GetMapping("/test")
    public BaseResponse<String> test() {
        return ResultUtils.success("ChatController is working!");
    }
    
    /**
     * 获取未读消息数量
     * 
     * @return 未读消息数量
     */
    @GetMapping("/message/unread/count")
    public BaseResponse<Long> getUnreadCount(HttpServletRequest httpRequest) {
        User currentUser = userService.getLoginUser(httpRequest);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        
        Long unreadCount = chatMessageService.getUnreadCount(currentUser.getUserId());
        return ResultUtils.success(unreadCount);
    }
    
    /**
     * 标记消息为已读
     * 
     * @param friendId 好友ID
     * @param httpRequest HTTP请求
     * @return 操作结果
     */
    @PostMapping("/message/read")
    public BaseResponse<Boolean> markMessagesAsRead(@RequestParam Long friendId,
                                                    HttpServletRequest httpRequest) {
        User currentUser = userService.getLoginUser(httpRequest);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        
        boolean result = chatMessageService.markMessagesAsRead(currentUser.getUserId(), friendId);
        
        // 清除未读消息数
        Long sessionId = chatSessionService.getOrCreateSession(currentUser.getUserId(), friendId);
        chatSessionService.clearUnreadCount(sessionId, currentUser.getUserId());
        
        return ResultUtils.success(result);
    }
}