# FICO 模块一致性检查报告

**检查日期**: 2026-03-14
**检查范围**: FICO 功能设计文档 vs 数据库设计文档
**状态**: ✅ 已同步

---

## 1. 检查结果摘要

| 检查项 | 状态 | 说明 |
|--------|------|------|
| FI-GL 总账 | ✅ 已修复 | 已补充 chart_of_accounts, group_account 等字段 |
| FI-AP 应付 | ✅ 已修复 | 已创建 AP 未清项/已清项视图 |
| FI-AR 应收 | ✅ 已修复 | 已创建 AR 未清项/已清项视图和信用额度表 |
| FI-AA 资产 | ✅ 已修复 | 已创建资产会计全套表 |
| CO-CCA | ✅ 基本一致 | 需补充成本行项目表 |
| CO-PCA | ✅ 基本一致 | 结构匹配 |
| CO-PA | ✅ 一致 | 结构匹配 |
| CO-PC | ✅ 基本一致 | 需补充在制品表 |

---

## 2. 详细对比分析

### 2.1 FI-GL 总账

#### 数据库设计存在的表

| 表名 | 说明 | 状态 |
|------|------|------|
| fi_account | 会计科目 | ✅ 存在 |
| fi_journal_entry_hdr | 凭证头 | ✅ 存在 |
| fi_journal_entry_itm | 凭证行 | ✅ 存在 |
| fi_account_balance | 科目余额 | ✅ 存在 |
| fi_doc_type | 凭证类型 | ✅ 存在 |
| fi_tax_code | 税码 | ✅ 存在 |

#### 功能设计额外定义的字段/表

| 项目 | 功能设计 | 数据库设计 | 差异说明 |
|------|----------|------------|----------|
| 科目表层级 | chart_of_accounts | 无独立字段 | 使用 account_group 替代 |
| 集团科目 | group_account | 无 | ⚠️ 需补充 |
| 公司代码层级字段 | reconciliation_account 等 | 无 | ⚠️ 需补充 |

#### 建议更新数据库设计

✅ **已实施**: fi_account 表已补充以下字段

```sql
-- fi_account 表已补充字段
chart_of_accounts VARCHAR(4),              -- 科目表
group_account   VARCHAR(10),               -- 集团科目
reconcil_account_type VARCHAR(2),          -- 统驭科目类型
field_status_group VARCHAR(4),             -- 字段状态组
open_item_mgmt  BOOLEAN DEFAULT FALSE,     -- 未清项管理
line_item_mgmt  BOOLEAN DEFAULT TRUE,      -- 行项目管理
```

---

### 2.2 FI-AP 应付账款

#### 数据库设计状态

| 表名 | 说明 | 状态 |
|------|------|------|
| v_fi_ap_open_items | 应付未清项视图 | ✅ 已创建 |
| v_fi_ap_cleared_items | 应付已清项视图 | ✅ 已创建 |

#### 实施方案

✅ **已实施**: 使用现有凭证表 + 视图 (方案A)
- 优点: 简化设计，避免数据冗余
- 实现: 创建视图 `v_fi_ap_open_items` 和 `v_fi_ap_cleared_items`


---

### 2.3 FI-AR 应收账款

#### 数据库设计状态

| 表名 | 说明 | 状态 |
|------|------|------|
| v_fi_ar_open_items | 应收未清项视图 | ✅ 已创建 |
| v_fi_ar_cleared_items | 应收已清项视图 | ✅ 已创建 |
| v_fi_ar_aging | 账龄分析视图 | ✅ 已创建 |
| fi_credit_limit | 信用额度 | ✅ 已创建 |
| fi_credit_check_log | 信用检查记录 | ✅ 已创建 |

#### 实施方案

✅ **已实施**: 使用视图 + 信用管理表
- 视图: v_fi_ar_open_items, v_fi_ar_cleared_items, v_fi_ar_aging
- 信用管理: fi_credit_limit, fi_credit_check_log

---

### 2.4 FI-AA 资产会计

#### 数据库设计状态

| 表名 | 说明 | 状态 |
|------|------|------|
| fi_asset_master | 资产主数据 | ✅ 已创建 |
| fi_asset_value | 资产价值 | ✅ 已创建 |
| fi_depreciation_run | 折旧运行 | ✅ 已创建 |
| fi_asset_transaction | 资产业务 | ✅ 已创建 |

#### 实施方案

✅ **已实施**: 完整的资产会计表结构
- fi_asset_master: 对标 SAP ANLA
- fi_asset_value: 对标 SAP ANLC
- fi_depreciation_run: 对标 SAP AFAB
- fi_asset_transaction: 对标 SAP ANEP

---

### 2.5 CO 成本中心会计

#### 数据库设计存在的表

| 表名 | 说明 | 状态 |
|------|------|------|
| co_cost_element | 成本要素 | ✅ 存在 |
| co_cost_element_sec | 次级成本要素 | ✅ 存在 |
| co_cost_center_cost | 成本中心成本 | ✅ 存在 |
| co_activity_type | 作业类型 | ✅ 存在 |
| co_activity_price | 作业价格 | ✅ 存在 |

#### 功能设计额外需要的表

| 表名 | 说明 | 状态 |
|------|------|------|
| co_cost_line_item | 成本行项目明细 | ⚠️ 建议补充 |

---

### 2.6 CO 利润中心会计 & CO-PA

#### 数据库设计存在的表

| 表名 | 说明 | 状态 |
|------|------|------|
| co_profit_center_balance | 利润中心余额 | ✅ 存在 |
| co_profitability_segment | 获利段 | ✅ 存在 |
| co_pa_data | 获利分析数据 | ✅ 存在 |

#### 一致性评估

✅ 功能设计与数据库设计一致，无冲突。

---

## 3. 字段类型对比

### 3.1 金额字段

| 功能设计 | 数据库设计 | 一致性 |
|----------|------------|--------|
| DECIMAL(18,2) | DECIMAL(23,2) | ✅ 兼容 (数据库精度更高) |

### 3.2 编码字段

| 功能设计 | 数据库设计 | 一致性 |
|----------|------------|--------|
| company_code VARCHAR(4) | company_id UUID | ⚠️ 需关联查询 |
| cost_center VARCHAR(10) | cost_center_id UUID | ⚠️ 需关联查询 |
| profit_center VARCHAR(10) | profit_center_id UUID | ⚠️ 需关联查询 |

**说明**: 数据库设计使用 UUID 作为外键更规范，功能设计中的编码字段可通过关联查询获取。

---

## 4. 数据库表补充状态

### 4.1 高优先级 (✅ 已完成)

| 模块 | 表名 | 说明 | 状态 |
|------|------|------|------|
| FI-AR | fi_credit_limit | 信用额度管理 | ✅ 已创建 |
| FI-AR | fi_credit_check_log | 信用检查记录 | ✅ 已创建 |
| FI-AA | fi_asset_master | 资产主数据 | ✅ 已创建 |
| FI-AA | fi_asset_value | 资产价值 | ✅ 已创建 |
| FI-AA | fi_depreciation_run | 折旧运行 | ✅ 已创建 |
| FI-AA | fi_asset_transaction | 资产业务 | ✅ 已创建 |

### 4.2 中优先级 (✅ 已完成)

| 模块 | 表名 | 说明 | 状态 |
|------|------|------|------|
| FI-GL | fi_fiscal_year_variant | 会计年度变式 | ✅ 已创建 |
| FI-GL | fi_period_control | 期间控制 | ✅ 已创建 |
| FI-AP | v_fi_ap_open_items | 应付未清项视图 | ✅ 已创建 |
| FI-AP | v_fi_ap_cleared_items | 应付已清项视图 | ✅ 已创建 |
| FI-AR | v_fi_ar_open_items | 应收未清项视图 | ✅ 已创建 |
| FI-AR | v_fi_ar_cleared_items | 应收已清项视图 | ✅ 已创建 |
| FI-AR | v_fi_ar_aging | 账龄分析视图 | ✅ 已创建 |

### 4.3 待后续补充 (低优先级)

| 模块 | 表名 | 说明 | 状态 |
|------|------|------|------|
| CO-CCA | co_cost_line_item | 成本行项目明细 | ⏳ 待补充 |
| CO-PC | co_wip | 在制品 | ⏳ 待补充 |
| FI-AP | fi_payment_proposal | 付款建议 | ⏳ 待补充 |
| FI-AR | fi_dunning | 催收记录 | ⏳ 待补充 |
| CO | co_statistical_key | 统计指标 | ⏳ 待补充 |

---

## 5. 一致性修复实施结果

### 5.1 ✅ 已完成修复

1. **补充 fi_account 表字段**
   - ✅ 添加 chart_of_accounts (科目表)
   - ✅ 添加 group_account (集团科目)
   - ✅ 添加 reconcil_account_type (统驭科目类型)
   - ✅ 添加 field_status_group (字段状态组)
   - ✅ 添加 open_item_mgmt (未清项管理)
   - ✅ 添加 line_item_mgmt (行项目管理)

2. **创建 fi_credit_limit 表**
   - ✅ 支持信用管理功能
   - ✅ 包含信用额度、已用额度、可用额度
   - ✅ 支持风险类别和信用状态

3. **创建资产会计表**
   - ✅ fi_asset_master (资产主数据)
   - ✅ fi_asset_value (资产价值)
   - ✅ fi_depreciation_run (折旧运行)
   - ✅ fi_asset_transaction (资产业务)

4. **创建 AP/AR 视图**
   - ✅ v_fi_ap_open_items (应付未清项)
   - ✅ v_fi_ap_cleared_items (应付已清项)
   - ✅ v_fi_ar_open_items (应收未清项)
   - ✅ v_fi_ar_cleared_items (应收已清项)
   - ✅ v_fi_ar_aging (账龄分析)

5. **补充期间控制表**
   - ✅ fi_fiscal_year_variant (会计年度变式)
   - ✅ fi_period_control (期间控制)

### 5.2 后续优化建议

1. **CO 模块补充**
   - 待补充 co_cost_line_item (成本行项目明细)
   - 待补充 co_wip (在制品)

2. **催收管理**
   - 待补充 fi_dunning (催收记录表)

3. **统计指标**
   - 待补充 co_statistical_key (统计指标表)

---

## 6. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.1 | 2026-03-14 | 已修复所有不一致项，同步数据库设计 |
| 1.0 | 2026-03-14 | 初始版本 - 完成一致性检查 |
