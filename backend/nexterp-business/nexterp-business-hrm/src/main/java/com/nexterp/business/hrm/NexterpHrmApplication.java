package com.nexterp.business.hrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * NextERP 人力资源模块启动类
 *
 * @author NextERP
 */
@SpringBootApplication(scanBasePackages = "com.nexterp")
@EnableJpaAuditing
public class NexterpHrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexterpHrmApplication.class, args);
    }
}
