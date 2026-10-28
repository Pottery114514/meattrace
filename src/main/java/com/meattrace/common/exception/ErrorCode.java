package com.meattrace.common.exception;

/**
 * 统一错误码。
 * <p>
 * 编码规范：
 * <ul>
 *   <li>1xxx —— 认证与授权</li>
 *   <li>2xxx —— 通用数据操作</li>
 *   <li>3xxx —— 业务规则（批次链 / 溯源 / 召回）</li>
 *   <li>400~599 —— 与 HTTP 状态码语义对齐</li>
 * </ul>
 */
public enum ErrorCode {

    SUCCESS(200, "success"),

    // ===== 协议层：与 HTTP 状态码对齐 =====
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "系统内部错误"),

    // ===== 1xxx 认证与授权 =====
    LOGIN_FAILED(1001, "用户名或密码错误"),
    TOKEN_INVALID(1002, "登录凭证无效"),
    TOKEN_EXPIRED(1003, "登录已过期，请重新登录"),
    ACCOUNT_DISABLED(1004, "账号已停用"),

    // ===== 2xxx 通用数据操作 =====
    PARAM_INVALID(2001, "参数校验失败"),
    DATA_NOT_FOUND(2002, "数据不存在"),
    DATA_DUPLICATE(2003, "数据已存在"),
    DATA_REFERENCED(2004, "数据已被引用，无法操作"),

    // ===== 3xxx 业务规则 =====
    ENT_TYPE_MISMATCH(3001, "企业类型与该业务环节不匹配"),
    UPSTREAM_NOT_FOUND(3002, "上游批次不存在或状态不可用"),
    BATCH_STATUS_ILLEGAL(3003, "批次当前状态不允许该操作"),
    TRACE_CODE_NOT_FOUND(3004, "溯源码不存在"),
    TRACE_CODE_DUPLICATE(3005, "溯源码生成冲突，请重试"),
    RECALL_CONFLICT(3006, "该批次已处于召回状态");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
