/**
 * 租户管理模块 - 多租户支持
 * 类型: OPEN
 * 依赖: shared
 */
@ApplicationModule(
    
    displayName = "多租户",
    allowedDependencies = {"shared"}
)
package com.nexterp.platform.tenant;

import org.springframework.modulith.ApplicationModule;
