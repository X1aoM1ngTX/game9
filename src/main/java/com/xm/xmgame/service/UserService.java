package com.xm.xmgame.service;

import com.xm.xmgame.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.xmgame.model.request.user.UserLoginRequest;
import com.xm.xmgame.model.request.user.UserRegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
 * @author XMTX8yyds
 * @描述  针对表【user(用户表)】的数据库操作Service
 * @创建时间  2024-10-10 13:26:55
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    Long userRegister(UserRegisterRequest registerRequest);

    /**
     * 用户登录
     */
    User userLogin(UserLoginRequest loginRequest, HttpServletRequest request);

    /**
     * 获取当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);
}