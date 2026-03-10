/**
 * 销售管理模块 - 客户与订单管理
 * 类型: CLOSED
 * 依赖: shared, auth, tenant
 */
@ApplicationModule(
    
    displayName = "销售管理",
    allowedDependencies = {"shared", "auth", "tenant"}
)
package com.nexterp.business.sales;

import org.springframework.modulith.ApplicationModule;
