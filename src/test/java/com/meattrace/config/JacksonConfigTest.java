package com.meattrace.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 锁定日期格式：防止后续有人误删配置后接口悄悄变成 ISO 格式。
 * <p>
 * 不用 {@code @JsonTest} 切片——切片不会加载普通 {@code @Configuration} 类，
 * 定制器不会生效，测试会假阴性。这里直接构造 builder 验证定制器行为。
 */
class JacksonConfigTest {

    private final ObjectMapper objectMapper = buildMapper();

    private static ObjectMapper buildMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonConfig().jsr310Customizer().customize(builder);
        return builder.build();
    }

    @Test
    @DisplayName("LocalDateTime 序列化为 yyyy-MM-dd HH:mm:ss")
    void localDateTimeFormat() throws Exception {
        String json = objectMapper.writeValueAsString(LocalDateTime.of(2026, 10, 28, 17, 49, 34));
        assertThat(json).isEqualTo("\"2026-10-28 17:49:34\"");
    }

    @Test
    @DisplayName("LocalDate 序列化为 yyyy-MM-dd")
    void localDateFormat() throws Exception {
        String json = objectMapper.writeValueAsString(LocalDate.of(2026, 10, 28));
        assertThat(json).isEqualTo("\"2026-10-28\"");
    }

    @Test
    @DisplayName("日期字符串可反序列化回 LocalDateTime")
    void localDateTimeDeserialize() throws Exception {
        LocalDateTime value = objectMapper.readValue("\"2026-10-28 17:49:34\"", LocalDateTime.class);
        assertThat(value).isEqualTo(LocalDateTime.of(2026, 10, 28, 17, 49, 34));
    }
}
