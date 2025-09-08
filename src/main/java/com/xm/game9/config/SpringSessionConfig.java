package com.xm.game9.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;

/**
 * Spring Session配置类
 * 
 * @author X1aoM1ngTX
 */
@Configuration
@EnableRedisIndexedHttpSession(
    maxInactiveIntervalInSeconds = 1478400, // 14天
    redisNamespace = "game9:sessions"
)
public class SpringSessionConfig {
}