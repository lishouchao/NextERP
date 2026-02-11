package com.nexterp.business.production;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * NextERP 生产模块启动类
 *
 * @author NextERP
 */
@SpringBootApplication(scanBasePackages = "com.nexterp")
@EnableJpaAuditing
public class NexterpProductionApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexterpProductionApplication.class, args);
    }
}
