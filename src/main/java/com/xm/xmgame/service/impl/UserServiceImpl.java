package com.xm.xmgame.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.xmgame.common.ErrorCode;
import com.xm.xmgame.exception.BusinessException;
import com.xm.xmgame.mapper.UserMapper;
import com.xm.xmgame.model.domain.User;
import com.xm.xmgame.model.entity.email.VerifyCodeEmail;
import com.xm.xmgame.model.request.user.*;
import com.xm.xmgame.service.UserService;
import com.xm.xmgame.utils.UserUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.xm.xmgame.constant.UserConstant.ADMIN_ROLE;
import static com.xm.xmgame.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author X1aoM1ngTX
 * @描述 针对表【user(用户表)】的数据库操作Service实现
 * @创建时间 2024-10-10 13:26:55
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    JavaMailSender javaMailSender;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${spring.mail.username}")
    private String emailFrom;

    private static String EMAIL_FROM;

    private static final String SALT = "xm";

    private static final String VERIFY_CODE_PREFIX = "verify:code:";
    private static final long VERIFY_CODE_EXPIRE = 5; // 验证码有效期（分钟）

    @PostConstruct
    public void init() {
        EMAIL_FROM = emailFrom;
    }

    /**
     * 用户注册。
     *
     * @param registerRequest 用户注册请求，包含用户名、邮箱和密码。
     * @return 新注册用户的ID。
     * @throws BusinessException 如果参数为空、用户名过短、密码过短、用户名已存在、邮箱已被注册或数据库错误，抛出业务异常。
     */
    @Override
    public Long userRegister(UserRegisterRequest registerRequest) {
        String userName = registerRequest.getUserName();
        String userEmail = registerRequest.getUserEmail();
        String userPhone = registerRequest.getUserPhone();
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
        if (userPhone.length() < 11) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号过短");
        }
        if (StringUtils.isAnyBlank(userEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
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
        user.setUserPhone(userPhone);
        user.setUserPassword(encryptPassword);
        boolean saveResult = save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        return user.getUserId();
    }

    /**
     * 用户登录。
     *
     * @param loginRequest 用户登录请求，包含用户名和密码。
     * @param request      HttpServlet请求，用于获取Session和设置用户登录状态。
     * @return 安全的用户对象，不包含敏感信息。
     * @throws BusinessException 如果参数为空、用户不存在或密码错误，抛出业务异常。
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
     * 获取当前登录的用户。
     *
     * @param request HttpServlet请求，用于获取Session中的用户登录状态。
     * @return 当前登录的用户对象。
     * @throws BusinessException 如果用户未登录或用户信息无效，抛出业务异常。
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
     * 用户注销。
     *
     * @param logoutRequest HttpServlet请求，用于获取Session中的用户登录状态。
     * @return 返回 true 表示注销成功。
     * @throws BusinessException 如果用户未登录，抛出业务异常。
     */
    @Override
    public boolean userLogout(HttpServletRequest logoutRequest) {
        if (logoutRequest.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 移除登录态
        logoutRequest.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 更新用户信息。
     *
     * @param updateRequest 用户更新请求，包含用户名、邮箱和手机号码。
     * @param userId        用户ID。
     * @return 返回 true 表示更新成功。
     * @throws BusinessException 如果请求参数为空、用户不存在、当前用户无权限修改该用户信息或数据库操作失败，抛出业务异常。
     */
    @Override
    public boolean userModify(UserModifyRequest modifyRequest, Long userId) {
        if (modifyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        try {
            User user = getById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
            }

            // 更新用户名
            if (StringUtils.isNotBlank(modifyRequest.getUserName())) {
                user.setUserName(modifyRequest.getUserName());
            }

            // 更新邮箱地址
            if (StringUtils.isNotBlank(modifyRequest.getUserEmail())) {
                user.setUserEmail(modifyRequest.getUserEmail());
            }

            // 更新手机号码
            if (StringUtils.isNotBlank(modifyRequest.getUserPhone())) {
                user.setUserPhone(modifyRequest.getUserPhone());
            }

            // 更新用户简介
            if (StringUtils.isNotBlank(modifyRequest.getUserProfile())) {
                user.setUserProfile(modifyRequest.getUserProfile());
            }
            
            return updateById(user);
        } catch (Exception e) {
            // 记录日志并抛出自定义异常
            log.error("更新用户信息失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误");
        }
    }

    /**
     * 更新管理员用户信息。
     *
     * @param updateRequest 管理员用户更新请求，包含用户名、邮箱、手机号码和用户权限。
     * @param userId        用户ID。
     * @return 返回 true 表示更新成功。
     * @throws BusinessException 如果用户不存在、用户名已存在或数据库操作失败，抛出业务异常。
     */
    @Override
    public boolean adminUserUpdate(AdminUserUpdateRequest updateRequest, Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        // 检查新用户名是否与当前用户名相同
        if (StringUtils.isNotBlank(updateRequest.getUserName()) && !updateRequest.getUserName().equals(user.getUserName())) {
            // 检查新用户名是否已经存在
            User existingUser = getOne(Wrappers.<User>lambdaQuery().eq(User::getUserName, updateRequest.getUserName()));
            if (existingUser != null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在，请选择其他用户名");
            }
            user.setUserName(updateRequest.getUserName());
        }

        // 更新邮箱
        if (StringUtils.isNotBlank(updateRequest.getUserEmail())) {
            user.setUserEmail(updateRequest.getUserEmail());
        }

        // 更新手机号码
        if (StringUtils.isNotBlank(updateRequest.getUserPhone())) {
            user.setUserPhone(updateRequest.getUserPhone());
        }

        // 更新用户权限
        if (updateRequest.getUserIsAdmin() != null) {
            user.setUserIsAdmin(updateRequest.getUserIsAdmin());
        }

        return updateById(user);
    }

    @Override
    public void sendEmail(EmailSendToUserRequest sendRequest) throws MessagingException, UnsupportedEncodingException {
        if (sendRequest == null || StringUtils.isBlank(sendRequest.getToEmail())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }

        // 生成6位随机验证码
        String verifyCode = String.format("%06d", new Random().nextInt(1000000));
        
        // 存入Redis，设置过期时间
        String key = VERIFY_CODE_PREFIX + sendRequest.getToEmail();
        try {
            // 先删除旧的验证码
            stringRedisTemplate.delete(key);
            // 存入新验证码
            stringRedisTemplate.opsForValue().set(key, verifyCode, VERIFY_CODE_EXPIRE, TimeUnit.MINUTES);
            
            // 打印日志，方便调试
            log.info("验证码已存储到Redis，key={}, code={}, expireTime={}分钟", key, verifyCode, VERIFY_CODE_EXPIRE);
            
            // 创建邮件内容
            String content = String.format(VerifyCodeEmail.content,
                VerifyCodeEmail.title,
                verifyCode
            );

            // 发送邮件
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(EMAIL_FROM, VerifyCodeEmail.organization);
            helper.setTo(sendRequest.getToEmail());
            helper.setSubject(VerifyCodeEmail.title);
            helper.setText(content, true);
            javaMailSender.send(message);
            
        } catch (Exception e) {
            log.error("验证码发送失败：", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码发送失败，请稍后重试");
        }
    }

    @Override
    public boolean verifyCode(VerifyCodeRequest verifyRequest) {
        if (verifyRequest == null || StringUtils.isAnyBlank(verifyRequest.getEmail(), verifyRequest.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不完整");
        }

        // 从Redis获取验证码
        String key = VERIFY_CODE_PREFIX + verifyRequest.getEmail();
        String savedCode = stringRedisTemplate.opsForValue().get(key);
        
        // 打印日志，方便调试
        log.info("验证码校验：key={}, savedCode={}, inputCode={}", 
            key, savedCode, verifyRequest.getCode());
        
        if (savedCode == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码已过期");
        }

        // 验证码匹配
        boolean isMatch = savedCode.equals(verifyRequest.getCode());
        if (isMatch) {
            // 验证成功后删除验证码
            stringRedisTemplate.delete(key);
            return true;
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
    }

    @Override
    public boolean resetPassword(ResetPasswordRequest resetRequest) {
        if (resetRequest == null || 
            StringUtils.isAnyBlank(
                resetRequest.getEmail(), 
                resetRequest.getVerifyCode(), 
                resetRequest.getNewPassword()
            )) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不完整");
        }

        // 验证验证码
        VerifyCodeRequest verifyRequest = new VerifyCodeRequest();
        verifyRequest.setEmail(resetRequest.getEmail());
        verifyRequest.setCode(resetRequest.getVerifyCode());
        if (!verifyCode(verifyRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }

        // 查找用户
        User user = userMapper.selectByEmail(resetRequest.getEmail());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 更新密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + resetRequest.getNewPassword()).getBytes());
        user.setUserPassword(encryptPassword);
        
        return updateById(user);
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


