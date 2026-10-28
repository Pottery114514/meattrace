package com.meattrace;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.meattrace.mapper")
public class MeatTraceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeatTraceApplication.class, args);
    }
}
