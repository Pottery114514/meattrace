package com.meattrace.common.exception;

import com.meattrace.common.api.R;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证异常 → 统一响应的映射规则：
 * 协议层错误同步 HTTP 状态码，业务错误保持 200 + 业务码。
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    @DisplayName("业务异常返回 HTTP 200 + 业务码")
    void bizExceptionKeepsHttp200() {
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn("/admin/province/1");

        ResponseEntity<R<Void>> resp =
                handler.handleBiz(BizException.of(ErrorCode.DATA_REFERENCED, "该省下还有 5 个城市"), request);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getCode()).isEqualTo(2004);
        assertThat(resp.getBody().getMsg()).contains("5 个城市");
    }

    @Test
    @DisplayName("外键冲突翻译成可读提示")
    void foreignKeyViolationTranslated() {
        DataIntegrityViolationException e = new DataIntegrityViolationException(
                "Cannot delete or update a parent row: a foreign key constraint fails");

        ResponseEntity<R<Void>> resp = handler.handleDataIntegrity(e);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getCode()).isEqualTo(ErrorCode.DATA_REFERENCED.getCode());
    }

    @Test
    @DisplayName("唯一键冲突翻译成可读提示")
    void duplicateKeyTranslated() {
        DataIntegrityViolationException e = new DataIntegrityViolationException(
                "Duplicate entry 'MT20261028001' for key 'uk_trace_code'");

        ResponseEntity<R<Void>> resp = handler.handleDataIntegrity(e);

        assertThat(resp.getBody().getCode()).isEqualTo(ErrorCode.DATA_DUPLICATE.getCode());
    }

    @Test
    @DisplayName("兜底异常返回 HTTP 500 且不泄露堆栈")
    void unexpectedReturns500() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/admin/batch");

        ResponseEntity<R<Void>> resp = handler.handleUnexpected(new RuntimeException("db down"), request);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody().getMsg()).isEqualTo("系统内部错误");
        assertThat(resp.getBody().getMsg()).doesNotContain("db down");
    }
}
