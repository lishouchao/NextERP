# NextERP 数据库优化总结

## 优化概述

基于对原始数据库设计的分析，已完成以下高优先级和中优先级优化。

## 已实施的优化

### 1. 统一审计触发器 (高优先级)

**文件**: `schema/optimizations/00-core-optimized.sql`

**优化内容**:
- 创建统一的 `audit_trigger_func()` 函数，替代分散的 `update_updated_at_column()`
- 自动处理 `created_at`, `updated_at`, `version` 字段
- 内置乐观锁检查，防止并发冲突

```sql
CREATE OR REPLACE FUNCTION audit_trigger_func()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    IF TG_OP = 'INSERT' THEN
        NEW.created_at = CURRENT_TIMESTAMP;
        NEW.version = 0;
    END IF;
    IF TG_OP = 'UPDATE' THEN
        IF OLD.version IS DISTINCT FROM NEW.version THEN
            RAISE EXCEPTION '乐观锁冲突: 表 %, ID %', TG_TABLE_NAME, OLD.id;
        END IF;
        NEW.version = OLD.version + 1;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

### 2. 表分区 (高优先级)

**文件**:
- `schema/optimizations/03-fi-co-optimized.sql`
- `schema/optimizations/04-mm-optimized.sql`
- `schema/optimizations/07-hr-optimized.sql`

**优化内容**:
- 会计凭证头 (`fi_journal_entry_hdr`) 按会计年度分区
- 采购订单头 (`mm_purchase_order_hdr`) 按年度分区
- 物料凭证头 (`mm_material_document_hdr`) 按年度分区
- 薪酬结果 (`hr_payroll_result`) 按年度分区
- 库存快照 (`mm_inventory_snapshot`) 按年度分区

```sql
CREATE TABLE fi_journal_entry_hdr (
    id UUID NOT NULL,
    fiscal_year INTEGER NOT NULL,
    ...
    PRIMARY KEY (fiscal_year, id)
) PARTITION BY RANGE (fiscal_year);

-- 创建分区
CREATE TABLE fi_journal_entry_hdr_2024
    PARTITION OF fi_journal_entry_hdr
    FOR VALUES FROM (2024) TO (2025);
```

### 3. 余额表结构优化 (高优先级)

**文件**: `schema/optimizations/03-fi-co-optimized.sql`

**优化内容**:
- 将余额数组 `period_balance[12]` 拆分为独立字段
- 避免数组索引的性能问题
- 支持高效的单期间查询

```sql
-- 原设计（数组）
period_balance DECIMAL(23,2)[] DEFAULT ARRAY[0,0,...]

-- 优化后（独立字段）
period_01_balance DECIMAL(23,2) DEFAULT 0,
period_01_debit DECIMAL(23,2) DEFAULT 0,
period_01_credit DECIMAL(23,2) DEFAULT 0,
period_02_balance DECIMAL(23,2) DEFAULT 0,
...
period_12_balance DECIMAL(23,2) DEFAULT 0,
period_12_debit DECIMAL(23,2) DEFAULT 0,
period_12_credit DECIMAL(23,2) DEFAULT 0,
```

### 4. 时间有效性约束 (高优先级)

**文件**:
- `schema/optimizations/01-tenant-optimized.sql`
- `schema/optimizations/02-business-partner-optimized.sql`
- `schema/optimizations/07-hr-optimized.sql`

**优化内容**:
- 使用 PostgreSQL 排他约束 (EXCLUDE) 防止时间重叠
- 确保同一员工/业务伙伴在同一时间段内只有一条有效记录

```sql
ALTER TABLE hr_it0008_basic_pay
ADD CONSTRAINT uk_hr_it0008_no_overlap
EXCLUDE USING GIST (
    employee_id WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);
```

### 5. 工资项拆分 (中优先级)

**文件**: `schema/optimizations/07-hr-optimized.sql`

**优化内容**:
- 创建独立的工资类型定义表 `hr_wage_type`
- 将 IT0008 工资项从 JSONB 拆分为独立明细表 `hr_it0008_wage_item`
- 预置常用工资类型（基本工资、津贴、奖金、扣款）

```sql
-- 工资类型定义
CREATE TABLE hr_wage_type (
    code VARCHAR(4) NOT NULL,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(2), -- BA/AL/BO/DE/OT
    is_taxable BOOLEAN,
    is_pension_base BOOLEAN,
    ...
);

-- 工资项明细
CREATE TABLE hr_it0008_wage_item (
    basic_pay_id UUID NOT NULL,
    wage_type_id UUID NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    ...
);
```

### 6. 全文搜索 (中优先级)

**文件**:
- `schema/optimizations/02-business-partner-optimized.sql`
- `schema/optimizations/04-mm-optimized.sql`

**优化内容**:
- 添加 `search_vector` 生成列，使用 `TSVECTOR`
- 创建 GIN 索引支持全文搜索
- 提供搜索函数 `search_bp_partner()`, `search_mm_material()`

```sql
search_vector TSVECTOR GENERATED ALWAYS AS (
    setweight(to_tsvector('simple', COALESCE(full_name, '')), 'A') ||
    setweight(to_tsvector('simple', COALESCE(organization_name, '')), 'A') ||
    setweight(to_tsvector('simple', COALESCE(search_term, '')), 'B')
) STORED,

CREATE INDEX idx_bp_partner_search_vector ON bp_partner USING GIN (search_vector);
```

### 7. 生成列优化 (中优先级)

**文件**: 所有优化文件

**优化内容**:
- 使用 `GENERATED ALWAYS AS` 创建计算列
- 自动计算可用库存、开放数量、总分等

```sql
-- 可用库存
available_stock DECIMAL(18,6) GENERATED ALWAYS AS (
    unrestricted_stock - in_transit_stock
) STORED,

-- 开放数量
open_quantity DECIMAL(18,6) GENERATED ALWAYS AS (
    quantity - quantity_delivered
) STORED,

-- 供应商综合评分
overall_score DECIMAL(3,2) GENERATED ALWAYS AS (
    (COALESCE(quality_score, 0) + COALESCE(delivery_score, 0) + COALESCE(price_score, 0)) / 3
) STORED
```

### 8. 行级安全 (RLS) (中优先级)

**文件**: `schema/optimizations/01-tenant-optimized.sql`

**优化内容**:
- 启用组织架构表的 RLS
- 基于 `app.current_tenant` 参数实现多租户隔离

```sql
ALTER TABLE sys_company ENABLE ROW LEVEL SECURITY;

CREATE POLICY rls_sys_company_tenant ON sys_company
    USING (tenant_id = current_setting('app.current_tenant', TRUE)::UUID);
```

### 9. 库存快照表 (中优先级)

**文件**: `schema/optimizations/04-mm-optimized.sql`

**优化内容**:
- 创建 `mm_inventory_snapshot` 分区表
- 支持月末库存快照，优化报表查询
- 提供快照生成函数 `generate_inventory_snapshot()`

### 10. 视图优化

**文件**: `schema/optimizations/99-views-optimized.sql`

**优化内容**:
- 适配分区表（带 `fiscal_year` 关联）
- 使用 `LATERAL JOIN` 优化当前有效记录查询
- 添加库存价值物化视图

## 优化文件清单

| 文件 | 说明 |
|------|------|
| `00-core-optimized.sql` | 核心函数、统一审计触发器 |
| `01-tenant-optimized.sql` | 租户组织架构、RLS 策略 |
| `02-business-partner-optimized.sql` | 业务伙伴、全文搜索 |
| `03-fi-co-optimized.sql` | 财务会计、表分区、余额表优化 |
| `04-mm-optimized.sql` | 物料管理、表分区、库存快照 |
| `07-hr-optimized.sql` | 人力资源、工资项拆分、时间约束 |
| `99-views-optimized.sql` | 视图、物化视图 |

## 性能提升预期

| 优化项 | 预期性能提升 |
|--------|-------------|
| 表分区 | 历史数据查询 10x+ |
| 统一审计触发器 | 减少 50% 触发器代码 |
| 独立余额字段 | 单期间查询 5x+ |
| 全文搜索 | 模糊搜索 10x+ |
| 时间约束 | 数据一致性 100% |
| 生成列 | 查询简化，无计算开销 |
| LATERAL JOIN | N+1 查询优化 |

## 使用说明

### 初始化顺序

```bash
# 1. 核心函数（必须首先执行）
psql -f schema/optimizations/00-core-optimized.sql

# 2. 基础表
psql -f schema/optimizations/01-tenant-optimized.sql
psql -f schema/optimizations/02-business-partner-optimized.sql

# 3. 业务模块
psql -f schema/optimizations/03-fi-co-optimized.sql
psql -f schema/optimizations/04-mm-optimized.sql
psql -f schema/optimizations/07-hr-optimized.sql

# 4. 视图
psql -f schema/optimizations/99-views-optimized.sql
```

### 应用程序配置

启用 RLS 需要在应用连接时设置租户参数：

```sql
-- 连接后执行
SET app.current_tenant = '租户UUID';
```

### 新增分区

每年需要为新年度创建分区：

```sql
-- 为 2026 年创建分区
CREATE TABLE fi_journal_entry_hdr_2026
    PARTITION OF fi_journal_entry_hdr
    FOR VALUES FROM (2026) TO (2027);

CREATE TABLE mm_purchase_order_hdr_2026
    PARTITION OF mm_purchase_order_hdr
    FOR VALUES FROM (2026) TO (2027);

-- ... 其他分区表
```

## 待优化项

以下优化可在后续版本实施：

1. **SD 模块**: 销售订单分区、交货单优化
2. **PP 模块**: 生产订单分区、工序记录优化
3. **迁移脚本**: 从原始 schema 迁移到优化 schema
4. **监控视图**: 性能监控和告警视图
5. **备份策略**: 分区表独立备份
