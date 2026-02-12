package com.nexterp.platform.auth.interfaces.config;

import com.nexterp.shared.security.properties.JwtProperties;
import com.nexterp.platform.auth.infrastructure.security.JwtAuthenticationFilter;
import com.nexterp.platform.auth.infrastructure.security.JwtSecurityContextRepository;
import com.nexterp.platform.auth.infrastructure.security.JwtTokenProvider;
import com.nexterp.platform.auth.infrastructure.handlers.JwtAuthenticationEntryPoint;
import com.nexterp.platform.auth.infrastructure.handlers.JwtAccessDeniedHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 配置类
 *
 * @author NextERP
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProperties jwtProperties;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtSecurityContextRepository jwtSecurityContextRepository;

    private static final String[] AUTH_WHITELIST = {
        // Swagger UI
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**",
        // Actuator
        "/actuator/**",
        // Auth API
        "/api/v1/auth/**",
        // Error
        "/error"
    };

    /**
     * 配置密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 配置 CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 配置安全过滤链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF
            .csrf(AbstractHttpConfigurer::disable)

            // 配置 CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 配置会话管理
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 配置安全上下文仓储
            .securityContext(securityContext -> securityContext
                .securityContextRepository(jwtSecurityContextRepository))

            // 配置异常处理
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler))

            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PathRequest.toH2Console()).permitAll()
                .requestMatchers(AUTH_WHITELIST).permitAll()
                .anyRequest().authenticated()
            )

            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * JWT 认证过滤器 Bean
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtProperties, jwtTokenProvider);
    }
}
