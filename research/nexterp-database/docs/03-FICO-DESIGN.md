# FI/CO 模块数据库设计

**模块**: Financial Accounting / Controlling (财务会计/管理会计)
**对标**: SAP ECC FI/CO + S/4HANA ACDOCA
**版本**: 1.1

---

## 1. 模块概述

### 1.1 业务范围

| 子模块 | 说明 | SAP 对标 |
|--------|------|----------|
| GL | 总账 | BKPF/BSEG |
| AP | 应付账款 | BSIK/BSAK |
| AR | 应收账款 | BSID/BSAD |
| AA | 资产会计 | ANEP/ANLC |
| CO | 管理会计 | COEP/COSP |

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                    FI/CO Module Architecture                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌─────────────┐         ┌─────────────┐                       │
│   │  凭证头     │  1:N    │  凭证项     │                       │
│   │  fi_journal │────────►│  fi_journal │                       │
│   │  _entry_hdr │         │  _entry_itm │                       │
│   └─────────────┘         └─────────────┘                       │
│         │                        │                               │
│         │                        │                               │
│         ▼                        ▼                               │
│   ┌──────────────────────────────────────────────────────┐      │
│   │                    科目余额                            │      │
│   │               fi_account_balance                      │      │
│   │  ┌─────┬─────┬─────┬─────┬─────┬─────┬─────┬─────┐  │      │
│   │  │ P01 │ P02 │ P03 │ P04 │ P05 │ P06 │...  │ P12 │  │      │
│   │  │借/贷│借/贷│借/贷│借/贷│借/贷│借/贷│     │借/贷│  │      │
│   │  └─────┴─────┴─────┴─────┴─────┴─────┴─────┴─────┘  │      │
│   └──────────────────────────────────────────────────────┘      │
│                                                                  │
│   ┌─────────────┐   ┌─────────────┐   ┌─────────────┐           │
│   │  会计科目   │   │  凭证类型   │   │  税码配置   │           │
│   │  fi_account │   │  fi_doc_type│   │  fi_tax_code│           │
│   └─────────────┘   └─────────────┘   └─────────────┘           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 与 S/4HANA ACDOCA 对比

| 特性 | SAP ECC | S/4HANA ACDOCA | NextERP |
|------|---------|----------------|---------|
| 凭证存储 | BKPF + BSEG | ACDOCA | fi_journal_entry_hdr + _itm |
| 余额存储 | 多表 (GLT0, etc.) | ACDOCA | fi_account_balance |
| 科目维度 | 有限 | 无限制 | 扩展维度 |
| 实时汇总 | 批量 | 实时 | 实时 |

---

## 2. 会计科目表

### 2.1 科目主数据 (fi_account)

```sql
CREATE TABLE fi_account (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 科目编码
    account_code    VARCHAR(10) NOT NULL,      -- 科目代码
    account_name    VARCHAR(100) NOT NULL,     -- 科目名称
    account_name_en VARCHAR(100),              -- 英文名称

    -- 科目属性
    account_type    VARCHAR(2) NOT NULL,       -- 科目类型
    -- AS:资产 LI:负债 EQ:权益 RE:收入 EX:费用

    -- 科目分类
    account_class   VARCHAR(2),                -- 科目分类
    -- CA:流动资产 NCA:非流动资产 CL:流动负债 NCL:非流动负债
    -- EQ:权益 OR:收入 EX:费用 CO:成本

    -- 余额方向
    balance_indicator VARCHAR(1) NOT NULL,     -- D:借方 C:贷方

    -- 科目表 (对标 SAP SKA1-KTOPL)
    chart_of_accounts VARCHAR(4),              -- 科目表

    -- 集团科目 (对标 SAP SKA1-KTOKS)
    group_account   VARCHAR(10),               -- 集团科目

    -- 科目组 (对标 SAP SKA1-KTOKS)
    account_group   VARCHAR(4),                -- 科目组

    -- 控制标识
    is_postable     BOOLEAN DEFAULT TRUE,      -- 是否可记账
    is_reconciliation BOOLEAN DEFAULT FALSE,   -- 是否统驭科目 (对标 SAP SKB1-MITKZ)
    reconcil_account_type VARCHAR(2),          -- 统驭科目类型 (对标 SAP SKB1-MITKZ)
    -- D:客户 K:供应商 A:资产 M:物料
    is_balance_sheet BOOLEAN DEFAULT FALSE,    -- 是否资产负债表科目
    is_p_and_l      BOOLEAN DEFAULT FALSE,     -- 是否损益科目

    -- 字段状态组 (对标 SAP SKB1-FSTAG)
    field_status_group VARCHAR(4),             -- 字段状态组

    -- 未清项/行项目管理 (对标 SAP SKB1-XOPVZ/XGKON)
    open_item_mgmt  BOOLEAN DEFAULT FALSE,     -- 未清项管理
    line_item_mgmt  BOOLEAN DEFAULT TRUE,      -- 行项目管理

    -- 现金流量
    cash_flow_type  VARCHAR(2),                -- 现金流量分类

    -- 税务
    tax_category    VARCHAR(2),                -- 税分类

    -- 父科目 (科目层级)
    parent_id       UUID REFERENCES fi_account(id),

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',
    status          general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, account_code)
);
```

**科目类型枚举**:

| 代码 | 类型 | 余额方向 | 说明 |
|------|------|----------|------|
| AS | 资产 | D | 借增贷减 |
| LI | 负债 | C | 贷增借减 |
| EQ | 权益 | C | 贷增借减 |
| RE | 收入 | C | 贷增借减 |
| EX | 费用 | D | 借增贷减 |

### 2.2 科目层级视图

```sql
CREATE VIEW v_fi_account_hierarchy AS
WITH RECURSIVE account_tree AS (
    -- 根科目
    SELECT id, account_code, account_name, account_type,
           parent_id, 1 AS level, ARRAY[account_code] AS path
    FROM fi_account
    WHERE parent_id IS NULL AND status = 'ACTIVE'

    UNION ALL

    -- 递归子科目
    SELECT a.id, a.account_code, a.account_name, a.account_type,
           a.parent_id, t.level + 1, t.path || a.account_code
    FROM fi_account a
    JOIN account_tree t ON a.parent_id = t.id
    WHERE a.status = 'ACTIVE'
)
SELECT * FROM account_tree
ORDER BY path;
```

---

## 3. 凭证管理

### 3.1 凭证头 (fi_journal_entry_hdr)

按会计年度分区，对标 SAP BKPF

```sql
CREATE TABLE fi_journal_entry_hdr (
    -- 主键 (分区键在前)
    id              UUID NOT NULL,
    fiscal_year     INTEGER NOT NULL,

    -- 租户
    tenant_id       UUID NOT NULL,

    -- 公司
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 凭证信息
    document_number VARCHAR(10) NOT NULL,      -- 凭证号
    document_type_id UUID REFERENCES fi_doc_type(id),
    document_date   DATE NOT NULL,             -- 凭证日期
    posting_date    DATE NOT NULL,             -- 过账日期
    period          INTEGER NOT NULL,          -- 期间 (1-16)

    -- 货币
    currency_id     UUID NOT NULL REFERENCES core_currency(id),
    exchange_rate   DECIMAL(12,6) DEFAULT 1,   -- 汇率

    -- 参考
    reference       VARCHAR(20),               -- 参考号
    header_text     VARCHAR(100),              -- 凭证抬头文本

    -- 来源
    source_type     VARCHAR(10),               -- 来源类型
    -- MM:物料管理 SD:销售分销 HR:人力 FA:资产 MANUAL:手工
    source_document VARCHAR(50),               -- 来源单据号

    -- 状态
    status          document_status DEFAULT 'DRAFT',
    is_posted       BOOLEAN DEFAULT FALSE,
    is_reversed     BOOLEAN DEFAULT FALSE,
    reversed_doc_id UUID,                      -- 冲销凭证ID

    -- 审批
    approval_status approval_status DEFAULT 'DRAFT',
    approved_by     UUID,
    approved_at     TIMESTAMP,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    posted_by       UUID,
    posted_at       TIMESTAMP,
    version         INTEGER DEFAULT 0,
    is_deleted      BOOLEAN DEFAULT FALSE,

    PRIMARY KEY (fiscal_year, id)
) PARTITION BY RANGE (fiscal_year);

-- 创建分区
CREATE TABLE fi_journal_entry_hdr_2024
    PARTITION OF fi_journal_entry_hdr
    FOR VALUES FROM (2024) TO (2025);

CREATE TABLE fi_journal_entry_hdr_2025
    PARTITION OF fi_journal_entry_hdr
    FOR VALUES FROM (2025) TO (2026);

CREATE TABLE fi_journal_entry_hdr_2026
    PARTITION OF fi_journal_entry_hdr
    FOR VALUES FROM (2026) TO (2027);

CREATE TABLE fi_journal_entry_hdr_default
    PARTITION OF fi_journal_entry_hdr DEFAULT;
```

**索引策略**:

```sql
-- 公司+凭证号查询
CREATE INDEX idx_fje_hdr_doc ON fi_journal_entry_hdr (company_id, document_number);

-- 过账日期查询
CREATE INDEX idx_fje_hdr_date ON fi_journal_entry_hdr (posting_date);

-- 状态查询
CREATE INDEX idx_fje_hdr_status ON fi_journal_entry_hdr (status, is_posted);

-- 来源追溯
CREATE INDEX idx_fje_hdr_source ON fi_journal_entry_hdr (source_type, source_document);
```

### 3.2 凭证项 (fi_journal_entry_itm)

对标 SAP BSEG

```sql
CREATE TABLE fi_journal_entry_itm (
    -- 主键 (分区键在前)
    id              UUID NOT NULL,
    fiscal_year     INTEGER NOT NULL,          -- 冗余，用于分区

    -- 租户
    tenant_id       UUID NOT NULL,

    -- 关联
    header_id       UUID NOT NULL,             -- 凭证头ID
    line_item       INTEGER NOT NULL,          -- 行号

    -- 科目
    account_id      UUID NOT NULL REFERENCES fi_account(id),
    account_code    VARCHAR(10),               -- 冗余存储

    -- 业务伙伴
    partner_id      UUID REFERENCES bp_business_partner(id),
    partner_type    VARCHAR(2),                -- C:客户 V:供应商 E:员工

    -- 借贷标识
    debit_credit    VARCHAR(1) NOT NULL CHECK (debit_credit IN ('D', 'C')),

    -- 金额
    amount          DECIMAL(23,2) NOT NULL,    -- 本位币金额
    amount_dc       DECIMAL(23,2),             -- 凭证币金额
    currency_id     UUID REFERENCES core_currency(id),

    -- 税
    tax_code        VARCHAR(2),                -- 税码
    tax_amount      DECIMAL(23,2),             -- 税额
    tax_base_amount DECIMAL(23,2),             -- 计税基数

    -- 成本对象
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    profit_center_id UUID REFERENCES sys_profit_center(id),
    internal_order  VARCHAR(12),               -- 内部订单
    project_id      UUID,                      -- 项目WBS

    -- 业务范围
    business_area   VARCHAR(4),                -- 业务范围
    segment         VARCHAR(10),               -- 报表段

    -- 文本
    item_text       VARCHAR(100),              -- 行项目文本

    -- 分配
    assignment      VARCHAR(18),               -- 分配号
    reference_key_1 VARCHAR(20),               -- 参考键1
    reference_key_2 VARCHAR(20),               -- 参考键2
    reference_key_3 VARCHAR(20),               -- 参考键3

    -- 付款/清算
    payment_term    VARCHAR(4),                -- 付款条款
    baseline_date   DATE,                      -- 基准日期
    due_date        DATE,                      -- 到期日
    clearing_date   DATE,                      -- 清算日期
    clearing_doc_id UUID,                      -- 清算凭证ID

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (fiscal_year, id)
) PARTITION BY RANGE (fiscal_year);

-- 创建分区
CREATE TABLE fi_journal_entry_itm_2024
    PARTITION OF fi_journal_entry_itm
    FOR VALUES FROM (2024) TO (2025);

CREATE TABLE fi_journal_entry_itm_2025
    PARTITION OF fi_journal_entry_itm
    FOR VALUES FROM (2025) TO (2026);

CREATE TABLE fi_journal_entry_itm_2026
    PARTITION OF fi_journal_entry_itm
    FOR VALUES FROM (2026) TO (2027);

CREATE TABLE fi_journal_entry_itm_default
    PARTITION OF fi_journal_entry_itm DEFAULT;
```

**索引策略**:

```sql
-- 凭证头关联
CREATE INDEX idx_fje_itm_header ON fi_journal_entry_itm (header_id);

-- 科目余额查询
CREATE INDEX idx_fje_itm_account ON fi_journal_entry_itm (account_id, posting_date);

-- 业务伙伴查询
CREATE INDEX idx_fje_itm_partner ON fi_journal_entry_itm (partner_id, partner_type);

-- 成本中心查询
CREATE INDEX idx_fje_itm_cost_center ON fi_journal_entry_itm (cost_center_id);

-- 未清项查询
CREATE INDEX idx_fje_itm_open ON fi_journal_entry_itm (partner_id, clearing_date)
    WHERE clearing_date IS NULL;
```

---

## 4. 科目余额

### 4.1 余额表 (fi_account_balance)

**设计决策**: 使用独立字段存储各期间借贷发生额，而非 JSONB 数组

**原因**:
1. 查询性能: 单字段查询比 JSONB 路径查询快 3-5 倍
2. 聚合效率: SUM/AVG 等聚合函数直接可用
3. 索引友好: 可对单个期间建立索引
4. 数据完整性: 可对单个期间添加约束

```sql
CREATE TABLE fi_account_balance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL REFERENCES sys_company(id),
    account_id      UUID NOT NULL REFERENCES fi_account(id),
    fiscal_year     INTEGER NOT NULL,
    currency_id     UUID NOT NULL REFERENCES core_currency(id),

    -- 期间借贷发生额 (24个独立字段)
    period_01_debit  DECIMAL(23,2) DEFAULT 0,
    period_01_credit DECIMAL(23,2) DEFAULT 0,

    period_02_debit  DECIMAL(23,2) DEFAULT 0,
    period_02_credit DECIMAL(23,2) DEFAULT 0,

    period_03_debit  DECIMAL(23,2) DEFAULT 0,
    period_03_credit DECIMAL(23,2) DEFAULT 0,

    period_04_debit  DECIMAL(23,2) DEFAULT 0,
    period_04_credit DECIMAL(23,2) DEFAULT 0,

    period_05_debit  DECIMAL(23,2) DEFAULT 0,
    period_05_credit DECIMAL(23,2) DEFAULT 0,

    period_06_debit  DECIMAL(23,2) DEFAULT 0,
    period_06_credit DECIMAL(23,2) DEFAULT 0,

    period_07_debit  DECIMAL(23,2) DEFAULT 0,
    period_07_credit DECIMAL(23,2) DEFAULT 0,

    period_08_debit  DECIMAL(23,2) DEFAULT 0,
    period_08_credit DECIMAL(23,2) DEFAULT 0,

    period_09_debit  DECIMAL(23,2) DEFAULT 0,
    period_09_credit DECIMAL(23,2) DEFAULT 0,

    period_10_debit  DECIMAL(23,2) DEFAULT 0,
    period_10_credit DECIMAL(23,2) DEFAULT 0,

    period_11_debit  DECIMAL(23,2) DEFAULT 0,
    period_11_credit DECIMAL(23,2) DEFAULT 0,

    period_12_debit  DECIMAL(23,2) DEFAULT 0,
    period_12_credit DECIMAL(23,2) DEFAULT 0,

    -- 年度累计
    year_debit      DECIMAL(23,2) DEFAULT 0,
    year_credit     DECIMAL(23,2) DEFAULT 0,

    -- 审计
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version         INTEGER DEFAULT 0,

    -- 唯一约束
    UNIQUE (company_id, account_id, fiscal_year, currency_id)
);
```

### 4.2 余额视图

```sql
CREATE VIEW v_fi_account_balance AS
SELECT
    id, tenant_id, company_id, account_id, fiscal_year, currency_id,

    -- 期间净额 (借-贷)
    period_01_debit - period_01_credit AS period_01_balance,
    period_02_debit - period_02_credit AS period_02_balance,
    period_03_debit - period_03_credit AS period_03_balance,
    period_04_debit - period_04_credit AS period_04_balance,
    period_05_debit - period_05_credit AS period_05_balance,
    period_06_debit - period_06_credit AS period_06_balance,
    period_07_debit - period_07_credit AS period_07_balance,
    period_08_debit - period_08_credit AS period_08_balance,
    period_09_debit - period_09_credit AS period_09_balance,
    period_10_debit - period_10_credit AS period_10_balance,
    period_11_debit - period_11_credit AS period_11_balance,
    period_12_debit - period_12_credit AS period_12_balance,

    -- 原始值
    period_01_debit, period_01_credit,
    period_02_debit, period_02_credit,
    period_03_debit, period_03_credit,
    period_04_debit, period_04_credit,
    period_05_debit, period_05_credit,
    period_06_debit, period_06_credit,
    period_07_debit, period_07_credit,
    period_08_debit, period_08_credit,
    period_09_debit, period_09_credit,
    period_10_debit, period_10_credit,
    period_11_debit, period_11_credit,
    period_12_debit, period_12_credit,

    -- 年度净额
    year_debit - year_credit AS year_balance,
    year_debit, year_credit

FROM fi_account_balance;
```

### 4.3 期间余额查询函数

```sql
CREATE OR REPLACE FUNCTION get_period_balance(
    p_company_id UUID,
    p_account_id UUID,
    p_fiscal_year INTEGER,
    p_period INTEGER
) RETURNS DECIMAL AS $$
DECLARE
    v_debit DECIMAL(23,2);
    v_credit DECIMAL(23,2);
BEGIN
    EXECUTE format('
        SELECT period_%s_debit, period_%s_credit
        FROM fi_account_balance
        WHERE company_id = $1
          AND account_id = $2
          AND fiscal_year = $3
    ', p_period, p_period)
    INTO v_debit, v_credit
    USING p_company_id, p_account_id, p_fiscal_year;

    RETURN COALESCE(v_debit, 0) - COALESCE(v_credit, 0);
END;
$$ LANGUAGE plpgsql;
```

---

## 5. 凭证过账

### 5.1 过账存储过程

```sql
CREATE OR REPLACE FUNCTION post_journal_entry(
    p_entry_id UUID,
    p_fiscal_year INTEGER
) RETURNS BOOLEAN AS $$
DECLARE
    v_tenant_id UUID;
    v_company_id UUID;
    v_period INTEGER;
    v_currency_id UUID;
    v_item RECORD;
    v_debit_total DECIMAL(23,2) := 0;
    v_credit_total DECIMAL(23,2) := 0;
BEGIN
    -- 1. 锁定凭证头
    SELECT tenant_id, company_id, currency_id, status, is_posted,
           EXTRACT(MONTH FROM posting_date)::INTEGER
    INTO v_tenant_id, v_company_id, v_currency_id, v_period
    FROM fi_journal_entry_hdr
    WHERE id = p_entry_id AND fiscal_year = p_fiscal_year
    FOR UPDATE;

    -- 2. 状态检查
    IF NOT FOUND THEN
        RAISE EXCEPTION '凭证不存在: %', p_entry_id;
    END IF;

    -- 3. 借贷平衡检查
    SELECT
        COALESCE(SUM(CASE WHEN debit_credit = 'D' THEN amount_dc ELSE 0 END), 0),
        COALESCE(SUM(CASE WHEN debit_credit = 'C' THEN amount_dc ELSE 0 END), 0)
    INTO v_debit_total, v_credit_total
    FROM fi_journal_entry_itm
    WHERE header_id = p_entry_id AND fiscal_year = p_fiscal_year;

    IF ABS(v_debit_total - v_credit_total) > 0.01 THEN
        RAISE EXCEPTION '借贷不平衡: 借方%, 贷方%', v_debit_total, v_credit_total;
    END IF;

    -- 4. 更新余额
    FOR v_item IN
        SELECT * FROM fi_journal_entry_itm
        WHERE header_id = p_entry_id AND fiscal_year = p_fiscal_year
    LOOP
        -- 动态更新对应期间余额
        EXECUTE format('
            INSERT INTO fi_account_balance (
                tenant_id, company_id, account_id, fiscal_year, currency_id,
                period_%s_debit, period_%s_credit,
                year_debit, year_credit
            ) VALUES ($1, $2, $3, $4, $5, $6, $7, $6, $7)
            ON CONFLICT (company_id, account_id, fiscal_year, currency_id) DO UPDATE SET
                period_%s_debit = fi_account_balance.period_%s_debit + $6,
                period_%s_credit = fi_account_balance.period_%s_credit + $7,
                year_debit = fi_account_balance.year_debit + $6,
                year_credit = fi_account_balance.year_credit + $7,
                updated_at = CURRENT_TIMESTAMP,
                version = fi_account_balance.version + 1
        ', v_period, v_period, v_period, v_period, v_period, v_period)
        USING v_tenant_id, v_company_id, v_item.account_id,
              p_fiscal_year, COALESCE(v_item.currency_id, v_currency_id),
              CASE WHEN v_item.debit_credit = 'D' THEN v_item.amount_dc ELSE 0 END,
              CASE WHEN v_item.debit_credit = 'C' THEN v_item.amount_dc ELSE 0 END;
    END LOOP;

    -- 5. 更新凭证状态
    UPDATE fi_journal_entry_hdr
    SET status = 'POSTED',
        is_posted = TRUE,
        posted_at = CURRENT_TIMESTAMP,
        version = version + 1
    WHERE id = p_entry_id AND fiscal_year = p_fiscal_year;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
```

### 5.2 冲销凭证

```sql
CREATE OR REPLACE FUNCTION reverse_journal_entry(
    p_entry_id UUID,
    p_fiscal_year INTEGER,
    p_reversal_date DATE,
    p_reversal_reason VARCHAR(100)
) RETURNS UUID AS $$
DECLARE
    v_new_id UUID;
    v_new_doc_num VARCHAR(10);
    v_tenant_id UUID;
    v_company_id UUID;
    v_currency_id UUID;
    v_item RECORD;
BEGIN
    -- 获取原凭证信息
    SELECT tenant_id, company_id, currency_id
    INTO v_tenant_id, v_company_id, v_currency_id
    FROM fi_journal_entry_hdr
    WHERE id = p_entry_id AND fiscal_year = p_fiscal_year;

    IF NOT FOUND THEN
        RAISE EXCEPTION '原凭证不存在';
    END IF;

    -- 生成冲销凭证号
    v_new_doc_num := next_val('fi_journal_seq_' || v_company_id::TEXT);

    -- 创建冲销凭证头
    INSERT INTO fi_journal_entry_hdr (
        id, tenant_id, company_id, document_number, fiscal_year,
        document_date, posting_date, period, currency_id,
        source_type, source_document,
        status, is_posted, reversed_doc_id,
        created_by
    ) VALUES (
        gen_random_uuid(), v_tenant_id, v_company_id, v_new_doc_num,
        EXTRACT(YEAR FROM p_reversal_date),
        p_reversal_date, p_reversal_date,
        EXTRACT(MONTH FROM p_reversal_date),
        v_currency_id,
        'REVERSAL', p_entry_id::TEXT,
        'POSTED', TRUE, p_entry_id,
        current_setting('app.current_user_id', TRUE)::UUID
    ) RETURNING id INTO v_new_id;

    -- 创建冲销凭证项 (借贷反向)
    FOR v_item IN
        SELECT * FROM fi_journal_entry_itm
        WHERE header_id = p_entry_id AND fiscal_year = p_fiscal_year
    LOOP
        INSERT INTO fi_journal_entry_itm (
            id, fiscal_year, tenant_id, header_id, line_item,
            account_id, account_code, partner_id, partner_type,
            debit_credit, amount, amount_dc, currency_id,
            tax_code, tax_amount,
            cost_center_id, profit_center_id, item_text
        ) VALUES (
            gen_random_uuid(),
            EXTRACT(YEAR FROM p_reversal_date),
            v_tenant_id,
            v_new_id,
            v_item.line_item,
            v_item.account_id,
            v_item.account_code,
            v_item.partner_id,
            v_item.partner_type,
            CASE WHEN v_item.debit_credit = 'D' THEN 'C' ELSE 'D' END,
            v_item.amount,
            v_item.amount_dc,
            v_item.currency_id,
            v_item.tax_code,
            v_item.tax_amount,
            v_item.cost_center_id,
            v_item.profit_center_id,
            '冲销: ' || COALESCE(v_item.item_text, '')
        );
    END LOOP;

    -- 过账冲销凭证
    PERFORM post_journal_entry(v_new_id, EXTRACT(YEAR FROM p_reversal_date));

    -- 标记原凭证已冲销
    UPDATE fi_journal_entry_hdr
    SET is_reversed = TRUE
    WHERE id = p_entry_id AND fiscal_year = p_fiscal_year;

    RETURN v_new_id;
END;
$$ LANGUAGE plpgsql;
```

---

## 6. 辅助表

### 6.1 凭证类型 (fi_doc_type)

```sql
CREATE TABLE fi_doc_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    doc_type        VARCHAR(2) NOT NULL,       -- 凭证类型代码
    name            VARCHAR(50) NOT NULL,      -- 名称

    -- 属性
    number_range    VARCHAR(4),                -- 号码范围
    is_postable     BOOLEAN DEFAULT TRUE,

    -- 凭证控制
    require_reference BOOLEAN DEFAULT FALSE,   -- 必须输入参考
    require_text    BOOLEAN DEFAULT FALSE,     -- 必须输入文本

    status          general_status DEFAULT 'ACTIVE',

    UNIQUE (tenant_id, doc_type)
);
```

**常用凭证类型**:

| 代码 | 名称 | 用途 |
|------|------|------|
| SA | 总账凭证 | 通用总账记账 |
| KA | 供应商凭证 | 供应商发票 |
| DA | 客户凭证 | 客户发票 |
| RE | 收款凭证 | 收款 |
| ZP | 付款凭证 | 付款 |
| RV | 冲销凭证 | 冲销 |

### 6.2 税码配置 (fi_tax_code)

```sql
CREATE TABLE fi_tax_code (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    tax_code        VARCHAR(2) NOT NULL,       -- 税码
    tax_type        VARCHAR(2) NOT NULL,       -- 税类型
    description     VARCHAR(100) NOT NULL,

    -- 税率
    tax_rate        DECIMAL(5,4) NOT NULL,     -- 税率 (如 0.13 = 13%)

    -- 科目
    tax_account_id  UUID REFERENCES fi_account(id), -- 税科目

    -- 有效期
    valid_from      DATE NOT NULL,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    status          general_status DEFAULT 'ACTIVE',

    UNIQUE (tenant_id, tax_code, valid_from)
);
```

**常用税码 (中国)**:

| 税码 | 名称 | 税率 |
|------|------|------|
| 13 | 增值税 13% | 0.13 |
| 09 | 增值税 9% | 0.09 |
| 06 | 增值税 6% | 0.06 |
| 03 | 增值税 3% | 0.03 |
| 00 | 零税率 | 0 |
| EX | 免税 | 0 |

---

## 7. 信用管理 (FI-AR)

### 7.1 信用额度表 (fi_credit_limit)

对标 SAP FSCM-CM (Financial Supply Chain Management - Credit Management)

```sql
CREATE TABLE fi_credit_limit (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 客户
    customer_id     UUID NOT NULL REFERENCES bp_business_partner(id),

    -- 信用控制范围 (对标 SAP UKMBP-CREDIT_SGMNT)
    credit_control_area VARCHAR(4) NOT NULL DEFAULT '0001',

    -- 信用额度
    credit_limit    DECIMAL(18,2) NOT NULL,    -- 信用额度
    credit_used     DECIMAL(18,2) DEFAULT 0,   -- 已用额度
    credit_available DECIMAL(18,2) GENERATED ALWAYS AS (
        credit_limit - credit_used
    ) STORED,                                   -- 可用额度

    -- 风险类别 (对标 SAP UKMBP-RISK_CLASS)
    risk_category   VARCHAR(4),                -- 风险类别
    -- LOW:低风险 MED:中风险 HIGH:高风险 CRIT:临界

    -- 信用组 (对标 SAP UKMBP-CREDIT_GROUP)
    credit_group    VARCHAR(4),                -- 信用组

    -- 信用状态
    credit_status   VARCHAR(1) DEFAULT 'A',    -- A:活跃 B:冻结 C:受限

    -- 审核日期
    last_review_date DATE,
    next_review_date DATE,

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 审计
    status          general_status DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,

    UNIQUE (tenant_id, customer_id, credit_control_area)
);

-- 索引
CREATE INDEX idx_fi_credit_customer ON fi_credit_limit (customer_id);
CREATE INDEX idx_fi_credit_status ON fi_credit_limit (credit_status);
```

### 7.2 信用检查记录表 (fi_credit_check_log)

```sql
CREATE TABLE fi_credit_check_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 客户
    customer_id     UUID NOT NULL REFERENCES bp_business_partner(id),

    -- 检查来源
    source_type     VARCHAR(10),               -- SO:销售订单 DN:发货 BI:发票
    source_id       UUID,                      -- 来源单据ID
    source_number   VARCHAR(20),               -- 来源单据号

    -- 检查结果
    check_result    VARCHAR(1) NOT NULL,       -- P:通过 W:警告 B:冻结
    credit_limit    DECIMAL(18,2),             -- 信用额度
    credit_used     DECIMAL(18,2),             -- 已用额度
    check_amount    DECIMAL(18,2),             -- 检查金额
    over_limit_amount DECIMAL(18,2),           -- 超额金额

    -- 处理
    released_by     UUID,                      -- 释放人
    released_at     TIMESTAMP,                 -- 释放时间

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 8. 资产会计 (FI-AA)

对标 SAP FI-AA (Financial Accounting - Asset Accounting)

### 8.1 资产主数据 (fi_asset_master)

对标 SAP ANLA

```sql
CREATE TABLE fi_asset_master (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 资产编号 (对标 SAP ANLA-ANLN1/ANLN2)
    asset_number    VARCHAR(12) NOT NULL,       -- 资产编号
    sub_number      VARCHAR(4) DEFAULT '0000',  -- 子编号

    -- 资产分类 (对标 SAP ANLA-ANLKL)
    asset_class     VARCHAR(8) NOT NULL,        -- 资产分类
    -- 1000:建筑物 1100:机器设备 1200:办公设备 1300:运输工具
    -- 1400:电子设备 1500:无形资产 1600:土地 1700:在建工程 1800:租赁资产

    -- 描述
    asset_name      VARCHAR(100) NOT NULL,      -- 资产名称
    asset_name2     VARCHAR(100),               -- 资产名称2
    description     TEXT,                       -- 描述

    -- 标识
    serial_number   VARCHAR(18),                -- 序列号 (对标 SAP ANLA-SERNR)
    inventory_number VARCHAR(25),               -- 库存编号 (对标 SAP ANLA-INVNR)

    -- 资产类型 (对标 SAP ANLA-ANLTP)
    asset_type      VARCHAR(2),                 -- 资产类型
    -- 00:普通 10:在建工程 20:低值 30:租赁 40:无形

    -- 资本化日期 (对标 SAP ANLA-AKTIV)
    capitalization_date DATE,                   -- 资本化日期
    acquisition_date DATE,                      -- 购置日期
    deactivation_date DATE,                     -- 停用日期

    -- 组织分配 (对标 SAP ANLA-KOSTL/PRCTR/GSBER)
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    profit_center_id UUID REFERENCES sys_profit_center(id),
    business_area   VARCHAR(4),                 -- 业务范围
    location        VARCHAR(20),                -- 资产位置 (对标 SAP ANLA-STORT)
    room            VARCHAR(8),                 -- 房间 (对标 SAP ANLA-RAUMN)
    plant_id        UUID,                       -- 工厂

    -- 供应商信息
    vendor_id       UUID REFERENCES bp_business_partner(id),
    manufacturer    VARCHAR(30),                -- 制造商 (对标 SAP ANLA-HERST)
    construction_year INTEGER,                  -- 建造年份 (对标 SAP ANLA-BAUJA)

    -- 折旧参数
    useful_life_years INTEGER,                  -- 使用年限(年)
    useful_life_periods INTEGER,                -- 使用年限(期间)
    depreciation_key VARCHAR(4),                -- 折旧码 (对标 SAP T093)
    -- LINR:直线法 DECL:余额递减 DBLV:双倍余额递减 SYD:年数总和 UNIT:工作量法

    -- 残值
    scrap_value     DECIMAL(18,2) DEFAULT 0,    -- 残值
    scrap_value_pct DECIMAL(5,2) DEFAULT 0,     -- 残值率%

    -- 状态 (对标 SAP ANLA-XSPEB)
    asset_status    VARCHAR(2) DEFAULT 'AC',    -- 资产状态
    -- AC:活跃 DP:已处置 IP:在建 BL:冻结

    -- 审计
    status          general_status DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (company_id, asset_number, sub_number)
);

-- 索引
CREATE INDEX idx_fi_asset_company ON fi_asset_master (company_id);
CREATE INDEX idx_fi_asset_class ON fi_asset_master (asset_class);
CREATE INDEX idx_fi_asset_cost_center ON fi_asset_master (cost_center_id);
CREATE INDEX idx_fi_asset_status ON fi_asset_master (asset_status);
```

### 8.2 资产价值 (fi_asset_value)

对标 SAP ANLC

```sql
CREATE TABLE fi_asset_value (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 资产关联
    asset_id        UUID NOT NULL REFERENCES fi_asset_master(id),
    fiscal_year     INTEGER NOT NULL,

    -- 折旧范围 (对标 SAP ANLC-AFABER)
    depreciation_area VARCHAR(2) DEFAULT '01',
    -- 01:账面折旧 10:税务折旧 20:集团折旧 30:IFRS折旧 40:管理折旧

    -- 价值字段 (对标 SAP ANLC)
    acquisition_value DECIMAL(18,2) DEFAULT 0,  -- 购置价值 (AKTIV)
    cum_acquisition DECIMAL(18,2) DEFAULT 0,    -- 累计购置 (KANSW)
    ord_depreciation DECIMAL(18,2) DEFAULT 0,   -- 累计折旧 (NAPRO)
    planned_depr    DECIMAL(18,2) DEFAULT 0,    -- 计划折旧 (PLPRO)
    special_depr    DECIMAL(18,2) DEFAULT 0,    -- 特别折旧 (SOLPRO)
    revaluation    DECIMAL(18,2) DEFAULT 0,    -- 重估价值 (AUFNAH)

    -- 账面价值
    book_value      DECIMAL(18,2) DEFAULT 0,    -- 账面净值 (BUCHW)
    net_book_value  DECIMAL(18,2) GENERATED ALWAYS AS (
        acquisition_value - ord_depreciation
    ) STORED,                                   -- 净账面价值

    -- 本期折旧
    period_depr     DECIMAL(18,2) DEFAULT 0,    -- 本期折旧
    ytd_depr        DECIMAL(18,2) DEFAULT 0,    -- 年度累计折旧

    -- 残值 (对标 SAP ANLC-AFABU)
    scrap_value     DECIMAL(18,2) DEFAULT 0,    -- 残值

    -- 折旧开始日期
    depr_start_date DATE,                       -- 折旧开始日期

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version         INTEGER DEFAULT 0,

    UNIQUE (asset_id, fiscal_year, depreciation_area)
);

-- 索引
CREATE INDEX idx_fi_asset_value_asset ON fi_asset_value (asset_id);
CREATE INDEX idx_fi_asset_value_year ON fi_asset_value (fiscal_year);
```

### 8.3 折旧运行 (fi_depreciation_run)

对标 SAP AFAB

```sql
CREATE TABLE fi_depreciation_run (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 运行期间
    fiscal_year     INTEGER NOT NULL,
    period          INTEGER NOT NULL,

    -- 折旧范围
    depreciation_area VARCHAR(2) DEFAULT '01',

    -- 运行日期
    run_date        DATE NOT NULL DEFAULT CURRENT_DATE,

    -- 运行结果
    total_depreciation DECIMAL(18,2) DEFAULT 0, -- 折旧总额
    asset_count     INTEGER DEFAULT 0,          -- 资产数量

    -- 运行状态
    run_status      VARCHAR(2) DEFAULT '01',    -- 运行状态
    -- 01:计划中 02:运行中 03:已完成 04:已过账 05:错误

    -- 过账信息
    fi_document_id  UUID,                       -- 生成的FI凭证ID
    posted_at       TIMESTAMP,                  -- 过账时间
    posted_by       UUID,                       -- 过账人

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (company_id, fiscal_year, period, depreciation_area)
);
```

### 8.4 资产业务 (fi_asset_transaction)

对标 SAP ANEP

```sql
CREATE TABLE fi_asset_transaction (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 资产关联
    asset_id        UUID NOT NULL REFERENCES fi_asset_master(id),
    sub_number      VARCHAR(4) DEFAULT '0000',

    -- 交易类型 (对标 SAP ANEP-BWASL)
    transaction_type VARCHAR(3) NOT NULL,       -- 交易类型
    -- 100:外部购置 110:自建资产 120:赠予 130:租赁
    -- 200:转移(从) 210:转移(到)
    -- 300:出售 310:报废 320:清理

    -- 凭证关联
    fi_document_id  UUID,                       -- FI凭证ID
    fi_document_number VARCHAR(10),             -- FI凭证号

    -- 日期
    transaction_date DATE NOT NULL,             -- 交易日期
    posting_date    DATE NOT NULL,              -- 过账日期
    fiscal_year     INTEGER NOT NULL,
    period          INTEGER NOT NULL,

    -- 价值
    acquisition_value DECIMAL(18,2) DEFAULT 0,  -- 购置价值
    accumulated_depr DECIMAL(18,2) DEFAULT 0,   -- 累计折旧
    book_value      DECIMAL(18,2) DEFAULT 0,    -- 账面价值

    -- 业务伙伴
    partner_id      UUID REFERENCES bp_business_partner(id),
    partner_type    VARCHAR(2),                 -- C:客户 V:供应商

    -- 参考
    reference       VARCHAR(16),                -- 参考号
    description     TEXT,                       -- 描述

    -- 资产转移专用字段
    from_asset_id   UUID REFERENCES fi_asset_master(id),
    to_asset_id     UUID REFERENCES fi_asset_master(id),
    from_cost_center_id UUID,
    to_cost_center_id UUID,

    -- 资产处置专用字段
    disposal_type   VARCHAR(2),                 -- 处置类型
    disposal_reason TEXT,                       -- 处置原因
    revenue         DECIMAL(18,2) DEFAULT 0,    -- 处置收入
    gain_loss       DECIMAL(18,2) DEFAULT 0,    -- 处置损益

    -- 审计
    status          general_status DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID
);

-- 索引
CREATE INDEX idx_fi_asset_txn_asset ON fi_asset_transaction (asset_id);
CREATE INDEX idx_fi_asset_txn_type ON fi_asset_transaction (transaction_type);
CREATE INDEX idx_fi_asset_txn_date ON fi_asset_transaction (transaction_date);
CREATE INDEX idx_fi_asset_txn_document ON fi_asset_transaction (fi_document_id);
```

---

## 9. 期间控制

### 9.1 会计年度变式 (fi_fiscal_year_variant)

对标 SAP T009/T009B

```sql
CREATE TABLE fi_fiscal_year_variant (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 变式代码 (对标 SAP T009-PERIV)
    variant_code    VARCHAR(2) NOT NULL,        -- 变式代码
    name            VARCHAR(50) NOT NULL,       -- 名称

    -- 年度类型
    fiscal_year_type VARCHAR(1) NOT NULL,       -- 年度类型
    -- K:日历年 (1月-12月)
    -- V:非日历年 (如:4月-次年3月)

    -- 期间数量
    normal_periods  INTEGER DEFAULT 12,         -- 正常期间数
    special_periods INTEGER DEFAULT 4,          -- 特殊期间数

    -- 年度起始月
    year_start_month INTEGER DEFAULT 1,         -- 年度起始月份

    status          general_status DEFAULT 'ACTIVE',

    UNIQUE (tenant_id, variant_code)
);
```

### 9.2 期间控制 (fi_period_control)

对标 SAP T001B

```sql
CREATE TABLE fi_period_control (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 年度期间
    fiscal_year     INTEGER NOT NULL,
    fiscal_period   INTEGER NOT NULL,

    -- 科目类型控制
    asset_postable      BOOLEAN DEFAULT TRUE,   -- 资产科目可过账
    liability_postable  BOOLEAN DEFAULT TRUE,   -- 负债科目可过账
    equity_postable     BOOLEAN DEFAULT TRUE,   -- 权益科目可过账
    revenue_postable    BOOLEAN DEFAULT TRUE,   -- 收入科目可过账
    expense_postable    BOOLEAN DEFAULT TRUE,   -- 费用科目可过账

    -- 特殊标识
    is_open         BOOLEAN DEFAULT TRUE,       -- 期间是否开放
    is_special      BOOLEAN DEFAULT FALSE,      -- 是否特殊期间

    -- 期间日期
    period_start    DATE NOT NULL,              -- 期间开始日期
    period_end      DATE NOT NULL,              -- 期间结束日期

    -- 关闭信息
    closed_at       TIMESTAMP,                  -- 关闭时间
    closed_by       UUID,                       -- 关闭人

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (company_id, fiscal_year, fiscal_period)
);
```

---

## 10. AP/AR 视图

### 10.1 应付账款未清项视图

对标 SAP BSIK

```sql
CREATE VIEW v_fi_ap_open_items AS
SELECT
    i.id,
    i.tenant_id,
    i.header_id,
    i.line_item,
    h.document_number,
    h.document_date,
    h.posting_date,
    h.fiscal_year,
    h.period,
    h.document_type_id,

    -- 供应商信息
    i.partner_id AS vendor_id,
    bp.name AS vendor_name,

    -- 科目信息
    i.account_id,
    i.account_code,
    a.account_name,

    -- 金额
    i.debit_credit,
    i.amount,
    i.amount_dc,
    h.currency_id,

    -- 付款信息
    i.payment_term,
    i.baseline_date,
    i.due_date,

    -- 清算
    i.clearing_date,
    i.clearing_doc_id,

    -- 逾期天数
    CASE
        WHEN i.clearing_date IS NULL AND i.due_date < CURRENT_DATE
        THEN CURRENT_DATE - i.due_date
        ELSE 0
    END AS days_overdue,

    -- 成本对象
    i.cost_center_id,
    i.profit_center_id

FROM fi_journal_entry_itm i
JOIN fi_journal_entry_hdr h ON h.id = i.header_id AND h.fiscal_year = i.fiscal_year
LEFT JOIN bp_business_partner bp ON bp.id = i.partner_id
LEFT JOIN fi_account a ON a.id = i.account_id
WHERE i.partner_type = 'V'                    -- 供应商
  AND i.clearing_date IS NULL                  -- 未清
  AND h.is_posted = TRUE
  AND h.is_reversed = FALSE;
```

### 10.2 应付账款已清项视图

对标 SAP BSAK

```sql
CREATE VIEW v_fi_ap_cleared_items AS
SELECT
    i.id,
    i.tenant_id,
    i.header_id,
    i.line_item,
    h.document_number,
    h.document_date,
    h.posting_date,
    h.fiscal_year,

    -- 供应商信息
    i.partner_id AS vendor_id,
    bp.name AS vendor_name,

    -- 科目信息
    i.account_id,
    i.account_code,

    -- 金额
    i.debit_credit,
    i.amount,
    i.amount_dc,

    -- 清算
    i.clearing_date,
    i.clearing_doc_id,
    c.document_number AS clearing_doc_number

FROM fi_journal_entry_itm i
JOIN fi_journal_entry_hdr h ON h.id = i.header_id AND h.fiscal_year = i.fiscal_year
LEFT JOIN bp_business_partner bp ON bp.id = i.partner_id
LEFT JOIN fi_journal_entry_hdr c ON c.id = i.clearing_doc_id
WHERE i.partner_type = 'V'                    -- 供应商
  AND i.clearing_date IS NOT NULL              -- 已清
  AND h.is_posted = TRUE;
```

### 10.3 应收账款未清项视图

对标 SAP BSID

```sql
CREATE VIEW v_fi_ar_open_items AS
SELECT
    i.id,
    i.tenant_id,
    i.header_id,
    i.line_item,
    h.document_number,
    h.document_date,
    h.posting_date,
    h.fiscal_year,
    h.period,
    h.document_type_id,

    -- 客户信息
    i.partner_id AS customer_id,
    bp.name AS customer_name,

    -- 科目信息
    i.account_id,
    i.account_code,
    a.account_name,

    -- 金额
    i.debit_credit,
    i.amount,
    i.amount_dc,
    h.currency_id,

    -- 收款信息
    i.payment_term,
    i.baseline_date,
    i.due_date,

    -- 清算
    i.clearing_date,
    i.clearing_doc_id,

    -- 逾期天数
    CASE
        WHEN i.clearing_date IS NULL AND i.due_date < CURRENT_DATE
        THEN CURRENT_DATE - i.due_date
        ELSE 0
    END AS days_overdue,

    -- 成本对象
    i.cost_center_id,
    i.profit_center_id,

    -- 信用检查
    cl.credit_limit,
    cl.credit_used,
    cl.credit_available

FROM fi_journal_entry_itm i
JOIN fi_journal_entry_hdr h ON h.id = i.header_id AND h.fiscal_year = i.fiscal_year
LEFT JOIN bp_business_partner bp ON bp.id = i.partner_id
LEFT JOIN fi_account a ON a.id = i.account_id
LEFT JOIN fi_credit_limit cl ON cl.customer_id = i.partner_id AND cl.status = 'ACTIVE'
WHERE i.partner_type = 'C'                    -- 客户
  AND i.clearing_date IS NULL                  -- 未清
  AND h.is_posted = TRUE
  AND h.is_reversed = FALSE;
```

### 10.4 应收账款已清项视图

对标 SAP BSAD

```sql
CREATE VIEW v_fi_ar_cleared_items AS
SELECT
    i.id,
    i.tenant_id,
    i.header_id,
    i.line_item,
    h.document_number,
    h.document_date,
    h.posting_date,
    h.fiscal_year,

    -- 客户信息
    i.partner_id AS customer_id,
    bp.name AS customer_name,

    -- 科目信息
    i.account_id,
    i.account_code,

    -- 金额
    i.debit_credit,
    i.amount,
    i.amount_dc,

    -- 清算
    i.clearing_date,
    i.clearing_doc_id,
    c.document_number AS clearing_doc_number

FROM fi_journal_entry_itm i
JOIN fi_journal_entry_hdr h ON h.id = i.header_id AND h.fiscal_year = i.fiscal_year
LEFT JOIN bp_business_partner bp ON bp.id = i.partner_id
LEFT JOIN fi_journal_entry_hdr c ON c.id = i.clearing_doc_id
WHERE i.partner_type = 'C'                    -- 客户
  AND i.clearing_date IS NOT NULL              -- 已清
  AND h.is_posted = TRUE;
```

### 10.5 账龄分析视图

```sql
CREATE VIEW v_fi_ar_aging AS
SELECT
    customer_id,
    customer_name,
    currency_id,

    -- 账龄区间
    SUM(CASE WHEN days_overdue <= 0 THEN amount ELSE 0 END) AS current_amount,
    SUM(CASE WHEN days_overdue > 0 AND days_overdue <= 30 THEN amount ELSE 0 END) AS overdue_1_30,
    SUM(CASE WHEN days_overdue > 30 AND days_overdue <= 60 THEN amount ELSE 0 END) AS overdue_31_60,
    SUM(CASE WHEN days_overdue > 60 AND days_overdue <= 90 THEN amount ELSE 0 END) AS overdue_61_90,
    SUM(CASE WHEN days_overdue > 90 THEN amount ELSE 0 END) AS overdue_over_90,

    -- 总计
    SUM(amount) AS total_amount

FROM v_fi_ar_open_items
GROUP BY customer_id, customer_name, currency_id;
```

---

## 11. 报表视图

### 11.1 试算平衡表

```sql
CREATE VIEW v_fi_trial_balance AS
SELECT
    a.account_code,
    a.account_name,
    a.account_type,

    -- 年初余额 (需要从上年度结转)
    0 AS opening_balance,

    -- 本期借方
    b.year_debit,
    -- 本期贷方
    b.year_credit,

    -- 期末余额
    b.year_debit - b.year_credit AS closing_balance

FROM fi_account a
LEFT JOIN fi_account_balance b
    ON b.account_id = a.id
    AND b.fiscal_year = EXTRACT(YEAR FROM CURRENT_DATE)
WHERE a.status = 'ACTIVE'
  AND a.is_postable = TRUE
ORDER BY a.account_code;
```

### 11.2 凭证明细查询

```sql
CREATE VIEW v_fi_journal_detail AS
SELECT
    h.fiscal_year,
    h.document_number,
    h.document_date,
    h.posting_date,
    h.header_text,

    i.line_item,
    i.account_code,
    a.account_name,
    i.debit_credit,
    i.amount,
    i.amount_dc,
    i.item_text,

    i.cost_center_id,
    i.profit_center_id

FROM fi_journal_entry_hdr h
JOIN fi_journal_entry_itm i
    ON i.header_id = h.id
    AND i.fiscal_year = h.fiscal_year
JOIN fi_account a ON a.id = i.account_id
WHERE h.is_deleted = FALSE
  AND h.is_posted = TRUE;
```

---

## 12. 性能优化

### 12.1 索引优化

```sql
-- 覆盖索引: 凭证列表查询
CREATE INDEX idx_fje_hdr_list ON fi_journal_entry_hdr (company_id, posting_date DESC)
    INCLUDE (document_number, document_date, status, header_text);

-- 复合索引: 科目余额查询
CREATE INDEX idx_fi_balance_lookup ON fi_account_balance
    (company_id, fiscal_year, account_id)
    INCLUDE (period_01_debit, period_01_credit);
```

### 12.2 分区维护

```sql
-- 自动创建下年度分区 (定时任务)
CREATE OR REPLACE FUNCTION create_fiscal_year_partitions(p_year INTEGER)
RETURNS VOID AS $$
BEGIN
    EXECUTE format('
        CREATE TABLE IF NOT EXISTS fi_journal_entry_hdr_%s
            PARTITION OF fi_journal_entry_hdr
            FOR VALUES FROM (%s) TO (%s)
    ', p_year, p_year, p_year + 1);

    EXECUTE format('
        CREATE TABLE IF NOT EXISTS fi_journal_entry_itm_%s
            PARTITION OF fi_journal_entry_itm
            FOR VALUES FROM (%s) TO (%s)
    ', p_year, p_year, p_year + 1);
END;
$$ LANGUAGE plpgsql;
```

---

## 13. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.1 | 2026-03-14 | 补充科目表字段、信用管理表、资产会计表、期间控制表、AP/AR视图 |
| 1.0 | 2026-03-14 | 初始版本 |
