package com.nexterp.platform.report;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * NextERP 报表模块启动类
 *
 * @author NextERP
 */
@SpringBootApplication(scanBasePackages = "com.nexterp")
@EnableJpaAuditing
public class NexterpReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexterpReportApplication.class, args);
    }
}
