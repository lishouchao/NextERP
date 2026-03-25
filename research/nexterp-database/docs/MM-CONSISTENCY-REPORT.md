# MM 模块一致性检查报告

**检查日期**: 2026-03-14
**检查范围**: MM 功能设计文档 vs 数据库设计文档
**状态**: ✅ 已同步 (高/中优先级项已完成)

---

## 1. 检查结果摘要

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 物料主数据 | ✅ 已补充 | 已添加销售视图表 |
| 采购申请 | ✅ 基本一致 | 结构匹配 |
| 采购订单 | ✅ 基本一致 | 结构匹配 |
| 框架协议 | ✅ 已补充 | 已添加合同表 |
| 询价报价 | ⏸️ 待定 | 低优先级，暂不实现 |
| 货源清单 | ✅ 已补充 | 已添加货源清单表 |
| 库存移动 | ✅ 基本一致 | 建议分离凭证头表(可选) |
| 库存余额 | ✅ 基本一致 | 结构匹配 |
| 预留管理 | ✅ 已补充 | 已添加预留表 |
| 盘点管理 | ✅ 已补充 | 已添加盘点表 |
| 发票校验 | ✅ 已补充 | 已添加发票校验和GR/IR表 |
| 仓库管理 | ⏸️ 待定 | 低优先级，暂不实现 |

---

## 2. 详细对比分析

### 2.1 物料主数据

#### 数据库设计存在的表

| 表名 | 说明 | 状态 |
|------|------|------|
| mm_material | 物料基本数据 | ✅ 存在 |
| mm_material_plant | 物料工厂数据 | ✅ 存在 |
| mm_material_sloc | 物料存储位置数据 | ✅ 存在 |

#### 功能设计定义的表

| 表名 | 说明 | 状态 |
|------|------|------|
| 物料基本数据 (MARA) | 跨客户端数据 | ✅ 对应 mm_material |
| 物料工厂数据 (MARC) | 工厂层级数据 | ✅ 对应 mm_material_plant |
| 物料销售数据 (MVKE) | 销售组织+分销渠道 | ❌ 缺失 |
| 物料评估数据 (MBEW) | 评估范围数据 | ⚠️ 部分在 mm_material_plant |
| 批次主数据 (MCHA) | 批次信息 | ⚠️ 字段存在但无独立表 |

#### 建议新增表

```sql
-- 物料销售数据 (对标 SAP MVKE)
CREATE TABLE mm_material_sales (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 关联
    material_id     UUID NOT NULL REFERENCES mm_material(id) ON DELETE CASCADE,

    -- 销售组织
    sales_org_id    UUID NOT NULL REFERENCES sys_sales_org(id),
    distribution_channel VARCHAR(2),               -- 分销渠道

    -- 销售数据
    delivering_plant_id UUID REFERENCES sys_plant(id),
    sales_unit      VARCHAR(3),                    -- 销售单位
    min_order_qty   DECIMAL(13,3),                 -- 最小订单量
    min_deliv_qty   DECIMAL(13,3),                 -- 最小发货量

    -- 定价
    pricing_group   VARCHAR(2),                    -- 定价组
    price_group     VARCHAR(2),                    -- 价格组
    item_category_group VARCHAR(4),                -- 项目类别组
    account_assignment_group VARCHAR(2),           -- 科目分配组

    -- 产品层次
    product_hierarchy VARCHAR(18),                 -- 产品层次
    material_group_1 VARCHAR(3),                   -- 物料组1-5
    material_group_2 VARCHAR(3),
    material_group_3 VARCHAR(3),
    material_group_4 VARCHAR(3),
    material_group_5 VARCHAR(3),

    -- 状态
    sales_status    VARCHAR(1),                    -- 销售状态
    valid_from      DATE,
    valid_to        DATE DEFAULT '9999-12-31',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (material_id, sales_org_id, distribution_channel)
);
```

---

### 2.2 采购管理

#### 数据库设计存在的表

| 表名 | 说明 | 状态 |
|------|------|------|
| mm_purchase_requisition | 采购申请 | ✅ 存在 |
| mm_purchase_order_hdr | 采购订单头 | ✅ 存在 |
| mm_purchase_order_itm | 采购订单项 | ✅ 存在 |
| mm_pir | 采购信息记录 | ✅ 存在 |

#### 功能设计定义但缺失的表

| 表名 | 说明 | 状态 |
|------|------|------|
| mm_contract | 合同/框架协议 | ❌ 缺失 |
| mm_scheduling_agreement | 计划协议 | ❌ 缺失 |
| mm_rfq | 询价报价 | ❌ 缺失 |
| mm_source_list | 货源清单 | ❌ 缺失 |

#### 建议新增表

```sql
-- 合同/框架协议头 (对标 SAP EKKO - 合同类型)
CREATE TABLE mm_contract_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    contract_number VARCHAR(10) NOT NULL,          -- 合同号
    contract_type   VARCHAR(4) NOT NULL,           -- 合同类型 (WK/MK)
    vendor_id       UUID NOT NULL REFERENCES bp_business_partner(id),

    -- 组织
    purchasing_org_id UUID REFERENCES sys_purchasing_org(id),
    purchasing_group VARCHAR(3),

    -- 有效期
    validity_start  DATE NOT NULL,
    validity_end    DATE NOT NULL,

    -- 金额
    target_value    DECIMAL(15,2),                 -- 目标价值
    released_value  DECIMAL(15,2) DEFAULT 0,       -- 已释放价值
    currency_id     UUID REFERENCES core_currency(id),

    -- 条款
    payment_term    VARCHAR(4),
    incoterms       VARCHAR(3),
    incoterms_loc   VARCHAR(28),

    -- 状态
    contract_status VARCHAR(2) DEFAULT '01',        -- 01:激活 02:冻结 03:过期

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, contract_number)
);

-- 合同项
CREATE TABLE mm_contract_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    header_id       UUID NOT NULL REFERENCES mm_contract_hdr(id) ON DELETE CASCADE,
    contract_item   INTEGER NOT NULL,

    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    short_text      VARCHAR(100),

    target_quantity DECIMAL(13,3),                  -- 目标数量
    released_quantity DECIMAL(13,3) DEFAULT 0,      -- 已释放数量
    unit            VARCHAR(3),

    net_price       DECIMAL(15,2),
    price_unit      INTEGER DEFAULT 1,

    plant_id        UUID REFERENCES sys_plant(id),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, contract_item)
);

-- 货源清单 (对标 SAP EORD)
CREATE TABLE mm_source_list (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    material_id     UUID NOT NULL REFERENCES mm_material(id),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    vendor_id       UUID REFERENCES bp_business_partner(id),
    contract_id     UUID REFERENCES mm_contract_hdr(id),
    pir_id          UUID REFERENCES mm_pir(id),

    validity_start  DATE NOT NULL,
    validity_end    DATE NOT NULL DEFAULT '9999-12-31',

    priority        INTEGER DEFAULT 1,              -- 优先级
    blocked         BOOLEAN DEFAULT FALSE,          -- 冻结

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (material_id, plant_id, vendor_id, validity_start)
);

-- 询价报价 (对标 SAP ME41/ME47)
CREATE TABLE mm_rfq_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    rfq_number      VARCHAR(10) NOT NULL,           -- 询价号
    rfq_type        VARCHAR(4) DEFAULT 'AN',        -- 询价类型

    purchasing_org_id UUID REFERENCES sys_purchasing_org(id),
    purchasing_group VARCHAR(3),

    quotation_deadline DATE,                        -- 报价截止日
    document_date   DATE NOT NULL DEFAULT CURRENT_DATE,

    status          VARCHAR(2) DEFAULT '01',        -- 01:创建 02:已发出 03:已收到 04:已关闭

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, rfq_number)
);

CREATE TABLE mm_rfq_vendor (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    rfq_id          UUID NOT NULL REFERENCES mm_rfq_hdr(id) ON DELETE CASCADE,
    vendor_id       UUID NOT NULL REFERENCES bp_business_partner(id),

    quotation_number VARCHAR(10),                   -- 供应商报价单号
    quotation_date  DATE,

    total_value     DECIMAL(15,2),                  -- 报价总值
    currency_id     UUID REFERENCES core_currency(id),

    -- 评分
    price_score     DECIMAL(3,1),
    delivery_score  DECIMAL(3,1),
    quality_score   DECIMAL(3,1),
    total_score     DECIMAL(3,1),

    status          VARCHAR(2),                     -- 报价状态

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (rfq_id, vendor_id)
);
```

---

### 2.3 库存管理

#### 数据库设计存在的表

| 表名 | 说明 | 状态 |
|------|------|------|
| mm_material_document | 物料凭证 | ⚠️ 存在，建议分离头/项 |
| mm_stock_balance | 库存余额 | ✅ 存在 |

#### 功能设计定义但缺失的表

| 表名 | 说明 | 状态 |
|------|------|------|
| mm_material_document_hdr | 物料凭证头 | ⚠️ 建议分离 |
| mm_reservation | 预留头 | ❌ 缺失 |
| mm_reservation_itm | 预留项 | ❌ 缺失 |
| mm_physical_inventory | 盘点凭证 | ❌ 缺失 |
| mm_phys_inv_item | 盘点项目 | ❌ 缺失 |

#### 建议新增表

```sql
-- 物料凭证头 (对标 SAP MKPF) - 建议从 mm_material_document 分离
CREATE TABLE mm_material_document_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    document_number VARCHAR(10) NOT NULL,           -- 凭证号
    fiscal_year     INTEGER NOT NULL,               -- 会计年度

    posting_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    document_date   DATE NOT NULL DEFAULT CURRENT_DATE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    header_text     VARCHAR(50),                    -- 凭证文本
    reference       VARCHAR(20),                    -- 参考号

    UNIQUE (tenant_id, document_number, fiscal_year)
);

-- 预留头 (对标 SAP RKPF)
CREATE TABLE mm_reservation_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    reservation_number VARCHAR(10) NOT NULL,        -- 预留号
    movement_type   VARCHAR(3) NOT NULL,            -- 移动类型

    requirement_date DATE NOT NULL,                 -- 需求日期

    -- 成本对象
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    order_id        UUID,                           -- 生产订单
    wbs_element     VARCHAR(24),                    -- WBS元素
    sales_order_id  UUID,                           -- 销售订单

    header_text     VARCHAR(50),

    status          VARCHAR(2) DEFAULT '01',        -- 01:创建 02:已批准 03:已完成

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, reservation_number)
);

-- 预留项 (对标 SAP RESB)
CREATE TABLE mm_reservation_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    header_id       UUID NOT NULL REFERENCES mm_reservation_hdr(id) ON DELETE CASCADE,
    item_number     INTEGER NOT NULL,

    material_id     UUID NOT NULL REFERENCES mm_material(id),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),
    batch_number    VARCHAR(10),

    requirement_quantity DECIMAL(13,3) NOT NULL,    -- 需求数量
    withdrawn_quantity DECIMAL(13,3) DEFAULT 0,     -- 已提取数量
    unit            VARCHAR(3) NOT NULL,

    requirement_date DATE,

    deletion_flag   BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, item_number)
);

-- 盘点凭证头 (对标 SAP IKPF)
CREATE TABLE mm_physical_inventory_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    phys_inventory_number VARCHAR(10) NOT NULL,     -- 盘点凭证号
    fiscal_year     INTEGER NOT NULL,

    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),

    phys_inventory_type VARCHAR(1) DEFAULT '1',     -- 1:期间 2:连续

    count_date      DATE NOT NULL,                  -- 盘点日期
    planned_count_date DATE,                        -- 计划盘点日期

    -- 冻结
    stock_freeze    BOOLEAN DEFAULT FALSE,          -- 库存冻结
    freeze_date     DATE,

    -- 状态
    status          VARCHAR(2) DEFAULT '01',        -- 01:创建 02:盘点中 03:差异 04:已过账

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    posted_at       TIMESTAMP,
    posted_by       UUID,

    UNIQUE (tenant_id, phys_inventory_number, fiscal_year)
);

-- 盘点项目 (对标 SAP ISEG)
CREATE TABLE mm_physical_inventory_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    header_id       UUID NOT NULL REFERENCES mm_physical_inventory_hdr(id) ON DELETE CASCADE,
    item_number     INTEGER NOT NULL,

    material_id     UUID NOT NULL REFERENCES mm_material(id),
    batch_number    VARCHAR(10),
    special_stock   VARCHAR(1),

    book_quantity   DECIMAL(13,3),                  -- 账面数量
    counted_quantity DECIMAL(13,3),                 -- 实盘数量
    difference_quantity DECIMAL(13,3),              -- 差异数量

    unit            VARCHAR(3) NOT NULL,

    book_value      DECIMAL(15,2),                  -- 账面价值
    difference_value DECIMAL(15,2),                 -- 差异价值
    currency_id     UUID REFERENCES core_currency(id),

    -- 重盘
    recount_required BOOLEAN DEFAULT FALSE,
    recount_quantity DECIMAL(13,3),

    counted_by      UUID,
    counted_date    DATE,

    posted          BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, item_number)
);
```

---

### 2.4 发票校验 (LIV)

#### 数据库设计状态

| 表名 | 说明 | 状态 |
|------|------|------|
| mm_invoice_hdr | 发票头 | ❌ 完全缺失 |
| mm_invoice_itm | 发票项 | ❌ 完全缺失 |
| mm_gr_ir | GR/IR清算 | ❌ 完全缺失 |

#### 建议新增表

```sql
-- 发票凭证头 (对标 SAP RBKP)
CREATE TABLE mm_invoice_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    invoice_number  VARCHAR(10) NOT NULL,           -- 发票凭证号
    fiscal_year     INTEGER NOT NULL,

    invoice_type    VARCHAR(2) NOT NULL,            -- RE:发票 G2:贷项凭证
    document_date   DATE NOT NULL,
    posting_date    DATE NOT NULL,

    vendor_id       UUID NOT NULL REFERENCES bp_business_partner(id),
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    currency_id     UUID NOT NULL REFERENCES core_currency(id),
    exchange_rate   DECIMAL(12,6) DEFAULT 1,

    -- 发票参考
    supplier_invoice VARCHAR(16),                   -- 供应商发票号
    supplier_invoice_date DATE,

    -- 金额
    gross_amount    DECIMAL(15,2),
    net_amount      DECIMAL(15,2),
    tax_amount      DECIMAL(15,2),
    discount_amount DECIMAL(14,2),

    -- 付款
    payment_term    VARCHAR(4),
    baseline_date   DATE,
    due_date        DATE,

    -- 冻结
    blocking_reason VARCHAR(2),
    blocked         BOOLEAN DEFAULT FALSE,

    -- 状态
    invoice_status  VARCHAR(2) DEFAULT '01',        -- 01:创建 02:已校验 03:已过账
    payment_status  VARCHAR(1) DEFAULT 'A',         -- A:未付 B:部分 C:已付

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    posted_at       TIMESTAMP,
    posted_by       UUID,

    UNIQUE (tenant_id, invoice_number, fiscal_year)
);

-- 发票凭证项 (对标 SAP RSEG)
CREATE TABLE mm_invoice_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    header_id       UUID NOT NULL REFERENCES mm_invoice_hdr(id) ON DELETE CASCADE,
    line_item       INTEGER NOT NULL,

    -- 采购订单参考
    po_id           UUID REFERENCES mm_purchase_order_hdr(id),
    po_item         INTEGER,

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    short_text      VARCHAR(100),

    -- 数量金额
    quantity        DECIMAL(13,3),
    unit            VARCHAR(3),
    net_price       DECIMAL(15,2),
    net_amount      DECIMAL(15,2),

    -- 税
    tax_code        VARCHAR(2),
    tax_amount      DECIMAL(15,2),

    -- 成本对象
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    profit_center_id UUID,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, line_item)
);

-- GR/IR 清算 (跟踪收货与发票的匹配)
CREATE TABLE mm_gr_ir (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 采购订单
    po_id           UUID NOT NULL REFERENCES mm_purchase_order_hdr(id),
    po_item         INTEGER NOT NULL,

    -- 收货凭证
    gr_document_id  UUID REFERENCES mm_material_document(id),
    gr_quantity     DECIMAL(13,3),
    gr_amount       DECIMAL(15,2),
    gr_date         DATE,

    -- 发票凭证
    invoice_id      UUID REFERENCES mm_invoice_hdr(id),
    invoice_item    INTEGER,
    ir_quantity     DECIMAL(13,3),
    ir_amount       DECIMAL(15,2),
    ir_date         DATE,

    -- 清算状态
    cleared         BOOLEAN DEFAULT FALSE,
    cleared_date    DATE,
    clearing_doc_id UUID,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

### 2.5 仓库管理 (WM)

#### 数据库设计状态

| 表名 | 说明 | 状态 |
|------|------|------|
| wm_warehouse | 仓库号 | ❌ 完全缺失 |
| wm_storage_type | 存储类型 | ❌ 完全缺失 |
| wm_storage_bin | 仓位 | ❌ 完全缺失 |
| wm_quant | 仓位库存 | ❌ 完全缺失 |
| wm_transfer_requirement | 转运需求 | ❌ 完全缺失 |
| wm_transfer_order | 转运订单 | ❌ 完全缺失 |

#### 建议新增表

```sql
-- 仓库号 (对标 SAP LGNUM)
CREATE TABLE wm_warehouse (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    warehouse_number VARCHAR(3) NOT NULL,           -- 仓库号
    description     VARCHAR(50) NOT NULL,

    plant_id        UUID REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),

    status          VARCHAR(1) DEFAULT 'A',         -- A:激活 B:冻结

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, warehouse_number)
);

-- 存储类型 (对标 SAP T301)
CREATE TABLE wm_storage_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    warehouse_id    UUID NOT NULL REFERENCES wm_warehouse(id),
    storage_type    VARCHAR(3) NOT NULL,            -- 存储类型
    description     VARCHAR(50) NOT NULL,

    role            VARCHAR(1) NOT NULL,            -- S:标准 P:拣配 G:收货
    putaway_strategy VARCHAR(1) DEFAULT 'P',        -- 上架策略
    picking_strategy VARCHAR(1) DEFAULT 'F',        -- 拣配策略

    capacity_check  BOOLEAN DEFAULT FALSE,

    status          VARCHAR(1) DEFAULT 'A',

    UNIQUE (warehouse_id, storage_type)
);

-- 仓位 (对标 SAP LQUA)
CREATE TABLE wm_storage_bin (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    warehouse_id    UUID NOT NULL REFERENCES wm_warehouse(id),
    storage_type_id UUID NOT NULL REFERENCES wm_storage_type(id),
    storage_bin     VARCHAR(10) NOT NULL,           -- 仓位

    storage_section VARCHAR(4),                     -- 存储区域
    bin_type        VARCHAR(1),                     -- 仓位类型

    -- 容量
    max_weight      DECIMAL(13,3),
    max_volume      DECIMAL(13,3),
    max_pallets     INTEGER,

    -- 状态
    blocked         BOOLEAN DEFAULT FALSE,
    block_reason    VARCHAR(2),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (warehouse_id, storage_type_id, storage_bin)
);

-- 仓位库存 (对标 SAP LQUA)
CREATE TABLE wm_quant (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    storage_bin_id  UUID NOT NULL REFERENCES wm_storage_bin(id),

    material_id     UUID NOT NULL REFERENCES mm_material(id),
    batch_number    VARCHAR(10),

    quantity        DECIMAL(13,3) NOT NULL,
    unit            VARCHAR(3) NOT NULL,

    quantity_available DECIMAL(13,3),               -- 可用数量
    quantity_picking DECIMAL(13,3) DEFAULT 0,       -- 拣配中数量

    stock_category  VARCHAR(1),                     -- 库存类别
    special_stock   VARCHAR(1),                     -- 特殊库存

    last_change_date DATE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (storage_bin_id, material_id, batch_number, stock_category)
);

-- 转运需求 (对标 SAP LTBK/LTBP)
CREATE TABLE wm_transfer_requirement (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    tr_number       VARCHAR(10) NOT NULL,           -- 需求号
    tr_type         VARCHAR(1) NOT NULL,            -- P:拣配 S:上架

    warehouse_id    UUID NOT NULL REFERENCES wm_warehouse(id),

    -- 来源
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    quantity        DECIMAL(13,3) NOT NULL,
    unit            VARCHAR(3) NOT NULL,

    source_storage_type VARCHAR(3),
    source_storage_bin VARCHAR(10),
    dest_storage_type VARCHAR(3),
    dest_storage_bin VARCHAR(10),

    -- 参考
    material_document_id UUID,
    sales_order_id  UUID,
    delivery_id     UUID,

    status          VARCHAR(2) DEFAULT '01',        -- 01:创建 02:已处理

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, tr_number)
);

-- 转运订单 (对标 SAP LTAK/LTAP)
CREATE TABLE wm_transfer_order_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    to_number       VARCHAR(10) NOT NULL,           -- 订单号
    warehouse_id    UUID NOT NULL REFERENCES wm_warehouse(id),

    to_type         VARCHAR(1) NOT NULL,            -- P:拣配 S:上架

    tr_id           UUID REFERENCES wm_transfer_requirement(id),

    priority        VARCHAR(1) DEFAULT '3',         -- 1-9

    status          VARCHAR(2) DEFAULT '01',        -- 01:创建 02:已确认

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at    TIMESTAMP,
    confirmed_by    UUID,

    UNIQUE (tenant_id, to_number)
);

CREATE TABLE wm_transfer_order_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    header_id       UUID NOT NULL REFERENCES wm_transfer_order_hdr(id) ON DELETE CASCADE,
    to_item         INTEGER NOT NULL,

    material_id     UUID NOT NULL REFERENCES mm_material(id),
    batch_number    VARCHAR(10),

    quantity        DECIMAL(13,3) NOT NULL,
    confirmed_quantity DECIMAL(13,3) DEFAULT 0,

    source_storage_type VARCHAR(3),
    source_storage_bin VARCHAR(10),
    dest_storage_type VARCHAR(3),
    dest_storage_bin VARCHAR(10),

    status          VARCHAR(1) DEFAULT 'A',         -- A:开放 C:已确认

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, to_item)
);
```

---

## 3. 已补充的数据库表

### 3.1 高优先级 ✅ 已完成

| 模块 | 表名 | 说明 | 状态 |
|------|------|------|------|
| MM-IV | mm_invoice_hdr | 发票凭证头 | ✅ 已添加 |
| MM-IV | mm_invoice_itm | 发票凭证项 | ✅ 已添加 |
| MM-IV | mm_gr_ir | GR/IR清算 | ✅ 已添加 |
| MM-IM | mm_reservation_hdr | 预留头 | ✅ 已添加 |
| MM-IM | mm_reservation_itm | 预留项 | ✅ 已添加 |
| MM-IM | mm_physical_inventory_hdr | 盘点凭证头 | ✅ 已添加 |
| MM-IM | mm_physical_inventory_itm | 盘点项目 | ✅ 已添加 |

### 3.2 中优先级 ✅ 已完成

| 模块 | 表名 | 说明 | 状态 |
|------|------|------|------|
| MM-PO | mm_contract_hdr | 合同头 | ✅ 已添加 |
| MM-PO | mm_contract_itm | 合同项 | ✅ 已添加 |
| MM-PO | mm_source_list | 货源清单 | ✅ 已添加 |
| MM-MM | mm_material_sales | 物料销售数据 | ✅ 已添加 |

### 3.3 低优先级 ⏸️ 暂不实现

| 模块 | 表名 | 说明 |
|------|------|------|
| MM-PO | mm_rfq_hdr | 询价头 |
| MM-PO | mm_rfq_vendor | 供应商报价 |
| MM-PO | mm_scheduling_agreement | 计划协议 |
| WM | wm_warehouse | 仓库号 |
| WM | wm_storage_type | 存储类型 |
| WM | wm_storage_bin | 仓位 |
| WM | wm_quant | 仓位库存 |
| WM | wm_transfer_requirement | 转运需求 |
| WM | wm_transfer_order_hdr | 转运订单头 |
| WM | wm_transfer_order_itm | 转运订单项 |

---

## 4. 一致性修复建议

### 4.1 立即修复 (高优先级)

1. **创建发票校验表**
   - mm_invoice_hdr, mm_invoice_itm, mm_gr_ir
   - 支持三单匹配和GR/IR清算

2. **创建预留表**
   - mm_reservation_hdr, mm_reservation_itm
   - 支持生产预留和领料

3. **创建盘点表**
   - mm_physical_inventory_hdr, mm_physical_inventory_itm
   - 支持实物盘点和差异处理

### 4.2 短期完善 (中优先级)

1. **创建合同/货源清单表**
   - 支持框架协议和货源管理

2. **分离物料凭证头**
   - 将 mm_material_document 拆分为头/项结构

3. **添加物料销售视图**
   - 支持SD模块集成

### 4.3 长期优化 (低优先级)

1. **创建仓库管理模块**
   - 如有精细化管理需求

2. **添加询价报价功能**
   - 支持采购寻源流程

---

## 5. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 - 完成一致性检查 |
| 1.1 | 2026-03-14 | 完成高/中优先级表补充 - 发票校验、预留、盘点、合同、货源清单、物料销售视图 |
