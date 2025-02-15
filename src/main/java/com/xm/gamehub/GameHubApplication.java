package com.xm.gamehub;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.xm.gamehub.mapper")
@EnableScheduling
public class GameHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameHubApplication.class, args);
    }

}
