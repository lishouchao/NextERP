/**
 * 供应链模块 - 采购与库存管理
 * 类型: CLOSED
 * 依赖: shared, auth, tenant, workflow
 */
@ApplicationModule(displayName = "供应链管理", allowedDependencies = {"shared", "auth", "tenant", "workflow"})
package com.nexterp.business.supply;

import org.springframework.modulith.ApplicationModule;
