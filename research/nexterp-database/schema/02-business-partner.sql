-- ============================================================================
-- NextERP Business Partner Schema
-- 业务伙伴 (客户/供应商/员工) - 借鉴 SAP S/4HANA BP 模型
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 业务伙伴主表 (参考 SAP BUT000)
-- ----------------------------------------------------------------------------

-- 业务伙伴
CREATE TABLE bp_partner (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    -- 编码
    partner_number  VARCHAR(10),                  -- 业务伙伴编号 (可空，自动生成)
    external_id     VARCHAR(50),                  -- 外部 ID

    -- 类别 (1=组织, 2=个人, 3=组)
    partner_type    VARCHAR(2) NOT NULL CHECK (partner_type IN ('1', '2', '3')),

    -- 个人信息 (partner_type = '2')
    title           VARCHAR(20),                  -- 称谓
    first_name      VARCHAR(40),
    last_name       VARCHAR(40),
    full_name       VARCHAR(80) GENERATED ALWAYS AS (
        COALESCE(NULLIF(first_name, '') || ' ', '') ||
        COALESCE(last_name, '')
    ) STORED,

    -- 组织信息 (partner_type = '1')
    organization_name VARCHAR(100),
    organization_name_2 VARCHAR(100),

    -- 通用信息
    search_term     VARCHAR(20),                  -- 搜索词
    language        VARCHAR(5) DEFAULT 'zh-CN',

    -- 法律信息
    legal_entity    VARCHAR(100),                 -- 法人实体
    tax_id          VARCHAR(50),                  -- 税号
    tax_type        VARCHAR(10),                  -- 税号类型
    registration_number VARCHAR(50),              -- 注册号

    -- 行业
    industry        VARCHAR(10),                  -- 行业代码
    industry_sector VARCHAR(10),                  -- 行业部门

    -- 分类
    partner_group   VARCHAR(4),                   -- 业务伙伴分组
    customer_class  VARCHAR(2),                   -- 客户分类
    supplier_group  VARCHAR(4),                   -- 供应商分组

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    central_block   BOOLEAN DEFAULT FALSE,        -- 中央冻结
    posting_block   BOOLEAN DEFAULT FALSE,        -- 过账冻结
    purchasing_block BOOLEAN DEFAULT FALSE,       -- 采购冻结

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, partner_number)
);

CREATE INDEX idx_bp_partner_tenant ON bp_partner (tenant_id);
CREATE INDEX idx_bp_partner_name ON bp_partner (tenant_id, full_name);
CREATE INDEX idx_bp_partner_org_name ON bp_partner (tenant_id, organization_name);
CREATE INDEX idx_bp_partner_tax_id ON bp_partner (tenant_id, tax_id);
CREATE INDEX idx_bp_partner_search ON bp_partner (tenant_id, search_term);
CREATE INDEX idx_bp_partner_valid ON bp_partner (tenant_id, valid_from, valid_to);

COMMENT ON TABLE bp_partner IS '业务伙伴主表 (参考 SAP BUT000)';
COMMENT ON COLUMN bp_partner.partner_type IS '1=组织, 2=个人, 3=组';

-- 业务伙伴角色 (参考 SAP BUT100)
CREATE TABLE bp_partner_role (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    partner_id      UUID NOT NULL REFERENCES bp_partner(id) ON DELETE CASCADE,

    -- 角色类型
    role_type       VARCHAR(6) NOT NULL,          -- FLCU00=客户, FLVN00=供应商, BUR011=员工
    role_name       VARCHAR(50),

    -- 角色分类
    category        VARCHAR(2),                   -- 角色分类

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    is_primary      BOOLEAN DEFAULT FALSE,        -- 主要角色

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (partner_id, role_type, valid_from)
);

CREATE INDEX idx_bp_partner_role_partner ON bp_partner_role (partner_id);
CREATE INDEX idx_bp_partner_role_type ON bp_partner_role (tenant_id, role_type);

COMMENT ON TABLE bp_partner_role IS '业务伙伴角色表 (参考 SAP BUT100)';
COMMENT ON COLUMN bp_partner_role.role_type IS 'FLCU00=客户, FLVN00=供应商, BUR011=员工等';

-- ----------------------------------------------------------------------------
-- 地址信息 (参考 SAP BUT020)
-- ----------------------------------------------------------------------------

-- 业务伙伴地址
CREATE TABLE bp_address (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    partner_id      UUID NOT NULL REFERENCES bp_partner(id) ON DELETE CASCADE,

    -- 地址类型
    address_type    VARCHAR(2) DEFAULT 'XX',      -- XX=默认, RE=开票, SH=发货

    -- 地址信息
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city_id         UUID REFERENCES core_city(id),

    street          VARCHAR(60),
    street_2        VARCHAR(60),
    postal_code     VARCHAR(20),
    city            VARCHAR(40),
    district        VARCHAR(40),

    -- PO Box
    po_box          VARCHAR(20),
    po_box_city     VARCHAR(40),
    po_box_postal   VARCHAR(20),

    -- 联系信息
    phone           VARCHAR(50),
    phone_2         VARCHAR(50),
    mobile          VARCHAR(50),
    fax             VARCHAR(50),
    email           VARCHAR(100),
    website         VARCHAR(200),

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 标识
    is_default      BOOLEAN DEFAULT FALSE,
    is_billing      BOOLEAN DEFAULT FALSE,
    is_shipping     BOOLEAN DEFAULT FALSE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (partner_id, address_type, valid_from)
);

CREATE INDEX idx_bp_address_partner ON bp_address (partner_id);
CREATE INDEX idx_bp_address_type ON bp_address (tenant_id, address_type);

COMMENT ON TABLE bp_address IS '业务伙伴地址表 (参考 SAP BUT020)';

-- ----------------------------------------------------------------------------
-- 银行信息 (参考 SAP BUT0BK)
-- ----------------------------------------------------------------------------

-- 业务伙伴银行账户
CREATE TABLE bp_bank_account (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    partner_id      UUID NOT NULL REFERENCES bp_partner(id) ON DELETE CASCADE,

    -- 银行信息
    bank_country    VARCHAR(3) NOT NULL,          -- 银行所在国家
    bank_key        VARCHAR(15),                  -- 银行代码
    bank_name       VARCHAR(100),                 -- 银行名称
    bank_branch     VARCHAR(100),                 -- 分行名称
    swift_code      VARCHAR(11),                  -- SWIFT 代码

    -- 账户信息
    account_number  VARCHAR(30) NOT NULL,         -- 账号
    account_holder  VARCHAR(100),                 -- 户名
    account_type    VARCHAR(2),                   -- 账户类型
    currency_id     UUID REFERENCES core_currency(id),

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 标识
    is_default      BOOLEAN DEFAULT FALSE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (partner_id, account_number, valid_from)
);

CREATE INDEX idx_bp_bank_account_partner ON bp_bank_account (partner_id);

COMMENT ON TABLE bp_bank_account IS '业务伙伴银行账户表 (参考 SAP BUT0BK)';

-- ----------------------------------------------------------------------------
-- 客户扩展数据 (参考 SAP KNB1, KNVV)
-- ----------------------------------------------------------------------------

-- 客户公司代码数据 (参考 SAP KNB1)
CREATE TABLE bp_customer_company (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    partner_id      UUID NOT NULL REFERENCES bp_partner(id) ON DELETE CASCADE,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 会计信息
    reconc_account   VARCHAR(10),                 -- 统驭科目
    payment_terms   VARCHAR(4),                   -- 付款条件
    payment_method   VARCHAR(2),                  -- 付款方式

    -- 信贷管理
    credit_limit    DECIMAL(15,2),                -- 信用额度
    credit_currency UUID REFERENCES core_currency(id),

    -- 税务
    tax_type        VARCHAR(2),                   -- 税类型
    tax_number      VARCHAR(20),                  -- 税号

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    company_block   BOOLEAN DEFAULT FALSE,        -- 公司代码冻结

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (partner_id, company_id, valid_from)
);

CREATE INDEX idx_bp_customer_company_partner ON bp_customer_company (partner_id);
CREATE INDEX idx_bp_customer_company_company ON bp_customer_company (company_id);

COMMENT ON TABLE bp_customer_company IS '客户公司代码数据 (参考 SAP KNB1)';

-- 客户销售范围数据 (参考 SAP KNVV)
CREATE TABLE bp_customer_sales (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    partner_id      UUID NOT NULL REFERENCES bp_partner(id) ON DELETE CASCADE,
    sales_area_id   UUID NOT NULL REFERENCES sys_sales_area(id),

    -- 销售信息
    customer_group  VARCHAR(2),                   -- 客户组
    sales_district  VARCHAR(6),                   -- 销售区域
    sales_office    VARCHAR(4),                   -- 销售办事处
    sales_group     VARCHAR(3),                   -- 销售组

    -- 定价
    price_group     VARCHAR(2),                   -- 价格组
    price_list      VARCHAR(2),                   -- 价格清单
    customer_pricing_group VARCHAR(2),            -- 客户定价组

    -- 付款
    payment_terms   VARCHAR(4),
    payment_method  VARCHAR(2),

    -- 交货
    shipping_conditions VARCHAR(2),               -- 装运条件
    delivery_priority VARCHAR(2),                 -- 交货优先级
    delivering_plant_id UUID REFERENCES sys_plant(id),

    -- 开票
    billing_currency_id UUID REFERENCES core_currency(id),
    incoterms       VARCHAR(3),                   -- 国际贸易条款
    incoterms_2     VARCHAR(28),                  -- 国际贸易条款 2

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    sales_block     BOOLEAN DEFAULT FALSE,        -- 销售冻结
    delivery_block  BOOLEAN DEFAULT FALSE,        -- 交货冻结
    billing_block   BOOLEAN DEFAULT FALSE,        -- 开票冻结

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (partner_id, sales_area_id, valid_from)
);

CREATE INDEX idx_bp_customer_sales_partner ON bp_customer_sales (partner_id);
CREATE INDEX idx_bp_customer_sales_area ON bp_customer_sales (sales_area_id);

COMMENT ON TABLE bp_customer_sales IS '客户销售范围数据 (参考 SAP KNVV)';

-- ----------------------------------------------------------------------------
-- 供应商扩展数据 (参考 SAP LFB1, LFM1)
-- ----------------------------------------------------------------------------

-- 供应商公司代码数据 (参考 SAP LFB1)
CREATE TABLE bp_supplier_company (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    partner_id      UUID NOT NULL REFERENCES bp_partner(id) ON DELETE CASCADE,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 会计信息
    reconc_account   VARCHAR(10),                 -- 统驭科目
    payment_terms   VARCHAR(4),
    payment_method   VARCHAR(2),

    -- 税务
    tax_type        VARCHAR(2),
    tax_number      VARCHAR(20),

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    company_block   BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (partner_id, company_id, valid_from)
);

CREATE INDEX idx_bp_supplier_company_partner ON bp_supplier_company (partner_id);

COMMENT ON TABLE bp_supplier_company IS '供应商公司代码数据 (参考 SAP LFB1)';

-- 供应商采购组织数据 (参考 SAP LFM1)
CREATE TABLE bp_supplier_purchasing (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    partner_id      UUID NOT NULL REFERENCES bp_partner(id) ON DELETE CASCADE,
    purchasing_org_id UUID NOT NULL REFERENCES sys_purchasing_organization(id),

    -- 采购信息
    supplier_group  VARCHAR(4),                   -- 供应商分组
    purchasing_group_id UUID REFERENCES sys_purchasing_group(id),

    -- 订单货币
    currency_id     UUID REFERENCES core_currency(id),
    payment_terms   VARCHAR(4),

    -- 评估
    quality_score   DECIMAL(3,1),                 -- 质量评分
    delivery_score  DECIMAL(3,1),                 -- 交货评分
    price_score     DECIMAL(3,1),                 -- 价格评分

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    purchasing_block BOOLEAN DEFAULT FALSE,       -- 采购冻结

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (partner_id, purchasing_org_id, valid_from)
);

CREATE INDEX idx_bp_supplier_purchasing_partner ON bp_supplier_purchasing (partner_id);

COMMENT ON TABLE bp_supplier_purchasing IS '供应商采购组织数据 (参考 SAP LFM1)';

-- ----------------------------------------------------------------------------
-- 联系人
-- ----------------------------------------------------------------------------

-- 业务伙伴联系人
CREATE TABLE bp_contact_person (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    partner_id      UUID NOT NULL REFERENCES bp_partner(id) ON DELETE CASCADE,

    -- 个人信息
    title           VARCHAR(20),
    first_name      VARCHAR(40),
    last_name       VARCHAR(40),
    full_name       VARCHAR(80) GENERATED ALWAYS AS (
        COALESCE(NULLIF(first_name, '') || ' ', '') ||
        COALESCE(last_name, '')
    ) STORED,

    -- 职位
    department      VARCHAR(40),
    position        VARCHAR(40),

    -- 联系方式
    phone           VARCHAR(50),
    mobile          VARCHAR(50),
    fax             VARCHAR(50),
    email           VARCHAR(100),

    -- 备注
    notes           TEXT,

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 标识
    is_primary      BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bp_contact_person_partner ON bp_contact_person (partner_id);

COMMENT ON TABLE bp_contact_person IS '业务伙伴联系人表';

-- ----------------------------------------------------------------------------
-- 触发器
-- ----------------------------------------------------------------------------

CREATE TRIGGER trigger_bp_partner_updated_at
    BEFORE UPDATE ON bp_partner
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_bp_address_updated_at
    BEFORE UPDATE ON bp_address
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 自动生成业务伙伴编号
CREATE OR REPLACE FUNCTION generate_partner_number()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.partner_number IS NULL THEN
        NEW.partner_number := LPAD(
            nextval('seq_partner_number_' || NEW.tenant_id::TEXT)::TEXT,
            10, '0'
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为每个租户创建序列
-- CREATE SEQUENCE seq_partner_number_<tenant_id> START 1;
