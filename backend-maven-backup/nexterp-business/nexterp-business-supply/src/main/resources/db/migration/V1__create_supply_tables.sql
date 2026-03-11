-- ==============================================================
-- 供应链模块 - 数据库变更脚本
-- 创建时间: 2025-01-15
-- 说明: 供应商、物料相关表结构
-- ==============================================================

-- 1. 创建供应商表
-- ==============================================================
CREATE TABLE sup_supplier (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(100) NOT NULL,
    short_name VARCHAR(50),
    supplier_type INT NOT NULL DEFAULT 1,
    category_id BIGINT,
    category_name VARCHAR(50),
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    contact_mobile VARCHAR(20),
    contact_email VARCHAR(100),
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    address VARCHAR(200),
    tax_no VARCHAR(50),
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    credit_limit DECIMAL(19,2) DEFAULT 0,
    credit_days INT DEFAULT 0,
    payment_terms VARCHAR(50),
    currency VARCHAR(10),
    delivery_days INT DEFAULT 7,
    minimum_order_qty INT,
    quality_level VARCHAR(20),
    qualified_rate DECIMAL(5,2) DEFAULT 100.00,
    on_time_delivery_rate DECIMAL(5,2) DEFAULT 100.00,
    status INT NOT NULL DEFAULT 1,
    cooperation_start_date DATE,
    last_purchase_date DATE,
    total_purchase_amount DECIMAL(19,2) DEFAULT 0,
    purchase_count INT DEFAULT 0,
    remark VARCHAR(500),
    attachments TEXT,
    custom_field1 VARCHAR(100),
    custom_field2 VARCHAR(100),
    custom_field3 VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sup_supplier IS '供应商表';
COMMENT ON COLUMN sup_supplier.supplier_code IS '供应商编码';
COMMENT ON COLUMN sup_supplier.supplier_name IS '供应商名称';
COMMENT ON COLUMN sup_supplier.short_name IS '供应商简称';
COMMENT ON COLUMN sup_supplier.supplier_type IS '供应商类型 (1-一般供应商 2-重点供应商 3-战略供应商)';
COMMENT ON COLUMN sup_supplier.category_id IS '供应商分类ID';
COMMENT ON COLUMN sup_supplier.contact_person IS '联系人';
COMMENT ON COLUMN sup_supplier.contact_phone IS '联系电话';
COMMENT ON COLUMN sup_supplier.contact_mobile IS '联系手机';
COMMENT ON COLUMN sup_supplier.contact_email IS '联系邮箱';
COMMENT ON COLUMN sup_supplier.tax_no IS '纳税人识别号';
COMMENT ON COLUMN sup_supplier.bank_name IS '开户银行';
COMMENT ON COLUMN sup_supplier.bank_account IS '银行账号';
COMMENT ON COLUMN sup_supplier.credit_limit IS '信用额度';
COMMENT ON COLUMN sup_supplier.credit_days IS '信用期限(天)';
COMMENT ON COLUMN sup_supplier.payment_terms IS '付款条件';
COMMENT ON COLUMN sup_supplier.delivery_days IS '交货周期(天)';
COMMENT ON COLUMN sup_supplier.minimum_order_qty IS '最小起订量';
COMMENT ON COLUMN sup_supplier.quality_level IS '质量等级';
COMMENT ON COLUMN sup_supplier.qualified_rate IS '合格率(%)';
COMMENT ON COLUMN sup_supplier.on_time_delivery_rate IS '准时交货率(%)';
COMMENT ON COLUMN sup_supplier.status IS '状态 (0-禁用 1-启用)';

CREATE UNIQUE INDEX uk_supplier_code ON sup_supplier(supplier_code, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_supplier_type ON sup_supplier(supplier_type, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_supplier_category ON sup_supplier(category_id, tenant_id) WHERE is_deleted = FALSE;

-- 2. 创建物料表
-- ==============================================================
CREATE TABLE inv_material (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(100) NOT NULL,
    short_name VARCHAR(50),
    material_type INT NOT NULL DEFAULT 1,
    category_id BIGINT,
    category_name VARCHAR(50),
    specification VARCHAR(200),
    model VARCHAR(100),
    brand VARCHAR(100),
    unit_id BIGINT,
    unit_name VARCHAR(20),
    warehouse_id BIGINT,
    warehouse_name VARCHAR(50),
    location VARCHAR(50),
    min_stock DECIMAL(19,4),
    max_stock DECIMAL(19,4),
    safety_stock DECIMAL(19,4),
    lead_time INT DEFAULT 0,
    cycle_days INT DEFAULT 0,
    abc_category CHAR(1),
    is_batch_managed BOOLEAN DEFAULT FALSE,
    is_serial_managed BOOLEAN DEFAULT FALSE,
    is_shelf_life_managed BOOLEAN DEFAULT FALSE,
    shelf_life INT,
    shelf_life_unit VARCHAR(10),
    purchase_price DECIMAL(19,4),
    sale_price DECIMAL(19,4),
    cost_price DECIMAL(19,4),
    tax_rate DECIMAL(5,2) DEFAULT 0.00,
    barcode VARCHAR(50),
    image_url VARCHAR(500),
    attachments TEXT,
    status INT NOT NULL DEFAULT 1,
    remark VARCHAR(500),
    custom_field1 VARCHAR(100),
    custom_field2 VARCHAR(100),
    custom_field3 VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE inv_material IS '物料表';
COMMENT ON COLUMN inv_material.material_code IS '物料编码';
COMMENT ON COLUMN inv_material.material_name IS '物料名称';
COMMENT ON COLUMN inv_material.short_name IS '物料简称';
COMMENT ON COLUMN inv_material.material_type IS '物料类型 (1-原材料 2-半成品 3-成品 4-商品)';
COMMENT ON COLUMN inv_material.category_id IS '物料分类ID';
COMMENT ON COLUMN inv_material.specification IS '规格型号';
COMMENT ON COLUMN inv_material.model IS '型号';
COMMENT ON COLUMN inv_material.brand IS '品牌';
COMMENT ON COLUMN inv_material.unit_id IS '计量单位ID';
COMMENT ON COLUMN inv_material.unit_name IS '计量单位名称';
COMMENT ON COLUMN inv_material.warehouse_id IS '默认仓库ID';
COMMENT ON COLUMN inv_material.location IS '默认货位';
COMMENT ON COLUMN inv_material.min_stock IS '最小库存';
COMMENT ON COLUMN inv_material.max_stock IS '最大库存';
COMMENT ON COLUMN inv_material.safety_stock IS '安全库存';
COMMENT ON COLUMN inv_material.lead_time IS '采购提前期(天)';
COMMENT ON COLUMN inv_material.cycle_days IS '生产周期(天)';
COMMENT ON COLUMN inv_material.abc_category IS 'ABC分类 (A-高价值 B-中价值 C-低价值)';
COMMENT ON COLUMN inv_material.is_batch_managed IS '是否批次管理';
COMMENT ON COLUMN inv_material.is_serial_managed IS '是否序列号管理';
COMMENT ON COLUMN inv_material.is_shelf_life_managed IS '是否保质期管理';
COMMENT ON COLUMN inv_material.shelf_life IS '保质期';
COMMENT ON COLUMN inv_material.shelf_life_unit IS '保质期单位';
COMMENT ON COLUMN inv_material.purchase_price IS '采购价格';
COMMENT ON COLUMN inv_material.sale_price IS '销售价格';
COMMENT ON COLUMN inv_material.cost_price IS '成本价格';
COMMENT ON COLUMN inv_material.tax_rate IS '税率(%)';
COMMENT ON COLUMN inv_material.barcode IS '条形码';
COMMENT ON COLUMN inv_material.image_url IS '图片URL';
COMMENT ON COLUMN inv_material.status IS '状态 (0-禁用 1-启用)';

CREATE UNIQUE INDEX uk_material_code ON inv_material(material_code, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_material_type ON inv_material(material_type, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_material_category ON inv_material(category_id, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_material_barcode ON inv_material(barcode) WHERE is_deleted = FALSE;

-- 3. 创建客户表
-- ==============================================================
CREATE TABLE sal_customer (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    customer_code VARCHAR(50) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    short_name VARCHAR(50),
    customer_type INT NOT NULL DEFAULT 1,
    category_id BIGINT,
    category_name VARCHAR(50),
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    contact_mobile VARCHAR(20),
    contact_email VARCHAR(100),
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    address VARCHAR(200),
    tax_no VARCHAR(50),
    bank_name VARCHAR(100),
    bank_account VARCHAR(50),
    credit_limit DECIMAL(19,2) DEFAULT 0,
    credit_days INT DEFAULT 0,
    payment_terms VARCHAR(50),
    currency VARCHAR(10),
    delivery_terms VARCHAR(50),
    sales_person_id BIGINT,
    sales_person_name VARCHAR(50),
    status INT NOT NULL DEFAULT 1,
    cooperation_start_date DATE,
    last_sale_date DATE,
    total_sale_amount DECIMAL(19,2) DEFAULT 0,
    sale_count INT DEFAULT 0,
    remark VARCHAR(500),
    attachments TEXT,
    custom_field1 VARCHAR(100),
    custom_field2 VARCHAR(100),
    custom_field3 VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE sal_customer IS '客户表';
COMMENT ON COLUMN sal_customer.customer_code IS '客户编码';
COMMENT ON COLUMN sal_customer.customer_name IS '客户名称';
COMMENT ON COLUMN sal_customer.short_name IS '客户简称';
COMMENT ON COLUMN sal_customer.customer_type IS '客户类型 (1-一般客户 2-重点客户 3-战略客户)';
COMMENT ON COLUMN sal_customer.category_id IS '客户分类ID';
COMMENT ON COLUMN sal_customer.contact_person IS '联系人';
COMMENT ON COLUMN sal_customer.contact_phone IS '联系电话';
COMMENT ON COLUMN sal_customer.contact_mobile IS '联系手机';
COMMENT ON COLUMN sal_customer.contact_email IS '联系邮箱';
COMMENT ON COLUMN sal_customer.tax_no IS '纳税人识别号';
COMMENT ON COLUMN sal_customer.bank_name IS '开户银行';
COMMENT ON COLUMN sal_customer.bank_account IS '银行账号';
COMMENT ON COLUMN sal_customer.credit_limit IS '信用额度';
COMMENT ON COLUMN sal_customer.credit_days IS '信用期限(天)';
COMMENT ON COLUMN sal_customer.payment_terms IS '付款条件';
COMMENT ON COLUMN sal_customer.delivery_terms IS '交货条件';
COMMENT ON COLUMN sal_customer.sales_person_id IS '销售员ID';
COMMENT ON COLUMN sal_customer.sales_person_name IS '销售员姓名';
COMMENT ON COLUMN sal_customer.status IS '状态 (0-禁用 1-启用)';

CREATE UNIQUE INDEX uk_customer_code ON sal_customer(customer_code, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_customer_type ON sal_customer(customer_type, tenant_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_customer_category ON sal_customer(category_id, tenant_id) WHERE is_deleted = FALSE;

-- 4. 插入示例数据
-- ==============================================================
INSERT INTO sup_supplier (tenant_id, supplier_code, supplier_name, short_name, supplier_type, status)
VALUES
(0, 'S001', '示例供应商A', '供应商A', 1, 1),
(0, 'S002', '示例供应商B', '供应商B', 2, 1);

INSERT INTO inv_material (tenant_id, material_code, material_name, short_name, material_type, unit_name, status)
VALUES
(0, 'M001', '示例物料A', '物料A', 1, '个', 1),
(0, 'M002', '示例物料B', '物料B', 2, 'kg', 1);

INSERT INTO sal_customer (tenant_id, customer_code, customer_name, short_name, customer_type, status)
VALUES
(0, 'C001', '示例客户A', '客户A', 1, 1),
(0, 'C002', '示例客户B', '客户B', 2, 1);
