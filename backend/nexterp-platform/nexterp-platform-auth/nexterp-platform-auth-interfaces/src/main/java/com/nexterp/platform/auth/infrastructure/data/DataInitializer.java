package com.nexterp.platform.auth.infrastructure.data;

import com.nexterp.platform.auth.domain.model.SysUser;
import com.nexterp.platform.auth.domain.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 在开发环境自动创建测试用户
 *
 * @author NextERP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("数据库已有用户数据，跳过初始化");
            return;
        }

        log.info("开始初始化测试用户数据...");

        // 创建管理员用户 - 使用 @Builder 模式
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        SysUser admin = SysUser.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .realName("系统管理员")
                .email("admin@nexterp.com")
                .phone("13800138000")
                .gender(1)
                .status(1)
                .userType(1)
                .tenantId(0L)
                .pwdUpdateTime(now)
                // @CreatedDate 会在持久化时自动设置 createdAt 和 createdBy
                .build();

        userRepository.save(admin);
        log.info("测试用户创建成功: username=admin, password=admin123");
    }
}
