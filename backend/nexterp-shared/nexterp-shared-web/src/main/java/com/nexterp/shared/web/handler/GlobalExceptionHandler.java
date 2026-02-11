package com.nexterp.shared.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexterp.shared.core.exception.BusinessException;
import com.nexterp.shared.core.exception.AuthenticationException;
import com.nexterp.shared.core.exception.AuthorizationException;
import com.nexterp.shared.core.exception.ResourceNotFoundException;
import com.nexterp.shared.core.exception.ValidationException;
import com.nexterp.shared.core.result.Result;
import com.nexterp.shared.core.result.ErrorResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException as SpringAuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author NextERP
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ObjectMapper objectMapper;

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("Business exception: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.businessError(e.getMessage());
    }

    /**
     * 认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.warn("Authentication exception: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.unauthorized(e.getMessage());
    }

    /**
     * 授权异常
     */
    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAuthorizationException(AuthorizationException e, HttpServletRequest request) {
        log.warn("Authorization exception: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.forbidden(e.getMessage());
    }

    /**
     * 资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {
        log.warn("Resource not found: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.notFound(e.getMessage());
    }

    /**
     * 验证异常
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(ValidationException e, HttpServletRequest request) {
        log.warn("Validation exception: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.validationError(e.getMessage());
    }

    /**
     * 方法参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("Method argument not valid: {} - {}", request.getRequestURI(), e.getMessage());

        Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null
                                ? fieldError.getDefaultMessage()
                                : "验证失败",
                        (existing, replacement) -> existing + "; " + replacement
                ));

        return Result.<Map<String, String>>builder()
                .code(400)
                .message("参数验证失败")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        log.warn("Constraint violation: {} - {}", request.getRequestURI(), e.getMessage());

        Map<String, String> errors = e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> {
                            String path = violation.getPropertyPath().toString();
                            return path.substring(path.lastIndexOf('.') + 1);
                        },
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing + "; " + replacement
                ));

        return Result.<Map<String, String>>builder()
                .code(400)
                .message("参数验证失败")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("Argument type mismatch: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.badRequest("参数类型不匹配: " + e.getName());
    }

    /**
     * Spring Security认证异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        log.warn("Bad credentials: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.unauthorized("用户名或密码错误");
    }

    /**
     * Spring Security授权异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access denied: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.forbidden("无权限访问");
    }

    /**
     * Spring Security认证异常
     */
    @ExceptionHandler(SpringAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleSpringAuthenticationException(
            SpringAuthenticationException e, HttpServletRequest request) {
        log.warn("Spring authentication exception: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.unauthorized("认证失败");
    }

    /**
     * 404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("No handler found: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.notFound("请求的资源不存在");
    }

    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Illegal argument: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.badRequest(e.getMessage());
    }

    /**
     * 非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleIllegalStateException(IllegalStateException e, HttpServletRequest request) {
        log.error("Illegal state: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error("系统状态异常");
    }

    /**
     * 空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("Null pointer: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error("系统内部错误");
    }

    /**
     * 运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("Runtime exception: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error("系统运行时异常");
    }

    /**
     * 通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("Exception: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error("系统内部错误");
    }
}
