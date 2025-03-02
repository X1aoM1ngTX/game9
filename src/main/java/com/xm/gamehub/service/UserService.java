package com.xm.gamehub.service;

import com.xm.gamehub.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.gamehub.model.request.user.*;
import com.xm.gamehub.model.request.admin.AdminUserUpdateRequest;
import com.xm.gamehub.model.request.admin.BatchImportUsersRequest;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 用户服务
 *
 * @author X1aoM1ngTX
 * @描述 针对表【user(用户表)】的数据库操作Service
 * @创建时间 2024-10-10 13:26:55
 */
@Service
@Transactional
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
     * 用户信息修改
     */
    boolean userModify(UserModifyRequest updateRequest, Long userId);

    /**
     * 管理员用户信息修改
     */
    boolean adminUserUpdate(AdminUserUpdateRequest updateRequest, Long userId);

    /**
     * 发送邮件
     */
    void sendEmailCode(String toEmail) throws MessagingException, UnsupportedEncodingException;

    /**
     * 验证验证码
     */
    boolean verifyCode(VerifyCodeRequest verifyRequest);

    /**
     * 重置密码
     */
    boolean resetPassword(ResetPasswordRequest resetRequest);

    /**
     * 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 更新用户头像
     */
    String updateUserAvatar(Long userId, MultipartFile file);

    /**
     * 批量导入用户
     */
    int batchImportUsers(List<BatchImportUsersRequest.UserImportInfo> users);

    /**
     * 用户签到
     */
    void userSignIn(Long userId);

    /**
     * 查询某天是否签到
     */
    boolean checkSignIn(Long userId, LocalDate date);

    /**
     * 获取用户签到历史（返回已签到的日期列表）
     */
    List<LocalDate> getSignInHistory(Long userId, int year);

    /**
     * 获取用户一年内的签到次数
     */
    long countSignInDays(Long userId, int year);
}