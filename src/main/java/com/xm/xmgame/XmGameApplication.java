package com.xm.xmgame;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xm.xmgame.mapper")
public class XmGameApplication {

    public static void main(String[] args) {
        SpringApplication.run(XmGameApplication.class, args);
    }

}
