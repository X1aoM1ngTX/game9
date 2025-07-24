package com.xm.game9;

import com.xm.game9.utils.RedisUtil;
import jakarta.annotation.Resource;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
@MapperScan("com.xm.game9.mapper")
@EnableScheduling
public class GameNineApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GameNineApplication.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisUtil redisUtil;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Autowired
    private Environment env;

    @Resource
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(GameNineApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("应用启动中... 当前环境: {}", activeProfile);
        log.info("检查Redis连接...");
        redisUtil.checkConnection();
        // 检查MySQL连接
        log.info("检查MySQL连接...");
        try (Connection conn = dataSource.getConnection()) {
            log.info("MySQL连接检查成功");
        } catch (Exception e) {
            log.error("MySQL连接检查失败: {}", e.getMessage(), e);
        }
    }

}
