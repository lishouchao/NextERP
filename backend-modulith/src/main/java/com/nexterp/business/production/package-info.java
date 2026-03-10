/**
 * 生产管理模块 - 生产计划与执行
 * 类型: CLOSED
 * 依赖: shared, auth, tenant, workflow
 */
@ApplicationModule(
    
    displayName = "生产管理",
    allowedDependencies = {"shared", "auth", "tenant", "workflow"}
)
package com.nexterp.business.production;

import org.springframework.modulith.ApplicationModule;
