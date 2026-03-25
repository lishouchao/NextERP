# RC 招聘管理功能设计

**模块**: Recruitment (招聘管理)
**对标**: SAP PB40 (Personnel Recruitment)
**版本**: 2.0
**更新日期**: 2026-03-14

---

## 1. 模块概述

### 1.1 功能范围

招聘管理 (RC) 模块管理企业招聘全流程，包括：

- **招聘需求** - 用人部门招聘申请
- **职位发布** - 招聘职位多渠道发布
- **候选人管理** - 简历收集与筛选
- **面试流程** - 面试安排与评估
- **录用管理** - Offer发放与入职

### 1.2 招聘流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    招聘流程 - 对标 SAP PB40                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ 招聘需求    │────►│ 需求审批    │────►│ 职位发布    │                   │
│  │ requisition │     │ approval    │     │ posting     │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│         │                                       │                          │
│         │                                       ▼                          │
│         │                               ┌─────────────┐                   │
│         │                               │ 候选人申请  │                   │
│         │                               │ application │                   │
│         │                               └─────────────┘                   │
│         │                                       │                          │
│         │                                       ▼                          │
│         │                               ┌─────────────┐                   │
│         │                               │ 简历筛选    │                   │
│         │                               │ screening   │                   │
│         │                               └─────────────┘                   │
│         │                                       │                          │
│         │                                       ▼                          │
│         │         ┌─────────────┐     ┌─────────────┐                     │
│         │         │ 录用/Offer  │◄────│ 面试评估    │                     │
│         │         │ offer       │     │ interview   │                     │
│         │         └─────────────┘     └─────────────┘                     │
│         │                                       │                          │
│         │                                       ▼                          │
│         │                               ┌─────────────┐                   │
│         └──────────────────────────────►│ 入职办理    │                   │
│                                         │ onboarding  │                   │
│                                         └─────────────┘                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 招聘需求

### 2.1 功能说明

记录用人部门的招聘需求，包括职位、人数、薪资范围等信息。

### 2.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| requisition_no | VARCHAR(20) | 需求编号 |
| position_id | UUID | 关联职位 |
| job_id | UUID | 关联职务 |
| org_unit_id | UUID | 所属组织 |
| job_title | VARCHAR(100) | 职位名称 |
| job_description | TEXT | 职位描述 |
| requirements | TEXT | 任职要求 |
| headcount | INTEGER | 需求人数 |
| filled_count | INTEGER | 已录用人数 |
| salary_min | DECIMAL(15,2) | 薪资下限 |
| salary_max | DECIMAL(15,2) | 薪资上限 |
| expected_date | DATE | 期望到岗日期 |
| recruit_type | VARCHAR(2) | 招聘类型 |
| approval_status | VARCHAR(2) | 审批状态 |
| requisition_status | VARCHAR(2) | 需求状态 |
| requester_id | UUID | 需求提交人 |

### 2.3 招聘类型

| 代码 | 说明 |
|------|------|
| NE | 新增编制 |
| RE | 替补 (离职替补) |
| TE | 临时用工 |

### 2.4 需求状态

| 代码 | 说明 |
|------|------|
| OP | 开放中 |
| PA | 部分完成 |
| CL | 已关闭 |
| CA | 已取消 |

---

## 3. 职位发布

### 3.1 功能说明

将招聘职位发布到各招聘渠道。

### 3.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| posting_no | VARCHAR(20) | 发布编号 |
| requisition_id | UUID | 关联需求 |
| posting_title | VARCHAR(200) | 发布标题 |
| job_description | TEXT | 职位描述 |
| channels | JSONB | 发布渠道 |
| publish_date | DATE | 发布日期 |
| expire_date | DATE | 过期日期 |
| work_location | VARCHAR(200) | 工作地点 |
| posting_status | VARCHAR(2) | 发布状态 |
| view_count | INTEGER | 浏览次数 |
| apply_count | INTEGER | 申请次数 |

### 3.3 发布渠道

| 渠道 | 说明 |
|------|------|
| INTERNAL | 内部招聘 |
| ZHAOPIN | 智联招聘 |
| LIEPIN | 猎聘 |
| BOSS | BOSS直聘 |
| JOB51 | 前程无忧 |
| LINKEDIN | 领英 |

---

## 4. 候选人管理

### 4.1 候选人主数据

| 字段 | 类型 | 说明 |
|------|------|------|
| full_name | VARCHAR(80) | 姓名 |
| gender | ENUM | 性别 |
| birth_date | DATE | 出生日期 |
| email | VARCHAR(100) | 邮箱 |
| mobile | VARCHAR(50) | 手机 |
| education | VARCHAR(4) | 最高学历 |
| school | VARCHAR(100) | 毕业院校 |
| work_years | INTEGER | 工作年限 |
| current_company | VARCHAR(100) | 当前公司 |
| expected_salary_min | DECIMAL(15,2) | 期望薪资下限 |
| expected_salary_max | DECIMAL(15,2) | 期望薪资上限 |
| expected_city | VARCHAR(50) | 期望城市 |
| source | VARCHAR(20) | 来源渠道 |
| resume_url | VARCHAR(500) | 简历URL |
| resume_text | TEXT | 简历文本 |
| candidate_status | VARCHAR(2) | 候选人状态 |
| employee_id | UUID | 已入职员工ID |

### 4.2 候选人状态

| 代码 | 说明 |
|------|------|
| NE | 新建 |
| SC | 筛选中 |
| IN | 面试中 |
| OF | Offer中 |
| HI | 已入职 |
| RE | 已拒绝 |

### 4.3 候选人申请

| 字段 | 类型 | 说明 |
|------|------|------|
| candidate_id | UUID | 候选人ID |
| posting_id | UUID | 职位发布ID |
| application_status | VARCHAR(2) | 申请状态 |
| applied_at | TIMESTAMP | 申请时间 |
| current_stage | VARCHAR(4) | 当前阶段 |
| recruiter_id | UUID | 招聘负责人 |
| rating | DECIMAL(2,1) | 综合评分 |
| notes | TEXT | 备注 |

---

## 5. 面试流程

### 5.1 面试安排

| 字段 | 类型 | 说明 |
|------|------|------|
| application_id | UUID | 申请ID |
| round_no | INTEGER | 面试轮次 |
| round_name | VARCHAR(50) | 轮次名称 |
| interview_type | VARCHAR(2) | 面试类型 |
| scheduled_date | DATE | 面试日期 |
| scheduled_time | TIME | 面试时间 |
| duration | INTEGER | 时长(分钟) |
| location | VARCHAR(200) | 面试地点 |
| interviewer_id | UUID | 面试官 |
| interview_status | VARCHAR(2) | 面试状态 |
| result | VARCHAR(2) | 面试结果 |
| feedback | TEXT | 面试反馈 |
| rating | DECIMAL(2,1) | 评分 |

### 5.2 面试类型

| 代码 | 说明 |
|------|------|
| PH | 电话面试 |
| VI | 视频面试 |
| PE | 现场面试 |
| WR | 笔试 |
| GR | 群面 |

### 5.3 面试结果

| 代码 | 说明 |
|------|------|
| PA | 通过 |
| RE | 拒绝 |
| PE | 待定 |
| CA | 取消 |

---

## 6. Offer管理

### 6.1 Offer信息

| 字段 | 类型 | 说明 |
|------|------|------|
| offer_no | VARCHAR(20) | Offer编号 |
| application_id | UUID | 申请ID |
| candidate_id | UUID | 候选人ID |
| position_id | UUID | 职位ID |
| job_title | VARCHAR(100) | 职位名称 |
| monthly_salary | DECIMAL(15,2) | 月薪 |
| annual_salary | DECIMAL(15,2) | 年薪 |
| probation_months | INTEGER | 试用期(月) |
| expected_join_date | DATE | 期望入职日期 |
| valid_until | DATE | Offer有效期 |
| offer_status | VARCHAR(2) | Offer状态 |
| approved_by | UUID | 审批人 |
| sent_at | TIMESTAMP | 发送时间 |

### 6.2 Offer状态

| 代码 | 说明 |
|------|------|
| DR | 草稿 |
| PE | 待审批 |
| AP | 已批准 |
| SE | 已发送 |
| AC | 已接受 |
| RE | 已拒绝 |
| EX | 已过期 |

---

## 7. 接口设计

### 7.1 招聘需求接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/recruitment/requisitions | GET/POST | 需求列表/创建 |
| /api/recruitment/requisitions/{id} | GET/PUT | 需求详情/更新 |
| /api/recruitment/requisitions/{id}/approve | POST | 审批需求 |
| /api/recruitment/requisitions/{id}/close | POST | 关闭需求 |

### 7.2 职位发布接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/recruitment/postings | GET/POST | 发布列表/创建 |
| /api/recruitment/postings/{id} | GET/PUT | 发布详情/更新 |
| /api/recruitment/postings/{id}/publish | POST | 发布职位 |
| /api/recruitment/postings/{id}/unpublish | POST | 下线职位 |

### 7.3 候选人接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/recruitment/candidates | GET/POST | 候选人列表/创建 |
| /api/recruitment/candidates/{id} | GET/PUT | 候选人详情/更新 |
| /api/recruitment/candidates/{id}/applications | GET | 候选人申请记录 |

### 7.4 面试接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/recruitment/interviews | GET/POST | 面试列表/创建 |
| /api/recruitment/interviews/{id} | GET/PUT | 面试详情/更新 |
| /api/recruitment/interviews/{id}/feedback | POST | 提交反馈 |

### 7.5 Offer接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/recruitment/offers | GET/POST | Offer列表/创建 |
| /api/recruitment/offers/{id} | GET/PUT | Offer详情/更新 |
| /api/recruitment/offers/{id}/approve | POST | 审批Offer |
| /api/recruitment/offers/{id}/send | POST | 发送Offer |
| /api/recruitment/offers/{id}/accept | POST | 接受Offer |
| /api/recruitment/offers/{id}/reject | POST | 拒绝Offer |

---

## 8. 相关文档

- [HR 模块总览](./00-HR-OVERVIEW.md)
- [OM 组织管理](./01-OM-DESIGN.md)
- [PA 人事管理](./02-PA-DESIGN.md)

---

## 9. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
