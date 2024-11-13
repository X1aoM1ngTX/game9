package com.xm.xmgame.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xm.xmgame.common.BaseResponse;
import com.xm.xmgame.common.ErrorCode;
import com.xm.xmgame.common.ResultUtils;
import com.xm.xmgame.exception.BusinessException;
import com.xm.xmgame.model.domain.User;
import com.xm.xmgame.model.request.user.UserLoginRequest;
import com.xm.xmgame.model.request.user.UserRegisterRequest;
import com.xm.xmgame.service.UserService;
import com.xm.xmgame.common.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userName = registerRequest.getUserName();
        String userPassword = registerRequest.getUserPassword();
        String userEmail = registerRequest.getUserEmail();
        if (StringUtils.isAnyBlank(userName, userEmail, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        long result = userService.userRegister(registerRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest loginRequest, HttpServletRequest request) {
        if (loginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
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
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     */
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
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
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
     * 删除用户
     */
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
