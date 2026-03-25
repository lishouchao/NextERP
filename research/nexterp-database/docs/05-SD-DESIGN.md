# SD 模块数据库设计

**模块**: Sales and Distribution (销售与分销)
**对标**: SAP ECC SD (VA/VD/VM/VF)
**版本**: 1.0

---

## 1. 模块概述

### 1.1 业务范围

| 子模块 | 说明 | SAP 对标 |
|--------|------|----------|
| 销售订单 | 订单创建与管理 | VA01/VA02/VA03 |
| 交货单 | 出库与发运 | VL01N/VL02N |
| 开票 | 发票与贷项凭证 | VF01/VF02 |
| 定价 | 价格与折扣 | VK11/VK12 |
| 信用管理 | 信用检查与控制 | FD32 |

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                     SD Module Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                     销售流程                             │    │
│  │                                                          │    │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐    │    │
│  │  │ 询价单  │─►│ 报价单  │─►│销售订单 │─►│ 交货单  │    │    │
│  │  │ sd_rfq  │  │sd_quote │  │ sd_so   │  │ sd_dn   │    │    │
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘    │    │
│  │       │            │            │            │          │    │
│  │       └────────────┴────────────┴────────────┘          │    │
│  │                          │                               │    │
│  │                          ▼                               │    │
│  │                   ┌─────────────┐                       │    │
│  │                   │   开票单    │                       │    │
│  │                   │ sd_billing  │                       │    │
│  │                   └─────────────┘                       │    │
│  │                          │                               │    │
│  └──────────────────────────┼───────────────────────────────┘    │
│                             │                                    │
│  ┌──────────────────────────┼───────────────────────────────┐    │
│  │                     支撑数据                              │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │    │
│  │  │ 定价条件    │  │ 客户主数据  │  │ 销售区域    │      │    │
│  │  │sd_condition │  │ bp_customer │  │sales_area   │      │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │    │
│  └──────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 销售范围概念

```
销售范围 = 销售组织 + 分销渠道 + 产品组

┌────────────────────────────────────────────────────────────┐
│                    销售范围 (Sales Area)                    │
├────────────────────────────────────────────────────────────┤
│                                                            │
│   销售组织 (Sales Org)                                     │
│   ├── 1000 华东销售区                                      │
│   │   ├── 分销渠道: 10 批发                               │
│   │   │   ├── 产品组: 01 消费品                           │
│   │   │   └── 产品组: 02 工业品                           │
│   │   └── 分销渠道: 20 零售                               │
│   │       └── 产品组: 01 消费品                           │
│   └── 2000 华北销售区                                      │
│       └── ...                                              │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

---

## 2. 销售订单

### 2.1 销售订单头 (sd_sales_order_hdr)

对标 SAP VBAK

```sql
CREATE TABLE sd_sales_order_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 订单信息
    order_number    VARCHAR(10) NOT NULL,      -- 订单号
    order_type      VARCHAR(4) NOT NULL,       -- 订单类型
    -- OR:标准订单 CR:贷项订单 DR:借项订单 RE:退货订单

    -- 销售范围
    sales_org_id    UUID NOT NULL REFERENCES sys_sales_org(id),
    distribution_channel VARCHAR(2) NOT NULL,  -- 分销渠道
    -- 10:批发 20:零售 30:直销 40:电商
    division        VARCHAR(2) NOT NULL,       -- 产品组
    -- 01:消费品 02:工业品 03:服务

    -- 客户
    sold_to_party   UUID NOT NULL REFERENCES bp_business_partner(id), -- 售达方
    ship_to_party   UUID REFERENCES bp_business_partner(id),          -- 送达方
    bill_to_party   UUID REFERENCES bp_business_partner(id),          -- 开票方
    payer_party     UUID REFERENCES bp_business_partner(id),          -- 付款方

    -- 日期
    document_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    requested_delivery_date DATE,              -- 要求交货日期
    pricing_date    DATE,                      -- 定价日期

    -- 货币
    currency_id     UUID NOT NULL REFERENCES core_currency(id),
    exchange_rate   DECIMAL(12,6) DEFAULT 1,

    -- 金额
    net_value       DECIMAL(15,2) DEFAULT 0,   -- 净值
    tax_amount      DECIMAL(15,2) DEFAULT 0,   -- 税额
    gross_value     DECIMAL(15,2) DEFAULT 0,   -- 含税金额

    -- 定价
    pricing_schema  VARCHAR(6),                -- 定价过程

    -- 国际贸易
    incoterms       VARCHAR(3),                -- 国际贸易条件
    incoterms_loc   VARCHAR(28),               -- 地点

    -- 付款
    payment_term    VARCHAR(4),                -- 付款条款
    payment_method  VARCHAR(2),                -- 付款方式

    -- 参考
    purchase_order  VARCHAR(35),               -- 客户PO号
    reference       VARCHAR(20),

    -- 状态
    order_status    VARCHAR(2) DEFAULT '01',   -- 01:创建 02:审批 03:处理中 04:已完成
    overall_status  VARCHAR(2) DEFAULT 'A',    -- A:未处理 B:部分处理 C:已完成
    delivery_status VARCHAR(2) DEFAULT 'A',    -- A:未交货 B:部分交货 C:已交货
    billing_status  VARCHAR(2) DEFAULT 'A',    -- A:未开票 B:部分开票 C:已开票

    -- 审批
    approval_status approval_status DEFAULT 'DRAFT',
    approved_by     UUID,
    approved_at     TIMESTAMP,

    -- 阻止
    delivery_block  VARCHAR(2),                -- 交货阻止
    billing_block   VARCHAR(2),                -- 开票阻止

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, order_number)
);
```

### 2.2 销售订单项 (sd_sales_order_itm)

对标 SAP VBAP

```sql
CREATE TABLE sd_sales_order_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    header_id       UUID NOT NULL REFERENCES sd_sales_order_hdr(id) ON DELETE CASCADE,
    order_number    VARCHAR(10),               -- 冗余
    item_number     INTEGER NOT NULL,          -- 行号

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    description     VARCHAR(100),              -- 物料描述

    -- 数量
    ordered_qty     DECIMAL(13,3) NOT NULL,    -- 订购数量
    delivered_qty   DECIMAL(13,3) DEFAULT 0,   -- 已交货数量
    invoiced_qty    DECIMAL(13,3) DEFAULT 0,   -- 已开票数量
    sales_unit      VARCHAR(3) NOT NULL,       -- 销售单位

    -- 价格
    gross_price     DECIMAL(15,2),             -- 含税单价
    net_price       DECIMAL(15,2) NOT NULL,    -- 净单价
    discount_percent DECIMAL(5,2) DEFAULT 0,   -- 折扣%
    discount_amount DECIMAL(15,2) DEFAULT 0,   -- 折扣金额
    surcharge       DECIMAL(15,2) DEFAULT 0,   -- 附加费

    -- 金额
    net_value       DECIMAL(15,2) NOT NULL,    -- 净值
    tax_code        VARCHAR(2),                -- 税码
    tax_amount      DECIMAL(15,2) DEFAULT 0,   -- 税额
    gross_value     DECIMAL(15,2) DEFAULT 0,   -- 含税金额

    -- 成本
    cost_estimate   DECIMAL(15,2),             -- 预估成本

    -- 交货
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),
    delivery_date   DATE,                      -- 交货日期
    delivery_priority VARCHAR(2),              -- 交货优先级

    -- 项目类别
    item_category   VARCHAR(4),                -- 项目类别
    -- TAN:标准项目 TANN:免费项目 TAS:服务项目

    -- 定价
    pricing_group   VARCHAR(2),                -- 定价组

    -- 状态
    item_status     VARCHAR(2) DEFAULT '01',   -- 01:未处理 02:部分处理 03:已完成
    delivery_status VARCHAR(2) DEFAULT 'A',    -- A:未交货 B:部分交货 C:已交货
    billing_status  VARCHAR(2) DEFAULT 'A',    -- A:未开票 B:部分开票 C:已开票

    -- 完成
    is_completed    BOOLEAN DEFAULT FALSE,
    rejection_reason VARCHAR(2),               -- 拒绝原因

    -- 文本
    item_text       VARCHAR(100),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, item_number)
);
```

---

## 3. 交货单

### 3.1 交货单头 (sd_delivery_hdr)

对标 SAP LIKP

```sql
CREATE TABLE sd_delivery_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 交货信息
    delivery_number VARCHAR(10) NOT NULL,      -- 交货单号
    delivery_type   VARCHAR(4) NOT NULL,       -- 交货类型
    -- LF:外向交货 LR:退货交货 LO:无订单交货

    -- 销售范围
    sales_org_id    UUID NOT NULL REFERENCES sys_sales_org(id),
    distribution_channel VARCHAR(2),
    division        VARCHAR(2),

    -- 客户
    sold_to_party   UUID NOT NULL REFERENCES bp_business_partner(id),
    ship_to_party   UUID REFERENCES bp_business_partner(id),

    -- 日期
    document_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    planned_gi_date DATE,                      -- 计划发货日期
    actual_gi_date  DATE,                      -- 实际发货日期
    loading_date    DATE,                      -- 装载日期

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 金额
    net_value       DECIMAL(15,2) DEFAULT 0,

    -- 重量/体积
    net_weight      DECIMAL(13,3),
    gross_weight    DECIMAL(13,3),
    weight_unit     VARCHAR(3),
    volume          DECIMAL(13,3),
    volume_unit     VARCHAR(3),

    -- 发运
    shipping_point  VARCHAR(4),                -- 装运点
    shipping_condition VARCHAR(2),             -- 装运条件
    delivery_priority VARCHAR(2),              -- 交货优先级

    -- 国际贸易
    incoterms       VARCHAR(3),
    incoterms_loc   VARCHAR(28),

    -- 来源
    order_id        UUID REFERENCES sd_sales_order_hdr(id), -- 来源订单

    -- 状态
    delivery_status VARCHAR(2) DEFAULT '01',   -- 01:未处理 02:拣配中 03:已拣配 04:已发货
    picking_status  VARCHAR(2) DEFAULT 'A',    -- A:未拣配 B:部分 C:完成
    gi_status       VARCHAR(2) DEFAULT 'A',    -- A:未发货 B:已发货
    billing_status  VARCHAR(2) DEFAULT 'A',    -- A:未开票 B:部分 C:完成

    -- 过账
    posted_by       UUID,
    posted_at       TIMESTAMP,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, delivery_number)
);
```

### 3.2 交货单项 (sd_delivery_itm)

对标 SAP LIPS

```sql
CREATE TABLE sd_delivery_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    header_id       UUID NOT NULL REFERENCES sd_delivery_hdr(id) ON DELETE CASCADE,
    delivery_number VARCHAR(10),
    item_number     INTEGER NOT NULL,

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    description     VARCHAR(100),

    -- 数量
    delivery_qty    DECIMAL(13,3) NOT NULL,    -- 交货数量
    picked_qty      DECIMAL(13,3) DEFAULT 0,   -- 拣配数量
    sales_unit      VARCHAR(3) NOT NULL,
    base_unit       VARCHAR(3),

    -- 批次
    batch_number    VARCHAR(10),

    -- 价格
    net_price       DECIMAL(15,2),
    net_value       DECIMAL(15,2),

    -- 来源
    order_id        UUID REFERENCES sd_sales_order_hdr(id),
    order_item_id   UUID REFERENCES sd_sales_order_itm(id),

    -- 组织
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),

    -- 重量/体积
    net_weight      DECIMAL(13,3),
    gross_weight    DECIMAL(13,3),
    weight_unit     VARCHAR(3),

    -- 项目类别
    item_category   VARCHAR(4),

    -- 状态
    picking_status  VARCHAR(2) DEFAULT 'A',
    gi_status       VARCHAR(2) DEFAULT 'A',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, item_number)
);
```

---

## 4. 开票

### 4.1 开票凭证头 (sd_billing_hdr)

对标 SAP VBRK

```sql
CREATE TABLE sd_billing_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 开票信息
    billing_number  VARCHAR(10) NOT NULL,      -- 开票号
    billing_type    VARCHAR(4) NOT NULL,       -- 开票类型
    -- F1:发票 F2:贷项凭证 F3:借项凭证 S1:取消

    -- 销售范围
    sales_org_id    UUID NOT NULL REFERENCES sys_sales_org(id),
    distribution_channel VARCHAR(2),
    division        VARCHAR(2),

    -- 客户
    sold_to_party   UUID NOT NULL REFERENCES bp_business_partner(id),
    bill_to_party   UUID REFERENCES bp_business_partner(id),
    payer_party     UUID REFERENCES bp_business_partner(id),

    -- 日期
    document_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    billing_date    DATE NOT NULL,             -- 开票日期

    -- 货币
    currency_id     UUID NOT NULL REFERENCES core_currency(id),
    exchange_rate   DECIMAL(12,6) DEFAULT 1,

    -- 金额
    net_value       DECIMAL(15,2) DEFAULT 0,   -- 净值
    tax_amount      DECIMAL(15,2) DEFAULT 0,   -- 税额
    gross_value     DECIMAL(15,2) DEFAULT 0,   -- 含税金额

    -- 付款
    payment_term    VARCHAR(4),
    payment_due_date DATE,                     -- 到期日

    -- 国际贸易
    incoterms       VARCHAR(3),
    incoterms_loc   VARCHAR(28),

    -- 来源
    delivery_id     UUID REFERENCES sd_delivery_hdr(id),
    order_id        UUID REFERENCES sd_sales_order_hdr(id),

    -- 会计凭证
    accounting_doc_id UUID,                    -- 生成的会计凭证ID
    is_posted       BOOLEAN DEFAULT FALSE,

    -- 状态
    billing_status  VARCHAR(2) DEFAULT '01',   -- 01:创建 02:已过账 03:已取消
    cancellation_doc_id UUID,                  -- 取消凭证ID

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, billing_number)
);
```

### 4.2 开票凭证项 (sd_billing_itm)

对标 SAP VBRP

```sql
CREATE TABLE sd_billing_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    header_id       UUID NOT NULL REFERENCES sd_billing_hdr(id) ON DELETE CASCADE,
    billing_number  VARCHAR(10),
    item_number     INTEGER NOT NULL,

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    description     VARCHAR(100),

    -- 数量
    billed_qty      DECIMAL(13,3) NOT NULL,    -- 开票数量
    sales_unit      VARCHAR(3) NOT NULL,

    -- 价格
    gross_price     DECIMAL(15,2),
    net_price       DECIMAL(15,2) NOT NULL,
    discount_amount DECIMAL(15,2) DEFAULT 0,

    -- 金额
    net_value       DECIMAL(15,2) NOT NULL,
    tax_code        VARCHAR(2),
    tax_amount      DECIMAL(15,2) DEFAULT 0,
    gross_value     DECIMAL(15,2) DEFAULT 0,

    -- 成本
    cost_value      DECIMAL(15,2),             -- 成本金额

    -- 来源
    delivery_id     UUID REFERENCES sd_delivery_hdr(id),
    delivery_item_id UUID REFERENCES sd_delivery_itm(id),
    order_id        UUID REFERENCES sd_sales_order_hdr(id),
    order_item_id   UUID REFERENCES sd_sales_order_itm(id),

    -- 组织
    plant_id        UUID REFERENCES sys_plant(id),

    -- 项目类别
    item_category   VARCHAR(4),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, item_number)
);
```

---

## 5. 定价

### 5.1 定价条件表 (sd_condition)

对标 SAP KONP

```sql
CREATE TABLE sd_condition (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 条件类型
    condition_type  VARCHAR(4) NOT NULL,       -- 条件类型
    -- PR00:价格 K004:折扣 K007:附加费

    -- 条件记录号
    condition_record VARCHAR(10) NOT NULL,     -- 条件记录号
    condition_item  INTEGER NOT NULL,          -- 条件项

    -- 定价数据
    amount          DECIMAL(15,2),             -- 金额
    rate            DECIMAL(9,5),              -- 比率/百分比
    price_unit      INTEGER DEFAULT 1,         -- 价格单位
    currency_id     UUID REFERENCES core_currency(id),

    -- 计算类型
    calculation_type VARCHAR(1),               -- 计算类型
    -- A:百分比 B:固定金额 C:数量

    -- 有效期
    valid_from      DATE NOT NULL,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 销售范围
    sales_org_id    UUID REFERENCES sys_sales_org(id),
    distribution_channel VARCHAR(2),

    -- 客户
    customer_id     UUID REFERENCES bp_business_partner(id),

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_pricing_group VARCHAR(2),

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, condition_record, condition_item)
);
```

**常用条件类型**:

| 条件类型 | 说明 | 计算类型 |
|----------|------|----------|
| PR00 | 价格 | 固定金额 |
| K004 | 折扣 | 百分比 |
| K007 | 附加费 | 百分比 |
| MWST | 税 | 百分比 |
| SKTO | 现金折扣 | 百分比 |

### 5.2 定价过程 (sd_pricing_procedure)

```sql
CREATE TABLE sd_pricing_procedure (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 定价过程
    procedure_code  VARCHAR(6) NOT NULL,       -- 定价过程代码
    procedure_name  VARCHAR(100) NOT NULL,     -- 定价过程名称

    -- 步骤
    step_number     INTEGER NOT NULL,          -- 步骤号

    -- 条件类型
    condition_type  VARCHAR(4),                -- 条件类型

    -- 描述
    description     VARCHAR(100),

    -- 计算
    from_step       INTEGER,                   -- 从步骤
    to_step         INTEGER,                   -- 到步骤

    -- 类型
    is_statistical  BOOLEAN DEFAULT FALSE,     -- 统计性
    is_subtotal     BOOLEAN DEFAULT FALSE,     -- 小计

    -- 排序
    sort_order      INTEGER,

    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, procedure_code, step_number)
);
```

---

## 6. 信用管理

### 6.1 客户信用主数据 (sd_credit_master)

```sql
CREATE TABLE sd_credit_master (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 客户
    customer_id     UUID NOT NULL REFERENCES bp_business_partner(id),
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 信用额度
    credit_limit    DECIMAL(15,2) NOT NULL,    -- 信用额度
    used_limit      DECIMAL(15,2) DEFAULT 0,   -- 已用额度
    available_limit DECIMAL(15,2),             -- 可用额度

    -- 风险类
    risk_class      VARCHAR(1),                -- 风险类别
    -- 1:低风险 2:中风险 3:高风险

    -- 信用组
    credit_group    VARCHAR(4),                -- 信用组

    -- 状态
    credit_status   VARCHAR(2) DEFAULT '01',   -- 01:正常 02:预警 03:冻结

    -- 检查规则
    check_rule      VARCHAR(1),                -- 信用检查规则
    -- 1:简单 2:复杂

    -- 上次检查
    last_check_date DATE,
    next_check_date DATE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (customer_id, company_id)
);
```

### 6.2 信用检查日志 (sd_credit_check_log)

```sql
CREATE TABLE sd_credit_check_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 客户
    customer_id     UUID NOT NULL REFERENCES bp_business_partner(id),
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 检查信息
    check_type      VARCHAR(2),                -- 检查类型
    -- 01:订单 02:交货 03:开票

    document_type   VARCHAR(4),                -- 单据类型
    document_id     UUID,                      -- 单据ID
    document_number VARCHAR(10),               -- 单据号

    -- 检查金额
    check_amount    DECIMAL(15,2),             -- 检查金额

    -- 检查结果
    check_result    VARCHAR(2),                -- 检查结果
    -- OK:通过 WA:警告 BL:阻止
    result_message  TEXT,                      -- 结果消息

    -- 信用状态
    credit_limit    DECIMAL(15,2),
    used_before     DECIMAL(15,2),             -- 检查前已用
    used_after      DECIMAL(15,2),             -- 检查后已用

    -- 时间
    check_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    check_by        UUID,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 7. 销售统计

### 7.1 销售统计表 (sd_sales_statistics)

```sql
CREATE TABLE sd_sales_statistics (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 期间
    stat_year       INTEGER NOT NULL,          -- 统计年度
    stat_month      INTEGER NOT NULL,          -- 统计月份
    stat_date       DATE NOT NULL,             -- 统计日期

    -- 维度
    company_id      UUID REFERENCES sys_company(id),
    sales_org_id    UUID REFERENCES sys_sales_org(id),
    customer_id     UUID REFERENCES bp_business_partner(id),
    material_id     UUID REFERENCES mm_material(id),
    plant_id        UUID REFERENCES sys_plant(id),

    -- 数量
    order_qty       DECIMAL(13,3) DEFAULT 0,   -- 订单数量
    delivery_qty    DECIMAL(13,3) DEFAULT 0,   -- 交货数量
    billing_qty     DECIMAL(13,3) DEFAULT 0,   -- 开票数量

    -- 金额
    order_value     DECIMAL(15,2) DEFAULT 0,   -- 订单金额
    delivery_value  DECIMAL(15,2) DEFAULT 0,   -- 交货金额
    billing_value   DECIMAL(15,2) DEFAULT 0,   -- 开票金额
    cost_value      DECIMAL(15,2) DEFAULT 0,   -- 成本金额

    -- 利润
    gross_profit    DECIMAL(15,2) DEFAULT 0,   -- 毛利
    profit_margin   DECIMAL(5,2) DEFAULT 0,    -- 毛利率%

    -- 单据数
    order_count     INTEGER DEFAULT 0,         -- 订单数
    delivery_count  INTEGER DEFAULT 0,         -- 交货数
    billing_count   INTEGER DEFAULT 0,         -- 开票数

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, stat_date, company_id, sales_org_id, customer_id, material_id)
);
```

---

## 8. 存储过程

### 8.1 创建销售订单

```sql
CREATE OR REPLACE FUNCTION sd_create_sales_order(
    p_tenant_id UUID,
    p_order_type VARCHAR,
    p_sales_org_id UUID,
    p_distribution_channel VARCHAR,
    p_division VARCHAR,
    p_sold_to_party UUID,
    p_currency_id UUID,
    p_user_id UUID
) RETURNS UUID AS $$
DECLARE
    v_order_id UUID;
    v_order_number VARCHAR(10);
BEGIN
    -- 生成订单号
    v_order_number := generate_business_code(p_tenant_id, 'SO', NULL, NULL);

    -- 创建订单头
    INSERT INTO sd_sales_order_hdr (
        tenant_id, order_number, order_type,
        sales_org_id, distribution_channel, division,
        sold_to_party, currency_id,
        created_by, updated_by
    ) VALUES (
        p_tenant_id, v_order_number, p_order_type,
        p_sales_org_id, p_distribution_channel, p_division,
        p_sold_to_party, p_currency_id,
        p_user_id, p_user_id
    ) RETURNING id INTO v_order_id;

    RETURN v_order_id;
END;
$$ LANGUAGE plpgsql;
```

### 8.2 发货过账

```sql
CREATE OR REPLACE FUNCTION sd_post_goods_issue(
    p_delivery_id UUID,
    p_user_id UUID
) RETURNS BOOLEAN AS $$
DECLARE
    v_tenant_id UUID;
    v_delivery RECORD;
    v_item RECORD;
    v_mat_doc_id UUID;
BEGIN
    -- 获取交货单信息
    SELECT * INTO v_delivery
    FROM sd_delivery_hdr WHERE id = p_delivery_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION '交货单不存在';
    END IF;

    -- 检查状态
    IF v_delivery.gi_status != 'A' THEN
        RAISE EXCEPTION '交货单已发货';
    END IF;

    -- 遍历明细，执行库存发货
    FOR v_item IN
        SELECT * FROM sd_delivery_itm WHERE header_id = p_delivery_id
    LOOP
        -- 调用 MM 模块发货函数
        v_mat_doc_id := mm_post_goods_issue(
            v_item.material_id,
            v_item.plant_id,
            v_item.sloc_id,
            v_item.delivery_qty,
            v_item.sales_unit,
            '601',  -- 销售发货移动类型
            p_user_id
        );
    END LOOP;

    -- 更新交货单状态
    UPDATE sd_delivery_hdr
    SET gi_status = 'B',
        actual_gi_date = CURRENT_DATE,
        delivery_status = '04',
        posted_by = p_user_id,
        posted_at = CURRENT_TIMESTAMP
    WHERE id = p_delivery_id;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
```

---

## 9. 视图定义

### 9.1 销售订单跟踪视图

```sql
CREATE VIEW v_sd_order_tracking AS
SELECT
    h.order_number,
    h.document_date,
    h.order_type,
    c.bp_number AS customer_code,
    c.name AS customer_name,
    h.net_value,
    h.delivery_status,
    h.billing_status,

    -- 交货信息
    COUNT(DISTINCT d.id) AS delivery_count,
    SUM(d_item.delivery_qty) AS total_delivered_qty,

    -- 开票信息
    COUNT(DISTINCT b.id) AS billing_count,
    SUM(b_item.net_value) AS total_billed_value

FROM sd_sales_order_hdr h
JOIN bp_business_partner c ON c.id = h.sold_to_party
LEFT JOIN sd_delivery_hdr d ON d.order_id = h.id
LEFT JOIN sd_delivery_itm d_item ON d_item.header_id = d.id
LEFT JOIN sd_billing_hdr b ON b.order_id = h.id
LEFT JOIN sd_billing_itm b_item ON b_item.header_id = b.id
GROUP BY h.id, h.order_number, h.document_date, h.order_type,
         c.bp_number, c.name, h.net_value, h.delivery_status, h.billing_status;
```

### 9.2 销售报表视图

```sql
CREATE VIEW v_sd_sales_report AS
SELECT
    s.stat_year,
    s.stat_month,
    c.company_code,
    c.company_name,
    so.sales_org_code,
    so.sales_org_name,
    cu.bp_number AS customer_code,
    cu.name AS customer_name,
    m.material_code,
    m.description AS material_desc,

    SUM(s.order_qty) AS total_order_qty,
    SUM(s.delivery_qty) AS total_delivery_qty,
    SUM(s.billing_qty) AS total_billing_qty,

    SUM(s.order_value) AS total_order_value,
    SUM(s.billing_value) AS total_billing_value,
    SUM(s.cost_value) AS total_cost_value,
    SUM(s.gross_profit) AS total_gross_profit,

    CASE WHEN SUM(s.billing_value) > 0
         THEN SUM(s.gross_profit) / SUM(s.billing_value) * 100
         ELSE 0 END AS profit_margin_pct

FROM sd_sales_statistics s
LEFT JOIN sys_company c ON c.id = s.company_id
LEFT JOIN sys_sales_org so ON so.id = s.sales_org_id
LEFT JOIN bp_business_partner cu ON cu.id = s.customer_id
LEFT JOIN mm_material m ON m.id = s.material_id
GROUP BY s.stat_year, s.stat_month, c.company_code, c.company_name,
         so.sales_org_code, so.sales_org_name, cu.bp_number, cu.name,
         m.material_code, m.description;
```

---

## 10. 索引策略

```sql
-- 销售订单
CREATE INDEX idx_sd_so_number ON sd_sales_order_hdr (tenant_id, order_number);
CREATE INDEX idx_sd_so_customer ON sd_sales_order_hdr (sold_to_party);
CREATE INDEX idx_sd_so_date ON sd_sales_order_hdr (document_date);
CREATE INDEX idx_sd_so_status ON sd_sales_order_hdr (order_status);

CREATE INDEX idx_sd_so_itm_header ON sd_sales_order_itm (header_id);
CREATE INDEX idx_sd_so_itm_material ON sd_sales_order_itm (material_id);

-- 交货单
CREATE INDEX idx_sd_dn_number ON sd_delivery_hdr (tenant_id, delivery_number);
CREATE INDEX idx_sd_dn_order ON sd_delivery_hdr (order_id);
CREATE INDEX idx_sd_dn_date ON sd_delivery_hdr (document_date);

-- 开票
CREATE INDEX idx_sd_bill_number ON sd_billing_hdr (tenant_id, billing_number);
CREATE INDEX idx_sd_bill_customer ON sd_billing_hdr (sold_to_party);
CREATE INDEX idx_sd_bill_date ON sd_billing_hdr (billing_date);

-- 定价条件
CREATE INDEX idx_sd_cond_type ON sd_condition (condition_type, valid_from, valid_to);
CREATE INDEX idx_sd_cond_customer ON sd_condition (customer_id);
CREATE INDEX idx_sd_cond_material ON sd_condition (material_id);

-- 信用
CREATE INDEX idx_sd_credit_customer ON sd_credit_master (customer_id, company_id);
```

---

## 11. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
