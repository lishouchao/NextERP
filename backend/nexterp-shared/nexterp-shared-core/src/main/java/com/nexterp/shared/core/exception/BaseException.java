package com.nexterp.shared.core.exception;

import com.nexterp.shared.core.result.Result;
import lombok.Getter;

/**
 * 基础异常类
 *
 * @author NextERP
 */
@Getter
public class BaseException extends RuntimeException {

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 错误详情
     */
    private final transient Object detail;

    public BaseException(String message) {
        this("500", message, null);
    }

    public BaseException(String code, String message) {
        this(code, message, null);
    }

    public BaseException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.detail = null;
    }

    public BaseException(String code, String message, Object detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
    }

    public BaseException(String code, String message, Throwable cause, Object detail) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.detail = detail;
    }

    /**
     * 转换为结果对象
     *
     * @return 结果对象
     */
    public Result<Void> toResult() {
        return Result.<Void>builder()
            .code(Integer.parseInt(this.code))
            .message(this.message)
            .build();
    }

    /**
     * 创建业务异常
     *
     * @param message 错误消息
     * @return 业务异常
     */
    public static BusinessException of(String message) {
        return new BusinessException(message);
    }

    /**
     * 创建认证异常
     *
     * @param message 错误消息
     * @return 认证异常
     */
    public static AuthenticationException auth(String message) {
        return new AuthenticationException(message);
    }

    /**
     * 创建权限异常
     *
     * @param message 错误消息
     * @return 权限异常
     */
    public static PermissionException permission(String message) {
        return new PermissionException(message);
    }
}
