package com.nexterp.business.sales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * NextERP 销售模块启动类
 *
 * @author NextERP
 */
@SpringBootApplication(scanBasePackages = "com.nexterp")
@EnableJpaAuditing
public class NexterpSalesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexterpSalesApplication.class, args);
    }
}
