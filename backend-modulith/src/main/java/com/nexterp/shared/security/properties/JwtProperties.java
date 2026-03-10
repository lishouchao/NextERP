package com.nexterp.shared.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 *
 * @author NextERP
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT 密钥
     */
    private String secret = "nexterp-secret-key-for-jwt-token-generation-must-be-at-least-256-bits";

    /**
     * 访问令牌过期时间 (秒)
     */
    private Long expiration = 7200L;  // 2小时

    /**
     * 刷新令牌过期时间 (秒)
     */
    private Long refreshExpiration = 604800L;  // 7天

    /**
     * 令牌前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * HTTP 头
     */
    private String header = "Authorization";

    /**
     * 是否启用 JWT
     */
    private Boolean enabled = true;
}
