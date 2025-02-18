package com.xm.gamehub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.boot.CommandLineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;

@SpringBootApplication
@MapperScan("com.xm.gamehub.mapper")
@EnableScheduling
public class GameHubApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GameHubApplication.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(GameHubApplication.class, args);
    }

    @Override
    public void run(String... args) {
        checkRedisConnection();
    }

    private void checkRedisConnection() {
        if (stringRedisTemplate == null) {
            log.error("Redis配置错误: StringRedisTemplate未注入");
            System.exit(1);
            return;
        }

        RedisConnectionFactory connectionFactory = stringRedisTemplate.getConnectionFactory();
        if (connectionFactory == null) {
            log.error("Redis配置错误: RedisConnectionFactory未配置");
            System.exit(1);
            return;
        }

        try {
            connectionFactory.getConnection().close();
            log.info("Redis连接成功");
        } catch (Exception e) {
            log.error("Redis连接失败，请确保Redis服务已启动: {}", e.getMessage());
            System.exit(1);
        }
    }
}
