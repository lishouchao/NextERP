package com.nexterp.shared.core.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一返回结果封装
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 请求追踪ID
     */
    private String traceId;

    /**
     * 成功响应
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 响应结果
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
            .code(200)
            .message("success")
            .data(data)
            .build();
    }

    /**
     * 成功响应 (无数据)
     *
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return Result.<T>builder()
            .code(code)
            .message(message)
            .build();
    }

    /**
     * 失败响应 (使用默认错误码)
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }

    /**
     * 业务异常响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> Result<T> businessError(String message) {
        return error(400, message);
    }

    /**
     * 权限异常响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> Result<T> permissionError(String message) {
        return error(403, message);
    }

    /**
     * 认证异常响应
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> Result<T> unauthorized(String message) {
        return error(401, message);
    }

    /**
     * 判断是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }

    /**
     * 判断是否失败
     *
     * @return 是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * 403 Forbidden
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> Result<T> forbidden(String message) {
        return error(403, message);
    }

    /**
     * 404 Not Found
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> Result<T> notFound(String message) {
        return error(404, message);
    }

    /**
     * 400 Bad Request
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> Result<T> badRequest(String message) {
        return error(400, message);
    }

    /**
     * 422 Validation Error
     *
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> Result<T> validationError(String message) {
        return error(422, message);
    }
}
