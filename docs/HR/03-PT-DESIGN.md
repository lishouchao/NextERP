# PT 时间管理功能设计

**模块**: Time Management (时间管理)
**对标**: SAP PT (Personnel Time Management)
**版本**: 2.0
**更新日期**: 2026-03-14

---

## 1. 模块概述

### 1.1 功能范围

时间管理 (PT) 模块管理员工的考勤和时间数据，包括：

- **排班管理** - 工作时间规则、班次安排
- **出勤管理** - 打卡记录、出勤统计
- **缺勤管理** - 请假申请、假期配额
- **加班管理** - 加班申请、调休管理
- **工时汇总** - 月度/年度工时统计

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    时间管理 (PT) 架构 - 对标 SAP PT                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ IT0007      │     │ IT2002      │     │ IT2005      │                   │
│  │ 排班计划    │────►│ 出勤记录    │◄────│ 加班记录    │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│         │                   │                   │                          │
│         │                   │                   │                          │
│         ▼                   ▼                   ▼                          │
│  ┌──────────────────────────────────────────────────────────────────┐      │
│  │                      工时汇总 (Time Evaluation)                    │      │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │      │
│  │  │ 月度汇总 │  │ 年度汇总 │  │ 假期余额 │  │ 加班余额 │         │      │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │      │
│  └──────────────────────────────────────────────────────────────────┘      │
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ IT2001      │     │ IT2003      │     │ IT2006      │                   │
│  │ 缺勤记录    │────►│ 出差记录    │────►│ 工时记录    │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 排班管理

### 2.1 IT0007 排班计划

记录员工的工作时间安排。

### 2.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| work_rule_id | UUID | 工时规则 |
| schedule_type | VARCHAR(2) | 工时类型 |
| weekly_hours | DECIMAL(4,1) | 周工作小时 |
| daily_hours | DECIMAL(3,1) | 日工作小时 |
| work_days | VARCHAR(7) | 工作日 (周一至周日) |
| is_flexible | BOOLEAN | 是否弹性工时 |
| flex_core_start | TIME | 核心时间开始 |
| flex_core_end | TIME | 核心时间结束 |

### 2.3 工时类型

| 代码 | 说明 |
|------|------|
| FT | 全职 (Full-time) |
| PT | 兼职 (Part-time) |
| SE | 轮班 (Shift) |

### 2.4 班次定义

| 字段 | 类型 | 说明 |
|------|------|------|
| shift_code | VARCHAR(4) | 班次编码 |
| shift_name | VARCHAR(50) | 班次名称 |
| work_start | TIME | 上班时间 |
| work_end | TIME | 下班时间 |
| break_start | TIME | 休息开始 |
| break_end | TIME | 休息结束 |
| work_hours | DECIMAL(3,1) | 工作小时 |
| is_overnight | BOOLEAN | 是否跨天 |
| clock_in_early | INTEGER | 可提前打卡分钟 |
| clock_in_late | INTEGER | 迟到宽限分钟 |
| clock_out_late | INTEGER | 可延后打卡分钟 |

---

## 3. 出勤管理

### 3.1 IT2002 出勤记录

记录员工每日的打卡和出勤数据。

### 3.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| record_date | DATE | 记录日期 |
| shift_id | UUID | 班次 |
| clock_in | TIMESTAMP | 上班打卡 |
| clock_out | TIMESTAMP | 下班打卡 |
| clock_in_location | VARCHAR(100) | 打卡地点 |
| clock_in_gps | VARCHAR(50) | GPS坐标 |
| work_hours | DECIMAL(4,2) | 实际工作小时 |
| break_hours | DECIMAL(3,2) | 休息小时 |
| net_work_hours | DECIMAL(4,2) | 净工作小时 |
| is_late | BOOLEAN | 是否迟到 |
| late_minutes | INTEGER | 迟到分钟 |
| is_early_leave | BOOLEAN | 是否早退 |
| early_minutes | INTEGER | 早退分钟 |
| is_absent | BOOLEAN | 是否缺勤 |
| source | VARCHAR(2) | 数据来源 |

### 3.3 数据来源

| 代码 | 说明 |
|------|------|
| AP | APP打卡 |
| BI | 生物识别 (指纹/人脸) |
| MA | 手动录入 |

---

## 4. 缺勤管理

### 4.1 IT2001 缺勤记录

记录员工的请假信息。

### 4.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| subtype | VARCHAR(4) | 假期类型 |
| valid_from | DATE | 开始日期 |
| valid_to | DATE | 结束日期 |
| start_time | TIME | 开始时间 |
| end_time | TIME | 结束时间 |
| days | DECIMAL(5,1) | 天数 |
| hours | DECIMAL(5,2) | 小时 |
| leave_type_id | UUID | 假期类型 |
| quota_year | INTEGER | 配额年度 |
| quota_deducted | DECIMAL(5,1) | 扣减配额 |
| reason | TEXT | 请假原因 |
| attachment_url | VARCHAR(500) | 附件 |
| approval_status | VARCHAR(2) | 审批状态 |

---

## 5. 假期类型与配额

### 5.1 假期类型 (对标 SAP T556A)

| 代码 | 名称 | 配额 | 带薪 |
|------|------|------|------|
| ANNUAL | 年假 | 按司龄/法定 | 是 |
| SICK | 病假 | 无限制 | 是/否 |
| PERSONAL | 事假 | 无限制 | 否 |
| MARRIAGE | 婚假 | 3天 | 是 |
| MATERNITY | 产假 | 98天+ | 是 |
| PATERNITY | 陪产假 | 15天 | 是 |
| BEREAVEMENT | 丧假 | 3天 | 是 |
| COMP | 调休假 | 按加班累积 | 是 |

### 5.2 假期配额 (对标 SAP PA2006)

| 字段 | 类型 | 说明 |
|------|------|------|
| quota_year | INTEGER | 配额年度 |
| opening_balance | DECIMAL(5,1) | 期初/结转 |
| accrued | DECIMAL(5,1) | 本期获得 |
| adjusted | DECIMAL(5,1) | 调整 |
| used | DECIMAL(5,1) | 已使用 |
| expired | DECIMAL(5,1) | 已过期 |
| balance | DECIMAL(5,1) | 余额 (计算) |

### 5.3 业务规则

1. **年假结转**: 支持跨年结转，可设置结转上限和过期时间
2. **配额扣减**: 按配置的扣减顺序依次扣减
3. **有效期**: 每种假期可配置有效期

---

## 6. 加班管理

### 6.1 IT2005 加班记录

记录员工的加班信息。

### 6.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| overtime_date | DATE | 加班日期 |
| start_time | TIME | 开始时间 |
| end_time | TIME | 结束时间 |
| hours | DECIMAL(4,2) | 加班小时 |
| break_hours | DECIMAL(3,2) | 休息小时 |
| overtime_type | VARCHAR(4) | 加班类型 |
| compensation_type | VARCHAR(2) | 补偿方式 |
| comp_hours | DECIMAL(4,2) | 调休小时 |
| comp_expiry | DATE | 调休过期日 |
| reason | TEXT | 加班原因 |
| approval_status | VARCHAR(2) | 审批状态 |

### 6.3 加班类型 (对标 SAP AWART)

| 代码 | 说明 | 补偿倍率 |
|------|------|----------|
| 01 | 工作日加班 | 1.5倍 |
| 02 | 周末加班 | 2.0倍 |
| 03 | 法定假日加班 | 3.0倍 |

### 6.4 补偿方式

| 代码 | 说明 |
|------|------|
| PY | 调薪 (加班费) |
| TM | 调休 |
| BF | 混合 (部分调薪+部分调休) |

---

## 7. 工时汇总

### 7.1 月度工时汇总 (对标 SAP PT 平衡表)

| 字段 | 类型 | 说明 |
|------|------|------|
| period_year | INTEGER | 年度 |
| period_month | INTEGER | 月份 |
| scheduled_days | DECIMAL(5,1) | 应出勤天数 |
| scheduled_hours | DECIMAL(6,1) | 应出勤小时 |
| actual_days | DECIMAL(5,1) | 实际出勤天数 |
| actual_hours | DECIMAL(6,1) | 实际工作小时 |
| overtime_hours | DECIMAL(5,1) | 加班小时 |
| overtime_paid | DECIMAL(5,1) | 已支付加班 |
| overtime_comp | DECIMAL(5,1) | 调休加班 |
| absence_days | DECIMAL(5,1) | 缺勤天数 |
| absence_hours | DECIMAL(5,1) | 缺勤小时 |
| paid_leave_days | DECIMAL(5,1) | 带薪假天数 |
| unpaid_leave_days | DECIMAL(5,1) | 无薪假天数 |
| late_count | INTEGER | 迟到次数 |
| late_minutes | INTEGER | 迟到分钟 |
| early_count | INTEGER | 早退次数 |
| early_minutes | INTEGER | 早退分钟 |
| status | VARCHAR(10) | 汇总状态 |

---

## 8. 业务流程

### 8.1 请假流程

```
1. 员工提交请假申请
   └── hr_it2001_absence: approval_status='PENDING'

2. 系统校验
   ├── 检查假期配额是否充足
   ├── 检查时间冲突
   └── 检查审批规则

3. 审批流程
   └── 直线经理审批

4. 审批通过
   ├── approval_status='APPROVED'
   ├── 扣减假期配额
   └── 通知相关人员
```

### 8.2 加班流程

```
1. 员工提交加班申请
   └── hr_it2005_overtime: approval_status='PENDING'

2. 审批流程
   └── 直线经理审批

3. 审批通过
   ├── approval_status='APPROVED'
   └── 累积调休配额 (如适用)
```

---

## 9. 接口设计

### 9.1 考勤管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/time/attendance | GET/POST | 出勤记录 |
| /api/time/attendance/clock-in | POST | 上班打卡 |
| /api/time/attendance/clock-out | POST | 下班打卡 |
| /api/time/attendance/my | GET | 我的考勤记录 |

### 9.2 请假管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/time/leaves | GET/POST | 请假记录 |
| /api/time/leaves/{id}/approve | POST | 审批请假 |
| /api/time/leaves/{id}/reject | POST | 拒绝请假 |
| /api/time/leave-types | GET | 假期类型列表 |
| /api/time/leave-quotas | GET | 假期配额 |

### 9.3 加班管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/time/overtime | GET/POST | 加班记录 |
| /api/time/overtime/{id}/approve | POST | 审批加班 |
| /api/time/overtime/compensatory | GET | 调休余额 |

---

## 10. 相关文档

- [HR 模块总览](./00-HR-OVERVIEW.md)
- [PA 人事管理](./02-PA-DESIGN.md)
- [PY 薪酬管理](./04-PY-DESIGN.md)

---

## 11. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
| 2.0 | 2026-03-14 | 完善假期配额和加班管理 |
