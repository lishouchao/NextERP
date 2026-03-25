# TM 培训管理功能设计

**模块**: Training Management (培训管理)
**对标**: SAP PE (Personnel Education/Training)
**版本**: 2.0
**更新日期**: 2026-03-14

---

## 1. 模块概述

### 1.1 功能范围

培训管理 (TM) 模块管理企业培训全流程，包括：

- **课程目录** - 课程分类、课程信息
- **培训班次** - 培训计划、时间地点
- **培训报名** - 学员报名、审批
- **培训执行** - 签到、评估、考核
- **资格认证** - 证书管理、有效期

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    培训管理 (TM) 架构 - 对标 SAP PE                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ 课程目录    │────►│ 培训班次    │────►│ 学员报名    │                   │
│  │ course      │     │ class       │     │ enrollment  │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│         │                   │                   │                          │
│         │                   │                   │                          │
│         ▼                   ▼                   ▼                          │
│  ┌──────────────────────────────────────────────────────────────────┐      │
│  │                        培训执行 (Training Execution)               │      │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │      │
│  │  │ 签到记录 │  │ 培训评估 │  │ 考试成绩 │  │ 证书发放 │         │      │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │      │
│  └──────────────────────────────────────────────────────────────────┘      │
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ 培训记录    │────►│ 资格认证    │────►│ 技能矩阵    │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 课程目录

### 2.1 课程分类

| 字段 | 类型 | 说明 |
|------|------|------|
| category_code | VARCHAR(10) | 分类编码 |
| category_name | VARCHAR(100) | 分类名称 |
| parent_id | UUID | 上级分类 |
| level | INTEGER | 层级 |
| path | VARCHAR(200) | 层级路径 |

### 2.2 课程信息 (对标 SAP HRP1000 OTYPE='E')

| 字段 | 类型 | 说明 |
|------|------|------|
| course_code | VARCHAR(10) | 课程编码 |
| course_name | VARCHAR(200) | 课程名称 |
| category_id | UUID | 所属分类 |
| course_type | VARCHAR(2) | 课程类型 |
| description | TEXT | 课程描述 |
| objectives | TEXT | 培训目标 |
| target_audience | TEXT | 目标学员 |
| prerequisites | TEXT | 前置要求 |
| duration_hours | DECIMAL(5,1) | 时长(小时) |
| duration_days | DECIMAL(3,1) | 时长(天) |
| cost | DECIMAL(10,2) | 费用 |
| vendor_id | UUID | 外部供应商 |
| material_url | VARCHAR(500) | 课件地址 |

### 2.3 课程类型

| 代码 | 说明 |
|------|------|
| IN | 内部培训 |
| EX | 外部培训 |
| ON | 在线培训 |

---

## 3. 培训班次

### 3.1 功能说明

课程的具体实施计划，包含时间、地点、讲师、容量等信息。

### 3.2 核心字段 (对标 SAP PE 事件管理)

| 字段 | 类型 | 说明 |
|------|------|------|
| class_code | VARCHAR(20) | 班次编码 |
| class_name | VARCHAR(200) | 班次名称 |
| course_id | UUID | 关联课程 |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| start_time | TIME | 开始时间 |
| end_time | TIME | 结束时间 |
| location | VARCHAR(200) | 培训地点 |
| room | VARCHAR(50) | 教室 |
| online_link | VARCHAR(500) | 在线链接 |
| online_platform | VARCHAR(50) | 在线平台 |
| capacity | INTEGER | 容量上限 |
| enrolled_count | INTEGER | 已报名人数 |
| completed_count | INTEGER | 已完成人数 |
| instructor_id | UUID | 内部讲师 |
| instructor_name | VARCHAR(80) | 讲师姓名 |
| external_instructor | VARCHAR(80) | 外部讲师 |
| unit_cost | DECIMAL(10,2) | 人均费用 |
| total_cost | DECIMAL(12,2) | 总费用 |
| class_status | VARCHAR(2) | 班次状态 |
| enroll_deadline | DATE | 报名截止 |

### 3.3 班次状态

| 代码 | 说明 |
|------|------|
| PL | 计划中 (Planned) |
| RE | 报名中 (Recruiting) |
| RU | 进行中 (Running) |
| CO | 已完成 (Completed) |
| CA | 已取消 (Cancelled) |

---

## 4. 培训报名

### 4.1 报名信息

| 字段 | 类型 | 说明 |
|------|------|------|
| class_id | UUID | 班次ID |
| employee_id | UUID | 员工ID |
| enroll_date | DATE | 报名日期 |
| enroll_status | VARCHAR(2) | 报名状态 |
| approved_by | UUID | 审批人 |
| approved_at | TIMESTAMP | 审批时间 |
| completion_status | VARCHAR(2) | 完成状态 |
| attendance_rate | DECIMAL(5,2) | 出勤率 |
| score | DECIMAL(5,1) | 考试成绩 |
| evaluation_score | DECIMAL(2,1) | 评估分数 |
| evaluation_comment | TEXT | 评估意见 |
| certificate_no | VARCHAR(50) | 证书编号 |
| certificate_date | DATE | 发证日期 |

### 4.2 报名状态

| 代码 | 说明 |
|------|------|
| PE | 待审批 (Pending) |
| AP | 已批准 (Approved) |
| RE | 已拒绝 (Rejected) |
| CA | 已取消 (Cancelled) |

### 4.3 完成状态

| 代码 | 说明 |
|------|------|
| EN | 已报名 (Enrolled) |
| AT | 已出席 (Attended) |
| CO | 已通过 (Completed) |
| FA | 未通过 (Failed) |

---

## 5. 培训签到

### 5.1 签到记录

| 字段 | 类型 | 说明 |
|------|------|------|
| enrollment_id | UUID | 报名ID |
| class_id | UUID | 班次ID |
| employee_id | UUID | 员工ID |
| attendance_date | DATE | 签到日期 |
| check_in_time | TIMESTAMP | 签到时间 |
| check_out_time | TIMESTAMP | 签退时间 |
| attendance_status | VARCHAR(2) | 出勤状态 |
| remarks | TEXT | 备注 |

### 5.2 出勤状态

| 代码 | 说明 |
|------|------|
| PR | 出勤 (Present) |
| AB | 缺勤 (Absent) |
| LA | 迟到 (Late) |
| EA | 早退 (Early) |

---

## 6. 资格认证

### 6.1 资格定义 (对标 SAP HRP1000 OTYPE='Q')

| 字段 | 类型 | 说明 |
|------|------|------|
| qual_code | VARCHAR(10) | 资格编码 |
| qual_name | VARCHAR(200) | 资格名称 |
| category_id | UUID | 所属分类 |
| qual_type | VARCHAR(2) | 资格类型 |
| description | TEXT | 描述 |
| validity_months | INTEGER | 有效期(月) |
| requires_renewal | BOOLEAN | 需要更新 |
| evaluation_criteria | TEXT | 评估标准 |

### 6.2 资格类型

| 代码 | 说明 |
|------|------|
| CE | 证书 (Certificate) |
| SK | 技能 (Skill) |
| LA | 执照 (License) |

### 6.3 员工资格 (对标 SAP IT0024)

| 字段 | 类型 | 说明 |
|------|------|------|
| employee_id | UUID | 员工ID |
| qual_id | UUID | 资格ID |
| qual_code | VARCHAR(10) | 资格编码 |
| qual_name | VARCHAR(200) | 资格名称 |
| proficiency_level | VARCHAR(2) | 熟练度 |
| acquisition_type | VARCHAR(2) | 获取方式 |
| acquisition_date | DATE | 获取日期 |
| certificate_no | VARCHAR(50) | 证书编号 |
| certificate_org | VARCHAR(100) | 发证机构 |
| expiry_date | DATE | 过期日期 |
| valid_from | DATE | 生效日期 |
| valid_to | DATE | 失效日期 |

### 6.4 熟练度等级

| 代码 | 说明 |
|------|------|
| 01 | 初级 |
| 02 | 中级 |
| 03 | 高级 |
| 04 | 专家 |
| 05 | 大师 |

### 6.5 获取方式

| 代码 | 说明 |
|------|------|
| TR | 培训获得 |
| EX | 工作经验 |
| CE | 认证考试 |

---

## 7. 接口设计

### 7.1 课程管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/training/courses | GET/POST | 课程列表/创建 |
| /api/training/courses/{id} | GET/PUT/DELETE | 课程详情/更新/删除 |
| /api/training/categories | GET/POST | 课程分类 |

### 7.2 培训班次接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/training/classes | GET/POST | 班次列表/创建 |
| /api/training/classes/{id} | GET/PUT | 班次详情/更新 |
| /api/training/classes/{id}/enroll | POST | 报名 |
| /api/training/classes/{id}/enrollments | GET | 报名列表 |

### 7.3 培训记录接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/training/enrollments/{id} | GET | 报名详情 |
| /api/training/enrollments/{id}/check-in | POST | 签到 |
| /api/training/enrollments/{id}/evaluate | POST | 评估 |
| /api/training/my | GET | 我的培训记录 |

### 7.4 资格认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/training/qualifications | GET/POST | 资格列表/创建 |
| /api/training/qualifications/{id} | GET/PUT | 资格详情/更新 |
| /api/training/employee-quals | GET/POST | 员工资格 |

---

## 8. 相关文档

- [HR 模块总览](./00-HR-OVERVIEW.md)
- [PA 人事管理](./02-PA-DESIGN.md)
- [PM 绩效管理](./07-PM-DESIGN.md)

---

## 9. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
