package com.nexterp.platform.auth.infrastructure.security;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 登录尝试数据
 *
 * @author NextERP
 */
@Data
public class LoginAttemptData {
    private int attempts;
    private boolean locked;
    private LocalDateTime lastAttemptTime;

    // Getters and setters 由 Lombok @Data 注解自动生成
}
