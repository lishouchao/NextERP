/**
 * 成本控制模块 - 成本管理与盈利分析
 * 类型: CLOSED (仅暴露 API，强制模块边界)
 * 依赖: shared, auth, tenant, finance
 */
@ApplicationModule(displayName = "成本控制", allowedDependencies = {"shared", "auth", "tenant", "finance"})
package com.nexterp.business.controlling;

import org.springframework.modulith.ApplicationModule;
