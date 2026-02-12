package com.nexterp.platform.auth.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刷新令牌响应
 *
 * @author NextERP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponse {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间(秒)
     */
    private Long expiresIn;
}
