package com.xm.game9.service.impl;

import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.mapper.UserMapper;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.user.UserRegisterRequest;
import com.xm.game9.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplSimpleTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void userRegister_Success() {
        // 准备测试数据
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserName("testuser");
        registerRequest.setUserEmail("test@example.com");
        registerRequest.setUserPassword("password123");
        registerRequest.setUserCheckPassword("password123");
        registerRequest.setVerifyCode("123456");
        registerRequest.setEncrypted(false);

        // 模拟Redis返回验证码
        when(redisUtil.get(anyString())).thenReturn("123456");
        
        // 模拟用户名不存在
        when(userMapper.selectByUserName("testuser")).thenReturn(null);
        
        // 模拟邮箱不存在
        when(userMapper.selectByEmail("test@example.com")).thenReturn(null);
        
        // 模拟保存成功
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // 执行测试
        Long userId = userService.userRegister(registerRequest);

        // 验证结果
        assertNotNull(userId);
        verify(redisUtil).delete(anyString());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void userRegister_EmptyRequest_ThrowsException() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userRegister(registerRequest);
        });
        
        assertEquals(ErrorCode.PARAMS_ERROR.getErrorCode(), exception.getCode());
        assertEquals("注册信息不完整", exception.getMessage());
    }

    @Test
    void userRegister_UsernameTooShort_ThrowsException() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserName("abc");
        registerRequest.setUserEmail("test@example.com");
        registerRequest.setUserPassword("password123");
        registerRequest.setUserCheckPassword("password123");
        registerRequest.setVerifyCode("123456");
        registerRequest.setEncrypted(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userRegister(registerRequest);
        });
        
        assertEquals(ErrorCode.PARAMS_ERROR.getErrorCode(), exception.getCode());
        assertEquals("用户名长度不能小于4位", exception.getMessage());
    }

    @Test
    void userRegister_PasswordTooShort_ThrowsException() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserName("testuser");
        registerRequest.setUserEmail("test@example.com");
        registerRequest.setUserPassword("1234567");
        registerRequest.setUserCheckPassword("1234567");
        registerRequest.setVerifyCode("123456");
        registerRequest.setEncrypted(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userRegister(registerRequest);
        });
        
        assertEquals(ErrorCode.PARAMS_ERROR.getErrorCode(), exception.getCode());
        assertEquals("密码长度不能小于8位", exception.getMessage());
    }

    @Test
    void userRegister_PasswordMismatch_ThrowsException() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserName("testuser");
        registerRequest.setUserEmail("test@example.com");
        registerRequest.setUserPassword("password123");
        registerRequest.setUserCheckPassword("password456");
        registerRequest.setVerifyCode("123456");
        registerRequest.setEncrypted(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userRegister(registerRequest);
        });
        
        assertEquals(ErrorCode.PARAMS_ERROR.getErrorCode(), exception.getCode());
        assertEquals("两次密码不一致", exception.getMessage());
    }

    @Test
    void userRegister_InvalidEmail_ThrowsException() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserName("testuser");
        registerRequest.setUserEmail("invalid-email");
        registerRequest.setUserPassword("password123");
        registerRequest.setUserCheckPassword("password123");
        registerRequest.setVerifyCode("123456");
        registerRequest.setEncrypted(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userRegister(registerRequest);
        });
        
        assertEquals(ErrorCode.PARAMS_ERROR.getErrorCode(), exception.getCode());
        assertEquals("邮箱格式不正确", exception.getMessage());
    }

    @Test
    void userRegister_WrongVerifyCode_ThrowsException() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserName("testuser");
        registerRequest.setUserEmail("test@example.com");
        registerRequest.setUserPassword("password123");
        registerRequest.setUserCheckPassword("password123");
        registerRequest.setVerifyCode("wrongcode");
        registerRequest.setEncrypted(false);

        when(redisUtil.get(anyString())).thenReturn("123456");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userRegister(registerRequest);
        });
        
        assertEquals(ErrorCode.USER_EMAIL_CODE_ERROR.getErrorCode(), exception.getCode());
        assertEquals("验证码错误或已过期", exception.getMessage());
    }

    @Test
    void userRegister_UsernameExists_ThrowsException() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserName("existinguser");
        registerRequest.setUserEmail("test@example.com");
        registerRequest.setUserPassword("password123");
        registerRequest.setUserCheckPassword("password123");
        registerRequest.setVerifyCode("123456");
        registerRequest.setEncrypted(false);

        when(redisUtil.get(anyString())).thenReturn("123456");
        
        User existingUser = new User();
        existingUser.setUserName("existinguser");
        when(userMapper.selectByUserName("existinguser")).thenReturn(existingUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userRegister(registerRequest);
        });
        
        assertEquals(ErrorCode.USER_ACCOUNT_ALREADY_EXIST.getErrorCode(), exception.getCode());
        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    void userRegister_EmailExists_ThrowsException() {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserName("testuser");
        registerRequest.setUserEmail("existing@example.com");
        registerRequest.setUserPassword("password123");
        registerRequest.setUserCheckPassword("password123");
        registerRequest.setVerifyCode("123456");
        registerRequest.setEncrypted(false);

        when(redisUtil.get(anyString())).thenReturn("123456");
        when(userMapper.selectByUserName("testuser")).thenReturn(null);
        
        User existingUser = new User();
        existingUser.setUserEmail("existing@example.com");
        when(userMapper.selectByEmail("existing@example.com")).thenReturn(existingUser);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.userRegister(registerRequest);
        });
        
        assertEquals(ErrorCode.USER_EMAIL_ALREADY_EXIST.getErrorCode(), exception.getCode());
        assertEquals("邮箱已存在", exception.getMessage());
    }
}