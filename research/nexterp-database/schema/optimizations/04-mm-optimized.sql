-- ============================================================================
-- NextERP 优化版 MM Schema
-- 优化点：分区表、统一审计触发器、全文搜索、库存快照
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. 物料主数据 (参考 SAP MARA, MARC, MARD, MBEW)
-- ----------------------------------------------------------------------------

-- 物料类型 (参考 SAP T134)
CREATE TABLE mm_material_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    code            VARCHAR(4) NOT NULL,
    name            VARCHAR(100) NOT NULL,

    -- 属性
    is_valuated     BOOLEAN DEFAULT TRUE,
    is_quantity     BOOLEAN DEFAULT TRUE,
    is_value_update BOOLEAN DEFAULT TRUE,

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
    level           INTEGER DEFAULT 1,
    path            VARCHAR(100),                -- 物化路径 (如: /01/02/03)

    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

CREATE INDEX idx_mm_material_group_path ON mm_material_group (tenant_id, path);

COMMENT ON TABLE mm_material_group IS '物料组 (参考 SAP T023)';

-- 物料主数据通用视图 (参考 SAP MARA)
CREATE TABLE mm_material (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    -- 编码
    material_number VARCHAR(18),
    external_id     VARCHAR(50),

    -- 类型
    material_type_id UUID NOT NULL REFERENCES mm_material_type(id),
    material_group_id UUID REFERENCES mm_material_group(id),

    -- 描述
    name            VARCHAR(100) NOT NULL,
    name_en         VARCHAR(100),
    description     TEXT,

    -- 基本数据
    base_uom_id     UUID NOT NULL REFERENCES core_uom(id),
    order_uom_id    UUID REFERENCES core_uom(id),
    conversion_factor DECIMAL(5,3) DEFAULT 1,

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
    industry_sector VARCHAR(1),
    product_hierarchy VARCHAR(18),

    -- 交叉引用
    old_material_number VARCHAR(18),
    manufacturer    VARCHAR(100),
    manufacturer_part VARCHAR(40),

    -- 全文搜索向量
    search_vector   TSVECTOR GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', COALESCE(material_number, '')), 'A') ||
        setweight(to_tsvector('simple', COALESCE(name, '')), 'A') ||
        setweight(to_tsvector('simple', COALESCE(name_en, '')), 'B') ||
        setweight(to_tsvector('simple', COALESCE(description, '')), 'C')
    ) STORED,

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

-- 全文搜索索引
CREATE INDEX idx_mm_material_search_vector ON mm_material USING GIN (search_vector);
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
    mrp_type        VARCHAR(4) DEFAULT 'PD',
    mrp_controller  VARCHAR(3),
    lot_size_procedure VARCHAR(2),
    minimum_lot_size DECIMAL(13,3),
    maximum_lot_size DECIMAL(13,3),
    fixed_lot_size  DECIMAL(13,3),
    rounding_value  DECIMAL(13,3),

    -- 安全库存
    safety_stock    DECIMAL(13,3),
    minimum_stock   DECIMAL(13,3),
    maximum_stock   DECIMAL(13,3),

    -- 提前期
    planned_delivery_time INTEGER,
    goods_receipt_time INTEGER,
    inhouse_production_time DECIMAL(5,2),

    -- 采购
    purchasing_group_id UUID REFERENCES sys_purchasing_group(id),
    purchasing_type VARCHAR(1),
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

-- 物料评估数据 (参考 SAP MBEW) - 带时间有效性
CREATE TABLE mm_material_valuation (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    material_id     UUID NOT NULL REFERENCES mm_material(id) ON DELETE CASCADE,
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    -- 评估类
    valuation_class VARCHAR(4),

    -- 价格控制
    price_control   VARCHAR(1) DEFAULT 'S' CHECK (price_control IN ('S', 'V')),
    standard_price  DECIMAL(15,2),
    moving_avg_price DECIMAL(15,2),
    price_unit      INTEGER DEFAULT 1,

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 价值
    total_valuation DECIMAL(23,2),
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
    unrestricted_stock DECIMAL(18,6) DEFAULT 0,
    quality_stock   DECIMAL(18,6) DEFAULT 0,
    blocked_stock   DECIMAL(18,6) DEFAULT 0,
    in_transit_stock DECIMAL(18,6) DEFAULT 0,

    -- 可用库存（生成列）
    available_stock DECIMAL(18,6) GENERATED ALWAYS AS (
        unrestricted_stock - in_transit_stock
    ) STORED,

    -- 价值
    unrestricted_value DECIMAL(23,2) DEFAULT 0,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (material_id, storage_location_id)
);

CREATE INDEX idx_mm_material_storage_material ON mm_material_storage (material_id);

COMMENT ON TABLE mm_material_storage IS '物料库存数据 (参考 SAP MARD)';

-- ----------------------------------------------------------------------------
-- 2. 采购申请 (参考 SAP EBAN)
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
    version         INTEGER DEFAULT 0,

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

    -- 开放数量（生成列）
    open_quantity   DECIMAL(18,6) GENERATED ALWAYS AS (
        quantity - quantity_ordered
    ) STORED,

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
-- 3. 采购订单 (参考 SAP EKKO, EKPO) - 分区表
-- ----------------------------------------------------------------------------

-- 采购订单头 (参考 SAP EKKO) - 按年度分区
CREATE TABLE mm_purchase_order_hdr (
    id              UUID NOT NULL,
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 年度（分区键）
    fiscal_year     INTEGER NOT NULL DEFAULT EXTRACT(YEAR FROM CURRENT_DATE),

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
    vendor_ref      VARCHAR(35),

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

    PRIMARY KEY (fiscal_year, id),
    UNIQUE (tenant_id, fiscal_year, po_number)
) PARTITION BY RANGE (fiscal_year);

-- 创建分区
CREATE TABLE mm_purchase_order_hdr_2024
    PARTITION OF mm_purchase_order_hdr
    FOR VALUES FROM (2024) TO (2025);

CREATE TABLE mm_purchase_order_hdr_2025
    PARTITION OF mm_purchase_order_hdr
    FOR VALUES FROM (2025) TO (2026);

CREATE TABLE mm_purchase_order_hdr_default
    PARTITION OF mm_purchase_order_hdr DEFAULT;

CREATE INDEX idx_mm_purchase_order_hdr_supplier ON mm_purchase_order_hdr (supplier_id);
CREATE INDEX idx_mm_purchase_order_hdr_date ON mm_purchase_order_hdr (document_date);

COMMENT ON TABLE mm_purchase_order_hdr IS '采购订单头 (参考 SAP EKKO) - 按年度分区';

-- 采购订单项 (参考 SAP EKPO)
CREATE TABLE mm_purchase_order_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    fiscal_year     INTEGER NOT NULL,
    header_id       UUID NOT NULL,

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

    -- 开放数量（生成列）
    open_quantity   DECIMAL(18,6) GENERATED ALWAYS AS (
        quantity - quantity_delivered
    ) STORED,

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
    item_category   VARCHAR(1) DEFAULT '0',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',
    is_deleted      BOOLEAN DEFAULT FALSE,
    is_closed       BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (fiscal_year, header_id, line_item),
    FOREIGN KEY (fiscal_year, header_id) REFERENCES mm_purchase_order_hdr(fiscal_year, id) ON DELETE CASCADE
);

CREATE INDEX idx_mm_purchase_order_itm_header ON mm_purchase_order_itm (header_id);
CREATE INDEX idx_mm_purchase_order_itm_material ON mm_purchase_order_itm (material_id);

COMMENT ON TABLE mm_purchase_order_itm IS '采购订单项 (参考 SAP EKPO)';

-- ----------------------------------------------------------------------------
-- 4. 库存移动 (参考 SAP MKPF, MSEG) - 分区表
-- ----------------------------------------------------------------------------

-- 移动类型 (参考 SAP T156)
CREATE TABLE mm_movement_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    code            VARCHAR(3) NOT NULL,
    name            VARCHAR(100) NOT NULL,

    -- 属性
    is_receipt      BOOLEAN DEFAULT FALSE,
    is_issue        BOOLEAN DEFAULT FALSE,
    is_transfer     BOOLEAN DEFAULT FALSE,

    -- 科目确定
    account_modification VARCHAR(1),

    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, code)
);

COMMENT ON TABLE mm_movement_type IS '移动类型 (参考 SAP T156)';

-- 物料凭证头 (参考 SAP MKPF) - 按年度分区
CREATE TABLE mm_material_document_hdr (
    id              UUID NOT NULL,
    tenant_id       UUID NOT NULL,

    -- 年度（分区键）
    fiscal_year     INTEGER NOT NULL DEFAULT EXTRACT(YEAR FROM CURRENT_DATE),

    -- 编号
    document_number VARCHAR(10),

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

    PRIMARY KEY (fiscal_year, id),
    UNIQUE (tenant_id, fiscal_year, document_number)
) PARTITION BY RANGE (fiscal_year);

-- 创建分区
CREATE TABLE mm_material_document_hdr_2024
    PARTITION OF mm_material_document_hdr
    FOR VALUES FROM (2024) TO (2025);

CREATE TABLE mm_material_document_hdr_2025
    PARTITION OF mm_material_document_hdr
    FOR VALUES FROM (2025) TO (2026);

CREATE TABLE mm_material_document_hdr_default
    PARTITION OF mm_material_document_hdr DEFAULT;

CREATE INDEX idx_mm_material_document_hdr_date ON mm_material_document_hdr (posting_date);

COMMENT ON TABLE mm_material_document_hdr IS '物料凭证头 (参考 SAP MKPF) - 按年度分区';

-- 物料凭证项 (参考 SAP MSEG)
CREATE TABLE mm_material_document_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    fiscal_year     INTEGER NOT NULL,
    header_id       UUID NOT NULL,

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
    po_item_id      UUID,

    -- 成本对象
    cost_center_id  UUID REFERENCES sys_cost_center(id),

    -- 文本
    item_text       VARCHAR(100),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (fiscal_year, header_id, line_item),
    FOREIGN KEY (fiscal_year, header_id) REFERENCES mm_material_document_hdr(fiscal_year, id) ON DELETE CASCADE
);

CREATE INDEX idx_mm_material_document_itm_material ON mm_material_document_itm (material_id);
CREATE INDEX idx_mm_material_document_itm_plant ON mm_material_document_itm (plant_id);
CREATE INDEX idx_mm_material_document_itm_date ON mm_material_document_itm (created_at);

COMMENT ON TABLE mm_material_document_itm IS '物料凭证项 (参考 SAP MSEG)';

-- ----------------------------------------------------------------------------
-- 5. 库存快照（优化报表查询）
-- ----------------------------------------------------------------------------

-- 库存快照表 - 按月分区
CREATE TABLE mm_inventory_snapshot (
    id              UUID NOT NULL,
    tenant_id       UUID NOT NULL,

    -- 快照月份（分区键）
    snapshot_year   INTEGER NOT NULL,
    snapshot_month  INTEGER NOT NULL,
    snapshot_date   DATE NOT NULL,

    -- 物料位置
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    storage_location_id UUID REFERENCES sys_storage_location(id),

    -- 库存数量
    unrestricted_stock DECIMAL(18,6) NOT NULL,
    quality_stock   DECIMAL(18,6) NOT NULL,
    blocked_stock   DECIMAL(18,6) NOT NULL,
    in_transit_stock DECIMAL(18,6) NOT NULL,

    -- 价值
    stock_value     DECIMAL(23,2),
    currency_id     UUID REFERENCES core_currency(id),

    -- 移动统计
    receipt_qty     DECIMAL(18,6) DEFAULT 0,
    issue_qty       DECIMAL(18,6) DEFAULT 0,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (snapshot_year, id),
    UNIQUE (snapshot_year, snapshot_month, material_id, plant_id, storage_location_id)
) PARTITION BY RANGE (snapshot_year);

-- 创建分区
CREATE TABLE mm_inventory_snapshot_2024
    PARTITION OF mm_inventory_snapshot
    FOR VALUES FROM (2024) TO (2025);

CREATE TABLE mm_inventory_snapshot_2025
    PARTITION OF mm_inventory_snapshot
    FOR VALUES FROM (2025) TO (2026);

CREATE TABLE mm_inventory_snapshot_default
    PARTITION OF mm_inventory_snapshot DEFAULT;

CREATE INDEX idx_mm_inventory_snapshot_material ON mm_inventory_snapshot (material_id);
CREATE INDEX idx_mm_inventory_snapshot_plant ON mm_inventory_snapshot (plant_id);

COMMENT ON TABLE mm_inventory_snapshot IS '库存快照表 - 按月分区，优化报表查询';

-- ----------------------------------------------------------------------------
-- 6. 全文搜索函数
-- ----------------------------------------------------------------------------

-- 物料搜索函数
CREATE OR REPLACE FUNCTION search_mm_material(
    p_tenant_id UUID,
    p_query TEXT,
    p_limit INTEGER DEFAULT 20
) RETURNS TABLE (
    id UUID,
    material_number VARCHAR,
    name VARCHAR,
    description TEXT,
    rank REAL
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        m.id,
        m.material_number,
        m.name,
        m.description,
        ts_rank(m.search_vector, plainto_tsquery('simple', p_query)) AS rank
    FROM mm_material m
    WHERE m.tenant_id = p_tenant_id
      AND m.status = 'ACTIVE'
      AND m.is_deleted = FALSE
      AND m.search_vector @@ plainto_tsquery('simple', p_query)
    ORDER BY rank DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION search_mm_material IS '物料全文搜索';

-- ----------------------------------------------------------------------------
-- 7. 库存快照生成函数
-- ----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION generate_inventory_snapshot(
    p_tenant_id UUID,
    p_snapshot_date DATE DEFAULT CURRENT_DATE
) RETURNS INTEGER AS $$
DECLARE
    v_year INTEGER := EXTRACT(YEAR FROM p_snapshot_date);
    v_month INTEGER := EXTRACT(MONTH FROM p_snapshot_date);
    v_count INTEGER;
BEGIN
    -- 删除当月已有快照
    DELETE FROM mm_inventory_snapshot
    WHERE tenant_id = p_tenant_id
      AND snapshot_year = v_year
      AND snapshot_month = v_month;

    -- 插入新快照
    INSERT INTO mm_inventory_snapshot (
        id, tenant_id, snapshot_year, snapshot_month, snapshot_date,
        material_id, plant_id, storage_location_id,
        unrestricted_stock, quality_stock, blocked_stock, in_transit_stock,
        stock_value, currency_id
    )
    SELECT
        gen_random_uuid(),
        p_tenant_id,
        v_year,
        v_month,
        p_snapshot_date,
        ms.material_id,
        s.plant_id,
        ms.storage_location_id,
        ms.unrestricted_stock,
        ms.quality_stock,
        ms.blocked_stock,
        ms.in_transit_stock,
        ms.unrestricted_value,
        mv.currency_id
    FROM mm_material_storage ms
    JOIN sys_storage_location s ON s.id = ms.storage_location_id
    LEFT JOIN mm_material_valuation mv ON mv.material_id = ms.material_id
        AND mv.plant_id = s.plant_id
        AND mv.valid_from <= p_snapshot_date
        AND mv.valid_to >= p_snapshot_date
    WHERE ms.tenant_id = p_tenant_id;

    GET DIAGNOSTICS v_count = ROW_COUNT;
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION generate_inventory_snapshot IS '生成库存快照';

-- ----------------------------------------------------------------------------
-- 8. 添加审计触发器
-- ----------------------------------------------------------------------------

PERFORM add_audit_trigger('mm_material');
PERFORM add_audit_trigger('mm_material_plant');
PERFORM add_audit_trigger('mm_material_valuation');
PERFORM add_audit_trigger('mm_material_storage');
PERFORM add_audit_trigger('mm_purchase_requisition_hdr');
PERFORM add_audit_trigger('mm_purchase_order_hdr');
PERFORM add_audit_trigger('mm_purchase_order_itm');
