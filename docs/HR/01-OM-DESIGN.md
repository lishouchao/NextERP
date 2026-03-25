# OM 组织管理功能设计

**模块**: Organization Management (组织管理)
**对标**: SAP HRP1000/HRP1001 (PD Org Management)
**版本**: 2.0
**更新日期**: 2026-03-14

---

## 1. 模块概述

### 1.1 功能范围

组织管理 (OM) 模块提供完整的组织架构管理能力，包括：

- **组织单元 (Organizational Unit)** - 公司、部门、团队等组织实体
- **职位 (Position)** - 具体的工作岗位
- **职务 (Job)** - 抽象的工作职能
- **任务 (Task)** - 工作任务和职责
- **关系管理** - 组织元素之间的关联关系

### 1.2 SAP HRP1000/HRP1001 对标

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    OM 对象模型 - 对标 SAP HRP1000                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  对象类型 (OTYPE)                                                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│  │    O     │  │    S     │  │    C     │  │    T     │  │    P     │     │
│  │ 组织单元 │  │   职位   │  │   职务   │  │   任务   │  │   人员   │     │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘  └──────────┘     │
│       │              │              │              │              │         │
│       │              │              │              │              │         │
│       ▼              ▼              ▼              ▼              ▼         │
│  ┌──────────────────────────────────────────────────────────────────┐      │
│  │                    HRP1001 关系表 (hr_om_relationship)            │      │
│  │                                                                   │      │
│  │   关系类型 (RELAT):                                               │      │
│  │   • 002 - belongs to (隶属于)                                     │      │
│  │   • 003 - includes (包含)                                         │      │
│  │   • 008 - holder of (持有者)                                      │      │
│  │   • 009 - line supervisor (直线汇报)                              │      │
│  │   • 011 - spec. of position (职位专指)                            │      │
│  │   • 012 - has task (拥有任务)                                     │      │
│  │   • 013 - person to position (人员到职位)                         │      │
│  │   • 020 - matrix supervisor (矩阵汇报)                            │      │
│  │   • 030 - has qualification (拥有资格)                            │      │
│  │   • 040 - successor (继任者)                                      │      │
│  └──────────────────────────────────────────────────────────────────┘      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 对象类型定义

### 2.1 OM 对象类型枚举 (对标 SAP OTYPE)

| 代码 | 名称 | 说明 | SAP 对应 |
|------|------|------|----------|
| O | Organizational Unit | 组织单元 (公司/部门/团队) | O |
| S | Position | 职位 (具体岗位) | S |
| C | Job | 职务 (抽象职能) | C |
| T | Task | 任务 (工作内容) | T |
| K | Cost Center | 成本中心 | K |
| Q | Qualification | 资格/技能 | Q |
| US | User | 系统用户 | US |
| P | Person | 人员 (员工) | P |

### 2.2 OM 关系类型枚举 (对标 SAP RELAT)

| 代码 | 名称 | A→B 方向说明 | 典型应用 |
|------|------|--------------|----------|
| 002 | belongs to | 组织单元 → 上级组织 | 部门归属公司 |
| 003 | includes | 组织单元 → 下级职位 | 部门包含职位 |
| 004 | is holder of | 人员 → 职位 | 人员任职 |
| 007 | describes | 职务 → 任务 | 职务描述 |
| 008 | holder of | 职位 → 人员 | 职位持有人 |
| 009 | line supervisor | 组织/职位 → 下级 | 直线汇报 |
| 010 | cost center assign | 对象 → 成本中心 | 成本中心分配 |
| 011 | spec. of position | 职位 → 职务 | 职位对应的职务 |
| 012 | has task | 职位/职务 → 任务 | 工作任务分配 |
| 013 | person to position | 人员 → 职位 | 人员职位关联 |
| 014 | subordinate org | 组织 → 下级组织 | 组织层级 |
| 015 | org to cost center | 组织 → 成本中心 | 组织成本中心 |
| 020 | matrix supervisor | 职位 → 汇报对象 | 矩阵汇报 |
| 030 | has qualification | 人员 → 资格 | 人员资格 |
| 031 | requires qualif. | 职位 → 资格 | 职位要求 |
| 040 | successor | 人员 → 职位 | 继任计划 |
| 045 | substitute | 人员 → 人员 | 代理人 |
| A/B | A/B relationship | 双向关系 | 灵活关联 |

### 2.3 规划版本 (对标 SAP PLVAR)

| 代码 | 名称 | 说明 |
|------|------|------|
| 01 | Current Plan | 当前活动计划 |
| 02 | Organizational Plan | 组织规划 |
| 03 | Test Plan | 测试计划 |
| 99 | Archive | 归档版本 |

---

## 3. 组织单元 (Organizational Unit)

### 3.1 功能说明

组织单元代表企业组织架构中的节点，可以是：
- 公司/法人实体
- 业务部门
- 职能部门
- 团队/小组

### 3.2 业务规则

1. **层级结构**: 组织单元支持无限层级
2. **时间有效性**: 支持组织变革的历史追溯和未来规划
3. **编制管理**: 每个组织单元可设置编制上限
4. **成本中心关联**: 可关联成本中心用于成本归集

### 3.3 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| org_code | VARCHAR(12) | 组织编码 |
| parent_object_id | UUID | 上级组织 |
| org_category | VARCHAR(2) | 组织分类 |
| org_level | INTEGER | 层级深度 |
| path | VARCHAR(500) | 层级路径 |
| company_id | UUID | 所属公司 |
| cost_center_id | UUID | 成本中心 |
| headcount | INTEGER | 当前人数 |
| max_headcount | INTEGER | 编制上限 |

---

## 4. 职务 (Job)

### 4.1 功能说明

职务是对一类工作职责的抽象定义，代表具有相似工作内容的岗位集合。

### 4.2 业务规则

1. **职位模板**: 一个职务可以对应多个职位
2. **职务族**: 支持按职务族分类管理
3. **职级体系**: 支持职级职等关联
4. **任职要求**: 可定义任职资格要求

### 4.3 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| job_code | VARCHAR(8) | 职务编码 |
| job_family | VARCHAR(4) | 职务族 |
| job_function | VARCHAR(4) | 职能分类 |
| job_grade | VARCHAR(4) | 职级 |
| job_level | INTEGER | 职等 |
| description | TEXT | 职责描述 |
| requirements | TEXT | 任职要求 |
| qualifications | JSONB | 所需资格 |

---

## 5. 职位 (Position)

### 5.1 功能说明

职位是组织中具体的工作岗位，代表一个人可以担任的具体职务。

### 5.2 业务规则

1. **职务关联**: 每个职位必须关联一个职务
2. **组织归属**: 每个职位必须属于一个组织单元
3. **编制管理**: 支持单人头或多头职位
4. **状态管理**: 空缺、已填充、冻结、废除

### 5.3 职位状态

| 状态 | 代码 | 说明 |
|------|------|------|
| 空缺 | VA | Vacant - 职位空缺中 |
| 已填充 | FI | Filled - 已有任职者 |
| 冻结 | FR | Frozen - 职位冻结 |
| 废除 | AB | Abolished - 职位已废除 |

### 5.4 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| position_code | VARCHAR(8) | 职位编码 |
| job_object_id | UUID | 关联职务 |
| org_object_id | UUID | 所属组织 |
| holder_object_id | UUID | 当前任职者 |
| holder_name | VARCHAR(80) | 任职者姓名 |
| cost_center_id | UUID | 成本中心 |
| headcount | INTEGER | 编制数 |
| current_count | INTEGER | 当前人数 |
| position_status | VARCHAR(2) | 职位状态 |

---

## 6. 关系管理 (Relationship)

### 6.1 功能说明

关系管理实现任意 OM 对象之间的关联，对标 SAP HRP1001 的设计。

### 6.2 关系方向 (A/B)

SAP 风格的 A/B 方向设计：
- **A方向**: 关系的主动方 (上级/持有者)
- **B方向**: 关系的被动方 (下级/被持有)

```
示例: 职位持有人员

A方向 (职位) ──[008: holder of]──► B方向 (人员)
   职位A                              人员P
   "职位A 持有 人员P"

B方向 (人员) ◄──[013: person to position]── A方向 (职位)
   人员P                              职位A
   "人员P 任职于 职位A"
```

### 6.3 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| relation_type | ENUM | 关系类型 |
| object_type_a | ENUM | A方向对象类型 |
| object_id_a | UUID | A方向对象ID |
| object_type_b | ENUM | B方向对象类型 |
| object_id_b | UUID | B方向对象ID |
| percentage | DECIMAL(5,2) | 比例 (分摊场景) |
| priority | INTEGER | 优先级 (多汇报线) |
| is_primary | BOOLEAN | 是否主要关系 |
| valid_from | DATE | 生效日期 |
| valid_to | DATE | 失效日期 |

---

## 7. 典型业务场景

### 7.1 组织架构搭建

```
1. 创建根组织 (公司)
   ├── OM Object: O-10000001 (总公司)
   └── org_unit_detail: 公司名称、编码

2. 创建子组织 (部门)
   ├── OM Object: O-10000002 (人力资源部)
   └── Relationship: 002 (belongs to) O-10000001

3. 创建职务
   ├── OM Object: C-20000001 (人力资源经理)
   └── job_detail: 职责、要求

4. 创建职位
   ├── OM Object: S-30000001 (人力资源经理-总部)
   ├── Relationship: 011 (spec. of position) → C-20000001
   └── Relationship: 003 (includes) ← O-10000002
```

### 7.2 员工入职任职

```
1. 创建人员对象
   └── OM Object: P-EMP001 (张三)

2. 建立职位-人员关系
   ├── Relationship: 008 (holder of)
   │   ├── A: S-30000001 (职位)
   │   └── B: P-EMP001 (人员)
   └── 更新职位状态: VA → FI
```

### 7.3 组织调整

```
1. 部门合并
   ├── 修改旧部门有效期: valid_to = 调整日期-1
   ├── 创建新部门
   └── 调整下级组织/职位的关系

2. 员工调动
   ├── 结束旧职位关系: valid_to = 调动日期-1
   └── 创建新职位关系: valid_from = 调动日期
```

---

## 8. 查询视图

### 8.1 组织树形视图

提供递归查询组织架构的能力，支持：
- 自顶向下遍历
- 层级路径显示
- 编制统计

### 8.2 职位完整视图

提供职位及其关联信息的完整视图：
- 职位基本信息
- 关联职务信息
- 所属组织信息
- 当前任职者信息

### 8.3 汇报关系视图

提供直线和矩阵汇报关系的查询：
- 上级-下级关系
- 主要/次要汇报线
- 有效期过滤

---

## 9. 接口设计

### 9.1 组织管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/om/org-units | GET/POST | 组织单元 CRUD |
| /api/om/org-units/{id}/children | GET | 获取下级组织 |
| /api/om/org-units/{id}/positions | GET | 获取组织下职位 |
| /api/om/org-units/tree | GET | 组织树结构 |

### 9.2 职位管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/om/positions | GET/POST | 职位 CRUD |
| /api/om/positions/{id}/holder | GET/PUT | 获取/设置任职者 |
| /api/om/positions/vacant | GET | 空缺职位列表 |

### 9.3 职务管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/om/jobs | GET/POST | 职务 CRUD |
| /api/om/jobs/{id}/positions | GET | 职务下所有职位 |

---

## 10. 相关文档

- [HR 模块总览](./00-HR-OVERVIEW.md)
- [PA 人事管理](./02-PA-DESIGN.md)
- [数据库设计](../../research/nexterp-database/docs/07-HR-DESIGN.md)

---

## 11. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
| 2.0 | 2026-03-14 | 完善 SAP HRP1000/HRP1001 对标 |
