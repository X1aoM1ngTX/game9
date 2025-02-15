package com.xm.xmgame.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xm.xmgame.common.BaseResponse;
import com.xm.xmgame.common.ErrorCode;
import com.xm.xmgame.common.ResultUtils;
import com.xm.xmgame.exception.BusinessException;
import com.xm.xmgame.model.domain.User;
import com.xm.xmgame.model.request.user.*;
import com.xm.xmgame.service.UserService;
import com.xm.xmgame.utils.UserUtils;

import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户接口
 */
@Tag(name = "用户接口", description = "用户相关的所有接口")
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"},allowCredentials = "true")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "注册新的用户")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userName = registerRequest.getUserName();
        String userPassword = registerRequest.getUserPassword();
        String userEmail = registerRequest.getUserEmail();
        String userPhone = registerRequest.getUserPhone();
        if (StringUtils.isAnyBlank(userName, userEmail, userPassword, userPhone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        long result = userService.userRegister(registerRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户登录")
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest loginRequest, HttpServletRequest request) {
        if (loginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String userName = loginRequest.getUserName();
        String userPassword = loginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userName, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        User user = userService.userLogin(loginRequest, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     */
    @Operation(summary = "用户退出登录", description = "用户退出登录")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 发送邮件
     *
     * @param request 发送请求
     * @return 是否发送成功
     */
    @Operation(summary = "发送邮件", description = "发送邮件")
    @PostMapping("/sendEmail")
    public BaseResponse<String> sendEmail(@RequestBody EmailSendToUserRequest request) throws MessagingException, UnsupportedEncodingException {
        if (StringUtils.isBlank(request.getToEmail())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }
        userService.sendEmail(request);
        return ResultUtils.success("发送成功");
    }

    /**
     * 验证验证码
     *
     * @param verifyRequest 验证
     * @return 是否成功
     */
    @Operation(summary = "验证验证码", description = "验证验证码")
    @PostMapping("/verifyCode")
    public BaseResponse<Boolean> verifyCode(@RequestBody VerifyCodeRequest verifyRequest) {
        if (verifyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.verifyCode(verifyRequest);
        return ResultUtils.success(result);
    }

    /**
     * 重置密码
     *
     * @param resetRequest 重置密码请求
     * @return 是否成功
     */
    @Operation(summary = "重置密码", description = "重置密码")
    @PostMapping("/resetPassword")
    public BaseResponse<Boolean> resetPassword(@RequestBody ResetPasswordRequest resetRequest) {
        if (resetRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.resetPassword(resetRequest);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     */
    @Operation(summary = "获取当前用户", description = "获取当前用户")
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return ResultUtils.success(UserUtils.getSafetyUser(loginUser));
    }

    /**
     * 查询用户
     */
    @Operation(summary = "查询用户", description = "查询用户")
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("userName", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> safetyUserList = userList.stream()
                .map(UserUtils::getSafetyUser)
                .collect(Collectors.toList());
        return ResultUtils.success(safetyUserList);
    }

    /**
     * 修改用户信息
     *
     * @param updateRequest 用户更新请求
     * @param request HttpServlet请求
     * @return 是否更新成功
     */
    @Operation(summary = "修改用户自己的信息", description = "修改用户信息")
    @PostMapping("/modify")
    public BaseResponse<Boolean> userModify(@RequestBody UserModifyRequest modifyRequest, HttpServletRequest request) {
        if (modifyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        boolean result = userService.userModify(modifyRequest, loginUser.getUserId());
        return ResultUtils.success(result);
    }

    /**
     * 管理员用户信息修改
     */
    @Operation(summary = "管理员用户信息修改", description = "管理员用户信息修改")
    @PostMapping("/adminUpdate")
    public BaseResponse<Boolean> adminUserUpdate(@RequestBody AdminUserUpdateRequest updateRequest) {
        if (updateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 检查管理员权限
        @SuppressWarnings("null")
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        boolean result = userService.adminUserUpdate(updateRequest, updateRequest.getUserId());
        return ResultUtils.success(result);
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "删除用户")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(id);
        return ResultUtils.success(result);
    }

}
