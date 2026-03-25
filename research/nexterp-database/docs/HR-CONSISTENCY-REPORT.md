# HR 模块一致性检查报告

**检查日期**: 2026-03-14
**检查范围**: 07-HR-DESIGN.md vs 其他模块设计文档
**状态**: ✅ 已修复

---

## 1. 检查结果摘要

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 枚举类型一致性 | ⚠️ 部分通过 | HR定义了独有枚举，建议移至CORE |
| 外键引用一致性 | ✅ 通过 | 所有外键引用正确 |
| ~~循环依赖~~ | ✅ **已修复** | 采用逻辑关联方案 |
| 标准字段一致性 | ✅ 通过 | 符合设计规范 |
| 命名规范一致性 | ✅ 通过 | 符合命名规范 |
| 时间有效性模式 | ✅ 通过 | InfoType架构实现正确 |

---

## 2. 循环依赖问题 - 已修复 ✅

### 2.1 原问题描述

```
00-CORE/sys_user.employee_id → 07-HR/hr_employee.id  (物理外键)
07-HR/hr_employee.created_by → 00-CORE/sys_user.id  (隐式引用)

导致模块加载顺序无法确定，形成循环依赖。
```

### 2.2 解决方案 (已实施)

采用 **方案D: 分离身份与业务**，参考 SAP USR02 + PA0105 的设计：

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          修复后的架构                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  00-CORE                                                                    │
│      │                                                                      │
│      ├── auth_user (纯认证)                                                 │
│      │       └── 不引用任何业务表                                            │
│      │                                                                      │
│      └── sys_user (业务用户)                                                │
│              │                                                              │
│              │ auth_user_id ──► auth_user.id (物理外键)                     │
│              │                                                              │
│              │ employee_number VARCHAR(8)  -- 逻辑关联，无物理外键           │
│              │                                                              │
│              └────────────────────┐                                        │
│                                   │ 值相等                                  │
│                                   ▼                                        │
│  07-HR                             hr_employee.employee_number             │
│      │                                                                      │
│      └── hr_employee (员工主数据)                                           │
│              │                                                              │
│              └── created_by, updated_by (UUID，无外键约束)                  │
│                                                                             │
│  ✅ 无物理外键循环                                                          │
│  ✅ sys_user 在 CORE，其他模块可用                                          │
│  ✅ HR 模块可选                                                             │
│  ✅ 符合 SAP 设计哲学                                                        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.3 表结构变更

**新增 auth_user (CORE 模块)**:
```sql
CREATE TABLE auth_user (
    id              UUID PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password_hash   VARCHAR(255),
    email           VARCHAR(100) UNIQUE,
    ...
    -- 不引用任何业务表
);
```

**修改 sys_user (CORE 模块)**:
```sql
CREATE TABLE sys_user (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    auth_user_id    UUID NOT NULL REFERENCES auth_user(id),  -- 新增

    -- 移除: employee_id UUID REFERENCES hr_employee(id)
    -- 改为逻辑关联:
    employee_number VARCHAR(8),  -- 对应 hr_employee.employee_number

    ...
);
```

**hr_employee (HR 模块) - 无变更**:
```sql
CREATE TABLE hr_employee (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    employee_number VARCHAR(8) NOT NULL,  -- 与 sys_user.employee_number 匹配

    -- 审计字段无物理外键
    created_by      UUID,  -- 逻辑关联 sys_user.id
    updated_by      UUID,

    ...
);
```

---

## 3. 模块依赖关系 (最终版本)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          模块依赖关系图                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  00-CORE                                                                    │
│      │                                                                      │
│      ├── auth_user (认证用户)                                               │
│      │       └── 无外部依赖 ✅                                              │
│      │                                                                      │
│      └── sys_user (业务用户)                                                │
│              │                                                              │
│              ├── auth_user_id → auth_user.id (物理外键)                    │
│              └── employee_number → hr_employee.employee_number (逻辑)      │
│                                                                             │
│      │                                                                      │
│      ▼                                                                      │
│  01-TENANT                                                                  │
│      │                                                                      │
│      ├── sys_tenant                                                         │
│      ├── sys_company                                                        │
│      ├── sys_cost_center                                                    │
│      └── sys_profit_center                                                  │
│                                                                             │
│      │                                                                      │
│      ▼                                                                      │
│  02-BP (业务伙伴)                                                           │
│      │                                                                      │
│      └── bp_business_partner                                                │
│                                                                             │
│      │                                                                      │
│      ▼                                                                      │
│  07-HR (人力资源)                                                           │
│      │                                                                      │
│      ├── hr_employee                                                        │
│      │       └── employee_number (与 sys_user 逻辑关联)                    │
│      │                                                                      │
│      └── 其他 InfoType 表                                                   │
│                                                                             │
│      │                                                                      │
│      ▼                                                                      │
│  03-FI-CO, 04-MM, 05-SD 等其他业务模块                                      │
│                                                                             │
│  加载顺序: auth_user → sys_user → sys_tenant → ... → hr_employee           │
│                                                                             │
│  ✅ 无循环依赖                                                              │
│  ✅ 所有模块都可以引用 sys_user                                             │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. 待优化项 (低优先级)

### 4.1 枚举类型位置

HR 模块定义了特有的枚举类型，建议后续统一到 CORE：

| 枚举类型 | 当前位置 | 建议 |
|----------|----------|------|
| hr_om_object_type | HR | 可保留 (HR特有) |
| hr_om_relation_type | HR | 可保留 (HR特有) |
| hr_om_plan_version | HR | 可保留 (HR特有) |

### 4.2 部分表缺少 updated_by

以下表可以补充 `updated_by` 字段：

- hr_it0000_actions
- hr_it0008_wage_item
- hr_payroll_item
- hr_candidate_application
- hr_interview_schedule

---

## 5. 验证清单

| 检查项 | 验证SQL | 状态 |
|--------|---------|------|
| auth_user 无业务表引用 | 检查 REFERENCES | ✅ |
| sys_user → auth_user 物理外键 | 检查 REFERENCES | ✅ |
| sys_user 无 hr_employee 外键 | 检查 REFERENCES | ✅ |
| hr_employee 无 sys_user 外键 | 检查 REFERENCES | ✅ |
| 员工-用户可逻辑关联 | JOIN on employee_number | ✅ |

---

## 6. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 - 发现循环依赖 |
| 1.1 | 2026-03-14 | 修复循环依赖，采用 auth_user + sys_user 分离方案 |
