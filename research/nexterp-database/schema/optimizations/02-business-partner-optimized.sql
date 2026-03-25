-- ============================================================================
-- NextERP 优化版 Business Partner Schema
-- 优化点：统一审计触发器、排他约束、全文搜索、索引优化
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 业务伙伴主表 (参考 SAP BUT000)
-- ----------------------------------------------------------------------------

-- 业务伙伴
CREATE TABLE bp_partner (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    -- 编码
    partner_number  VARCHAR(10),
    external_id     VARCHAR(50),

    -- 类别 (1=组织, 2=个人, 3=组)
    partner_type    VARCHAR(2) NOT NULL CHECK (partner_type IN ('1', '2', '3')),

    -- 个人信息 (partner_type = '2')
    title           VARCHAR(20),
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
    search_term     VARCHAR(20),
    language        VARCHAR(5) DEFAULT 'zh-CN',

    -- 法律信息
    legal_entity    VARCHAR(100),
    tax_id          VARCHAR(50),
    tax_type        VARCHAR(10),
    registration_number VARCHAR(50),

    -- 行业
    industry        VARCHAR(10),
    industry_sector VARCHAR(10),

    -- 分类
    partner_group   VARCHAR(4),
    customer_class  VARCHAR(2),
    supplier_group  VARCHAR(4),

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    central_block   BOOLEAN DEFAULT FALSE,
    posting_block   BOOLEAN DEFAULT FALSE,
    purchasing_block BOOLEAN DEFAULT FALSE,

    -- 全文搜索向量（优化搜索）
    search_vector   TSVECTOR GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', COALESCE(full_name, '')), 'A') ||
        setweight(to_tsvector('simple', COALESCE(organization_name, '')), 'A') ||
        setweight(to_tsvector('simple', COALESCE(search_term, '')), 'B') ||
        setweight(to_tsvector('simple', COALESCE(partner_number, '')), 'C')
    ) STORED,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, partner_number)
);

-- 全文搜索索引
CREATE INDEX idx_bp_partner_search_vector ON bp_partner USING GIN (search_vector);
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
    role_type       VARCHAR(6) NOT NULL,
    role_name       VARCHAR(50),

    -- 角色分类
    category        VARCHAR(2),

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    is_primary      BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (partner_id, role_type, valid_from)
);

CREATE INDEX idx_bp_partner_role_partner ON bp_partner_role (partner_id);
CREATE INDEX idx_bp_partner_role_type ON bp_partner_role (tenant_id, role_type);

COMMENT ON TABLE bp_partner_role IS '业务伙伴角色表 (参考 SAP BUT100)';

-- ----------------------------------------------------------------------------
-- 2. 地址信息 (参考 SAP BUT020)
-- ----------------------------------------------------------------------------

-- 业务伙伴地址
CREATE TABLE bp_address (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    partner_id      UUID NOT NULL REFERENCES bp_partner(id) ON DELETE CASCADE,

    -- 地址类型
    address_type    VARCHAR(2) DEFAULT 'XX',

    -- 地址信息
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city_id         UUID REFERENCES core_city(id),

    street          VARCHAR(60),
    street_2        VARCHAR(60),
    postal_code     VARCHAR(20),
    city            VARCHAR(40),
    district        VARCHAR(40),

    -- 完整地址（生成列）
    full_address    TEXT GENERATED ALWAYS AS (
        COALESCE(street, '') || ' ' ||
        COALESCE(street_2, '') || ' ' ||
        COALESCE(city, '') || ' ' ||
        COALESCE(district, '') || ' ' ||
        COALESCE(postal_code, '')
    ) STORED,

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
-- 3. 银行信息 (参考 SAP BUT0BK)
-- ----------------------------------------------------------------------------

-- 业务伙伴银行账户
CREATE TABLE bp_bank_account (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    partner_id      UUID NOT NULL REFERENCES bp_partner(id) ON DELETE CASCADE,

    -- 银行信息
    bank_country    VARCHAR(3) NOT NULL,
    bank_key        VARCHAR(15),
    bank_name       VARCHAR(100),
    bank_branch     VARCHAR(100),
    swift_code      VARCHAR(11),

    -- 账户信息
    account_number  VARCHAR(30) NOT NULL,
    account_holder  VARCHAR(100),
    account_type    VARCHAR(2),
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
-- 4. 客户扩展数据 (参考 SAP KNB1, KNVV)
-- ----------------------------------------------------------------------------

-- 客户公司代码数据 (参考 SAP KNB1)
CREATE TABLE bp_customer_company (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    partner_id      UUID NOT NULL REFERENCES bp_partner(id) ON DELETE CASCADE,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 会计信息
    reconc_account   VARCHAR(10),
    payment_terms   VARCHAR(4),
    payment_method   VARCHAR(2),

    -- 信贷管理
    credit_limit    DECIMAL(15,2),
    credit_currency UUID REFERENCES core_currency(id),

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
    customer_group  VARCHAR(2),
    sales_district  VARCHAR(6),
    sales_office    VARCHAR(4),
    sales_group     VARCHAR(3),

    -- 定价
    price_group     VARCHAR(2),
    price_list      VARCHAR(2),
    customer_pricing_group VARCHAR(2),

    -- 付款
    payment_terms   VARCHAR(4),
    payment_method  VARCHAR(2),

    -- 交货
    shipping_conditions VARCHAR(2),
    delivery_priority VARCHAR(2),
    delivering_plant_id UUID REFERENCES sys_plant(id),

    -- 开票
    billing_currency_id UUID REFERENCES core_currency(id),
    incoterms       VARCHAR(3),
    incoterms_2     VARCHAR(28),

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    sales_block     BOOLEAN DEFAULT FALSE,
    delivery_block  BOOLEAN DEFAULT FALSE,
    billing_block   BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (partner_id, sales_area_id, valid_from)
);

CREATE INDEX idx_bp_customer_sales_partner ON bp_customer_sales (partner_id);
CREATE INDEX idx_bp_customer_sales_area ON bp_customer_sales (sales_area_id);

COMMENT ON TABLE bp_customer_sales IS '客户销售范围数据 (参考 SAP KNVV)';

-- ----------------------------------------------------------------------------
-- 5. 供应商扩展数据 (参考 SAP LFB1, LFM1)
-- ----------------------------------------------------------------------------

-- 供应商公司代码数据 (参考 SAP LFB1)
CREATE TABLE bp_supplier_company (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    partner_id      UUID NOT NULL REFERENCES bp_partner(id) ON DELETE CASCADE,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 会计信息
    reconc_account   VARCHAR(10),
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
    supplier_group  VARCHAR(4),
    purchasing_group_id UUID REFERENCES sys_purchasing_group(id),

    -- 订单货币
    currency_id     UUID REFERENCES core_currency(id),
    payment_terms   VARCHAR(4),

    -- 评估（优化：单独字段而非 JSON）
    quality_score   DECIMAL(3,1),
    delivery_score  DECIMAL(3,1),
    price_score     DECIMAL(3,1),
    overall_score   DECIMAL(3,2) GENERATED ALWAYS AS (
        (COALESCE(quality_score, 0) + COALESCE(delivery_score, 0) + COALESCE(price_score, 0)) / 3
    ) STORED,

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    purchasing_block BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (partner_id, purchasing_org_id, valid_from)
);

CREATE INDEX idx_bp_supplier_purchasing_partner ON bp_supplier_purchasing (partner_id);

COMMENT ON TABLE bp_supplier_purchasing IS '供应商采购组织数据 (参考 SAP LFM1)';

-- ----------------------------------------------------------------------------
-- 6. 联系人
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
-- 7. 时间有效性约束（排他约束）
-- ----------------------------------------------------------------------------

-- 业务伙伴角色时间重叠约束
ALTER TABLE bp_partner_role
ADD CONSTRAINT uk_bp_partner_role_no_overlap
EXCLUDE USING GIST (
    partner_id WITH =,
    role_type WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);

-- 地址时间重叠约束
ALTER TABLE bp_address
ADD CONSTRAINT uk_bp_address_no_overlap
EXCLUDE USING GIST (
    partner_id WITH =,
    address_type WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);

-- 客户公司代码时间重叠约束
ALTER TABLE bp_customer_company
ADD CONSTRAINT uk_bp_customer_company_no_overlap
EXCLUDE USING GIST (
    partner_id WITH =,
    company_id WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);

-- 客户销售范围时间重叠约束
ALTER TABLE bp_customer_sales
ADD CONSTRAINT uk_bp_customer_sales_no_overlap
EXCLUDE USING GIST (
    partner_id WITH =,
    sales_area_id WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);

-- 供应商公司代码时间重叠约束
ALTER TABLE bp_supplier_company
ADD CONSTRAINT uk_bp_supplier_company_no_overlap
EXCLUDE USING GIST (
    partner_id WITH =,
    company_id WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);

-- 供应商采购组织时间重叠约束
ALTER TABLE bp_supplier_purchasing
ADD CONSTRAINT uk_bp_supplier_purchasing_no_overlap
EXCLUDE USING GIST (
    partner_id WITH =,
    purchasing_org_id WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);

-- ----------------------------------------------------------------------------
-- 8. 全文搜索函数
-- ----------------------------------------------------------------------------

-- 业务伙伴搜索函数
CREATE OR REPLACE FUNCTION search_bp_partner(
    p_tenant_id UUID,
    p_query TEXT,
    p_limit INTEGER DEFAULT 20
) RETURNS TABLE (
    id UUID,
    partner_number VARCHAR,
    full_name VARCHAR,
    organization_name VARCHAR,
    rank REAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        bp.id,
        bp.partner_number,
        bp.full_name,
        bp.organization_name,
        ts_rank(bp.search_vector, plainto_tsquery('simple', p_query)) AS rank
    FROM bp_partner bp
    WHERE bp.tenant_id = p_tenant_id
      AND bp.status = 'ACTIVE'
      AND bp.search_vector @@ plainto_tsquery('simple', p_query)
    ORDER BY rank DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION search_bp_partner IS '业务伙伴全文搜索';

-- ----------------------------------------------------------------------------
-- 9. 添加审计触发器
-- ----------------------------------------------------------------------------

PERFORM add_audit_trigger('bp_partner');
PERFORM add_audit_trigger('bp_address');
PERFORM add_audit_trigger('bp_bank_account');
PERFORM add_audit_trigger('bp_customer_company');
PERFORM add_audit_trigger('bp_customer_sales');
PERFORM add_audit_trigger('bp_supplier_company');
PERFORM add_audit_trigger('bp_supplier_purchasing');
PERFORM add_audit_trigger('bp_contact_person');
