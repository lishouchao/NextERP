-- ============================================================================
-- NextERP HR Schema
-- 人力资源 - 借鉴 SAP ECC 信息类型架构
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 信息类型基础表 (使用 PostgreSQL 表继承)
-- ----------------------------------------------------------------------------

-- 信息类型基础表 (所有 IT 表继承此表)
CREATE TABLE hr_infotype_base (
    -- 员工标识
    employee_id     UUID NOT NULL,

    -- 信息类型
    infotype        VARCHAR(4) NOT NULL,
    subtype         VARCHAR(4) DEFAULT '',
    object_id       VARCHAR(2) DEFAULT '',
    lock_indicator  BOOLEAN DEFAULT FALSE,

    -- 时间有效性 (SAP 风格)
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 序号
    sequence_number INTEGER DEFAULT 0,

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,

    PRIMARY KEY (employee_id, infotype, subtype, valid_from)
);

COMMENT ON TABLE hr_infotype_base IS '信息类型基础表 - 所有 IT 表继承此表';

-- ----------------------------------------------------------------------------
-- 组织管理 (OM) - 参考 SAP HRP1000, HRP1001
-- ----------------------------------------------------------------------------

-- 组织单位 (参考 SAP HRP1000, OTYPE='O')
CREATE TABLE hr_org_unit (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 编码
    org_code        VARCHAR(12) NOT NULL,

    -- 基本信息
    name            VARCHAR(100) NOT NULL,
    short_name      VARCHAR(20),

    -- 组织类型
    org_type        VARCHAR(2) CHECK (org_type IN (
        'CO', -- 公司
        'BR', -- 分支机构
        'DE', -- 部门
        'CE', -- 中心
        'TE', -- 团队
        'PJ'  -- 项目
    )),

    -- 层级
    parent_id       UUID REFERENCES hr_org_unit(id),
    level           INTEGER DEFAULT 1,

    -- 负责人
    manager_id      UUID,                         -- 员工 ID
    manager_position_id UUID,

    -- 成本中心
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    profit_center_id UUID REFERENCES sys_profit_center(id),

    -- 公司
    company_id      UUID REFERENCES sys_company(id),

    -- 地点
    location        VARCHAR(100),

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    -- 人数
    headcount       INTEGER DEFAULT 0,
    max_headcount   INTEGER,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, org_code, valid_from)
);

CREATE INDEX idx_hr_org_unit_parent ON hr_org_unit (parent_id);
CREATE INDEX idx_hr_org_unit_company ON hr_org_unit (company_id);

COMMENT ON TABLE hr_org_unit IS '组织单位 (参考 SAP HRP1000, OTYPE=O)';

-- 职务 (参考 SAP HRP1000, OTYPE='C')
CREATE TABLE hr_job (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 编码
    job_code        VARCHAR(8) NOT NULL,

    -- 名称
    name            VARCHAR(100) NOT NULL,
    short_name      VARCHAR(20),

    -- 职务分类
    job_category    VARCHAR(1) CHECK (job_category IN (
        'M', -- 管理
        'P', -- 专业
        'S', -- 支持
        'O'  -- 操作
    )),

    -- 职级
    job_grade       VARCHAR(4),
    job_level       INTEGER,

    -- 描述
    description     TEXT,
    requirements    TEXT,
    responsibilities TEXT,

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, job_code, valid_from)
);

COMMENT ON TABLE hr_job IS '职务 (参考 SAP HRP1000, OTYPE=C)';

-- 职位 (参考 SAP HRP1000, OTYPE='S')
CREATE TABLE hr_position (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 编码
    position_code   VARCHAR(8) NOT NULL,

    -- 名称
    name            VARCHAR(100) NOT NULL,

    -- 关联
    job_id          UUID NOT NULL REFERENCES hr_job(id),
    org_unit_id     UUID NOT NULL REFERENCES hr_org_unit(id),

    -- 任职者
    holder_id       UUID,                         -- 当前任职员工 ID
    holder_name     VARCHAR(80),

    -- 工作中心
    work_center_id  UUID,

    -- 成本中心
    cost_center_id  UUID REFERENCES sys_cost_center(id),

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    position_status VARCHAR(2) CHECK (position_status IN (
        'VA', -- 空缺
        'FI', -- 已填充
        'FR', -- 冻结
        'AB'  -- 废除
    )) DEFAULT 'VA',

    headcount       INTEGER DEFAULT 1,
    current_count   INTEGER DEFAULT 0,

    -- 描述
    description     TEXT,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, position_code, valid_from)
);

CREATE INDEX idx_hr_position_job ON hr_position (job_id);
CREATE INDEX idx_hr_position_org_unit ON hr_position (org_unit_id);
CREATE INDEX idx_hr_position_holder ON hr_position (holder_id);

COMMENT ON TABLE hr_position IS '职位 (参考 SAP HRP1000, OTYPE=S)';

-- ----------------------------------------------------------------------------
-- 员工主数据 (PA) - 参考 SAP PA0000-PA9999
-- ----------------------------------------------------------------------------

-- 员工主数据
CREATE TABLE hr_employee (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 员工号
    employee_number VARCHAR(8) NOT NULL,

    -- 状态
    employee_status VARCHAR(2) CHECK (employee_status IN (
        'AC', -- 在职
        'IN', -- 休假
        'TE', -- 离职
        'RE'  -- 退休
    )) DEFAULT 'AC',

    -- 基本信息 (冗余存储，来自 IT0002)
    full_name       VARCHAR(80),
    gender          gender,
    birth_date      DATE,
    id_number       VARCHAR(20),

    -- 组织信息 (冗余存储，来自 IT0001)
    org_unit_id     UUID REFERENCES hr_org_unit(id),
    position_id     UUID REFERENCES hr_position(id),
    job_id          UUID REFERENCES hr_job(id),

    -- 联系方式
    email           VARCHAR(100),
    phone           VARCHAR(50),
    mobile          VARCHAR(50),

    -- 入职日期
    hire_date       DATE NOT NULL,

    -- 司龄
    seniority       DECIMAL(5,1),                 -- 年

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, employee_number)
);

CREATE INDEX idx_hr_employee_tenant ON hr_employee (tenant_id);
CREATE INDEX idx_hr_employee_org_unit ON hr_employee (org_unit_id);
CREATE INDEX idx_hr_employee_position ON hr_employee (position_id);

COMMENT ON TABLE hr_employee IS '员工主数据';

-- IT0001 组织分配 (参考 SAP PA0001)
CREATE TABLE hr_it0001_org_assignment (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    -- 信息类型字段
    infotype        VARCHAR(4) DEFAULT '0001',
    subtype         VARCHAR(4) DEFAULT '',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 组织信息
    org_unit_id     UUID REFERENCES hr_org_unit(id),
    org_unit_name   VARCHAR(100),
    position_id     UUID REFERENCES hr_position(id),
    position_name   VARCHAR(100),
    job_id          UUID REFERENCES hr_job(id),
    job_name        VARCHAR(100),

    -- 成本中心
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    cost_center_name VARCHAR(100),

    -- 上级
    manager_id      UUID REFERENCES hr_employee(id),
    manager_name    VARCHAR(80),

    -- 员工分类
    employee_group  VARCHAR(1),                   -- 员工组
    employee_subgroup VARCHAR(2),                 -- 员工子组

    -- 公司/人事范围
    company_id      UUID REFERENCES sys_company(id),
    personnel_area  VARCHAR(4),
    personnel_subarea VARCHAR(4),

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, valid_from)
);

COMMENT ON TABLE hr_it0001_org_assignment IS 'IT0001 组织分配 (参考 SAP PA0001)';

-- IT0002 个人数据 (参考 SAP PA0002)
CREATE TABLE hr_it0002_personal_data (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    -- 信息类型字段
    infotype        VARCHAR(4) DEFAULT '0002',
    subtype         VARCHAR(4) DEFAULT '',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 姓名
    last_name       VARCHAR(40) NOT NULL,
    first_name      VARCHAR(40) NOT NULL,
    full_name       VARCHAR(80) GENERATED ALWAYS AS (
        first_name || ' ' || last_name
    ) STORED,

    -- 性别
    gender          gender NOT NULL,

    -- 出生日期
    birth_date      DATE NOT NULL,

    -- 国籍/民族
    nationality     VARCHAR(3),
    ethnicity       VARCHAR(3),

    -- 婚姻状况
    marital_status  VARCHAR(1) CHECK (marital_status IN (
        '1', -- 未婚
        '2', -- 已婚
        '3', -- 丧偶
        '4', -- 离婚
        '5'  -- 其他
    )),

    -- 政治面貌
    political_status VARCHAR(2),

    -- 身份证
    id_type         VARCHAR(4) DEFAULT '01',      -- 证件类型
    id_number       VARCHAR(20) NOT NULL,         -- 身份证号
    id_issue_date   DATE,
    id_issue_place  VARCHAR(100),
    id_expiry_date  DATE,

    -- 籍贯
    native_place    VARCHAR(100),

    -- 户籍
    household_type  VARCHAR(1),                   -- 户口性质
    household_address TEXT,

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, valid_from)
);

COMMENT ON TABLE hr_it0002_personal_data IS 'IT0002 个人数据 (参考 SAP PA0002)';

-- IT0006 地址 (参考 SAP PA0006)
CREATE TABLE hr_it0006_address (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    -- 信息类型字段
    infotype        VARCHAR(4) DEFAULT '0006',
    subtype         VARCHAR(4) DEFAULT '1',       -- 1=永久地址, 2=临时地址
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 地址
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city_id         UUID REFERENCES core_city(id),
    street          VARCHAR(60),
    postal_code     VARCHAR(10),

    -- 联系方式
    phone           VARCHAR(50),
    mobile          VARCHAR(50),

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, subtype, valid_from)
);

COMMENT ON TABLE hr_it0006_address IS 'IT0006 地址 (参考 SAP PA0006)';

-- IT0008 基本工资 (参考 SAP PA0008)
CREATE TABLE hr_it0008_basic_pay (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    -- 信息类型字段
    infotype        VARCHAR(4) DEFAULT '0008',
    subtype         VARCHAR(4) DEFAULT '',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 薪酬类型
    pay_type        VARCHAR(2),
    pay_area        VARCHAR(2),

    -- 薪酬等级
    pay_grade       VARCHAR(4),
    pay_level       VARCHAR(2),

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 工资项 (使用 JSONB 存储，支持多个工资项)
    -- 格式: [{"type": "1000", "name": "基本工资", "amount": 10000}, ...]
    wage_items      JSONB NOT NULL DEFAULT '[]'::JSONB,

    -- 总额
    total_amount    DECIMAL(15,2) GENERATED ALWAYS AS (
        (SELECT COALESCE(SUM((item->>'amount')::DECIMAL), 0)
         FROM jsonb_array_elements(wage_items) AS item)
    ) STORED,

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,

    PRIMARY KEY (employee_id, valid_from)
);

COMMENT ON TABLE hr_it0008_basic_pay IS 'IT0008 基本工资 (参考 SAP PA0008)';
COMMENT ON COLUMN hr_it0008_basic_pay.wage_items IS '工资项 JSONB 数组';

-- IT0009 银行信息 (参考 SAP PA0009)
CREATE TABLE hr_it0009_bank_details (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    -- 信息类型字段
    infotype        VARCHAR(4) DEFAULT '0009',
    subtype         VARCHAR(4) DEFAULT '0',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 银行信息
    bank_country    VARCHAR(3) NOT NULL,
    bank_key        VARCHAR(15),                  -- 银行代码
    bank_name       VARCHAR(100),                 -- 银行名称
    bank_branch     VARCHAR(100),                 -- 分行名称

    -- 账户
    account_number  VARCHAR(30) NOT NULL,
    account_holder  VARCHAR(100),                 -- 户名
    account_type    VARCHAR(2),                   -- 账户类型

    -- 标识
    is_primary      BOOLEAN DEFAULT FALSE,

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, subtype, valid_from)
);

COMMENT ON TABLE hr_it0009_bank_details IS 'IT0009 银行信息 (参考 SAP PA0009)';

-- IT0016 合同 (参考 SAP PA0016)
CREATE TABLE hr_it0016_contract (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    -- 信息类型字段
    infotype        VARCHAR(4) DEFAULT '0016',
    subtype         VARCHAR(4) DEFAULT '',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 合同信息
    contract_type   VARCHAR(2),                   -- 合同类型
    contract_number VARCHAR(20),                  -- 合同编号
    contract_start  DATE NOT NULL,                -- 合同开始
    contract_end    DATE,                         -- 合同结束

    -- 试用期
    probation_start DATE,
    probation_end   DATE,

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, valid_from)
);

COMMENT ON TABLE hr_it0016_contract IS 'IT0016 合同 (参考 SAP PA0016)';

-- IT0021 家庭成员 (参考 SAP PA0021)
CREATE TABLE hr_it0021_family (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    -- 信息类型字段
    infotype        VARCHAR(4) DEFAULT '0021',
    subtype         VARCHAR(4) NOT NULL,          -- 01=配偶, 02=子女, 03=父母
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 家庭成员信息
    last_name       VARCHAR(40),
    first_name      VARCHAR(40),
    full_name       VARCHAR(80) GENERATED ALWAYS AS (
        COALESCE(first_name, '') || ' ' || COALESCE(last_name, '')
    ) STORED,
    gender          gender,
    birth_date      DATE,
    id_number       VARCHAR(20),

    -- 联系方式
    phone           VARCHAR(50),

    -- 紧急联系人
    is_emergency    BOOLEAN DEFAULT FALSE,

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, subtype, valid_from)
);

COMMENT ON TABLE hr_it0021_family IS 'IT0021 家庭成员 (参考 SAP PA0021)';

-- IT0022 教育 (参考 SAP PA0022)
CREATE TABLE hr_it0022_education (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    -- 信息类型字段
    infotype        VARCHAR(4) DEFAULT '0022',
    subtype         VARCHAR(4) DEFAULT '',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 教育信息
    education_level VARCHAR(3),                   -- 学历
    degree          VARCHAR(3),                   -- 学位
    school          VARCHAR(100),                 -- 学校
    major           VARCHAR(100),                 -- 专业

    -- 时间
    start_date      DATE,
    end_date        DATE,

    -- 证书
    certificate     VARCHAR(100),

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, valid_from)
);

COMMENT ON TABLE hr_it0022_education IS 'IT0022 教育 (参考 SAP PA0022)';

-- ----------------------------------------------------------------------------
-- 时间管理 (PT) - 参考 SAP PA2001, PA2002
-- ----------------------------------------------------------------------------

-- 假期类型
CREATE TABLE hr_leave_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    code            VARCHAR(4) NOT NULL,
    name            VARCHAR(100) NOT NULL,

    -- 配额
    has_quota       BOOLEAN DEFAULT TRUE,
    default_quota   DECIMAL(5,1),                 -- 默认额度 (天)
    carry_over      BOOLEAN DEFAULT FALSE,        -- 是否结转
    max_carry_over  DECIMAL(5,1),                 -- 最大结转天数

    -- 有效期
    valid_months    INTEGER DEFAULT 12,           -- 有效月数

    -- 工资
    is_paid         BOOLEAN DEFAULT TRUE,

    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE hr_leave_type IS '假期类型';

-- IT2001 缺勤 (参考 SAP PA2001)
CREATE TABLE hr_it2001_absence (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    -- 信息类型字段
    infotype        VARCHAR(4) DEFAULT '2001',
    subtype         VARCHAR(4) NOT NULL,          -- 假期类型
    valid_from      DATE NOT NULL,
    valid_to        DATE NOT NULL,

    -- 假期信息
    leave_type_id   UUID REFERENCES hr_leave_type(id),
    leave_type_name VARCHAR(100),

    -- 时间
    start_time      TIME,
    end_time        TIME,

    -- 天数/小时
    days            DECIMAL(5,1) NOT NULL,
    hours           DECIMAL(5,2),

    -- 原因
    reason          TEXT,

    -- 审批
    approval_status approval_status DEFAULT 'DRAFT',
    approved_by     UUID,
    approved_at     TIMESTAMP,

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    PRIMARY KEY (employee_id, valid_from, subtype)
);

COMMENT ON TABLE hr_it2001_absence IS 'IT2001 缺勤 (参考 SAP PA2001)';

-- 假期余额 (参考 SAP PT_QT)
CREATE TABLE hr_leave_quota (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,
    leave_type_id   UUID NOT NULL REFERENCES hr_leave_type(id),

    -- 年度
    quota_year      INTEGER NOT NULL,

    -- 额度
    opening_balance DECIMAL(5,1) DEFAULT 0,       -- 期初余额
    accrued         DECIMAL(5,1) DEFAULT 0,       -- 本期获得
    used            DECIMAL(5,1) DEFAULT 0,       -- 已使用
    balance         DECIMAL(5,1) DEFAULT 0,       -- 余额

    -- 有效期
    valid_from      DATE NOT NULL,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',
    expire_date     DATE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (employee_id, leave_type_id, quota_year)
);

COMMENT ON TABLE hr_leave_quota IS '假期余额';

-- ----------------------------------------------------------------------------
-- 薪酬管理 (PY)
-- ----------------------------------------------------------------------------

-- 薪酬结果 (参考 SAP HRPY_RT)
CREATE TABLE hr_payroll_result (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    -- 期间
    payroll_period  VARCHAR(7) NOT NULL,          -- YYYYMM
    payroll_year    INTEGER NOT NULL,
    payroll_month   INTEGER NOT NULL,

    -- 薪酬项 (JSONB)
    -- 格式: [{"code": "1000", "name": "基本工资", "amount": 10000}, ...]
    payroll_items   JSONB NOT NULL DEFAULT '[]'::JSONB,

    -- 汇总
    gross_pay       DECIMAL(15,2) DEFAULT 0,      -- 应发合计
    total_deduction DECIMAL(15,2) DEFAULT 0,      -- 扣款合计
    net_pay         DECIMAL(15,2) DEFAULT 0,      -- 实发合计

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 状态
    status          VARCHAR(10) CHECK (status IN ('DRAFT', 'CONFIRMED', 'PAID')) DEFAULT 'DRAFT',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (employee_id, payroll_period)
);

COMMENT ON TABLE hr_payroll_result IS '薪酬结果';

-- 社会保险配置 (中国本地化)
CREATE TABLE hr_social_insurance_config (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 类型
    insurance_type  VARCHAR(4) NOT NULL,          -- 养老/医疗/失业/工伤/生育
    insurance_name  VARCHAR(100) NOT NULL,

    -- 缴费基数
    base_min        DECIMAL(10,2),
    base_max        DECIMAL(10,2),

    -- 比例
    company_rate    DECIMAL(5,4),                 -- 公司比例
    personal_rate   DECIMAL(5,4),                 -- 个人比例

    -- 有效期
    valid_from      DATE NOT NULL,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (company_id, insurance_type, valid_from)
);

COMMENT ON TABLE hr_social_insurance_config IS '社会保险配置 (中国本地化)';

-- IT0591 社会保险 (中国本地化)
CREATE TABLE hr_it0591_social_insurance (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '0591',
    subtype         VARCHAR(4) DEFAULT '',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 缴费基数
    pension_base    DECIMAL(10,2),                -- 养老基数
    medical_base    DECIMAL(10,2),                -- 医疗基数
    unemployment_base DECIMAL(10,2),              -- 失业基数
    injury_base     DECIMAL(10,2),                -- 工伤基数
    maternity_base  DECIMAL(10,2),                -- 生育基数

    -- 个人缴纳
    pension_personal DECIMAL(10,2),
    medical_personal DECIMAL(10,2),
    unemployment_personal DECIMAL(10,2),

    -- 公司缴纳
    pension_company DECIMAL(10,2),
    medical_company DECIMAL(10,2),
    unemployment_company DECIMAL(10,2),
    injury_company  DECIMAL(10,2),
    maternity_company DECIMAL(10,2),

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, valid_from)
);

COMMENT ON TABLE hr_it0591_social_insurance IS 'IT0591 社会保险 (中国本地化)';

-- IT0592 住房公积金 (中国本地化)
CREATE TABLE hr_it0592_housing_fund (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '0592',
    subtype         VARCHAR(4) DEFAULT '',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 缴费基数
    fund_base       DECIMAL(10,2),

    -- 比例
    company_rate    DECIMAL(5,4),
    personal_rate   DECIMAL(5,4),

    -- 金额
    company_amount  DECIMAL(10,2),
    personal_amount DECIMAL(10,2),

    -- 公积金账号
    fund_account    VARCHAR(30),

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, valid_from)
);

COMMENT ON TABLE hr_it0592_housing_fund IS 'IT0592 住房公积金 (中国本地化)';

-- ----------------------------------------------------------------------------
-- 触发器
-- ----------------------------------------------------------------------------

CREATE TRIGGER trigger_hr_employee_updated_at
    BEFORE UPDATE ON hr_employee
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_hr_org_unit_updated_at
    BEFORE UPDATE ON hr_org_unit
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_hr_position_updated_at
    BEFORE UPDATE ON hr_position
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
