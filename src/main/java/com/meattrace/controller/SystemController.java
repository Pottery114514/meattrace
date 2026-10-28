package com.meattrace.controller;

import com.meattrace.common.api.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "00. 系统")
@RestController
@RequestMapping("/system")
public class SystemController {

    @Value("${spring.application.name:meattrace}")
    private String appName;

    @Value("${spring.profiles.active:dev}")
    private String profile;

    @Operation(summary = "健康检查", description = "用于验证服务已启动、配置已加载")
    @GetMapping("/ping")
    public R<Map<String, Object>> ping() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("app", appName);
        data.put("profile", profile);
        data.put("time", LocalDateTime.now());
        data.put("status", "UP");
        return R.ok(data);
    }
}
