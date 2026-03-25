# SP 继任计划功能设计

**模块**: Succession Planning (继任计划)
**对标**: SAP PP (Personnel Planning) / HRTMC (Talent Management Center)
**版本**: 2.0
**更新日期**: 2026-03-14

---

## 1. 模块概述

### 1.1 功能范围

继任计划 (SP) 模块管理企业人才梯队建设，包括：

- **人才池** - 高潜人才、继任者池
- **继任计划** - 关键职位继任规划
- **发展计划** - 人才发展活动
- **人才盘点** - 九宫格、人才地图

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    继任计划 (SP) 架构 - 对标 SAP PP                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ 关键职位    │────►│ 继任者池    │────►│ 发展计划    │                   │
│  │ key_position│     │ successor   │     │ dev_plan    │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│         │                   │                   │                          │
│         │                   │                   │                          │
│         ▼                   ▼                   ▼                          │
│  ┌──────────────────────────────────────────────────────────────────┐      │
│  │                        人才盘点 (Talent Review)                    │      │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │      │
│  │  │ 九宫格   │  │ 人才地图 │  │ 风险评估 │  │ 保留策略 │         │      │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │      │
│  └──────────────────────────────────────────────────────────────────┘      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 人才池

### 2.1 人才池定义

| 字段 | 类型 | 说明 |
|------|------|------|
| pool_code | VARCHAR(10) | 人才池编码 |
| pool_name | VARCHAR(100) | 人才池名称 |
| pool_type | VARCHAR(2) | 人才池类型 |
| target_level | VARCHAR(4) | 目标层级 |
| capacity | INTEGER | 容量上限 |
| current_count | INTEGER | 当前人数 |
| owner_id | UUID | 负责人 |
| description | TEXT | 描述 |
| status | VARCHAR(2) | 状态 |

### 2.2 人才池类型

| 代码 | 说明 |
|------|------|
| SU | 继任者池 (Successor Pool) |
| HI | 高潜人才池 (High Potential) |
| EX | 专家池 (Expert) |
| LE | 领导力池 (Leadership) |

---

## 3. 人才池成员

### 3.1 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| pool_id | UUID | 人才池ID |
| employee_id | UUID | 员工ID |
| join_date | DATE | 加入日期 |
| readiness | VARCHAR(2) | 准备度 |
| performance_level | VARCHAR(2) | 绩效等级 |
| potential_level | VARCHAR(2) | 潜力等级 |
| grid_position | VARCHAR(2) | 九宫格位置 |
| flight_risk | VARCHAR(2) | 离职风险 |
| retention_risk | VARCHAR(2) | 保留风险 |
| development_focus | TEXT | 发展重点 |
| member_status | VARCHAR(2) | 成员状态 |

### 3.2 准备度 (对标 SAP 准备度)

| 代码 | 说明 |
|------|------|
| RE | 就绪 (Ready Now) |
| 1Y | 1年内 (1 Year) |
| 2Y | 1-2年 |
| 2P | 2年以上 |
| NO | 未确定 |

### 3.3 风险等级

| 代码 | 说明 |
|------|------|
| HI | 高风险 (High) |
| ME | 中风险 (Medium) |
| LO | 低风险 (Low) |

### 3.4 成员状态

| 代码 | 说明 |
|------|------|
| AC | 活跃 (Active) |
| PR | 已晋升 (Promoted) |
| RE | 已移出 (Removed) |

---

## 4. 继任计划

### 4.1 功能说明

为关键职位规划继任者，确保人才梯队。

### 4.2 核心字段 (对标 SAP PP)

| 字段 | 类型 | 说明 |
|------|------|------|
| position_id | UUID | 职位ID |
| position_name | VARCHAR(100) | 职位名称 |
| current_holder_id | UUID | 当前任职者 |
| holder_name | VARCHAR(80) | 任职者姓名 |
| vacancy_risk | VARCHAR(2) | 空缺风险 |
| bench_strength | VARCHAR(2) | 板凳深度 |
| successor_count | INTEGER | 继任者数量 |
| plan_status | VARCHAR(2) | 计划状态 |

### 4.3 空缺风险

| 代码 | 说明 |
|------|------|
| HI | 高风险 - 预计1年内空缺 |
| ME | 中风险 - 预计1-2年空缺 |
| LO | 低风险 - 稳定 |

### 4.4 板凳深度

| 代码 | 说明 |
|------|------|
| ST | 强 (Strong) - 有2+就绪继任者 |
| AD | 充足 (Adequate) - 有1个就绪继任者 |
| WE | 弱 (Weak) - 无就绪继任者 |

---

## 5. 继任者

### 5.1 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| plan_id | UUID | 继任计划ID |
| employee_id | UUID | 员工ID |
| employee_name | VARCHAR(80) | 姓名 |
| succession_order | INTEGER | 继任顺序 |
| readiness | VARCHAR(2) | 准备度 |
| strength | TEXT | 优势 |
| development_area | TEXT | 待发展领域 |
| development_plan_id | UUID | 发展计划 |
| successor_status | VARCHAR(2) | 继任者状态 |
| last_review_date | DATE | 最近评估日期 |
| next_review_date | DATE | 下次评估日期 |

### 5.2 继任顺序

| 顺序 | 说明 |
|------|------|
| 1 | 首选继任者 |
| 2 | 次选继任者 |
| 3 | 第三选择 |

### 5.3 继任者状态

| 代码 | 说明 |
|------|------|
| AC | 活跃 (Active) |
| PR | 已晋升 (Promoted) |
| RE | 已移除 (Removed) |
| NO | 不再考虑 |

---

## 6. 发展计划

### 6.1 功能说明

为人才池成员或继任者制定个人发展计划。

### 6.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| employee_id | UUID | 员工ID |
| plan_year | INTEGER | 计划年度 |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| target_position_id | UUID | 目标职位 |
| target_position_name | VARCHAR(100) | 目标职位名称 |
| target_competencies | JSONB | 目标能力 |
| plan_status | VARCHAR(2) | 计划状态 |
| approved_by | UUID | 审批人 |
| approved_at | TIMESTAMP | 审批时间 |
| progress_pct | DECIMAL(5,2) | 完成进度 |

### 6.3 计划状态

| 代码 | 说明 |
|------|------|
| DR | 草稿 (Draft) |
| AP | 已批准 (Approved) |
| IP | 进行中 (In Progress) |
| CO | 已完成 (Completed) |
| CA | 已取消 (Cancelled) |

---

## 7. 发展活动

### 7.1 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| plan_id | UUID | 发展计划ID |
| activity_type | VARCHAR(2) | 活动类型 |
| activity_name | VARCHAR(200) | 活动名称 |
| description | TEXT | 描述 |
| planned_start | DATE | 计划开始 |
| planned_end | DATE | 计划结束 |
| actual_start | DATE | 实际开始 |
| actual_end | DATE | 实际结束 |
| activity_status | VARCHAR(2) | 活动状态 |
| effectiveness | VARCHAR(2) | 有效性评估 |
| mentor_id | UUID | 导师 |
| notes | TEXT | 备注 |

### 7.2 活动类型 (对标 SAP 70-20-10 原则)

| 代码 | 说明 | 占比建议 |
|------|------|----------|
| TR | 培训 (Training) | 10% |
| CO | 辅导 (Coaching) | 20% |
| RO | 轮岗 (Rotation) | 70% |
| PR | 项目 (Project) | 70% |
| SE | 自学 (Self-study) | 10% |
| ME | 导师 (Mentoring) | 20% |

### 7.3 活动状态

| 代码 | 说明 |
|------|------|
| PL | 计划中 (Planned) |
| IP | 进行中 (In Progress) |
| CO | 已完成 (Completed) |
| CA | 已取消 (Cancelled) |

---

## 8. 人才盘点 (Talent Review)

### 8.1 九宫格 (对标 SAP 九宫格模型)

```
                        潜力 (Potential)
                    低 ◄────────────────────► 高
                 ┌─────────┬─────────┬─────────┐
                 │         │         │         │
            高   │   C3    │   B3    │   A3    │
                 │ 明星    │ 高潜    │ 超级明星│
    绩           │         │         │         │
    效     ──────┼─────────┼─────────┼─────────┤
   (Performance) │         │         │         │
            中   │   C2    │   B2    │   A2    │
                 │ 中坚    │ 核心员工│ 高绩效  │
                 │         │         │         │
            ──────┼─────────┼─────────┼─────────┤
                 │         │         │         │
            低   │   C1    │   B1    │   A1    │
                 │ 需改进  │ 待培养  │ 有潜力  │
                 │         │         │         │
                 └─────────┴─────────┴─────────┘
```

### 8.2 九宫格位置定义

| 位置 | 绩效 | 潜力 | 分类 | 建议 |
|------|------|------|------|------|
| A1 | 低 | 高 | 有潜力 | 加速培养 |
| A2 | 中 | 高 | 高绩效 | 重点发展 |
| A3 | 高 | 高 | 超级明星 | 继任计划 |
| B1 | 低 | 中 | 待培养 | 绩效改进 |
| B2 | 中 | 中 | 核心员工 | 稳定发展 |
| B3 | 高 | 中 | 高潜 | 继任培养 |
| C1 | 低 | 低 | 需改进 | 绩效改进/淘汰 |
| C2 | 中 | 低 | 中坚 | 保持稳定 |
| C3 | 高 | 低 | 明星 | 专业发展 |

### 8.3 人才盘点会

| 字段 | 类型 | 说明 |
|------|------|------|
| review_name | VARCHAR(100) | 盘点会名称 |
| review_year | INTEGER | 盘点年度 |
| review_type | VARCHAR(2) | 盘点类型 |
| scope_org_id | UUID | 盘点范围 |
| participants | JSONB | 参与人 |
| review_date | DATE | 盘点日期 |
| facilitator_id | UUID | 主持人 |
| status | VARCHAR(2) | 状态 |
| summary | TEXT | 盘点总结 |
| action_items | JSONB | 行动计划 |

---

## 9. 人才地图

### 9.1 功能说明

可视化展示组织人才分布和继任情况。

### 9.2 人才地图视图

- **组织人才概览** - 各部门人才分布
- **关键职位状态** - 继任者就绪情况
- **风险预警** - 高风险职位/人员
- **多样性分析** - 性别、年龄、学历分布

---

## 10. 接口设计

### 10.1 人才池接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/talent/pools | GET/POST | 人才池列表/创建 |
| /api/talent/pools/{id} | GET/PUT | 人才池详情/更新 |
| /api/talent/pools/{id}/members | GET | 池成员列表 |
| /api/talent/pools/{id}/add-member | POST | 添加成员 |
| /api/talent/pools/{id}/remove-member | POST | 移除成员 |

### 10.2 继任计划接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/talent/succession-plans | GET/POST | 继任计划列表/创建 |
| /api/talent/succession-plans/{id} | GET/PUT | 计划详情/更新 |
| /api/talent/succession-plans/{id}/successors | GET | 继任者列表 |
| /api/talent/succession-plans/{id}/add-successor | POST | 添加继任者 |

### 10.3 发展计划接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/talent/development-plans | GET/POST | 发展计划列表/创建 |
| /api/talent/development-plans/{id} | GET/PUT | 计划详情/更新 |
| /api/talent/development-plans/{id}/activities | GET/POST | 发展活动 |
| /api/talent/my-plans | GET | 我的发展计划 |

### 10.4 人才盘点接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/talent/reviews | GET/POST | 盘点会列表/创建 |
| /api/talent/reviews/{id} | GET | 盘点会详情 |
| /api/talent/reviews/{id}/matrix | GET | 九宫格数据 |
| /api/talent/reviews/{id}/complete | POST | 完成盘点 |

### 10.5 分析报表接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/talent/dashboard | GET | 人才仪表盘 |
| /api/talent/talent-map | GET | 人才地图 |
| /api/talent/risk-report | GET | 风险报告 |

---

## 11. 相关文档

- [HR 模块总览](./00-HR-OVERVIEW.md)
- [OM 组织管理](./01-OM-DESIGN.md)
- [PA 人事管理](./02-PA-DESIGN.md)
- [PM 绩效管理](./07-PM-DESIGN.md)

---

## 12. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
