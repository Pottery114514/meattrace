package com.meattrace.common.exception;

import lombok.Getter;

/**
 * 业务异常。
 * <p>
 * 与 {@link GlobalExceptionHandler} 配合：抛出后由全局处理器统一转换为 {@code R}，
 * 业务代码中不再出现 try-catch 包裹、也不再手工拼装错误响应。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
    }

    /**
     * 使用自定义提示覆盖枚举默认文案，例如把「数据已被引用」替换成「该省下还有 5 个城市」。
     */
    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.code = errorCode.getCode();
    }

    public static BizException of(ErrorCode errorCode) {
        return new BizException(errorCode);
    }

    public static BizException of(ErrorCode errorCode, String message) {
        return new BizException(errorCode, message);
    }
}
