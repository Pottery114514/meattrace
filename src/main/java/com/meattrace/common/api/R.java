package com.meattrace.common.api;

import com.meattrace.common.exception.ErrorCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * 统一响应体。
 * <p>
 * 设计约定（与 HTTP 状态码的关系）：
 * <ul>
 *   <li>协议层错误（400/401/403/404/405/409/500）—— HTTP 状态码同步返回，便于网关、浏览器、监控正确识别；</li>
 *   <li>业务层错误（1xxx/2xxx/3xxx）—— HTTP 仍为 200，业务码放在 body 的 {@code code} 字段，由前端按业务码提示。</li>
 * </ul>
 * 这样既不违反 HTTP 语义，也不让业务错误污染接口的成功率统计。
 */
@Getter
public class R<T> implements Serializable {

    private final int code;
    private final String msg;
    private final T data;
    private final long timestamp;

    private R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> R<T> ok() {
        return new R<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg(), null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMsg(), data);
    }

    public static <T> R<T> fail(ErrorCode errorCode) {
        return new R<>(errorCode.getCode(), errorCode.getMsg(), null);
    }

    public static <T> R<T> fail(ErrorCode errorCode, String msg) {
        return new R<>(errorCode.getCode(), msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public boolean isSuccess() {
        return this.code == ErrorCode.SUCCESS.getCode();
    }
}
