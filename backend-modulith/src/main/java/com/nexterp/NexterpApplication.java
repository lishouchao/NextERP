package com.nexterp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * NextERP 主启动类
 * 基于 Spring Modulith 的模块化单体架构
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class NexterpApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexterpApplication.class, args);
    }
}
