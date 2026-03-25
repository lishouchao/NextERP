-- ============================================================================
-- NextERP 优化版 HR Schema
-- 优化点：工资项拆分、时间有效性约束、索引优化、信息类型视图
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 工资类型定义表
-- ----------------------------------------------------------------------------

CREATE TABLE hr_wage_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    code            VARCHAR(4) NOT NULL,          -- 工资类型代码
    name            VARCHAR(100) NOT NULL,        -- 名称
    name_en         VARCHAR(100),

    -- 类别
    category        VARCHAR(2) CHECK (category IN (
        'BA', -- 基本工资
        'AL', -- 津贴
        'BO', -- 奖金
        'DE', -- 扣款
        'OT'  -- 其他
    )),

    -- 税务处理
    is_taxable      BOOLEAN DEFAULT TRUE,
    tax_priority    INTEGER DEFAULT 0,            -- 计税优先级

    -- 社保处理
    is_pension_base BOOLEAN DEFAULT FALSE,        -- 是否计入养老基数
    is_medical_base BOOLEAN DEFAULT FALSE,        -- 是否计入医疗基数

    -- 特性
    is_recurring    BOOLEAN DEFAULT TRUE,         -- 是否经常性
    is_fixed        BOOLEAN DEFAULT FALSE,        -- 是否固定金额

    -- 排序
    sort_order      INTEGER DEFAULT 0,

    -- 状态
    status          VARCHAR(20) DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

-- 预置常用工资类型
INSERT INTO hr_wage_type (tenant_id, code, name, category, is_taxable, is_pension_base, is_medical_base, is_recurring, is_fixed, sort_order) VALUES
-- 基本工资
('00000000-0000-0000-0000-000000000000', '1000', '基本工资', 'BA', TRUE, TRUE, TRUE, TRUE, TRUE, 10),
('00000000-0000-0000-0000-000000000000', '1001', '岗位工资', 'BA', TRUE, TRUE, TRUE, TRUE, TRUE, 11),
('00000000-0000-0000-0000-000000000000', '1002', '技能工资', 'BA', TRUE, TRUE, TRUE, TRUE, TRUE, 12),

-- 津贴
('00000000-0000-0000-0000-000000000000', '2000', '交通补贴', 'AL', TRUE, FALSE, FALSE, TRUE, FALSE, 20),
('00000000-0000-0000-0000-000000000000', '2001', '通讯补贴', 'AL', TRUE, FALSE, FALSE, TRUE, FALSE, 21),
('00000000-0000-0000-0000-000000000000', '2002', '餐饮补贴', 'AL', TRUE, FALSE, FALSE, TRUE, FALSE, 22),
('00000000-0000-0000-0000-000000000000', '2003', '住房补贴', 'AL', TRUE, TRUE, TRUE, TRUE, FALSE, 23),
('00000000-0000-0000-0000-000000000000', '2004', '高温补贴', 'AL', FALSE, FALSE, FALSE, TRUE, FALSE, 24),

-- 奖金
('00000000-0000-0000-0000-000000000000', '3000', '绩效奖金', 'BO', TRUE, TRUE, TRUE, FALSE, FALSE, 30),
('00000000-0000-0000-0000-000000000000', '3001', '年终奖', 'BO', TRUE, FALSE, FALSE, FALSE, FALSE, 31),
('00000000-0000-0000-0000-000000000000', '3002', '项目奖金', 'BO', TRUE, FALSE, FALSE, FALSE, FALSE, 32),

-- 扣款
('00000000-0000-0000-0000-000000000000', '4000', '养老保险(个人)', 'DE', FALSE, FALSE, FALSE, TRUE, FALSE, 40),
('00000000-0000-0000-0000-000000000000', '4001', '医疗保险(个人)', 'DE', FALSE, FALSE, FALSE, TRUE, FALSE, 41),
('00000000-0000-0000-0000-000000000000', '4002', '失业保险(个人)', 'DE', FALSE, FALSE, FALSE, TRUE, FALSE, 42),
('00000000-0000-0000-0000-000000000000', '4003', '住房公积金(个人)', 'DE', FALSE, FALSE, FALSE, TRUE, FALSE, 43),
('00000000-0000-0000-0000-000000000000', '4004', '个人所得税', 'DE', FALSE, FALSE, FALSE, TRUE, FALSE, 44),
('00000000-0000-0000-0000-000000000000', '4005', '事假扣款', 'DE', FALSE, FALSE, FALSE, FALSE, FALSE, 45),
('00000000-0000-0000-0000-000000000000', '4006', '病假扣款', 'DE', FALSE, FALSE, FALSE, FALSE, FALSE, 46);

COMMENT ON TABLE hr_wage_type IS '工资类型定义表';

-- ----------------------------------------------------------------------------
-- 2. IT0008 基本工资 - 主表（不含工资项）
-- ----------------------------------------------------------------------------

CREATE TABLE hr_it0008_basic_pay (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id     UUID NOT NULL,
    tenant_id       UUID NOT NULL,

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 薪酬类型
    pay_type        VARCHAR(2),
    pay_area        VARCHAR(2),

    -- 薪酬等级
    pay_grade       VARCHAR(4),
    pay_level       VARCHAR(2),

    -- 货币
    currency_id     UUID,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,
    is_deleted      BOOLEAN DEFAULT FALSE,

    UNIQUE (employee_id, valid_from)
);

-- 工资项明细表
CREATE TABLE hr_it0008_wage_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    basic_pay_id    UUID NOT NULL REFERENCES hr_it0008_basic_pay(id) ON DELETE CASCADE,

    wage_type_id    UUID NOT NULL REFERENCES hr_wage_type(id),
    amount          DECIMAL(15,2) NOT NULL,
    currency_id     UUID,

    -- 数量（用于计件工资）
    quantity        DECIMAL(10,2) DEFAULT 1,
    unit_price      DECIMAL(15,2),

    -- 备注
    remark          VARCHAR(200),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (basic_pay_id, wage_type_id)
);

-- 索引
CREATE INDEX idx_hr_it0008_employee ON hr_it0008_basic_pay (employee_id);
CREATE INDEX idx_hr_it0008_valid ON hr_it0008_basic_pay (employee_id, valid_from, valid_to);
CREATE INDEX idx_hr_it0008_wage_item_basic ON hr_it0008_wage_item (basic_pay_id);

-- 工资项汇总视图
CREATE VIEW v_hr_it0008_with_items AS
SELECT
    bp.id,
    bp.employee_id,
    bp.tenant_id,
    bp.valid_from,
    bp.valid_to,
    bp.pay_type,
    bp.pay_area,
    bp.pay_grade,
    bp.pay_level,
    bp.currency_id,

    -- 汇总金额
    COALESCE(SUM(CASE WHEN wt.category IN ('BA', 'AL', 'BO') THEN wi.amount ELSE 0 END), 0) AS gross_amount,
    COALESCE(SUM(CASE WHEN wt.category = 'DE' THEN wi.amount ELSE 0 END), 0) AS deduction_amount,
    COALESCE(SUM(CASE WHEN wt.category IN ('BA', 'AL', 'BO') THEN wi.amount ELSE -wi.amount END), 0) AS net_amount,

    -- 工资项 JSON（便于前端使用）
    jsonb_agg(jsonb_build_object(
        'code', wt.code,
        'name', wt.name,
        'category', wt.category,
        'amount', wi.amount,
        'is_taxable', wt.is_taxable
    ) ORDER BY wt.sort_order) AS wage_items

FROM hr_it0008_basic_pay bp
LEFT JOIN hr_it0008_wage_item wi ON wi.basic_pay_id = bp.id
LEFT JOIN hr_wage_type wt ON wt.id = wi.wage_type_id
WHERE bp.is_deleted = FALSE
GROUP BY bp.id;

COMMENT ON TABLE hr_it0008_basic_pay IS 'IT0008 基本工资主表（优化版）';
COMMENT ON TABLE hr_it0008_wage_item IS 'IT0008 工资项明细表';
COMMENT ON VIEW v_hr_it0008_with_items IS 'IT0008 工资汇总视图';

-- ----------------------------------------------------------------------------
-- 3. 时间有效性约束（使用排他约束）
-- ----------------------------------------------------------------------------

-- 启用 btree_gist 扩展
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- 为信息类型表添加排他约束
-- IT0001 组织分配
ALTER TABLE hr_it0001_org_assignment
ADD CONSTRAINT uk_hr_it0001_no_overlap
EXCLUDE USING GIST (
    employee_id WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);

-- IT0002 个人数据
ALTER TABLE hr_it0002_personal_data
ADD CONSTRAINT uk_hr_it0002_no_overlap
EXCLUDE USING GIST (
    employee_id WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);

-- IT0008 基本工资
ALTER TABLE hr_it0008_basic_pay
ADD CONSTRAINT uk_hr_it0008_no_overlap
EXCLUDE USING GIST (
    employee_id WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);

-- IT0016 合同
ALTER TABLE hr_it0016_contract
ADD CONSTRAINT uk_hr_it0016_no_overlap
EXCLUDE USING GIST (
    employee_id WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);

-- ----------------------------------------------------------------------------
-- 4. 员工信息综合视图（优化 N+1 查询）
-- ----------------------------------------------------------------------------

CREATE VIEW v_hr_employee_full AS
SELECT
    e.id,
    e.tenant_id,
    e.employee_number,
    e.employee_status,
    e.hire_date,
    e.email,
    e.phone,

    -- IT0002 个人数据（当前有效）
    it2.first_name,
    it2.last_name,
    it2.full_name,
    it2.gender,
    it2.birth_date,
    it2.id_number,
    it2.nationality,
    it2.marital_status,

    -- IT0001 组织分配（当前有效）
    it1.org_unit_id,
    it1.org_unit_name,
    it1.position_id,
    it1.position_name,
    it1.job_id,
    it1.job_name,
    it1.cost_center_id,
    it1.cost_center_name,
    it1.manager_id,
    it1.manager_name,

    -- IT0008 薪资（当前有效）
    it8.currency_id AS salary_currency,
    it8.pay_grade,
    it8.pay_level,
    it8.gross_amount AS current_gross_salary,
    it8.net_amount AS current_net_salary

FROM hr_employee e

-- 关联 IT0002
LEFT JOIN LATERAL (
    SELECT * FROM hr_it0002_personal_data
    WHERE employee_id = e.id
      AND valid_from <= CURRENT_DATE
      AND valid_to >= CURRENT_DATE
    LIMIT 1
) it2 ON TRUE

-- 关联 IT0001
LEFT JOIN LATERAL (
    SELECT * FROM hr_it0001_org_assignment
    WHERE employee_id = e.id
      AND valid_from <= CURRENT_DATE
      AND valid_to >= CURRENT_DATE
    LIMIT 1
) it1 ON TRUE

-- 关联 IT0008（通过视图）
LEFT JOIN LATERAL (
    SELECT * FROM v_hr_it0008_with_items
    WHERE employee_id = e.id
      AND valid_from <= CURRENT_DATE
      AND valid_to >= CURRENT_DATE
    LIMIT 1
) it8 ON TRUE

WHERE e.status = 'ACTIVE';

COMMENT ON VIEW v_hr_employee_full IS '员工信息综合视图（优化 N+1 查询）';

-- ----------------------------------------------------------------------------
-- 5. 薪酬结果优化（分区）
-- ----------------------------------------------------------------------------

CREATE TABLE hr_payroll_result (
    id              UUID NOT NULL,
    tenant_id       UUID NOT NULL,
    employee_id     UUID NOT NULL,

    -- 期间（分区键）
    payroll_year    INTEGER NOT NULL,
    payroll_month   INTEGER NOT NULL,
    payroll_period  VARCHAR(7) NOT NULL,          -- YYYYMM

    -- 汇总金额
    gross_pay       DECIMAL(15,2) DEFAULT 0,
    total_deduction DECIMAL(15,2) DEFAULT 0,
    net_pay         DECIMAL(15,2) DEFAULT 0,

    -- 货币
    currency_id     UUID,

    -- 状态
    status          VARCHAR(10) DEFAULT 'DRAFT',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    version         INTEGER DEFAULT 0,

    PRIMARY KEY (payroll_year, id)
) PARTITION BY RANGE (payroll_year);

-- 创建分区
CREATE TABLE hr_payroll_result_2024
    PARTITION OF hr_payroll_result
    FOR VALUES FROM (2024) TO (2025);

CREATE TABLE hr_payroll_result_2025
    PARTITION OF hr_payroll_result
    FOR VALUES FROM (2025) TO (2026);

CREATE TABLE hr_payroll_result_default
    PARTITION OF hr_payroll_result DEFAULT;

-- 薪酬项明细表
CREATE TABLE hr_payroll_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payroll_year    INTEGER NOT NULL,
    payroll_result_id UUID NOT NULL,

    wage_type_id    UUID NOT NULL REFERENCES hr_wage_type(id),
    amount          DECIMAL(15,2) NOT NULL,

    -- 计算基础
    base_amount     DECIMAL(15,2),                -- 基数
    rate            DECIMAL(10,4),                -- 比率
    hours           DECIMAL(10,2),                -- 小时数

    -- 分类
    item_type       VARCHAR(2),                   -- 类型标识

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_hr_payroll_item_result ON hr_payroll_item (payroll_result_id);
CREATE INDEX idx_hr_payroll_item_type ON hr_payroll_item (wage_type_id);

COMMENT ON TABLE hr_payroll_result IS '薪酬结果（按年度分区）';
COMMENT ON TABLE hr_payroll_item IS '薪酬项明细表';

-- ----------------------------------------------------------------------------
-- 6. 添加索引
-- ----------------------------------------------------------------------------

-- 组织单位
CREATE INDEX idx_hr_org_unit_valid ON hr_org_unit (tenant_id, valid_from, valid_to);
CREATE INDEX idx_hr_org_unit_parent ON hr_org_unit (parent_id);

-- 职务
CREATE INDEX idx_hr_job_valid ON hr_job (tenant_id, valid_from, valid_to);

-- 职位
CREATE INDEX idx_hr_position_valid ON hr_position (tenant_id, valid_from, valid_to);
CREATE INDEX idx_hr_position_holder ON hr_position (holder_id);
CREATE INDEX idx_hr_position_org_unit ON hr_position (org_unit_id);

-- 员工
CREATE INDEX idx_hr_employee_tenant ON hr_employee (tenant_id);
CREATE INDEX idx_hr_employee_org_unit ON hr_employee (org_unit_id);
CREATE INDEX idx_hr_employee_status ON hr_employee (tenant_id, employee_status);

-- 信息类型
CREATE INDEX idx_hr_it0001_valid ON hr_it0001_org_assignment (employee_id, valid_from, valid_to);
CREATE INDEX idx_hr_it0002_valid ON hr_it0002_personal_data (employee_id, valid_from, valid_to);
CREATE INDEX idx_hr_it0006_valid ON hr_it0006_address (employee_id, valid_from, valid_to);
CREATE INDEX idx_hr_it0009_valid ON hr_it0009_bank_details (employee_id, valid_from, valid_to);
CREATE INDEX idx_hr_it0016_valid ON hr_it0016_contract (employee_id, valid_from, valid_to);
CREATE INDEX idx_hr_it0021_valid ON hr_it0021_family (employee_id, valid_from, valid_to);
CREATE INDEX idx_hr_it0022_valid ON hr_it0022_education (employee_id, valid_from, valid_to);

-- 请假
CREATE INDEX idx_hr_it2001_valid ON hr_it2001_absence (employee_id, valid_from, valid_to);
CREATE INDEX idx_hr_leave_quota_employee ON hr_leave_quota (employee_id, leave_type_id, quota_year);

-- ----------------------------------------------------------------------------
-- 7. 添加审计触发器
-- ----------------------------------------------------------------------------

PERFORM add_audit_trigger('hr_employee');
PERFORM add_audit_trigger('hr_org_unit');
PERFORM add_audit_trigger('hr_position');
PERFORM add_audit_trigger('hr_it0008_basic_pay');
PERFORM add_audit_trigger('hr_payroll_result');
