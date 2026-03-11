package com.nexterp.business.supply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * NextERP 供应链模块启动类
 *
 * @author NextERP
 */
@SpringBootApplication(scanBasePackages = "com.nexterp")
@EnableJpaAuditing
public class NexterpSupplyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexterpSupplyApplication.class, args);
    }
}
