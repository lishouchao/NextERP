# MM 模块数据库设计

**模块**: Material Management (物料管理)
**对标**: SAP ECC MM
**版本**: 1.0

---

## 1. 模块概述

### 1.1 业务范围

| 子模块 | 说明 | SAP 对标 |
|--------|------|----------|
| 物料主数据 | 物料基本信息 | MARA/MARC |
| 采购管理 | 采购申请、订单、收货 | EBAN/EKPO |
| 库存管理 | 库存移动、盘点 | MSEG/MARD |
| 供应商管理 | 供应商主数据 | LFA1/LFB1 |
| 采购定价 | 价格条件 | KONP/A004 |

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                     MM Module Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │
│  │  物料主数据  │    │  供应商主数据 │    │  采购信息记录│          │
│  │ mm_material │◄──►│  bp_vendor   │◄──►│ mm_pir      │          │
│  └─────────────┘    └─────────────┘    └─────────────┘          │
│        │                   │                   │                 │
│        │                   │                   │                 │
│        ▼                   ▼                   ▼                 │
│  ┌──────────────────────────────────────────────────────┐       │
│  │                    采购流程                           │       │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐ │       │
│  │  │采购申请 │─►│报价请求 │─►│采购订单 │─►│收货单   │ │       │
│  │  │mm_pr    │  │mm_rfq   │  │mm_po    │  │mm_gr    │ │       │
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘ │       │
│  └──────────────────────────────────────────────────────┘       │
│                                   │                              │
│                                   ▼                              │
│  ┌──────────────────────────────────────────────────────┐       │
│  │                    库存管理                           │       │
│  │  ┌─────────────┐    ┌─────────────┐                  │       │
│  │  │ 库存记录    │◄───│ 库存移动    │                  │       │
│  │  │ mm_stock    │    │ mm_movement │                  │       │
│  │  └─────────────┘    └─────────────┘                  │       │
│  └──────────────────────────────────────────────────────┘       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 物料主数据

### 2.1 物料基本信息 (mm_material)

对标 SAP MARA

```sql
CREATE TABLE mm_material (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 物料编码
    material_code   VARCHAR(18) NOT NULL,      -- 物料编号
    material_type   VARCHAR(4) NOT NULL,       -- 物料类型

    -- 描述
    description     VARCHAR(100) NOT NULL,     -- 物料描述
    description_en  VARCHAR(100),              -- 英文描述

    -- 基本单位
    base_uom        VARCHAR(3) NOT NULL,       -- 基本单位
    order_uom       VARCHAR(3),                -- 订购单位
    gr_uom          VARCHAR(3),                -- 收货单位

    -- 物料组
    material_group  VARCHAR(9),                -- 物料组
    material_group_desc VARCHAR(100),          -- 物料组描述

    -- 分类
    division        VARCHAR(2),                -- 产品组

    -- 尺寸重量
    gross_weight    DECIMAL(13,3),             -- 毛重
    net_weight      DECIMAL(13,3),             -- 净重
    weight_unit     VARCHAR(3),                -- 重量单位
    volume          DECIMAL(13,3),             -- 体积
    volume_unit     VARCHAR(3),                -- 体积单位

    -- 标识
    old_mat_no      VARCHAR(25),               -- 旧物料号
    ean_upc         VARCHAR(18),               -- EAN/UPC码
    ean_type        VARCHAR(2),                -- EAN类型

    -- 状态
    mat_status      VARCHAR(2),                -- 跨工厂状态
    -- 01:激活 02:冻结 03:删除

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, material_code)
);
```

**物料类型枚举**:

| 代码 | 类型 | 说明 |
|------|------|------|
| ROH | 原材料 | Raw Material |
| HALB | 半成品 | Semi-finished |
| FERT | 成品 | Finished Product |
| HAWA | 贸易商品 | Trading Goods |
| DIEN | 服务 | Service |
| VERP | 包装 | Packaging |
| NLAG | 非库存物料 | Non-stock Material |

### 2.2 物料工厂数据 (mm_material_plant)

对标 SAP MARC

```sql
CREATE TABLE mm_material_plant (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 关联
    material_id     UUID NOT NULL REFERENCES mm_material(id) ON DELETE CASCADE,
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    -- MRP
    mrp_type        VARCHAR(4),                -- MRP类型
    -- PD:MRP VB:按需订货 ND:无MRP
    mrp_controller  VARCHAR(3),                -- MRP控制者
    lot_size_procedure VARCHAR(2),             -- 批量过程
    -- EX: Exact Lot Size, FX: Fixed Lot Size
    min_lot_size    DECIMAL(13,3),             -- 最小批量
    max_lot_size    DECIMAL(13,3),             -- 最大批量
    reorder_point   DECIMAL(13,3),             -- 再订货点
    safety_stock    DECIMAL(13,3),             -- 安全库存

    -- 计划
    planning_time_fence INTEGER,               -- 计划时界 (天)
    planning_cycle  VARCHAR(3),                -- 计划周期

    -- 采购
    procurement_type VARCHAR(1),               -- 采购类型
    -- E:外部采购 F:自制 X:两者均可
    special_procurement VARCHAR(2),            -- 特殊采购
    purchasing_group VARCHAR(3),               -- 采购组
    planned_delivery_time INTEGER,             -- 计划交货时间 (天)
    gr_processing_time INTEGER,                -- 收货处理时间 (天)

    -- 评估
    valuation_class VARCHAR(4),                -- 评估类
    price_control   VARCHAR(1) DEFAULT 'S',    -- 价格控制
    -- S:标准价 V:移动平均价
    moving_price    DECIMAL(15,2),             -- 移动平均价
    standard_price  DECIMAL(15,2),             -- 标准价
    price_unit      INTEGER DEFAULT 1,         -- 价格单位

    -- 状态
    plant_status    VARCHAR(2),                -- 工厂状态
    -- 01:激活 02:冻结 03:删除

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (material_id, plant_id)
);
```

### 2.3 物料存储位置数据 (mm_material_sloc)

对标 SAP MARD

```sql
CREATE TABLE mm_material_sloc (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    material_id     UUID NOT NULL REFERENCES mm_material(id) ON DELETE CASCADE,
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID NOT NULL REFERENCES sys_storage_location(id),

    -- 库存
    unrestricted_qty DECIMAL(13,3) DEFAULT 0,  -- 非限制库存
    quality_inspection_qty DECIMAL(13,3) DEFAULT 0, -- 质检库存
    blocked_qty     DECIMAL(13,3) DEFAULT 0,   -- 冻结库存
    in_transit_qty  DECIMAL(13,3) DEFAULT 0,   -- 在途库存

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (material_id, plant_id, sloc_id)
);
```

---

## 3. 采购管理

### 3.1 采购申请 (mm_purchase_requisition)

对标 SAP EBAN

```sql
CREATE TABLE mm_purchase_requisition (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 申请信息
    pr_number       VARCHAR(10) NOT NULL,      -- 申请号
    pr_item         INTEGER NOT NULL,          -- 行号

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),               -- 冗余
    short_text      VARCHAR(100),              -- 短文本 (非物料时使用)

    -- 数量
    quantity        DECIMAL(13,3) NOT NULL,    -- 申请数量
    quantity_ordered DECIMAL(13,3) DEFAULT 0,  -- 已订购数量
    unit            VARCHAR(3) NOT NULL,       -- 单位

    -- 日期
    delivery_date   DATE NOT NULL,             -- 交货日期
    created_on      DATE NOT NULL DEFAULT CURRENT_DATE,

    -- 组织
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),
    purchasing_group VARCHAR(3),               -- 采购组
    purchasing_org  UUID REFERENCES sys_purchasing_org(id),

    -- 成本
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    project_id      UUID,
    asset_id        UUID,

    -- 价格
    price           DECIMAL(15,2),             -- 估价
    currency_id     UUID REFERENCES core_currency(id),

    -- 状态
    pr_status       VARCHAR(2) DEFAULT '01',   -- 01:未处理 02:处理中 03:已完成 04:已关闭
    is_closed       BOOLEAN DEFAULT FALSE,

    -- 来源
    source_type     VARCHAR(2),                -- 来源类型
    source_document VARCHAR(20),               -- 来源单据

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, pr_number, pr_item)
);
```

### 3.2 采购订单头 (mm_purchase_order_hdr)

对标 SAP EKKO

```sql
CREATE TABLE mm_purchase_order_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 订单信息
    po_number       VARCHAR(10) NOT NULL,      -- 订单号
    po_type         VARCHAR(4) NOT NULL,       -- 订单类型
    -- NB:标准 PO UB:跨公司 PO ZNB:自定义

    -- 供应商
    vendor_id       UUID NOT NULL REFERENCES bp_business_partner(id),
    vendor_code     VARCHAR(10),               -- 冗余

    -- 组织
    company_id      UUID NOT NULL REFERENCES sys_company(id),
    purchasing_org_id UUID REFERENCES sys_purchasing_org(id),
    purchasing_group VARCHAR(3),

    -- 货币
    currency_id     UUID NOT NULL REFERENCES core_currency(id),
    exchange_rate   DECIMAL(12,6) DEFAULT 1,

    -- 日期
    document_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_from      DATE,
    valid_to        DATE,

    -- 金额
    total_value     DECIMAL(15,2) DEFAULT 0,   -- 订单总值
    tax_amount      DECIMAL(15,2) DEFAULT 0,   -- 税额

    -- 条款
    payment_term    VARCHAR(4),                -- 付款条款
    incoterms       VARCHAR(3),                -- 国际贸易条件
    incoterms_loc   VARCHAR(28),               -- 地点

    -- 状态
    po_status       VARCHAR(2) DEFAULT '01',   -- 01:创建 02:已审批 03:已发货 04:已完成
    approval_status approval_status DEFAULT 'DRAFT',

    -- 参考
    reference       VARCHAR(20),
    header_text     VARCHAR(100),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, po_number)
);
```

### 3.3 采购订单项 (mm_purchase_order_itm)

对标 SAP EKPO

```sql
CREATE TABLE mm_purchase_order_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    header_id       UUID NOT NULL REFERENCES mm_purchase_order_hdr(id) ON DELETE CASCADE,
    po_number       VARCHAR(10),               -- 冗余
    po_item         INTEGER NOT NULL,          -- 行号

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    short_text      VARCHAR(100),

    -- 数量
    quantity        DECIMAL(13,3) NOT NULL,    -- 订购数量
    quantity_received DECIMAL(13,3) DEFAULT 0, -- 已收货数量
    quantity_invoiced DECIMAL(13,3) DEFAULT 0, -- 已开票数量
    unit            VARCHAR(3) NOT NULL,
    order_unit      VARCHAR(3),

    -- 价格
    net_price       DECIMAL(15,2) NOT NULL,    -- 净价
    price_unit      INTEGER DEFAULT 1,         -- 价格单位
    gross_price     DECIMAL(15,2),             -- 含税价
    discount        DECIMAL(5,2),              -- 折扣%

    -- 金额
    net_value       DECIMAL(15,2),             -- 净值
    tax_code        VARCHAR(2),                -- 税码
    tax_amount      DECIMAL(15,2),             -- 税额

    -- 组织
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),

    -- 日期
    delivery_date   DATE NOT NULL,
    statistical_delivery_date DATE,

    -- 成本
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    project_id      UUID,

    -- 删除标记
    deletion_flag   BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, po_item)
);
```

---

## 4. 库存管理

### 4.1 库存移动 (mm_material_document)

对标 SAP MSEG (物料凭证)

```sql
CREATE TABLE mm_material_document (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 凭证信息
    document_number VARCHAR(10) NOT NULL,      -- 凭证号
    document_item   INTEGER NOT NULL,          -- 行号

    -- 移动类型
    movement_type   VARCHAR(3) NOT NULL,       -- 移动类型
    -- 101:采购收货 102:采购退货 201:发货到成本中心
    -- 301:工厂间转移 311:库存地点转移 561:初始化库存
    movement_reason VARCHAR(4),                -- 移动原因

    -- 物料
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID NOT NULL REFERENCES sys_storage_location(id),

    -- 批次管理
    batch_number    VARCHAR(10),               -- 批次号

    -- 数量
    quantity        DECIMAL(13,3) NOT NULL,    -- 移动数量
    unit            VARCHAR(3) NOT NULL,       -- 单位
    entry_unit      VARCHAR(3),                -- 录入单位
    entry_quantity  DECIMAL(13,3),             -- 录入数量

    -- 金额
    amount          DECIMAL(15,2),             -- 金额
    currency_id     UUID REFERENCES core_currency(id),

    -- 来源/目标
    source_plant_id UUID,                      -- 源工厂
    source_sloc_id  UUID,                      -- 源库位
    target_plant_id UUID,                      -- 目标工厂
    target_sloc_id  UUID,                      -- 目标库位

    -- 采购订单参考
    po_id           UUID,                      -- 采购订单ID
    po_item         INTEGER,                   -- 订单行号

    -- 过账
    posting_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    document_date   DATE NOT NULL DEFAULT CURRENT_DATE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, document_number, document_item)
);

-- 按年度分区
CREATE INDEX idx_mm_mat_doc_date ON mm_material_document (posting_date);
CREATE INDEX idx_mm_mat_doc_material ON mm_material_document (material_id, plant_id);
CREATE INDEX idx_mm_mat_doc_po ON mm_material_document (po_id);
```

**移动类型枚举**:

| 代码 | 类型 | 说明 |
|------|------|------|
| 101 | 收货 | 采购订单收货 |
| 102 | 退货 | 采购订单退货 |
| 122 | 退货 | 退货至供应商 |
| 201 | 发货 | 发货到成本中心 |
| 261 | 发货 | 发货到生产订单 |
| 301 | 转移 | 工厂间转移 (一步) |
| 311 | 转移 | 库存地点转移 |
| 312 | 转移 | 库存地点转移 (反向) |
| 561 | 初始化 | 库存初始化 |

### 4.2 库存余额 (mm_stock_balance)

```sql
CREATE TABLE mm_stock_balance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 维度
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID NOT NULL REFERENCES sys_storage_location(id),
    batch_number    VARCHAR(10),               -- 批次 (可选)
    special_stock   VARCHAR(1),                -- 特殊库存标识

    -- 库存类型
    stock_type      VARCHAR(2) NOT NULL,       -- 库存类型
    -- 01:非限制 02:质检 03:冻结 04:在途

    -- 数量
    quantity        DECIMAL(13,3) DEFAULT 0,   -- 库存数量
    value           DECIMAL(15,2) DEFAULT 0,   -- 库存价值

    -- 单位
    base_unit       VARCHAR(3) NOT NULL,

    -- 评估
    valuation_type  VARCHAR(10),               -- 评估类型
    moving_price    DECIMAL(15,2),             -- 移动平均价

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, material_id, plant_id, sloc_id, batch_number, stock_type)
);
```

---

## 5. 采购信息记录

### 5.1 信息记录 (mm_pir)

对标 SAP EINA/EINE

```sql
CREATE TABLE mm_pir (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 物料供应商
    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    vendor_id       UUID NOT NULL REFERENCES bp_business_partner(id),
    vendor_code     VARCHAR(10),

    -- 组织
    purchasing_org_id UUID REFERENCES sys_purchasing_org(id),
    plant_id        UUID REFERENCES sys_plant(id),

    -- 价格
    net_price       DECIMAL(15,2),             -- 净价
    price_unit      INTEGER DEFAULT 1,         -- 价格单位
    currency_id     UUID REFERENCES core_currency(id),

    -- 条件
    min_order_qty   DECIMAL(13,3),             -- 最小订购量

    -- 交货
    planned_delivery_time INTEGER,             -- 计划交货时间 (天)

    -- 有效期
    valid_from      DATE,
    valid_to        DATE DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, material_id, vendor_id, plant_id)
);
```

---

## 6. 库存过账存储过程

### 6.1 收货过账

```sql
CREATE OR REPLACE FUNCTION mm_post_goods_receipt(
    p_po_id UUID,
    p_po_item INTEGER,
    p_quantity DECIMAL,
    p_sloc_id UUID,
    p_user_id UUID
) RETURNS UUID AS $$
DECLARE
    v_doc_id UUID;
    v_doc_number VARCHAR(10);
    v_material_id UUID;
    v_plant_id UUID;
    v_unit VARCHAR(3);
    v_price DECIMAL(15,2);
    v_po_hdr RECORD;
BEGIN
    -- 获取采购订单信息
    SELECT * INTO v_po_hdr
    FROM mm_purchase_order_hdr WHERE id = p_po_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION '采购订单不存在';
    END IF;

    -- 获取订单行信息
    SELECT material_id, plant_id, unit, net_price
    INTO v_material_id, v_plant_id, v_unit, v_price
    FROM mm_purchase_order_itm
    WHERE header_id = p_po_id AND po_item = p_po_item;

    -- 生成物料凭证号
    v_doc_number := next_val('mm_material_doc_seq');

    -- 创建物料凭证
    INSERT INTO mm_material_document (
        tenant_id, document_number, document_item,
        movement_type, material_id, plant_id, sloc_id,
        quantity, unit, amount, currency_id,
        po_id, po_item, posting_date, created_by
    ) VALUES (
        v_po_hdr.tenant_id, v_doc_number, 1,
        '101', v_material_id, v_plant_id, p_sloc_id,
        p_quantity, v_unit, p_quantity * v_price, v_po_hdr.currency_id,
        p_po_id, p_po_item, CURRENT_DATE, p_user_id
    ) RETURNING id INTO v_doc_id;

    -- 更新库存余额
    INSERT INTO mm_stock_balance (
        tenant_id, material_id, plant_id, sloc_id, stock_type,
        quantity, value, base_unit
    ) VALUES (
        v_po_hdr.tenant_id, v_material_id, v_plant_id, p_sloc_id, '01',
        p_quantity, p_quantity * v_price, v_unit
    )
    ON CONFLICT (tenant_id, material_id, plant_id, sloc_id, batch_number, stock_type)
    DO UPDATE SET
        quantity = mm_stock_balance.quantity + p_quantity,
        value = mm_stock_balance.value + p_quantity * v_price,
        updated_at = CURRENT_TIMESTAMP,
        version = mm_stock_balance.version + 1;

    -- 更新采购订单已收货数量
    UPDATE mm_purchase_order_itm
    SET quantity_received = quantity_received + p_quantity
    WHERE header_id = p_po_id AND po_item = p_po_item;

    RETURN v_doc_id;
END;
$$ LANGUAGE plpgsql;
```

### 6.2 库存转移

```sql
CREATE OR REPLACE FUNCTION mm_post_stock_transfer(
    p_material_id UUID,
    p_from_plant_id UUID,
    p_from_sloc_id UUID,
    p_to_plant_id UUID,
    p_to_sloc_id UUID,
    p_quantity DECIMAL,
    p_unit VARCHAR,
    p_user_id UUID
) RETURNS UUID AS $$
DECLARE
    v_doc_id UUID;
    v_doc_number VARCHAR(10);
    v_tenant_id UUID;
    v_price DECIMAL(15,2);
BEGIN
    -- 获取租户和价格
    SELECT tenant_id, COALESCE(moving_price, standard_price)
    INTO v_tenant_id, v_price
    FROM mm_material_plant
    WHERE material_id = p_material_id AND plant_id = p_from_plant_id;

    -- 生成凭证号
    v_doc_number := next_val('mm_material_doc_seq');

    -- 创建物料凭证 (311 移动类型)
    INSERT INTO mm_material_document (
        tenant_id, document_number, document_item,
        movement_type, material_id,
        plant_id, sloc_id,
        source_plant_id, source_sloc_id,
        target_plant_id, target_sloc_id,
        quantity, unit, amount,
        posting_date, created_by
    ) VALUES (
        v_tenant_id, v_doc_number, 1,
        '311', p_material_id,
        p_from_plant_id, p_from_sloc_id,
        p_from_plant_id, p_from_sloc_id,
        p_to_plant_id, p_to_sloc_id,
        p_quantity, p_unit, p_quantity * v_price,
        CURRENT_DATE, p_user_id
    ) RETURNING id INTO v_doc_id;

    -- 源库位扣减
    UPDATE mm_stock_balance
    SET quantity = quantity - p_quantity,
        value = value - p_quantity * v_price,
        updated_at = CURRENT_TIMESTAMP
    WHERE material_id = p_material_id
      AND plant_id = p_from_plant_id
      AND sloc_id = p_from_sloc_id
      AND stock_type = '01';

    -- 目标库位增加
    INSERT INTO mm_stock_balance (
        tenant_id, material_id, plant_id, sloc_id, stock_type,
        quantity, value, base_unit
    ) VALUES (
        v_tenant_id, p_material_id, p_to_plant_id, p_to_sloc_id, '01',
        p_quantity, p_quantity * v_price, p_unit
    )
    ON CONFLICT (tenant_id, material_id, plant_id, sloc_id, batch_number, stock_type)
    DO UPDATE SET
        quantity = mm_stock_balance.quantity + p_quantity,
        value = mm_stock_balance.value + p_quantity * v_price,
        updated_at = CURRENT_TIMESTAMP;

    RETURN v_doc_id;
END;
$$ LANGUAGE plpgsql;
```

---

## 7. 发票校验 (LIV)

对标 SAP MM-LIV (Logistics Invoice Verification)

### 7.1 发票凭证头 (mm_invoice_hdr)

对标 SAP RBKP

```sql
CREATE TABLE mm_invoice_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 凭证信息
    invoice_number  VARCHAR(10) NOT NULL,           -- 发票凭证号
    fiscal_year     INTEGER NOT NULL,               -- 会计年度

    -- 发票类型
    invoice_type    VARCHAR(2) NOT NULL,            -- RE:发票 G2:贷项凭证
    document_date   DATE NOT NULL,                  -- 凭证日期
    posting_date    DATE NOT NULL,                  -- 过账日期

    -- 供应商
    vendor_id       UUID NOT NULL REFERENCES bp_business_partner(id),
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 货币
    currency_id     UUID NOT NULL REFERENCES core_currency(id),
    exchange_rate   DECIMAL(12,6) DEFAULT 1,

    -- 供应商发票参考
    supplier_invoice VARCHAR(16),                   -- 供应商发票号
    supplier_invoice_date DATE,                     -- 供应商发票日期

    -- 金额
    gross_amount    DECIMAL(15,2),                  -- 总金额
    net_amount      DECIMAL(15,2),                  -- 净金额
    tax_amount      DECIMAL(15,2),                  -- 税额
    discount_amount DECIMAL(14,2),                  -- 折扣金额

    -- 付款条款
    payment_term    VARCHAR(4),                     -- 付款条款
    baseline_date   DATE,                           -- 基准日期
    due_date        DATE,                           -- 到期日

    -- 冻结
    blocking_reason VARCHAR(2),                     -- 冻结原因
    -- A:价格差异 B:数量差异 C:金额差异 D:日期差异 E:质量 F:手工
    blocked         BOOLEAN DEFAULT FALSE,

    -- 状态
    invoice_status  VARCHAR(2) DEFAULT '01',        -- 01:创建 02:已校验 03:已过账
    payment_status  VARCHAR(1) DEFAULT 'A',         -- A:未付 B:部分 C:已付

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    posted_at       TIMESTAMP,
    posted_by       UUID,

    UNIQUE (tenant_id, invoice_number, fiscal_year)
);

-- 索引
CREATE INDEX idx_mm_invoice_vendor ON mm_invoice_hdr (vendor_id);
CREATE INDEX idx_mm_invoice_posting ON mm_invoice_hdr (posting_date);
CREATE INDEX idx_mm_invoice_status ON mm_invoice_hdr (invoice_status);
```

### 7.2 发票凭证项 (mm_invoice_itm)

对标 SAP RSEG

```sql
CREATE TABLE mm_invoice_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 关联
    header_id       UUID NOT NULL REFERENCES mm_invoice_hdr(id) ON DELETE CASCADE,
    line_item       INTEGER NOT NULL,               -- 行号

    -- 采购订单参考
    po_id           UUID REFERENCES mm_purchase_order_hdr(id),
    po_item         INTEGER,                        -- 采购订单行号

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    short_text      VARCHAR(100),

    -- 数量金额
    quantity        DECIMAL(13,3),                  -- 发票数量
    unit            VARCHAR(3),                     -- 单位
    net_price       DECIMAL(15,2),                  -- 净价
    net_amount      DECIMAL(15,2),                  -- 净金额

    -- 税
    tax_code        VARCHAR(2),                     -- 税码
    tax_amount      DECIMAL(15,2),                  -- 税额

    -- 成本对象
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    profit_center_id UUID,
    gl_account_id   UUID REFERENCES fi_account(id),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, line_item)
);
```

### 7.3 GR/IR 清算 (mm_gr_ir)

跟踪收货与发票的匹配状态

```sql
CREATE TABLE mm_gr_ir (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 采购订单
    po_id           UUID NOT NULL REFERENCES mm_purchase_order_hdr(id),
    po_item         INTEGER NOT NULL,

    -- 收货凭证
    gr_document_id  UUID REFERENCES mm_material_document(id),
    gr_quantity     DECIMAL(13,3),                  -- 收货数量
    gr_amount       DECIMAL(15,2),                  -- 收货金额
    gr_date         DATE,                           -- 收货日期

    -- 发票凭证
    invoice_id      UUID REFERENCES mm_invoice_hdr(id),
    invoice_item    INTEGER,                        -- 发票行号
    ir_quantity     DECIMAL(13,3),                  -- 发票数量
    ir_amount       DECIMAL(15,2),                  -- 发票金额
    ir_date         DATE,                           -- 发票日期

    -- 清算状态
    cleared         BOOLEAN DEFAULT FALSE,          -- 是否清算
    cleared_date    DATE,                           -- 清算日期
    clearing_doc_id UUID,                           -- 清算凭证ID

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_mm_gr_ir_po ON mm_gr_ir (po_id, po_item);
CREATE INDEX idx_mm_gr_ir_gr ON mm_gr_ir (gr_document_id);
CREATE INDEX idx_mm_gr_ir_invoice ON mm_gr_ir (invoice_id);
CREATE INDEX idx_mm_gr_ir_open ON mm_gr_ir (cleared) WHERE cleared = FALSE;
```

---

## 8. 预留管理

对标 SAP RKPF/RESB

### 8.1 预留头 (mm_reservation_hdr)

```sql
CREATE TABLE mm_reservation_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 预留信息
    reservation_number VARCHAR(10) NOT NULL,        -- 预留号
    movement_type   VARCHAR(3) NOT NULL,            -- 移动类型

    -- 需求日期
    requirement_date DATE NOT NULL,

    -- 成本对象
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    order_id        UUID,                           -- 生产订单
    wbs_element     VARCHAR(24),                    -- WBS元素
    sales_order_id  UUID,                           -- 销售订单
    network_id      UUID,                           -- 网络

    -- 文本
    header_text     VARCHAR(50),

    -- 状态
    reservation_status VARCHAR(2) DEFAULT '01',     -- 01:创建 02:已批准 03:已完成 04:已关闭
    approval_status VARCHAR(1) DEFAULT 'D',         -- D:草稿 A:已批准 R:已拒绝

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    approved_by     UUID,
    approved_at     TIMESTAMP,

    UNIQUE (tenant_id, reservation_number)
);

-- 索引
CREATE INDEX idx_mm_res_cost_center ON mm_reservation_hdr (cost_center_id);
CREATE INDEX idx_mm_res_date ON mm_reservation_hdr (requirement_date);
CREATE INDEX idx_mm_res_status ON mm_reservation_hdr (reservation_status);
```

### 8.2 预留项 (mm_reservation_itm)

```sql
CREATE TABLE mm_reservation_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 关联
    header_id       UUID NOT NULL REFERENCES mm_reservation_hdr(id) ON DELETE CASCADE,
    item_number     INTEGER NOT NULL,               -- 行号

    -- 物料
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),
    batch_number    VARCHAR(10),                    -- 批次号

    -- 数量
    requirement_quantity DECIMAL(13,3) NOT NULL,    -- 需求数量
    withdrawn_quantity DECIMAL(13,3) DEFAULT 0,     -- 已提取数量
    unit            VARCHAR(3) NOT NULL,

    -- 需求日期
    requirement_date DATE,

    -- 项目文本
    item_text       VARCHAR(50),

    -- 删除标记
    deletion_flag   BOOLEAN DEFAULT FALSE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, item_number)
);

-- 索引
CREATE INDEX idx_mm_res_itm_material ON mm_reservation_itm (material_id, plant_id);
```

---

## 9. 盘点管理

对标 SAP MI01/MI04/MI07

### 9.1 盘点凭证头 (mm_physical_inventory_hdr)

```sql
CREATE TABLE mm_physical_inventory_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 凭证信息
    phys_inventory_number VARCHAR(10) NOT NULL,     -- 盘点凭证号
    fiscal_year     INTEGER NOT NULL,               -- 会计年度

    -- 组织
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),

    -- 盘点类型
    phys_inventory_type VARCHAR(1) DEFAULT '1',     -- 1:期间盘点 2:连续盘点

    -- 日期
    count_date      DATE NOT NULL,                  -- 盘点日期
    planned_count_date DATE,                        -- 计划盘点日期

    -- 库存冻结
    stock_freeze    BOOLEAN DEFAULT FALSE,          -- 库存冻结
    freeze_date     DATE,

    -- 状态
    phys_inventory_status VARCHAR(2) DEFAULT '01',  -- 01:创建 02:盘点中 03:差异 04:已过账

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    posted_at       TIMESTAMP,
    posted_by       UUID,

    UNIQUE (tenant_id, phys_inventory_number, fiscal_year)
);

-- 索引
CREATE INDEX idx_mm_pi_plant ON mm_physical_inventory_hdr (plant_id);
CREATE INDEX idx_mm_pi_date ON mm_physical_inventory_hdr (count_date);
CREATE INDEX idx_mm_pi_status ON mm_physical_inventory_hdr (phys_inventory_status);
```

### 9.2 盘点项目 (mm_physical_inventory_itm)

```sql
CREATE TABLE mm_physical_inventory_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 关联
    header_id       UUID NOT NULL REFERENCES mm_physical_inventory_hdr(id) ON DELETE CASCADE,
    item_number     INTEGER NOT NULL,               -- 行号

    -- 物料
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    batch_number    VARCHAR(10),                    -- 批次号
    special_stock   VARCHAR(1),                     -- 特殊库存

    -- 数量
    book_quantity   DECIMAL(13,3),                  -- 账面数量
    counted_quantity DECIMAL(13,3),                 -- 实盘数量
    difference_quantity DECIMAL(13,3) GENERATED ALWAYS AS (
        COALESCE(counted_quantity, 0) - COALESCE(book_quantity, 0)
    ) STORED,                                       -- 差异数量
    unit            VARCHAR(3) NOT NULL,

    -- 价值
    book_value      DECIMAL(15,2),                  -- 账面价值
    difference_value DECIMAL(15,2),                 -- 差异价值
    currency_id     UUID REFERENCES core_currency(id),

    -- 重盘
    recount_required BOOLEAN DEFAULT FALSE,         -- 是否需要重盘
    recount_quantity DECIMAL(13,3),                 -- 重盘数量
    recount_reason  VARCHAR(4),                     -- 重盘原因

    -- 盘点人
    counted_by      UUID,
    counted_date    DATE,

    -- 过账状态
    posted          BOOLEAN DEFAULT FALSE,
    posted_date     DATE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, item_number)
);

-- 索引
CREATE INDEX idx_mm_pi_itm_material ON mm_physical_inventory_itm (material_id);
CREATE INDEX idx_mm_pi_itm_diff ON mm_physical_inventory_itm (difference_quantity)
    WHERE difference_quantity != 0;
```

---

## 10. 合同与货源清单

### 10.1 合同头 (mm_contract_hdr)

对标 SAP EKKO - 合同类型

```sql
CREATE TABLE mm_contract_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 合同信息
    contract_number VARCHAR(10) NOT NULL,           -- 合同号
    contract_type   VARCHAR(4) NOT NULL,            -- 合同类型
    -- WK:数量合同 MK:价值合同

    -- 供应商
    vendor_id       UUID NOT NULL REFERENCES bp_business_partner(id),
    vendor_code     VARCHAR(10),

    -- 组织
    purchasing_org_id UUID REFERENCES sys_purchasing_org(id),
    purchasing_group VARCHAR(3),

    -- 有效期
    validity_start  DATE NOT NULL,
    validity_end    DATE NOT NULL,

    -- 目标金额/数量
    target_value    DECIMAL(15,2),                  -- 目标价值
    released_value  DECIMAL(15,2) DEFAULT 0,        -- 已释放价值
    currency_id     UUID REFERENCES core_currency(id),

    -- 条款
    payment_term    VARCHAR(4),
    incoterms       VARCHAR(3),
    incoterms_loc   VARCHAR(28),

    -- 状态
    contract_status VARCHAR(2) DEFAULT '01',        -- 01:激活 02:冻结 03:过期
    approval_status VARCHAR(1) DEFAULT 'D',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, contract_number)
);

-- 索引
CREATE INDEX idx_mm_contract_vendor ON mm_contract_hdr (vendor_id);
CREATE INDEX idx_mm_contract_valid ON mm_contract_hdr (validity_start, validity_end);
```

### 10.2 合同项 (mm_contract_itm)

```sql
CREATE TABLE mm_contract_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 关联
    header_id       UUID NOT NULL REFERENCES mm_contract_hdr(id) ON DELETE CASCADE,
    contract_item   INTEGER NOT NULL,

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    short_text      VARCHAR(100),

    -- 目标数量
    target_quantity DECIMAL(13,3),                  -- 目标数量
    released_quantity DECIMAL(13,3) DEFAULT 0,      -- 已释放数量
    unit            VARCHAR(3),

    -- 价格
    net_price       DECIMAL(15,2),
    price_unit      INTEGER DEFAULT 1,
    currency_id     UUID REFERENCES core_currency(id),

    -- 工厂
    plant_id        UUID REFERENCES sys_plant(id),

    -- 删除标记
    deletion_flag   BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, contract_item)
);
```

### 10.3 货源清单 (mm_source_list)

对标 SAP EORD

```sql
CREATE TABLE mm_source_list (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 物料
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    -- 来源
    vendor_id       UUID REFERENCES bp_business_partner(id),
    contract_id     UUID REFERENCES mm_contract_hdr(id),
    pir_id          UUID REFERENCES mm_pir(id),

    -- 有效期
    validity_start  DATE NOT NULL,
    validity_end    DATE NOT NULL DEFAULT '9999-12-31',

    -- 优先级
    priority        INTEGER DEFAULT 1,              -- 1-9, 1最高

    -- 冻结
    blocked         BOOLEAN DEFAULT FALSE,
    block_reason    VARCHAR(4),

    -- 审计
    status          general_status DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (material_id, plant_id, vendor_id, validity_start)
);

-- 索引
CREATE INDEX idx_mm_source_material ON mm_source_list (material_id, plant_id);
CREATE INDEX idx_mm_source_vendor ON mm_source_list (vendor_id);
```

---

## 11. 物料销售视图

对标 SAP MVKE

### 11.1 物料销售数据 (mm_material_sales)

```sql
CREATE TABLE mm_material_sales (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 关联
    material_id     UUID NOT NULL REFERENCES mm_material(id) ON DELETE CASCADE,

    -- 销售组织
    sales_org_id    UUID NOT NULL REFERENCES sys_sales_org(id),
    distribution_channel VARCHAR(2),                -- 分销渠道

    -- 发货工厂
    delivering_plant_id UUID REFERENCES sys_plant(id),

    -- 销售单位
    sales_unit      VARCHAR(3),                     -- 销售单位
    min_order_qty   DECIMAL(13,3),                  -- 最小订单量
    min_deliv_qty   DECIMAL(13,3),                  -- 最小发货量
    max_deliv_qty   DECIMAL(13,3),                  -- 最大发货量

    -- 定价
    pricing_group   VARCHAR(2),                     -- 定价组
    price_group     VARCHAR(2),                     -- 价格组
    cust_price_group VARCHAR(2),                    -- 客户价格组
    material_pricing_group VARCHAR(2),              -- 物料定价组

    -- 项目类别
    item_category_group VARCHAR(4),                 -- 项目类别组
    gen_item_cat_group VARCHAR(4),                  -- 通用项目类别组

    -- 科目分配
    account_assignment_group VARCHAR(2),            -- 科目分配组

    -- 产品层次
    product_hierarchy VARCHAR(18),                  -- 产品层次
    material_group_1 VARCHAR(3),                    -- 物料组1-5
    material_group_2 VARCHAR(3),
    material_group_3 VARCHAR(3),
    material_group_4 VARCHAR(3),
    material_group_5 VARCHAR(3),

    -- 税
    tax_category    VARCHAR(1),                     -- 税类别
    tax_classification VARCHAR(1),                  -- 税分类

    -- 状态
    sales_status    VARCHAR(1) DEFAULT 'A',         -- A:可销售 B:冻结
    valid_from      DATE,
    valid_to        DATE DEFAULT '9999-12-31',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (material_id, sales_org_id, distribution_channel)
);

-- 索引
CREATE INDEX idx_mm_mat_sales_org ON mm_material_sales (sales_org_id);
CREATE INDEX idx_mm_mat_sales_material ON mm_material_sales (material_id);
```

---

## 12. 报表视图

### 12.1 库存明细视图

```sql
CREATE VIEW v_mm_stock_detail AS
SELECT
    s.tenant_id,
    s.material_id,
    m.material_code,
    m.description AS material_desc,
    m.material_type,
    m.base_unit,
    s.plant_id,
    p.plant_code,
    p.plant_name,
    s.sloc_id,
    sl.sloc_code,
    sl.sloc_name,
    s.stock_type,
    s.quantity,
    s.value,
    CASE WHEN s.quantity > 0
         THEN s.value / s.quantity
         ELSE 0 END AS unit_price,
    s.batch_number
FROM mm_stock_balance s
JOIN mm_material m ON m.id = s.material_id
JOIN sys_plant p ON p.id = s.plant_id
JOIN sys_storage_location sl ON sl.id = s.sloc_id
WHERE s.quantity > 0;
```

### 12.2 采购订单跟踪视图

```sql
CREATE VIEW v_mm_po_tracking AS
SELECT
    h.po_number,
    h.document_date,
    h.vendor_id,
    v.name AS vendor_name,
    i.po_item,
    i.material_code,
    i.short_text,
    i.quantity,
    i.unit,
    i.quantity_received,
    i.quantity_invoiced,
    i.quantity - i.quantity_received AS open_quantity,
    i.net_price,
    i.net_value,
    i.delivery_date,
    h.po_status
FROM mm_purchase_order_hdr h
JOIN mm_purchase_order_itm i ON i.header_id = h.id
LEFT JOIN bp_business_partner v ON v.id = h.vendor_id
WHERE i.deletion_flag = FALSE;
```

### 12.3 GR/IR 分析视图

```sql
CREATE VIEW v_mm_gr_ir_analysis AS
SELECT
    gri.tenant_id,
    gri.po_id,
    po.po_number,
    gri.po_item,
    gri.gr_document_id,
    gri.gr_quantity,
    gri.gr_amount,
    gri.gr_date,
    gri.invoice_id,
    inv.invoice_number,
    gri.ir_quantity,
    gri.ir_amount,
    gri.ir_date,
    gri.gr_quantity - COALESCE(gri.ir_quantity, 0) AS open_quantity,
    gri.gr_amount - COALESCE(gri.ir_amount, 0) AS open_amount,
    gri.cleared,
    gri.cleared_date
FROM mm_gr_ir gri
LEFT JOIN mm_purchase_order_hdr po ON po.id = gri.po_id
LEFT JOIN mm_invoice_hdr inv ON inv.id = gri.invoice_id
WHERE gri.cleared = FALSE;
```

---

## 13. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-10 | 初始版本 - 物料主数据、采购管理、库存管理、采购信息记录 |
| 1.1 | 2026-03-14 | 补充发票校验、预留管理、盘点管理、合同与货源清单、物料销售视图表 |
