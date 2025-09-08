package com.xm.game9.controller;

import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.service.UserService;
import com.xm.game9.utils.SessionManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 单设备登录管理接口
 * 
 * @author X1aoM1ngTX
 */
@Tag(name = "单设备登录管理", description = "单设备登录相关的管理接口")
@RestController
@RequestMapping("/session")
@Slf4j
public class SessionManagerController {

    @Resource
    private SessionManager sessionManager;
    
    @Resource
    private UserService userService;

    /**
     * 强制用户下线（管理员功能）
     * 
     * @param userId 用户ID
     * @param request HTTP请求
     * @return 是否成功
     */
    @Operation(summary = "强制用户下线", description = "强制指定用户下线（管理员功能）")
    @PostMapping("/forceOffline")
    public BaseResponse<Boolean> forceUserOffline(@RequestParam Long userId, HttpServletRequest request) {
        // 检查管理员权限
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无管理员权限");
        }
        
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        
        sessionManager.forceUserOffline(userId);
        log.info("[强制下线] 管理员强制用户下线，用户ID: {}", userId);
        
        return ResultUtils.success(true);
    }

    /**
     * 获取用户当前登录信息
     * 
     * @param userId 用户ID
     * @param request HTTP请求
     * @return 用户登录信息
     */
    @Operation(summary = "获取用户登录信息", description = "获取指定用户的当前登录信息（管理员功能）")
    @GetMapping("/userInfo")
    public BaseResponse<SessionUserInfo> getUserSessionInfo(@RequestParam Long userId, HttpServletRequest request) {
        // 检查管理员权限
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无管理员权限");
        }
        
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        
        java.util.Set<String> sessionIds = sessionManager.getUserSessionIds(userId);
        String userIp = sessionManager.getUserIp(userId);
        
        SessionUserInfo userInfo = new SessionUserInfo();
        userInfo.setUserId(userId);
        userInfo.setSessionIds(sessionIds);
        userInfo.setIp(userIp);
        userInfo.setOnline(sessionIds != null && !sessionIds.isEmpty());
        
        return ResultUtils.success(userInfo);
    }

    /**
     * 验证当前用户Session是否有效
     * 
     * @param request HTTP请求
     * @return 是否有效
     */
    @Operation(summary = "验证Session", description = "验证当前用户的Session是否有效")
    @GetMapping("/validate")
    public BaseResponse<Boolean> validateSession(HttpServletRequest request) {
        try {
            // 获取当前登录用户
            var currentUser = userService.getLoginUser(request);
            boolean isValid = sessionManager.validateSession(currentUser.getUserId(), request);
            return ResultUtils.success(isValid);
        } catch (BusinessException e) {
            return ResultUtils.success(false);
        }
    }

    /**
     * 用户登录信息响应对象
     */
    public static class SessionUserInfo {
        private Long userId;
        private java.util.Set<String> sessionIds;
        private String ip;
        private boolean online;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public java.util.Set<String> getSessionIds() {
            return sessionIds;
        }

        public void setSessionIds(java.util.Set<String> sessionIds) {
            this.sessionIds = sessionIds;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }
    }
}