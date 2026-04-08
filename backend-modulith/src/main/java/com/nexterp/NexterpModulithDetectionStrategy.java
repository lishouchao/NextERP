package com.nexterp;

import java.util.stream.Stream;

import org.springframework.modulith.core.ApplicationModuleDetectionStrategy;
import org.springframework.modulith.core.JavaPackage;

/**
 * 自定义 Spring Modulith 模块检测策略
 * 支持嵌套模块结构：business.finance, business.hrm, platform.auth 等
 */
public class NexterpModulithDetectionStrategy implements ApplicationModuleDetectionStrategy {

    @Override
    public Stream<JavaPackage> getModuleBasePackages(JavaPackage basePackage) {
        return basePackage.getDirectSubPackages().stream()
            .flatMap(parent -> {
                String parentName = parent.getName();

                // shared 模块是顶层模块，直接返回
                if (parentName.endsWith(".shared")) {
                    return Stream.of(parent);
                }

                // 对于 business 和 platform 包，返回它们的子包作为模块
                if (parentName.endsWith(".business") || parentName.endsWith(".platform")) {
                    return parent.getDirectSubPackages().stream();
                }

                // 其他情况，返回空流（不作为模块）
                return Stream.empty();
            });
    }
}
