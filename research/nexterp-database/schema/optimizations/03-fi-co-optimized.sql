-- ============================================================================
-- NextERP 优化版 FI/CO Schema
-- 优化点：分区表、余额表结构、时间有效性索引、凭证过账事务
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 会计凭证头 - 按年度分区
-- ----------------------------------------------------------------------------

CREATE TABLE fi_journal_entry_hdr (
    id              UUID NOT NULL,
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL,

    -- 凭证信息
    document_number VARCHAR(10) NOT NULL,
    fiscal_year     INTEGER NOT NULL,
    document_type_id UUID,

    -- 日期
    document_date   DATE NOT NULL,
    posting_date    DATE NOT NULL,
    period          INTEGER NOT NULL,

    -- 货币
    currency_id     UUID NOT NULL,
    exchange_rate   DECIMAL(12,6) DEFAULT 1,

    -- 参考
    reference       VARCHAR(20),
    header_text     VARCHAR(100),

    -- 来源
    source_type     VARCHAR(10),
    source_document VARCHAR(50),

    -- 状态
    status          VARCHAR(20) DEFAULT 'DRAFT',
    is_posted       BOOLEAN DEFAULT FALSE,
    is_reversed     BOOLEAN DEFAULT FALSE,
    reversed_doc_id UUID,

    -- 审批
    approval_status VARCHAR(20) DEFAULT 'DRAFT',
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

-- 分区表索引
CREATE INDEX idx_fje_hdr_company ON fi_journal_entry_hdr (company_id);
CREATE INDEX idx_fje_hdr_date ON fi_journal_entry_hdr (posting_date);
CREATE INDEX idx_fje_hdr_doc ON fi_journal_entry_hdr (company_id, document_number);
CREATE INDEX idx_fje_hdr_status ON fi_journal_entry_hdr (status, is_posted);

COMMENT ON TABLE fi_journal_entry_hdr IS '会计凭证头（按年度分区）';

-- ----------------------------------------------------------------------------
-- 2. 会计凭证项 - 按年度分区
-- ----------------------------------------------------------------------------

CREATE TABLE fi_journal_entry_itm (
    id              UUID NOT NULL,
    fiscal_year     INTEGER NOT NULL,  -- 冗余存储用于分区
    tenant_id       UUID NOT NULL,

    header_id       UUID NOT NULL,
    line_item       INTEGER NOT NULL,

    -- 科目
    account_id      UUID NOT NULL,

    -- 业务伙伴
    partner_id      UUID,

    -- 借贷标识
    debit_credit    VARCHAR(1) NOT NULL CHECK (debit_credit IN ('D', 'C')),

    -- 金额
    amount          DECIMAL(23,2) NOT NULL,
    amount_dc       DECIMAL(23,2),
    currency_id     UUID,

    -- 税
    tax_code        VARCHAR(2),
    tax_amount      DECIMAL(23,2),

    -- 成本对象
    cost_center_id  UUID,
    profit_center_id UUID,
    internal_order  VARCHAR(12),

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
    clearing_doc_id UUID,

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

-- 索引
CREATE INDEX idx_fje_itm_header ON fi_journal_entry_itm (header_id);
CREATE INDEX idx_fje_itm_account ON fi_journal_entry_itm (account_id);
CREATE INDEX idx_fje_itm_partner ON fi_journal_entry_itm (partner_id);
CREATE INDEX idx_fje_itm_cost_center ON fi_journal_entry_itm (cost_center_id);

COMMENT ON TABLE fi_journal_entry_itm IS '会计凭证项（按年度分区）';

-- ----------------------------------------------------------------------------
-- 3. 科目余额 - 优化为独立字段
-- ----------------------------------------------------------------------------

CREATE TABLE fi_account_balance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL,
    account_id      UUID NOT NULL,
    fiscal_year     INTEGER NOT NULL,
    currency_id     UUID NOT NULL,

    -- 期间余额（使用独立字段替代数组，便于索引和查询）
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

    UNIQUE (company_id, account_id, fiscal_year, currency_id)
);

-- 创建计算余额的视图
CREATE VIEW v_fi_account_balance AS
SELECT
    id, tenant_id, company_id, account_id, fiscal_year, currency_id,

    -- 期间余额（借-贷）
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

    year_debit - year_credit AS year_balance,
    year_debit, year_credit

FROM fi_account_balance;

CREATE INDEX idx_fi_account_balance_company ON fi_account_balance (company_id, fiscal_year);

COMMENT ON TABLE fi_account_balance IS '科目余额表（优化版：独立字段）';
COMMENT ON VIEW v_fi_account_balance IS '科目余额视图（含计算余额）';

-- ----------------------------------------------------------------------------
-- 4. 凭证过账存储过程（原子化）
-- ----------------------------------------------------------------------------

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
    v_debit_amount DECIMAL(23,2);
    v_credit_amount DECIMAL(23,2);
BEGIN
    -- 锁定凭证头
    SELECT tenant_id, company_id, fiscal_year,
           EXTRACT(MONTH FROM posting_date)::INTEGER, currency_id,
           status, is_posted
    INTO v_tenant_id, v_company_id, v_fiscal_year, v_period, v_currency_id
    FROM fi_journal_entry_hdr
    WHERE id = p_entry_id AND fiscal_year = p_fiscal_year
    FOR UPDATE;

    -- 检查状态
    IF NOT FOUND THEN
        RAISE EXCEPTION '凭证不存在：%', p_entry_id;
    END IF;

    IF v_tenant_id IS NULL THEN
        RAISE EXCEPTION '无效的凭证';
    END IF;

    -- 遍历凭证项，更新余额
    FOR v_item IN
        SELECT * FROM fi_journal_entry_itm
        WHERE header_id = p_entry_id AND fiscal_year = p_fiscal_year
    LOOP
        -- 计算借贷金额
        IF v_item.debit_credit = 'D' THEN
            v_debit_amount := v_item.amount_dc;
            v_credit_amount := 0;
        ELSE
            v_debit_amount := 0;
            v_credit_amount := v_item.amount_dc;
        END IF;

        -- 更新余额（使用动态SQL根据期间更新）
        EXECUTE format('
            INSERT INTO fi_account_balance (
                tenant_id, company_id, account_id, fiscal_year, currency_id,
                period_%s_debit, period_%s_credit,
                year_debit, year_credit, version
            ) VALUES ($1, $2, $3, $4, $5, $6, $7, $6, $7, 0)
            ON CONFLICT (company_id, account_id, fiscal_year, currency_id) DO UPDATE SET
                period_%s_debit = fi_account_balance.period_%s_debit + $6,
                period_%s_credit = fi_account_balance.period_%s_credit + $7,
                year_debit = fi_account_balance.year_debit + $6,
                year_credit = fi_account_balance.year_credit + $7,
                version = fi_account_balance.version + 1,
                updated_at = CURRENT_TIMESTAMP
        ', v_period, v_period, v_period, v_period, v_period, v_period)
        USING v_tenant_id, v_company_id, v_item.account_id,
              v_fiscal_year, COALESCE(v_item.currency_id, v_currency_id),
              v_debit_amount, v_credit_amount;
    END LOOP;

    -- 更新凭证状态
    UPDATE fi_journal_entry_hdr
    SET status = 'COMPLETED',
        is_posted = TRUE,
        posted_at = CURRENT_TIMESTAMP,
        version = version + 1
    WHERE id = p_entry_id AND fiscal_year = p_fiscal_year;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- 5. 冲销凭证存储过程
-- ----------------------------------------------------------------------------

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
    v_new_doc_num := generate_business_code(
        v_tenant_id, 'JE', 'RV', TRUE
    );

    -- 创建冲销凭证头
    INSERT INTO fi_journal_entry_hdr (
        id, tenant_id, company_id, document_number, fiscal_year,
        document_date, posting_date, period, currency_id,
        source_type, source_document,
        status, is_posted, reversed_doc_id,
        created_by, updated_by
    ) VALUES (
        gen_random_uuid(), v_tenant_id, v_company_id, v_new_doc_num,
        EXTRACT(YEAR FROM p_reversal_date),
        p_reversal_date, p_reversal_date,
        EXTRACT(MONTH FROM p_reversal_date),
        v_currency_id,
        'REVERSAL', p_entry_id::TEXT,
        'COMPLETED', TRUE, p_entry_id,
        current_setting('app.current_user_id', TRUE)::UUID,
        current_setting('app.current_user_id', TRUE)::UUID
    ) RETURNING id INTO v_new_id;

    -- 创建冲销凭证项（借贷反向）
    FOR v_item IN
        SELECT * FROM fi_journal_entry_itm
        WHERE header_id = p_entry_id
    LOOP
        INSERT INTO fi_journal_entry_itm (
            id, fiscal_year, tenant_id, header_id, line_item,
            account_id, partner_id, debit_credit,
            amount, amount_dc, currency_id,
            tax_code, tax_amount,
            cost_center_id, profit_center_id, item_text
        ) VALUES (
            gen_random_uuid(),
            EXTRACT(YEAR FROM p_reversal_date),
            v_tenant_id,
            v_new_id,
            v_item.line_item,
            v_item.account_id,
            v_item.partner_id,
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

    -- 更新余额
    PERFORM post_journal_entry(v_new_id, EXTRACT(YEAR FROM p_reversal_date));

    -- 标记原凭证已冲销
    UPDATE fi_journal_entry_hdr
    SET is_reversed = TRUE
    WHERE id = p_entry_id;

    RETURN v_new_id;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- 6. 添加审计触发器
-- ----------------------------------------------------------------------------

PERFORM add_audit_trigger('fi_journal_entry_hdr');
PERFORM add_audit_trigger('fi_journal_entry_itm');
PERFORM add_audit_trigger('fi_account_balance');

-- 添加审计日志触发器（关键表）
PERFORM add_audit_log_trigger('fi_journal_entry_hdr');
PERFORM add_audit_log_trigger('fi_account_balance');

COMMENT ON FUNCTION post_journal_entry IS '凭证过账存储过程（原子化更新余额）';
COMMENT ON FUNCTION reverse_journal_entry IS '凭证冲销存储过程';
