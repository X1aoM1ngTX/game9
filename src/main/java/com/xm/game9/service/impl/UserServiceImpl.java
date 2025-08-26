package com.xm.game9.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.mapper.UserMapper;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.admin.AdminUserUpdateRequest;
import com.xm.game9.model.request.admin.BatchImportUsersRequest;
import com.xm.game9.model.request.user.*;
import com.xm.game9.service.UserService;
import com.xm.game9.utils.EmailUtil;
import com.xm.game9.utils.RedisUtil;
import com.xm.game9.utils.UploadUtil;
import com.xm.game9.utils.UserUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.xm.game9.constant.UserConstant.ADMIN_ROLE;
import static com.xm.game9.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author X1aoM1ngTX
 * @描述 针对表【user(用户表)】的数据库操作Service实现
 * @创建时间 2024-10-10 13:26:55
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String VERIFY_CODE_PREFIX = "verify:code:";
    private static final String SALT = "xm";
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Resource
    private UserMapper userMapper;
    @Resource
    private JavaMailSender javaMailSender;
    @Resource
    private UploadUtil uploadUtil;
    @Value("${spring.mail.username}")
    private String emailFrom;
    @Resource
    private RedisUtil redisUtil;

    // 初始化邮箱地址
    @PostConstruct
    public void init() {
    }

    // 检查 Redis 连接
    private void checkRedisConnection() {
        try {
            redisUtil.checkConnection();
        } catch (BusinessException e) {
            log.error("Redis服务未启动: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 用户注册
     *
     * @param registerRequest 用户注册请求，包含用户名、邮箱和密码。
     * @return 新注册用户的ID。
     * @throws BusinessException 如果参数为空、用户名过短、密码过短、用户名已存在、邮箱已被注册或数据库错误，抛出业务异常。
     */
    @Override
    public Long userRegister(UserRegisterRequest registerRequest) {
        // 1. 校验数据
        if (registerRequest == null || StringUtils.isAnyBlank(
                registerRequest.getUserName(),
                registerRequest.getUserEmail(),
                registerRequest.getUserPassword(),
                registerRequest.getUserCheckPassword())) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "注册信息不完整");
        }
        if (registerRequest.getUserName().length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名长度不能小于4位");
        }
        if (registerRequest.getUserPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8位");
        }
        if (StringUtils.isAnyBlank(registerRequest.getUserEmail())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }

        // 2. 两次密码不一致
        if (!registerRequest.getUserPassword().equals(registerRequest.getUserCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }

        // 3. 校验邮箱格式
        if (!registerRequest.getUserEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        // 检查 Redis 连接
        checkRedisConnection();

        // 4. 校验验证码
        String cacheCode = redisUtil.get(VERIFY_CODE_PREFIX + registerRequest.getUserEmail());
        if (cacheCode == null || !cacheCode.equals(registerRequest.getVerifyCode())) {
            throw new BusinessException(ErrorCode.USER_EMAIL_CODE_ERROR, "验证码错误或已过期");
        }

        // 5. 账户不能重复
        User existUser = userMapper.selectByUserName(registerRequest.getUserName());
        if (existUser != null) {
            throw new BusinessException(ErrorCode.USER_ACCOUNT_ALREADY_EXIST, "用户名已存在");
        }

        // 6. 检查邮箱是否已被注册
        existUser = userMapper.selectByEmail(registerRequest.getUserEmail());
        if (existUser != null) {
            throw new BusinessException(ErrorCode.USER_EMAIL_ALREADY_EXIST, "邮箱已存在");
        }

        // 7. 加密密码
        String encryptPassword = passwordEncoder.encode(registerRequest.getUserPassword());

        // 8. 插入数据
        User user = new User();
        user.setUserName(registerRequest.getUserName());
        user.setUserEmail(registerRequest.getUserEmail());
        user.setUserPassword(encryptPassword);
        boolean saveResult = save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        log.info("用户注册成功: {}", user.getUserId());

        // 9. 删除验证码
        if (saveResult) {
            redisUtil.delete(VERIFY_CODE_PREFIX + registerRequest.getUserEmail());
            log.info("验证码删除成功: {}", registerRequest.getUserEmail());
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
            throw new BusinessException(ErrorCode.NULL_ERROR, "登录信息不完整");
        }
        // 2. 查询用户是否存在
        User user = userMapper.selectByUserName(userName);
        if (user == null) {
            log.info("user login failed, userName cannot match userPassword");
            throw new BusinessException(ErrorCode.USER_ACCOUNT_NOT_EXIST, "用户名不存在");
        }
        // 3. 校验密码（先BCrypt，后MD5，自动升级）
        boolean passwordMatch = false;
        if (passwordEncoder.matches(userPassword, user.getUserPassword())) {
            passwordMatch = true;
        } else {
            // 兼容老用户MD5
            String md5Password = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            if (user.getUserPassword().equals(md5Password)) {
                passwordMatch = true;
                // 自动升级为BCrypt
                user.setUserPassword(passwordEncoder.encode(userPassword));
                updateById(user);
                log.info("老用户密码已自动升级为BCrypt: {}", user.getUserId());
            }
        }
        if (!passwordMatch) {
            log.info("user login failed, userName cannot match userPassword");
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR, "密码错误");
        }
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        log.info("[Service-登录] 用户名:{}, IP:{}, 时间:{}", userName, request.getRemoteAddr(), java.time.LocalDateTime.now());
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
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getUserId();
        currentUser = getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
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
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        // 移除登录态
        logoutRequest.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 更新用户信息。
     *
     * @param updateRequest 用户更新请求，包含用户名、昵称、邮箱和手机号码。
     * @param userId        用户ID。
     * @return 返回 true 表示更新成功。
     * @throws BusinessException 如果请求参数为空、用户不存在、当前用户无权限修改该用户信息或数据库操作失败，抛出业务异常。
     */
    @Override
    public boolean userModify(UserModifyRequest updateRequest, Long userId) {
        if (StringUtils.isAnyBlank(updateRequest.getUserName(), updateRequest.getUserNickname())) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户名和昵称不能为空");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 检查用户名是否被其他用户使用
        User existUser = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getUserName, updateRequest.getUserName())
                        .ne(User::getUserId, userId));
        if (existUser != null) {
            throw new BusinessException(ErrorCode.USER_ACCOUNT_ALREADY_EXIST, "用户名已存在");
        }

        user.setUserName(updateRequest.getUserName());
        user.setUserNickname(updateRequest.getUserNickname());
        user.setUserEmail(updateRequest.getUserEmail());
        
        // 校验手机号格式
        if (StringUtils.isNotBlank(updateRequest.getUserPhone())) {
            if (!updateRequest.getUserPhone().matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式不正确");
            }
            // 检查手机号是否被其他用户使用
            User existPhoneUser = userMapper.selectOne(
                    Wrappers.<User>lambdaQuery()
                            .eq(User::getUserPhone, updateRequest.getUserPhone())
                            .ne(User::getUserId, userId));
            if (existPhoneUser != null) {
                throw new BusinessException(ErrorCode.USER_PHONE_ALREADY_EXIST, "手机号已被其他用户使用");
            }
            user.setUserPhone(updateRequest.getUserPhone());
        }
        
        user.setUserProfile(updateRequest.getUserProfile());

        log.info("[Service-用户信息修改] 用户ID:{}, 时间:{}", userId, java.time.LocalDateTime.now());
        return updateById(user);
    }

    /**
     * 更新管理员用户信息。
     *
     * @param updateRequest 管理员用户更新请求，包含用户名、昵称、邮箱、手机号码和用户权限。
     * @param userId        用户ID。
     * @return 返回 true 表示更新成功。
     * @throws BusinessException 如果用户不存在、用户名已存在或数据库操作失败，抛出业务异常。
     */
    @Override
    public boolean adminUserUpdate(AdminUserUpdateRequest updateRequest, Long userId) {
        if (StringUtils.isAnyBlank(updateRequest.getUserName(), updateRequest.getUserNickname())) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户名和昵称不能为空");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 检查用户名是否被其他用户使用
        User existUser = userMapper.selectOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getUserName, updateRequest.getUserName())
                        .ne(User::getUserId, userId));
        if (existUser != null) {
            throw new BusinessException(ErrorCode.USER_ACCOUNT_ALREADY_EXIST, "用户名已存在");
        }

        user.setUserName(updateRequest.getUserName());
        user.setUserNickname(updateRequest.getUserNickname());
        user.setUserEmail(updateRequest.getUserEmail());
        
        // 校验手机号格式
        if (StringUtils.isNotBlank(updateRequest.getUserPhone())) {
            if (!updateRequest.getUserPhone().matches("^1[3-9]\\d{9}$")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式不正确");
            }
            // 检查手机号是否被其他用户使用
            User existPhoneUser = userMapper.selectOne(
                    Wrappers.<User>lambdaQuery()
                            .eq(User::getUserPhone, updateRequest.getUserPhone())
                            .ne(User::getUserId, userId));
            if (existPhoneUser != null) {
                throw new BusinessException(ErrorCode.USER_PHONE_ALREADY_EXIST, "手机号已被其他用户使用");
            }
            user.setUserPhone(updateRequest.getUserPhone());
        }
        
        user.setUserIsAdmin(updateRequest.getUserIsAdmin());

        return updateById(user);
    }

    /**
     * 发送邮箱验证码。
     *
     * @param toEmail 目标邮箱地址。
     * @throws BusinessException 如果发送失败，抛出业务异常。
     */
    @Override
    public void sendEmailCode(String toEmail) {
        // 检查 Redis 连接
        checkRedisConnection();

        // 使用单例模式获取 EmailUtil 实例并发送验证码
        try {
            EmailUtil.getInstance().sendVerificationCode(toEmail);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.USER_EMAIL_SEND_ERROR);
        }
    }

    /**
     * 验证邮箱验证码。
     *
     * @param verifyRequest 验证请求，包含邮箱和验证码。
     * @return 返回 true 表示验证成功。
     * @throws BusinessException 如果参数不完整、验证码已过期或验证码错误，抛出业务异常。
     */
    @Override
    public boolean verifyCode(VerifyCodeRequest verifyRequest) {
        if (verifyRequest == null || StringUtils.isAnyBlank(verifyRequest.getEmail(), verifyRequest.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不完整");
        }

        // 检查 Redis 连接
        checkRedisConnection();

        // 从Redis获取验证码
        String key = VERIFY_CODE_PREFIX + verifyRequest.getEmail();
        String savedCode = redisUtil.get(key);

        log.info("验证码校验 - 邮箱: {}, 输入验证码: {}, 存储验证码: {}",
                verifyRequest.getEmail(), verifyRequest.getCode(), savedCode);

        if (savedCode == null) {
            log.warn("验证码已过期 - 邮箱: {}", verifyRequest.getEmail());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码已过期，请重新获取");
        }

        // 验证码匹配（不区分大小写）
        if (savedCode.equalsIgnoreCase(verifyRequest.getCode())) {
            // 验证成功后刷新过期时间
            redisUtil.expire(key, 5, TimeUnit.MINUTES);
            log.info("验证码验证成功 - 邮箱: {}", verifyRequest.getEmail());
            return true;
        }

        log.warn("验证码错误 - 邮箱: {}, 输入: {}, 正确: {}",
                verifyRequest.getEmail(), verifyRequest.getCode(), savedCode);
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
    }

    /**
     * 重置密码。
     *
     * @param resetRequest 重置请求，包含邮箱、验证码和新密码。
     * @return 返回 true 表示重置成功。
     * @throws BusinessException 如果参数不完整、密码格式不正确或验证码错误，抛出业务异常。
     */
    @Override
    public boolean resetPassword(ResetPasswordRequest resetRequest) {
        // 参数校验
        if (resetRequest == null ||
                StringUtils.isAnyBlank(
                        resetRequest.getEmail(),
                        resetRequest.getVerifyCode(),
                        resetRequest.getNewPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不完整");
        }

        // 检查 Redis 连接
        checkRedisConnection();

        // 验证密码格式
        if (resetRequest.getNewPassword().length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8位");
        }

        // 从Redis获取验证码
        String key = VERIFY_CODE_PREFIX + resetRequest.getEmail();
        String savedCode = redisUtil.get(key);

        log.info("重置密码 - 邮箱: {}, 验证码: {}, Redis中的验证码: {}",
                resetRequest.getEmail(), resetRequest.getVerifyCode(), savedCode);

        // 验证码校验
        if (savedCode == null) {
            log.warn("重置密码失败 - 验证码已过期，邮箱: {}", resetRequest.getEmail());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码已过期，请重新获取");
        }

        if (!savedCode.equalsIgnoreCase(resetRequest.getVerifyCode())) {
            log.warn("重置密码失败 - 验证码错误，邮箱: {}, 输入: {}, 正确: {}", resetRequest.getEmail(), resetRequest.getVerifyCode(),
                    savedCode);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }

        // 更新密码
        User user = lambdaQuery()
                .eq(User::getUserEmail, resetRequest.getEmail())
                .one();

        if (user == null) {
            log.warn("重置密码失败 - 用户不存在，邮箱: {}", resetRequest.getEmail());
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 加密新密码
        String encryptedPassword = passwordEncoder.encode(resetRequest.getNewPassword());
        user.setUserPassword(encryptedPassword);

        // 更新成功后删除验证码
        boolean updated = updateById(user);
        if (updated) {
            redisUtil.delete(key);
            log.info("重置密码成功 - 邮箱: {}", resetRequest.getEmail());
        }

        log.info("[Service-重置密码] 邮箱:{}, 时间:{}", resetRequest.getEmail(), java.time.LocalDateTime.now());
        return updated;
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

    /**
     * 更新用户头像
     *
     * @param userId 用户ID
     * @param file   头像文件
     * @return 新的头像URL
     */
    @Override
    public String updateUserAvatar(Long userId, MultipartFile file) {
        // 1. 参数校验
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "头像文件不能为空");
        }

        // 2. 校验文件大小（限制为2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.FILE_SIZE_ERROR, "头像文件大小不能超过2MB");
        }

        // 3. 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.FILE_TYPE_ERROR, "文件类型必须是图片");
        }

        try {
            // 4. 上传文件到阿里云OSS
            String avatarUrl = uploadUtil.uploadR2(file);

            // 5. 更新用户头像URL
            User user = getById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
            }

            user.setUserAvatar(avatarUrl);
            boolean updated = updateById(user);
            if (!updated) {
                throw new BusinessException(ErrorCode.USER_AVATAR_UPLOAD_ERROR, "更新用户头像失败");
            }

            return avatarUrl;
        } catch (IOException e) {
            log.error("上传头像失败", e);
            throw new BusinessException(ErrorCode.USER_AVATAR_UPLOAD_ERROR, "上传头像失败");
        }
    }

    /**
     * 批量导入用户
     *
     * @param users 用户列表
     * @return 导入的用户数量
     * @throws BusinessException 如果用户列表为空或用户信息不完整，抛出业务异常。
     */
    @Override
    public int batchImportUsers(List<BatchImportUsersRequest.UserImportInfo> users) {
        if (users == null || users.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户列表为空");
        }

        List<User> userList = new ArrayList<>();
        for (BatchImportUsersRequest.UserImportInfo userInfo : users) {
            // 参数校验
            if (StringUtils.isAnyBlank(
                    userInfo.getUserName(),
                    userInfo.getUserEmail(),
                    userInfo.getUserPassword())) {
                log.warn("用户信息不完整: {}", userInfo);
                continue;
            }

            // 检查用户名是否已存在
            if (userMapper.selectByUserName(userInfo.getUserName()) != null) {
                log.warn("用户名已存在: {}", userInfo.getUserName());
                continue;
            }

            // 创建用户对象
            User user = new User();
            user.setUserName(userInfo.getUserName());
            user.setUserEmail(userInfo.getUserEmail());
            user.setUserPassword(passwordEncoder.encode(userInfo.getUserPassword()));
            user.setUserPhone(userInfo.getUserPhone());
            user.setUserIsAdmin(userInfo.getUserIsAdmin());

            userList.add(user);
        }

        if (userList.isEmpty()) {
            return 0;
        }

        // 批量插入
        saveBatch(userList);
        return userList.size();
    }

    /**
     * 用户签到
     *
     * @param userId 用户ID
     */
    @Override
    public void userSignIn(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        // 获取当前日期
        LocalDate today = LocalDate.now();
        String key = String.format("sign:%d:%d", userId, today.getYear());
        int dayOfYear = today.getDayOfYear() - 1; // 从0开始计数

        try {
            // 判断是否已经签到
            Boolean signed = redisUtil.getBit(key, dayOfYear);
            if (Boolean.TRUE.equals(signed)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "今天已经签到过了");
            }

            // 设置签到标记
            redisUtil.setBit(key, dayOfYear, true);

            // 设置过期时间为下一年的1月1日
            LocalDate firstDayOfNextYear = today.plusYears(1).withDayOfYear(1);
            long expireSeconds = firstDayOfNextYear.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
                    - System.currentTimeMillis() / 1000;
            redisUtil.expire(key, expireSeconds, TimeUnit.SECONDS);

            log.info("用户签到成功 - userId: {}, date: {}", userId, today);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户签到失败 - userId: {}, error: {}", userId, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "签到失败，请稍后重试");
        }
    }

    /**
     * 检查用户是否签到
     *
     * @param userId 用户ID
     * @param date   日期
     * @return 是否签到
     */
    @Override
    public boolean checkSignIn(Long userId, LocalDate date) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        if (date == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "日期不能为空");
        }

        String key = String.format("sign:%d:%d", userId, date.getYear());
        int dayOfYear = date.getDayOfYear() - 1;

        try {
            Boolean signed = redisUtil.getBit(key, dayOfYear);
            return Boolean.TRUE.equals(signed);
        } catch (Exception e) {
            log.error("检查签到状态失败 - userId: {}, date: {}, error: {}", userId, date, e.getMessage());
            throw new BusinessException(ErrorCode.USER_SIGN_IN_ERROR, "检查签到状态失败");
        }
    }

    /**
     * 获取用户签到历史
     *
     * @param userId 用户ID
     * @param year   年份
     * @return 签到日期列表
     */
    @Override
    public List<LocalDate> getSignInHistory(Long userId, int year) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        if (year <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "年份不正确");
        }

        String key = String.format("sign:%d:%d", userId, year);
        List<LocalDate> signInDates = new ArrayList<>();

        try {
            int daysInYear = Year.of(year).length();
            for (int i = 0; i < daysInYear; i++) {
                if (Boolean.TRUE.equals(redisUtil.getBit(key, i))) {
                    signInDates.add(LocalDate.ofYearDay(year, i + 1));
                }
            }
            return signInDates;
        } catch (Exception e) {
            log.error("获取签到历史失败 - userId: {}, year: {}, error: {}", userId, year, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取签到历史失败");
        }
    }

    /**
     * 获取用户一年内的签到次数
     *
     * @param userId 用户ID
     * @param year   年份
     * @return 签到次数
     */
    @Override
    public long countSignInDays(Long userId, int year) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        if (year <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "年份不正确");
        }

        try {
            String key = String.format("sign:%d:%d", userId, year);
            return redisUtil.bitCount(key);
        } catch (Exception e) {
            log.error("统计签到次数失败 - userId: {}, year: {}, error: {}", userId, year, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "统计签到次数失败");
        }
    }
}
