# 数据库设计与实现一致性分析报告

**分析日期**: 2026-03-16
**分析范围**: FI (财务会计) + HR (人力资源)
**设计文档**: 03-FICO-DESIGN.md, 07-HR-DESIGN.md

---

## 一、FI/CO 模块分析

### 1.1 设计文档期望的表结构

| 表名 | 说明 | SAP 对标 |
|------|------|----------|
| `fi_account` | 科目主数据 | SKA1/SKB1 |
| `fi_journal_entry_hdr` | 凭证头 (分区表) | BKPF |
| `fi_journal_entry_itm` | 凭证项 (分区表) | BSEG |
| `fi_account_balance` | 科目余额 | GLT0 |
| `fi_doc_type` | 凭证类型 | T003 |
| `fi_tax_code` | 税码配置 | A003 |

### 1.2 实际实现的表结构

| 实体类 | 表名 | 状态 |
|--------|------|------|
| `FinAccount` | `fin_account` | ✅ 已实现 |
| `FinVoucher` | `fin_voucher` | ✅ 已实现 |
| `FinVoucherEntry` | `fin_voucher_entry` | ✅ 已实现 |
| `FinAccountingPeriod` | `fin_accounting_period` | ✅ 已实现 |

### 1.3 差异分析

#### 1.3.1 命名规范不一致

| 设计 | 实现 | 影响 |
|------|------|------|
| `fi_` 前缀 | `fin_` 前缀 | 中等 - 需统一 |
| `fi_journal_entry_hdr` | `fin_voucher` | 高 - 概念映射不同 |
| `fi_journal_entry_itm` | `fin_voucher_entry` | 高 - 概念映射不同 |

#### 1.3.2 缺少的表

| 表名 | 说明 | 优先级 |
|------|------|--------|
| `fi_account_balance` | 科目余额表 (期间余额) | **高** |
| `fi_doc_type` | 凭证类型配置 | 中 |
| `fi_tax_code` | 税码配置 | 中 |
| AP/AR 相关表 | 应收/应付子模块 | 低 |

#### 1.3.3 字段差异 - `fin_account` vs `fi_account`

| 设计字段 | 实现字段 | 状态 |
|----------|----------|------|
| `chart_of_accounts` | ❌ 缺少 | SAP 科目表概念 |
| `group_account` | ❌ 缺少 | 集团科目 |
| `is_reconciliation` | ❌ 缺少 | 统驭科目标识 |
| `reconcil_account_type` | ❌ 缺少 | 统驭科目类型 |
| `field_status_group` | ❌ 缺少 | 字段状态组 |
| `open_item_mgmt` | ❌ 缺少 | 未清项管理 |
| `line_item_mgmt` | ❌ 缺少 | 行项目管理 |
| `cash_flow_type` | ❌ 缺少 | 现金流量分类 |
| `tax_category` | ❌ 缺少 | 税分类 |
| `valid_from/valid_to` | ❌ 缺少 | 有效期管理 |
| `is_auxiliary` | ✅ 有 | 辅助核算 |
| `opening_balance` | ✅ 有 | 期初余额 |
| `current_debit/credit` | ✅ 有 | 本期发生额 |

#### 1.3.4 字段差异 - `fin_voucher` vs `fi_journal_entry_hdr`

| 设计字段 | 实现字段 | 状态 |
|----------|----------|------|
| `fiscal_year` (分区键) | ❌ 缺少 | **关键** - 分区表设计 |
| `company_id` | ❌ 缺少 | 公司关联 |
| `period` | ❌ 缺少 (用 accounting_period) | 期间 |
| `exchange_rate` | ❌ 缺少 | 汇率 |
| `source_type` | ✅ 有 | 来源类型 |
| `is_reversed` | ❌ 缺少 | 冲销标识 |
| `reversed_doc_id` | ❌ 缺少 | 冲销凭证 |

#### 1.3.5 字段差异 - `fin_voucher_entry` vs `fi_journal_entry_itm`

| 设计字段 | 实现字段 | 状态 |
|----------|----------|------|
| `partner_id` | ❌ 缺少 | 业务伙伴 |
| `partner_type` | ❌ 缺少 | 业务伙伴类型 |
| `cost_center_id` | ❌ 缺少 | 成本中心 |
| `profit_center_id` | ❌ 缺少 | 利润中心 |
| `internal_order` | ❌ 缺少 | 内部订单 |
| `tax_code` | ❌ 缺少 | 税码 |
| `tax_amount` | ❌ 缺少 | 税额 |
| `payment_term` | ❌ 缺少 | 付款条款 |
| `due_date` | ❌ 缺少 | 到期日 |
| `clearing_date` | ❌ 缺少 | 清算日期 |

### 1.4 架构差异

| 方面 | 设计 | 实现 |
|------|------|------|
| 分区策略 | 按 `fiscal_year` 分区 | 无分区 |
| 主键类型 | UUID | Long (IDENTITY) |
| 余额存储 | 独立余额表 + 24个期间字段 | 科目表内嵌 |

---

## 二、HR 模块分析

### 2.1 设计文档期望的表结构

#### OM (组织管理) - SAP HRP1000 风格

| 表名 | 说明 | SAP 对标 |
|------|------|----------|
| `hr_om_object` | OM 对象主表 | HRP1000 |
| `hr_om_org_unit_detail` | 组织单元详情 | HRP1000 (O类型) |
| `hr_om_job_detail` | 职务详情 | HRP1000 (C类型) |
| `hr_om_position_detail` | 职位详情 | HRP1000 (S类型) |
| `hr_om_relationship` | 对象关系表 | HRP1001 |

#### PA (人事管理) - InfoType 风格

| 表名 | 说明 | SAP 对标 |
|------|------|----------|
| `hr_employee` | 员工主数据 | PA0001 |
| `hr_it0000_actions` | 操作/状态 | IT0000 |
| `hr_it0001_org_assignment` | 组织分配 | IT0001 |
| `hr_it0002_personal_data` | 个人数据 | IT0002 |
| ... | 更多 InfoType | ... |

### 2.2 实际实现的表结构

| 实体类 | 表名 | 状态 |
|--------|------|------|
| `HrmDepartment` | `hrm_department` | ✅ 已实现 |
| `HrmEmployee` | `hrm_employee` | ✅ 已实现 |
| `HrmPosition` | `hrm_position` | ✅ 已实现 |
| `HrmJob` | `hrm_job` | ✅ 已实现 |
| `HrmAttendance` | `hrm_attendance` | ✅ 已实现 |
| `HrmLeave` | `hrm_leave` | ✅ 已实现 |
| `HrmPayrollResult` | `hrm_payroll_result` | ✅ 已实现 |
| `HrmRecruitment` | `hrm_recruitment` | ✅ 已实现 |
| `HrmCandidate` | `hrm_candidate` | ✅ 已实现 |

### 2.3 差异分析

#### 2.3.1 架构差异 (重大)

| 方面 | 设计 | 实现 |
|------|------|------|
| **架构模式** | SAP OM + PA 架构 | 传统独立表结构 |
| **对象模型** | 通用 OM 对象 + 关系表 | 独立业务表 |
| **InfoType** | 时间有效性 InfoType 架构 | 单表存储 |
| **关系管理** | `hr_om_relationship` (HRP1001) | 外键直接关联 |

**设计意图**: 采用 SAP HRP1000/HRP1001 的通用对象-关系模型，支持复杂组织架构、矩阵汇报、时间有效性等企业级场景。

**实现现状**: 采用传统的独立表结构，每个业务实体一个表，简化了实现但降低了灵活性。

#### 2.3.2 缺少的核心表

| 表名 | 说明 | 影响 |
|------|------|------|
| `hr_om_object` | OM 对象主表 | **高** - SAP 架构核心 |
| `hr_om_relationship` | 对象关系表 | **高** - HRP1001 |
| `hr_it0000_actions` | 操作记录 | 中 - 人事操作追溯 |
| `hr_it0001_org_assignment` | 组织分配历史 | 中 - 时间有效性 |
| 假期余额表 | 员工假期余额 | 中 |

#### 2.3.3 命名规范差异

| 设计 | 实现 |
|------|------|
| `hr_` 前缀 | `hrm_` 前缀 |
| `employee_number` | `employee_no` |
| `full_name` | `employee_name` |

#### 2.3.4 字段差异 - `hrm_employee` vs `hr_employee`

| 设计字段 | 实现字段 | 状态 |
|----------|----------|------|
| `om_object_id` | ❌ 缺少 | OM 对象关联 |
| `employee_status` | `work_status` ✅ | 状态 (命名不同) |
| `action_reason` | ❌ 缺少 | 离职原因编码 |
| `seniority` | ❌ 缺少 | 司龄 |
| `probation_end` | `regular_date` ✅ | 试用期结束 |
| `termination_date` | `resign_date` ✅ | 离职日期 |
| `termination_type` | ❌ 缺少 | 离职类型编码 |
| `id_number` | `id_card` ✅ | 身份证号 |
| `email_work` | `email` ✅ | 工作邮箱 |
| `original_hire` | ❌ 缺少 | 最初入职日 |

#### 2.3.5 字段差异 - `hrm_department` vs `hr_om_org_unit_detail`

| 设计字段 | 实现字段 | 状态 |
|----------|----------|------|
| `object_id` (OM关联) | ❌ 缺少 | OM 对象关联 |
| `org_code` | `dept_code` ✅ | 编码 |
| `parent_object_id` | `parent_id` ✅ | 父级 |
| `org_level` | `dept_level` ✅ | 层级 |
| `path` | `dept_path` ✅ | 路径 |
| `company_id` | `company_code` ✅ | 公司 |
| `headcount` | ❌ 缺少 | 当前人数 |
| `max_headcount` | ❌ 缺少 | 编制上限 |
| `valid_from/to` | ✅ 有 | 有效期 |

#### 2.3.6 主键类型差异

| 方面 | 设计 | 实现 |
|------|------|------|
| 主键类型 | UUID | Long (IDENTITY) |
| 外键类型 | UUID | Long |

---

## 三、总结与建议

### 3.1 一致性评分

| 模块 | 命名一致性 | 结构一致性 | 字段一致性 | 架构一致性 | 总体评分 |
|------|------------|------------|------------|------------|----------|
| FI/CO | 60% | 50% | 40% | 30% | **45%** |
| HR | 70% | 60% | 55% | **20%** | **51%** |

### 3.2 主要问题

1. **架构偏离 (HR 严重)**
   - 设计采用 SAP OM + InfoType 架构
   - 实现采用传统独立表结构
   - 影响系统扩展性和灵活性

2. **缺少关键功能表 (FI)**
   - `fi_account_balance` 科目余额表
   - 按年度分区的凭证表设计

3. **字段简化**
   - 大量 SAP 风格字段未实现
   - 业务伙伴、成本中心等维度字段缺失

4. **命名规范不统一**
   - FI: `fi_` vs `fin_`
   - HR: `hr_` vs `hrm_`

### 3.3 建议方案

#### 方案 A: 对齐实现 (推荐 - 成本较低)

1. **更新设计文档**以匹配当前实现
2. 在现有结构基础上补充缺失字段
3. 添加必要的功能表 (如科目余额表)

#### 方案 B: 重构实现 (成本较高)

1. HR 模块采用 OM + InfoType 架构重构
2. FI 模块实现分区表和余额表
3. 统一命名规范为 `fi_` / `hr_`

#### 方案 C: 渐进对齐

1. **短期**: 补充 FI 余额表和关键字段
2. **中期**: 为 HR 添加关系表支持复杂架构
3. **长期**: 考虑 UUID 主键迁移

### 3.4 优先级建议

| 优先级 | 模块 | 行动项 |
|--------|------|--------|
| **P0** | FI | 实现 `fi_account_balance` 科目余额表 |
| **P1** | FI | 凭证表添加 `company_id`, `fiscal_year` 字段 |
| **P1** | HR | 添加员工假期余额表 |
| **P2** | FI | 凭证分录添加成本中心、业务伙伴维度 |
| **P2** | HR | 考虑添加 `hr_om_relationship` 关系表 |
| **P3** | ALL | 统一命名规范 |

---

## 附录: 详细表对照

### A.1 FI 模块表对照

```
设计文档                          实际实现
─────────────────────────────────────────────────
fi_account                    →   fin_account ✅
fi_journal_entry_hdr          →   fin_voucher ✅
fi_journal_entry_itm          →   fin_voucher_entry ✅
fi_account_balance            →   ❌ 未实现
fi_doc_type                   →   ❌ 未实现
fi_tax_code                   →   ❌ 未实现
```

### A.2 HR 模块表对照

```
设计文档                          实际实现
─────────────────────────────────────────────────
hr_om_object                  →   ❌ 未实现 (架构差异)
hr_om_org_unit_detail         →   hrm_department ✅ (简化版)
hr_om_job_detail              →   hrm_job ✅ (简化版)
hr_om_position_detail         →   hrm_position ✅ (简化版)
hr_om_relationship            →   ❌ 未实现 (架构差异)
hr_employee                   →   hrm_employee ✅
hr_it0000_actions             →   ❌ 未实现
hr_it0001_org_assignment      →   ❌ 未实现
考勤/请假/薪酬                 →   hrm_attendance/leave/payroll ✅
```

---

**报告生成**: Claude Code
**最后更新**: 2026-03-16
