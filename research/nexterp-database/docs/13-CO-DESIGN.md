# CO 模块数据库设计

**模块**: Controlling (管理会计/控制)
**对标**: SAP ECC CO (KS01/KO01/KKA1)
**版本**: 1.0

---

## 1. 模块概述

### 1.1 业务范围

| 子模块 | 说明 | SAP 对标 |
|--------|------|----------|
| 成本要素 | 成本分类管理 | KA01/KA02 |
| 成本中心 | 成本中心会计 | KS01/KS02 |
| 内部订单 | 订单成本管理 | KO01/KO02 |
| 利润中心 | 利润中心会计 | KE51/KE52 |
| 产品成本 | 产品成本核算 | CK11N/CK24 |
| 获利分析 | CO-PA分析 | KEA0/KE30 |

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                     CO Module Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    成本要素层                            │    │
│  │  ┌─────────────┐  ┌─────────────┐                       │    │
│  │  │初级成本要素 │  │次级成本要素 │                       │    │
│  │  │co_cost_     │  │co_cost_     │                       │    │
│  │  │element_pri  │  │element_sec  │                       │    │
│  │  └─────────────┘  └─────────────┘                       │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    成本对象层                            │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │    │
│  │  │  成本中心   │  │  内部订单   │  │  利润中心   │      │    │
│  │  │co_cost_     │  │co_internal_ │  │co_profit_   │      │    │
│  │  │  center     │  │  order      │  │  center     │      │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│        ┌─────────────────────┼─────────────────────┐            │
│        ▼                     ▼                     ▼            │
│  ┌──────────┐          ┌──────────┐          ┌──────────┐      │
│  │ 成本分配 │          │ 产品成本 │          │ 获利分析 │      │
│  │co_alloc  │          │co_product│          │  co_pa   │      │
│  │          │          │  _cost   │          │          │      │
│  └──────────┘          └──────────┘          └──────────┘      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 与 FI 模块集成

```
┌─────────────────────────────────────────────────────────────────┐
│                     FI & CO Integration                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   FI 模块                           CO 模块                      │
│  ┌─────────────┐                   ┌─────────────┐              │
│  │ 会计科目    │─────────────────►│初级成本要素 │              │
│  │ fi_account  │     1:1 映射     │co_element   │              │
│  └─────────────┘                   └─────────────┘              │
│        │                                  │                      │
│        ▼                                  ▼                      │
│  ┌─────────────┐                   ┌─────────────┐              │
│  │ 凭证过账    │                   │ 成本对象    │              │
│  │ fi_journal  │─────────────────►│ 成本中心    │              │
│  │ _entry      │     实时传输     │ 内部订单    │              │
│  └─────────────┘                   └─────────────┘              │
│                                           │                      │
│                                           ▼                      │
│                                   ┌─────────────┐              │
│                                   │ 成本分配    │              │
│                                   │ 期末结算    │              │
│                                   └─────────────┘              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 成本要素

### 2.1 初级成本要素 (co_cost_element)

对标 SAP CSKA

```sql
CREATE TABLE co_cost_element (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 成本要素
    cost_element    VARCHAR(10) NOT NULL,      -- 成本要素代码
    name            VARCHAR(100) NOT NULL,     -- 名称
    description     TEXT,

    -- 类型
    element_type    VARCHAR(1) NOT NULL,       -- 类型
    -- 1:成本 2:收入 3:收入-销售扣除 4:成本-收入抵减

    -- 类别
    element_category VARCHAR(2),               -- 成本要素类别
    -- 01:初级成本/收入

    -- 科目关联
    gl_account_id   UUID REFERENCES fi_account(id),
    gl_account_code VARCHAR(10),

    -- 控制范围
    controlling_area VARCHAR(4),               -- 控制范围

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, cost_element, valid_from)
);
```

**常用成本要素**:

| 成本要素 | 名称 | 类型 | 科目 |
|----------|------|------|------|
| 400000 | 直接材料 | 1 | 6401 |
| 400001 | 直接人工 | 1 | 6402 |
| 400002 | 制造费用 | 1 | 6403 |
| 400003 | 折旧费用 | 1 | 6404 |
| 500000 | 销售收入 | 2 | 6001 |

### 2.2 次级成本要素 (co_cost_element_sec)

对标 SAP CSKU

```sql
CREATE TABLE co_cost_element_sec (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 成本要素
    cost_element    VARCHAR(10) NOT NULL,
    name            VARCHAR(100) NOT NULL,

    -- 类型
    element_category VARCHAR(2) NOT NULL,      -- 成本要素类别
    -- 21:分摊 22:作业分配 23:结算 42:订单结算 43:项目结算

    -- 控制范围
    controlling_area VARCHAR(4),

    -- 用途
    allocation_type VARCHAR(2),                -- 分配类型
    -- 01:分摊 02:作业 03:结算

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, cost_element)
);
```

---

## 3. 成本中心会计

### 3.1 成本中心 (已在 sys_cost_center 定义)

主要字段回顾：
- cost_center_code - 成本中心代码
- cost_center_name - 成本中心名称
- company_id - 公司
- parent_id - 父成本中心
- manager_id - 负责人
- profit_center_id - 关联利润中心

### 3.2 成本中心成本 (co_cost_center_cost)

```sql
CREATE TABLE co_cost_center_cost (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 成本中心
    cost_center_id  UUID NOT NULL REFERENCES sys_cost_center(id),
    cost_center_code VARCHAR(10),

    -- 期间
    fiscal_year     INTEGER NOT NULL,
    period          INTEGER NOT NULL,

    -- 成本要素
    cost_element_id UUID REFERENCES co_cost_element(id),
    cost_element    VARCHAR(10),
    element_type    VARCHAR(1),

    -- 金额
    planned_cost    DECIMAL(15,2) DEFAULT 0,   -- 计划成本
    actual_cost     DECIMAL(15,2) DEFAULT 0,   -- 实际成本
    allocated_cost  DECIMAL(15,2) DEFAULT 0,   -- 分配成本
    variance        DECIMAL(15,2) GENERATED ALWAYS AS (
        actual_cost - allocated_cost
    ) STORED,                                   -- 差异

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 版本
    version         INTEGER DEFAULT 0,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (cost_center_id, fiscal_year, period, cost_element)
);
```

### 3.3 作业类型 (co_activity_type)

对标 SAP CSLA

```sql
CREATE TABLE co_activity_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 作业类型
    activity_type   VARCHAR(6) NOT NULL,       -- 作业类型代码
    name            VARCHAR(100) NOT NULL,     -- 名称
    description     TEXT,

    -- 单位
    unit            VARCHAR(3) NOT NULL,       -- 作业单位

    -- 类别
    activity_category VARCHAR(1),              -- 作业类别
    -- 1:人工 2:机器 3:其他

    -- 成本中心
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    cost_center_code VARCHAR(10),

    -- 价格
    plan_price      DECIMAL(15,4),             -- 计划价格
    plan_price_unit INTEGER DEFAULT 1,         -- 价格单位

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, activity_type)
);
```

**常用作业类型**:

| 作业类型 | 名称 | 单位 | 类别 |
|----------|------|------|------|
| LAB001 | 直接人工 | H | 1 |
| LAB002 | 间接人工 | H | 1 |
| MAC001 | 机器工时 | H | 2 |
| MAC002 | 设备使用 | H | 2 |
| OTH001 | 管理费分摊 | % | 3 |

### 3.4 作业价格 (co_activity_price)

```sql
CREATE TABLE co_activity_price (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 作业类型
    activity_type_id UUID NOT NULL REFERENCES co_activity_type(id),
    activity_type   VARCHAR(6),

    -- 成本中心
    cost_center_id  UUID REFERENCES sys_cost_center(id),

    -- 期间
    fiscal_year     INTEGER NOT NULL,
    period_from     INTEGER NOT NULL,          -- 起始期间
    period_to       INTEGER NOT NULL,          -- 结束期间

    -- 价格
    plan_price      DECIMAL(15,4) NOT NULL,    -- 计划价格
    actual_price    DECIMAL(15,4),             -- 实际价格
    price_unit      INTEGER DEFAULT 1,

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 状态
    price_status    VARCHAR(2) DEFAULT '01',   -- 01:计划 02:已释放

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (activity_type_id, cost_center_id, fiscal_year, period_from)
);
```

---

## 4. 内部订单

### 4.1 内部订单头 (co_internal_order_hdr)

对标 SAP AUFK

```sql
CREATE TABLE co_internal_order_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 订单信息
    order_number    VARCHAR(12) NOT NULL,      -- 订单号
    order_type      VARCHAR(4) NOT NULL,       -- 订单类型
    -- IO01:间接费用订单 IO02:投资订单 IO03:维修订单 IO04:研发订单

    -- 描述
    description     VARCHAR(100) NOT NULL,

    -- 组织
    company_id      UUID NOT NULL REFERENCES sys_company(id),
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    profit_center_id UUID REFERENCES sys_profit_center(id),

    -- 控制范围
    controlling_area VARCHAR(4),

    -- 负责人
    responsible_id  UUID REFERENCES hr_employee(id),

    -- 日期
    start_date      DATE,
    end_date        DATE,

    -- 预算
    total_budget    DECIMAL(15,2) DEFAULT 0,   -- 总预算
    committed_cost  DECIMAL(15,2) DEFAULT 0,   -- 承诺成本
    actual_cost     DECIMAL(15,2) DEFAULT 0,   -- 实际成本

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 结算规则
    settlement_rule VARCHAR(2),                -- 结算规则
    settlement_receiver VARCHAR(24),           -- 结算接收方

    -- 状态
    order_status    VARCHAR(2) DEFAULT '01',   -- 01:创建 02:释放 03:完成 04:结算
    system_status   VARCHAR(10),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, order_number)
);
```

### 4.2 订单成本 (co_order_cost)

```sql
CREATE TABLE co_order_cost (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 订单
    order_id        UUID NOT NULL REFERENCES co_internal_order_hdr(id),
    order_number    VARCHAR(12),

    -- 期间
    fiscal_year     INTEGER NOT NULL,
    period          INTEGER NOT NULL,

    -- 成本要素
    cost_element_id UUID REFERENCES co_cost_element(id),
    cost_element    VARCHAR(10),

    -- 金额
    planned_cost    DECIMAL(15,2) DEFAULT 0,
    actual_cost     DECIMAL(15,2) DEFAULT 0,
    committed_cost  DECIMAL(15,2) DEFAULT 0,
    settled_cost    DECIMAL(15,2) DEFAULT 0,

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (order_id, fiscal_year, period, cost_element)
);
```

---

## 5. 利润中心会计

### 5.1 利润中心 (已在 sys_profit_center 定义)

### 5.2 利润中心余额 (co_profit_center_balance)

```sql
CREATE TABLE co_profit_center_balance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 利润中心
    profit_center_id UUID NOT NULL REFERENCES sys_profit_center(id),
    profit_center_code VARCHAR(10),

    -- 期间
    fiscal_year     INTEGER NOT NULL,
    period          INTEGER NOT NULL,

    -- 金额
    revenue         DECIMAL(15,2) DEFAULT 0,   -- 收入
    cost_of_sales   DECIMAL(15,2) DEFAULT 0,   -- 销售成本
    gross_profit    DECIMAL(15,2) GENERATED ALWAYS AS (
        revenue - cost_of_sales
    ) STORED,                                   -- 毛利
    operating_cost  DECIMAL(15,2) DEFAULT 0,   -- 运营成本
    net_profit      DECIMAL(15,2) GENERATED ALWAYS AS (
        revenue - cost_of_sales - operating_cost
    ) STORED,                                   -- 净利润

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (profit_center_id, fiscal_year, period)
);
```

---

## 6. 产品成本核算

### 6.1 成本估算头 (co_cost_estimate_hdr)

对标 SAP KEKO

```sql
CREATE TABLE co_cost_estimate_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 估算信息
    estimate_number VARCHAR(12) NOT NULL,      -- 估算号
    estimate_type   VARCHAR(2) NOT NULL,       -- 估算类型
    -- 01:标准成本估算 02:修改估算 03:销售订单估算

    -- 物料
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    -- 版本
    costing_version VARCHAR(3),                -- 成本核算版本

    -- 日期
    costing_date    DATE NOT NULL,             -- 核算日期
    valid_from      DATE,                      -- 有效起始
    valid_to        DATE,                      -- 有效结束

    -- 数量
    costing_lot_size DECIMAL(13,3) DEFAULT 1,  -- 成本批量

    -- 金额
    total_cost      DECIMAL(15,2) NOT NULL,    -- 总成本
    material_cost   DECIMAL(15,2) DEFAULT 0,   -- 材料成本
    labor_cost      DECIMAL(15,2) DEFAULT 0,   -- 人工成本
    machine_cost    DECIMAL(15,2) DEFAULT 0,   -- 机器成本
    overhead_cost   DECIMAL(15,2) DEFAULT 0,   -- 制造费用
    external_cost   DECIMAL(15,2) DEFAULT 0,   -- 外协成本

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 状态
    estimate_status VARCHAR(2) DEFAULT '01',   -- 01:创建 02:审批 03:标记 04:发布

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, estimate_number)
);
```

### 6.2 成本估算项 (co_cost_estimate_itm)

对标 SAP KEPH

```sql
CREATE TABLE co_cost_estimate_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 估算头
    header_id       UUID NOT NULL REFERENCES co_cost_estimate_hdr(id) ON DELETE CASCADE,
    estimate_number VARCHAR(12),

    -- 层级
    level_number    INTEGER NOT NULL,          -- 层级
    item_number     INTEGER NOT NULL,          -- 项目号

    -- 成本类别
    cost_category   VARCHAR(4) NOT NULL,       -- 成本类别
    -- M001:原材料 M002:半成品 L001:人工 L002:准备人工
    -- M001:机器 V001:外协 O001:制造费用

    -- 成本要素
    cost_element_id UUID REFERENCES co_cost_element(id),
    cost_element    VARCHAR(10),

    -- 来源
    source_type     VARCHAR(2),                -- 来源类型
    -- 01:BOM 02:工艺 03:采购信息记录 04:手工

    -- 金额
    total_cost      DECIMAL(15,2) NOT NULL,    -- 总成本
    fixed_cost      DECIMAL(15,2) DEFAULT 0,   -- 固定成本
    variable_cost   DECIMAL(15,2) DEFAULT 0,   -- 变动成本

    -- 数量
    quantity        DECIMAL(13,3),
    unit            VARCHAR(3),
    unit_cost       DECIMAL(15,4),             -- 单位成本

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, level_number, item_number)
);
```

---

## 7. 成本分配

### 7.1 分配规则 (co_allocation_rule)

```sql
CREATE TABLE co_allocation_rule (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 规则信息
    rule_code       VARCHAR(6) NOT NULL,       -- 规则代码
    rule_name       VARCHAR(100) NOT NULL,     -- 规则名称

    -- 类型
    allocation_type VARCHAR(2) NOT NULL,       -- 分配类型
    -- 01:分配 02:分摊 03:作业分配 04:间接费用

    -- 发送方
    sender_type     VARCHAR(2) NOT NULL,       -- 发送方类型
    -- CC:成本中心 OR:内部订单
    sender_id       UUID,
    sender_code     VARCHAR(20),

    -- 成本要素
    cost_element_id UUID REFERENCES co_cost_element(id),
    cost_element    VARCHAR(10),
    sec_cost_element_id UUID REFERENCES co_cost_element_sec(id), -- 次级成本要素

    -- 接收方类型
    receiver_type   VARCHAR(2) NOT NULL,       -- 接收方类型
    -- CC:成本中心 OR:内部订单 PC:利润中心

    -- 分配基础
    allocation_base VARCHAR(2),                -- 分配基础
    -- 01:固定比例 02:实际金额 03:统计指标 04:作业量

    -- 期间
    period_from     INTEGER DEFAULT 1,
    period_to       INTEGER DEFAULT 12,

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, rule_code)
);
```

### 7.2 分配规则接收方 (co_allocation_receiver)

```sql
CREATE TABLE co_allocation_receiver (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 规则
    rule_id         UUID NOT NULL REFERENCES co_allocation_rule(id) ON DELETE CASCADE,

    -- 接收方
    receiver_id     UUID NOT NULL,
    receiver_code   VARCHAR(20),

    -- 分配比例
    share_percent   DECIMAL(5,2) NOT NULL,     -- 分配比例%
    share_amount    DECIMAL(15,2),             -- 固定金额

    -- 接收方成本要素
    receiver_cost_element VARCHAR(10),

    -- 有效期
    valid_from      DATE DEFAULT CURRENT_DATE,
    valid_to        DATE DEFAULT '9999-12-31',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 7.3 分配运行 (co_allocation_run)

```sql
CREATE TABLE co_allocation_run (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 运行信息
    run_number      VARCHAR(10) NOT NULL,
    run_type        VARCHAR(2),                -- 运行类型
    -- 01:计划 02:实际

    -- 规则
    rule_id         UUID REFERENCES co_allocation_rule(id),
    rule_code       VARCHAR(6),

    -- 期间
    fiscal_year     INTEGER NOT NULL,
    period          INTEGER NOT NULL,

    -- 金额
    total_allocated DECIMAL(15,2),             -- 总分配金额
    sender_count    INTEGER,                   -- 发送方数
    receiver_count  INTEGER,                   -- 接收方数

    -- 运行时间
    run_date        DATE NOT NULL DEFAULT CURRENT_DATE,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,

    -- 状态
    run_status      VARCHAR(2) DEFAULT '01',   -- 01:运行中 02:完成 03:错误

    -- 凭证
    fi_document_id  UUID,                      -- 生成的FI凭证

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, run_number)
);
```

---

## 8. 获利分析 (CO-PA)

### 8.1 获利段 (co_profitability_segment)

```sql
CREATE TABLE co_profitability_segment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 获利段编码
    segment_code    VARCHAR(30) NOT NULL,      -- 获利段编码

    -- 维度
    company_id      UUID REFERENCES sys_company(id),
    profit_center_id UUID REFERENCES sys_profit_center(id),
    customer_id     UUID REFERENCES bp_business_partner(id),
    material_id     UUID REFERENCES mm_material(id),
    sales_org_id    UUID REFERENCES sys_sales_org(id),
    distribution_channel VARCHAR(2),
    division        VARCHAR(2),

    -- 产品组
    product_group   VARCHAR(4),

    -- 地区
    region_id       UUID REFERENCES core_region(id),
    country_id      UUID REFERENCES core_country(id),

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, segment_code)
);
```

### 8.2 获利分析数据 (co_pa_data)

对标 SAP CE1XXXX

```sql
CREATE TABLE co_pa_data (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 获利段
    segment_id      UUID NOT NULL REFERENCES co_profitability_segment(id),
    segment_code    VARCHAR(30),

    -- 期间
    fiscal_year     INTEGER NOT NULL,
    period          INTEGER NOT NULL,

    -- 凭证来源
    record_type     VARCHAR(1),                -- 记录类型
    -- A:实际 B:计划 F:FI凭证 V:销售凭证

    -- 来源单据
    source_type     VARCHAR(2),                -- 来源类型
    source_document VARCHAR(20),
    source_item     INTEGER,

    -- 销售数据
    sales_qty       DECIMAL(13,3),             -- 销售数量
    sales_unit      VARCHAR(3),
    revenue         DECIMAL(15,2),             -- 销售收入
    cost_of_sales   DECIMAL(15,2),             -- 销售成本
    gross_profit    DECIMAL(15,2),             -- 毛利

    -- 成本明细
    material_cost   DECIMAL(15,2),             -- 材料成本
    labor_cost      DECIMAL(15,2),             -- 人工成本
    overhead_cost   DECIMAL(15,2),             -- 制造费用

    -- 折扣/返利
    discount_amount DECIMAL(15,2),             -- 折扣金额
    rebate_amount   DECIMAL(15,2),             -- 返利金额

    -- 贡献毛利
    contribution_margin DECIMAL(15,2),         -- 贡献毛利

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 日期
    posting_date    DATE NOT NULL,
    document_date   DATE,

    -- 客户信息 (冗余)
    customer_group  VARCHAR(2),
    customer_hierarchy VARCHAR(10),

    -- 产品信息 (冗余)
    material_group  VARCHAR(9),
    material_type   VARCHAR(4),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 9. 视图定义

### 9.1 成本中心成本报表视图

```sql
CREATE VIEW v_co_cost_center_report AS
SELECT
    cc.cost_center_code,
    cc.cost_center_name,
    c.fiscal_year,
    c.period,
    ce.cost_element,
    ce.name AS cost_element_name,
    ce.element_type,
    c.planned_cost,
    c.actual_cost,
    c.allocated_cost,
    c.variance,
    ROUND(c.variance / NULLIF(c.actual_cost, 0) * 100, 2) AS variance_pct

FROM co_cost_center_cost c
JOIN sys_cost_center cc ON cc.id = c.cost_center_id
JOIN co_cost_element ce ON ce.id = c.cost_element_id
ORDER BY cc.cost_center_code, c.fiscal_year, c.period, ce.cost_element;
```

### 9.2 产品成本明细视图

```sql
CREATE VIEW v_co_product_cost_detail AS
SELECT
    e.estimate_number,
    e.estimate_type,
    m.material_code,
    m.description AS material_desc,
    p.plant_code,
    e.costing_date,
    e.total_cost,
    e.material_cost,
    e.labor_cost,
    e.machine_cost,
    e.overhead_cost,
    e.total_cost / e.costing_lot_size AS unit_cost,
    e.currency_id,
    e.estimate_status

FROM co_cost_estimate_hdr e
JOIN mm_material m ON m.id = e.material_id
JOIN sys_plant p ON p.id = e.plant_id
ORDER BY e.costing_date DESC;
```

### 9.3 获利分析报表视图

```sql
CREATE VIEW v_co_pa_report AS
SELECT
    p.fiscal_year,
    p.period,
    s.segment_code,
    c.company_code,
    pc.profit_center_code,
    bp.name AS customer_name,
    m.material_code,
    m.description AS material_desc,

    SUM(p.sales_qty) AS total_sales_qty,
    SUM(p.revenue) AS total_revenue,
    SUM(p.cost_of_sales) AS total_cogs,
    SUM(p.gross_profit) AS total_gross_profit,
    ROUND(SUM(p.gross_profit) / NULLIF(SUM(p.revenue), 0) * 100, 2) AS gross_margin_pct,

    SUM(p.contribution_margin) AS total_contribution

FROM co_pa_data p
JOIN co_profitability_segment s ON s.id = p.segment_id
LEFT JOIN sys_company c ON c.id = s.company_id
LEFT JOIN sys_profit_center pc ON pc.id = s.profit_center_id
LEFT JOIN bp_business_partner bp ON bp.id = s.customer_id
LEFT JOIN mm_material m ON m.id = s.material_id
GROUP BY p.fiscal_year, p.period, s.segment_code,
         c.company_code, pc.profit_center_code, bp.name,
         m.material_code, m.description
ORDER BY p.fiscal_year, p.period;
```

---

## 10. 索引策略

```sql
-- 成本要素
CREATE INDEX idx_co_ce_code ON co_cost_element (tenant_id, cost_element);
CREATE INDEX idx_co_ce_type ON co_cost_element (element_type);

-- 成本中心成本
CREATE INDEX idx_co_ccc_cc ON co_cost_center_cost (cost_center_id);
CREATE INDEX idx_co_ccc_period ON co_cost_center_cost (fiscal_year, period);
CREATE INDEX idx_co_ccc_element ON co_cost_center_cost (cost_element_id);

-- 内部订单
CREATE INDEX idx_co_io_number ON co_internal_order_hdr (tenant_id, order_number);
CREATE INDEX idx_co_io_type ON co_internal_order_hdr (order_type);
CREATE INDEX idx_co_io_status ON co_internal_order_hdr (order_status);

-- 利润中心余额
CREATE INDEX idx_co_pcb_pc ON co_profit_center_balance (profit_center_id);
CREATE INDEX idx_co_pcb_period ON co_profit_center_balance (fiscal_year, period);

-- 成本估算
CREATE INDEX idx_co_ce_hdr_material ON co_cost_estimate_hdr (material_id, plant_id);
CREATE INDEX idx_co_ce_hdr_date ON co_cost_estimate_hdr (costing_date);

-- 分配规则
CREATE INDEX idx_co_alloc_rule_code ON co_allocation_rule (tenant_id, rule_code);
CREATE INDEX idx_co_alloc_rule_type ON co_allocation_rule (allocation_type);

-- CO-PA
CREATE INDEX idx_co_pa_segment ON co_pa_data (segment_id);
CREATE INDEX idx_co_pa_period ON co_pa_data (fiscal_year, period);
CREATE INDEX idx_co_pa_date ON co_pa_data (posting_date);
```

---

## 11. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
