package com.nexterp.platform.auth.security;

import com.nexterp.shared.security.properties.JwtProperties;
import com.nexterp.shared.security.userdetails.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 工具类
 *
 * @author NextERP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * 生成 JWT 令牌
     *
     * @param userInfo 用户信息
     * @return JWT 令牌
     */
    public String generateToken(UserInfo userInfo) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtProperties.getExpiration() * 1000);

        return Jwts.builder()
            .claims()
            .subject(userInfo.getUsername())
            .addClaims(buildClaims(userInfo))
            .issuedAt(now)
            .expiration(expirationDate)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * 生成刷新令牌
     *
     * @param username 用户名
     * @return 刷新令牌
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtProperties.getRefreshExpiration() * 1000);

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expirationDate)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT 令牌
     * @return 用户名
     */
    public String getUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取声明
     *
     * @param token          JWT 令牌
     * @param claimsResolver 声明解析器
     * @param <T>            声明类型
     * @return 声明值
     */
    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从令牌中获取所有声明
     *
     * @param token JWT 令牌
     * @return 所有声明
     */
    public Claims getAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * 验证令牌是否有效
     *
     * @param token JWT 令牌
     * @return 是否有效
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取令牌过期时间
     *
     * @param token JWT 令牌
     * @return 过期时间
     */
    public Date getExpirationDate(String token) {
        return getClaim(token, Claims::getExpiration);
    }

    /**
     * 检查令牌是否过期
     *
     * @param token JWT 令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDate(token);
        return expiration.before(new Date());
    }

    /**
     * 获取签名密钥
     *
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 构建用户声明
     *
     * @param userInfo 用户信息
     * @return 声明 Map
     */
    private Map<String, Object> buildClaims(UserInfo userInfo) {
        return Map.of(
            "userId", userInfo.getUserId(),
            "tenantId", userInfo.getTenantId(),
            "roles", userInfo.getRoles(),
            "permissions", userInfo.getPermissions()
        );
    }
}
