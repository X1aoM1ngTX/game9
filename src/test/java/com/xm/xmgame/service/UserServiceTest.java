package com.xm.xmgame.service;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.xm.xmgame.model.domain.User;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 用户服务测试
 *
 * @author XMTX8yyds
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

}
