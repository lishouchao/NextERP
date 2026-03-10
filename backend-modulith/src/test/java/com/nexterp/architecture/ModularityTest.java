package com.nexterp.architecture;

import com.nexterp.NexterpApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 模块边界验证测试
 * 确保 Spring Modulith 模块结构正确
 */
class ModularityTest {

    ApplicationModules modules = ApplicationModules.of(NexterpApplication.class);

    @Test
    @DisplayName("验证模块边界完整性")
    void verifyModuleBoundaries() {
        // 验证模块边界、依赖方向、内部封装
        modules.verify();
    }

    @Test
    @DisplayName("打印模块信息")
    void printModuleInfo() {
        modules.forEach(module -> {
            System.out.println("\n========== " + module.getDisplayName() + " ==========");
            System.out.println("逻辑名称: " + module.getName());
            System.out.println("基础包: " + module.getBasePackage().getName());
            System.out.println("\nSpring Beans:");
            module.getBeanReferences().forEach(ref -> {
                String visibility = ref.isApi() ? "API" : "内部";
                System.out.println("  [" + visibility + "] " + ref.getType().getSimpleName());
            });
            System.out.println("\n依赖模块:");
            module.getDependencies().forEach(dep ->
                System.out.println("  -> " + dep.getModuleName())
            );
        });
    }

    @Test
    @DisplayName("生成架构文档")
    void generateDocumentation() {
        new Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml();

        System.out.println("\n架构文档已生成到 target/modulith-docs/");
    }

    @Test
    @DisplayName("验证业务模块之间无直接依赖")
    void verifyBusinessModuleIsolation() {
        var businessModules = modules.stream()
            .filter(m -> m.getName().startsWith("business"))
            .toList();

        for (var module : businessModules) {
            var hasBusinessDependency = module.getDependencies().stream()
                .anyMatch(dep -> dep.getModuleName().startsWith("business"));

            assertThat(hasBusinessDependency)
                .as("业务模块 %s 不应直接依赖其他业务模块", module.getDisplayName())
                .isFalse();
        }
    }
}
