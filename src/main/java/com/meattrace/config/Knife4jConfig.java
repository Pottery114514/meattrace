package com.meattrace.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档配置，访问地址：http://localhost:8080/api/doc.html
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI meatTraceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MeatTrace 肉类全链路溯源平台 API")
                        .description("""
                                四段式批次溯源系统接口文档。

                                模块划分：
                                - /admin/**   管理端（需鉴权）
                                - /public/**  消费者端（免鉴权，扫码溯源）

                                响应约定：code=200 成功；1xxx 认证类；2xxx 通用数据类；3xxx 业务规则类。
                                """)
                        .version("0.1.0")
                        .contact(new Contact().name("MeatTrace"))
                        .license(new License().name("MIT")));
    }
}
