package com.nexterp.business.finance.config;

import com.nexterp.business.finance.domain.repository.*;
import com.nexterp.business.finance.application.service.*;
import com.nexterp.platform.auth.domain.repository.*;
import com.nexterp.platform.auth.application.service.*;
import com.nexterp.shared.data.repository.TenantAwareRepository;
import com.nexterp.shared.web.interceptor.TenantInterceptor;
import com.nexterp.shared.web.resolver.TenantResolver;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

/**
 * 财务模块测试配置
 *
 * @author NextERP
 */
@TestConfiguration
@ComponentScan(
        basePackages = {
                "com.nexterp.business.finance",
                "com.nexterp.platform",
                "com.nexterp.shared"
        },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Controller")
        }
)
@EnableJpaRepositories(
        basePackages = {
                "com.nexterp.business.finance.domain.repository",
                "com.nexterp.platform.auth.domain.repository",
                "com.nexterp.shared"
        }
)
@EnableJpaAuditing
public class TestConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/**");
    }

    @Bean
    public TenantResolver tenantResolver() {
        return new TenantResolver();
    }

    @Bean
    public TenantInterceptor tenantInterceptor() {
        return new TenantInterceptor();
    }
}
