# Tenant 模块数据库设计

**模块**: Tenant & Organization (多租户与组织架构)
**对标**: SAP 公司代码/工厂/成本中心架构
**版本**: 1.0

---

## 1. 模块概述

### 1.1 设计理念

NextERP 采用多租户 SaaS 架构，核心设计原则：

1. **租户隔离** - 通过 tenant_id + RLS 实现数据隔离
2. **组织层级** - 支持多公司、多工厂、多成本中心
3. **灵活扩展** - 组织架构可按需扩展
4. **审计追踪** - 所有组织变更可追溯

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                  Tenant Module Architecture                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                     sys_tenant                           │    │
│  │                     (系统租户)                            │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                     sys_company                          │    │
│  │                   (公司代码 - FI)                         │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │    │
│  │  │  公司代码   │  │  公司代码   │  │  公司代码   │      │    │
│  │  │  1000       │  │  2000       │  │  3000       │      │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │    │
│  └─────────────────────────────────────────────────────────┘    │
│        │                  │                  │                   │
│        ▼                  ▼                  ▼                   │
│  ┌──────────┐       ┌──────────┐       ┌──────────┐            │
│  │sys_plant │       │sys_plant │       │sys_plant │            │
│  │ (工厂)   │       │ (工厂)   │       │ (工厂)   │            │
│  │ 1001     │       │ 2001     │       │ 3001     │            │
│  └──────────┘       └──────────┘       └──────────┘            │
│        │                  │                  │                   │
│        ▼                  ▼                  ▼                   │
│  ┌──────────────────────────────────────────────────────┐       │
│  │                sys_cost_center                        │       │
│  │                   (成本中心)                           │       │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐    │       │
│  │  │ CC1001  │ │ CC1002  │ │ CC1003  │ │ CC1004  │    │       │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘    │       │
│  └──────────────────────────────────────────────────────┘       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 组织层次关系

```
sys_tenant (租户)
    │
    ├── sys_company (公司代码) ─────────────── 财务主体
    │       │
    │       ├── sys_plant (工厂) ───────────── 物流/生产主体
    │       │       │
    │       │       ├── sys_storage_location (库存地点)
    │       │       │
    │       │       └── sys_cost_center (成本中心) ─── 成本归集
    │       │
    │       ├── sys_sales_org (销售组织)
    │       │       │
    │       │       └── sys_sales_office (销售办公室)
    │       │
    │       └── sys_purchasing_org (采购组织)
    │
    └── sys_profit_center (利润中心) ───────── 利润归集
```

---

## 2. 租户管理

### 2.1 系统租户 (sys_tenant)

```sql
CREATE TABLE sys_tenant (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- 租户标识
    tenant_code     VARCHAR(10) NOT NULL UNIQUE, -- 租户编码
    tenant_name     VARCHAR(100) NOT NULL,       -- 租户名称

    -- 联系信息
    contact_person  VARCHAR(80),               -- 联系人
    phone           VARCHAR(50),               -- 电话
    email           VARCHAR(100),              -- 邮箱
    website         VARCHAR(100),              -- 网站

    -- 地址
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city_id         UUID REFERENCES core_city(id),
    street          VARCHAR(60),
    postal_code     VARCHAR(10),

    -- 订阅信息
    subscription_plan VARCHAR(20),             -- 订阅计划
    -- FREE:免费 BASIC:基础 PROFESSIONAL:专业 ENTERPRISE:企业
    max_users       INTEGER DEFAULT 10,        -- 最大用户数
    max_companies   INTEGER DEFAULT 1,         -- 最大公司数
    storage_quota   BIGINT DEFAULT 10737418240, -- 存储配额 (字节)

    -- 订阅时间
    subscribed_at   TIMESTAMP,                 -- 订阅时间
    expires_at      TIMESTAMP,                 -- 过期时间

    -- 状态
    tenant_status   VARCHAR(2) DEFAULT '01',   -- 01:试用 02:激活 03:冻结 04:注销
    status          general_status DEFAULT 'ACTIVE',

    -- 配置 (JSONB)
    config          JSONB DEFAULT '{}',        -- 租户配置

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_code)
);
```

### 2.2 租户配置示例

```json
{
    "modules": ["FI", "CO", "MM", "SD", "HR"],
    "features": {
        "multi_currency": true,
        "multi_language": true,
        "approval_workflow": true
    },
    "defaults": {
        "currency": "CNY",
        "language": "zh_CN",
        "timezone": "Asia/Shanghai"
    },
    "integrations": {
        "email": {
            "smtp_host": "smtp.example.com",
            "smtp_port": 587
        }
    }
}
```

---

## 3. 公司代码

### 3.1 公司代码 (sys_company)

对标 SAP T001

```sql
CREATE TABLE sys_company (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),

    -- 公司标识
    company_code    VARCHAR(4) NOT NULL,       -- 公司代码 (4位)
    company_name    VARCHAR(100) NOT NULL,     -- 公司名称
    short_name      VARCHAR(40),               -- 简称

    -- 法律实体
    legal_entity_type VARCHAR(4),              -- 法律形式
    registration_no VARCHAR(20),               -- 注册号
    tax_number      VARCHAR(18),               -- 税号
    legal_representative VARCHAR(80),          -- 法人代表

    -- 地址
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city_id         UUID REFERENCES core_city(id),
    street          VARCHAR(60),
    postal_code     VARCHAR(10),

    -- 联系信息
    phone           VARCHAR(50),
    fax             VARCHAR(50),
    email           VARCHAR(100),

    -- 财务
    currency_id     UUID NOT NULL REFERENCES core_currency(id), -- 本位币
    fiscal_year_variant VARCHAR(2),            -- 会计年度变式
    -- K4:4-4-5 V9:年度 V6:半年
    period_variant  VARCHAR(2),                -- 期间变式

    -- 控制
    is_retail       BOOLEAN DEFAULT FALSE,     -- 零售公司
    is_production   BOOLEAN DEFAULT TRUE,      -- 生产公司
    is_tax_enabled  BOOLEAN DEFAULT TRUE,      -- 税务功能

    -- 银行
    bank_name       VARCHAR(100),
    bank_account    VARCHAR(30),

    -- 状态
    company_status  general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, company_code)
);
```

---

## 4. 工厂管理

### 4.1 工厂 (sys_plant)

对标 SAP T001W

```sql
CREATE TABLE sys_plant (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 工厂标识
    plant_code      VARCHAR(4) NOT NULL,       -- 工厂代码 (4位)
    plant_name      VARCHAR(100) NOT NULL,     -- 工厂名称
    short_name      VARCHAR(40),               -- 简称

    -- 地址
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city_id         UUID REFERENCES core_city(id),
    street          VARCHAR(60),
    postal_code     VARCHAR(10),

    -- 联系信息
    phone           VARCHAR(50),
    fax             VARCHAR(50),
    email           VARCHAR(100),

    -- 工厂属性
    plant_type      VARCHAR(2),                -- 工厂类型
    -- 01:生产工厂 02:配送中心 03:服务中心 04:维护工厂
    is_mrp_plant    BOOLEAN DEFAULT TRUE,      -- MRP工厂
    is_batch_managed BOOLEAN DEFAULT FALSE,    -- 批次管理

    -- 评估
    valuation_area  VARCHAR(2) DEFAULT '01',   -- 评估范围
    -- 01:按工厂估值 02:按公司估值

    -- 工厂日历
    factory_calendar VARCHAR(2),               -- 工厂日历

    -- 仓库
    warehouse_id    UUID,                      -- 仓库号

    -- 状态
    plant_status    general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, plant_code)
);
```

### 4.2 库存地点 (sys_storage_location)

对标 SAP T001L

```sql
CREATE TABLE sys_storage_location (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    -- 库存地点标识
    sloc_code       VARCHAR(4) NOT NULL,       -- 库存地点代码 (4位)
    sloc_name       VARCHAR(100) NOT NULL,     -- 库存地点名称

    -- 类型
    sloc_type       VARCHAR(2),                -- 库存地点类型
    -- 01:原材料仓 02:成品仓 03:半成品仓 04:备件仓 05:退货仓

    -- 地址
    address         TEXT,                      -- 地址

    -- 仓库管理
    is_wm_managed   BOOLEAN DEFAULT FALSE,     -- 是否WM管理
    warehouse_number VARCHAR(3),               -- 仓库号

    -- 状态
    sloc_status     general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (plant_id, sloc_code)
);
```

---

## 5. 成本控制

### 5.1 成本中心 (sys_cost_center)

对标 SAP CSKS

```sql
CREATE TABLE sys_cost_center (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 成本中心标识
    cost_center_code VARCHAR(10) NOT NULL,     -- 成本中心代码
    cost_center_name VARCHAR(100) NOT NULL,    -- 成本中心名称

    -- 层级
    parent_id       UUID REFERENCES sys_cost_center(id),
    level           INTEGER DEFAULT 1,
    path            VARCHAR(500),              -- 层级路径

    -- 标准层次
    standard_hierarchy VARCHAR(10),            -- 标准层次节点

    -- 负责人
    manager_id      UUID,                      -- 负责人 (员工ID)
    department      VARCHAR(100),              -- 部门

    -- 关联
    plant_id        UUID REFERENCES sys_plant(id),
    profit_center_id UUID REFERENCES sys_profit_center(id),

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    cc_status       general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (company_id, cost_center_code)
);
```

### 5.2 利润中心 (sys_profit_center)

对标 SAP CEPC

```sql
CREATE TABLE sys_profit_center (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 利润中心标识
    profit_center_code VARCHAR(10) NOT NULL,   -- 利润中心代码
    profit_center_name VARCHAR(100) NOT NULL,  -- 利润中心名称

    -- 层级
    parent_id       UUID REFERENCES sys_profit_center(id),
    level           INTEGER DEFAULT 1,
    path            VARCHAR(500),

    -- 标准层次
    standard_hierarchy VARCHAR(10),

    -- 负责人
    manager_id      UUID,

    -- 关联
    plant_id        UUID REFERENCES sys_plant(id),

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    pc_status       general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (company_id, profit_center_code)
);
```

---

## 6. 销售与采购组织

### 6.1 销售组织 (sys_sales_org)

对标 SAP TVKO

```sql
CREATE TABLE sys_sales_org (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    -- 销售组织标识
    sales_org_code  VARCHAR(4) NOT NULL,       -- 销售组织代码
    sales_org_name  VARCHAR(100) NOT NULL,     -- 销售组织名称

    -- 描述
    description     TEXT,

    -- 地址
    address         TEXT,

    -- 状态
    sales_org_status general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, sales_org_code)
);
```

### 6.2 销售办公室 (sys_sales_office)

对标 SAP TVBUR

```sql
CREATE TABLE sys_sales_office (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    sales_org_id    UUID NOT NULL REFERENCES sys_sales_org(id),

    -- 销售办公室标识
    sales_office_code VARCHAR(4) NOT NULL,     -- 销售办公室代码
    sales_office_name VARCHAR(100) NOT NULL,   -- 销售办公室名称

    -- 描述
    description     TEXT,

    -- 地址
    address         TEXT,

    -- 销售组
    sales_group     VARCHAR(3),

    -- 状态
    office_status   general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (sales_org_id, sales_office_code)
);
```

### 6.3 采购组织 (sys_purchasing_org)

对标 SAP T024E

```sql
CREATE TABLE sys_purchasing_org (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES sys_tenant(id),
    company_id      UUID REFERENCES sys_company(id),

    -- 采购组织标识
    purch_org_code  VARCHAR(4) NOT NULL,       -- 采购组织代码
    purch_org_name  VARCHAR(100) NOT NULL,     -- 采购组织名称

    -- 类型
    purch_org_type  VARCHAR(2),                -- 采购组织类型
    -- 01:工厂采购组织 02:公司采购组织 03:跨公司采购组织

    -- 描述
    description     TEXT,

    -- 地址
    address         TEXT,

    -- 状态
    purch_org_status general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, purch_org_code)
);
```

---

## 7. 行级安全 (RLS)

### 7.1 启用 RLS

```sql
-- 为所有业务表启用 RLS
ALTER TABLE sys_company ENABLE ROW LEVEL SECURITY;
ALTER TABLE sys_plant ENABLE ROW LEVEL SECURITY;
ALTER TABLE sys_cost_center ENABLE ROW LEVEL SECURITY;
ALTER TABLE fi_journal_entry_hdr ENABLE ROW LEVEL SECURITY;
ALTER TABLE mm_material ENABLE ROW LEVEL SECURITY;
ALTER TABLE hr_employee ENABLE ROW LEVEL SECURITY;

-- 创建租户隔离策略
CREATE POLICY tenant_isolation ON sys_company
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE)::UUID);

CREATE POLICY tenant_isolation ON sys_plant
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE)::UUID);

CREATE POLICY tenant_isolation ON fi_journal_entry_hdr
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE)::UUID);

CREATE POLICY tenant_isolation ON mm_material
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE)::UUID);

CREATE POLICY tenant_isolation ON hr_employee
    USING (tenant_id = current_setting('app.current_tenant_id', TRUE)::UUID);
```

### 7.2 设置租户上下文

```sql
-- 应用层在连接后设置租户上下文
SET app.current_tenant_id = 'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx';
SET app.current_user_id = 'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx';
```

---

## 8. 视图定义

### 8.1 组织架构视图

```sql
CREATE VIEW v_sys_org_hierarchy AS
SELECT
    c.company_code,
    c.company_name,
    p.plant_code,
    p.plant_name,
    s.sloc_code,
    s.sloc_name,
    cc.cost_center_code,
    cc.cost_center_name,
    pc.profit_center_code,
    pc.profit_center_name

FROM sys_company c
LEFT JOIN sys_plant p ON p.company_id = c.id
LEFT JOIN sys_storage_location s ON s.plant_id = p.id
LEFT JOIN sys_cost_center cc ON cc.company_id = c.id
LEFT JOIN sys_profit_center pc ON pc.company_id = c.id
WHERE c.company_status = 'ACTIVE'
  AND (p.plant_status IS NULL OR p.plant_status = 'ACTIVE')
ORDER BY c.company_code, p.plant_code, s.sloc_code;
```

### 8.2 成本中心层级视图

```sql
CREATE VIEW v_sys_cost_center_tree AS
WITH RECURSIVE cc_tree AS (
    -- 根节点
    SELECT id, cost_center_code, cost_center_name,
           parent_id, company_id, level,
           ARRAY[cost_center_code] AS path_codes,
           ARRAY[cost_center_name] AS path_names
    FROM sys_cost_center
    WHERE parent_id IS NULL AND cc_status = 'ACTIVE'

    UNION ALL

    -- 递归
    SELECT cc.id, cc.cost_center_code, cc.cost_center_name,
           cc.parent_id, cc.company_id, cc.level,
           t.path_codes || cc.cost_center_code,
           t.path_names || cc.cost_center_name
    FROM sys_cost_center cc
    JOIN cc_tree t ON cc.parent_id = t.id
    WHERE cc.cc_status = 'ACTIVE'
)
SELECT * FROM cc_tree
ORDER BY path_codes;
```

---

## 9. 索引策略

```sql
-- 租户
CREATE INDEX idx_sys_tenant_code ON sys_tenant (tenant_code);
CREATE INDEX idx_sys_tenant_status ON sys_tenant (tenant_status);

-- 公司
CREATE INDEX idx_sys_company_tenant ON sys_company (tenant_id);
CREATE INDEX idx_sys_company_code ON sys_company (tenant_id, company_code);

-- 工厂
CREATE INDEX idx_sys_plant_company ON sys_plant (company_id);
CREATE INDEX idx_sys_plant_code ON sys_plant (tenant_id, plant_code);

-- 成本中心
CREATE INDEX idx_sys_cc_company ON sys_cost_center (company_id);
CREATE INDEX idx_sys_cc_parent ON sys_cost_center (parent_id);
CREATE INDEX idx_sys_cc_validity ON sys_cost_center (valid_from, valid_to);

-- 利润中心
CREATE INDEX idx_sys_pc_company ON sys_profit_center (company_id);
CREATE INDEX idx_sys_pc_parent ON sys_profit_center (parent_id);
```

---

## 10. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
