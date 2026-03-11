package com.nexterp.shared.core.exception;

/**
 * 认证异常
 *
 * @author NextERP
 */
public class AuthenticationException extends BaseException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super("401", message, cause);
    }

    public AuthenticationException(String code, String message) {
        super(code, message);
    }

    public AuthenticationException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
