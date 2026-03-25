# PA 人事管理功能设计

**模块**: Personnel Administration (人事管理)
**对标**: SAP PA0000-PA9999 (Personnel Administration)
**版本**: 2.0
**更新日期**: 2026-03-14

---

## 1. 模块概述

### 1.1 功能范围

人事管理 (PA) 模块管理员工全生命周期信息，包括：

- **员工主数据** - 员工编号、状态、基本信息
- **个人信息** - 姓名、身份证、婚姻状况等
- **组织分配** - 所属组织、职位、成本中心
- **薪酬信息** - 基本工资、工资项
- **地址信息** - 住址、通讯地址、紧急联系人
- **银行信息** - 工资发放账户
- **家庭信息** - 家庭成员、受抚养人
- **教育背景** - 学历、学位、培训经历
- **通讯方式** - 邮箱、手机、系统账号
- **中国本地化** - 社保、公积金、个税专项扣除

### 1.2 InfoType 架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    PA InfoType 架构 - 对标 SAP PA0000-PA9999                │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  基础信息 (0000-0999)                                                       │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐              │
│  │ IT0000  │ │ IT0001  │ │ IT0002  │ │ IT0006  │ │ IT0007  │              │
│  │ 操作    │ │ 组织分配│ │ 个人数据│ │ 地址    │ │ 排班    │              │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘              │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐              │
│  │ IT0008  │ │ IT0009  │ │ IT0021  │ │ IT0022  │ │ IT0024  │              │
│  │ 基本工资│ │ 银行    │ │ 家庭    │ │ 教育    │ │ 资格    │              │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘              │
│  ┌─────────┐                                                                │
│  │ IT0105  │                                                                │
│  │ 通讯    │                                                                │
│  └─────────┘                                                                │
│                                                                             │
│  时间管理 (2000-2999)                                                       │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐              │
│  │ IT2001  │ │ IT2002  │ │ IT2003  │ │ IT2005  │ │ IT2006  │              │
│  │ 缺勤    │ │ 出勤    │ │ 出差    │ │ 加班    │ │ 假期配额│              │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘              │
│                                                                             │
│  中国本地化 (0400-0599)                                                     │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐                                       │
│  │ IT0406  │ │ IT0588  │ │ IT0591  │                                       │
│  │ 税务    │ │ 公积金  │ │ 社保    │                                       │
│  └─────────┘ └─────────┘ └─────────┘                                       │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 员工主数据

### 2.1 员工状态

| 状态 | 代码 | 说明 |
|------|------|------|
| 在职 | AC | Active - 正常在职 |
| 休假 | IN | Inactive - 长期休假 |
| 离职 | TE | Terminated - 已离职 |
| 退休 | RE | Retired - 已退休 |

### 2.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| employee_number | VARCHAR(8) | 员工编号 (与 sys_user 逻辑关联) |
| om_object_id | UUID | OM 对象ID (P类型) |
| employee_status | VARCHAR(2) | 员工状态 |
| full_name | VARCHAR(80) | 全名 (冗余) |
| gender | ENUM | 性别 |
| birth_date | DATE | 出生日期 |
| id_number | VARCHAR(20) | 身份证号 |
| org_unit_id | UUID | 组织单元 (冗余) |
| position_id | UUID | 职位 (冗余) |
| job_id | UUID | 职务 (冗余) |
| hire_date | DATE | 入职日期 |
| seniority | DECIMAL(5,1) | 司龄 (年) |
| probation_end | DATE | 试用期结束日 |
| termination_date | DATE | 离职日期 |

---

## 3. IT0000 操作/状态

### 3.1 功能说明

记录员工的人事操作和状态变更历史，是员工全生命周期管理的核心。

### 3.2 操作类型 (对标 SAP MASSN)

| 代码 | 操作 | 说明 |
|------|------|------|
| 01 | 招聘入职 | Hiring |
| 02 | 离职 | Termination |
| 03 | 调动/调岗 | Transfer |
| 04 | 转正 | Probation Complete |
| 05 | 晋升 | Promotion |
| 06 | 降职 | Demotion |
| 07 | 停薪留职 | Leave Without Pay |
| 08 | 复职 | Reinstatement |
| 09 | 退休 | Retirement |
| 10 | 合同续签 | Contract Renewal |

### 3.3 业务规则

1. **时间约束 T1**: 无间隙、无重叠
2. **审批流程**: 状态变更操作需审批
3. **历史追溯**: 保留所有历史操作记录

---

## 4. IT0001 组织分配

### 4.1 功能说明

记录员工的组织归属信息，包括部门、职位、职务、成本中心等。

### 4.2 业务规则

1. **时间约束 T1**: 必须无间隙、无重叠
2. **自动更新**: 员工主数据冗余字段自动同步
3. **审批流程**: 调岗操作需审批

### 4.3 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| org_unit_id | UUID | 组织单元 |
| position_id | UUID | 职位 |
| job_id | UUID | 职务 |
| cost_center_id | UUID | 成本中心 |
| manager_id | UUID | 直线经理 |
| employee_group | VARCHAR(1) | 员工组 |
| employee_subgroup | VARCHAR(2) | 员工子组 |
| company_id | UUID | 公司 |
| personnel_area | VARCHAR(4) | 人事范围 |
| personnel_subarea | VARCHAR(4) | 人事子范围 |

### 4.4 员工分类 (对标 SAP PERSG/PERSK)

**员工组 (PERSG)**:
| 代码 | 说明 |
|------|------|
| 1 | 正式员工 |
| 2 | 合同工 |
| 3 | 实习生 |
| 4 | 外包人员 |

---

## 5. IT0002 个人数据

### 5.1 功能说明

记录员工的个人基本信息。

### 5.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| last_name | VARCHAR(40) | 姓 |
| first_name | VARCHAR(40) | 名 |
| gender | ENUM | 性别 |
| birth_date | DATE | 出生日期 |
| nationality | VARCHAR(3) | 国籍 |
| ethnicity | VARCHAR(3) | 民族 |
| marital_status | VARCHAR(1) | 婚姻状况 |
| id_type | VARCHAR(4) | 证件类型 |
| id_number | VARCHAR(20) | 证件号码 |
| native_place | VARCHAR(100) | 籍贯 |
| household_type | VARCHAR(1) | 户籍类型 |
| political_status | VARCHAR(2) | 政治面貌 |
| photo_url | VARCHAR(500) | 照片 |

### 5.3 婚姻状况 (对标 SAP FAMST)

| 代码 | 说明 |
|------|------|
| 1 | 未婚 |
| 2 | 已婚 |
| 3 | 丧偶 |
| 4 | 离婚 |

---

## 6. IT0006 地址信息

### 6.1 子类型 (SUBTY)

| 代码 | 说明 |
|------|------|
| 1 | 永久地址 |
| 2 | 临时地址 |
| 3 | 邮寄地址 |
| 4 | 紧急联系人 |

### 6.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| country | VARCHAR(3) | 国家 |
| province | VARCHAR(50) | 省份 |
| city | VARCHAR(50) | 城市 |
| district | VARCHAR(50) | 区县 |
| street | VARCHAR(200) | 街道地址 |
| postal_code | VARCHAR(10) | 邮编 |
| phone | VARCHAR(50) | 电话 |
| mobile | VARCHAR(50) | 手机 |
| contact_name | VARCHAR(80) | 紧急联系人姓名 |
| contact_relation | VARCHAR(20) | 与本人关系 |
| contact_phone | VARCHAR(50) | 紧急联系人电话 |

---

## 7. IT0008 基本工资

### 7.1 功能说明

记录员工的基本薪酬信息，支持多个工资项组合。

### 7.2 业务规则

1. **时间约束 T2**: 可有间隙、不可重叠
2. **工资项**: 支持多个工资类型组合
3. **总额计算**: 自动汇总工资项金额

### 7.3 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| pay_type | VARCHAR(2) | 薪酬类型 |
| pay_area | VARCHAR(2) | 薪酬区域 |
| pay_grade | VARCHAR(4) | 薪等 |
| pay_level | VARCHAR(2) | 薪级 |
| currency_id | UUID | 货币 |
| total_amount | DECIMAL(15,2) | 工资总额 |
| annual_salary | DECIMAL(15,2) | 年薪 |

### 7.4 工资类型 (对标 SAP LGART)

| 类别 | 代码前缀 | 说明 |
|------|----------|------|
| BA | BA** | 基本工资 |
| AL | AL** | 津贴 |
| BO | BO** | 奖金 |
| DE | DE** | 扣款 |

---

## 8. IT0009 银行信息

### 8.1 子类型 (SUBTY)

| 代码 | 说明 |
|------|------|
| 0 | 主账户 (工资发放) |
| 1 | 其他账户 |

### 8.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| bank_code | VARCHAR(20) | 银行编码 |
| bank_name | VARCHAR(100) | 银行名称 |
| branch_name | VARCHAR(100) | 支行名称 |
| account_number | VARCHAR(30) | 账号 |
| account_name | VARCHAR(80) | 户名 |
| account_type | VARCHAR(2) | 账户类型 |
| payment_method | VARCHAR(2) | 支付方式 |
| is_primary | BOOLEAN | 是否主账户 |

---

## 9. IT0021 家庭成员

### 9.1 子类型 (SUBTY)

| 代码 | 说明 |
|------|------|
| 01 | 配偶 |
| 02 | 子女 |
| 03 | 父母 |
| 04 | 兄弟姐妹 |

### 9.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| full_name | VARCHAR(80) | 姓名 |
| gender | ENUM | 性别 |
| birth_date | DATE | 出生日期 |
| id_number | VARCHAR(20) | 身份证号 |
| employer | VARCHAR(100) | 工作单位 |
| occupation | VARCHAR(50) | 职业 |
| is_dependent | BOOLEAN | 是否受抚养 |
| phone | VARCHAR(50) | 联系电话 |

---

## 10. IT0022 教育背景

### 10.1 子类型 (SUBTY)

| 代码 | 说明 |
|------|------|
| 01 | 高中 |
| 02 | 大专 |
| 03 | 本科 |
| 04 | 硕士 |
| 05 | 博士 |

### 10.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| school_name | VARCHAR(100) | 学校名称 |
| school_country | VARCHAR(3) | 学校所在国家 |
| school_type | VARCHAR(2) | 学校类型 |
| major | VARCHAR(100) | 专业 |
| degree | VARCHAR(4) | 学位 |
| start_date | DATE | 入学日期 |
| end_date | DATE | 毕业日期 |
| is_graduated | BOOLEAN | 是否毕业 |
| gpa | DECIMAL(3,2) | GPA |
| certificate_no | VARCHAR(50) | 证书编号 |
| is_highest | BOOLEAN | 是否最高学历 |

---

## 11. IT0105 通讯方式

### 11.1 子类型 (SUBTY)

| 代码 | 说明 |
|------|------|
| 0001 | 系统用户 |
| 0010 | 邮箱 |
| 0020 | 手机 |

### 11.2 核心字段

| 字段 | 类型 | 说明 |
|------|------|------|
| comm_id | VARCHAR(100) | 通讯ID |
| comm_method | VARCHAR(4) | 通讯方式 |
| system_user | VARCHAR(50) | 系统用户名 |
| system_id | UUID | 关联系统用户 |
| is_primary | BOOLEAN | 是否主要联系方式 |

---

## 12. 中国本地化

### 12.1 IT0591 社保信息

| 字段 | 类型 | 说明 |
|------|------|------|
| policy_id | UUID | 社保政策 |
| pension_base | DECIMAL(10,2) | 养老基数 |
| medical_base | DECIMAL(10,2) | 医疗基数 |
| unemployment_base | DECIMAL(10,2) | 失业基数 |
| pension_personal | DECIMAL(10,2) | 养老个人 |
| medical_personal | DECIMAL(10,2) | 医疗个人 |
| unemployment_personal | DECIMAL(10,2) | 失业个人 |

### 12.2 IT0588 公积金信息

| 字段 | 类型 | 说明 |
|------|------|------|
| fund_config_id | UUID | 公积金配置 |
| fund_base | DECIMAL(10,2) | 公积金基数 |
| fund_personal | DECIMAL(10,2) | 个人公积金 |
| fund_company | DECIMAL(10,2) | 公司公积金 |
| fund_account | VARCHAR(30) | 公积金账号 |

### 12.3 IT0406 个税专项扣除

| 扣除类型 | 代码 | 月度金额 |
|----------|------|----------|
| 子女教育 | 01 | 2000/孩 |
| 继续教育 | 02 | 400 |
| 大病医疗 | 03 | 据实 |
| 住房贷款利息 | 04 | 1000 |
| 住房租金 | 05 | 800-1500 |
| 赡养老人 | 06 | 2000 |
| 3岁以下婴幼儿照护 | 07 | 2000/孩 |

---

## 13. 人事操作流程

### 13.1 入职流程 (对标 SAP PA40 Hiring Action)

```
1. 创建员工主数据
   └── hr_employee: 员工编号、姓名、状态

2. 创建 IT0000 操作记录
   └── action_type = '01' (招聘入职)

3. 创建 IT0001 组织分配
   └── 部门、职位、成本中心

4. 创建 IT0002 个人数据
   └── 姓名、性别、出生日期、身份证

5. 创建 IT0008 基本工资
   └── 工资项、薪酬等级

6. 创建 IT0009 银行信息
   └── 工资发放账户

7. 创建 OM 关系
   └── 职位-人员关系 (008)
```

### 13.2 离职流程

```
1. 创建 IT0000 操作记录
   └── action_type = '02' (离职)

2. 结束当前 InfoType 记录
   └── valid_to = 离职日期

3. 更新员工状态
   └── employee_status = 'TE'

4. 结束 OM 关系
   └── 职位-人员关系 valid_to = 离职日期

5. 更新职位状态
   └── position_status = 'VA' (空缺)
```

---

## 14. 接口设计

### 14.1 员工管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/employees | GET/POST | 员工列表/创建 |
| /api/employees/{id} | GET/PUT/DELETE | 员工详情/更新/删除 |
| /api/employees/{id}/infotypes | GET | 获取员工所有 InfoType |
| /api/employees/{id}/infotypes/{it} | GET/POST/PUT | InfoType CRUD |
| /api/employees/{id}/actions | POST | 执行人事操作 |

### 14.2 人事操作接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/employees/{id}/hire | POST | 入职 |
| /api/employees/{id}/terminate | POST | 离职 |
| /api/employees/{id}/transfer | POST | 调动 |
| /api/employees/{id}/promote | POST | 晋升 |

---

## 15. 相关文档

- [HR 模块总览](./00-HR-OVERVIEW.md)
- [OM 组织管理](./01-OM-DESIGN.md)
- [PT 时间管理](./03-PT-DESIGN.md)
- [PY 薪酬管理](./04-PY-DESIGN.md)

---

## 16. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
| 2.0 | 2026-03-14 | 完善中国本地化 InfoType |
