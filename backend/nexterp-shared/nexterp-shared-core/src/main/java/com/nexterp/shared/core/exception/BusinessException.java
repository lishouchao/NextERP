package com.nexterp.shared.core.exception;

/**
 * 业务异常
 *
 * @author NextERP
 */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super("500", message);
    }

    public BusinessException(String code, String message) {
        super(code, message);
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public BusinessException(String message, Object detail) {
        super("500", message, detail);
    }
}
