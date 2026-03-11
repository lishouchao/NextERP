-- ============================================================================
-- NextERP MM Schema
-- 物料管理 - 物料主数据、采购、库存 - 借鉴 SAP ECC MM 模块
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 物料主数据 (参考 SAP MARA, MARC, MARD, MBEW)
-- ----------------------------------------------------------------------------

-- 物料类型 (参考 SAP T134)
CREATE TABLE mm_material_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    code            VARCHAR(4) NOT NULL,
    name            VARCHAR(100) NOT NULL,

    -- 属性
    is_valuated     BOOLEAN DEFAULT TRUE,         -- 是否评估
    is_quantity     BOOLEAN DEFAULT TRUE,         -- 是否数量管理
    is_value_update BOOLEAN DEFAULT TRUE,         -- 是否价值更新

    -- 科目确定
    account_category_reference VARCHAR(4),

    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE mm_material_type IS '物料类型 (参考 SAP T134)';

-- 物料组 (参考 SAP T023)
CREATE TABLE mm_material_group (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    code            VARCHAR(9) NOT NULL,
    name            VARCHAR(100) NOT NULL,

    -- 层级
    parent_id       UUID REFERENCES mm_material_group(id),

    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE mm_material_group IS '物料组 (参考 SAP T023)';

-- 物料主数据通用视图 (参考 SAP MARA)
CREATE TABLE mm_material (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    -- 编码
    material_number VARCHAR(18),                  -- 物料号 (可自动生成)
    external_id     VARCHAR(50),                  -- 外部编号

    -- 类型
    material_type_id UUID NOT NULL REFERENCES mm_material_type(id),
    material_group_id UUID REFERENCES mm_material_group(id),

    -- 描述
    name            VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),
    description     TEXT,

    -- 基本数据
    base_uom_id     UUID NOT NULL REFERENCES core_uom(id),  -- 基本单位
    order_uom_id    UUID REFERENCES core_uom(id),           -- 订单单位
    conversion_factor DECIMAL(5,3) DEFAULT 1,               -- 换算系数

    -- 重量
    gross_weight    DECIMAL(13,3),
    net_weight      DECIMAL(13,3),
    weight_uom_id   UUID REFERENCES core_uom(id),

    -- 体积
    volume          DECIMAL(13,3),
    volume_uom_id   UUID REFERENCES core_uom(id),

    -- 尺寸
    length          DECIMAL(13,3),
    width           DECIMAL(13,3),
    height          DECIMAL(13,3),
    size_uom_id     UUID REFERENCES core_uom(id),

    -- 条码
    ean_code        VARCHAR(18),
    ean_type        VARCHAR(2),

    -- 批次管理
    batch_managed   BOOLEAN DEFAULT FALSE,
    batch_required  BOOLEAN DEFAULT FALSE,

    -- 序列号管理
    serial_managed  BOOLEAN DEFAULT FALSE,
    serial_required BOOLEAN DEFAULT FALSE,

    -- 危险品
    is_hazardous    BOOLEAN DEFAULT FALSE,
    hazard_class    VARCHAR(4),

    -- 分类
    industry_sector VARCHAR(1),                   -- 行业部门
    product_hierarchy VARCHAR(18),

    -- 交叉引用
    old_material_number VARCHAR(18),
    manufacturer    VARCHAR(100),
    manufacturer_part VARCHAR(40),

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    is_deleted      BOOLEAN DEFAULT FALSE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, material_number)
);

CREATE INDEX idx_mm_material_tenant ON mm_material (tenant_id);
CREATE INDEX idx_mm_material_type ON mm_material (material_type_id);
CREATE INDEX idx_mm_material_group ON mm_material (material_group_id);
CREATE INDEX idx_mm_material_name ON mm_material (tenant_id, name);
CREATE INDEX idx_mm_material_valid ON mm_material (valid_from, valid_to);

COMMENT ON TABLE mm_material IS '物料主数据 (参考 SAP MARA)';

-- 物料工厂数据 (参考 SAP MARC)
CREATE TABLE mm_material_plant (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    material_id     UUID NOT NULL REFERENCES mm_material(id) ON DELETE CASCADE,
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    -- MRP 数据
    mrp_type        VARCHAR(4) DEFAULT 'PD',      -- MRP 类型
    mrp_controller  VARCHAR(3),                   -- MRP 控制员
    lot_size_procedure VARCHAR(2),                -- 批量过程
    minimum_lot_size DECIMAL(13,3),
    maximum_lot_size DECIMAL(13,3),
    fixed_lot_size  DECIMAL(13,3),
    rounding_value  DECIMAL(13,3),

    -- 安全库存
    safety_stock    DECIMAL(13,3),
    minimum_stock   DECIMAL(13,3),
    maximum_stock   DECIMAL(13,3),

    -- 提前期
    planned_delivery_time INTEGER,                -- 计划交货时间 (天)
    goods_receipt_time INTEGER,                   -- 收货处理时间 (天)
    inhouse_production_time DECIMAL(5,2),         -- 厂内生产时间 (天)

    -- 采购
    purchasing_group_id UUID REFERENCES sys_purchasing_group(id),
    purchasing_type VARCHAR(1),                   -- E=外部, F=外部+内部, X=内部
    special_procurement VARCHAR(2),
    production_storage_location_id UUID REFERENCES sys_storage_location(id),

    -- 批次
    batch_managed   BOOLEAN DEFAULT FALSE,

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (material_id, plant_id)
);

CREATE INDEX idx_mm_material_plant_material ON mm_material_plant (material_id);
CREATE INDEX idx_mm_material_plant_plant ON mm_material_plant (plant_id);

COMMENT ON TABLE mm_material_plant IS '物料工厂数据 (参考 SAP MARC)';

-- 物料评估数据 (参考 SAP MBEW)
CREATE TABLE mm_material_valuation (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    material_id     UUID NOT NULL REFERENCES mm_material(id) ON DELETE CASCADE,
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    -- 评估类
    valuation_class VARCHAR(4),

    -- 价格控制
    price_control   VARCHAR(1) DEFAULT 'S' CHECK (price_control IN ('S', 'V')), -- S=标准, V=移动平均
    standard_price  DECIMAL(15,2),                -- 标准价格
    moving_avg_price DECIMAL(15,2),               -- 移动平均价
    price_unit      INTEGER DEFAULT 1,

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 价值
    total_valuation DECIMAL(23,2),                -- 总评估价值
    total_quantity  DECIMAL(18,6),

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (material_id, plant_id, valid_from)
);

COMMENT ON TABLE mm_material_valuation IS '物料评估数据 (参考 SAP MBEW)';

-- 物料库存数据 (参考 SAP MARD)
CREATE TABLE mm_material_storage (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    material_id     UUID NOT NULL REFERENCES mm_material(id) ON DELETE CASCADE,
    storage_location_id UUID NOT NULL REFERENCES sys_storage_location(id),

    -- 库存数量
    unrestricted_stock DECIMAL(18,6) DEFAULT 0,   -- 非限制库存
    quality_stock   DECIMAL(18,6) DEFAULT 0,      -- 质量检验库存
    blocked_stock   DECIMAL(18,6) DEFAULT 0,      -- 冻结库存
    in_transit_stock DECIMAL(18,6) DEFAULT 0,     -- 在途库存

    -- 价值
    unrestricted_value DECIMAL(23,2) DEFAULT 0,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (material_id, storage_location_id)
);

CREATE INDEX idx_mm_material_storage_material ON mm_material_storage (material_id);

COMMENT ON TABLE mm_material_storage IS '物料库存数据 (参考 SAP MARD)';

-- ----------------------------------------------------------------------------
-- 采购申请 (参考 SAP EBAN)
-- ----------------------------------------------------------------------------

CREATE TABLE mm_purchase_requisition_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    -- 编号
    pr_number       VARCHAR(10),
    document_type   VARCHAR(4) DEFAULT 'NB',

    -- 日期
    pr_date         DATE NOT NULL DEFAULT CURRENT_DATE,
    required_date   DATE,

    -- 状态
    status          general_status DEFAULT 'DRAFT',
    approval_status approval_status DEFAULT 'DRAFT',

    -- 审批
    approved_by     UUID,
    approved_at     TIMESTAMP,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, pr_number)
);

CREATE TABLE mm_purchase_requisition_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    header_id       UUID NOT NULL REFERENCES mm_purchase_requisition_hdr(id) ON DELETE CASCADE,

    -- 项目
    line_item       INTEGER NOT NULL,

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_number VARCHAR(18),
    description     VARCHAR(100),

    -- 数量
    quantity        DECIMAL(18,6) NOT NULL,
    uom_id          UUID NOT NULL REFERENCES core_uom(id),
    quantity_ordered DECIMAL(18,6) DEFAULT 0,

    -- 日期
    delivery_date   DATE,

    -- 价格
    price           DECIMAL(15,2),
    currency_id     UUID REFERENCES core_currency(id),

    -- 采购信息
    purchasing_org_id UUID REFERENCES sys_purchasing_organization(id),
    purchasing_group_id UUID REFERENCES sys_purchasing_group(id),

    -- 成本对象
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    account_id      UUID REFERENCES fi_gl_account(id),

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    is_closed       BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, line_item)
);

COMMENT ON TABLE mm_purchase_requisition_hdr IS '采购申请头';
COMMENT ON TABLE mm_purchase_requisition_itm IS '采购申请项';

-- ----------------------------------------------------------------------------
-- 采购订单 (参考 SAP EKKO, EKPO)
-- ----------------------------------------------------------------------------

-- 采购订单头 (参考 SAP EKKO)
CREATE TABLE mm_purchase_order_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 编号
    po_number       VARCHAR(10),
    document_type   VARCHAR(4) DEFAULT 'NB',

    -- 供应商
    supplier_id     UUID NOT NULL REFERENCES bp_partner(id),

    -- 组织
    purchasing_org_id UUID REFERENCES sys_purchasing_organization(id),
    purchasing_group_id UUID REFERENCES sys_purchasing_group(id),

    -- 日期
    document_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    validity_start  DATE,
    validity_end    DATE,

    -- 货币
    currency_id     UUID NOT NULL REFERENCES core_currency(id),
    exchange_rate   DECIMAL(12,6) DEFAULT 1,

    -- 付款条件
    payment_term_id UUID REFERENCES fi_payment_term(id),
    incoterms       VARCHAR(3),
    incoterms_2     VARCHAR(28),

    -- 金额
    total_amount    DECIMAL(23,2) DEFAULT 0,

    -- 参考
    vendor_ref      VARCHAR(35),                  -- 供应商参考号

    -- 状态
    status          general_status DEFAULT 'DRAFT',
    approval_status approval_status DEFAULT 'DRAFT',

    -- 审批
    approved_by     UUID,
    approved_at     TIMESTAMP,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, po_number)
);

CREATE INDEX idx_mm_purchase_order_hdr_supplier ON mm_purchase_order_hdr (supplier_id);
CREATE INDEX idx_mm_purchase_order_hdr_date ON mm_purchase_order_hdr (document_date);

COMMENT ON TABLE mm_purchase_order_hdr IS '采购订单头 (参考 SAP EKKO)';

-- 采购订单项 (参考 SAP EKPO)
CREATE TABLE mm_purchase_order_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    header_id       UUID NOT NULL REFERENCES mm_purchase_order_hdr(id) ON DELETE CASCADE,

    -- 项目
    line_item       INTEGER NOT NULL,

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_number VARCHAR(18),
    description     VARCHAR(100),

    -- 工厂/库存地点
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    storage_location_id UUID REFERENCES sys_storage_location(id),

    -- 数量
    quantity        DECIMAL(18,6) NOT NULL,
    uom_id          UUID NOT NULL REFERENCES core_uom(id),
    quantity_delivered DECIMAL(18,6) DEFAULT 0,
    quantity_invoiced DECIMAL(18,6) DEFAULT 0,

    -- 日期
    delivery_date   DATE,

    -- 价格
    price           DECIMAL(15,2),
    price_unit      INTEGER DEFAULT 1,
    currency_id     UUID REFERENCES core_currency(id),

    -- 金额
    net_amount      DECIMAL(23,2),
    tax_amount      DECIMAL(23,2),
    gross_amount    DECIMAL(23,2),

    -- 税
    tax_code_id     UUID REFERENCES fi_tax_code(id),

    -- 成本对象
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    account_id      UUID REFERENCES fi_gl_account(id),

    -- 项目类别
    item_category   VARCHAR(1) DEFAULT '0',       -- 0=标准, 1=寄售

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    is_deleted      BOOLEAN DEFAULT FALSE,
    is_closed       BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, line_item)
);

CREATE INDEX idx_mm_purchase_order_itm_header ON mm_purchase_order_itm (header_id);
CREATE INDEX idx_mm_purchase_order_itm_material ON mm_purchase_order_itm (material_id);

COMMENT ON TABLE mm_purchase_order_itm IS '采购订单项 (参考 SAP EKPO)';

-- ----------------------------------------------------------------------------
-- 库存移动 (参考 SAP MKPF, MSEG)
-- ----------------------------------------------------------------------------

-- 移动类型 (参考 SAP T156)
CREATE TABLE mm_movement_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    code            VARCHAR(3) NOT NULL,          -- 移动类型代码
    name            VARCHAR(100) NOT NULL,

    -- 属性
    is_receipt      BOOLEAN DEFAULT FALSE,        -- 收货
    is_issue        BOOLEAN DEFAULT FALSE,        -- 发货
    is_transfer     BOOLEAN DEFAULT FALSE,        -- 转储

    -- 科目确定
    account_modification VARCHAR(1),

    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

-- 插入常用移动类型
INSERT INTO mm_movement_type (tenant_id, code, name, is_receipt, is_issue, is_transfer) VALUES
((SELECT id FROM sys_tenant LIMIT 1), '101', '采购订单收货', TRUE, FALSE, FALSE),
((SELECT id FROM sys_tenant LIMIT 1), '102', '采购订单收货冲销', FALSE, TRUE, FALSE),
((SELECT id FROM sys_tenant LIMIT 1), '201', '成本中心发货', FALSE, TRUE, FALSE),
((SELECT id FROM sys_tenant LIMIT 1), '202', '成本中心发货冲销', TRUE, FALSE, FALSE),
((SELECT id FROM sys_tenant LIMIT 1), '301', '工厂间转储', FALSE, FALSE, TRUE),
((SELECT id FROM sys_tenant LIMIT 1), '311', '库存地点转储', FALSE, FALSE, TRUE),
((SELECT id FROM sys_tenant LIMIT 1), '501', '无采购订单收货', TRUE, FALSE, FALSE);

COMMENT ON TABLE mm_movement_type IS '移动类型 (参考 SAP T156)';

-- 物料凭证头 (参考 SAP MKPF)
CREATE TABLE mm_material_document_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 编号
    document_number VARCHAR(10),
    fiscal_year     INTEGER NOT NULL,

    -- 日期
    document_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    posting_date    DATE NOT NULL DEFAULT CURRENT_DATE,

    -- 文本
    header_text     VARCHAR(100),

    -- 参考
    reference       VARCHAR(20),

    -- 状态
    status          general_status DEFAULT 'COMPLETED',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, document_number, fiscal_year)
);

CREATE INDEX idx_mm_material_document_hdr_date ON mm_material_document_hdr (posting_date);

COMMENT ON TABLE mm_material_document_hdr IS '物料凭证头 (参考 SAP MKPF)';

-- 物料凭证项 (参考 SAP MSEG)
CREATE TABLE mm_material_document_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    header_id       UUID NOT NULL REFERENCES mm_material_document_hdr(id) ON DELETE CASCADE,

    -- 项目
    line_item       INTEGER NOT NULL,

    -- 物料
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    material_number VARCHAR(18),

    -- 工厂/库存地点
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    storage_location_id UUID REFERENCES sys_storage_location(id),

    -- 批次
    batch_number    VARCHAR(10),

    -- 移动类型
    movement_type_id UUID NOT NULL REFERENCES mm_movement_type(id),
    movement_type   VARCHAR(3),

    -- 数量
    quantity        DECIMAL(18,6) NOT NULL,
    uom_id          UUID NOT NULL REFERENCES core_uom(id),

    -- 借贷标识
    debit_credit    debit_credit NOT NULL,

    -- 金额
    amount          DECIMAL(23,2),
    currency_id     UUID REFERENCES core_currency(id),

    -- 来源
    po_item_id      UUID REFERENCES mm_purchase_order_itm(id),

    -- 成本对象
    cost_center_id  UUID REFERENCES sys_cost_center(id),

    -- 文本
    item_text       VARCHAR(100),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, line_item)
);

CREATE INDEX idx_mm_material_document_itm_material ON mm_material_document_itm (material_id);
CREATE INDEX idx_mm_material_document_itm_plant ON mm_material_document_itm (plant_id);

COMMENT ON TABLE mm_material_document_itm IS '物料凭证项 (参考 SAP MSEG)';

-- ----------------------------------------------------------------------------
-- 触发器
-- ----------------------------------------------------------------------------

CREATE TRIGGER trigger_mm_material_updated_at
    BEFORE UPDATE ON mm_material
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_mm_purchase_order_hdr_updated_at
    BEFORE UPDATE ON mm_purchase_order_hdr
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
