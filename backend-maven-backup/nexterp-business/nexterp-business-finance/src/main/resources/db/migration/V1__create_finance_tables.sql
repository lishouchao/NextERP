-- ==============================================================
-- 财务模块 - 数据库变更脚本
-- 创建时间: 2025-01-15
-- 说明: 财务科目、凭证、期间相关表结构
-- ==============================================================

-- 1. 创建财务科目表
-- ==============================================================
CREATE TABLE fin_account (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    account_code VARCHAR(50) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    account_type INT NOT NULL,
    account_direction INT NOT NULL,
    parent_id BIGINT,
    account_level INT NOT NULL,
    is_leaf BOOLEAN NOT NULL DEFAULT TRUE,
    is_cash BOOLEAN NOT NULL DEFAULT FALSE,
    is_bank BOOLEAN NOT NULL DEFAULT FALSE,
    is_quantity BOOLEAN NOT NULL DEFAULT FALSE,
    quantity_unit VARCHAR(20),
    is_foreign_currency BOOLEAN NOT NULL DEFAULT FALSE,
    currency VARCHAR(10),
    is_auxiliary BOOLEAN NOT NULL DEFAULT FALSE,
    auxiliary_type TEXT,
    opening_balance DECIMAL(19,2) DEFAULT 0,
    opening_quantity DECIMAL(19,4) DEFAULT 0,
    current_debit DECIMAL(19,2) DEFAULT 0,
    current_credit DECIMAL(19,2) DEFAULT 0,
    year_debit DECIMAL(19,2) DEFAULT 0,
    year_credit DECIMAL(19,2) DEFAULT 0,
    ending_balance DECIMAL(19,2) DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    sort_order INT,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE fin_account IS '财务科目表';
COMMENT ON COLUMN fin_account.account_code IS '科目编码';
COMMENT ON COLUMN fin_account.account_name IS '科目名称';
COMMENT ON COLUMN fin_account.account_type IS '科目类型 (1-资产 2-负债 3-所有者权益 4-成本 5-损益)';
COMMENT ON COLUMN fin_account.account_direction IS '科目方向 (1-借方 2-贷方)';
COMMENT ON COLUMN fin_account.parent_id IS '父科目ID';
COMMENT ON COLUMN fin_account.account_level IS '科目层级';
COMMENT ON COLUMN fin_account.is_leaf IS '是否叶子节点';
COMMENT ON COLUMN fin_account.is_cash IS '是否现金科目';
COMMENT ON COLUMN fin_account.is_bank IS '是否银行科目';
COMMENT ON COLUMN fin_account.is_quantity IS '是否数量核算';
COMMENT ON COLUMN fin_account.quantity_unit IS '数量单位';
COMMENT ON COLUMN fin_account.is_foreign_currency IS '是否外币核算';
COMMENT ON COLUMN fin_account.currency IS '币种';
COMMENT ON COLUMN fin_account.is_auxiliary IS '是否辅助核算';
COMMENT ON COLUMN fin_account.auxiliary_type IS '辅助核算类型(JSON数组)';
COMMENT ON COLUMN fin_account.opening_balance IS '期初余额';
COMMENT ON COLUMN fin_account.opening_quantity IS '期初数量';
COMMENT ON COLUMN fin_account.current_debit IS '本期借方发生额';
COMMENT ON COLUMN fin_account.current_credit IS '本期贷方发生额';
COMMENT ON COLUMN fin_account.year_debit IS '本年借方累计';
COMMENT ON COLUMN fin_account.year_credit IS '本年贷方累计';
COMMENT ON COLUMN fin_account.ending_balance IS '期末余额';
COMMENT ON COLUMN fin_account.status IS '状态 (0-禁用 1-启用)';

CREATE UNIQUE INDEX uk_account_code ON fin_account(account_code, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_account_type ON fin_account(account_type, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_account_parent ON fin_account(parent_id, tenant_id) WHERE is_deleted = FALSE;

-- 2. 创建财务凭证表
-- ==============================================================
CREATE TABLE fin_voucher (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    voucher_no VARCHAR(50) NOT NULL,
    voucher_word VARCHAR(10),
    voucher_date DATE NOT NULL,
    accounting_period VARCHAR(10) NOT NULL,
    voucher_type INT NOT NULL,
    attachment_count INT DEFAULT 0,
    debit_amount DECIMAL(19,2),
    credit_amount DECIMAL(19,2),
    created_by_id BIGINT,
    created_by_name VARCHAR(50),
    approved_by_id BIGINT,
    approved_by_name VARCHAR(50),
    approved_at TIMESTAMP,
    posted_by_id BIGINT,
    posted_by_name VARCHAR(50),
    posted_at TIMESTAMP,
    voucher_status INT NOT NULL DEFAULT 0,
    reject_reason VARCHAR(500),
    summary VARCHAR(500),
    remark VARCHAR(500),
    source_type VARCHAR(50),
    source_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE fin_voucher IS '财务凭证表';
COMMENT ON COLUMN fin_voucher.voucher_no IS '凭证号';
COMMENT ON COLUMN fin_voucher.voucher_word IS '凭证字 (记、收、付、转)';
COMMENT ON COLUMN fin_voucher.voucher_date IS '凭证日期';
COMMENT ON COLUMN fin_voucher.accounting_period IS '会计期间 (格式: YYYY-MM)';
COMMENT ON COLUMN fin_voucher.voucher_type IS '凭证类型 (1-收款 2-付款 3-转账)';
COMMENT ON COLUMN fin_voucher.attachment_count IS '附件数量';
COMMENT ON COLUMN fin_voucher.debit_amount IS '借方金额合计';
COMMENT ON COLUMN fin_voucher.credit_amount IS '贷方金额合计';
COMMENT ON COLUMN fin_voucher.created_by_id IS '制单人ID';
COMMENT ON COLUMN fin_voucher.created_by_name IS '制单人姓名';
COMMENT ON COLUMN fin_voucher.approved_by_id IS '审核人ID';
COMMENT ON COLUMN fin_voucher.approved_by_name IS '审核人姓名';
COMMENT ON COLUMN fin_voucher.approved_at IS '审核时间';
COMMENT ON COLUMN fin_voucher.posted_by_id IS '记账人ID';
COMMENT ON COLUMN fin_voucher.posted_by_name IS '记账人姓名';
COMMENT ON COLUMN fin_voucher.posted_at IS '记账时间';
COMMENT ON COLUMN fin_voucher.voucher_status IS '凭证状态 (0-草稿 1-待审核 2-已审核 3-已记账 4-已驳回)';
COMMENT ON COLUMN fin_voucher.reject_reason IS '驳回原因';
COMMENT ON COLUMN fin_voucher.summary IS '摘要';
COMMENT ON COLUMN fin_voucher.source_type IS '来源单据类型';
COMMENT ON COLUMN fin_voucher.source_id IS '来源单据ID';

CREATE UNIQUE INDEX uk_voucher_no ON fin_voucher(voucher_no, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_voucher_period ON fin_voucher(accounting_period, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_voucher_date ON fin_voucher(voucher_date, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_voucher_status ON fin_voucher(voucher_status, tenant_id) WHERE is_deleted = FALSE;

-- 3. 创建财务凭证分录表
-- ==============================================================
CREATE TABLE fin_voucher_entry (
    id BIGSERIAL PRIMARY KEY,
    voucher_id BIGINT NOT NULL,
    line_no INT NOT NULL,
    account_id BIGINT NOT NULL,
    account_code VARCHAR(50),
    account_name VARCHAR(100),
    summary VARCHAR(500),
    debit_amount DECIMAL(19,2),
    credit_amount DECIMAL(19,2),
    quantity DECIMAL(19,4),
    unit_price DECIMAL(19,4),
    currency VARCHAR(10),
    foreign_amount DECIMAL(19,2),
    exchange_rate DECIMAL(10,6),
    aux_customer_id BIGINT,
    aux_supplier_id BIGINT,
    aux_dept_id BIGINT,
    aux_employee_id BIGINT,
    aux_project_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE fin_voucher_entry IS '财务凭证分录表';
COMMENT ON COLUMN fin_voucher_entry.voucher_id IS '凭证ID';
COMMENT ON COLUMN fin_voucher_entry.line_no IS '行号';
COMMENT ON COLUMN fin_voucher_entry.account_id IS '科目ID';
COMMENT ON COLUMN fin_voucher_entry.account_code IS '科目编码';
COMMENT ON COLUMN fin_voucher_entry.account_name IS '科目名称';
COMMENT ON COLUMN fin_voucher_entry.summary IS '摘要';
COMMENT ON COLUMN fin_voucher_entry.debit_amount IS '借方金额';
COMMENT ON COLUMN fin_voucher_entry.credit_amount IS '贷方金额';
COMMENT ON COLUMN fin_voucher_entry.quantity IS '数量';
COMMENT ON COLUMN fin_voucher_entry.unit_price IS '单价';
COMMENT ON COLUMN fin_voucher_entry.currency IS '币种';
COMMENT ON COLUMN fin_voucher_entry.foreign_amount IS '原币金额';
COMMENT ON COLUMN fin_voucher_entry.exchange_rate IS '汇率';
COMMENT ON COLUMN fin_voucher_entry.aux_customer_id IS '辅助核算-客户ID';
COMMENT ON COLUMN fin_voucher_entry.aux_supplier_id IS '辅助核算-供应商ID';
COMMENT ON COLUMN fin_voucher_entry.aux_dept_id IS '辅助核算-部门ID';
COMMENT ON COLUMN fin_voucher_entry.aux_employee_id IS '辅助核算-员工ID';
COMMENT ON COLUMN fin_voucher_entry.aux_project_id IS '辅助核算-项目ID';

CREATE INDEX idx_voucher_entry_voucher ON fin_voucher_entry(voucher_id);
CREATE INDEX idx_voucher_entry_account ON fin_voucher_entry(account_id);

-- 4. 创建会计期间表
-- ==============================================================
CREATE TABLE fin_accounting_period (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    accounting_year INT NOT NULL,
    accounting_period VARCHAR(10) NOT NULL,
    period_start_date DATE NOT NULL,
    period_end_date DATE NOT NULL,
    period_status INT NOT NULL DEFAULT 0,
    voucher_start_no INT,
    voucher_end_no INT,
    voucher_count INT DEFAULT 0,
    total_debit DECIMAL(19,2) DEFAULT 0,
    total_credit DECIMAL(19,2) DEFAULT 0,
    closed_by_id BIGINT,
    closed_by_name VARCHAR(50),
    closed_at TIMESTAMP,
    reopened_by_id BIGINT,
    reopened_by_name VARCHAR(50),
    reopened_at TIMESTAMP,
    remark VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE fin_accounting_period IS '会计期间表';
COMMENT ON COLUMN fin_accounting_period.accounting_year IS '会计年度';
COMMENT ON COLUMN fin_accounting_period.accounting_period IS '会计期间 (格式: YYYY-MM)';
COMMENT ON COLUMN fin_accounting_period.period_start_date IS '期间起始日期';
COMMENT ON COLUMN fin_accounting_period.period_end_date IS '期间结束日期';
COMMENT ON COLUMN fin_accounting_period.period_status IS '期间状态 (0-未开启 1-已开启 2-已结账)';
COMMENT ON COLUMN fin_accounting_period.voucher_start_no IS '凭证起始号';
COMMENT ON COLUMN fin_accounting_period.voucher_end_no IS '凭证结束号';
COMMENT ON COLUMN fin_accounting_period.voucher_count IS '凭证数量';
COMMENT ON COLUMN fin_accounting_period.total_debit IS '借方发生额合计';
COMMENT ON COLUMN fin_accounting_period.total_credit IS '贷方发生额合计';
COMMENT ON COLUMN fin_accounting_period.closed_by_id IS '结账人ID';
COMMENT ON COLUMN fin_accounting_period.closed_by_name IS '结账人姓名';
COMMENT ON COLUMN fin_accounting_period.closed_at IS '结账时间';
COMMENT ON COLUMN fin_accounting_period.reopened_by_id IS '反结账人ID';
COMMENT ON COLUMN fin_accounting_period.reopened_by_name IS '反结账人姓名';
COMMENT ON COLUMN fin_accounting_period.reopened_at IS '反结账时间';

CREATE UNIQUE INDEX uk_accounting_period ON fin_accounting_period(accounting_period, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_accounting_year ON fin_accounting_period(accounting_year, tenant_id) WHERE is_deleted = FALSE;

-- 5. 插入系统默认科目数据 (示例)
-- ==============================================================
INSERT INTO fin_account (tenant_id, account_code, account_name, account_type, account_direction, parent_id, account_level, is_leaf, status, sort_order)
VALUES
(0, '1', '资产', 1, 1, NULL, 1, FALSE, 1, 1),
(0, '1001', '库存现金', 1, 1, 1, 2, TRUE, 1, 1),
(0, '1002', '银行存款', 1, 1, 1, 2, FALSE, 1, 2),
(0, '1002001', '工商银行', 1, 1, 4, 3, TRUE, 1, 1),
(0, '1002002', '建设银行', 1, 1, 4, 3, TRUE, 1, 2),
(0, '1121', '应收账款', 1, 1, 1, 2, TRUE, 1, 3),
(0, '2', '负债', 2, 2, NULL, 1, FALSE, 1, 2),
(0, '2202', '应付账款', 2, 2, 7, 2, TRUE, 1, 1),
(0, '3', '所有者权益', 3, 2, NULL, 1, FALSE, 1, 3),
(0, '4001', '实收资本', 3, 2, 9, 2, TRUE, 1, 1),
(0, '4103', '本年利润', 3, 2, 9, 2, TRUE, 1, 2),
(0, '4', '成本', 4, 1, NULL, 1, FALSE, 1, 4),
(0, '5001', '生产成本', 4, 1, 12, 2, TRUE, 1, 1),
(0, '5101', '制造费用', 4, 1, 12, 2, TRUE, 1, 2),
(0, '5', '损益', 5, 2, NULL, 1, FALSE, 1, 5),
(0, '6001', '主营业务收入', 5, 2, 15, 2, TRUE, 1, 1),
(0, '6401', '主营业务成本', 5, 1, 15, 2, TRUE, 1, 2);
