-- ============================================================================
-- NextERP Tenant & Organization Schema
-- 多租户、公司、组织架构
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 租户管理
-- ----------------------------------------------------------------------------

-- 租户 (参考 SAP T000)
CREATE TABLE sys_tenant (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code            VARCHAR(10) NOT NULL,         -- 租户代码
    name            VARCHAR(100) NOT NULL,        -- 租户名称

    -- 联系信息
    email           VARCHAR(100),
    phone           VARCHAR(50),
    address         TEXT,

    -- 配置
    timezone        VARCHAR(50) DEFAULT 'Asia/Shanghai',
    language        VARCHAR(5) DEFAULT 'zh-CN',
    currency_id     UUID REFERENCES core_currency(id),  -- 本位币

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    license_type    VARCHAR(20),                  -- 许可类型
    license_expires DATE,                         -- 许可到期日

    -- 限制
    max_users       INTEGER DEFAULT 100,
    max_companies   INTEGER DEFAULT 1,
    storage_quota   BIGINT DEFAULT 10737418240,  -- 10GB

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (code)
);

COMMENT ON TABLE sys_tenant IS '租户主表';
COMMENT ON COLUMN sys_tenant.id IS '租户 UUID';
COMMENT ON COLUMN sys_tenant.code IS '租户代码 (如: T001)';

-- 租户配置
CREATE TABLE sys_tenant_config (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id) ON DELETE CASCADE,

    config_key      VARCHAR(100) NOT NULL,        -- 配置键
    config_value    TEXT,                         -- 配置值
    config_type     VARCHAR(20) DEFAULT 'STRING', -- 类型 (STRING/JSON/NUMBER/BOOLEAN)

    description     TEXT,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, config_key)
);

COMMENT ON TABLE sys_tenant_config IS '租户配置表';

-- ----------------------------------------------------------------------------
-- 组织架构 (参考 SAP T001, T001W)
-- ----------------------------------------------------------------------------

-- 公司代码 (参考 SAP T001)
CREATE TABLE sys_company (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    code            VARCHAR(4) NOT NULL,          -- 公司代码
    name            VARCHAR(100) NOT NULL,        -- 公司名称
    name_en         VARCHAR(100),                 -- 英文名称

    -- 法律信息
    legal_name      VARCHAR(200),                 -- 法定名称
    tax_id          VARCHAR(50),                  -- 税号
    reg_number      VARCHAR(50),                  -- 注册号

    -- 地址信息
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city_id         UUID REFERENCES core_city(id),
    address         TEXT,                         -- 详细地址
    postal_code     VARCHAR(20),

    -- 联系信息
    phone           VARCHAR(50),
    fax             VARCHAR(50),
    email           VARCHAR(100),
    website         VARCHAR(200),

    -- 财务配置
    currency_id     UUID REFERENCES core_currency(id),  -- 本位币
    fiscal_year_variant VARCHAR(2) DEFAULT 'K4',  -- 会计年度变式
    fiscal_year_start_month INTEGER DEFAULT 1,    -- 会计年度起始月

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, code)
);

CREATE INDEX idx_sys_company_tenant ON sys_company (tenant_id);
CREATE INDEX idx_sys_company_status ON sys_company (tenant_id, status);

COMMENT ON TABLE sys_company IS '公司代码表 (参考 SAP T001)';

-- 工厂 (参考 SAP T001W)
CREATE TABLE sys_plant (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    code            VARCHAR(4) NOT NULL,          -- 工厂代码
    name            VARCHAR(100) NOT NULL,        -- 工厂名称
    name_en         VARCHAR(100),

    -- 工厂类型
    plant_type      VARCHAR(10) DEFAULT 'PRODUCTION', -- 生产/采购/分销

    -- 地址信息
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city_id         UUID REFERENCES core_city(id),
    address         TEXT,
    postal_code     VARCHAR(20),

    -- 联系信息
    phone           VARCHAR(50),
    email           VARCHAR(100),

    -- 配置
    language        VARCHAR(5) DEFAULT 'zh-CN',
    currency_id     UUID REFERENCES core_currency(id),

    -- 评估
    valuation_area  VARCHAR(4),                   -- 评估范围

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, code)
);

CREATE INDEX idx_sys_plant_company ON sys_plant (company_id);
CREATE INDEX idx_sys_plant_tenant ON sys_plant (tenant_id);

COMMENT ON TABLE sys_plant IS '工厂表 (参考 SAP T001W)';

-- 库存地点 (参考 SAP T001L)
CREATE TABLE sys_storage_location (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    code            VARCHAR(4) NOT NULL,          -- 库存地点代码
    name            VARCHAR(100) NOT NULL,        -- 库存地点名称
    name_en         VARCHAR(100),

    -- 类型
    location_type   VARCHAR(20) DEFAULT 'NORMAL', -- 普通/寄售/外协

    -- 地址
    address         TEXT,

    -- 配置
    is_storeroom    BOOLEAN DEFAULT FALSE,        -- 是否仓库
    mrp_indicator   BOOLEAN DEFAULT TRUE,         -- MRP 标识

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (plant_id, code)
);

CREATE INDEX idx_sys_storage_location_plant ON sys_storage_location (plant_id);

COMMENT ON TABLE sys_storage_location IS '库存地点表 (参考 SAP T001L)';

-- 销售组织 (参考 SAP TVKO)
CREATE TABLE sys_sales_organization (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    code            VARCHAR(4) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE sys_sales_organization IS '销售组织表 (参考 SAP TVKO)';

-- 分销渠道 (参考 SAP TVTW)
CREATE TABLE sys_distribution_channel (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    code            VARCHAR(2) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE sys_distribution_channel IS '分销渠道表 (参考 SAP TVTW)';

-- 产品组 (参考 SAP TSPA)
CREATE TABLE sys_division (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    code            VARCHAR(2) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE sys_division IS '产品组表 (参考 SAP TSPA)';

-- 销售范围 (参考 SAP TVTA)
CREATE TABLE sys_sales_area (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    sales_org_id    UUID NOT NULL REFERENCES sys_sales_organization(id),
    dist_channel_id UUID NOT NULL REFERENCES sys_distribution_channel(id),
    division_id     UUID NOT NULL REFERENCES sys_division(id),

    name            VARCHAR(200),                 -- 销售范围名称

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (sales_org_id, dist_channel_id, division_id)
);

COMMENT ON TABLE sys_sales_area IS '销售范围表 (参考 SAP TVTA)';

-- 采购组织 (参考 SAP T024E)
CREATE TABLE sys_purchasing_organization (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    company_id      UUID REFERENCES sys_company(id),

    code            VARCHAR(4) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE sys_purchasing_organization IS '采购组织表 (参考 SAP T024E)';

-- 采购组 (参考 SAP T024)
CREATE TABLE sys_purchasing_group (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    code            VARCHAR(3) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),

    -- 采购员
    buyer_email     VARCHAR(100),
    buyer_phone     VARCHAR(50),

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE sys_purchasing_group IS '采购组表 (参考 SAP T024)';

-- 成本控制范围 (参考 SAP TKA01)
CREATE TABLE sys_controlling_area (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    code            VARCHAR(4) NOT NULL,
    name            VARCHAR(100) NOT NULL,

    -- 配置
    currency_id     UUID REFERENCES core_currency(id),
    fiscal_year_variant VARCHAR(2) DEFAULT 'K4',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE sys_controlling_area IS '成本控制范围表 (参考 SAP TKA01)';

-- 成本中心 (参考 SAP CSKS)
CREATE TABLE sys_cost_center (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    controlling_area_id UUID NOT NULL REFERENCES sys_controlling_area(id),
    company_id      UUID REFERENCES sys_company(id),

    code            VARCHAR(10) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),

    -- 层级
    parent_id       UUID REFERENCES sys_cost_center(id),

    -- 负责人
    manager_id      UUID,                         -- 员工 ID

    -- 标准层次
    standard_hierarchy VARCHAR(10),

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (controlling_area_id, code, valid_from)
);

CREATE INDEX idx_sys_cost_center_parent ON sys_cost_center (parent_id);

COMMENT ON TABLE sys_cost_center IS '成本中心表 (参考 SAP CSKS)';

-- 利润中心 (参考 SAP CEPC)
CREATE TABLE sys_profit_center (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    controlling_area_id UUID NOT NULL REFERENCES sys_controlling_area(id),

    code            VARCHAR(10) NOT NULL,
    name            VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),

    -- 层级
    parent_id       UUID REFERENCES sys_profit_center(id),

    -- 公司
    company_id      UUID REFERENCES sys_company(id),

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (controlling_area_id, code, valid_from)
);

COMMENT ON TABLE sys_profit_center IS '利润中心表 (参考 SAP CEPC)';

-- ----------------------------------------------------------------------------
-- 触发器
-- ----------------------------------------------------------------------------

CREATE TRIGGER trigger_sys_tenant_updated_at
    BEFORE UPDATE ON sys_tenant
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_sys_company_updated_at
    BEFORE UPDATE ON sys_company
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_sys_plant_updated_at
    BEFORE UPDATE ON sys_plant
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
