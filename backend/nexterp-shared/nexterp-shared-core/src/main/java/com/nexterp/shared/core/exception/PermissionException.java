package com.nexterp.shared.core.exception;

/**
 * 权限异常
 *
 * @author NextERP
 */
public class PermissionException extends BaseException {

    public PermissionException(String message) {
        super("403", message);
    }

    public PermissionException(String code, String message) {
        super(code, message);
    }
}
