package com.xm.game9;

import com.xm.game9.utils.RedisUtil;
import jakarta.annotation.Resource;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.xm.game9.mapper")
@EnableScheduling
public class GameNineApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GameNineApplication.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(GameNineApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String activeProfile = System.getProperty("spring.profiles.active", "dev");
        log.info("应用启动中... 当前环境: {}", activeProfile);
        log.info("检查Redis连接...");
        RedisUtil.getInstance().checkConnection();
    }

}
