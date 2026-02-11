package com.nexterp;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * NextERP 架构测试
 * 使用 ArchUnit 确保代码遵循架构规则
 */
class ArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
        .importPackages("com.nexterp");

    // ========== 平台层规则 ==========

    @Test
    void platform_module_should_only_depend_on_shared() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..platform..")
            .should().dependOnClassesThat()
            .resideInAPackage("..business..");

        rule.check(classes);
    }

    // ========== 业务层规则 ==========

    @Test
    void business_modules_should_be_isolated() {
        ArchRule rule = slices()
            .matching("com.nexterp.business.(*)..")
            .should().notDependOnEachOther();

        rule.check(classes);
    }

    @Test
    void business_modules_should_only_access_platform_interfaces() {
        ArchRule rule = classes()
            .that().resideInAPackage("..business..")
            .should().onlyAccessClassesThat()
            .resideInAnyPackage(
                "..business..",
                "..platform..api..",
                "..shared..",
                "java..",
                "org.springframework..",
                "lombok.."
            );

        rule.check(classes);
    }

    // ========== 共享层规则 ==========

    @Test
    void shared_modules_should_not_depend_on_business() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..shared..")
            .should().dependOnClassesThat()
            .resideInAPackage("..business..");

        rule.check(classes);
    }

    // ========== 控制器规则 ==========

    @Test
    void controllers_should_only_reside_in_designated_packages() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(RestController.class)
            .or().areAnnotatedWith(Controller.class)
            .should().resideInAnyPackage(
                "..api..controller..",
                "..platform..controller..",
                "..business..api..controller.."
            );

        rule.check(classes);
    }

    // ========== 服务规则 ==========

    @Test
    void services_should_only_be_annotated_in_designated_packages() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(Service.class)
            .should().resideInAnyPackage(
                "..application..service..",
                "..domain..service..",
                "..infrastructure..service.."
            );

        rule.check(classes);
    }

    // ========== 仓储规则 ==========

    @Test
    void repositories_should_only_be_annotated_in_designated_packages() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(Repository.class)
            .should().resideInAnyPackage(
                "..infrastructure..repository.."
            );

        rule.check(classes);
    }

    // ========== 命名约定 ==========

    @Test
    void domain_services_should_have_suffixed_names() {
        ArchRule rule = classes()
            .that().resideInAPackage("..domain..service..")
            .and().areAnnotatedWith(Service.class)
            .should().haveSimpleNameEndingWith("DomainService");

        rule.check(classes);
    }

    @Test
    void application_services_should_have_suffixed_names() {
        ArchRule rule = classes()
            .that().resideInAPackage("..application..service..")
            .and().areAnnotatedWith(Service.class)
            .should().haveSimpleNameEndingWith("Service");

        rule.check(classes);
    }

    @Test
    void facades_should_have_suffixed_names() {
        ArchRule rule = classes()
            .that().resideInAPackage("..api..facade..")
            .should().haveSimpleNameEndingWith("Facade");

        rule.check(classes);
    }
}
