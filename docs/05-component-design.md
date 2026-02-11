# NextERP 组件设计规范

## 文档概述

本文档详细定义 NextERP 系统各模块的组件设计，包括组件职责、能力定义和接口规范。

**设计原则：**
- 高内聚、低耦合
- 接口隔离（API/SPI 分层）
- 事件驱动解耦
- 依赖倒置（依赖抽象而非实现）

---

## 组件架构分层

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        NextERP 组件架构                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                    表现层 (Presentation)                           │  │
│  │  - Controller (REST API)                                          │  │
│  │  - Facade (门面接口)                                              │  │
│  │  - DTO (数据传输对象)                                             │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                  │                                      │
│                                  ▼                                      │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                    应用层 (Application)                            │  │
│  │  - ApplicationService (应用服务)                                  │  │
│  │  - EventHandler (事件处理器)                                      │  │
│  │  - Command/Query (命令/查询)                                      │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                  │                                      │
│                                  ▼                                      │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                    领域层 (Domain)                                │  │
│  │  - Entity (实体)                                                  │  │
│  │  - ValueObject (值对象)                                           │  │
│  │  - DomainService (领域服务)                                       │  │
│  │  - DomainEvent (领域事件)                                         │  │
│  │  - Repository (仓储接口)                                          │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                  │                                      │
│                                  ▼                                      │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                    基础设施层 (Infrastructure)                     │  │
│  │  - RepositoryImpl (仓储实现)                                      │  │
│  │  - Mapper (数据映射)                                              │  │
│  │  - ExternalService (外部服务)                                     │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                    跨层组件 (Cross-Cutting)                       │  │
│  │  - Common (公共工具)                                              │  │
│  │  - Security (安全)                                                │  │
│  │  - Cache (缓存)                                                   │  │
│  │  - Event (事件总线)                                               │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 平台层组件设计

### 1. 认证授权模块 (Platform-Auth)

#### 1.1 组件结构

```
nexterp-platform-auth/
├── auth-api/                          # 对外接口
│   ├── dto/
│   │   ├── LoginRequestDTO.java
│   │   ├── LoginResponseDTO.java
│   │   ├── TokenRefreshRequestDTO.java
│   │   └── UserinfoDTO.java
│   ├── vo/
│   │   ├── MenuTreeVO.java
│   │   ├── PermissionVO.java
│   │   └── RoleVO.java
│   └── facade/
│       └── AuthFacade.java            # 认证门面
│
├── auth-spi/                          # 扩展接口
│   ├── AuthProvider.java              # 认证提供者接口
│   ├── UserDetailService.java         # 用户详情服务接口
│   └── TokenStore.java                # Token 存储接口
│
├── auth-domain/                       # 领域模型
│   ├── model/
│   │   ├── User.java                  # 用户实体
│   │   ├── Role.java                  # 角色实体
│   │   ├── Permission.java            # 权限实体
│   │   ├── Menu.java                  # 菜单实体
│   │   └── Token.java                 # Token 实体
│   ├── event/
│   │   ├── UserLoggedInEvent.java     # 用户登录事件
│   │   ├── UserLoggedOutEvent.java    # 用户登出事件
│   │   └── PermissionChangedEvent.java # 权限变更事件
│   ├── service/
│   │   ├── PasswordService.java       # 密码服务
│   │   ├── TokenService.java          # Token 服务
│   │   └── PermissionService.java     # 权限服务
│   └── repository/
│       ├── UserRepository.java
│       ├── RoleRepository.java
│       └── PermissionRepository.java
│
├── auth-application/                  # 应用服务
│   ├── service/
│   │   ├── AuthApplicationService.java
│   │   ├── UserApplicationService.java
│   │   └── RoleApplicationService.java
│   └── listener/
│       ├── LoginListener.java         # 登录监听器
│       └── PermissionChangeListener.java
│
└── auth-infrastructure/               # 基础设施
    ├── repository/
    │   ├── UserRepositoryImpl.java
    │   └── RoleRepositoryImpl.java
    ├── security/
    │   ├── JWTTokenProvider.java      # JWT Token 提供者
    │   ├── SecurityConfig.java        # 安全配置
    │   └── CustomUserDetailsService.java
    └── config/
        └── AuthModuleConfig.java
```

#### 1.2 组件能力定义

##### AuthFacade (认证门面)

```java
package com.nexterp.platform.auth.api.facade;

import com.nexterp.platform.auth.api.dto.*;
import com.nexterp.platform.auth.api.vo.*;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 认证授权门面接口
 * 提供统一的认证、授权、用户管理能力
 */
@Tag(name = "认证授权", description = "认证授权相关接口")
public interface AuthFacade {

    // ==================== 认证能力 ====================

    /**
     * 用户登录
     * @param request 登录请求 (用户名/密码, 手机号/验证码, 邮箱/密码)
     * @return 登录响应 (包含 access_token, refresh_token)
     */
    @Operation(summary = "用户登录")
    Result<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request);

    /**
     * Token 刷新
     * @param request 刷新请求 (refresh_token)
     * @return 新的登录响应
     */
    @Operation(summary = "刷新Token")
    Result<LoginResponseDTO> refreshToken(@Valid @RequestBody TokenRefreshRequestDTO request);

    /**
     * 用户登出
     * @param token 当前访问 token
     */
    @Operation(summary = "用户登出")
    Result<Void> logout(@RequestHeader("Authorization") String token);

    /**
     * Token 验证
     * @param token 待验证 token
     * @return 是否有效
     */
    @Operation(summary = "验证Token")
    Result<Boolean> validateToken(@RequestParam String token);

    // ==================== 用户信息能力 ====================

    /**
     * 获取当前用户信息
     * @return 用户详情
     */
    @Operation(summary = "获取当前用户信息")
    Result<UserinfoDTO> getCurrentUser();

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param dto 更新数据
     */
    @Operation(summary = "更新用户信息")
    Result<Void> updateUser(@PathVariable Long userId, @Valid @RequestBody UserUpdateDTO dto);

    /**
     * 修改密码
     * @param dto 修改密码请求
     */
    @Operation(summary = "修改密码")
    Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto);

    // ==================== 权限能力 ====================

    /**
     * 获取用户权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    @Operation(summary = "获取用户权限")
    Result<List<PermissionVO>> getUserPermissions(@PathVariable Long userId);

    /**
     * 获取用户菜单树
     * @param userId 用户ID
     * @return 菜单树
     */
    @Operation(summary = "获取用户菜单")
    Result<List<MenuTreeVO>> getUserMenus(@PathVariable Long userId);

    /**
     * 检查权限
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否拥有权限
     */
    @Operation(summary = "检查权限")
    Result<Boolean> checkPermission(@PathVariable Long userId, @RequestParam String permission);

    // ==================== 角色能力 ====================

    /**
     * 创建角色
     * @param dto 角色创建请求
     * @return 角色ID
     */
    @Operation(summary = "创建角色")
    Result<Long> createRole(@Valid @RequestBody RoleCreateDTO dto);

    /**
     * 分配角色权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    @Operation(summary = "分配角色权限")
    Result<Void> assignRolePermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds);

    /**
     * 分配用户角色
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    @Operation(summary = "分配用户角色")
    Result<Void> assignUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds);
}
```

##### AuthApplicationService (认证应用服务)

```java
package com.nexterp.platform.auth.application.service;

import com.nexterp.platform.auth.api.dto.*;
import com.nexterp.platform.auth.domain.model.*;
import com.nexterp.platform.auth.domain.event.*;
import com.nexterp.platform.auth.domain.service.*;
import com.nexterp.shared.security.TenantContext;
import com.nexterp.shared.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证应用服务
 * 负责认证流程编排、Token 管理
 */
@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 登录处理
     * 1. 验证用户凭证
     * 2. 生成 Token
     * 3. 更新登录信息
     * 4. 发布登录事件
     */
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. 认证
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        // 2. 获取用户
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new NotFoundException("用户不存在"));

        // 3. 验证租户
        if (!user.getTenantId().equals(TenantContext.getTenantId())) {
            throw new BusinessException("租户不匹配");
        }

        // 4. 生成 Token
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        // 5. 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        userRepository.save(user);

        // 6. 发布登录事件
        eventPublisher.publishEvent(new UserLoggedInEvent(
            user.getId(),
            user.getUsername(),
            UserContext.getClientIp(),
            LocalDateTime.now()
        ));

        return LoginResponseDTO.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(tokenService.getAccessTokenExpiration())
            .userInfo(UserinfoDTO.from(user))
            .build();
    }

    /**
     * Token 刷新
     */
    @Transactional
    public LoginResponseDTO refreshToken(TokenRefreshRequestDTO request) {
        // 1. 验证 refresh token
        String username = tokenService.validateRefreshToken(request.getRefreshToken());

        // 2. 获取用户
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("用户不存在"));

        // 3. 生成新 Token
        String accessToken = tokenService.generateAccessToken(user);
        String newRefreshToken = tokenService.generateRefreshToken(user);

        return LoginResponseDTO.builder()
            .accessToken(accessToken)
            .refreshToken(newRefreshToken)
            .tokenType("Bearer")
            .expiresIn(tokenService.getAccessTokenExpiration())
            .build();
    }

    /**
     * 登出处理
     */
    @Transactional
    public void logout(String token) {
        // 1. 使 Token 失效
        tokenService.revokeToken(token);

        // 2. 获取当前用户
        Long userId = UserContext.getUserId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("用户不存在"));

        // 3. 发布登出事件
        eventPublisher.publishEvent(new UserLoggedOutEvent(
            userId,
            user.getUsername(),
            LocalDateTime.now()
        ));
    }

    /**
     * 修改密码
     */
    @Transactional
    public void changePassword(ChangePasswordDTO dto) {
        Long userId = UserContext.getUserId();
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("用户不存在"));

        // 1. 验证旧密码
        if (!passwordService.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        // 2. 更新密码
        user.setPassword(passwordService.encode(dto.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        // 3. 使所有 Token 失效
        tokenService.revokeAllUserTokens(userId);
    }
}
```

#### 1.3 事件接口

```java
// ==================== 领域事件 ====================

/**
 * 用户登录事件
 */
public record UserLoggedInEvent(
    Long userId,
    String username,
    String clientIp,
    LocalDateTime loginTime
) {}

/**
 * 用户登出事件
 */
public record UserLoggedOutEvent(
    Long userId,
    String username,
    LocalDateTime logoutTime
) {}

/**
 * 权限变更事件
 * 当用户角色或角色权限变更时发布
 */
public record PermissionChangedEvent(
    Long userId,
    Set<Long> roleIds,
    LocalDateTime changeTime
) {}
```

---

### 2. 多租户模块 (Platform-Tenant)

#### 2.1 组件结构

```
nexterp-platform-tenant/
├── tenant-api/
│   ├── dto/
│   │   ├── TenantCreateDTO.java
│   │   ├── TenantUpdateDTO.java
│   │   └── TenantQueryDTO.java
│   ├── vo/
│   │   ├── TenantVO.java
│   │   └── TenantStatisticsVO.java
│   └── facade/
│       └── TenantFacade.java
│
├── tenant-domain/
│   ├── model/
│   │   ├── Tenant.java               # 租户实体
│   │   ├── TenantConfig.java         # 租户配置
│   │   └── TenantPackage.java        # 租户套餐
│   ├── service/
│   │   ├── TenantService.java
│   │   └── TenantQuotaService.java   # 配额管理
│   └── event/
│       ├── TenantCreatedEvent.java
│       ├── TenantExpiredEvent.java
│       └── TenantUpgradedEvent.java
│
└── tenant-application/
    └── listener/
        └── TenantCreatedListener.java
```

#### 2.2 组件能力定义

##### TenantFacade (租户门面)

```java
package com.nexterp.platform.tenant.api.facade;

import com.nexterp.platform.tenant.api.dto.*;
import com.nexterp.platform.tenant.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * 租户管理门面接口
 */
public interface TenantFacade {

    /**
     * 创建租户
     */
    @Operation(summary = "创建租户")
    Result<Long> createTenant(@Valid @RequestBody TenantCreateDTO dto);

    /**
     * 更新租户信息
     */
    @Operation(summary = "更新租户")
    Result<Void> updateTenant(@PathVariable Long id, @Valid @RequestBody TenantUpdateDTO dto);

    /**
     * 查询租户详情
     */
    @Operation(summary = "查询租户详情")
    Result<TenantVO> getTenant(@PathVariable Long id);

    /**
     * 分页查询租户
     */
    @Operation(summary = "分页查询租户")
    Result<PageResult<TenantVO>> pageTenants(@Valid TenantQueryDTO query);

    /**
     * 启用/禁用租户
     */
    @Operation(summary = "启用/禁用租户")
    Result<Void> setTenantStatus(@PathVariable Long id, @RequestParam Integer status);

    /**
     * 升级租户套餐
     */
    @Operation(summary = "升级租户套餐")
    Result<Void> upgradePackage(@PathVariable Long id, @RequestParam Integer packageLevel);

    /**
     * 获取租户统计信息
     */
    @Operation(summary = "获取租户统计")
    Result<TenantStatisticsVO> getStatistics(@PathVariable Long id);

    /**
     * 检查租户配额
     */
    @Operation(summary = "检查租户配额")
    Result<Boolean> checkQuota(@PathVariable Long tenantId, @RequestParam String quotaType);

    /**
     * 重置租户管理员密码
     */
    @Operation(summary = "重置管理员密码")
    Result<String> resetAdminPassword(@PathVariable Long tenantId);
}
```

---

### 3. 工作流模块 (Platform-Workflow)

#### 3.1 组件结构

```
nexterp-platform-workflow/
├── workflow-api/
│   ├── dto/
│   │   ├── ProcessDefinitionDTO.java
│   │   ├── ProcessInstanceDTO.java
│   │   ├── TaskDTO.java
│   │   └── TaskCompleteDTO.java
│   ├── vo/
│   │   ├── ProcessDefinitionVO.java
│   │   ├── ProcessInstanceVO.java
│   │   ├── TaskVO.java
│   │   └── ProcessHistoryVO.java
│   └── facade/
│       └── WorkflowFacade.java
│
├── workflow-domain/
│   ├── model/
│   │   ├── ProcessDefinition.java
│   │   ├── ProcessInstance.java
│   │   ├── Task.java
│   │   └── TaskVariable.java
│   ├── service/
│   │   ├── ProcessService.java
│   │   ├── TaskService.java
│   │   └── FormService.java
│   └── event/
│       ├── ProcessStartedEvent.java
│       ├── ProcessCompletedEvent.java
│       ├── TaskCreatedEvent.java
│       └── TaskCompletedEvent.java
│
└── workflow-infrastructure/
    └── integration/
        └── FlowableIntegration.java  # Flowable 集成
```

#### 3.2 组件能力定义

##### WorkflowFacade (工作流门面)

```java
package com.nexterp.platform.workflow.api.facade;

import com.nexterp.platform.workflow.api.dto.*;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 工作流门面接口
 */
public interface WorkflowFacade {

    // ==================== 流程定义能力 ====================

    /**
     * 部署流程定义
     * @param bpmnXml BPMN XML 内容
     * @return 流程定义ID
     */
    @Operation(summary = "部署流程")
    Result<String> deployProcess(@RequestBody String bpmnXml);

    /**
     * 查询流程定义列表
     */
    @Operation(summary = "查询流程定义")
    Result<List<ProcessDefinitionVO>> listProcessDefinitions();

    /**
     * 获取流程定义详情
     */
    @Operation(summary = "流程定义详情")
    Result<ProcessDefinitionVO> getProcessDefinition(@PathVariable String processDefinitionId);

    /**
     * 获取流程图
     */
    @Operation(summary = "获取流程图")
    Result<byte[]> getProcessDiagram(@PathVariable String processDefinitionId);

    // ==================== 流程实例能力 ====================

    /**
     * 启动流程实例
     * @param request 启动请求 (流程定义key, 业务key, 变量)
     * @return 流程实例ID
     */
    @Operation(summary = "启动流程")
    Result<String> startProcess(@Valid @RequestBody ProcessStartDTO request);

    /**
     * 查询流程实例
     */
    @Operation(summary = "查询流程实例")
    Result<ProcessInstanceVO> getProcessInstance(@PathVariable String processInstanceId);

    /**
     * 终止流程实例
     */
    @Operation(summary = "终止流程")
    Result<Void> terminateProcess(@PathVariable String processInstanceId);

    /**
     * 获取流程变量
     */
    @Operation(summary = "获取流程变量")
    Result<Map<String, Object>> getProcessVariables(@PathVariable String processInstanceId);

    // ==================== 任务能力 ====================

    /**
     * 查询待办任务
     * @param userId 用户ID (默认当前用户)
     */
    @Operation(summary = "查询待办任务")
    Result<List<TaskVO>> listPendingTasks(@RequestParam(required = false) Long userId);

    /**
     * 查询已办任务
     */
    @Operation(summary = "查询已办任务")
    Result<List<TaskVO>> listCompletedTasks(@RequestParam Long userId);

    /**
     * 完成任务
     * @param taskId 任务ID
     * @param dto 完成请求 (包含审批意见、变量)
     */
    @Operation(summary = "完成任务")
    Result<Void> completeTask(@PathVariable String taskId, @Valid @RequestBody TaskCompleteDTO dto);

    /**
     * 转派任务
     */
    @Operation(summary = "转派任务")
    Result<Void> delegateTask(@PathVariable String taskId, @RequestParam Long toUserId);

    /**
     * 撤回任务
     */
    @Operation(summary = "撤回任务")
    Result<Void> withdrawTask(@PathVariable String taskId);

    // ==================== 表单能力 ====================

    /**
     * 获取任务表单
     */
    @Operation(summary = "获取任务表单")
    Result<Map<String, Object>> getTaskForm(@PathVariable String taskId);

    /**
     * 保存任务表单
     */
    @Operation(summary = "保存任务表单")
    Result<Void> saveTaskForm(@PathVariable String taskId, @RequestBody Map<String, Object> formData);

    // ==================== 历史记录能力 ====================

    /**
     * 查询流程历史
     */
    @Operation(summary = "查询流程历史")
    Result<List<ProcessHistoryVO>> getProcessHistory(@PathVariable String processInstanceId);

    /**
     * 查询任务历史
     */
    @Operation(summary = "查询任务历史")
    Result<List<TaskHistoryVO>> getTaskHistory(@PathVariable String processInstanceId);
}
```

---

### 4. 报表引擎模块 (Platform-Report)

#### 4.1 组件结构

```
nexterp-platform-report/
├── report-api/
│   ├── dto/
│   │   ├── ReportDefinitionDTO.java
│   │   ├── ReportQueryDTO.java
│   │   └── ReportParameterDTO.java
│   ├── vo/
│   │   ├── ReportDefinitionVO.java
│   │   ├── ReportDataVO.java
│   │   └── ReportExportVO.java
│   └── facade/
│       └── ReportFacade.java
│
├── report-domain/
│   ├── model/
│   │   ├── ReportDefinition.java
│   │   ├── ReportParameter.java
│   │   ├── ReportDataset.java
│   │   └── ReportExport.java
│   ├── service/
│   │   ├── ReportService.java
│   │   ├── DatasetService.java
│   │   └── ExportService.java
│   └── event/
│       └── ReportGeneratedEvent.java
│
└── report-infrastructure/
    ├── engine/
    │   ├── JasperReportEngine.java
    │   └── DynamicReportEngine.java
    └── exporter/
        ├── ExcelExporter.java
        ├── PdfExporter.java
        └── CsvExporter.java
```

#### 4.2 组件能力定义

##### ReportFacade (报表门面)

```java
package com.nexterp.platform.report.api.facade;

import com.nexterp.platform.report.api.dto.*;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * 报表引擎门面接口
 */
public interface ReportFacade {

    // ==================== 报表定义能力 ====================

    /**
     * 创建报表定义
     */
    @Operation(summary = "创建报表")
    Result<Long> createReport(@Valid @RequestBody ReportDefinitionDTO dto);

    /**
     * 更新报表定义
     */
    @Operation(summary = "更新报表")
    Result<Void> updateReport(@PathVariable Long id, @Valid @RequestBody ReportDefinitionDTO dto);

    /**
     * 查询报表列表
     */
    @Operation(summary = "查询报表列表")
    Result<List<ReportDefinitionVO>> listReports(@RequestParam String category);

    /**
     * 获取报表定义
     */
    @Operation(summary = "获取报表定义")
    Result<ReportDefinitionVO> getReport(@PathVariable Long id);

    // ==================== 报表执行能力 ====================

    /**
     * 执行报表查询
     * @param reportId 报表ID
     * @param parameters 查询参数
     * @return 报表数据
     */
    @Operation(summary = "执行报表")
    Result<ReportDataVO> executeReport(
        @PathVariable Long id,
        @RequestBody Map<String, Object> parameters
    );

    /**
     * 分页查询报表数据
     */
    @Operation(summary = "分页查询报表")
    Result<PageResult<Map<String, Object>>> queryReportData(
        @PathVariable Long id,
        @Valid @RequestBody ReportQueryDTO query
    );

    // ==================== 报表导出能力 ====================

    /**
     * 导出报表 (Excel)
     */
    @Operation(summary = "导出Excel")
    Result<byte[]> exportExcel(
        @PathVariable Long id,
        @RequestBody Map<String, Object> parameters
    );

    /**
     * 导出报表 (PDF)
     */
    @Operation(summary = "导出PDF")
    Result<byte[]> exportPdf(
        @PathVariable Long id,
        @RequestBody Map<String, Object> parameters
    );

    /**
     * 异步导出报表
     */
    @Operation(summary = "异步导出")
    Result<Long> asyncExport(
        @PathVariable Long id,
        @RequestBody Map<String, Object> parameters,
        @RequestParam String exportType
    );

    // ==================== 数据集能力 ====================

    /**
     * 创建数据集
     */
    @Operation(summary = "创建数据集")
    Result<Long> createDataset(@Valid @RequestBody DatasetDTO dto);

    /**
     * 测试数据集SQL
     */
    @Operation(summary = "测试数据集")
    Result<List<Map<String, Object>>> testDataset(@PathVariable Long datasetId, @RequestBody Map<String, Object> parameters);

    // ==================== 模板能力 ====================

    /**
     * 上传报表模板
     */
    @Operation(summary = "上传模板")
    Result<Void> uploadTemplate(@PathVariable Long reportId, @RequestBody MultipartFile file);

    /**
     * 下载报表模板
     */
    @Operation(summary = "下载模板")
    Result<byte[]> downloadTemplate(@PathVariable Long reportId);
}
```

---

### 5. 消息通知模块 (Platform-Notification)

#### 5.1 组件结构

```
nexterp-platform-notification/
├── notification-api/
│   ├── dto/
│   │   ├── NotificationSendDTO.java
│   │   ├── NotificationTemplateDTO.java
│   │   └── NotificationQueryDTO.java
│   ├── vo/
│   │   ├── NotificationVO.java
│   │   └── NotificationTemplateVO.java
│   └── facade/
│       └── NotificationFacade.java
│
├── notification-domain/
│   ├── model/
│   │   ├── Notification.java
│   │   ├── NotificationTemplate.java
│   │   ├── NotificationChannel.java
│   │   └── NotificationLog.java
│   ├── service/
│   │   ├── NotificationService.java
│   │   ├── TemplateService.java
│   │   └── ChannelService.java
│   └── event/
│       └── NotificationSentEvent.java
│
└── notification-infrastructure/
    └── channel/
        ├── EmailChannel.java
        ├── SmsChannel.java
        ├── WebhookChannel.java
        └── InAppChannel.java
```

#### 5.2 组件能力定义

##### NotificationFacade (通知门面)

```java
package com.nexterp.platform.notification.api.facade;

import com.nexterp.platform.notification.api.dto.*;
import com.nexterp.platform.notification.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 消息通知门面接口
 */
public interface NotificationFacade {

    // ==================== 通知发送能力 ====================

    /**
     * 发送通知
     */
    @Operation(summary = "发送通知")
    Result<Long> sendNotification(@Valid @RequestBody NotificationSendDTO dto);

    /**
     * 批量发送通知
     */
    @Operation(summary = "批量发送")
    Result<List<Long>> sendBatch(@Valid @RequestBody List<NotificationSendDTO> dtos);

    /**
     * 使用模板发送通知
     */
    @Operation(summary = "模板发送")
    Result<Long> sendByTemplate(
        @PathVariable String templateCode,
        @RequestBody Map<String, Object> params
    );

    // ==================== 通知查询能力 ====================

    /**
     * 查询用户通知
     */
    @Operation(summary = "查询通知")
    Result<PageResult<NotificationVO>> listNotifications(
        @RequestParam(required = false) Long userId,
        @Valid @RequestBody NotificationQueryDTO query
    );

    /**
     * 获取未读数量
     */
    @Operation(summary = "未读数量")
    Result<Long> getUnreadCount(@RequestParam(required = false) Long userId);

    // ==================== 通知操作能力 ====================

    /**
     * 标记已读
     */
    @Operation(summary = "标记已读")
    Result<Void> markAsRead(@PathVariable Long notificationId);

    /**
     * 批量标记已读
     */
    @Operation(summary = "批量已读")
    Result<Void> markAllAsRead(@RequestParam(required = false) Long userId);

    /**
     * 删除通知
     */
    @Operation(summary = "删除通知")
    Result<Void> deleteNotification(@PathVariable Long notificationId);

    // ==================== 模板管理能力 ====================

    /**
     * 创建通知模板
     */
    @Operation(summary = "创建模板")
    Result<Long> createTemplate(@Valid @RequestBody NotificationTemplateDTO dto);

    /**
     * 查询模板列表
     */
    @Operation(summary = "查询模板")
    Result<List<NotificationTemplateVO>> listTemplates(@RequestParam String channel);

    /**
     * 更新模板
     */
    @Operation(summary = "更新���板")
    Result<Void> updateTemplate(@PathVariable Long id, @Valid @RequestBody NotificationTemplateDTO dto);

    // ==================== 通道配置能力 ====================

    /**
     * 配置通知通道
     */
    @Operation(summary = "配置通道")
    Result<Void> configChannel(@RequestParam String channel, @RequestBody Map<String, Object> config);

    /**
     * 测试通道连接
     */
    @Operation(summary = "测试通道")
    Result<Boolean> testChannel(@RequestParam String channel);
}
```

---

## 业务层组件设计

### 1. 财务模块 (Business-Finance)

#### 1.1 组件结构

```
nexterp-business-finance/
├── finance-api/
│   ├── dto/
│   │   ├── voucher/
│   │   │   ├── VoucherCreateDTO.java
│   │   │   ├── VoucherUpdateDTO.java
│   │   │   └── VoucherQueryDTO.java
│   │   ├── account/
│   │   │   ├── AccountCreateDTO.java
│   │   │   └── AccountTreeDTO.java
│   │   ├── period/
│   │   │   ├── PeriodOpenDTO.java
│   │   │   └── PeriodCloseDTO.java
│   │   └── report/
│   │       ├── BalanceSheetDTO.java
│   │       └── IncomeStatementDTO.java
│   ├── vo/
│   │   ├── VoucherVO.java
│   │   ├── AccountVO.java
│   │   ├── BalanceVO.java
│   │   └── FinancialReportVO.java
│   └── facade/
│       ├── VoucherFacade.java
│       ├── AccountFacade.java
│       ├── PeriodFacade.java
│       └── ReportFacade.java
│
├── finance-spi/
│   ├── VoucherHandler.java            # 凭证处理器接口
│   ├── AccountValidator.java          # 科目验证器接口
│   └── ReportGenerator.java           # 报表生成器接口
│
├── finance-domain/
│   ├── model/
│   │   ├── voucher/
│   │   │   ├── Voucher.java           # 凭证
│   │   │   ├── VoucherDetail.java     # 凭证明细
│   │   │   └── VoucherStatus.java
│   │   ├── account/
│   │   │   ├── Account.java           # 会计科目
│   │   │   ├── AccountType.java
│   │   │   └── BalanceDirection.java
│   │   ├── period/
│   │   │   ├── AccountingPeriod.java  # 会计期间
│   │   │   └── PeriodStatus.java
│   │   ├── auxiliary/
│   │   │   ├── Customer.java          # 客户
│   │   │   ├── Supplier.java          # 供应商
│   │   │   └── Employee.java          # 职员
│   │   └── report/
│   │       ├── BalanceSheet.java
│   │       └── IncomeStatement.java
│   ├── service/
│   │   ├── VoucherService.java
│   │   ├── AccountService.java
│   │   ├── PeriodService.java
│   │   ├── PostingService.java        # 过账服务
│   │   ├── CarryingForwardService.java # 结转服务
│   │   └── ReportService.java
│   ├── repository/
│   │   ├── VoucherRepository.java
│   │   ├── AccountRepository.java
│   │   └── PeriodRepository.java
│   └── event/
│       ├── VoucherCreatedEvent.java
│       ├── VoucherPostedEvent.java
│       ├── PeriodOpenedEvent.java
│       ├── PeriodClosedEvent.java
│       └── AccountBalanceChangedEvent.java
│
└── finance-application/
    ├── service/
    │   ├── VoucherApplicationService.java
    │   ├── AccountApplicationService.java
    │   ├── PeriodApplicationService.java
    │   └── ReportApplicationService.java
    └── listener/
        ├── SalesOrderListener.java    # 监听销售订单
        ├── PurchaseOrderListener.java # 监听采购订单
        └── PaymentListener.java       # 监听收付款
```

#### 1.2 组件能力定义

##### VoucherFacade (凭证门面)

```java
package com.nexterp.business.finance.api.facade;

import com.nexterp.business.finance.api.dto.voucher.*;
import com.nexterp.business.finance.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 财务凭证门面接口
 */
public interface VoucherFacade {

    // ==================== 凭证CRUD ====================

    /**
     * 创建凭证
     */
    @Operation(summary = "创建凭证")
    Result<Long> createVoucher(@Valid @RequestBody VoucherCreateDTO dto);

    /**
     * 更新凭证 (仅草稿状态)
     */
    @Operation(summary = "更新凭证")
    Result<Void> updateVoucher(@PathVariable Long id, @Valid @RequestBody VoucherUpdateDTO dto);

    /**
     * 删除凭证 (仅草稿状态)
     */
    @Operation(summary = "删除凭证")
    Result<Void> deleteVoucher(@PathVariable Long id);

    /**
     * 查询凭证详情
     */
    @Operation(summary = "凭证详情")
    Result<VoucherVO> getVoucher(@PathVariable Long id);

    /**
     * 分页查询凭证
     */
    @Operation(summary = "分页查询凭证")
    Result<PageResult<VoucherVO>> pageVouchers(@Valid VoucherQueryDTO query);

    // ==================== 凭证操作 ====================

    /**
     * 凭证过账
     * 将草稿状态的凭证过账，生成正式会计分录
     */
    @Operation(summary = "凭证过账")
    Result<Void> postVoucher(@PathVariable Long id);

    /**
     * 凭证审核
     */
    @Operation(summary = "凭证审核")
    Result<Void> reviewVoucher(@PathVariable Long id, @RequestParam Boolean approved);

    /**
     * 凭证反审核
     */
    @Operation(summary = "凭证反审核")
    Result<Void> unreviewVoucher(@PathVariable Long id);

    /**
     * 凭证冲销
     * 创建红字凭证冲销已过账凭证
     */
    @Operation(summary = "凭证冲销")
    Result<Long> reverseVoucher(@PathVariable Long id, @RequestBody ReverseReasonDTO dto);

    // ==================== 凭证辅助功能 ====================

    /**
     * 获取凭证号
     * 根据日期和凭证类型生成新凭证号
     */
    @Operation(summary = "获取新凭证号")
    Result<String> getNewVoucherNo(@RequestParam String voucherDate, @RequestParam String voucherType);

    /**
     * 借贷平衡检查
     */
    @Operation(summary = "借贷平衡检查")
    Result<Boolean> checkBalance(@RequestBody List<VoucherDetailDTO> details);

    /**
     * 批量导入凭证
     */
    @Operation(summary = "批量导入")
    Result<List<Long>> importVouchers(@RequestBody List<VoucherCreateDTO> dtos);

    /**
     * 导出凭证
     */
    @Operation(summary = "导出凭证")
    Result<byte[]> exportVouchers(@Valid VoucherQueryDTO query, @RequestParam String exportType);
}
```

##### AccountFacade (科目门面)

```java
package com.nexterp.business.finance.api.facade;

import com.nexterp.business.finance.api.dto.account.*;
import com.nexterp.business.finance.api.vo.*;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 会计科目门面接口
 */
public interface AccountFacade {

    /**
     * 创建科目
     */
    @Operation(summary = "创建科目")
    Result<Long> createAccount(@Valid @RequestBody AccountCreateDTO dto);

    /**
     * 更新科目
     */
    @Operation(summary = "更新科目")
    Result<Void> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountUpdateDTO dto);

    /**
     * 删除科目
     */
    @Operation(summary = "删除科目")
    Result<Void> deleteAccount(@PathVariable Long id);

    /**
     * 查询科目树
     */
    @Operation(summary = "科目树")
    Result<List<AccountTreeVO>> getAccountTree();

    /**
     * 查询科目详情
     */
    @Operation(summary = "科目详情")
    Result<AccountVO> getAccount(@PathVariable Long id);

    /**
     * 查询科目余额
     */
    @Operation(summary = "科目余额")
    Result<BalanceVO> getAccountBalance(@PathVariable Long accountId, @RequestParam String period);

    /**
     * 启用/停用科目
     */
    @Operation(summary = "启用/停用科目")
    Result<Void> setAccountEnabled(@PathVariable Long id, @RequestParam Boolean enabled);

    /**
     * 批量导入科目
     */
    @Operation(summary = "导入科目模板")
    Result<Void> importAccountTemplate(@RequestParam String templateType);

    /**
     * 获取科目初始余额
     */
    @Operation(summary = "初始余额")
    Result<InitialBalanceVO> getInitialBalance(@PathVariable Long accountId);
}
```

##### PeriodFacade (期间门面)

```java
package com.nexterp.business.finance.api.facade;

import com.nexterp.business.finance.api.dto.period.*;
import com.nexterp.business.finance.api.vo.*;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 会计期间门面接口
 */
public interface PeriodFacade {

    /**
     * 开启会计期间
     */
    @Operation(summary = "开启期间")
    Result<Void> openPeriod(@Valid @RequestBody PeriodOpenDTO dto);

    /**
     * 关闭会计期间
     */
    @Operation(summary = "关闭期间")
    Result<Void> closePeriod(@PathVariable Long periodId, @RequestBody PeriodCloseDTO dto);

    /**
     * 反结账
     */
    @Operation(summary = "反结账")
    Result<Void> reopenPeriod(@PathVariable Long periodId);

    /**
     * 查询期间列表
     */
    @Operation(summary = "期间列表")
    Result<List<AccountingPeriodVO>> listPeriods(@RequestParam Integer fiscalYear);

    /**
     * 获取当前期间
     */
    @Operation(summary = "当前期间")
    Result<AccountingPeriodVO> getCurrentPeriod();

    /**
     * 期间结转
     * 将本期余额结转到下期
     */
    @Operation(summary = "期间结转")
    Result<Void> carryForward(@PathVariable Long periodId);

    /**
     * 年度结转
     */
    @Operation(summary = "年度结转")
    Result<Void> annualCarryForward(@RequestParam Integer fiscalYear);

    /**
     * 检查期间状态
     */
    @Operation(summary = "检查期间状态")
    Result<PeriodStatusVO> checkPeriodStatus(@PathVariable Long periodId);
}
```

##### ReportFacade (财务报表门面)

```java
package com.nexterp.business.finance.api.facade;

import com.nexterp.business.finance.api.dto.report.*;
import com.nexterp.business.finance.api.vo.*;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 财务报表门面接口
 */
public interface ReportFacade {

    /**
     * 资产负债表
     */
    @Operation(summary = "资产负债表")
    Result<BalanceSheetVO> getBalanceSheet(@RequestParam String period);

    /**
     * 利润表
     */
    @Operation(summary = "利润表")
    Result<IncomeStatementVO> getIncomeStatement(@RequestParam String period);

    /**
     * 现金流量表
     */
    @Operation(summary = "现金流量表")
    Result<CashFlowVO> getCashFlow(@RequestParam String period);

    /**
     * 科目余额表
     */
    @Operation(summary = "科目余额表")
    Result<AccountBalanceReportVO> getAccountBalanceReport(@RequestParam String period);

    /**
     * 明细账
     */
    @Operation(summary = "明细账")
    Result<DetailLedgerVO> getDetailLedger(@RequestBody DetailLedgerQueryDTO query);

    /**
     * 总账
     */
    @Operation(summary = "总账")
    Result<GeneralLedgerVO> getGeneralLedger(@RequestParam String period);

    /**
     * 辅助核算明细账
     */
    @Operation(summary = "辅助核算明细账")
    Result<AuxiliaryLedgerVO> getAuxiliaryLedger(@RequestBody AuxiliaryQueryDTO query);

    /**
     * 自定义报表
     */
    @Operation(summary = "自定义报表")
    Result<Map<String, Object>> getCustomReport(@RequestParam Long reportId, @RequestBody Map<String, Object> params);

    /**
     * 导出报表
     */
    @Operation(summary = "导出报表")
    Result<byte[]> exportReport(@RequestParam String reportType, @RequestParam String period, @RequestParam String exportType);
}
```

#### 1.3 事件接口

```java
// ==================== 财务模块事件 ====================

/**
 * 凭证创建事件
 * 供其他模块（如销售、采购）监听处理
 */
public record VoucherCreatedEvent(
    Long tenantId,
    Long voucherId,
    String voucherNo,
    String voucherType,
    BigDecimal debitAmount,
    BigDecimal creditAmount,
    LocalDateTime createTime
) {}

/**
 * 凭证过账事件
 * 凭证过账后发布，触发总账更新
 */
public record VoucherPostedEvent(
    Long tenantId,
    Long voucherId,
    String voucherNo,
    String accountingPeriod,
    LocalDateTime postTime
) {}

/**
 * 期间开启事件
 */
public record PeriodOpenedEvent(
    Long tenantId,
    Long periodId,
    String periodCode,
    LocalDateTime openTime
) {}

/**
 * 期间关闭事件
 */
public record PeriodClosedEvent(
    Long tenantId,
    Long periodId,
    String periodCode,
    LocalDateTime closeTime
) {}

/**
 * 科目余额变更事件
 */
public record AccountBalanceChangedEvent(
    Long tenantId,
    Long accountId,
    String accountCode,
    String period,
    BigDecimal debitBalance,
    BigDecimal creditBalance,
    LocalDateTime changeTime
) {}
```

---

### 2. 供应链模块 (Business-Supply)

#### 2.1 组件结构

```
nexterp-business-supply/
├── supply-api/
│   ├── dto/
│   │   ├── purchase/
│   │   │   ├── PurchaseOrderCreateDTO.java
│   │   │   └── PurchaseOrderQueryDTO.java
│   │   ├── inventory/
│   │   │   ├── StockInDTO.java
│   │   │   ├── StockOutDTO.java
│   │   │   └── InventoryQueryDTO.java
│   │   └── supplier/
│   │       └── SupplierCreateDTO.java
│   ├── vo/
│   │   ├── PurchaseOrderVO.java
│   │   ├── InventoryVO.java
│   │   └── SupplierVO.java
│   └── facade/
│       ├── PurchaseFacade.java
│       ├── InventoryFacade.java
│       └── SupplierFacade.java
│
├── supply-domain/
│   ├── model/
│   │   ├── purchase/
│   │   │   ├── PurchaseOrder.java
│   │   │   ├── PurchaseOrderDetail.java
│   │   │   └── PurchaseOrderStatus.java
│   │   ├── inventory/
│   │   │   ├── Inventory.java
│   │   │   ├── InventoryTransaction.java
│   │   │   └── Warehouse.java
│   │   └── supplier/
│   │       └── Supplier.java
│   ├── service/
│   │   ├── PurchaseOrderService.java
│   │   ├── InventoryService.java
│   │   └── SupplierService.java
│   └── event/
│       ├── PurchaseOrderCreatedEvent.java
│       ├── PurchaseOrderApprovedEvent.java
│       ├── StockInEvent.java
│       └── StockOutEvent.java
│
└── supply-application/
    └── listener/
        └── FinanceVoucherListener.java
```

#### 2.2 组件能力定义

##### PurchaseFacade (采购门面)

```java
package com.nexterp.business.supply.api.facade;

import com.nexterp.business.supply.api.dto.purchase.*;
import com.nexterp.business.supply.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 采购管理门面接口
 */
public interface PurchaseFacade {

    // ==================== 采购订单 ====================

    @Operation(summary = "创建采购订单")
    Result<Long> createOrder(@Valid @RequestBody PurchaseOrderCreateDTO dto);

    @Operation(summary = "更新采购订单")
    Result<Void> updateOrder(@PathVariable Long id, @Valid @RequestBody PurchaseOrderUpdateDTO dto);

    @Operation(summary = "删除采购订单")
    Result<Void> deleteOrder(@PathVariable Long id);

    @Operation(summary = "采购订单详情")
    Result<PurchaseOrderVO> getOrder(@PathVariable Long id);

    @Operation(summary = "分页查询采购订单")
    Result<PageResult<PurchaseOrderVO>> pageOrders(@Valid PurchaseOrderQueryDTO query);

    // ==================== 采购流程 ====================

    @Operation(summary = "提交审核")
    Result<Void> submitForApproval(@PathVariable Long id);

    @Operation(summary = "审核采购订单")
    Result<Void> approveOrder(@PathVariable Long id, @RequestParam Boolean approved);

    @Operation(summary = "关闭采购订单")
    Result<Void> closeOrder(@PathVariable Long id);

    @Operation(summary = "采购订单入库")
    Result<Long> receipt(@PathVariable Long orderId, @Valid @RequestBody PurchaseReceiptDTO dto);

    // ==================== 采购退货 ====================

    @Operation(summary = "创建退货单")
    Result<Long> createReturn(@Valid @RequestBody PurchaseReturnCreateDTO dto);

    @Operation(summary = "退货单审核")
    Result<Void> approveReturn(@PathVariable Long id);
}
```

##### InventoryFacade (库存门面)

```java
package com.nexterp.business.supply.api.facade;

import com.nexterp.business.supply.api.dto.inventory.*;
import com.nexterp.business.supply.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 库存管理门面接口
 */
public interface InventoryFacade {

    // ==================== 库存查询 ====================

    @Operation(summary = "库存查询")
    Result<PageResult<InventoryVO>> pageInventory(@Valid InventoryQueryDTO query);

    @Operation(summary = "库存详情")
    Result<InventoryVO> getInventory(@PathVariable Long id);

    @Operation(summary = "实时库存")
    Result<List<RealTimeInventoryVO>> getRealTimeInventory(@RequestBody List<String> materialCodes);

    // ==================== 库存操作 ====================

    @Operation(summary = "入库")
    Result<Void> stockIn(@Valid @RequestBody StockInDTO dto);

    @Operation(summary = "出库")
    Result<Void> stockOut(@Valid @RequestBody StockOutDTO dto);

    @Operation(summary = "调拨")
    Result<Void> transfer(@Valid @RequestBody StockTransferDTO dto);

    @Operation(summary = "盘点")
    Result<Void> stocktake(@Valid @RequestBody StocktakeDTO dto);

    // ==================== 库存预警 ====================

    @Operation(summary = "库存预警列表")
    Result<List<InventoryAlertVO>> getAlerts();

    @Operation(summary = "安全库存设置")
    Result<Void> setSafetyStock(@PathVariable Long id, @RequestParam BigDecimal safetyStock);

    // ==================== 批次管理 ====================

    @Operation(summary = "批次查询")
    Result<List<BatchInventoryVO>> getBatches(@RequestParam String materialCode);

    @Operation(summary = "批次过期预警")
    Result<List<BatchExpirationVO>> getExpirationAlerts();
}
```

##### SupplierFacade (供应商门面)

```java
package com.nexterp.business.supply.api.facade;

import com.nexterp.business.supply.api.dto.supplier.*;
import com.nexterp.business.supply.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商管理门面接口
 */
public interface SupplierFacade {

    @Operation(summary = "创建供应商")
    Result<Long> createSupplier(@Valid @RequestBody SupplierCreateDTO dto);

    @Operation(summary = "更新供应商")
    Result<Void> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierUpdateDTO dto);

    @Operation(summary = "删除供应商")
    Result<Void> deleteSupplier(@PathVariable Long id);

    @Operation(summary = "供应商详情")
    Result<SupplierVO> getSupplier(@PathVariable Long id);

    @Operation(summary = "分页查询供应商")
    Result<PageResult<SupplierVO>> pageSuppliers(@Valid SupplierQueryDTO query);

    @Operation(summary = "启用/停用供应商")
    Result<Void> setSupplierStatus(@PathVariable Long id, @RequestParam Integer status);

    @Operation(summary = "供应商审核")
    Result<Void> approveSupplier(@PathVariable Long id, @RequestParam Boolean approved);
}
```

---

### 3. 销售模块 (Business-Sales)

#### 3.1 组件结构

```
nexterp-business-sales/
├── sales-api/
│   ├── dto/
│   │   ├── order/
│   │   │   ├── SalesOrderCreateDTO.java
│   │   │   └── SalesOrderQueryDTO.java
│   │   ├── delivery/
│   │   │   └── DeliveryCreateDTO.java
│   │   └── customer/
│   │       └── CustomerCreateDTO.java
│   ├── vo/
│   │   ├── SalesOrderVO.java
│   │   ├── DeliveryVO.java
│   │   └── CustomerVO.java
│   └── facade/
│       ├── SalesOrderFacade.java
│       ├── DeliveryFacade.java
│       └── CustomerFacade.java
│
├── sales-domain/
│   ├── model/
│   │   ├── order/
│   │   │   ├── SalesOrder.java
│   │   │   ├── SalesOrderDetail.java
│   │   │   └── SalesOrderStatus.java
│   │   ├── delivery/
│   │   │   ├── Delivery.java
│   │   │   └── DeliveryDetail.java
│   │   └── customer/
│   │       └── Customer.java
│   ├── service/
│   │   ├── SalesOrderService.java
│   │   ├── DeliveryService.java
│   │   └── CustomerService.java
│   └── event/
│       ├── SalesOrderCreatedEvent.java
│       ├── SalesOrderApprovedEvent.java
│       └── DeliveryShippedEvent.java
│
└── sales-application/
    └── listener/
        └── FinanceReceivableListener.java
```

#### 3.2 组件能力定义

##### SalesOrderFacade (销售订单门面)

```java
package com.nexterp.business.sales.api.facade;

import com.nexterp.business.sales.api.dto.order.*;
import com.nexterp.business.sales.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 销售订单门面接口
 */
public interface SalesOrderFacade {

    // ==================== 销售订单 ====================

    @Operation(summary = "创建销售订单")
    Result<Long> createOrder(@Valid @RequestBody SalesOrderCreateDTO dto);

    @Operation(summary = "更新销售订单")
    Result<Void> updateOrder(@PathVariable Long id, @Valid @RequestBody SalesOrderUpdateDTO dto);

    @Operation(summary = "删除销售订单")
    Result<Void> deleteOrder(@PathVariable Long id);

    @Operation(summary = "销售订单详情")
    Result<SalesOrderVO> getOrder(@PathVariable Long id);

    @Operation(summary = "分页查询销售订单")
    Result<PageResult<SalesOrderVO>> pageOrders(@Valid SalesOrderQueryDTO query);

    // ==================== 销售流程 ====================

    @Operation(summary = "提交审核")
    Result<Void> submitForApproval(@PathVariable Long id);

    @Operation(summary = "审核销售订单")
    Result<Void> approveOrder(@PathVariable Long id, @RequestParam Boolean approved);

    @Operation(summary = "关闭销售订单")
    Result<Void> closeOrder(@PathVariable Long id);

    @Operation(summary = "订单出库")
    Result<Long> delivery(@PathVariable Long orderId, @Valid @RequestBody DeliveryCreateDTO dto);

    // ==================== 价格管理 ====================

    @Operation(summary = "查询价格")
    Result<PriceVO> getPrice(@RequestParam Long customerId, @RequestParam Long materialId);

    @Operation(summary = "查询折扣")
    Result<DiscountVO> getDiscount(@RequestParam Long customerId, @RequestParam BigDecimal amount);
}
```

---

### 4. 生产模块 (Business-Production)

#### 4.1 组件结构

```
nexterp-business-production/
├── production-api/
│   ├── dto/
│   │   ├── bom/
│   │   │   └── BomCreateDTO.java
│   │   ├── productionorder/
│   │   │   └── ProductionOrderCreateDTO.java
│   │   └── routing/
│   │       └── RoutingCreateDTO.java
│   ├── vo/
│   │   ├── BomVO.java
│   │   ├── ProductionOrderVO.java
│   │   └── RoutingVO.java
│   └── facade/
│       ├── BomFacade.java
│       ├── ProductionOrderFacade.java
│       └── RoutingFacade.java
│
├── production-domain/
│   ├── model/
│   │   ├── bom/
│   │   │   └── BillOfMaterial.java
│   │   ├── order/
│   │   │   └── ProductionOrder.java
│   │   └── routing/
│   │       └── Routing.java
│   ├── service/
│   │   ├── BomService.java
│   │   ├── ProductionOrderService.java
│   │   └── RoutingService.java
│   └── event/
│       ├── ProductionOrderCreatedEvent.java
│       └── ProductionOrderCompletedEvent.java
```

#### 4.2 组件能力定义

##### BomFacade (BOM门面)

```java
package com.nexterp.business.production.api.facade;

import com.nexterp.business.production.api.dto.bom.*;
import com.nexterp.business.production.api.vo.*;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * BOM管理门面接口
 */
public interface BomFacade {

    /**
     * 创建BOM
     */
    @Operation(summary = "创建BOM")
    Result<Long> createBom(@Valid @RequestBody BomCreateDTO dto);

    /**
     * 更新BOM
     */
    @Operation(summary = "更新BOM")
    Result<Void> updateBom(@PathVariable Long id, @Valid @RequestBody BomUpdateDTO dto);

    /**
     * 删除BOM
     */
    @Operation(summary = "删除BOM")
    Result<Void> deleteBom(@PathVariable Long id);

    /**
     * 查询BOM详情
     */
    @Operation(summary = "BOM详情")
    Result<BomVO> getBom(@PathVariable Long id);

    /**
     * 查询产品BOM列表
     */
    @Operation(summary = "产品BOM列表")
    Result<List<BomVO>> listByProduct(@PathVariable Long productId);

    /**
     * BOM版本对比
     */
    @Operation(summary = "BOM版本对比")
    Result<BomCompareVO> compareVersions(@PathVariable Long version1Id, @PathVariable Long version2Id);

    /**
     * BOM展开（低阶码展开）
     */
    @Operation(summary = "BOM展开")
    Result<List<BomItemVO>> explodeBom(@PathVariable Long bomId);

    /**
     * 反查BOM（物料被哪些产品使用）
     */
    @Operation(summary = "反查BOM")
    Result<List<BomUsageVO>> whereUsed(@PathVariable Long materialId);
}
```

##### ProductionOrderFacade (生产订单门面)

```java
package com.nexterp.business.production.api.facade;

import com.nexterp.business.production.api.dto.productionorder.*;
import com.nexterp.business.production.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 生产订单门面接口
 */
public interface ProductionOrderFacade {

    // ==================== 生产订单 ====================

    @Operation(summary = "创建生产订单")
    Result<Long> createOrder(@Valid @RequestBody ProductionOrderCreateDTO dto);

    @Operation(summary = "更新生产订单")
    Result<Void> updateOrder(@PathVariable Long id, @Valid @RequestBody ProductionOrderUpdateDTO dto);

    @Operation(summary = "删除生产订单")
    Result<Void> deleteOrder(@PathVariable Long id);

    @Operation(summary = "生产订单详情")
    Result<ProductionOrderVO> getOrder(@PathVariable Long id);

    @Operation(summary = "分页查询生产订单")
    Result<PageResult<ProductionOrderVO>> pageOrders(@Valid ProductionOrderQueryDTO query);

    // ==================== 生产流程 ====================

    @Operation(summary = "下达生产订单")
    Result<Void> releaseOrder(@PathVariable Long id);

    @Operation(summary = "开始生产")
    Result<Void> startProduction(@PathVariable Long id);

    @Operation(summary = "汇报工时")
    Result<Void> reportLabor(@PathVariable Long id, @Valid @RequestBody LaborReportDTO dto);

    @Operation(summary = "完工入库")
    Result<Void> completeOrder(@PathVariable Long id, @Valid @RequestBody ProductionCompletionDTO dto);

    @Operation(summary = "关闭生产订单")
    Result<Void> closeOrder(@PathVariable Long id);

    // ==================== 领料与退料 ====================

    @Operation(summary = "生产领料")
    Result<Void> issueMaterial(@PathVariable Long orderId, @Valid @RequestBody MaterialIssueDTO dto);

    @Operation(summary = "生产退料")
    Result<Void> returnMaterial(@PathVariable Long orderId, @Valid @RequestBody MaterialReturnDTO dto);

    // ==================== 进度查询 ====================

    @Operation(summary = "查询生产进度")
    Result<ProductionProgressVO> getProgress(@PathVariable Long orderId);

    @Operation(summary = "车间在制品查询")
    Result<List<WipVO>> listWip(@RequestParam Long workCenterId);
}
```

##### RoutingFacade (工艺路线门面)

```java
package com.nexterp.business.production.api.facade;

import com.nexterp.business.production.api.dto.routing.*;
import com.nexterp.business.production.api.vo.*;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 工艺路线门面接口
 */
public interface RoutingFacade {

    @Operation(summary = "创建工艺路线")
    Result<Long> createRouting(@Valid @RequestBody RoutingCreateDTO dto);

    @Operation(summary = "更新工艺路线")
    Result<Void> updateRouting(@PathVariable Long id, @Valid @RequestBody RoutingUpdateDTO dto);

    @Operation(summary = "删除工艺路线")
    Result<Void> deleteRouting(@PathVariable Long id);

    @Operation(summary = "工艺路线详情")
    Result<RoutingVO> getRouting(@PathVariable Long id);

    @Operation(summary = "查询产品工艺路线")
    Result<List<RoutingVO>> listByProduct(@PathVariable Long productId);

    @Operation(summary = "添加工序")
    Result<Void> addOperation(@PathVariable Long routingId, @Valid @RequestBody OperationCreateDTO dto);

    @Operation(summary = "更新工序")
    Result<Void> updateOperation(@PathVariable Long operationId, @Valid @RequestBody OperationUpdateDTO dto);

    @Operation(summary = "删除工序")
    Result<Void> deleteOperation(@PathVariable Long operationId);
}
```

---

### 5. 人力资源模块 (Business-HRM)

#### 5.1 组件结构

```
nexterp-business-hrm/
├── hrm-api/
│   ├── dto/
│   │   ├── employee/
│   │   │   ├── EmployeeCreateDTO.java
│   │   │   └── EmployeeQueryDTO.java
│   │   ├── attendance/
│   │   │   ├── AttendanceRecordDTO.java
│   │   │   └── LeaveRequestDTO.java
│   │   ├── payroll/
│   │   │   ├── SalaryCalculateDTO.java
│   │   │   └── PayslipDTO.java
│   │   └── performance/
│   │       └── PerformanceReviewDTO.java
│   ├── vo/
│   │   ├── EmployeeVO.java
│   │   ├── AttendanceVO.java
│   │   ├── PayslipVO.java
│   │   └── PerformanceVO.java
│   └── facade/
│       ├── EmployeeFacade.java
│       ├── AttendanceFacade.java
│       ├── PayrollFacade.java
│       └── PerformanceFacade.java
│
├── hrm-domain/
│   ├── model/
│   │   ├── employee/
│   │   │   ├── Employee.java
│   │   │   ├── EmployeeContract.java
│   │   │   └── Position.java
│   │   ├── attendance/
│   │   │   ├── AttendanceRecord.java
│   │   │   ├── LeaveRequest.java
│   │   │   └── Shift.java
│   │   ├── payroll/
│   │   │   ├── SalaryPeriod.java
│   │   │   ├── Payslip.java
│   │   │   └── SalaryItem.java
│   │   └── performance/
│   │       ├── PerformanceReview.java
│   │       └── KPI.java
│   ├── service/
│   │   ├── EmployeeService.java
│   │   ├── AttendanceService.java
│   │   ├── PayrollService.java
│   │   └── PerformanceService.java
│   └── event/
│       ├── EmployeeOnboardEvent.java
│       ├── EmployeeResignEvent.java
│       └── PayrollCalculatedEvent.java
│
└── hrm-application/
    └── listener/
        └── FinanceVoucherListener.java
```

#### 5.2 组件能力定义

##### EmployeeFacade (员工门面)

```java
package com.nexterp.business.hrm.api.facade;

import com.nexterp.business.hrm.api.dto.employee.*;
import com.nexterp.business.hrm.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 员工管理门面接口
 */
public interface EmployeeFacade {

    // ==================== 员工档案 ====================

    @Operation(summary = "创建员工")
    Result<Long> createEmployee(@Valid @RequestBody EmployeeCreateDTO dto);

    @Operation(summary = "更新员工")
    Result<Void> updateEmployee(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateDTO dto);

    @Operation(summary = "删除员工")
    Result<Void> deleteEmployee(@PathVariable Long id);

    @Operation(summary = "员工详情")
    Result<EmployeeVO> getEmployee(@PathVariable Long id);

    @Operation(summary = "分页查询员工")
    Result<PageResult<EmployeeVO>> pageEmployees(@Valid EmployeeQueryDTO query);

    // ==================== 入职离职 ====================

    @Operation(summary = "员工入职")
    Result<Void> onboard(@PathVariable Long id, @Valid @RequestBody OnboardDTO dto);

    @Operation(summary = "员工离职")
    Result<Void> resign(@PathVariable Long id, @Valid @RequestBody ResignDTO dto);

    @Operation(summary = "员工转正")
    Result<Void> regularize(@PathVariable Long id, @Valid @RequestBody RegularizeDTO dto);

    // ==================== 合同管理 ====================

    @Operation(summary = "创建合同")
    Result<Long> createContract(@Valid @RequestBody ContractCreateDTO dto);

    @Operation(summary = "续签合同")
    Result<Void> renewContract(@PathVariable Long contractId, @Valid @RequestBody ContractRenewDTO dto);

    @Operation(summary = "终止合同")
    Result<Void> terminateContract(@PathVariable Long contractId, @RequestBody TerminateReasonDTO dto);

    @Operation(summary = "查询员工合同")
    Result<List<ContractVO>> listContracts(@PathVariable Long employeeId);

    // ==================== 岗位调动 ====================

    @Operation(summary = "岗位调动")
    Result<Void> transferPosition(@PathVariable Long employeeId, @Valid @RequestBody PositionTransferDTO dto);
}
```

##### AttendanceFacade (考勤门面)

```java
package com.nexterp.business.hrm.api.facade;

import com.nexterp.business.hrm.api.dto.attendance.*;
import com.nexterp.business.hrm.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 考勤管理门面接口
 */
public interface AttendanceFacade {

    // ==================== 考勤记录 ====================

    @Operation(summary = "签到")
    Result<Void> checkIn(@Valid @RequestBody CheckInDTO dto);

    @Operation(summary = "签退")
    Result<Void> checkOut(@Valid @RequestBody CheckOutDTO dto);

    @Operation(summary = "补签")
    Result<Void> supplement(@Valid @RequestBody SupplementDTO dto);

    @Operation(summary = "查询考勤记录")
    Result<PageResult<AttendanceVO>> pageRecords(@Valid AttendanceQueryDTO query);

    // ==================== 请假管理 ====================

    @Operation(summary = "申请请假")
    Result<Long> requestLeave(@Valid @RequestBody LeaveRequestDTO dto);

    @Operation(summary = "审批请假")
    Result<Void> approveLeave(@PathVariable Long requestId, @RequestParam Boolean approved);

    @Operation(summary = "撤销请假")
    Result<Void> cancelLeave(@PathVariable Long requestId);

    @Operation(summary = "查询请假记录")
    Result<List<LeaveVO>> listLeaves(@PathVariable Long employeeId);

    // ==================== 排班管理 ====================

    @Operation(summary = "创建排班")
    Result<Long> createShift(@Valid @RequestBody ShiftCreateDTO dto);

    @Operation(summary = "批量排班")
    Result<Void> batchSchedule(@RequestBody List<ShiftScheduleDTO> list);

    @Operation(summary = "查询排班")
    Result<List<ShiftVO>> listShifts(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate);

    // ==================== 加班管理 ====================

    @Operation(summary = "申请加班")
    Result<Long> requestOvertime(@Valid @RequestBody OvertimeRequestDTO dto);

    @Operation(summary = "审批加班")
    Result<Void> approveOvertime(@PathVariable Long requestId, @RequestParam Boolean approved);
}
```

##### PayrollFacade (薪酬门面)

```java
package com.nexterp.business.hrm.api.facade;

import com.nexterp.business.hrm.api.dto.payroll.*;
import com.nexterp.business.hrm.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 薪酬管理门面接口
 */
public interface PayrollFacade {

    // ==================== 薪酬计算 ====================

    @Operation(summary = "计算薪酬")
    Result<Void> calculate(@Valid @RequestBody PayrollCalculateDTO dto);

    @Operation(summary = "批量计算薪酬")
    Result<Void> batchCalculate(@Valid @RequestBody BatchCalculateDTO dto);

    @Operation(summary = "薪酬计算结果")
    Result<PayslipVO> getPayslip(@PathVariable Long employeeId, @PathVariable String period);

    @Operation(summary = "薪酬发放")
    Result<Void> pay(@Valid @RequestBody PayrollPayDTO dto);

    // ==================== 薪酬项目 ====================

    @Operation(summary = "创建薪酬项目")
    Result<Long> createSalaryItem(@Valid @RequestBody SalaryItemCreateDTO dto);

    @Operation(summary = "更新薪酬项目")
    Result<Void> updateSalaryItem(@PathVariable Long id, @Valid @RequestBody SalaryItemUpdateDTO dto);

    @Operation(summary = "查询薪酬项目")
    Result<List<SalaryItemVO>> listSalaryItems();

    // ==================== 社保公积金 ====================

    @Operation(summary = "设置社保")
    Result<Void> setSocialSecurity(@PathVariable Long employeeId, @Valid @RequestBody SocialSecurityDTO dto);

    @Operation(summary = "设置公积金")
    Result<Void> setProvidentFund(@PathVariable Long employeeId, @Valid @RequestBody ProvidentFundDTO dto);

    // ==================== 报表导出 ====================

    @Operation(summary = "薪酬报表")
    Result<byte[]> exportPayrollReport(@Valid @RequestBody PayrollReportQueryDTO query);
}
```

##### PerformanceFacade (绩效门面)

```java
package com.nexterp.business.hrm.api.facade;

import com.nexterp.business.hrm.api.dto.performance.*;
import com.nexterp.business.hrm.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 绩效管理门面接口
 */
public interface PerformanceFacade {

    @Operation(summary = "创建考核周期")
    Result<Long> createCycle(@Valid @RequestBody PerformanceCycleCreateDTO dto);

    @Operation(summary = "发起绩效评估")
    Result<Long> initiateReview(@Valid @RequestBody ReviewInitiateDTO dto);

    @Operation(summary = "提交自评")
    Result<Void> submitSelfReview(@PathVariable Long reviewId, @Valid @RequestBody SelfReviewDTO dto);

    @Operation(summary = "提交上级评价")
    Result<Void> submitManagerReview(@PathVariable Long reviewId, @Valid @RequestBody ManagerReviewDTO dto);

    @Operation(summary = "确认绩效结果")
    Result<Void> confirmResult(@PathVariable Long reviewId);

    @Operation(summary = "查询绩效记录")
    Result<PageResult<PerformanceVO>> pageReviews(@Valid PerformanceQueryDTO query);

    @Operation(summary = "设置KPI")
    Result<Void> setKPI(@PathVariable Long employeeId, @Valid @RequestBody KPISetDTO dto);
}
```

---

### 6. 项目管理模块 (Business-Project)

#### 6.1 组件结构

```
nexterp-business-project/
├── project-api/
│   ├── dto/
│   │   ├── project/
│   │   │   ├── ProjectCreateDTO.java
│   │   │   └── ProjectQueryDTO.java
│   │   ├── task/
│   │   │   └── TaskCreateDTO.java
│   │   └── resource/
│   │       └── ResourceAssignDTO.java
│   ├── vo/
│   │   ├── ProjectVO.java
│   │   ├── TaskVO.java
│   │   └── ResourceVO.java
│   └── facade/
│       ├── ProjectFacade.java
│       ├── TaskFacade.java
│       └── ResourceFacade.java
│
├── project-domain/
│   ├── model/
│   │   ├── Project.java
│   │   ├── Task.java
│   │   ├── Milestone.java
│   │   └── ResourceAssignment.java
│   ├── service/
│   │   ├── ProjectService.java
│   │   ├── TaskService.java
│   │   └── ResourceService.java
│   └── event/
│       ├── ProjectCreatedEvent.java
│       ├── TaskCompletedEvent.java
│       └── MilestoneReachedEvent.java
```

#### 6.2 组件能力定义

##### ProjectFacade (项目门面)

```java
package com.nexterp.business.project.api.facade;

import com.nexterp.business.project.api.dto.project.*;
import com.nexterp.business.project.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 项目管理门面接口
 */
public interface ProjectFacade {

    // ==================== 项目管理 ====================

    @Operation(summary = "创建项目")
    Result<Long> createProject(@Valid @RequestBody ProjectCreateDTO dto);

    @Operation(summary = "更新项目")
    Result<Void> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectUpdateDTO dto);

    @Operation(summary = "删除项目")
    Result<Void> deleteProject(@PathVariable Long id);

    @Operation(summary = "项目详情")
    Result<ProjectVO> getProject(@PathVariable Long id);

    @Operation(summary = "分页查询项目")
    Result<PageResult<ProjectVO>> pageProjects(@Valid ProjectQueryDTO query);

    // ==================== 项目流程 ====================

    @Operation(summary = "启动项目")
    Result<Void> startProject(@PathVariable Long id);

    @Operation(summary = "暂停项目")
    Result<Void> pauseProject(@PathVariable Long id);

    @Operation(summary = "恢复项目")
    Result<Void> resumeProject(@PathVariable Long id);

    @Operation(summary = "完成项目")
    Result<Void> completeProject(@PathVariable Long id);

    @Operation(summary = "取消项目")
    Result<Void> cancelProject(@PathVariable Long id);

    // ==================== 里程碑 ====================

    @Operation(summary = "创建里程碑")
    Result<Long> createMilestone(@Valid @RequestBody MilestoneCreateDTO dto);

    @Operation(summary = "更新里程碑")
    Result<Void> updateMilestone(@PathVariable Long id, @Valid @RequestBody MilestoneUpdateDTO dto);

    @Operation(summary = "标记里程碑完成")
    Result<Void> completeMilestone(@PathVariable Long id);

    // ==================== 项目进度 ====================

    @Operation(summary = "查询项目进度")
    Result<ProjectProgressVO> getProgress(@PathVariable Long projectId);

    @Operation(summary = "项目成本统计")
    Result<ProjectCostVO> getCost(@PathVariable Long projectId);

    @Operation(summary = "项目甘特图")
    Result<GanttChartVO> getGanttChart(@PathVariable Long projectId);
}
```

##### TaskFacade (任务门面)

```java
package com.nexterp.business.project.api.facade;

import com.nexterp.business.project.api.dto.task.*;
import com.nexterp.business.project.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 任务管理门面接口
 */
public interface TaskFacade {

    @Operation(summary = "创建任务")
    Result<Long> createTask(@Valid @RequestBody TaskCreateDTO dto);

    @Operation(summary = "更新任务")
    Result<Void> updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpdateDTO dto);

    @Operation(summary = "删除任务")
    Result<Void> deleteTask(@PathVariable Long id);

    @Operation(summary = "任务详情")
    Result<TaskVO> getTask(@PathVariable Long id);

    @Operation(summary = "分页查询任务")
    Result<PageResult<TaskVO>> pageTasks(@Valid TaskQueryDTO query);

    @Operation(summary = "分配任务")
    Result<Void> assignTask(@PathVariable Long taskId, @RequestBody AssignDTO dto);

    @Operation(summary = "开始任务")
    Result<Void> startTask(@PathVariable Long id);

    @Operation(summary = "完成任务")
    Result<Void> completeTask(@PathVariable Long id, @RequestBody TaskCompletionDTO dto);

    @Operation(summary = "任务工时记录")
    Result<Void> logTime(@PathVariable Long taskId, @Valid @RequestBody TimeLogDTO dto);

    @Operation(summary = "查询任务工时")
    Result<List<TimeLogVO>> listTimeLogs(@PathVariable Long taskId);
}
```

---

### 7. 资产管理模块 (Business-Asset)

#### 7.1 组件结构

```
nexterp-business-asset/
├── asset-api/
│   ├── dto/
│   │   ├── AssetCreateDTO.java
│   │   ├── AssetQueryDTO.java
│   │   └── DepreciationDTO.java
│   ├── vo/
│   │   ├── AssetVO.java
│   │   └── DepreciationVO.java
│   └── facade/
│       └── AssetFacade.java
│
├── asset-domain/
│   ├── model/
│   │   ├── FixedAsset.java
│   │   ├── AssetCategory.java
│   │   ├── DepreciationPeriod.java
│   │   └── AssetTransaction.java
│   ├── service/
│   │   ├── AssetService.java
│   │   └── DepreciationService.java
│   └── event/
│       ├── AssetAcquiredEvent.java
│       ├── AssetDepreciatedEvent.java
│       └── AssetDisposalEvent.java
```

#### 7.2 组件能力定义

##### AssetFacade (资产门面)

```java
package com.nexterp.business.asset.api.facade;

import com.nexterp.business.asset.api.dto.*;
import com.nexterp.business.asset.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 固定资产管理门面接口
 */
public interface AssetFacade {

    // ==================== 资产档案 ====================

    @Operation(summary = "资产入库")
    Result<Long> acquireAsset(@Valid @RequestBody AssetAcquireDTO dto);

    @Operation(summary = "更新资产")
    Result<Void> updateAsset(@PathVariable Long id, @Valid @RequestBody AssetUpdateDTO dto);

    @Operation(summary = "资产报废")
    Result<Void> disposalAsset(@PathVariable Long id, @Valid @RequestBody AssetDisposalDTO dto);

    @Operation(summary = "资产调拨")
    Result<Void> transferAsset(@PathVariable Long id, @Valid @RequestBody AssetTransferDTO dto);

    @Operation(summary = "资产详情")
    Result<AssetVO> getAsset(@PathVariable Long id);

    @Operation(summary = "分页查询资产")
    Result<PageResult<AssetVO>> pageAssets(@Valid AssetQueryDTO query);

    // ==================== 资产分类 ====================

    @Operation(summary = "创建分类")
    Result<Long> createCategory(@Valid @RequestBody CategoryCreateDTO dto);

    @Operation(summary = "分类树")
    Result<List<CategoryTreeVO>> getCategoryTree();

    // ==================== 折旧管理 ====================

    @Operation(summary = "计算折旧")
    Result<Void> calculateDepreciation(@Valid @RequestBody DepreciationCalculateDTO dto);

    @Operation(summary = "批量折旧")
    Result<Void> batchDepreciation(@Valid @RequestBody BatchDepreciationDTO dto);

    @Operation(summary = "折旧历史")
    Result<List<DepreciationVO>> listDepreciations(@PathVariable Long assetId);

    @Operation(summary = "折旧报表")
    Result<byte[]> exportDepreciationReport(@RequestParam String period);

    // ==================== 资产盘点 ====================

    @Operation(summary = "创建盘点单")
    Result<Long> createInventory(@Valid @RequestBody AssetInventoryCreateDTO dto);

    @Operation(summary = "完成盘点")
    Result<Void> completeInventory(@PathVariable Long inventoryId, @Valid @RequestBody InventoryCompletionDTO dto);

    @Operation(summary = "盘点差异报告")
    Result<List<InventoryDifferenceVO>> getDifferenceReport(@PathVariable Long inventoryId);
}
```

---

### 8. 质量管理模块 (Business-Quality)

#### 8.1 组件结构

```
nexterp-business-quality/
├── quality-api/
│   ├── dto/
│   │   ├── InspectionCreateDTO.java
│   │   ├── InspectionExecuteDTO.java
│   │   └── NCRCreateDTO.java
│   ├── vo/
│   │   ├── InspectionVO.java
│   │   └── NCRVO.java
│   └── facade/
│       ├── InspectionFacade.java
│       └── NCRFacade.java
│
├── quality-domain/
│   ├── model/
│   │   ├── InspectionOrder.java
│   │   ├── InspectionItem.java
│   │   ├── NonConformanceReport.java
│   │   └── QualityStandard.java
│   ├── service/
│   │   ├── InspectionService.java
│   │   └── NCRService.java
│   └── event/
│       ├── InspectionCompletedEvent.java
│       └── NCREvent.java
```

#### 8.2 组件能力定义

##### InspectionFacade (检验门面)

```java
package com.nexterp.business.quality.api.facade;

import com.nexterp.business.quality.api.dto.*;
import com.nexterp.business.quality.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 质量检验门面接口
 */
public interface InspectionFacade {

    // ==================== 检验单 ====================

    @Operation(summary = "创建检验单")
    Result<Long> createInspection(@Valid @RequestBody InspectionCreateDTO dto);

    @Operation(summary = "执行检验")
    Result<Void> executeInspection(@PathVariable Long inspectionId, @Valid @RequestBody InspectionExecuteDTO dto);

    @Operation(summary = "审核检验结果")
    Result<Void> reviewInspection(@PathVariable Long inspectionId, @RequestParam Boolean approved);

    @Operation(summary = "检验单详情")
    Result<InspectionVO> getInspection(@PathVariable Long id);

    @Operation(summary = "分页查询检验单")
    Result<PageResult<InspectionVO>> pageInspections(@Valid InspectionQueryDTO query);

    // ==================== 质量标准 ====================

    @Operation(summary = "创建质量标准")
    Result<Long> createStandard(@Valid @RequestBody QualityStandardCreateDTO dto);

    @Operation(summary = "更新质量标准")
    Result<Void> updateStandard(@PathVariable Long id, @Valid @RequestBody QualityStandardUpdateDTO dto);

    @Operation(summary = "查询质量标准")
    Result<List<QualityStandardVO>> listStandards(@RequestParam Long materialId);

    // ==================== 抽样方案 ====================

    @Operation(summary = "创建抽样方案")
    Result<Long> createSamplingPlan(@Valid @RequestBody SamplingPlanCreateDTO dto);

    @Operation(summary = "查询抽样方案")
    Result<SamplingPlanVO> getSamplingPlan(@PathVariable Long planId);
}
```

##### NCRFacade (不合格品门面)

```java
package com.nexterp.business.quality.api.facade;

import com.nexterp.business.quality.api.dto.*;
import com.nexterp.business.quality.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 不合格品处理门面接口
 */
public interface NCRFacade {

    @Operation(summary = "创建NCR")
    Result<Long> createNCR(@Valid @RequestBody NCRCreateDTO dto);

    @Operation(summary = "NCR原因分析")
    Result<Void> rootCauseAnalysis(@PathVariable Long ncrId, @Valid @RequestBody RootCauseAnalysisDTO dto);

    @Operation(summary = "NMR处理方案")
    Result<Void> disposition(@PathVariable Long ncrId, @Valid @RequestBody DispositionDTO dto);

    @Operation(summary = "关闭NCR")
    Result<Void> closeNCR(@PathVariable Long ncrId);

    @Operation(summary = "NCR详情")
    Result<NCRVO> getNCR(@PathVariable Long id);

    @Operation(summary = "分页查询NCR")
    Result<PageResult<NCRVO>> pageNCRs(@Valid NCRQueryDTO query);

    @Operation(summary = "NMR统计")
    Result<NCRStatisticsVO> getStatistics(@RequestParam String period);
}
```

---

### 9. 补充销售模块完整能力

#### DeliveryFacade (发货门面)

```java
package com.nexterp.business.sales.api.facade;

import com.nexterp.business.sales.api.dto.delivery.*;
import com.nexterp.business.sales.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 发货管理门面接口
 */
public interface DeliveryFacade {

    @Operation(summary = "创建发货单")
    Result<Long> createDelivery(@Valid @RequestBody DeliveryCreateDTO dto);

    @Operation(summary = "更新发货单")
    Result<Void> updateDelivery(@PathVariable Long id, @Valid @RequestBody DeliveryUpdateDTO dto);

    @Operation(summary = "删除发货单")
    Result<Void> deleteDelivery(@PathVariable Long id);

    @Operation(summary = "发货单详情")
    Result<DeliveryVO> getDelivery(@PathVariable Long id);

    @Operation(summary = "分页查询发货单")
    Result<PageResult<DeliveryVO>> pageDeliveries(@Valid DeliveryQueryDTO query);

    @Operation(summary = "发货确认")
    Result<Void> confirmDelivery(@PathVariable Long id, @Valid @RequestBody DeliveryConfirmDTO dto);

    @Operation(summary = "签收确认")
    Result<Void> signReceipt(@PathVariable Long id, @Valid @RequestBody SignReceiptDTO dto);

    @Operation(summary = "退货处理")
    Result<Long> createReturn(@Valid @RequestBody SalesReturnCreateDTO dto);
}
```

#### CustomerFacade (客户门面)

```java
package com.nexterp.business.sales.api.facade;

import com.nexterp.business.sales.api.dto.customer.*;
import com.nexterp.business.sales.api.vo.*;
import com.nexterp.shared.web.PageResult;
import com.nexterp.shared.web.Result;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 客户管理门面接口
 */
public interface CustomerFacade {

    @Operation(summary = "创建客户")
    Result<Long> createCustomer(@Valid @RequestBody CustomerCreateDTO dto);

    @Operation(summary = "更新客户")
    Result<Void> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerUpdateDTO dto);

    @Operation(summary = "删除客户")
    Result<Void> deleteCustomer(@PathVariable Long id);

    @Operation(summary = "客户详情")
    Result<CustomerVO> getCustomer(@PathVariable Long id);

    @Operation(summary = "分页查询客户")
    Result<PageResult<CustomerVO>> pageCustomers(@Valid CustomerQueryDTO query);

    @Operation(summary = "设置信用额度")
    Result<Void> setCreditLimit(@PathVariable Long customerId, @RequestParam BigDecimal creditLimit);

    @Operation(summary = "查询客户余额")
    Result<CustomerBalanceVO> getBalance(@PathVariable Long customerId);

    @Operation(summary = "客户收货地址")
    Result<List<AddressVO>> listAddresses(@PathVariable Long customerId);

    @Operation(summary = "添加收货地址")
    Result<Long> addAddress(@PathVariable Long customerId, @Valid @RequestBody AddressCreateDTO dto);

    @Operation(summary = "客户联系人")
    Result<List<ContactVO>> listContacts(@PathVariable Long customerId);
}
```

---

### 10. 补充供应链模块事件接口

```java
// ==================== 供应链模块事件 ====================

/**
 * 采购订单创建事件
 */
public record PurchaseOrderCreatedEvent(
    Long tenantId,
    Long orderId,
    String orderNo,
    Long supplierId,
    BigDecimal amount,
    LocalDateTime createTime
) {}

/**
 * 采购订单审核事件
 */
public record PurchaseOrderApprovedEvent(
    Long tenantId,
    Long orderId,
    String orderNo,
    Boolean approved,
    LocalDateTime approveTime
) {}

/**
 * 入库事件
 */
public record StockInEvent(
    Long tenantId,
    String materialCode,
    String warehouseCode,
    BigDecimal quantity,
    String transactionType,
    LocalDateTime transactionTime
) {}

/**
 * 出库事件
 */
public record StockOutEvent(
    Long tenantId,
    String materialCode,
    String warehouseCode,
    BigDecimal quantity,
    String transactionType,
    LocalDateTime transactionTime
) {}

/**
 * 库存预警事件
 */
public record InventoryAlertEvent(
    Long tenantId,
    String materialCode,
    String warehouseCode,
    BigDecimal currentQty,
    BigDecimal safetyStock,
    String alertType,
    LocalDateTime alertTime
) {}
```

---

### 11. 补充生产模块事件接口

```java
// ==================== 生产模块事件 ====================

/**
 * 生产订单创建事件
 */
public record ProductionOrderCreatedEvent(
    Long tenantId,
    Long orderId,
    String orderNo,
    Long productId,
    BigDecimal quantity,
    LocalDateTime planStartTime,
    LocalDateTime createTime
) {}

/**
 * 生产订单完成事件
 */
public record ProductionOrderCompletedEvent(
    Long tenantId,
    Long orderId,
    String orderNo,
    Long productId,
    BigDecimal completedQty,
    LocalDateTime completeTime
) {}

/**
 * 领料事件
 */
public record MaterialIssueEvent(
    Long tenantId,
    Long orderId,
    String materialCode,
    BigDecimal quantity,
    LocalDateTime issueTime
) {}

/**
 * 工序完成事件
 */
public record OperationCompletedEvent(
    Long tenantId,
    Long orderId,
    Long operationId,
    String operationCode,
    LocalDateTime completeTime
) {}
```

---

## 组件间接口规范

### 1. 模块间调用接口 (API 层)

#### 接口定义规范

```java
package com.nexterp.{module}.api;

/**
 * 模块对外接口
 * 定义模块提供的核心能力
 */
public interface {Module}Service {

    // 接口方法必须包含:
    // 1. 清晰的方法名
    // 2. 参数验证
    // 3. 返回值
    // 4. 异常说明
}
```

#### 示例：财务模块对外接口

```java
package com.nexterp.business.finance.api;

import com.nexterp.business.finance.api.dto.*;
import com.nexterp.business.finance.api.vo.*;
import java.util.List;

/**
 * 财务模块对外接口
 * 供其他模块（销售、采购等）调用
 */
public interface FinanceService {

    /**
     * 生成应收账款凭证
     * @param request 应收凭证请求
     * @return 凭证ID
     */
    Long createReceivableVoucher(ReceivableVoucherRequest request);

    /**
     * 生成应付账款凭证
     * @param request 应付凭证请求
     * @return 凭证ID
     */
    Long createPayableVoucher(PayableVoucherRequest request);

    /**
     * 查询科目余额
     * @param accountCode 科目编码
     * @param period 期间
     * @return 余额
     */
    BigDecimal getAccountBalance(String accountCode, String period);

    /**
     * 获取客户信用额度
     * @param customerId 客户ID
     * @return 信用额度
     */
    CreditInfoVO getCustomerCredit(Long customerId);
}
```

### 2. 模块扩展接口 (SPI 层)

```java
package com.nexterp.{module}.spi;

/**
 * 模块扩展接口
 * 允许外部扩展模块能力
 */
public interface {Module}Extension {

    /**
     * 扩展点方法
     */
    void execute(ExtensionContext context);
}
```

### 3. 事件驱动接口

```java
package com.nexterp.{module}.event;

/**
 * 领域事件
 * 使用 Spring Event 机制实现模块解耦
 */
public record {Domain}Event(
    // 事件数据
) {}
```

---

## 模块间集成关系设计

### 模块依赖关系图

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      NextERP 模块集成关系                               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                        平台层 (Platform)                             │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │  │
│  │  │   Auth   │ │  Tenant  │ │ Workflow │ │  Report  │ │  Notify  │ │  │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ │  │
│  │       │            │            │            │            │       │  │
│  │       └────────────┴────────────┴────────────┴────────────┴───────┘  │  │
│  │                              │                                   │  │
│  └──────────────────────────────┼───────────────────────────────────┘  │
│                                  ▼                                   │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                      业务层 (Business)                             │  │
│  │                                                                  │  │
│  │  ┌──────────────┐     ┌──────────────┐                           │  │
│  │  │   Finance    │◄──►│    Sales     │                           │  │
│  │  │   (财务)     │     │   (销售)     │                           │  │
│  │  └──────┬───────┘     └──────┬───────┘                           │  │
│  │         │                     │                                   │  │
│  │         │              ┌──────┴───────┐                           │  │
│  │         ▼              ┌───┴────────────┴──┐                        │  │
│  │  ┌──────────────┐  ┌──┴──────────┐  ┌─────┴──────────┐              │  │
│  │  │   Supply     │  │ Production │  │     HRM       │              │  │
│  │  │  (供应链)    │  │  (生产)   │  │   (人力)      │              │  │
│  │  └──────┬───────┘  └───┬─────────┘  └─────┬──────────┘              │  │
│  │         │              │                   │                         │  │
│  │         └──────────────┴───────────────────┘                         │  │
│  │                                                                  │  │
│  │  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐        │  │
│  │  │   Project    │     │    Asset     │     │   Quality     │        │  │
│  │  │  (项目)      │     │  (资产)      │     │  (质量)      │        │  │
│  │  └──────┬───────┘     └──────┬───────┘     └──────┬───────┘        │  │
│  │         └──────────────┴────────────┴────────────┘                 │  │
│  └───────────────────────────────────────────────────────────────────┘  │
│                                                                         │
│  集成关系说明:                                                         │
│  ───────────► 同步调用 (API 接口)                                   │
│  ──────────► 异步事件 (领域事件)                                    │
│  ──────────► 数据共享 (共享数据库)                                  │
└─────────────────────────────────────────────────────────────────────────┘
```

### 核心集成点定义

#### 1. 财务模块集成点

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    财务模块 (Finance) 集成点                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  【销售模块】                                                           │
│  ├─ 销售订单创建 → 自动生成应收账款凭证                                │
│  ├─ 销售退货 → 自动生成红字冲销凭证                                    │
│  └─ 客户信用额度查询 → 实时控制信用风险                                 │
│                                                                         │
│  【供应链模块】                                                         │
│  ├─ 采购订单审核 → 自动生成应付账款凭证                                │
│  ├─ 采购入库 → 自动生成库存凭证/材料凭证                                │
│  └─ 采购退货 → 自动生成红字冲销凭证                                    │
│                                                                         │
│  【人力资源模块】                                                       │
│  ├─ 薪酬计算完成 → 自动生成应付职工薪酬凭证                            │
│  ├─ 社保公积金计提 → 自动生成相应凭证                                   │
│  └─ 员工借款/报销 → 生成其他应收/费用凭证                               │
│                                                                         │
│  【资产管理模块】                                                       │
│  ├─ 资产入库 → 自动生成固定资产凭证                                    │
│  ├─ 资产折旧 → 月末自动生成折旧凭证                                      │
│  └─ 资产报废 → 自动生成资产处置损益凭证                                 │
│                                                                         │
│  【项目管理模块】                                                       │
│  ├─ 项目成本归集 → 自动归集项目成本                                   │
│  ├─ 项目收入确认 → 按进度确认收入                                     │
│  └─ 项目结算 → 生成项目结算凭证                                       │
│                                                                         │
│  【质量管理模块】                                                       │
│  ├─ 质检异常处理 → 生成质量损失/供应商索赔凭证                          │
│  └─ 不合格品处理 → 生成相应损失凭证                                     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 集成接口详细定义

#### 财务模块对外集成接口

```java
package com.nexterp.business.finance.api;

/**
 * 财务模块对外集成接口
 * 提供给其他模块调用的核心能力
 */
public interface FinanceIntegrationService {

    // ==================== 应收应付接口 ====================

    /**
     * 生成应收账款凭证
     * @param request 应收凭证请求
     * @return 凭证ID
     */
    Long createReceivableVoucher(ReceivableVoucherRequest request);

    /**
     * 生成应付账款凭证
     * @param request 应付凭证请求
     * @return 凭证ID
     */
    Long createPayableVoucher(PayableVoucherRequest request);

    /**
     * 应收账款核销
     */
    void writeOffReceivable(Long receivableId, BigDecimal amount, String businessType);

    // ==================== 成本核算接口 ====================

    /**
     * 归集产品成本
     */
    void collectProductCost(Long productId, BigDecimal quantity, BigDecimal costAmount);

    /**
     * 归集项目成本
     */
    void collectProjectCost(Long projectId, String costItem, BigDecimal amount);

    // ==================== 查询接口 ====================

    /**
     * 查询科目余额
     */
    BigDecimal getAccountBalance(String accountCode, String period);

    /**
     * 查询客户信用额度
     */
    CreditInfoVO getCustomerCredit(Long customerId);

    /**
     * 查询供应商额度
     */
    SupplierQuotaVO getSupplierQuota(Long supplierId);
}
```

#### 供应链模块对外集成接口

```java
package com.nexterp.business.supply.api;

/**
 * 供应链模块对外集成接口
 */
public interface SupplyIntegrationService {

    // ==================== 库存操作接口 ====================

    /**
     * ATP可用量查询
     */
    BigDecimal queryAvailableQuantity(String materialCode, BigDecimal quantity, LocalDate date);

    /**
     * 库存预留
     */
    Long reserveInventory(String materialCode, BigDecimal quantity, Long referenceId);

    /**
     * 库存释放
     */
    void releaseInventory(Long reservationId);

    /**
     * 出库
     */
    Long stockOut(StockOutRequest request);

    /**
     * 入库
     */
    Long stockIn(StockInRequest request);

    // ==================== 供应商接口 ====================

    SupplierQuotaVO getSupplierQuota(Long supplierId);
    void updateSupplierQuota(Long supplierId, BigDecimal amount);
}
```

#### 销售模块对外集成接口

```java
package com.nexterp.business.sales.api;

/**
 * 销售模块对外集成接口
 */
public interface SalesIntegrationService {

    SalesOrderVO getSalesOrder(String orderNo);
    void updateOrderStatus(Long orderId, String status);
    PriceInfoVO getProductPrice(Long customerId, Long materialId, BigDecimal quantity, LocalDate orderDate);
    DiscountVO getDiscountPolicy(Long customerId, BigDecimal amount);
    CustomerVO getCustomer(Long customerId);
    CustomerBalanceVO getCustomerBalance(Long customerId);
}
```

### 集成场景详细设计

#### 场景1：销售订单到财务凭证

```java
// 销售模块发布事件
public record SalesOrderCreatedEvent(
    Long orderId,
    String orderNo,
    Long customerId,
    BigDecimal orderAmount,
    List<OrderLineItem> lineItems
) {}

// 财务模块订阅并处理
@Component
@RequiredArgsConstructor
public class SalesOrderFinanceListener {

    @EventListener
    @Async("financeEventExecutor")
    public void handleSalesOrderCreated(SalesOrderCreatedEvent event) {
        // 1. 查询客户信用额度
        CreditInfoVO creditInfo = financeService.getCustomerCredit(event.customerId());

        // 2. 检查信用额度
        if (event.orderAmount.compareTo(creditInfo.getAvailableCredit()) > 0) {
            notificationService.sendCreditAlert(event);
        }

        // 3. 生成应收账款凭证
        ReceivableVoucherRequest request = ReceivableVoucherRequest.builder()
            .orderNo(event.orderNo())
            .customerId(event.customerId())
            .amount(event.orderAmount())
            .build();

        Long voucherId = financeService.createReceivableVoucher(request);

        // 4. 更新订单状态
        salesService.updateOrderStatus(event.orderId(), "PENDING_PAYMENT");
    }
}
```

### 集成异常处理机制

```java
/**
 * 集成异常处理器
 */
@Component
public class IntegrationExceptionHandler {

    @EventListener
    public void handleIntegrationException(IntegrationExceptionEvent event) {
        // 1. 记录异常日志
        integrationErrorLog.save(event);

        // 2. 发送告警通知
        alertService.sendIntegrationAlert(event);

        // 3. 判断是否需要重试
        if (isRetryable(event.getErrorType())) {
            retryQueue.offer(event);
        }

        // 4. 判断是否需要补偿
        if (event.isNeedCompensation()) {
            compensationService.compensate(event);
        }
    }
}
```

---

## 跨模块业务流程示例

### 1. 销售订单到财务凭证流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    销售订单 → 财务凭证 流程                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐     发布事件      ┌──────────────┐                   │
│  │ 销售模块     │ ────────────────► │ 财务模块     │                   │
│  │ SalesOrder   │   SalesOrder      │ Finance      │                   │
│  │ Service      │   CreatedEvent    │ Listener     │                   │
│  └──────────────┘                   └──────────────┘                   │
│        │                                    │                           │
│        │ 1. 创建订单                         │                           │
│        │ 2. 发布 SalesOrderCreatedEvent     │                           │
│        │                                    │                           │
│        │                                    │ 3. 监听事件               │
│        │                                    │ 4. 生成应收账款凭证       │
│        │                                    │ 5. 发布 VoucherCreatedEvent│
│        │                                    │                           │
│        ▼                                    ▼                           │
│  ┌──────────────┐                   ┌──────────────┐                   │
│  │ 订单状态     │                   │ 凭证状态     │                   │
│  │ 待发货       │                   │ 已过账       │                   │
│  └──────────────┘                   └──────────────┘                   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**事件定义：**

```java
// 销售模块发布
package com.nexterp.business.sales.event;

public record SalesOrderCreatedEvent(
    Long orderId,
    String orderNo,
    Long customerId,
    BigDecimal amount,
    LocalDateTime createTime
) {}

// 财务模块监听
package com.nexterp.business.finance.application.listener;

@Component
@RequiredArgsConstructor
public class SalesOrderListener {

    private final FinanceService financeService;

    @EventListener
    @Async("financeEventExecutor")
    public void handleSalesOrderCreated(SalesOrderCreatedEvent event) {
        // 自动生成应收账款凭证
        financeService.createReceivableVoucher(
            ReceivableVoucherRequest.builder()
                .orderNo(event.orderNo())
                .customerId(event.customerId())
                .amount(event.amount())
                .build()
        );
    }
}
```

### 2. 采购入库到库存更新流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    采购入库 → 库存更新 → 财务凭证                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐           │
│  │ 采购模块     │────►│ 供应链模块   │────►│ 财务模块     │           │
│  │ Purchase     │     │ Supply       │     │ Finance      │           │
│  └──────────────┘     └──────────────┘     └──────────────┘           │
│        │                    │                    │                     │
│        │ 1. 创建采购订单    │                    │                     │
│        │ 2. 发布事件        │                    │                     │
│        │                    │                    │                     │
│        │                    │ 3. 执行入库        │                     │
│        │                    │ 4. 更新库存        │                     │
│        │                    │ 5. 发布事件        │                     │
│        │                    │                    │                     │
│        │                    │                    │ 6. 生成库存凭证      │
│        │                    │                    │ 7. 生成应付凭证      │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 3. 生产订单完整流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│              生产订单 → 领料 → 生产 → 入库 → 成本核算                   │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐           │
│  │ 生产模块     │────►│ 供应链模块   │────►│ 财务模块     │           │
│  │ Production   │     │ Supply       │     │ Finance      │           │
│  └──────────────┘     └──────────────┘     └──────────────┘           │
│        │                    │                    │                     │
│        │ 1. 下达生产订单    │                    │                     │
│        │ 2. 发布事件        │                    │                     │
│        │                    │                    │                     │
│        │                    │ 3. 生产领料        │                     │
│        │                    │ 4. 更新库存        │                     │
│        │                    │ 5. 发布事件        │                     │
│        │                    │                    │                     │
│        │ 6. 完工生产        │                    │                     │
│        │ 7. 产品入库        │                    │                     │
│        │ 8. 发布事件        │                    │                     │
│        │                    │                    │                     │
│        │                    │                    │ 9. 计算成本          │
│        │                    │                    │ 10. 生成凭证         │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

**事件流程：**

```java
// 1. 生产订单创建
ProductionOrderCreatedEvent → 库存模块预留材料

// 2. 生产领料
MaterialIssueEvent → 库存模块扣减材料

// 3. 生产完工
ProductionOrderCompletedEvent → 库存模块增加产品 → 财务模块计算成本
```

### 4. 员工入职到薪酬计算流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│              员工入职 → 考勤 → 薪酬计算 → 财务凭证                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐           │
│  │ 人力模块     │────►│ 财务模块     │────►│ 银行接口     │           │
│  │ HRM          │     │ Finance      │     │ Bank API     │           │
│  └──────────────┘     └──────────────┘     └──────────────┘           │
│        │                    │                    │                     │
│        │ 1. 员工入职        │                    │                     │
│        │ 2. 创建员工档案   │                    │                     │
│        │ 3. 发布事件        │                    │                     │
│        │                    │                    │                     │
│        │ 4. 考勤记录        │                    │                     │
│        │ 5. 请假/加班       │                    │                     │
│        │                    │                    │                     │
│        │ 6. 薪酬计算        │                    │                     │
│        │ 7. 发布事件        │                    │                     │
│        │                    │                    │                     │
│        │                    │ 8. 生成应付凭证   │                     │
│        │                    │ 9. 银行代发       │                     │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 5. 资产全生命周期管理流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│           资产采购 → 入库 → 折旧 → 维护 → 报废 → 财务处理               │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐           │
│  │ 采购模块     │────►│ 资产模块     │────►│ 财务模块     │           │
│  │ Purchase     │     │ Asset        │     │ Finance      │           │
│  └──────────────┘     └──────────────┘     └──────────────┘           │
│        │                    │                    │                     │
│        │ 1. 资产采购申请   │                    │                     │
│        │                    │                    │                     │
│        │                    │ 2. 资产入库        │                     │
│        │                    │ 3. 生成资产卡片    │                     │
│        │                    │ 4. 发布事件        │                     │
│        │                    │                    │                     │
│        │                    │ 5. 月度折旧        │                     │
│        │                    │ 6. 发布事件        │                     │
│        │                    │                    │                     │
│        │                    │ 7. 资产维护        │                     │
│        │                    │ 8. 资产报废        │                     │
│        │                    │ 9. 发布事件        │                     │
│        │                    │                    │                     │
│        │                    │                    │ 10. 生成折旧凭证    │
│        │                    │                    │ 11. 生成报废凭证    │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6. 质量检验异常处理流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│              质检异常 → NCR创建 → 原因分析 → 处理 → 财务处理             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐           │
│  │ 质量模块     │────►│ 供应链模块   │────►│ 财务模块     │           │
│  │ Quality      │     │ Supply       │     │ Finance      │           │
│  └──────────────┘     └──────────────┘     └──────────────┘           │
│        │                    │                    │                     │
│        │ 1. 质检发现异常   │                    │                     │
│        │ 2. 创建NCR        │                    │                     │
│        │ 3. 发布事件        │                    │                     │
│        │                    │                    │                     │
│        │                    │ 4. 原因分析        │                     │
│        │                    │ 5. 处理方案        │                     │
│        │                    │  - 退货           │                     │
│        │                    │  - 返工           │                     │
│        │                    │  - 报废           │                     │
│        │                    │ 6. 发布事件        │                     │
│        │                    │                    │                     │
│        │                    │                    │ 7. 生成损失凭证    │
│        │                    │                    │ 8. 供应商索赔       │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 7. 项目成本归集流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│              项目创建 → 任务分配 → 工时记录 → 成本归集 → 财务结算         │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐           │
│  │ 项目模块     │────►│ 人力模块     │────►│ 财务模块     │           │
│  │ Project      │     │ HRM          │     │ Finance      │           │
│  └──────────────┘     └──────────────┘     └──────────────┘           │
│        │                    │                    │                     │
│        │ 1. 创建项目        │                    │                     │
│        │ 2. 分配任务        │                    │                     │
│        │ 3. 分配资源        │                    │                     │
│        │                    │                    │                     │
│        │                    │ 4. 员工报工        │                     │
│        │                    │ 5. 工时统计        │                     │
│        │                    │ 6. 发布事件        │                     │
│        │                    │                    │                     │
│        │ 7. 成本归集        │                    │                     │
│        │ 8. 进度跟踪        │                    │                     │
│        │ 9. 项目完成        │                    │                     │
│        │                    │                    │                     │
│        │                    │                    │ 10. 生成项目成本    │
│        │                    │                    │ 11. 收入确认        │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 附录

### A. 接口命名规范

| 层级 | 命名规范 | 示例 |
|------|---------|------|
| **Facade** | {功能}Facade | VoucherFacade, AccountFacade |
| **ApplicationService** | {功能}ApplicationService | VoucherApplicationService |
| **DomainService** | {功能}Service | PasswordService, TokenService |
| **Repository** | {实体}Repository | VoucherRepository, UserRepository |
| **Event** | {动作}Event | UserLoggedInEvent, VoucherPostedEvent |

### B. DTO 命名规范

| 类型 | 命名规范 | 示例 |
|------|---------|------|
| **创建** | {实体}CreateDTO | VoucherCreateDTO |
| **更新** | {实体}UpdateDTO | VoucherUpdateDTO |
| **查询** | {实体}QueryDTO | VoucherQueryDTO |
| **响应** | {实体}VO | VoucherVO |
| **列表** | {实体}ListVO | VoucherListVO |
| **树形** | {实体}TreeVO | AccountTreeVO |

### C. 组件依赖规则

1. **禁止反向依赖**：
   - 业务模块不能依赖平台模块的实现
   - 只能通过 API 接口调用

2. **模块间通信**：
   - 同步调用：使用 API 接口
   - 异步解耦：使用领域事件

3. **共享组件**：
   - 所有模块可依赖 shared 层
   - shared 层不能依赖任何业务模块

### D. 版本控制

组件接口版本号遵循语义化版本规范：
- **MAJOR.MINOR.PATCH**
- 不兼容变更递增 MAJOR
- 向后兼容功能新增递增 MINOR
- 向后兼容问题修复递增 PATCH

示例：`v1.2.3`
- 1：主版本号
- 2：次版本号
- 3：修订号
