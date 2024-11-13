package com.xm.xmgame.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.xmgame.common.ErrorCode;
import com.xm.xmgame.exception.BusinessException;
import com.xm.xmgame.mapper.UserMapper;
import com.xm.xmgame.model.domain.User;
import com.xm.xmgame.model.request.user.UserLoginRequest;
import com.xm.xmgame.model.request.user.UserRegisterRequest;
import com.xm.xmgame.service.UserService;
import com.xm.xmgame.common.UserUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import static com.xm.xmgame.constant.UserConstant.ADMIN_ROLE;
import static com.xm.xmgame.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author xm
 * @描述 针对表【user(用户表)】的数据库操作Service实现
 * @创建时间 2024-10-10 13:26:55
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "xm";

    /**
     * 用户注册
     *
     * @param registerRequest 用户注册请求
     * @return 用户ID
     */
    @Override
    public Long userRegister(UserRegisterRequest registerRequest) {
        String userName = registerRequest.getUserName();
        String userEmail = registerRequest.getUserEmail();
        String userPassword = registerRequest.getUserPassword();

        // 1. 校验
        if (StringUtils.isAnyBlank(userName, userEmail, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userName.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }

        // 2. 账户不能重复
        User existUser = userMapper.selectByUserName(userName);
        if (existUser != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
        }

        // 3. 邮箱不能重复
        existUser = userMapper.selectByEmail(userEmail);
        if (existUser != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱已被注册");
        }

        // 4. 加密密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 5. 插入数据
        User user = new User();
        user.setUserName(userName);
        user.setUserEmail(userEmail);
        user.setUserPassword(encryptPassword);
        boolean saveResult = save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        return user.getUserId();
    }

    /**
     * 用户登录
     *
     * @param loginRequest 用户登录请求
     * @param request      HttpServlet请求
     * @return
     */
    @Override
    public User userLogin(UserLoginRequest loginRequest, HttpServletRequest request) {
        String userName = loginRequest.getUserName();
        String userPassword = loginRequest.getUserPassword();

        // 1. 校验
        if (StringUtils.isAnyBlank(userName, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 查询用户是否存在
        User user = userMapper.selectByUserName(userName);
        if (user == null) {
            log.info("user login failed, userName cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        // 4. 校验密码
        if (!user.getUserPassword().equals(encryptPassword)) {
            log.info("user login failed, userName cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 5. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        return UserUtils.getSafetyUser(user);
    }

    /**
     * @param request HttpServlet请求
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getUserId();
        currentUser = getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return currentUser;
    }

    /**
     * 用户退出
     *
     * @param request HttpServlet请求
     * @return 用户是否退出成功
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 判断是否为管理员
     *
     * @param request HttpServlet请求
     * @return ture / false
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserIsAdmin() == ADMIN_ROLE;
    }
}




