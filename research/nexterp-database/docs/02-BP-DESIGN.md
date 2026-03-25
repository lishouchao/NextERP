# BP 模块数据库设计

**模块**: Business Partner (业务伙伴)
**对标**: SAP S/4HANA BP (CVI - Customer Vendor Integration)
**版本**: 1.0

---

## 1. 模块概述

### 1.1 设计理念

S/4HANA 引入了统一的业务伙伴 (BP) 模型，将客户 (Customer) 和供应商 (Vendor) 合并为一个业务伙伴对象。NextERP 借鉴这一设计，实现：

1. **统一主数据** - 一个业务伙伴可以同时是客户和供应商
2. **角色分离** - 通过角色控制不同业务场景的属性
3. **地址管理** - 支持多地址、多用途
4. **银行账户** - 支持多银行账户管理

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                     BP Module Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                 bp_business_partner                      │    │
│  │                     (业务伙伴)                            │    │
│  │  ┌──────────────────────────────────────────────────┐   │    │
│  │  │  常规数据: 编码、名称、类型、搜索词               │   │    │
│  │  └──────────────────────────────────────────────────┘   │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│        ┌─────────────────────┼─────────────────────┐            │
│        │                     │                     │            │
│        ▼                     ▼                     ▼            │
│  ┌──────────┐          ┌──────────┐          ┌──────────┐      │
│  │ bp_role  │          │bp_address│          │ bp_bank  │      │
│  │ (角色)   │          │ (地址)   │          │(银行账户)│      │
│  │          │          │          │          │          │      │
│  │ • 客户   │          │ • 默认   │          │ • 主账户 │      │
│  │ • 供应商 │          │ • 开票   │          │ • 其他   │      │
│  │ • 工厂   │          │ • 收货   │          │          │      │
│  │ • 员工   │          │ • 送货   │          │          │      │
│  └──────────┘          └──────────┘          └──────────┘      │
│        │                     │                                   │
│        ▼                     │                                   │
│  ┌──────────────────────────────────────────────────────┐       │
│  │              角色属性 (按角色类型)                     │       │
│  │  ┌─────────────┐    ┌─────────────┐                  │       │
│  │  │ bp_customer │    │ bp_vendor   │                  │       │
│  │  │  (客户属性) │    │ (供应商属性)│                  │       │
│  │  └─────────────┘    └─────────────┘                  │       │
│  └──────────────────────────────────────────────────────┘       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 业务伙伴主数据

### 2.1 业务伙伴主表 (bp_business_partner)

对标 SAP BUT000

```sql
CREATE TABLE bp_business_partner (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 业务伙伴编码
    bp_number       VARCHAR(10) NOT NULL,      -- 业务伙伴编号
    bp_type         VARCHAR(4) NOT NULL,       -- 业务伙伴类型
    -- P:个人 O:组织 G:组织(集团)

    -- 名称
    name            VARCHAR(80) NOT NULL,      -- 名称/全称
    name_2          VARCHAR(80),               -- 名称2
    short_name      VARCHAR(40),               -- 简称
    name_en         VARCHAR(80),               -- 英文名

    -- 个人信息 (type=P 时使用)
    first_name      VARCHAR(40),
    last_name       VARCHAR(40),
    title           VARCHAR(4),                -- 称谓
    gender          gender,
    birth_date      DATE,

    -- 搜索词
    search_term_1   VARCHAR(20),               -- 搜索词1
    search_term_2   VARCHAR(20),               -- 搜索词2

    -- 分类
    industry        VARCHAR(4),                -- 行业
    bp_category     VARCHAR(10),               -- 业务伙伴分类

    -- 法律实体
    legal_entity_type VARCHAR(4),              -- 法律形式
    incorporation_country VARCHAR(3),          -- 注册国家
    tax_number      VARCHAR(18),               -- 税号
    tax_number_2    VARCHAR(18),               -- 税号2

    -- 地址 (冗余主地址)
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city_id         UUID REFERENCES core_city(id),
    street          VARCHAR(60),
    postal_code     VARCHAR(10),

    -- 联系方式 (冗余)
    phone           VARCHAR(50),
    mobile          VARCHAR(50),
    email           VARCHAR(100),
    website         VARCHAR(100),

    -- 信用控制
    credit_limit    DECIMAL(15,2) DEFAULT 0,   -- 信用额度
    credit_status   VARCHAR(2),                -- 信用状态

    -- 状态
    bp_status       VARCHAR(2) DEFAULT '01',   -- 01:激活 02:冻结 03:删除
    status          general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, bp_number)
);
```

### 2.2 业务伙伴角色 (bp_role)

对标 SAP BUT100

```sql
CREATE TABLE bp_role (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    bp_id           UUID NOT NULL REFERENCES bp_business_partner(id) ON DELETE CASCADE,

    -- 角色类型
    role_type       VARCHAR(6) NOT NULL,       -- 角色类型
    -- ZCUSTOMER:客户 ZVENDOR:供应商 ZPLANT:工厂 ZEMPLOYEE:员工
    -- ZPERSON:个人 ZORGANIZATION:组织

    -- 角色状态
    is_primary      BOOLEAN DEFAULT FALSE,     -- 主要角色
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',
    role_status     general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (bp_id, role_type)
);
```

**角色类型枚举**:

| 角色 | 说明 | 关联属性表 |
|------|------|------------|
| ZCUSTOMER | 客户 | bp_customer |
| ZVENDOR | 供应商 | bp_vendor |
| ZPLANT | 工厂 | - |
| ZPERSON | 个人 | - |
| ZORGANIZATION | 组织 | - |

---

## 3. 地址管理

### 3.1 业务伙伴地址 (bp_address)

对标 SAP BUT020 + ADRC

```sql
CREATE TABLE bp_address (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    bp_id           UUID NOT NULL REFERENCES bp_business_partner(id) ON DELETE CASCADE,

    -- 地址编号
    address_number  VARCHAR(10),               -- 地址号
    address_type    VARCHAR(2) NOT NULL,       -- 地址类型
    -- 01:默认地址 02:开票地址 03:送货地址 04:收货地址 05:其他

    -- 是否默认
    is_default      BOOLEAN DEFAULT FALSE,

    -- 地址信息
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city_id         UUID REFERENCES core_city(id),

    -- 街道地址
    street          VARCHAR(60),
    street_2        VARCHAR(60),
    street_3        VARCHAR(60),
    street_4        VARCHAR(60),
    house_number    VARCHAR(10),
    building        VARCHAR(20),
    floor           VARCHAR(10),
    room            VARCHAR(10),

    -- 邮政
    postal_code     VARCHAR(10),
    po_box          VARCHAR(10),
    po_box_postal_code VARCHAR(10),

    -- 城市/区域
    city            VARCHAR(40),
    district        VARCHAR(40),

    -- 联系方式
    phone           VARCHAR(50),
    phone_2         VARCHAR(50),
    fax             VARCHAR(50),
    email           VARCHAR(100),

    -- 收货信息
    contact_person  VARCHAR(80),               -- 联系人
    delivery_note   VARCHAR(100),              -- 送货备注

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    address_status  general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (bp_id, address_type, valid_from)
);
```

**地址类型枚举**:

| 类型 | 说明 | 用途 |
|------|------|------|
| 01 | 默认地址 | 主地址 |
| 02 | 开票地址 | 发票寄送地址 |
| 03 | 送货地址 | 客户收货地址 |
| 04 | 收货地址 | 供应商发货地址 |
| 05 | 其他 | 备用地址 |

---

## 4. 客户属性

### 4.1 客户公司代码数据 (bp_customer)

对标 SAP KNB1

```sql
CREATE TABLE bp_customer (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    bp_id           UUID NOT NULL REFERENCES bp_business_partner(id) ON DELETE CASCADE,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 客户编码 (公司范围)
    customer_code   VARCHAR(10) NOT NULL,

    -- 统驭科目
    recon_account   VARCHAR(10) NOT NULL,      -- 统驭科目
    -- 应收账款:1122

    -- 付款条款
    payment_term    VARCHAR(4),                -- 付款条款
    payment_method  VARCHAR(2),                -- 付款方式

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),
    is_foreign      BOOLEAN DEFAULT FALSE,

    -- 信用
    credit_limit    DECIMAL(15,2) DEFAULT 0,   -- 信用额度
    credit_group    VARCHAR(4),                -- 信用组

    -- 税务
    tax_type        VARCHAR(2),                -- 税类型
    tax_number      VARCHAR(18),               -- 税号

    -- 财务
    sort_key        VARCHAR(3),                -- 排序码
    account_group   VARCHAR(4),                -- 账户组

    -- 交易控制
    is_reconciliation BOOLEAN DEFAULT FALSE,   -- 是否清算科目
    is_one_time     BOOLEAN DEFAULT FALSE,     -- 是否一次性客户

    -- 状态
    cust_status     general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (company_id, customer_code)
);
```

### 4.2 客户销售范围数据 (bp_customer_sales)

对标 SAP KNVV

```sql
CREATE TABLE bp_customer_sales (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    bp_id           UUID NOT NULL REFERENCES bp_business_partner(id) ON DELETE CASCADE,

    -- 销售范围
    sales_org_id    UUID NOT NULL REFERENCES sys_sales_org(id),
    distribution_channel VARCHAR(2),           -- 分销渠道
    division        VARCHAR(2),                -- 产品组

    -- 销售数据
    sales_group     VARCHAR(3),                -- 销售组
    sales_office    VARCHAR(4),                -- 销售办公室
    sales_district  VARCHAR(6),                -- 销售区域

    -- 定价
    price_group     VARCHAR(2),                -- 价格组
    price_list_type VARCHAR(1),                -- 价格表类型
    customer_group  VARCHAR(2),                -- 客户组
    customer_pricing_group VARCHAR(2),         -- 客户定价组

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 交货
    delivery_priority VARCHAR(2),              -- 交货优先级
    shipping_condition VARCHAR(2),             -- 装运条件
    delivery_plant_id UUID REFERENCES sys_plant(id), -- 交货工厂

    -- 国际贸易
    incoterms       VARCHAR(3),                -- 国际贸易条件
    incoterms_loc   VARCHAR(28),               -- 地点

    -- 状态
    sales_status    general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (bp_id, sales_org_id, distribution_channel, division)
);
```

---

## 5. 供应商属性

### 5.1 供应商公司代码数据 (bp_vendor)

对标 SAP LFB1

```sql
CREATE TABLE bp_vendor (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    bp_id           UUID NOT NULL REFERENCES bp_business_partner(id) ON DELETE CASCADE,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 供应商编码 (公司范围)
    vendor_code     VARCHAR(10) NOT NULL,

    -- 统驭科目
    recon_account   VARCHAR(10) NOT NULL,      -- 统驭科目
    -- 应付账款:2202

    -- 付款条款
    payment_term    VARCHAR(4),                -- 付款条款
    payment_method  VARCHAR(2),                -- 付款方式
    payment_block   VARCHAR(2),                -- 付款冻结

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),
    is_foreign      BOOLEAN DEFAULT FALSE,

    -- 采购
    purchasing_group VARCHAR(3),               -- 采购组

    -- 税务
    tax_type        VARCHAR(2),                -- 税类型
    tax_number      VARCHAR(18),               -- 税号

    -- 财务
    sort_key        VARCHAR(3),                -- 排序码
    account_group   VARCHAR(4),                -- 账户组

    -- 状态
    vendor_status   general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (company_id, vendor_code)
);
```

### 5.2 供应商采购组织数据 (bp_vendor_purchasing)

对标 SAP LFM1

```sql
CREATE TABLE bp_vendor_purchasing (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    bp_id           UUID NOT NULL REFERENCES bp_business_partner(id) ON DELETE CASCADE,
    purchasing_org_id UUID NOT NULL REFERENCES sys_purchasing_org(id),

    -- 采购数据
    purchasing_group VARCHAR(3),               -- 采购组
    schema_group    VARCHAR(2),                -- 计算模式组

    -- 订单
    min_order_qty   DECIMAL(13,3),             -- 最小订购量

    -- 交货
    planned_delivery_time INTEGER,             -- 计划交货时间 (天)
    gr_processing_time INTEGER,                -- 收货处理时间 (天)

    -- 评估
    quality_score   DECIMAL(3,1),              -- 质量评分
    delivery_score  DECIMAL(3,1),              -- 交货评分

    -- 状态
    is_blocked      BOOLEAN DEFAULT FALSE,     -- 采购冻结
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (bp_id, purchasing_org_id)
);
```

---

## 6. 银行账户

### 6.1 业务伙伴银行账户 (bp_bank_account)

对标 SAP BUT0BK

```sql
CREATE TABLE bp_bank_account (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    bp_id           UUID NOT NULL REFERENCES bp_business_partner(id) ON DELETE CASCADE,

    -- 银行信息
    bank_country    VARCHAR(3) NOT NULL,       -- 银行国家
    bank_key        VARCHAR(15) NOT NULL,      -- 银行代码
    bank_name       VARCHAR(100),              -- 银行名称
    bank_branch     VARCHAR(100),              -- 分行名称
    swift_code      VARCHAR(11),               -- SWIFT代码

    -- 账户信息
    account_number  VARCHAR(30) NOT NULL,      -- 账号
    account_holder  VARCHAR(100),              -- 户名
    account_type    VARCHAR(2),                -- 账户类型
    -- 01:储蓄 02:支票 03:其他

    -- 标识
    is_primary      BOOLEAN DEFAULT FALSE,     -- 主账户
    iban            VARCHAR(34),               -- IBAN

    -- 用途
    usage_type      VARCHAR(2),                -- 用途
    -- 01:全部 02:收款 03:付款

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    bank_status     general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (bp_id, bank_key, account_number)
);
```

---

## 7. 视图定义

### 7.1 客户主数据视图

```sql
CREATE VIEW v_bp_customer AS
SELECT
    bp.id AS bp_id,
    bp.bp_number,
    bp.name,
    bp.short_name,
    bp.bp_type,

    -- 角色信息
    r.role_type,
    r.is_primary,

    -- 公司数据
    c.company_id,
    c.customer_code,
    c.recon_account,
    c.payment_term,
    c.credit_limit,

    -- 主地址
    a.country_id,
    a.city,
    a.street,
    a.postal_code,
    a.phone,
    a.email,

    -- 税务
    bp.tax_number

FROM bp_business_partner bp
JOIN bp_role r ON r.bp_id = bp.id AND r.role_type = 'ZCUSTOMER'
LEFT JOIN bp_customer c ON c.bp_id = bp.id
LEFT JOIN bp_address a ON a.bp_id = bp.id AND a.address_type = '01'
WHERE bp.status = 'ACTIVE';
```

### 7.2 供应商主数据视图

```sql
CREATE VIEW v_bp_vendor AS
SELECT
    bp.id AS bp_id,
    bp.bp_number,
    bp.name,
    bp.short_name,
    bp.bp_type,

    -- 角色信息
    r.role_type,
    r.is_primary,

    -- 公司数据
    v.company_id,
    v.vendor_code,
    v.recon_account,
    v.payment_term,
    v.purchasing_group,

    -- 主地址
    a.country_id,
    a.city,
    a.street,
    a.postal_code,
    a.phone,
    a.email,

    -- 税务
    bp.tax_number

FROM bp_business_partner bp
JOIN bp_role r ON r.bp_id = bp.id AND r.role_type = 'ZVENDOR'
LEFT JOIN bp_vendor v ON v.bp_id = bp.id
LEFT JOIN bp_address a ON a.bp_id = bp.id AND a.address_type = '01'
WHERE bp.status = 'ACTIVE';
```

### 7.3 客户/供应商统一视图

```sql
CREATE VIEW v_bp_partner AS
SELECT
    bp.id,
    bp.tenant_id,
    bp.bp_number,
    bp.name,
    bp.short_name,
    bp.bp_type,
    bp.tax_number,

    -- 角色标识
    EXISTS(SELECT 1 FROM bp_role WHERE bp_id = bp.id AND role_type = 'ZCUSTOMER') AS is_customer,
    EXISTS(SELECT 1 FROM bp_role WHERE bp_id = bp.id AND role_type = 'ZVENDOR') AS is_vendor,

    -- 主地址
    a.country_id,
    a.city,
    a.street,
    a.postal_code,
    a.phone,
    a.email,

    bp.bp_status,
    bp.status

FROM bp_business_partner bp
LEFT JOIN bp_address a ON a.bp_id = bp.id AND a.address_type = '01'
WHERE bp.status = 'ACTIVE';
```

---

## 8. 存储过程

### 8.1 创建业务伙伴

```sql
CREATE OR REPLACE FUNCTION bp_create_business_partner(
    p_tenant_id UUID,
    p_bp_type VARCHAR,
    p_name VARCHAR,
    p_short_name VARCHAR DEFAULT NULL,
    p_tax_number VARCHAR DEFAULT NULL,
    p_user_id UUID DEFAULT NULL
) RETURNS UUID AS $$
DECLARE
    v_bp_id UUID;
    v_bp_number VARCHAR(10);
    v_role_type VARCHAR(6);
BEGIN
    -- 生成业务伙伴编号
    v_bp_number := next_val('bp_number_seq');

    -- 创建业务伙伴
    INSERT INTO bp_business_partner (
        tenant_id, bp_number, bp_type,
        name, short_name, tax_number,
        created_by, updated_by
    ) VALUES (
        p_tenant_id, v_bp_number, p_bp_type,
        p_name, p_short_name, p_tax_number,
        p_user_id, p_user_id
    ) RETURNING id INTO v_bp_id;

    -- 根据类型分配默认角色
    CASE p_bp_type
        WHEN 'O' THEN
            v_role_type := 'ZORGANIZATION';
        WHEN 'P' THEN
            v_role_type := 'ZPERSON';
        ELSE
            v_role_type := 'ZORGANIZATION';
    END CASE;

    -- 创建默认角色
    INSERT INTO bp_role (tenant_id, bp_id, role_type, is_primary)
    VALUES (p_tenant_id, v_bp_id, v_role_type, TRUE);

    -- 创建默认地址
    INSERT INTO bp_address (tenant_id, bp_id, address_type, is_default)
    VALUES (p_tenant_id, v_bp_id, '01', TRUE);

    RETURN v_bp_id;
END;
$$ LANGUAGE plpgsql;
```

### 8.2 添加客户角色

```sql
CREATE OR REPLACE FUNCTION bp_add_customer_role(
    p_bp_id UUID,
    p_company_id UUID,
    p_customer_code VARCHAR,
    p_recon_account VARCHAR,
    p_payment_term VARCHAR DEFAULT NULL,
    p_user_id UUID DEFAULT NULL
) RETURNS UUID AS $$
DECLARE
    v_tenant_id UUID;
    v_role_id UUID;
    v_cust_id UUID;
BEGIN
    -- 获取租户ID
    SELECT tenant_id INTO v_tenant_id
    FROM bp_business_partner WHERE id = p_bp_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION '业务伙伴不存在';
    END IF;

    -- 添加客户角色
    INSERT INTO bp_role (tenant_id, bp_id, role_type)
    VALUES (v_tenant_id, p_bp_id, 'ZCUSTOMER')
    ON CONFLICT (bp_id, role_type) DO NOTHING
    RETURNING id INTO v_role_id;

    -- 创建客户公司数据
    INSERT INTO bp_customer (
        tenant_id, bp_id, company_id,
        customer_code, recon_account, payment_term
    ) VALUES (
        v_tenant_id, p_bp_id, p_company_id,
        p_customer_code, p_recon_account, p_payment_term
    ) RETURNING id INTO v_cust_id;

    RETURN v_cust_id;
END;
$$ LANGUAGE plpgsql;
```

### 8.3 添加供应商角色

```sql
CREATE OR REPLACE FUNCTION bp_add_vendor_role(
    p_bp_id UUID,
    p_company_id UUID,
    p_vendor_code VARCHAR,
    p_recon_account VARCHAR,
    p_payment_term VARCHAR DEFAULT NULL,
    p_user_id UUID DEFAULT NULL
) RETURNS UUID AS $$
DECLARE
    v_tenant_id UUID;
    v_role_id UUID;
    v_vend_id UUID;
BEGIN
    -- 获取租户ID
    SELECT tenant_id INTO v_tenant_id
    FROM bp_business_partner WHERE id = p_bp_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION '业务伙伴不存在';
    END IF;

    -- 添加供应商角色
    INSERT INTO bp_role (tenant_id, bp_id, role_type)
    VALUES (v_tenant_id, p_bp_id, 'ZVENDOR')
    ON CONFLICT (bp_id, role_type) DO NOTHING
    RETURNING id INTO v_role_id;

    -- 创建供应商公司数据
    INSERT INTO bp_vendor (
        tenant_id, bp_id, company_id,
        vendor_code, recon_account, payment_term
    ) VALUES (
        v_tenant_id, p_bp_id, p_company_id,
        p_vendor_code, p_recon_account, p_payment_term
    ) RETURNING id INTO v_vend_id;

    RETURN v_vend_id;
END;
$$ LANGUAGE plpgsql;
```

---

## 9. 索引策略

```sql
-- 业务伙伴查询
CREATE INDEX idx_bp_number ON bp_business_partner (tenant_id, bp_number);
CREATE INDEX idx_bp_name ON bp_business_partner (tenant_id, name);
CREATE INDEX idx_bp_search ON bp_business_partner (tenant_id, search_term_1, search_term_2);

-- 角色查询
CREATE INDEX idx_bp_role_bp ON bp_role (bp_id);
CREATE INDEX idx_bp_role_type ON bp_role (role_type, role_status);

-- 地址查询
CREATE INDEX idx_bp_address_bp ON bp_address (bp_id);
CREATE INDEX idx_bp_address_city ON bp_address (city_id);

-- 客户/供应商查询
CREATE INDEX idx_bp_customer_company ON bp_customer (company_id, customer_code);
CREATE INDEX idx_bp_vendor_company ON bp_vendor (company_id, vendor_code);

-- 银行账户查询
CREATE INDEX idx_bp_bank_bp ON bp_bank_account (bp_id);
```

---

## 10. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
