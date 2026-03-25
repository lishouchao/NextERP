# NextERP 数据库设计优化分析

## 一、当前设计评估

### 1.1 设计优点

| 方面 | 优点 | 说明 |
|------|------|------|
| **多租户** | ✅ 租户隔离 | 所有表包含 tenant_id |
| **UUID 主键** | ✅ 分布式友好 | 避免序列竞争 |
| **时间有效性** | ✅ SAP 风格 | valid_from/valid_to |
| **JSONB** | ✅ 灵活存储 | 工资项、薪酬项 |
| **数组类型** | ✅ 期间余额 | period_balance[] |
| **生成列** | ✅ 自动计算 | full_name, total_amount |
| **信息类型架构** | ✅ HR 专业 | 对标 SAP PAxxxx |

### 1.2 设计问题

## 二、发现的问题

### 2.1 架构层面

```
┌─────────────────────────────────────────────────────────────────────┐
│                        架构层面问题                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ❌ 问题1: 缺乏统一的审计字段基类                                     │
│  ────────────────────────────────                                   │
│  每个表都重复定义 created_at, updated_at, created_by, updated_by     │
│                                                                     │
│  ❌ 问题2: tenant_id 冗余                                            │
│  ────────────────────────────────                                   │
│  子表已经通过外键关联到租户，但仍存储 tenant_id                        │
│                                                                     │
│  ❌ 问题3: 缺乏软删除机制                                             │
│  ────────────────────────────────                                   │
│  部分表有 is_deleted，部分没有，不一致                               │
│                                                                     │
│  ❌ 问题4: 缺乏乐观锁                                                │
│  ────────────────────────────────                                   │
│  部分表有 version，大部分没有                                        │
│                                                                     │
│  ❌ 问题5: 外键引用不一致                                             │
│  ────────────────────────────────                                   │
│  有的 REFERENCES sys_tenant(id)，有的没有                            │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 性能层面

```
┌─────────────────────────────────────────────────────────────────────┐
│                        性能层面问题                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ❌ 问题1: JSONB 查询性能                                            │
│  ────────────────────────────────                                   │
│  hr_it0008_basic_pay.wage_items 使用 JSONB 计算 total_amount        │
│  每次查询都要解析 JSON，无法建立索引                                  │
│                                                                     │
│  ❌ 问题2: 数组索引问题                                              │
│  ────────────────────────────────                                   │
│  fi_account_balance.period_balance[] 无法直接索引单个期间            │
│  查询某期间余额需要：period_balance[3] 无法使用索引                   │
│                                                                     │
│  ❌ 问题3: 缺乏分区策略                                              │
│  ────────────────────────────────                                   │
│  大表（凭证、物料凭证、薪酬结果）未分区                               │
│  数据量大时查询性能下降                                              │
│                                                                     │
│  ❌ 问题4: 索引不足                                                  │
│  ────────────────────────────────                                   │
│  时间有效性查询 (valid_from <= ? AND valid_to >= ?) 缺乏复合索引     │
│                                                                     │
│  ❌ 问题5: N+1 查询风险                                              │
│  ────────────────────────────────                                   │
│  信息类型分散在多个表，查询员工完整信息需要关联多表                    │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.3 数据完整性层面

```
┌─────────────────────────────────────────────────────────────────────┐
│                      数据完整性问题                                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ❌ 问题1: 冗余数据一致性                                            │
│  ────────────────────────────────                                   │
│  fi_journal_entry_itm.account_number 冗余存储科目号                 │
│  hr_it0001.org_unit_name 冗余存储组织名称                            │
│  无机制保证冗余数据与源表同步                                         │
│                                                                     │
│  ❌ 问题2: 余额更新原子性                                            │
│  ────────────────────────────────                                   │
│  fi_account_balance 通过函数更新，但未在凭证过账事务中调用            │
│  可能导致凭证过账成功但余额未更新                                     │
│                                                                     │
│  ❌ 问题3: 时间有效性约束                                            │
│  ────────────────────────────────                                   │
│  信息类型的时间切片可能重叠                                           │
│  同一员工同一信息类型可能有多条有效记录                               │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.4 具体代码问题

```sql
-- 问题1: 余额更新函数复杂且低效
-- 当前设计
CREATE OR REPLACE FUNCTION update_account_balance(...) AS $$
    -- 使用 ARRAY_FILL 和数组操作，逻辑复杂
    -- 而且每次都要判断 INSERT 还是 UPDATE
$$

-- 问题2: JSONB 生成列每次都计算
total_amount DECIMAL(15,2) GENERATED ALWAYS AS (
    (SELECT COALESCE(SUM((item->>'amount')::DECIMAL), 0)
     FROM jsonb_array_elements(wage_items) AS item)  -- 每次查询都执行
) STORED

-- 问题3: 信息类型表结构不一致
-- hr_it0001: PRIMARY KEY (employee_id, valid_from)
-- hr_it0006: PRIMARY KEY (employee_id, subtype, valid_from)
-- hr_it2001: PRIMARY KEY (employee_id, valid_from, subtype)
-- 主键顺序不一致，影响查询优化
```

## 三、优化方案

### 3.1 架构优化

#### 3.1.1 引入基类表模式

```sql
-- ============================================================================
-- 优化1: 统一基类表
-- ============================================================================

-- 所有业务表继承此基类
CREATE TABLE base_entity (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    status          general_status DEFAULT 'ACTIVE',

    -- 软删除
    is_deleted      BOOLEAN DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    deleted_by      UUID,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,

    -- 乐观锁
    version         INTEGER DEFAULT 0
);

-- 时间有效性基类
CREATE TABLE base_time_dependent (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 审计（继承 base_entity 的字段，这里省略）
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0
);

-- 使用示例
CREATE TABLE fi_gl_account (
    -- 继承基类（PostgreSQL 表继承）
) INHERITS (base_time_dependent);

-- 或使用列复用（更推荐，避免继承的复杂性）
CREATE TABLE fi_gl_account (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 业务字段...

    -- 标准审计字段（通过触发器自动维护）
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,
    is_deleted      BOOLEAN DEFAULT FALSE
);
```

#### 3.1.2 统一审计触发器

```sql
-- ============================================================================
-- 优化2: 统一审计触发器
-- ============================================================================

-- 通用审计触发器函数
CREATE OR REPLACE FUNCTION audit_trigger_func()
RETURNS TRIGGER AS $$
BEGIN
    -- 更新时间
    NEW.updated_at = CURRENT_TIMESTAMP;

    -- 更新人（如果上下文中有）
    NEW.updated_by = COALESCE(
        current_setting('app.current_user_id', TRUE)::UUID,
        NEW.updated_by
    );

    -- 新记录设置创建信息
    IF TG_OP = 'INSERT' THEN
        NEW.created_at = CURRENT_TIMESTAMP;
        NEW.created_by = NEW.updated_by;
    END IF;

    -- 乐观锁
    IF TG_OP = 'UPDATE' THEN
        NEW.version = OLD.version + 1;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 批量为表添加触发器
-- 使用存储过程自动为所有表添加
CREATE OR REPLACE FUNCTION add_audit_triggers()
RETURNS VOID AS $$
DECLARE
    tbl RECORD;
BEGIN
    FOR tbl IN
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_type = 'BASE TABLE'
          AND table_name NOT LIKE 'core_%'
          AND table_name NOT LIKE 'sys_%'
    LOOP
        EXECUTE format('
            DROP TRIGGER IF EXISTS trg_%s_audit ON %I;
            CREATE TRIGGER trg_%s_audit
                BEFORE INSERT OR UPDATE ON %I
                FOR EACH ROW EXECUTE FUNCTION audit_trigger_func();
        ', tbl.table_name, tbl.table_name, tbl.table_name, tbl.table_name);
    END LOOP;
END;
$$ LANGUAGE plpgsql;
```

### 3.2 性能优化

#### 3.2.1 余额表结构优化

```sql
-- ============================================================================
-- 优化3: 余额表 - 使用独立字段替代数组
-- ============================================================================

-- 原设计（使用数组）
CREATE TABLE fi_account_balance_old (
    period_balance  DECIMAL(23,2)[] DEFAULT ARRAY[0,0,0,0,0,0,0,0,0,0,0,0],
    -- 问题：无法索引单个期间，查询需要解析数组
);

-- 优化设计（使用独立字段）
CREATE TABLE fi_account_balance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL,
    account_id      UUID NOT NULL,
    fiscal_year     INTEGER NOT NULL,
    currency_id     UUID NOT NULL,

    -- 期间余额（使用独立字段，便于索引和查询）
    period_01_balance DECIMAL(23,2) DEFAULT 0,
    period_01_debit   DECIMAL(23,2) DEFAULT 0,
    period_01_credit  DECIMAL(23,2) DEFAULT 0,

    period_02_balance DECIMAL(23,2) DEFAULT 0,
    period_02_debit   DECIMAL(23,2) DEFAULT 0,
    period_02_credit  DECIMAL(23,2) DEFAULT 0,

    -- ... 期间 3-12

    period_12_balance DECIMAL(23,2) DEFAULT 0,
    period_12_debit   DECIMAL(23,2) DEFAULT 0,
    period_12_credit  DECIMAL(23,2) DEFAULT 0,

    -- 年度累计
    year_balance    DECIMAL(23,2) DEFAULT 0,
    year_debit      DECIMAL(23,2) DEFAULT 0,
    year_credit     DECIMAL(23,2) DEFAULT 0,

    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (company_id, account_id, fiscal_year, currency_id)
);

-- 创建函数索引支持动态期间查询
CREATE INDEX idx_fi_account_balance_period ON fi_account_balance (
    company_id,
    fiscal_year,
    -- 使用表达式索引
    (period_01_balance + period_02_balance + period_03_balance)
);
```

#### 3.2.2 凭证表分区

```sql
-- ============================================================================
-- 优化4: 大表分区
-- ============================================================================

-- 凭证头按年度分区
CREATE TABLE fi_journal_entry_hdr (
    id              UUID NOT NULL,
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL,
    document_number VARCHAR(10) NOT NULL,
    fiscal_year     INTEGER NOT NULL,
    posting_date    DATE NOT NULL,
    -- ... 其他字段

    PRIMARY KEY (fiscal_year, id)
) PARTITION BY RANGE (fiscal_year);

-- 创建分区
CREATE TABLE fi_journal_entry_hdr_2024
    PARTITION OF fi_journal_entry_hdr
    FOR VALUES FROM (2024) TO (2025);

CREATE TABLE fi_journal_entry_hdr_2025
    PARTITION OF fi_journal_entry_hdr
    FOR VALUES FROM (2025) TO (2026);

CREATE TABLE fi_journal_entry_hdr_default
    PARTITION OF fi_journal_entry_hdr DEFAULT;

-- 凭证项按年度分区（继承头的分区键）
CREATE TABLE fi_journal_entry_itm (
    id              UUID NOT NULL,
    fiscal_year     INTEGER NOT NULL,  -- 冗余存储，用于分区
    header_id       UUID NOT NULL,
    line_item       INTEGER NOT NULL,
    -- ... 其他字段

    PRIMARY KEY (fiscal_year, id)
) PARTITION BY RANGE (fiscal_year);

-- 薪酬结果按年度分区
CREATE TABLE hr_payroll_result (
    id              UUID NOT NULL,
    payroll_year    INTEGER NOT NULL,  -- 分区键
    employee_id     UUID NOT NULL,
    payroll_period  VARCHAR(7) NOT NULL,
    -- ... 其他字段

    PRIMARY KEY (payroll_year, id)
) PARTITION BY RANGE (payroll_year);
```

#### 3.2.3 时间有效性索引

```sql
-- ============================================================================
-- 优化5: 时间有效性索引
-- ============================================================================

-- 为所有时间有效性表添加复合索引
CREATE INDEX idx_hr_org_unit_valid ON hr_org_unit (tenant_id, valid_from, valid_to);
CREATE INDEX idx_hr_job_valid ON hr_job (tenant_id, valid_from, valid_to);
CREATE INDEX idx_hr_position_valid ON hr_position (tenant_id, valid_from, valid_to);
CREATE INDEX idx_hr_it0001_valid ON hr_it0001_org_assignment (employee_id, valid_from, valid_to);

-- 时间有效性查询函数
CREATE OR REPLACE FUNCTION get_current_record(
    p_table_name TEXT,
    p_key_column TEXT,
    p_key_value UUID,
    p_check_date DATE DEFAULT CURRENT_DATE
) RETURNS UUID AS $$
DECLARE
    v_id UUID;
BEGIN
    EXECUTE format('
        SELECT id FROM %I
        WHERE %I = $1
          AND valid_from <= $2
          AND valid_to >= $2
        LIMIT 1
    ', p_table_name, p_key_column)
    INTO v_id
    USING p_key_value, p_check_date;

    RETURN v_id;
END;
$$ LANGUAGE plpgsql;
```

### 3.3 数据完整性优化

#### 3.3.1 冗余数据同步

```sql
-- ============================================================================
-- 优化6: 冗余字段自动同步触发器
-- ============================================================================

-- 方案A: 使用触发器同步（实时性高，性能影响大）
CREATE OR REPLACE FUNCTION sync_redundant_fields()
RETURNS TRIGGER AS $$
BEGIN
    -- 同步科目号到凭证项
    IF TG_TABLE_NAME = 'fi_journal_entry_itm' THEN
        SELECT account_number INTO NEW.account_number
        FROM fi_gl_account
        WHERE id = NEW.account_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 方案B: 使用物化视图（推荐）
CREATE MATERIALIZED VIEW mv_journal_entry_detail AS
SELECT
    i.id,
    i.header_id,
    i.line_item,
    i.account_id,
    a.account_number,  -- 实时关联
    a.name AS account_name,
    i.debit_credit,
    i.amount,
    i.amount_dc
FROM fi_journal_entry_itm i
JOIN fi_gl_account a ON a.id = i.account_id;

-- 方案C: 只在查询时关联（最简单）
-- 不存储冗余，通过视图关联
CREATE VIEW v_journal_entry_item AS
SELECT
    i.*,
    a.account_number,
    a.name AS account_name
FROM fi_journal_entry_itm i
JOIN fi_gl_account a ON a.id = i.account_id;
```

#### 3.3.2 余额更新事务绑定

```sql
-- ============================================================================
-- 优化7: 凭证过账与余额更新原子化
-- ============================================================================

-- 凭证过账存储过程
CREATE OR REPLACE FUNCTION post_journal_entry(
    p_entry_id UUID
) RETURNS BOOLEAN AS $$
DECLARE
    v_tenant_id UUID;
    v_company_id UUID;
    v_fiscal_year INTEGER;
    v_period INTEGER;
    v_currency_id UUID;
BEGIN
    -- 检查凭证状态
    SELECT tenant_id, company_id, fiscal_year,
           EXTRACT(MONTH FROM posting_date)::INTEGER, currency_id
    INTO v_tenant_id, v_company_id, v_fiscal_year, v_period, v_currency_id
    FROM fi_journal_entry_hdr
    WHERE id = p_entry_id AND status = 'DRAFT';

    IF NOT FOUND THEN
        RAISE EXCEPTION '凭证不存在或已过账';
    END IF;

    -- 更新余额（在同一事务中）
    INSERT INTO fi_account_balance (
        tenant_id, company_id, account_id, fiscal_year, currency_id,
        period_01_balance, period_01_debit, period_01_credit,
        -- ... 其他期间
        year_balance, year_debit, year_credit
    )
    SELECT
        v_tenant_id,
        v_company_id,
        i.account_id,
        v_fiscal_year,
        COALESCE(i.currency_id, v_currency_id),
        CASE WHEN v_period = 1 THEN
            CASE WHEN i.debit_credit = 'D' THEN i.amount ELSE -i.amount END
        ELSE 0 END,
        -- ... 完整逻辑
        CASE WHEN i.debit_credit = 'D' THEN i.amount ELSE -i.amount END,
        CASE WHEN i.debit_credit = 'D' THEN i.amount ELSE 0 END,
        CASE WHEN i.debit_credit = 'C' THEN i.amount ELSE 0 END
    FROM fi_journal_entry_itm i
    WHERE i.header_id = p_entry_id
    ON CONFLICT (company_id, account_id, fiscal_year, currency_id) DO UPDATE SET
        period_01_balance = CASE WHEN v_period = 1 THEN
            fi_account_balance.period_01_balance +
            CASE WHEN EXCLUDED.debit_credit = 'D' THEN EXCLUDED.amount ELSE -EXCLUDED.amount END
        ELSE fi_account_balance.period_01_balance END,
        -- ... 完整逻辑
        year_balance = fi_account_balance.year_balance +
            CASE WHEN EXCLUDED.debit_credit = 'D' THEN EXCLUDED.amount ELSE -EXCLUDED.amount END,
        updated_at = CURRENT_TIMESTAMP;

    -- 更新凭证状态
    UPDATE fi_journal_entry_hdr
    SET status = 'COMPLETED',
        is_posted = TRUE,
        posted_at = CURRENT_TIMESTAMP
    WHERE id = p_entry_id;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
```

#### 3.3.3 信息类型时间约束

```sql
-- ============================================================================
-- 优化8: 信息类型时间有效性约束
-- ============================================================================

-- 添加排他约束，防止时间重叠
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- 示例：IT0001 组织分配
ALTER TABLE hr_it0001_org_assignment
ADD CONSTRAINT uk_hr_it0001_no_overlap
EXCLUDE USING GIST (
    employee_id WITH =,
    daterange(valid_from, valid_to, '[]') WITH &&
);

-- 或使用触发器检查
CREATE OR REPLACE FUNCTION check_time_validity()
RETURNS TRIGGER AS $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM hr_it0001_org_assignment
    WHERE employee_id = NEW.employee_id
      AND id != NEW.id
      AND valid_from <= NEW.valid_to
      AND valid_to >= NEW.valid_from;

    IF v_count > 0 THEN
        RAISE EXCEPTION '时间有效性重叠：存在冲突的记录';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_hr_it0001_time_check
    BEFORE INSERT OR UPDATE ON hr_it0001_org_assignment
    FOR EACH ROW EXECUTE FUNCTION check_time_validity();
```

### 3.4 HR 模块优化

#### 3.4.1 信息类型统一主键

```sql
-- ============================================================================
-- 优化9: 信息类型主键标准化
-- ============================================================================

-- 标准化主键结构
-- 主键顺序：employee_id -> valid_from -> subtype

-- IT0001
ALTER TABLE hr_it0001_org_assignment
DROP CONSTRAINT hr_it0001_org_assignment_pkey,
ADD CONSTRAINT hr_it0001_org_assignment_pkey
    PRIMARY KEY (employee_id, valid_from);

-- IT0006
ALTER TABLE hr_it0006_address
DROP CONSTRAINT hr_it0006_address_pkey,
ADD CONSTRAINT hr_it0006_address_pkey
    PRIMARY KEY (employee_id, valid_from, subtype);

-- 统一添加信息类型标识
ALTER TABLE hr_it0001_org_assignment ADD COLUMN infotype VARCHAR(4) DEFAULT '0001';
ALTER TABLE hr_it0002_personal_data ADD COLUMN infotype VARCHAR(4) DEFAULT '0002';
```

#### 3.4.2 工资项优化

```sql
-- ============================================================================
-- 优化10: 工资项存储优化
-- ============================================================================

-- 方案A: 保留 JSONB 但添加 GIN 索引
CREATE INDEX idx_hr_it0008_wage_items ON hr_it0008_basic_pay USING GIN (wage_items);

-- 方案B: 拆分为独立表（推荐）
CREATE TABLE hr_wage_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    code            VARCHAR(4) NOT NULL,          -- 工资类型代码
    name            VARCHAR(100) NOT NULL,        -- 名称
    category        VARCHAR(2),                   -- 类别 (基本/津贴/扣款)
    is_recurring    BOOLEAN DEFAULT TRUE,         -- 是否经常性

    UNIQUE (tenant_id, code)
);

CREATE TABLE hr_it0008_wage_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    basic_pay_id    UUID NOT NULL REFERENCES hr_it0008_basic_pay(id) ON DELETE CASCADE,

    wage_type_id    UUID NOT NULL REFERENCES hr_wage_type(id),
    amount          DECIMAL(15,2) NOT NULL,
    currency_id     UUID REFERENCES core_currency(id),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_hr_it0008_wage_item_basic_pay ON hr_it0008_wage_item (basic_pay_id);
CREATE INDEX idx_hr_it0008_wage_item_type ON hr_it0008_wage_item (wage_type_id);

-- 视图汇总
CREATE VIEW v_hr_it0008_with_items AS
SELECT
    bp.id,
    bp.employee_id,
    bp.valid_from,
    bp.valid_to,
    bp.pay_type,
    bp.pay_area,
    bp.pay_grade,
    bp.currency_id,
    COALESCE(SUM(wi.amount), 0) AS total_amount,
    jsonb_agg(jsonb_build_object(
        'code', wt.code,
        'name', wt.name,
        'amount', wi.amount
    )) AS wage_items
FROM hr_it0008_basic_pay bp
LEFT JOIN hr_it0008_wage_item wi ON wi.basic_pay_id = bp.id
LEFT JOIN hr_wage_type wt ON wt.id = wi.wage_type_id
GROUP BY bp.id;
```

## 四、优化实施优先级

| 优先级 | 优化项 | 影响 | 实施难度 |
|--------|--------|------|---------|
| 🔴 高 | 凭证过账事务绑定 | 数据一致性 | 中 |
| 🔴 高 | 大表分区 | 性能 | 高 |
| 🔴 高 | 时间有效性索引 | 性能 | 低 |
| 🟡 中 | 统一审计触发器 | 维护性 | 低 |
| 🟡 中 | 余额表结构优化 | 性能 | 中 |
| 🟡 中 | 工资项拆分 | 查询性能 | 中 |
| 🟢 低 | 信息类型主键标准化 | 一致性 | 低 |
| 🟢 低 | 冗余字段同步 | 数据一致性 | 低 |

## 五、优化后收益预估

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 凭证查询 (100万条) | 5-10s | <1s | 5-10x |
| 余额汇总 | 2-3s | <0.5s | 4-6x |
| 员工信息查询 | 0.5s | <0.1s | 5x |
| 月末结账 | 30-60min | 5-10min | 3-6x |
| 数据一致性风险 | 高 | 低 | - |

## 六、下一步建议

1. **立即执行**: 添加时间有效性索引
2. **短期规划**: 实施凭证过账存储过程
3. **中期规划**: 凭证表分区、工资项拆分
4. **长期规划**: 统一审计机制、基类表重构
