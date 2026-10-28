package com.meattrace.common.api;

import com.meattrace.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RTest {

    @Test
    @DisplayName("ok() 返回 200 且 data 为空")
    void okWithoutData() {
        R<Void> r = R.ok();
        assertThat(r.getCode()).isEqualTo(200);
        assertThat(r.getMsg()).isEqualTo("success");
        assertThat(r.getData()).isNull();
        assertThat(r.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("ok(data) 携带业务数据")
    void okWithData() {
        R<String> r = R.ok("trace-001");
        assertThat(r.getCode()).isEqualTo(200);
        assertThat(r.getData()).isEqualTo("trace-001");
    }

    @Test
    @DisplayName("fail(ErrorCode) 使用枚举默认文案")
    void failWithErrorCode() {
        R<Void> r = R.fail(ErrorCode.DATA_NOT_FOUND);
        assertThat(r.getCode()).isEqualTo(2002);
        assertThat(r.getMsg()).isEqualTo("数据不存在");
        assertThat(r.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("fail(ErrorCode, msg) 可覆盖默认文案")
    void failWithCustomMessage() {
        R<Void> r = R.fail(ErrorCode.DATA_REFERENCED, "该省下还有 5 个城市，无法删除");
        assertThat(r.getCode()).isEqualTo(2004);
        assertThat(r.getMsg()).contains("5 个城市");
    }
}
