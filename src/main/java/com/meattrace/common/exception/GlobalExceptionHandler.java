package com.meattrace.common.exception;

import com.meattrace.common.api.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器。
 * <p>
 * 目标：Controller / Service 里不再出现 try-catch，所有异常在此收敛为统一格式。
 * 协议层错误同步返回 HTTP 状态码；业务错误返回 200 + 业务码（见 {@link R}）。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBiz(BizException e, HttpServletRequest request) {
        log.warn("业务异常 [{} {}] code={} msg={}", request.getMethod(), request.getRequestURI(), e.getCode(), e.getMessage());
        return build(e.getCode(), e.getMessage());
    }

    // ==================== 参数校验 ====================

    /** @RequestBody + @Valid 校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败: {}", detail);
        return build(ErrorCode.PARAM_INVALID.getCode(), detail);
    }

    /** 表单/查询参数绑定校验失败 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<R<Void>> handleBind(BindException e) {
        String detail = e.getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("；"));
        log.warn("参数绑定失败: {}", detail);
        return build(ErrorCode.PARAM_INVALID.getCode(), detail);
    }

    /** 方法级 @Validated 校验（如 @PathVariable @Min(1)）失败 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<R<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String detail = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("；"));
        log.warn("约束校验失败: {}", detail);
        return build(ErrorCode.PARAM_INVALID.getCode(), detail);
    }

    /** 必填查询参数缺失 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<R<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        return build(ErrorCode.BAD_REQUEST.getCode(), "缺少必填参数：" + e.getParameterName());
    }

    /** 参数类型不匹配，如 /province/abc */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<R<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return build(ErrorCode.BAD_REQUEST.getCode(), "参数 " + e.getName() + " 类型不正确");
    }

    /** 请求体不是合法 JSON */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<R<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return build(ErrorCode.BAD_REQUEST.getCode(), "请求体格式错误，请检查 JSON 是否合法");
    }

    // ==================== 数据完整性 ====================

    /**
     * 唯一键 / 外键冲突。
     * <p>
     * 这是「删除有下属城市的省」这类场景的兜底——不靠"先查再删"，
     * 而是让数据库约束兜底，再翻译成用户能看懂的话。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<R<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        String root = String.valueOf(NestedExceptionUtils.getMostSpecificCause(e).getMessage());
        log.warn("数据完整性冲突: {}", root);

        if (root.contains("Duplicate entry")) {
            return build(ErrorCode.DATA_DUPLICATE.getCode(), "数据已存在，请勿重复提交");
        }
        if (root.contains("foreign key") || root.contains("Cannot delete or update a parent row")) {
            return build(ErrorCode.DATA_REFERENCED.getCode(), "该数据已被其他记录引用，无法删除或修改");
        }
        return build(ErrorCode.CONFLICT.getCode(), "数据约束冲突，操作未生效");
    }

    // ==================== 协议层 ====================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<R<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return build(ErrorCode.METHOD_NOT_ALLOWED.getCode(), "不支持 " + e.getMethod() + " 请求");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<R<Void>> handleNoResource(NoResourceFoundException e) {
        return build(ErrorCode.NOT_FOUND.getCode(), "接口不存在：" + e.getResourcePath());
    }

    // ==================== 兜底 ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleUnexpected(Exception e, HttpServletRequest request) {
        log.error("未捕获异常 [{} {}]", request.getMethod(), request.getRequestURI(), e);
        return build(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMsg());
    }

    // ==================== 工具方法 ====================

    private String formatFieldError(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }

    /**
     * 协议层错误（400~599）同步 HTTP 状态码；业务错误保持 200，由 body 的 code 表达。
     */
    private ResponseEntity<R<Void>> build(int code, String msg) {
        HttpStatus status = (code >= 400 && code < 600)
                ? HttpStatus.valueOf(code)
                : HttpStatus.OK;
        return ResponseEntity.status(status).body(R.fail(code, msg));
    }
}
