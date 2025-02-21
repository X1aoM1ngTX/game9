package com.xm.gamehub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.boot.CommandLineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Resource;
import com.xm.gamehub.utils.RedisUtil;

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
        log.info("启动中...");
        log.info("检查Redis连接");
        RedisUtil.getInstance().checkConnection();
    }

}
