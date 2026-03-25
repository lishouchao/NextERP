# PS 模块一致性检查报告

**检查日期**: 2026-03-14
**检查范围**: PS 功能设计文档 vs 数据库设计文档
**状态**: ✅ 已同步 (高优先级项已完成)

---

## 1. 检查结果摘要

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 项目定义 | ✅ 一致 | ps_project 结构完整 |
| 项目类型配置 | ✅ 一致 | ps_project_type 存在 |
| WBS元素 | ✅ 一致 | ps_wbs_element 结构完整 |
| WBS层级视图 | ✅ 一致 | v_ps_wbs_hierarchy 存在 |
| 网络主数据 | ✅ 一致 | ps_network 结构完整 |
| 网络活动 | ✅ 一致 | ps_network_activity 结构完整 |
| 活动关系 | ✅ 一致 | ps_activity_relation 存在 |
| 活动确认 | ✅ 已补充 | ps_activity_confirmation 已添加 |
| 里程碑 | ✅ 一致 | ps_milestone 结构完整 |
| 预算头 | ✅ 一致 | ps_budget_hdr 结构完整 |
| 预算明细 | ✅ 一致 | ps_budget_item 存在 |
| 可用性控制 | ✅ 一致 | ps_availability_control 存在 |
| 项目凭证 | ✅ 一致 | ps_document 结构完整 |
| 项目结算 | ✅ 已补充 | ps_settlement_rule, ps_settlement_doc 已添加 |

---

## 2. 详细对比分析

### 2.1 项目定义

#### 数据库设计表: ps_project

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| project_number | ✅ | ✅ | 一致 |
| project_name | ✅ | ✅ | 一致 |
| project_type | ✅ | ✅ | 一致 |
| project_profile | ✅ | ✅ | 一致 |
| company_id | ✅ | ✅ | 一致 |
| project_manager | ✅ | ✅ | 一致 |
| planned_start/finish_date | ✅ | ✅ | 一致 |
| customer_id | ✅ | ✅ | 一致 |
| total_budget | ✅ | ✅ | 一致 |
| project_status | ✅ | ✅ | 一致 |
| system_status | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

### 2.2 WBS 工作分解结构

#### 数据库设计表: ps_wbs_element

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| wbs_number | ✅ | ✅ | 一致 |
| wbs_name | ✅ | ✅ | 一致 |
| parent_id | ✅ | ✅ | 一致 |
| level | ✅ | ✅ | 一致 |
| wbs_type | ✅ | ✅ | 一致 |
| wbs_category | ✅ | ✅ | 一致 |
| planned_cost | ✅ | ✅ | 一致 |
| actual_cost | ✅ | ✅ | 一致 |
| budget | ✅ | ✅ | 一致 |
| progress_percent | ✅ | ✅ | 一致 |
| settlement_rule | ✅ | ✅ | 一致 |
| has_milestone | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

### 2.3 网络与活动

#### 数据库设计表: ps_network, ps_network_activity

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| network_number | ✅ | ✅ | 一致 |
| network_type | ✅ | ✅ | 一致 |
| activity_number | ✅ | ✅ | 一致 |
| activity_type | ✅ | ✅ | 一致 |
| control_key | ✅ | ✅ | 一致 |
| planned_work | ✅ | ✅ | 一致 |
| actual_work | ✅ | ✅ | 一致 |
| confirmation_count | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

#### 活动关系: ps_activity_relation

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| predecessor_id | ✅ | ✅ | 一致 |
| successor_id | ✅ | ✅ | 一致 |
| relation_type (FS/SS/FF/SF) | ✅ | ✅ | 一致 |
| lag_days | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

### 2.4 活动确认 (CAT2/CN25)

#### 功能设计定义

功能设计文档 [02-PS-NETWORK.md](../../docs/PS/02-PS-NETWORK.md) 中定义了活动确认功能：

- 确认类型: 工时确认、完成确认、部分确认、最终确认
- 确认记录: 确认编号、确认日期、实际工时、剩余工时、进度百分比

#### 数据库设计状态

| 表名 | 说明 | 状态 |
|------|------|------|
| ps_activity_confirmation | 活动确认记录 | ❌ 缺失 |

#### 建议新增表

```sql
-- 活动确认 (对标 SAP CAT2/CN25)
CREATE TABLE ps_activity_confirmation (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 确认信息
    confirmation_number VARCHAR(12) NOT NULL,     -- 确认编号
    confirmation_date DATE NOT NULL DEFAULT CURRENT_DATE,

    -- 网络活动关联
    network_id      UUID NOT NULL REFERENCES ps_network(id),
    activity_id     UUID NOT NULL REFERENCES ps_network_activity(id),
    activity_number VARCHAR(4),

    -- 确认类型
    confirmation_type VARCHAR(2) NOT NULL,        -- 确认类型
    -- 01:工时确认 02:完成确认 03:部分确认 04:最终确认

    -- 工时
    actual_work     DECIMAL(10,2),                -- 实际工时
    remaining_work  DECIMAL(10,2),                -- 剩余工时
    work_unit       VARCHAR(3) DEFAULT 'H',       -- 工时单位

    -- 进度
    progress_percent DECIMAL(5,2),                -- 完成百分比

    -- 日期
    actual_start    DATE,                         -- 实际开始
    actual_finish   DATE,                         -- 实际完成

    -- 最终确认
    final_confirmation BOOLEAN DEFAULT FALSE,

    -- 成本
    actual_cost     DECIMAL(15,2),                -- 实际成本

    -- 确认人
    confirmed_by    UUID REFERENCES hr_employee(id),

    -- 备注
    remark          TEXT,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, confirmation_number)
);

-- 索引
CREATE INDEX idx_ps_conf_network ON ps_activity_confirmation (network_id);
CREATE INDEX idx_ps_conf_activity ON ps_activity_confirmation (activity_id);
CREATE INDEX idx_ps_conf_date ON ps_activity_confirmation (confirmation_date);
```

---

### 2.5 项目结算 (CJ88)

#### 功能设计定义

功能设计文档 [01-PS-PROJECT.md](../../docs/PS/01-PS-PROJECT.md) 中定义了结算规则：

- 结算规则类型: 自动结算、手动结算、按比例、100%
- 结算接收方: 成本中心、订单、网络、WBS、资产、销售订单、利润中心、G/L科目

#### 数据库设计状态

| 表名 | 说明 | 状态 |
|------|------|------|
| ps_settlement_rule | 结算规则配置 | ❌ 缺失 |
| ps_settlement_doc | 结算凭证记录 | ❌ 缺失 |

#### 建议新增表

```sql
-- 结算规则 (对标 SAP COBR)
CREATE TABLE ps_settlement_rule (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- WBS关联
    wbs_id          UUID NOT NULL REFERENCES ps_wbs_element(id),
    wbs_number      VARCHAR(24),

    -- 规则信息
    rule_number     VARCHAR(3) NOT NULL,          -- 规则号
    settlement_type VARCHAR(2) NOT NULL,          -- 结算类型
    -- 01:自动结算 02:手动结算 03:按比例 04:100%

    -- 接收方
    receiver_type   VARCHAR(2) NOT NULL,          -- 接收方类型
    -- 01:成本中心 02:订单 03:网络 04:WBS 05:资产 06:销售订单 07:利润中心 08:G/L科目
    receiver_id     UUID,                         -- 接收方ID
    receiver_code   VARCHAR(24),                  -- 接收方编码

    -- 比例
    settlement_percent DECIMAL(5,2) DEFAULT 100,  -- 结算比例%

    -- 有效期
    valid_from      DATE,
    valid_to        DATE DEFAULT '9999-12-31',

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (wbs_id, rule_number)
);

-- 结算凭证 (对标 SAP CJ88)
CREATE TABLE ps_settlement_doc (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 凭证信息
    settlement_number VARCHAR(12) NOT NULL,       -- 结算凭证号
    settlement_date DATE NOT NULL DEFAULT CURRENT_DATE,
    fiscal_year     INTEGER NOT NULL,
    fiscal_period   INTEGER NOT NULL,

    -- 项目关联
    project_id      UUID NOT NULL REFERENCES ps_project(id),
    wbs_id          UUID REFERENCES ps_wbs_element(id),

    -- 金额
    settlement_amount DECIMAL(15,2) NOT NULL,     -- 结算金额
    currency_id     UUID REFERENCES core_currency(id),

    -- 接收方
    rule_id         UUID REFERENCES ps_settlement_rule(id),
    receiver_type   VARCHAR(2),
    receiver_id     UUID,
    receiver_code   VARCHAR(24),

    -- FI凭证
    fi_document_id  UUID,                         -- 关联FI凭证
    fi_document_number VARCHAR(10),

    -- 状态
    settlement_status VARCHAR(2) DEFAULT '01',    -- 01:已创建 02:已过账 03:已冲销

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    posted_at       TIMESTAMP,
    posted_by       UUID,

    UNIQUE (tenant_id, settlement_number, fiscal_year)
);

-- 索引
CREATE INDEX idx_ps_settle_rule_wbs ON ps_settlement_rule (wbs_id);
CREATE INDEX idx_ps_settle_doc_project ON ps_settlement_doc (project_id);
CREATE INDEX idx_ps_settle_doc_wbs ON ps_settlement_doc (wbs_id);
CREATE INDEX idx_ps_settle_doc_date ON ps_settlement_doc (settlement_date);
```

---

### 2.6 预算管理

#### 数据库设计表: ps_budget_hdr, ps_budget_item, ps_availability_control

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| budget_type | ✅ | ✅ | 一致 |
| fiscal_year | ✅ | ✅ | 一致 |
| total_budget | ✅ | ✅ | 一致 |
| released_budget | ✅ | ✅ | 一致 |
| used_budget | ✅ | ✅ | 一致 |
| period_01~12 | ✅ | ✅ | 一致 |
| tolerance_limit | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

---

### 2.7 里程碑

#### 数据库设计表: ps_milestone

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| milestone_number | ✅ | ✅ | 一致 |
| milestone_name | ✅ | ✅ | 一致 |
| planned_date | ✅ | ✅ | 一致 |
| milestone_percent | ✅ | ✅ | 一致 |
| usage_type | ✅ | ✅ | 一致 |
| billing_rule | ✅ | ✅ | 一致 |
| billing_percent | ✅ | ✅ | 一致 |
| is_completed | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

---

## 3. 已补充的数据库表

### 3.1 高优先级 ✅ 已完成

| 模块 | 表名 | 说明 | 状态 |
|------|------|------|------|
| PS-EXEC | ps_activity_confirmation | 活动确认记录 | ✅ 已添加 |
| PS-SETTLE | ps_settlement_rule | 结算规则配置 | ✅ 已添加 |
| PS-SETTLE | ps_settlement_doc | 结算凭证 | ✅ 已添加 |

### 3.2 中优先级 ⏸️ 待定

| 模块 | 表名 | 说明 |
|------|------|------|
| PS-BUDGET | ps_budget_history | 预算变更历史 |
| PS-TRIGGER | ps_milestone_trigger | 里程碑触发规则 |

---

## 4. 一致性修复建议

### 4.1 高优先级 ✅ 已完成

1. **创建活动确认表**
   - ps_activity_confirmation ✅ 已添加
   - 支持工时确认和进度更新

2. **创建结算规则表**
   - ps_settlement_rule ✅ 已添加
   - ps_settlement_doc ✅ 已添加
   - 支持项目成本结算

### 4.2 中优先级 ⏸️ 待定

1. **预算变更历史**
   - 记录预算补充/返回/转移历史

2. **里程碑触发规则**
   - 配置里程碑触发的自动动作

---

## 5. 数据库表对比汇总

| 功能领域 | 功能设计 | 数据库设计 | 状态 |
|----------|----------|------------|------|
| 项目定义 | 项目主数据 | ps_project | ✅ 一致 |
| 项目配置 | 项目类型 | ps_project_type | ✅ 一致 |
| WBS管理 | WBS元素 | ps_wbs_element | ✅ 一致 |
| WBS视图 | 层级视图 | v_ps_wbs_hierarchy | ✅ 一致 |
| 网络管理 | 网络 | ps_network | ✅ 一致 |
| 活动管理 | 网络活动 | ps_network_activity | ✅ 一致 |
| 活动关系 | 关系类型 | ps_activity_relation | ✅ 一致 |
| 活动确认 | 确认记录 | ps_activity_confirmation | ✅ 已补充 |
| 里程碑 | 里程碑 | ps_milestone | ✅ 一致 |
| 预算管理 | 预算头 | ps_budget_hdr | ✅ 一致 |
| 预算明细 | 预算项 | ps_budget_item | ✅ 一致 |
| 预算控制 | AVC | ps_availability_control | ✅ 一致 |
| 项目凭证 | 凭证记录 | ps_document | ✅ 一致 |
| 结算规则 | 规则配置 | ps_settlement_rule | ✅ 已补充 |
| 结算凭证 | 结算记录 | ps_settlement_doc | ✅ 已补充 |

---

## 6. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 - 完成PS模块一致性检查 |
| 1.1 | 2026-03-14 | 补充活动确认、结算规则、结算凭证表 |
