package com.nexterp.platform.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * NextERP 认证授权模块启动类
 *
 * @author NextERP
 */
@SpringBootApplication(scanBasePackages = "com.nexterp")
@EnableJpaAuditing
public class NexterpAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexterpAuthApplication.class, args);
    }
}
