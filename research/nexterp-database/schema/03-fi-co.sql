-- ============================================================================
-- NextERP FI/CO Schema
-- 财务会计、成本控制 - 借鉴 SAP ECC BKPF/BSEG 结构
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 会计科目表 (参考 SAP SKA1, SKB1)
-- ----------------------------------------------------------------------------

-- 科目表定义 (参考 SAP T004)
CREATE TABLE fi_chart_of_accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    code            VARCHAR(4) NOT NULL,          -- 科目表代码
    name            VARCHAR(100) NOT NULL,        -- 科目表名称
    name_en         VARCHAR(100),

    -- 维护语言
    language        VARCHAR(5) DEFAULT 'zh-CN',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE fi_chart_of_accounts IS '科目表定义 (参考 SAP T004)';

-- 科目组 (参考 SAP T077S)
CREATE TABLE fi_account_group (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    chart_of_accounts_id UUID NOT NULL REFERENCES fi_chart_of_accounts(id),

    code            VARCHAR(4) NOT NULL,          -- 科目组代码
    name            VARCHAR(100) NOT NULL,

    -- 科目范围
    account_from    VARCHAR(10),                  -- 科目起
    account_to      VARCHAR(10),                  -- 科目止

    -- 属性
    is_pl_account   BOOLEAN DEFAULT FALSE,        -- 是否损益科目
    is_bs_account   BOOLEAN DEFAULT FALSE,        -- 是否资产负债表科目

    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (chart_of_accounts_id, code)
);

COMMENT ON TABLE fi_account_group IS '科目组 (参考 SAP T077S)';

-- 总账科目主数据 (参考 SAP SKA1 + SKB1)
CREATE TABLE fi_gl_account (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    chart_of_accounts_id UUID NOT NULL REFERENCES fi_chart_of_accounts(id),
    company_id      UUID REFERENCES sys_company(id),

    -- 科目编码
    account_number  VARCHAR(10) NOT NULL,         -- 科目代码
    account_group_id UUID REFERENCES fi_account_group(id),

    -- 描述
    name            VARCHAR(100) NOT NULL,        -- 科目名称
    name_en         VARCHAR(100),

    -- 科目类型
    account_type    VARCHAR(1) NOT NULL CHECK (account_type IN (
        'A',  -- 资产
        'L',  -- 负债
        'E',  -- 权益
        'I',  -- 收入
        'X'   -- 费用
    )),

    -- 损益/资产负债
    is_pl_account   BOOLEAN DEFAULT FALSE,
    pl_account_type VARCHAR(1),                   -- P=主营业, S=其他

    -- 控制数据
    is_postable     BOOLEAN DEFAULT TRUE,         -- 可否过账
    is_reconc_acct  BOOLEAN DEFAULT FALSE,        -- 是否统驭科目
    reconc_type     VARCHAR(1),                   -- D=客户, K=供应商, A=资产

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),
    is_currency_acct BOOLEAN DEFAULT FALSE,       -- 外币科目

    -- 税务
    tax_category    VARCHAR(1),                   -- 税类别

    -- 未清项管理
    open_item_mgmt  BOOLEAN DEFAULT FALSE,
    line_item_display BOOLEAN DEFAULT TRUE,

    -- 字段状态组
    field_status_group VARCHAR(4),

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    is_blocked      BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (chart_of_accounts_id, company_id, account_number)
);

CREATE INDEX idx_fi_gl_account_company ON fi_gl_account (company_id);
CREATE INDEX idx_fi_gl_account_type ON fi_gl_account (tenant_id, account_type);

COMMENT ON TABLE fi_gl_account IS '总账科目主数据 (参考 SAP SKA1 + SKB1)';

-- ----------------------------------------------------------------------------
-- 会计凭证 (参考 SAP BKPF, BSEG)
-- ----------------------------------------------------------------------------

-- 凭证类型 (参考 SAP T003)
CREATE TABLE fi_document_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    code            VARCHAR(2) NOT NULL,          -- 凭证类型代码
    name            VARCHAR(100) NOT NULL,

    -- 属性
    number_range    VARCHAR(4),                   -- 号码范围
    is_reverse      BOOLEAN DEFAULT FALSE,        -- 是否冲销凭证

    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE fi_document_type IS '凭证类型 (参考 SAP T003)';

-- 会计凭证头 (参考 SAP BKPF)
CREATE TABLE fi_journal_entry_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 凭证信息
    document_number VARCHAR(10) NOT NULL,         -- 凭证号
    fiscal_year     INTEGER NOT NULL,             -- 会计年度
    document_type_id UUID REFERENCES fi_document_type(id),

    -- 日期
    document_date   DATE NOT NULL,                -- 凭证日期
    posting_date    DATE NOT NULL,                -- 过账日期
    period          INTEGER NOT NULL,             -- 会计期间

    -- 货币
    currency_id     UUID NOT NULL REFERENCES core_currency(id),
    exchange_rate   DECIMAL(12,6) DEFAULT 1,

    -- 参考
    reference       VARCHAR(20),                  -- 参考号
    header_text     VARCHAR(100),                 -- 凭证头文本

    -- 来源
    source_type     VARCHAR(10),                  -- 来源类型 (手工/自动)
    source_document VARCHAR(50),                  -- 来源单据
    transaction_code VARCHAR(20),                 -- 事务代码

    -- 状态
    status          general_status DEFAULT 'DRAFT',
    is_posted       BOOLEAN DEFAULT FALSE,
    is_reversed     BOOLEAN DEFAULT FALSE,
    reversed_doc_id UUID REFERENCES fi_journal_entry_hdr(id),

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

    UNIQUE (company_id, document_number, fiscal_year)
);

CREATE INDEX idx_fi_journal_entry_hdr_company ON fi_journal_entry_hdr (company_id);
CREATE INDEX idx_fi_journal_entry_hdr_date ON fi_journal_entry_hdr (posting_date);
CREATE INDEX idx_fi_journal_entry_hdr_fiscal ON fi_journal_entry_hdr (company_id, fiscal_year, period);

COMMENT ON TABLE fi_journal_entry_hdr IS '会计凭证头 (参考 SAP BKPF)';

-- 会计凭证项 (参考 SAP BSEG)
CREATE TABLE fi_journal_entry_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    header_id       UUID NOT NULL REFERENCES fi_journal_entry_hdr(id) ON DELETE CASCADE,

    -- 行项目号
    line_item       INTEGER NOT NULL,

    -- 科目
    account_id      UUID NOT NULL REFERENCES fi_gl_account(id),
    account_number  VARCHAR(10),                  -- 冗余存储

    -- 业务伙伴
    partner_id      UUID REFERENCES bp_partner(id),
    customer_id     UUID REFERENCES bp_partner(id),
    supplier_id     UUID REFERENCES bp_partner(id),

    -- 借贷标识
    debit_credit    debit_credit NOT NULL,

    -- 金额
    amount          DECIMAL(23,2) NOT NULL,       -- 交易货币金额
    amount_dc       DECIMAL(23,2),                -- 本位币金额
    currency_id     UUID REFERENCES core_currency(id),

    -- 税
    tax_code        VARCHAR(2),
    tax_amount      DECIMAL(23,2),

    -- 成本对象
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    profit_center_id UUID REFERENCES sys_profit_center(id),
    internal_order  VARCHAR(12),

    -- 项目
    wbs_element     VARCHAR(24),

    -- 业务范围
    business_area   VARCHAR(4),

    -- 文本
    item_text       VARCHAR(100),

    -- 分配
    assignment      VARCHAR(18),
    reference_key_1 VARCHAR(20),
    reference_key_2 VARCHAR(20),
    reference_key_3 VARCHAR(20),

    -- 清算
    clearing_date   DATE,
    clearing_doc_id UUID REFERENCES fi_journal_entry_hdr(id),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, line_item)
);

CREATE INDEX idx_fi_journal_entry_itm_header ON fi_journal_entry_itm (header_id);
CREATE INDEX idx_fi_journal_entry_itm_account ON fi_journal_entry_itm (account_id);
CREATE INDEX idx_fi_journal_entry_itm_partner ON fi_journal_entry_itm (partner_id);
CREATE INDEX idx_fi_journal_entry_itm_cost_center ON fi_journal_entry_itm (cost_center_id);

COMMENT ON TABLE fi_journal_entry_itm IS '会计凭证项 (参考 SAP BSEG)';

-- ----------------------------------------------------------------------------
-- 余额表 (参考 SAP GLT0)
-- ----------------------------------------------------------------------------

-- 科目余额 (保留聚合表设计)
CREATE TABLE fi_account_balance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL REFERENCES sys_company(id),
    account_id      UUID NOT NULL REFERENCES fi_gl_account(id),

    -- 年度
    fiscal_year     INTEGER NOT NULL,

    -- 货币
    currency_id     UUID NOT NULL REFERENCES core_currency(id),

    -- 期间余额 (使用数组)
    -- period_balance[1] = 期间1余额, period_balance[12] = 期间12余额
    period_balance  DECIMAL(23,2)[] DEFAULT ARRAY[0,0,0,0,0,0,0,0,0,0,0,0]::DECIMAL(23,2)[],
    period_debit    DECIMAL(23,2)[] DEFAULT ARRAY[0,0,0,0,0,0,0,0,0,0,0,0]::DECIMAL(23,2)[],
    period_credit   DECIMAL(23,2)[] DEFAULT ARRAY[0,0,0,0,0,0,0,0,0,0,0,0]::DECIMAL(23,2)[],

    -- 年度累计
    year_balance    DECIMAL(23,2) DEFAULT 0,
    year_debit      DECIMAL(23,2) DEFAULT 0,
    year_credit     DECIMAL(23,2) DEFAULT 0,

    -- 审计
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (company_id, account_id, fiscal_year, currency_id)
);

CREATE INDEX idx_fi_account_balance_company ON fi_account_balance (company_id, fiscal_year);

COMMENT ON TABLE fi_account_balance IS '科目余额表 (参考 SAP GLT0)';
COMMENT ON COLUMN fi_account_balance.period_balance IS '期间余额数组 [p01..p12]';

-- 业务伙伴余额 (参考 SAP KNC1, LFC1)
CREATE TABLE fi_partner_balance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL REFERENCES sys_company(id),
    partner_id      UUID NOT NULL REFERENCES bp_partner(id),

    -- 年度
    fiscal_year     INTEGER NOT NULL,

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 期间余额
    period_balance  DECIMAL(23,2)[] DEFAULT ARRAY[0,0,0,0,0,0,0,0,0,0,0,0]::DECIMAL(23,2)[],
    period_debit    DECIMAL(23,2)[] DEFAULT ARRAY[0,0,0,0,0,0,0,0,0,0,0,0]::DECIMAL(23,2)[],
    period_credit   DECIMAL(23,2)[] DEFAULT ARRAY[0,0,0,0,0,0,0,0,0,0,0,0]::DECIMAL(23,2)[],

    -- 年度累计
    year_balance    DECIMAL(23,2) DEFAULT 0,
    year_debit      DECIMAL(23,2) DEFAULT 0,
    year_credit     DECIMAL(23,2) DEFAULT 0,

    -- 审计
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (company_id, partner_id, fiscal_year, currency_id)
);

CREATE INDEX idx_fi_partner_balance_partner ON fi_partner_balance (partner_id);
CREATE INDEX idx_fi_partner_balance_company ON fi_partner_balance (company_id, fiscal_year);

COMMENT ON TABLE fi_partner_balance IS '业务伙伴余额表 (参考 SAP KNC1, LFC1)';

-- ----------------------------------------------------------------------------
-- 税务配置 (参考 SAP T007A)
-- ----------------------------------------------------------------------------

-- 税码
CREATE TABLE fi_tax_code (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    code            VARCHAR(2) NOT NULL,          -- 税码
    name            VARCHAR(100) NOT NULL,

    -- 税类型
    tax_type        VARCHAR(1) CHECK (tax_type IN ('I', 'O')),  -- 进项/销项

    -- 税率
    tax_rate        DECIMAL(5,2) NOT NULL,        -- 税率 (%)

    -- 科目
    tax_account_id  UUID REFERENCES fi_gl_account(id),

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE fi_tax_code IS '税码表 (参考 SAP T007A)';

-- ----------------------------------------------------------------------------
-- 付款条件 (参考 SAP T052)
-- ----------------------------------------------------------------------------

CREATE TABLE fi_payment_term (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    code            VARCHAR(4) NOT NULL,
    name            VARCHAR(100) NOT NULL,

    -- 现金折扣天数
    day_1           INTEGER,                      -- 第一个折扣天数
    day_2           INTEGER,                      -- 第二个折扣天数
    day_3           INTEGER,                      -- 到期天数

    -- 现金折扣百分比
    discount_1      DECIMAL(5,2),                 -- 第一个折扣百分比
    discount_2      DECIMAL(5,2),                 -- 第二个折扣百分比

    -- 固定日期
    fixed_day       INTEGER,                      -- 固定日

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE fi_payment_term IS '付款条件表 (参考 SAP T052)';

-- ----------------------------------------------------------------------------
-- 函数和触发器
-- ----------------------------------------------------------------------------

-- 更新余额函数
CREATE OR REPLACE FUNCTION update_account_balance(
    p_company_id UUID,
    p_account_id UUID,
    p_fiscal_year INTEGER,
    p_period INTEGER,
    p_amount DECIMAL(23,2),
    p_debit_credit debit_credit,
    p_currency_id UUID
) RETURNS VOID AS $$
BEGIN
    INSERT INTO fi_account_balance (
        tenant_id, company_id, account_id, fiscal_year, currency_id,
        period_balance, period_debit, period_credit,
        year_balance, year_debit, year_credit
    )
    SELECT
        c.tenant_id, p_company_id, p_account_id, p_fiscal_year, p_currency_id,
        CASE WHEN p_debit_credit = 'D' THEN
            ARRAY_FILL(0::DECIMAL(23,2), ARRAY[12])::DECIMAL(23,2)[]
        ELSE
            ARRAY_FILL(0::DECIMAL(23,2), ARRAY[12])::DECIMAL(23,2)[]
        END,
        CASE WHEN p_debit_credit = 'D' THEN
            ARRAY_FILL(p_amount, ARRAY[1])::DECIMAL(23,2)[12] ||
            ARRAY_FILL(0::DECIMAL(23,2), ARRAY[11])::DECIMAL(23,2)[]
        ELSE
            ARRAY_FILL(0::DECIMAL(23,2), ARRAY[12])::DECIMAL(23,2)[]
        END,
        CASE WHEN p_debit_credit = 'C' THEN
            ARRAY_FILL(p_amount, ARRAY[1])::DECIMAL(23,2)[12] ||
            ARRAY_FILL(0::DECIMAL(23,2), ARRAY[11])::DECIMAL(23,2)[]
        ELSE
            ARRAY_FILL(0::DECIMAL(23,2), ARRAY[12])::DECIMAL(23,2)[]
        END,
        CASE WHEN p_debit_credit = 'D' THEN p_amount ELSE -p_amount END,
        CASE WHEN p_debit_credit = 'D' THEN p_amount ELSE 0 END,
        CASE WHEN p_debit_credit = 'C' THEN p_amount ELSE 0 END
    FROM sys_company c WHERE c.id = p_company_id
    ON CONFLICT (company_id, account_id, fiscal_year, currency_id) DO UPDATE SET
        period_balance[p_period] = fi_account_balance.period_balance[p_period] +
            CASE WHEN p_debit_credit = 'D' THEN p_amount ELSE -p_amount END,
        period_debit[p_period] = fi_account_balance.period_debit[p_period] +
            CASE WHEN p_debit_credit = 'D' THEN p_amount ELSE 0 END,
        period_credit[p_period] = fi_account_balance.period_credit[p_period] +
            CASE WHEN p_debit_credit = 'C' THEN p_amount ELSE 0 END,
        year_balance = fi_account_balance.year_balance +
            CASE WHEN p_debit_credit = 'D' THEN p_amount ELSE -p_amount END,
        year_debit = fi_account_balance.year_debit +
            CASE WHEN p_debit_credit = 'D' THEN p_amount ELSE 0 END,
        year_credit = fi_account_balance.year_credit +
            CASE WHEN p_debit_credit = 'C' THEN p_amount ELSE 0 END,
        updated_at = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- 触发器
CREATE TRIGGER trigger_fi_journal_entry_hdr_updated_at
    BEFORE UPDATE ON fi_journal_entry_hdr
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_fi_gl_account_updated_at
    BEFORE UPDATE ON fi_gl_account
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
