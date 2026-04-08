/**
 * 财务管理模块 - 会计核算与财务管理
 * 类型: CLOSED (仅暴露 API，强制模块边界)
 * 依赖: shared, auth, tenant
 */
@ApplicationModule(displayName = "财务管理", allowedDependencies = {"shared", "auth", "tenant"})
package com.nexterp.business.finance;

import org.springframework.modulith.ApplicationModule;
