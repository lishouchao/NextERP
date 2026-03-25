# PM 绩效管理功能设计

**模块**: Performance Management (绩效管理)
**对标**: SAP PA0380-PA0382 (Performance Management)
**版本**: 2.0
**更新日期**: 2026-03-14

---

## 1. 模块概述

### 1.1 功能范围

绩效管理 (PM) 模块管理员工绩效全流程，包括：

- **绩效周期** - 考核周期定义
- **目标设定** - 绩效目标制定
- **目标追踪** - 目标进度跟踪
- **绩效评估** - 自评、上级评、360度评估
- **绩效校准** - 评分校准会议
- **结果应用** - 绩效等级、奖金系数

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    绩效管理 (PM) 架构 - 对标 SAP PA0380                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ 绩效周期    │────►│ 目标设定    │────►│ 目标追踪    │                   │
│  │ period      │     │ goal        │     │ tracking    │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│         │                   │                   │                          │
│         │                   │                   │                          │
│         ▼                   ▼                   ▼                          │
│  ┌──────────────────────────────────────────────────────────────────┐      │
│  │                        绩效评估 (Performance Review)               │      │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │      │
│  │  │ 自评     │  │ 上级评   │  │ 360度评估│  │ 校准会议 │         │      │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │      │
│  └──────────────────────────────────────────────────────────────────┘      │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ 绩效结果    │────►│ 绩效等级    │────►│ 结果应用    │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 绩效周期

### 2.1 功能说明

定义绩效考核的时间周期和阶段控制。

### 2.2 核心字段 (对标 SAP 评估周期)

| 字段 | 类型 | 说明 |
|------|------|------|
| period_code | VARCHAR(10) | 周期编码 |
| period_name | VARCHAR(100) | 周期名称 |
| period_year | INTEGER | 年度 |
| period_type | VARCHAR(2) | 周期类型 |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| goal_start | DATE | 目标设定开始 |
| goal_end | DATE | 目标设定结束 |
| review_start | DATE | 评估开始 |
| review_end | DATE | 评估结束 |
| calibration_start | DATE | 校准开始 |
| calibration_end | DATE | 校准结束 |
| period_status | VARCHAR(2) | 周期状态 |
| template_id | UUID | 评估模板 |

### 2.3 周期类型

| 代码 | 说明 |
|------|------|
| AN | 年度考核 (Annual) |
| HY | 半年考核 (Half Year) |
| QU | 季度考核 (Quarterly) |
| MO | 月度考核 (Monthly) |

### 2.4 周期状态

| 代码 | 说明 |
|------|------|
| DR | 草稿 (Draft) |
| AC | 激活 (Active) |
| GO | 目标设定中 |
| RE | 评估中 |
| CA | 校准中 |
| CL | 已关闭 (Closed) |

---

## 3. 绩效目标

### 3.1 功能说明 (对标 SAP PA0380)

记录员工的绩效目标和完成情况。

### 3.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| employee_id | UUID | 员工ID |
| period_id | UUID | 绩效周期 |
| goal_category | VARCHAR(4) | 目标类别 |
| goal_title | VARCHAR(200) | 目标标题 |
| goal_description | TEXT | 目标描述 |
| weight | DECIMAL(5,2) | 权重 (%) |
| target_value | DECIMAL(15,2) | 目标值 |
| target_unit | VARCHAR(20) | 目标单位 |
| target_operator | VARCHAR(2) | 比较运算 |
| actual_value | DECIMAL(15,2) | 实际值 |
| achievement_rate | DECIMAL(5,2) | 完成率 (%) |
| self_rating | DECIMAL(3,1) | 自评分数 |
| manager_rating | DECIMAL(3,1) | 上级评分 |
| final_rating | DECIMAL(3,1) | 最终分数 |
| goal_status | VARCHAR(2) | 目标状态 |
| manager_comment | TEXT | 上级评语 |

### 3.3 目标类别

| 代码 | 说明 |
|------|------|
| KPI | 关键绩效指标 |
| OKR | 目标与关键结果 |
| COM | 能力目标 |
| DEV | 发展目标 |

### 3.4 目标状态

| 代码 | 说明 |
|------|------|
| DR | 草稿 |
| SU | 已提交 |
| AP | 已批准 |
| CO | 已完成 |

---

## 4. 绩效评估

### 4.1 评估主表 (对标 SAP PA0381)

| 字段 | 类型 | 说明 |
|------|------|------|
| employee_id | UUID | 员工ID |
| period_id | UUID | 绩效周期 |
| goals_avg_rating | DECIMAL(3,1) | 目标平均分 |
| competency_rating | DECIMAL(3,1) | 能力评分 |
| overall_rating | DECIMAL(3,1) | 综合评分 |
| performance_level | VARCHAR(2) | 绩效等级 |
| strengths | TEXT | 优势 |
| improvements | TEXT | 待改进 |
| development_plan | TEXT | 发展计划 |
| review_status | VARCHAR(2) | 评估状态 |
| reviewer_id | UUID | 评估人 |
| reviewed_at | TIMESTAMP | 评估时间 |
| calibrated_rating | DECIMAL(3,1) | 校准分数 |
| calibrated_level | VARCHAR(2) | 校准等级 |
| calibrated_by | UUID | 校准人 |
| calibrated_at | TIMESTAMP | 校准时间 |

### 4.2 评估状态

| 代码 | 说明 |
|------|------|
| DR | 草稿 |
| SU | 已提交 |
| AP | 已批准 |
| CA | 已校准 |

---

## 5. 360度评估

### 5.1 功能说明

收集来自多方位的评估反馈。

### 5.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| review_id | UUID | 评估ID |
| subject_id | UUID | 被评人 |
| evaluator_id | UUID | 评估人 |
| evaluator_type | VARCHAR(2) | 评估人类型 |
| competency_scores | JSONB | 能力评分详情 |
| overall_rating | DECIMAL(3,1) | 综合评分 |
| feedback | TEXT | 评价反馈 |
| eval_status | VARCHAR(2) | 评估状态 |
| submitted_at | TIMESTAMP | 提交时间 |

### 5.3 评估人类型

| 代码 | 说明 |
|------|------|
| SE | 自评 (Self) |
| MG | 上级 (Manager) |
| PE | 同级 (Peer) |
| SU | 下级 (Subordinate) |
| CU | 客户 (Customer) |

---

## 6. 绩效结果

### 6.1 功能说明 (对标 SAP PA0382)

记录员工绩效评估最终结果。

### 6.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| employee_id | UUID | 员工ID |
| period_id | UUID | 绩效周期 |
| review_id | UUID | 评估ID |
| period_year | INTEGER | 年度 |
| final_rating | DECIMAL(3,1) | 最终评分 |
| performance_level | VARCHAR(2) | 绩效等级 |
| rank_in_team | INTEGER | 团队排名 |
| rank_in_org | INTEGER | 组织排名 |
| percentile | DECIMAL(5,2) | 百分位 |
| bonus_factor | DECIMAL(3,2) | 奖金系数 |
| salary_increase_pct | DECIMAL(5,2) | 调薪比例 (%) |
| promotion_recommend | VARCHAR(2) | 晋升建议 |
| promotion_target | VARCHAR(100) | 晋升目标 |
| history | JSONB | 历年绩效摘要 |

### 6.3 绩效等级

| 代码 | 说明 | 比例建议 |
|------|------|----------|
| AA | 卓越 (A+) | 5-10% |
| AB | 优秀 (A) | 15-20% |
| BB | 良好 (B+) | 25-30% |
| BC | 合格 (B) | 30-35% |
| CC | 待改进 (C) | 10-15% |
| DD | 不合格 (D) | 5% |

### 6.4 晋升建议

| 代码 | 说明 |
|------|------|
| PR | 推荐晋升 (Promote) |
| HO | 保留观察 (Hold) |
| NO | 不推荐 (Not) |

---

## 7. 评估流程

### 7.1 标准评估流程

```
1. 周期激活
   └── 管理员激活绩效周期

2. 目标设定
   ├── 员工填写绩效目标
   ├── 直线经理审批目标
   └── 系统锁定已批准目标

3. 目标追踪
   └── 员工/经理更新进度

4. 自评
   └── 员工填写自评

5. 上级评估
   └── 直线经理进行评估

6. 360度评估 (可选)
   └── 收集多方反馈

7. 绩效校准
   └── 管理层校准评分

8. 结果发布
   └── 绩效结果沟通

9. 结果应用
   ├── 奖金计算
   ├── 调薪建议
   └── 晋升建议
```

---

## 8. 接口设计

### 8.1 绩效周期接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/performance/periods | GET/POST | 周期列表/创建 |
| /api/performance/periods/{id} | GET/PUT | 周期详情/更新 |
| /api/performance/periods/{id}/activate | POST | 激活周期 |
| /api/performance/periods/{id}/close | POST | 关闭周期 |

### 8.2 绩效目标接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/performance/goals | GET/POST | 目标列表/创建 |
| /api/performance/goals/{id} | GET/PUT | 目标详情/更新 |
| /api/performance/goals/{id}/submit | POST | 提交审批 |
| /api/performance/goals/{id}/approve | POST | 审批目标 |

### 8.3 绩效评估接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/performance/reviews | GET | 评估列表 |
| /api/performance/reviews/{id} | GET | 评估详情 |
| /api/performance/reviews/{id}/self-evaluate | POST | 自评 |
| /api/performance/reviews/{id}/manager-evaluate | POST | 上级评估 |
| /api/performance/reviews/{id}/calibrate | POST | 校准 |

### 8.4 360度评估接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/performance/360 | GET/POST | 360评估列表/创建 |
| /api/performance/360/{id} | GET | 评估详情 |
| /api/performance/360/{id}/submit | POST | 提交评估 |

### 8.5 绩效结果接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/performance/results | GET | 结果列表 |
| /api/performance/results/{id} | GET | 结果详情 |
| /api/performance/my-results | GET | 我的绩效结果 |

---

## 9. 相关文档

- [HR 模块总览](./00-HR-OVERVIEW.md)
- [PA 人事管理](./02-PA-DESIGN.md)
- [PY 薪酬管理](./04-PY-DESIGN.md)
- [SP 继任计划](./08-SP-DESIGN.md)

---

## 10. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
