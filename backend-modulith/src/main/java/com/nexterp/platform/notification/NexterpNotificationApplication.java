package com.nexterp.platform.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * NextERP 通知模块启动类
 *
 * @author NextERP
 */
@SpringBootApplication(scanBasePackages = "com.nexterp")
@EnableJpaAuditing
@EnableAsync
public class NexterpNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexterpNotificationApplication.class, args);
    }
}
