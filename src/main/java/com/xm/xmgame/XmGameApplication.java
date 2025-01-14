package com.xm.xmgame;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.xm.xmgame.mapper")
@EnableScheduling
public class XmGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(XmGameApplication.class, args);
    }

}
