package com.xm.game9.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.admin.AdminUserUpdateRequest;
import com.xm.game9.model.request.admin.BatchImportGamesRequest;
import com.xm.game9.model.request.admin.BatchImportUsersRequest;
import com.xm.game9.model.request.user.*;
import com.xm.game9.service.GameService;
import com.xm.game9.service.UserService;
import com.xm.game9.utils.RedisUtil;
import com.xm.game9.utils.UploadUtil;
import com.xm.game9.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户接口
 *
 * @author X1aoM1ngTX
 */
@Tag(name = "用户接口", description = "用户相关的所有接口")
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private GameService gameService;

    @Resource
    private UploadUtil uploadUtil;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 用户ID
     */
    @Operation(summary = "用户注册", description = "注册新的用户")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest registerRequest) {
        if (registerRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (StringUtils.isAnyBlank(registerRequest.getUserName(), registerRequest.getUserEmail(),
                registerRequest.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        long result = userService.userRegister(registerRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @param request      HTTP请求
     * @return 用户
     */
    @Operation(summary = "用户登录", description = "用户登录")
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest loginRequest, HttpServletRequest request) {
        if (loginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (StringUtils.isAnyBlank(loginRequest.getUserName(), loginRequest.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String userName = loginRequest.getUserName();
        String failKey = "login:fail:" + userName;
        String lockKey = "login:lock:" + userName;
        // 登录失败次数限制
        if (redisUtil.hasKey(lockKey)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "账号已锁定，请10分钟后再试");
        }
        try {
            User user = userService.userLogin(loginRequest, request);
            // 登录成功，清除失败计数
            redisUtil.delete(failKey);
            log.info("[登录成功] 用户名:{}, IP:{}, 时间:{}", userName, request.getRemoteAddr(), LocalDateTime.now());
            redisUtil.setWithExpire("user:online:" + user.getUserId(), "1", 60, TimeUnit.SECONDS);
            return ResultUtils.success(user);
        } catch (BusinessException e) {
            // 登录失败，计数+1
            long failCount = 1;
            if (redisUtil.hasKey(failKey)) {
                String val = redisUtil.get(failKey);
                try {
                    failCount = Long.parseLong(val) + 1;
                } catch (Exception ignore) {
                }
            }
            redisUtil.setWithExpire(failKey, String.valueOf(failCount), 10, TimeUnit.MINUTES);
            log.warn("[登录失败] 用户名:{}, IP:{}, 时间:{}, 失败次数:{}", userName, request.getRemoteAddr(), LocalDateTime.now(), failCount);
            // 超过5次锁定10分钟
            if (failCount >= 5) {
                redisUtil.setWithExpire(lockKey, "1", 10, TimeUnit.MINUTES);
                log.warn("[账号锁定] 用户名:{}, IP:{}, 时间:{}", userName, request.getRemoteAddr(), LocalDateTime.now());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "账号已锁定，请10分钟后再试");
            }
            throw e;
        }
    }

    /**
     * 用户注销
     *
     * @param request HTTP请求
     * @return 是否注销成功
     */
    @Operation(summary = "用户退出登录", description = "用户退出登录")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        boolean result = userService.userLogout(request);
        if (loginUser != null) {
            redisUtil.delete("user:online:" + loginUser.getUserId());
        }
        return ResultUtils.success(result);
    }

    /**
     * 发送验证码邮件
     *
     * @param request 发送请求
     * @return 验证码是否发送成功
     */
    @Operation(summary = "发送验证码邮件", description = "发送验证码到指定邮箱")
    @PostMapping("/sendEmailCode")
    public BaseResponse<String> sendEmailCode(@RequestBody EmailSendToUserRequest request) {
        try {
            if (request == null || StringUtils.isBlank(request.getToEmail())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
            }
            String email = request.getToEmail();
            String limitKey = "email:code:limit:" + email;
            // 频率限制：1分钟最多3次
            long count = 1;
            if (redisUtil.hasKey(limitKey)) {
                String val = redisUtil.get(limitKey);
                try {
                    count = Long.parseLong(val) + 1;
                } catch (Exception ignore) {
                }
            }
            if (count > 3) {
                log.warn("[验证码发送频率超限] 邮箱:{}, IP:{}, 时间:{}", email, null, LocalDateTime.now());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码发送过于频繁，请稍后再试");
            }
            redisUtil.setWithExpire(limitKey, String.valueOf(count), 1, TimeUnit.MINUTES);
            // 验证邮箱格式
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
            }
            userService.sendEmailCode(email);
            log.info("[验证码发送] 邮箱:{}, IP:{}, 时间:{}", email, null, LocalDateTime.now());
            return ResultUtils.success("验证码发送成功");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送验证码失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "验证码发送失败，请稍后重试");
        }
    }

    /**
     * 验证验证码
     *
     * @param verifyRequest 验证请求
     * @return 是否成功
     */
    @Operation(summary = "验证验证码", description = "验证验证码")
    @PostMapping("/verifyCode")
    public BaseResponse<Boolean> verifyCode(@RequestBody VerifyCodeRequest verifyRequest) {
        if (verifyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        boolean result = userService.resetPassword(resetRequest);
        log.info("[重置密码] 邮箱:{}, 时间:{}", resetRequest.getEmail(), LocalDateTime.now());
        return ResultUtils.success(result);
    }

    /**
     * 获取当前用户
     *
     * @param request HTTP请求
     * @return 当前用户
     */
    @Operation(summary = "获取当前用户", description = "获取当前用户")
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        return ResultUtils.success(UserUtils.getSafetyUser(loginUser));
    }

    /**
     * 查询用户
     *
     * @param username 用户名
     * @param request  HTTP请求
     * @return 用户列表
     */
    @Operation(summary = "查询用户", description = "查询用户")
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("userName", username)
                    .or()
                    .like("userNickname", username);
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
     * @param modifyRequest 用户更新请求
     * @param request       HTTP请求
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
        log.info("[用户信息修改] 用户ID:{}, 时间:{}", loginUser.getUserId(), LocalDateTime.now());
        return ResultUtils.success(result);
    }

    /**
     * 管理员用户信息修改
     *
     * @param updateRequest 管理员用户更新请求
     * @return 是否更新成功
     */
    @Operation(summary = "管理员用户信息修改", description = "管理员用户信息修改")
    @PostMapping("/adminUpdate")
    public BaseResponse<Boolean> adminUserUpdate(@RequestBody AdminUserUpdateRequest updateRequest) {
        if (updateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 检查管理员权限
        @SuppressWarnings("null")
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
        }
        boolean result = userService.adminUserUpdate(updateRequest, updateRequest.getUserId());
        return ResultUtils.success(result);
    }

    /**
     * 删除用户
     *
     * @param deleteRequest 用户删除请求
     * @param request       HTTP请求
     * @return 是否删除成功
     */
    @Operation(summary = "删除用户", description = "删除用户")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody UserDeleteRequest deleteRequest, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
        }
        if (deleteRequest == null || deleteRequest.getUserId() == null || deleteRequest.getUserId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        boolean result = userService.removeById(deleteRequest.getUserId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户头像
     *
     * @param file    头像文件
     * @param request HTTP请求
     * @return 新的头像URL
     */
    @Operation(summary = "更新用户头像", description = "上传并更新用户的头像")
    @PostMapping(value = "/updateAvatar")
    public BaseResponse<String> updateAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }

        try {
            String avatarUrl = uploadUtil.uploadR2(file);

            // 更新用户头像
            User user = new User();
            user.setUserId(loginUser.getUserId());
            user.setUserAvatar(avatarUrl);
            boolean updated = userService.updateById(user);
            if (!updated) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新用户头像失败");
            }

            return ResultUtils.success(avatarUrl);
        } catch (IOException e) {
            log.error("上传头像失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传头像失败");
        }
    }

    /**
     * 批量导入用户
     *
     * @param importRequest 导入请求
     * @param httpRequest   HTTP请求
     * @return 导入结果
     */
    @Operation(summary = "批量导入用户", description = "批量导入用户")
    @PostMapping("/batchImportUsers")
    public BaseResponse<Integer> batchImportUsers(@RequestBody BatchImportUsersRequest importRequest,
                                                  HttpServletRequest httpRequest) {
        // 仅管理员可操作
        if (!userService.isAdmin(httpRequest)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
        }

        int count = userService.batchImportUsers(importRequest.getUsers());
        return ResultUtils.success(count);
    }

    /**
     * 批量导入游戏
     *
     * @param importRequest 导入请求
     * @param httpRequest   HTTP请求
     * @return 导入结果
     */
    @Operation(summary = "批量导入游戏", description = "批量导入游戏")
    @PostMapping("/batchImportGames")
    public BaseResponse<Integer> batchImportGames(@RequestBody BatchImportGamesRequest importRequest,
                                                  HttpServletRequest httpRequest) {
        // 仅管理员可操作
        if (!userService.isAdmin(httpRequest)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户无权限");
        }

        int count = gameService.batchImportGames(importRequest.getGames());
        return ResultUtils.success(count);
    }

    /**
     * 用户签到
     *
     * @param request HTTP请求
     * @return 是否签到成功
     */
    @Operation(summary = "用户签到", description = "用户每日签到")
    @PostMapping("/signIn")
    public BaseResponse<Void> signIn(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }

        userService.userSignIn(loginUser.getUserId());
        return ResultUtils.success(null);
    }

    /**
     * 检查今日是否已签到
     *
     * @param request HTTP请求
     * @return 是否签到成功
     */
    @Operation(summary = "检查今日是否已签到", description = "检查用户今日是否已完成签到")
    @GetMapping("/sign/check")
    public BaseResponse<Boolean> checkTodaySignIn(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }

        boolean signed = userService.checkSignIn(loginUser.getUserId(), LocalDate.now());
        return ResultUtils.success(signed);
    }

    /**
     * 获取签到历史
     *
     * @param year    年份
     * @param request HTTP请求
     * @return 签到历史
     */
    @Operation(summary = "获取签到历史", description = "获取用户指定年份的签到记录")
    @GetMapping("/sign/history")
    public BaseResponse<List<LocalDate>> getSignInHistory(
            @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") Integer year,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }

        List<LocalDate> history = userService.getSignInHistory(loginUser.getUserId(), year);
        return ResultUtils.success(history);
    }

    /**
     * 获取签到统计
     *
     * @param year    年份
     * @param request HTTP请求
     * @return 签到统计
     */
    @Operation(summary = "获取签到统计", description = "获取用户指定年份的签到总天数")
    @GetMapping("/sign/count")
    public BaseResponse<Long> getSignInCount(
            @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") Integer year,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }

        long count = userService.countSignInDays(loginUser.getUserId(), year);
        return ResultUtils.success(count);
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户信息")
    @GetMapping("/{id}")
    public BaseResponse<User> getUserById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return ResultUtils.success(UserUtils.getSafetyUser(user));
    }

    /**
     * 检查用户名是否已被注册（注册时使用，不需要权限）
     *
     * @param userName 用户名
     * @return 用户名是否可用
     */
    @Operation(summary = "检查用户名是否可用", description = "检查用户名是否已被注册，注册时使用，不需要权限")
    @GetMapping("/checkUsernameAvailable")
    public BaseResponse<Boolean> checkUsernameAvailable(@RequestParam String userName) {
        if (StringUtils.isBlank(userName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名不能为空");
        }
        
        // 检查用户名是否已被注册
        User existUser = userService.lambdaQuery()
                .eq(User::getUserName, userName)
                .one();
        
        // 如果用户名已被注册，返回false；否则返回true
        return ResultUtils.success(existUser == null);
    }

  /**
     * 检查邮箱是否已被注册（注册时使用，不需要权限）
     *
     * @param email 邮箱地址
     * @return 邮箱是否可用
     */
    @Operation(summary = "检查邮箱是否可用", description = "检查邮箱是否已被注册，注册时使用，不需要权限")
    @GetMapping("/checkEmailAvailable")
    public BaseResponse<Boolean> checkEmailAvailable(@RequestParam String email) {
        if (StringUtils.isBlank(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }
        
        // 验证邮箱格式
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }
        
        // 检查邮箱是否已被注册
        User existUser = userService.lambdaQuery()
                .eq(User::getUserEmail, email)
                .one();
        
        // 如果邮箱已被注册，返回false；否则返回true
        return ResultUtils.success(existUser == null);
    }

    /**
     * 用户心跳，刷新在线状态
     *
     * @param request HTTP请求
     * @return 是否成功
     */
    @Operation(summary = "用户心跳", description = "刷新在线状态")
    @PostMapping("/heartbeat")
    public BaseResponse<Boolean> heartbeat(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        redisUtil.setWithExpire("user:online:" + loginUser.getUserId(), "1", 60, TimeUnit.SECONDS);
        return ResultUtils.success(true);
    }
}
