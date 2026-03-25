# TR 模块数据库设计

**模块**: Treasury (资金管理)
**对标**: SAP ECC TR (FDES/FF70/FF7A)
**版本**: 1.0

---

## 1. 模块概述

### 1.1 业务范围

| 子模块 | 说明 | SAP 对标 |
|--------|------|----------|
| 现金管理 | 现金头寸管理 | FF7A/FF7B |
| 流动性预测 | 资金预测 | FF64/FF65 |
| 银行账户 | 银行账户管理 | FI12 |
| 银行对账 | 银行对账单 | FF67/FEBA |
| 资金计划 | 现金流量计划 | FDES |

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                     TR Module Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    银行账户管理                          │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │    │
│  │  │  银行主数据 │  │  银行账户   │  │ 账户余额    │      │    │
│  │  │ tr_bank     │  │ tr_bank_    │  │ tr_account_ │      │    │
│  │  │             │  │  account    │  │  balance    │      │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│        ┌─────────────────────┼─────────────────────┐            │
│        ▼                     ▼                     ▼            │
│  ┌──────────┐          ┌──────────┐          ┌──────────┐      │
│  │ 银行对账 │          │ 现金管理 │          │ 流动预测 │      │
│  │tr_bank_  │          │tr_cash_  │          │tr_forecast│     │
│  │statement │          │ management│         │          │      │
│  └──────────┘          └──────────┘          └──────────┘      │
│        │                     │                     │            │
│        └─────────────────────┴─────────────────────┘            │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    资金计划                              │    │
│  │                    tr_cash_plan                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 资金管理流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    Treasury Management Flow                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐     │
│  │ 应收预测 │   │ 应付预测 │   │ 税金计划 │   │ 薪资计划 │     │
│  │ (SD模块) │   │ (MM模块) │   │ (FI模块) │   │ (HR模块) │     │
│  └──────────┘   └──────────┘   └──────────┘   └──────────┘     │
│       │              │              │              │             │
│       └──────────────┴──────────────┴──────────────┘             │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                  流动性预测 (Cash Forecast)              │    │
│  │   Day 1-7:    高确定性 (已开票应收/已确认应付)            │    │
│  │   Day 8-30:   中确定性 (预期收入/计划付款)                │    │
│  │   Day 31-90:  低确定性 (历史趋势预测)                     │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                   现金头寸 (Cash Position)               │    │
│  │   银行账户余额 + 在途资金 - 冻结资金 = 可用资金           │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                   资金决策 (Cash Decision)               │    │
│  │   ◆ 盈余资金 → 理财投资 / 提前还款                       │    │
│  │   ◆ 资金缺口 → 银行借款 / 延迟付款                       │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 银行主数据

### 2.1 银行主数据 (tr_bank)

对标 SAP BNKA

```sql
CREATE TABLE tr_bank (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID,

    -- 银行信息
    bank_country    VARCHAR(3) NOT NULL,       -- 银行国家
    bank_key        VARCHAR(15) NOT NULL,      -- 银行代码 (如中国:联行号)
    bank_name       VARCHAR(100) NOT NULL,     -- 银行名称
    bank_name_en    VARCHAR(100),              -- 英文名称

    -- 银行类型
    bank_type       VARCHAR(2),                -- 银行类型
    -- 01:商业银行 02:中央银行 03:外资银行 04:政策性银行

    -- 地址
    region_id       UUID REFERENCES core_region(id),
    city            VARCHAR(40),
    street          VARCHAR(60),
    postal_code     VARCHAR(10),

    -- 联系信息
    swift_code      VARCHAR(11),               -- SWIFT代码
    contact_person  VARCHAR(80),
    phone           VARCHAR(50),
    email           VARCHAR(100),

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (bank_country, bank_key)
);
```

### 2.2 银行账户 (tr_bank_account)

对标 SAP T012/T012K

```sql
CREATE TABLE tr_bank_account (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 银行
    bank_id         UUID NOT NULL REFERENCES tr_bank(id),
    bank_key        VARCHAR(15),
    bank_name       VARCHAR(100),

    -- 公司
    company_id      UUID NOT NULL REFERENCES sys_company(id),
    company_code    VARCHAR(4),

    -- 账户信息
    account_number  VARCHAR(30) NOT NULL,      -- 账号
    account_name    VARCHAR(100),              -- 账户名称
    account_holder  VARCHAR(100),              -- 户名

    -- 账户类型
    account_type    VARCHAR(2) NOT NULL,       -- 账户类型
    -- 01:活期 02:定期 03:保证金 04:外币 05:贷款

    -- 货币
    currency_id     UUID NOT NULL REFERENCES core_currency(id),
    currency_code   VARCHAR(3),

    -- GL科目
    gl_account_id   UUID REFERENCES fi_account(id),
    gl_account_code VARCHAR(10),

    -- 限额
    credit_limit    DECIMAL(18,2),             -- 授信额度
    used_limit      DECIMAL(18,2) DEFAULT 0,   -- 已用额度

    -- 利率
    interest_rate   DECIMAL(6,4),              -- 利率
    interest_type   VARCHAR(2),                -- 利率类型
    -- 01:固定 02:浮动

    -- 开户/到期
    open_date       DATE,
    maturity_date   DATE,

    -- 账户状态
    account_status  VARCHAR(2) DEFAULT '01',   -- 01:正常 02:冻结 03:销户

    -- 主账户标识
    is_primary      BOOLEAN DEFAULT FALSE,

    -- 网银
    is_ebank_enabled BOOLEAN DEFAULT FALSE,
    ebank_url       VARCHAR(200),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (company_id, account_number)
);
```

### 2.3 账户余额 (tr_account_balance)

```sql
CREATE TABLE tr_account_balance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 账户
    account_id      UUID NOT NULL REFERENCES tr_bank_account(id),
    account_number  VARCHAR(30),

    -- 日期
    balance_date    DATE NOT NULL,

    -- 余额
    opening_balance DECIMAL(18,2) NOT NULL,    -- 期初余额
    closing_balance DECIMAL(18,2) NOT NULL,    -- 期末余额

    -- 变动
    total_debit     DECIMAL(18,2) DEFAULT 0,   -- 借方合计
    total_credit    DECIMAL(18,2) DEFAULT 0,   -- 贷方合计

    -- 在途
    in_transit      DECIMAL(18,2) DEFAULT 0,   -- 在途金额

    -- 冻结
    frozen_amount   DECIMAL(18,2) DEFAULT 0,   -- 冻结金额

    -- 可用余额
    available_balance DECIMAL(18,2) GENERATED ALWAYS AS (
        closing_balance - frozen_amount
    ) STORED,

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (account_id, balance_date)
);
```

---

## 3. 银行对账

### 3.1 银行对账单头 (tr_bank_statement_hdr)

对标 SAP FEBKO

```sql
CREATE TABLE tr_bank_statement_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 对账单信息
    statement_number VARCHAR(18) NOT NULL,     -- 对账单号
    external_id     VARCHAR(35),               -- 外部ID

    -- 银行账户
    account_id      UUID NOT NULL REFERENCES tr_bank_account(id),
    account_number  VARCHAR(30),
    bank_key        VARCHAR(15),

    -- 日期
    statement_date  DATE NOT NULL,             -- 对账日期
    value_date      DATE,                      -- 起息日

    -- 余额
    opening_balance DECIMAL(18,2),             -- 期初余额
    closing_balance DECIMAL(18,2),             -- 期末余额

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 统计
    total_debit     DECIMAL(18,2) DEFAULT 0,   -- 借方合计
    total_credit    DECIMAL(18,2) DEFAULT 0,   -- 贷方合计
    debit_count     INTEGER DEFAULT 0,
    credit_count    INTEGER DEFAULT 0,

    -- 状态
    statement_status VARCHAR(2) DEFAULT '01',  -- 01:导入 02:处理中 03:已对账 04:已过账
    is_reconciled   BOOLEAN DEFAULT FALSE,

    -- 来源
    source_type     VARCHAR(2),                -- 来源类型
    -- 01:网银导入 02:手工录入 03:EBICS
    file_name       VARCHAR(200),              -- 原始文件名

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, statement_number, account_number, statement_date)
);
```

### 3.2 银行对账单项 (tr_bank_statement_itm)

对标 SAP FEBEP

```sql
CREATE TABLE tr_bank_statement_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 对账单头
    header_id       UUID NOT NULL REFERENCES tr_bank_statement_hdr(id) ON DELETE CASCADE,
    statement_number VARCHAR(18),

    -- 行号
    line_number     INTEGER NOT NULL,

    -- 日期
    value_date      DATE,                      -- 起息日
    posting_date    DATE,                      -- 记账日期

    -- 交易类型
    transaction_type VARCHAR(3),               -- 交易类型
    -- 001:入账 002:支出 003:转账 004:利息 005:手续费

    -- 金额
    amount          DECIMAL(18,2) NOT NULL,    -- 金额
    debit_credit    VARCHAR(1) NOT NULL,       -- D:借 C:贷

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 描述
    description     VARCHAR(140),              -- 摘要
    reference       VARCHAR(35),               -- 参考号

    -- 交易方
    partner_bank    VARCHAR(15),               -- 对方银行
    partner_account VARCHAR(30),               -- 对方账号
    partner_name    VARCHAR(100),              -- 对方户名

    -- 对账状态
    reconciliation_status VARCHAR(2) DEFAULT '01', -- 01:未对账 02:已对账 03:部分对账
    is_reconciled   BOOLEAN DEFAULT FALSE,

    -- 关联凭证
    fi_document_id  UUID,                      -- FI凭证ID
    fi_document_number VARCHAR(10),

    -- 对账金额
    reconciled_amount DECIMAL(18,2) DEFAULT 0,

    -- 备注
    remarks         TEXT,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, line_number)
);
```

### 3.3 银行对账明细 (tr_bank_reconciliation)

```sql
CREATE TABLE tr_bank_reconciliation (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 对账单项
    statement_item_id UUID NOT NULL REFERENCES tr_bank_statement_itm(id),

    -- FI凭证
    fi_document_id  UUID NOT NULL REFERENCES fi_journal_entry_hdr(id),
    fi_document_number VARCHAR(10),

    -- 对账金额
    reconciled_amount DECIMAL(18,2) NOT NULL,

    -- 对账日期
    reconciliation_date DATE NOT NULL DEFAULT CURRENT_DATE,

    -- 方式
    match_type      VARCHAR(2),                -- 匹配方式
    -- 01:自动 02:手工 03:规则

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID
);
```

---

## 4. 现金管理

### 4.1 现金头寸 (tr_cash_position)

对标 SAP FDSB

```sql
CREATE TABLE tr_cash_position (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 公司
    company_id      UUID NOT NULL REFERENCES sys_company(id),
    company_code    VARCHAR(4),

    -- 日期
    position_date   DATE NOT NULL,

    -- 货币
    currency_id     UUID NOT NULL REFERENCES core_currency(id),

    -- 银行余额
    bank_balance    DECIMAL(18,2) DEFAULT 0,   -- 银行余额

    -- 在途
    in_transit_in   DECIMAL(18,2) DEFAULT 0,   -- 在途收入
    in_transit_out  DECIMAL(18,2) DEFAULT 0,   -- 在途支出

    -- 冻结
    frozen_amount   DECIMAL(18,2) DEFAULT 0,   -- 冻结资金

    -- 现金余额
    cash_balance    DECIMAL(18,2) GENERATED ALWAYS AS (
        bank_balance + in_transit_in - in_transit_out - frozen_amount
    ) STORED,

    -- 信用额度
    credit_available DECIMAL(18,2) DEFAULT 0,  -- 可用信用额度

    -- 总可用
    total_available DECIMAL(18,2) GENERATED ALWAYS AS (
        cash_balance + credit_available
    ) STORED,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (company_id, position_date, currency_id)
);
```

### 4.2 现金流量明细 (tr_cash_flow_item)

对标 SAP FDES

```sql
CREATE TABLE tr_cash_flow_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 公司
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 类型
    flow_type       VARCHAR(2) NOT NULL,       -- 流量类型
    -- 01:经营 02:投资 03:筹资

    -- 方向
    direction       VARCHAR(1) NOT NULL,       -- I:流入 O:流出

    -- 分类
    category        VARCHAR(4),                -- 现金流量分类

    -- 日期
    value_date      DATE NOT NULL,             -- 起息日

    -- 金额
    amount          DECIMAL(18,2) NOT NULL,
    currency_id     UUID REFERENCES core_currency(id),

    -- 来源
    source_type     VARCHAR(2),                -- 来源类型
    -- FI:财务凭证 SD:销售订单 MM:采购订单 TR:资金手工
    source_document VARCHAR(20),
    source_item     INTEGER,

    -- 描述
    description     VARCHAR(100),

    -- 计划/实际
    is_planned      BOOLEAN DEFAULT FALSE,

    -- 状态
    is_realized     BOOLEAN DEFAULT FALSE,
    realized_date   DATE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID
);
```

---

## 5. 流动性预测

### 5.1 流动性预测头 (tr_forecast_hdr)

```sql
CREATE TABLE tr_forecast_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 预测信息
    forecast_number VARCHAR(10) NOT NULL,      -- 预测号
    forecast_type   VARCHAR(2) NOT NULL,       -- 预测类型
    -- 01:日预测 02:周预测 03:月预测

    -- 日期范围
    forecast_date   DATE NOT NULL,             -- 预测日期
    period_start    DATE NOT NULL,             -- 预测起始
    period_end      DATE NOT NULL,             -- 预测结束

    -- 公司
    company_id      UUID REFERENCES sys_company(id),

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 状态
    forecast_status VARCHAR(2) DEFAULT '01',   -- 01:创建 02:已确认

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, forecast_number)
);
```

### 5.2 流动性预测行 (tr_forecast_line)

对标 SAP T049

```sql
CREATE TABLE tr_forecast_line (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 预测头
    header_id       UUID NOT NULL REFERENCES tr_forecast_hdr(id) ON DELETE CASCADE,

    -- 公司
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 预测日期
    forecast_date   DATE NOT NULL,

    -- 日期层次
    week_number     INTEGER,
    month_number    INTEGER,
    quarter_number  INTEGER,
    fiscal_year     INTEGER,

    -- 现金流量类型
    cash_flow_group VARCHAR(4),                -- 现金流量组
    cash_flow_type  VARCHAR(2),                -- 类型
    -- 01:应收 02:应付 03:税金 04:工资 05:其他

    -- 确定性级别
    certainty_level VARCHAR(2),                -- 确定性
    -- A:高(已确认) B:中(已计划) C:低(预测)

    -- 预测金额
    inflow_amount   DECIMAL(18,2) DEFAULT 0,   -- 流入
    outflow_amount  DECIMAL(18,2) DEFAULT 0,   -- 流出
    net_flow        DECIMAL(18,2) GENERATED ALWAYS AS (
        inflow_amount - outflow_amount
    ) STORED,

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 来源
    source_type     VARCHAR(2),
    source_count    INTEGER DEFAULT 0,         -- 来源单据数

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 6. 资金计划

### 6.1 资金计划头 (tr_cash_plan_hdr)

```sql
CREATE TABLE tr_cash_plan_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 计划信息
    plan_number     VARCHAR(10) NOT NULL,      -- 计划号
    plan_name       VARCHAR(100) NOT NULL,     -- 计划名称
    description     TEXT,

    -- 计划类型
    plan_type       VARCHAR(2) NOT NULL,       -- 计划类型
    -- 01:日计划 02:周计划 03:月计划

    -- 期间
    plan_date       DATE NOT NULL,             -- 计划日期
    period_start    DATE NOT NULL,
    period_end      DATE NOT NULL,

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 状态
    plan_status     VARCHAR(2) DEFAULT '01',   -- 01:草稿 02:已审批 03:已执行

    -- 审批
    approved_by     UUID,
    approved_at     TIMESTAMP,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, plan_number)
);
```

### 6.2 资金计划行 (tr_cash_plan_line)

```sql
CREATE TABLE tr_cash_plan_line (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 计划头
    header_id       UUID NOT NULL REFERENCES tr_cash_plan_hdr(id) ON DELETE CASCADE,

    -- 公司
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 日期
    plan_date       DATE NOT NULL,

    -- 计划类型
    line_type       VARCHAR(2) NOT NULL,       -- 行类型
    -- 01:期初余额 02:预计收入 03:预计支出 04:期末余额

    -- 分类
    category        VARCHAR(4),                -- 分类

    -- 计划金额
    planned_amount  DECIMAL(18,2) NOT NULL,    -- 计划金额

    -- 实际金额
    actual_amount   DECIMAL(18,2) DEFAULT 0,   -- 实际金额

    -- 差异
    variance        DECIMAL(18,2) GENERATED ALWAYS AS (
        actual_amount - planned_amount
    ) STORED,

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 描述
    description     VARCHAR(100),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 7. 外汇管理

### 7.1 汇率类型 (tr_exchange_rate_type)

```sql
CREATE TABLE tr_exchange_rate_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    rate_type_code  VARCHAR(2) NOT NULL,       -- 汇率类型代码
    rate_type_name  VARCHAR(100) NOT NULL,     -- 名称

    -- 用途
    usage_type      VARCHAR(2),                -- 用途
    -- 01:记账 02:报表 03:预算

    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, rate_type_code)
);
```

### 7.2 汇率 (tr_exchange_rate)

对标 SAP TCURR

```sql
CREATE TABLE tr_exchange_rate (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 汇率类型
    rate_type_id    UUID NOT NULL REFERENCES tr_exchange_rate_type(id),
    rate_type_code  VARCHAR(2),

    -- 货币对
    from_currency_id UUID NOT NULL REFERENCES core_currency(id),
    from_currency   VARCHAR(3),
    to_currency_id  UUID NOT NULL REFERENCES core_currency(id),
    to_currency     VARCHAR(3),

    -- 汇率
    exchange_rate   DECIMAL(12,6) NOT NULL,    -- 汇率
    ratio_from      INTEGER DEFAULT 1,         -- 比率(从)
    ratio_to        INTEGER DEFAULT 1,         -- 比率(到)

    -- 有效期
    valid_from      DATE NOT NULL,
    valid_to        DATE DEFAULT '9999-12-31',

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (rate_type_code, from_currency, to_currency, valid_from)
);
```

---

## 8. 存储过程

### 8.1 更新现金头寸

```sql
CREATE OR REPLACE FUNCTION tr_update_cash_position(
    p_company_id UUID,
    p_date DATE,
    p_currency_id UUID
) RETURNS UUID AS $$
DECLARE
    v_position_id UUID;
    v_bank_balance DECIMAL(18,2) := 0;
    v_in_transit_in DECIMAL(18,2) := 0;
    v_in_transit_out DECIMAL(18,2) := 0;
    v_frozen DECIMAL(18,2) := 0;
BEGIN
    -- 计算银行余额
    SELECT COALESCE(SUM(closing_balance), 0)
    INTO v_bank_balance
    FROM tr_account_balance bal
    JOIN tr_bank_account acc ON acc.id = bal.account_id
    WHERE acc.company_id = p_company_id
      AND bal.balance_date = p_date
      AND acc.currency_id = p_currency_id;

    -- 计算在途收入
    SELECT COALESCE(SUM(amount), 0)
    INTO v_in_transit_in
    FROM tr_cash_flow_item
    WHERE company_id = p_company_id
      AND value_date <= p_date
      AND is_realized = FALSE
      AND direction = 'I'
      AND currency_id = p_currency_id;

    -- 计算在途支出
    SELECT COALESCE(SUM(amount), 0)
    INTO v_in_transit_out
    FROM tr_cash_flow_item
    WHERE company_id = p_company_id
      AND value_date <= p_date
      AND is_realized = FALSE
      AND direction = 'O'
      AND currency_id = p_currency_id;

    -- 插入或更新现金头寸
    INSERT INTO tr_cash_position (
        tenant_id, company_id, position_date, currency_id,
        bank_balance, in_transit_in, in_transit_out, frozen_amount
    ) VALUES (
        (SELECT tenant_id FROM sys_company WHERE id = p_company_id),
        p_company_id, p_date, p_currency_id,
        v_bank_balance, v_in_transit_in, v_in_transit_out, v_frozen
    )
    ON CONFLICT (company_id, position_date, currency_id) DO UPDATE SET
        bank_balance = v_bank_balance,
        in_transit_in = v_in_transit_in,
        in_transit_out = v_in_transit_out,
        frozen_amount = v_frozen,
        updated_at = CURRENT_TIMESTAMP
    RETURNING id INTO v_position_id;

    RETURN v_position_id;
END;
$$ LANGUAGE plpgsql;
```

### 8.2 生成流动性预测

```sql
CREATE OR REPLACE FUNCTION tr_generate_forecast(
    p_company_id UUID,
    p_start_date DATE,
    p_end_date DATE,
    p_user_id UUID
) RETURNS UUID AS $$
DECLARE
    v_forecast_id UUID;
    v_forecast_number VARCHAR(10);
    v_current_date DATE;
BEGIN
    -- 生成预测号
    v_forecast_number := generate_business_code(
        (SELECT tenant_id FROM sys_company WHERE id = p_company_id),
        'FC', NULL, NULL
    );

    -- 创建预测头
    INSERT INTO tr_forecast_hdr (
        tenant_id, forecast_number, forecast_type,
        forecast_date, period_start, period_end,
        company_id, created_by
    ) VALUES (
        (SELECT tenant_id FROM sys_company WHERE id = p_company_id),
        v_forecast_number, '01',
        CURRENT_DATE, p_start_date, p_end_date,
        p_company_id, p_user_id
    ) RETURNING id INTO v_forecast_id;

    -- 按日期生成预测行
    v_current_date := p_start_date;
    WHILE v_current_date <= p_end_date LOOP
        -- 应收预测 (来自销售模块)
        INSERT INTO tr_forecast_line (
            tenant_id, header_id, company_id, forecast_date,
            cash_flow_type, certainty_level,
            inflow_amount, currency_id, source_type
        )
        SELECT
            (SELECT tenant_id FROM sys_company WHERE id = p_company_id),
            v_forecast_id, p_company_id, v_current_date,
            '01', 'B',
            COALESCE(SUM(net_value), 0),
            currency_id, 'SD'
        FROM sd_sales_order_hdr
        WHERE company_id = p_company_id
          AND payment_term IS NOT NULL
          AND billing_status != 'C';

        -- 应付预测 (来自采购模块)
        INSERT INTO tr_forecast_line (
            tenant_id, header_id, company_id, forecast_date,
            cash_flow_type, certainty_level,
            outflow_amount, currency_id, source_type
        )
        SELECT
            (SELECT tenant_id FROM sys_company WHERE id = p_company_id),
            v_forecast_id, p_company_id, v_current_date,
            '02', 'B',
            COALESCE(SUM(total_value), 0),
            currency_id, 'MM'
        FROM mm_purchase_order_hdr
        WHERE company_id = p_company_id
          AND po_status IN ('02', '03')
          AND payment_term IS NOT NULL;

        v_current_date := v_current_date + INTERVAL '1 day';
    END LOOP;

    RETURN v_forecast_id;
END;
$$ LANGUAGE plpgsql;
```

---

## 9. 视图定义

### 9.1 银行账户概览视图

```sql
CREATE VIEW v_tr_bank_account_overview AS
SELECT
    ba.account_number,
    ba.account_name,
    ba.account_type,
    b.bank_name,
    b.bank_key,
    ba.currency_code,
    ba.company_code,
    c.company_name,
    bal.closing_balance,
    bal.available_balance,
    bal.balance_date,
    ba.account_status,
    ba.is_primary

FROM tr_bank_account ba
JOIN tr_bank b ON b.id = ba.bank_id
JOIN sys_company c ON c.id = ba.company_id
LEFT JOIN LATERAL (
    SELECT closing_balance, available_balance, balance_date
    FROM tr_account_balance
    WHERE account_id = ba.id
    ORDER BY balance_date DESC
    LIMIT 1
) bal ON TRUE
ORDER BY ba.company_code, ba.account_number;
```

### 9.2 现金流量报表视图

```sql
CREATE VIEW v_tr_cash_flow_report AS
SELECT
    c.company_code,
    c.company_name,
    cf.value_date,
    cf.flow_type,
    cf.direction,
    cf.category,
    cf.amount,
    cf.currency_id,
    cf.source_type,
    cf.source_document,
    cf.description,
    cf.is_planned,
    cf.is_realized,
    CASE cf.direction
        WHEN 'I' THEN '流入'
        WHEN 'O' THEN '流出'
    END AS direction_text,
    CASE cf.flow_type
        WHEN '01' THEN '经营活动'
        WHEN '02' THEN '投资活动'
        WHEN '03' THEN '筹资活动'
    END AS flow_type_text

FROM tr_cash_flow_item cf
JOIN sys_company c ON c.id = cf.company_id
ORDER BY cf.value_date DESC, c.company_code;
```

---

## 10. 索引策略

```sql
-- 银行
CREATE INDEX idx_tr_bank_key ON tr_bank (bank_country, bank_key);

-- 银行账户
CREATE INDEX idx_tr_bank_acct_company ON tr_bank_account (company_id);
CREATE INDEX idx_tr_bank_acct_number ON tr_bank_account (account_number);
CREATE INDEX idx_tr_bank_acct_bank ON tr_bank_account (bank_id);

-- 账户余额
CREATE INDEX idx_tr_acct_bal_account ON tr_account_balance (account_id);
CREATE INDEX idx_tr_acct_bal_date ON tr_account_balance (balance_date);

-- 银行对账单
CREATE INDEX idx_tr_bs_hdr_number ON tr_bank_statement_hdr (tenant_id, statement_number);
CREATE INDEX idx_tr_bs_hdr_account ON tr_bank_statement_hdr (account_id, statement_date);
CREATE INDEX idx_tr_bs_itm_header ON tr_bank_statement_itm (header_id);
CREATE INDEX idx_tr_bs_itm_date ON tr_bank_statement_itm (value_date);

-- 现金头寸
CREATE INDEX idx_tr_cash_pos_company ON tr_cash_position (company_id);
CREATE INDEX idx_tr_cash_pos_date ON tr_cash_position (position_date);

-- 现金流量
CREATE INDEX idx_tr_cf_company ON tr_cash_flow_item (company_id);
CREATE INDEX idx_tr_cf_date ON tr_cash_flow_item (value_date);
CREATE INDEX idx_tr_cf_type ON tr_cash_flow_item (flow_type);

-- 汇率
CREATE INDEX idx_tr_rate_currency ON tr_exchange_rate (from_currency, to_currency, valid_from);
```

---

## 11. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
