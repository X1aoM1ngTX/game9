package com.xm.game9.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 配置类
 * 
 * @author X1aoM1ngTX
 * @createDate 2024-11-05
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Game9 API")
                        .description("游戏商城后端接口文档")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("X1aoM1ngTX")
                                .email("1062829664@qq.com")
                                .url("https://github.com/X1aoM1ngTX"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
} 