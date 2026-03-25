# SCM 模块数据库设计

**模块**: Supply Chain Management (供应链管理)
**对标**: SAP SCM / Oracle SCM Cloud / Kinaxis
**版本**: 1.0

---

## 1. 模块概述

### 1.1 业务范围

| 子模块 | 说明 | 功能 |
|--------|------|------|
| 需求管理 | 需求预测与计划 | 需求预测、销售计划、需求汇总 |
| 供应计划 | 供应网络规划 | 供应计划、产能规划、供应商分配 |
| 库存优化 | 库存策略优化 | 安全库存、补货策略、库存分布 |
| 物流管理 | 运输与配送 | 发货管理、运输跟踪、物流协同 |
| 供应链协同 | 外部协作 | 供应商协同、VMI、CPFR |

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     SCM Module Architecture                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                       需求管理 (Demand Management)                    │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 销售预测 │  │ 需求计划 │  │ 预测调整 │  │ 需求汇总 │            │   │
│  │  │ Forecast │  │ Plan     │  │ Adjust   │  │ Consolid │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                       供应计划 (Supply Planning)                      │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 供应计划 │  │ 产能规划 │  │ MRP运行  │  │ 供应商分配│            │   │
│  │  │SupplyPlan│ │ Capacity │  │  MRP     │  │ Allocation│            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                       库存优化 (Inventory Optimization)               │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 库存策略 │  │ 安全库存 │  │ 补货计划 │  │ 库存分布 │            │   │
│  │  │ Policy   │  │SafetyStk │  │ Replenish│  │ Distribution│          │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                       物流管理 (Logistics)                            │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 发货管理 │  │ 运输计划 │  │ 物流跟踪 │  │ 配送优化 │            │   │
│  │  │ Shipment │  │Transport │  │ Tracking │  │ Delivery │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.3 SCM 与 ERP 集成

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     SCM & ERP Integration                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   SCM 模块                           ERP 模块                                │
│  ┌─────────────┐                   ┌─────────────┐                          │
│  │ 需求预测     │                   │ SD 销售订单  │                          │
│  │ Forecast    │◄─────────────────│ Sales Order │                          │
│  └─────────────┘                   └─────────────┘                          │
│        │                                  │                                  │
│        ▼                                  ▼                                  │
│  ┌─────────────┐                   ┌─────────────┐                          │
│  │ 供应计划     │─────────────────►│ PP 生产计划  │                          │
│  │ Supply Plan │                   │ MRP/Planned │                          │
│  └─────────────┘                   └─────────────┘                          │
│        │                                  │                                  │
│        ▼                                  ▼                                  │
│  ┌─────────────┐                   ┌─────────────┐                          │
│  │ 库存优化     │◄────────────────►│ MM 库存管理  │                          │
│  │ Inventory   │                   │ Stock       │                          │
│  └─────────────┘                   └─────────────┘                          │
│        │                                  │                                  │
│        ▼                                  ▼                                  │
│  ┌─────────────┐                   ┌─────────────┐                          │
│  │ 物流管理     │─────────────────►│ WM 仓库管理  │                          │
│  │ Logistics   │                   │ Warehouse   │                          │
│  └─────────────┘                   └─────────────┘                          │
│                                                                              │
│   集成方式:                                                                   │
│   • API调用: 实时同步需求、库存数据                                           │
│   • 事件驱动: 订单、发货事件触发计划更新                                       │
│   • 批量作业: 定期运行MRP、预测计算                                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 需求管理

### 2.1 需求预测 (scm_demand_forecast)

```sql
CREATE TABLE scm_demand_forecast (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 预测标识
    forecast_number VARCHAR(12) NOT NULL,      -- 预测编号
    forecast_name   VARCHAR(100) NOT NULL,     -- 预测名称
    forecast_type   VARCHAR(2),                -- 预测类型
    -- 01:销售预测 02:需求预测 03:补货预测

    -- 预测版本
    version         INTEGER DEFAULT 1,         -- 版本号
    baseline_id     UUID REFERENCES scm_demand_forecast(id), -- 基线版本

    -- 期间
    forecast_method VARCHAR(2),                -- 预测方法
    -- 01:移动平均 02:指数平滑 03:ARIMA 04:机器学习 05:人工判断
    period_type     VARCHAR(2) NOT NULL,       -- 期间类型
    -- 01:日 02:周 03:月 04:季 05:年
    start_date      DATE NOT NULL,             -- 开始日期
    end_date        DATE NOT NULL,             -- 结束日期

    -- 状态
    forecast_status VARCHAR(2) DEFAULT '01',   -- 预测状态
    -- 01:草稿 02:已发布 03:已归档 04:已过期

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    released_at     TIMESTAMP,
    released_by     UUID,

    UNIQUE (tenant_id, forecast_number, version)
);
```

### 2.2 预测明细 (scm_forecast_item)

```sql
CREATE TABLE scm_forecast_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 预测头
    forecast_id     UUID NOT NULL REFERENCES scm_demand_forecast(id) ON DELETE CASCADE,

    -- 产品维度
    product_id      UUID REFERENCES mm_material(id),      -- 产品
    product_group   VARCHAR(9),                           -- 产品组
    plant_id        UUID REFERENCES sys_plant(id),        -- 工厂
    customer_id     UUID REFERENCES bp_business_partner(id), -- 客户
    region_id       UUID REFERENCES core_region(id),      -- 区域

    -- 时间维度
    forecast_date   DATE NOT NULL,                        -- 预测日期
    period_year     INTEGER NOT NULL,                     -- 年度
    period_month    INTEGER,                              -- 月份
    period_week     INTEGER,                              -- 周次

    -- 预测数量
    forecast_qty    DECIMAL(13,3) NOT NULL,               -- 预测数量
    unit            VARCHAR(3),                           -- 单位

    -- 预测金额
    unit_price      DECIMAL(15,2),                        -- 单价
    forecast_amount DECIMAL(15,2),                        -- 预测金额
    currency_id     UUID REFERENCES core_currency(id),

    -- 调整
    original_qty    DECIMAL(13,3),                        -- 原始预测
    adjustment_qty  DECIMAL(13,3) DEFAULT 0,              -- 调整数量
    adjustment_reason VARCHAR(200),                       -- 调整原因

    -- 准确度跟踪
    actual_qty      DECIMAL(13,3),                        -- 实际数量
    accuracy_rate   DECIMAL(5,2),                         -- 准确率%
    variance_pct    DECIMAL(5,2),                         -- 偏差%

    -- 置信度
    confidence_level VARCHAR(2),                          -- 置信度
    -- 01:高 02:中 03:低
    lower_bound     DECIMAL(13,3),                        -- 下限
    upper_bound     DECIMAL(13,3),                        -- 上限

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_scm_forecast_item_date ON scm_forecast_item (forecast_date);
CREATE INDEX idx_scm_forecast_item_product ON scm_forecast_item (product_id);
CREATE INDEX idx_scm_forecast_item_plant ON scm_forecast_item (plant_id);
```

### 2.3 需求计划 (scm_demand_plan)

```sql
CREATE TABLE scm_demand_plan (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 计划标识
    plan_number     VARCHAR(12) NOT NULL,      -- 计划编号
    plan_name       VARCHAR(100) NOT NULL,     -- 计划名称
    plan_type       VARCHAR(2),                -- 计划类型
    -- 01:S&OP计划 02:需求计划 03:分销计划

    -- 期间
    planning_horizon INTEGER NOT NULL,         -- 计划跨度(天)
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,

    -- 来源
    source_type     VARCHAR(2),                -- 来源类型
    -- 01:销售订单 02:预测 03:订单+预测
    forecast_id     UUID REFERENCES scm_demand_forecast(id),

    -- 汇总级别
    aggregation_level VARCHAR(2),              -- 汇总级别
    -- 01:产品 02:产品组 03:产品线 04:客户 05:区域

    -- 状态
    plan_status     VARCHAR(2) DEFAULT '01',   -- 计划状态
    -- 01:草稿 02:已审批 03:已发布 04:已关闭

    -- 审批
    approved_by     UUID,
    approved_at     TIMESTAMP,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, plan_number)
);
```

### 2.4 需求计划明细 (scm_demand_plan_item)

```sql
CREATE TABLE scm_demand_plan_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 计划头
    plan_id         UUID NOT NULL REFERENCES scm_demand_plan(id) ON DELETE CASCADE,

    -- 维度
    product_id      UUID REFERENCES mm_material(id),
    plant_id        UUID REFERENCES sys_plant(id),
    customer_id     UUID REFERENCES bp_business_partner(id),

    -- 时间
    plan_date       DATE NOT NULL,
    period_year     INTEGER NOT NULL,
    period_month    INTEGER,
    period_week     INTEGER,

    -- 需求数量
    demand_qty      DECIMAL(13,3) NOT NULL,    -- 需求数量
    unit            VARCHAR(3),

    -- 需求分解
    confirmed_qty   DECIMAL(13,3) DEFAULT 0,   -- 确认数量
    planned_qty     DECIMAL(13,3) DEFAULT 0,   -- 计划数量
    open_qty        DECIMAL(13,3) GENERATED ALWAYS AS (
        demand_qty - confirmed_qty - planned_qty
    ) STORED,

    -- 优先级
    priority        VARCHAR(2),                -- 优先级
    -- 01:紧急 02:高 03:中 04:低

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (plan_id, product_id, plant_id, plan_date)
);
```

---

## 3. 供应计划

### 3.1 供应计划 (scm_supply_plan)

```sql
CREATE TABLE scm_supply_plan (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 计划标识
    plan_number     VARCHAR(12) NOT NULL,
    plan_name       VARCHAR(100) NOT NULL,
    plan_type       VARCHAR(2),                -- 计划类型
    -- 01:MPS主生产计划 02:MRP物料需求计划 03:DRP分销需求计划

    -- 期间
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    planning_horizon INTEGER NOT NULL,         -- 计划跨度(天)

    -- 运行参数
    run_mode        VARCHAR(2),                -- 运行模式
    -- 01:净改变 02:再生式 03:计划模拟
    scope           VARCHAR(2),                -- 计划范围
    -- 01:全工厂 02:指定物料 03:指定产品组

    -- MRP参数
    planning_mode   VARCHAR(2),                -- 计划模式
    -- 01:倒排 02:正排 03:混合
    consider_safety_stock BOOLEAN DEFAULT TRUE,
    consider_lead_time BOOLEAN DEFAULT TRUE,

    -- 状态
    plan_status     VARCHAR(2) DEFAULT '01',   -- 计划状态
    -- 01:运行中 02:已完成 03:已审批 04:已发布 05:错误

    -- 运行统计
    run_start_time  TIMESTAMP,
    run_end_time    TIMESTAMP,
    run_duration    INTEGER,                   -- 运行时长(秒)
    materials_processed INTEGER DEFAULT 0,     -- 处理物料数
    exceptions_count INTEGER DEFAULT 0,        -- 异常数

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    approved_by     UUID,
    approved_at     TIMESTAMP,

    UNIQUE (tenant_id, plan_number)
);
```

### 3.2 供应计划明细 (scm_supply_plan_item)

```sql
CREATE TABLE scm_supply_plan_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 计划头
    plan_id         UUID NOT NULL REFERENCES scm_supply_plan(id) ON DELETE CASCADE,

    -- 物料维度
    product_id      UUID NOT NULL REFERENCES mm_material(id),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    -- 时间
    plan_date       DATE NOT NULL,

    -- 需求信息
    gross_requirement DECIMAL(13,3) DEFAULT 0, -- 毛需求
    scheduled_receipt DECIMAL(13,3) DEFAULT 0, -- 计划入库
    on_hand         DECIMAL(13,3) DEFAULT 0,   -- 现有库存
    safety_stock    DECIMAL(13,3) DEFAULT 0,   -- 安全库存
    net_requirement DECIMAL(13,3),             -- 净需求 (计算)

    -- 供应信息
    planned_order   DECIMAL(13,3) DEFAULT 0,   -- 计划订单
    planned_receipt_date DATE,                 -- 计划入库日期
    planned_release_date DATE,                 -- 计划下达日期

    -- 供应来源
    supply_source   VARCHAR(2),                -- 供应来源
    -- 01:自制 02:外购 03:调拨 04:寄售
    supplier_id     UUID REFERENCES bp_business_partner(id),

    -- 单位
    unit            VARCHAR(3),

    -- 状态
    item_status     VARCHAR(2) DEFAULT '01',   -- 明细状态
    -- 01:未处理 02:已转采购申请 03:已转生产订单 04:已取消

    -- 异常
    exception_type  VARCHAR(2),                -- 异常类型
    -- 01:逾期 02:短缺 03:产能不足 04:供应商不足
    exception_msg   TEXT,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_scm_supply_item_product ON scm_supply_plan_item (product_id);
CREATE INDEX idx_scm_supply_item_date ON scm_supply_plan_item (plan_date);
CREATE INDEX idx_scm_supply_item_status ON scm_supply_plan_item (item_status);
```

### 3.3 供应网络 (scm_supply_network)

```sql
CREATE TABLE scm_supply_network (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 网络节点
    node_code       VARCHAR(10) NOT NULL,      -- 节点编码
    node_name       VARCHAR(100) NOT NULL,     -- 节点名称
    node_type       VARCHAR(2),                -- 节点类型
    -- 01:工厂 02:仓库 03:DC配送中心 04:供应商 05:客户

    -- 关联
    plant_id        UUID REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),
    supplier_id     UUID REFERENCES bp_business_partner(id),

    -- 地址
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city            VARCHAR(40),
    address         VARCHAR(200),
    postal_code     VARCHAR(10),

    -- 能力
    capacity        DECIMAL(15,3),             -- 产能
    capacity_uom    VARCHAR(3),                -- 产能单位
    daily_throughput DECIMAL(13,3),            -- 日吞吐量

    -- 成本
    handling_cost   DECIMAL(15,2),             -- 操作成本
    storage_cost    DECIMAL(15,2),             -- 存储成本

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, node_code)
);
```

### 3.4 供应网络路径 (scm_network_path)

```sql
CREATE TABLE scm_network_path (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 路径
    path_code       VARCHAR(10) NOT NULL,
    path_name       VARCHAR(100),

    -- 起止节点
    source_node_id  UUID NOT NULL REFERENCES scm_supply_network(id),
    dest_node_id    UUID NOT NULL REFERENCES scm_supply_network(id),

    -- 运输
    transport_mode  VARCHAR(2),                -- 运输方式
    -- 01:公路 02:铁路 03:海运 04:空运 05:快递
    lead_time       INTEGER,                   -- 在途时间(天)
    lead_time_variability INTEGER,             -- 在途时间波动(天)
    distance_km     DECIMAL(10,2),             -- 距离(公里)

    -- 成本
    transport_cost  DECIMAL(15,2),             -- 运输成本
    cost_per_unit   DECIMAL(15,2),             -- 单位成本
    cost_per_kg     DECIMAL(15,2),             -- 每公斤成本

    -- 优先级
    priority        INTEGER DEFAULT 10,

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,
    is_primary      BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, path_code)
);
```

---

## 4. 库存优化

### 4.1 库存策略 (scm_inventory_policy)

```sql
CREATE TABLE scm_inventory_policy (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 策略标识
    policy_code     VARCHAR(10) NOT NULL,
    policy_name     VARCHAR(100) NOT NULL,

    -- 适用范围
    product_id      UUID REFERENCES mm_material(id),
    product_group   VARCHAR(9),
    plant_id        UUID REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),

    -- 策略类型
    policy_type     VARCHAR(2),                -- 策略类型
    -- 01:连续盘点 02:定期盘点 03:双箱法 04:ABC分类

    -- 补货参数
    reorder_point   DECIMAL(13,3),             -- 再订货点
    reorder_quantity DECIMAL(13,3),            -- 订货批量
    max_stock       DECIMAL(13,3),             -- 最大库存
    min_stock       DECIMAL(13,3),             -- 最小库存

    -- 服务水平
    service_level   DECIMAL(5,2),              -- 服务水平%
    -- 如 95% 表示95%的需求能满足

    -- 周期
    review_cycle    INTEGER,                   -- 盘点周期(天)

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,
    effective_date  DATE,                      -- 生效日期
    expiry_date     DATE,                      -- 失效日期

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, policy_code)
);
```

### 4.2 安全库存 (scm_safety_stock)

```sql
CREATE TABLE scm_safety_stock (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 物料维度
    product_id      UUID NOT NULL REFERENCES mm_material(id),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),

    -- 安全库存计算
    avg_demand      DECIMAL(13,3),             -- 平均需求
    demand_variability DECIMAL(13,3),          -- 需求波动(标准差)
    avg_lead_time   DECIMAL(10,2),             -- 平均提前期(天)
    lead_time_variability DECIMAL(10,2),       -- 提前期波动(天)

    -- 服务水平
    service_level   DECIMAL(5,2),              -- 服务水平%
    z_score         DECIMAL(6,4),              -- Z值

    -- 计算结果
    safety_stock_qty DECIMAL(13,3),            -- 安全库存数量
    unit            VARCHAR(3),

    -- 期间
    calculation_date DATE,                     -- 计算日期
    valid_from      DATE,                      -- 有效开始
    valid_to        DATE,                      -- 有效结束

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (product_id, plant_id, sloc_id, valid_from)
);
```

### 4.3 补货计划 (scm_replenishment)

```sql
CREATE TABLE scm_replenishment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 补货标识
    replen_number   VARCHAR(12) NOT NULL,
    replen_type     VARCHAR(2),                -- 补货类型
    -- 01:自动补货 02:手动补货 03:调拨补货

    -- 物料
    product_id      UUID NOT NULL REFERENCES mm_material(id),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),

    -- 数量
    current_stock   DECIMAL(13,3),             -- 当前库存
    safety_stock    DECIMAL(13,3),             -- 安全库存
    in_transit      DECIMAL(13,3),             -- 在途数量
    open_orders     DECIMAL(13,3),             -- 未结订单
    net_requirement DECIMAL(13,3),             -- 净需求

    -- 补货
    replen_qty      DECIMAL(13,3) NOT NULL,    -- 补货数量
    unit            VARCHAR(3),
    source_type     VARCHAR(2),                -- 来源类型
    -- 01:采购 02:生产 03:调拨
    source_plant_id UUID REFERENCES sys_plant(id),
    supplier_id     UUID REFERENCES bp_business_partner(id),

    -- 日期
    required_date   DATE NOT NULL,             -- 需求日期
    order_date      DATE,                      -- 下单日期
    expected_date   DATE,                      -- 预计到货日期

    -- 状态
    replen_status   VARCHAR(2) DEFAULT '01',   -- 补货状态
    -- 01:待审批 02:已审批 03:已下达 04:已完成 05:已取消

    -- 关联单据
    reference_type  VARCHAR(2),                -- 关联类型
    -- 01:采购申请 02:采购订单 03:调拨单 04:生产订单
    reference_id    UUID,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    approved_by     UUID,
    approved_at     TIMESTAMP,

    UNIQUE (tenant_id, replen_number)
);
```

---

## 5. 物流管理

### 5.1 发货单 (scm_shipment)

```sql
CREATE TABLE scm_shipment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 发货标识
    shipment_number VARCHAR(12) NOT NULL,      -- 发货单号
    shipment_type   VARCHAR(2),                -- 发货类型
    -- 01:销售发货 02:调拨发货 03:退货发货 04:寄售发货

    -- 关联单据
    reference_type  VARCHAR(2),                -- 关联类型
    -- 01:销售订单 02:交货单 03:调拨单
    reference_id    UUID,
    reference_number VARCHAR(12),

    -- 发货方
    ship_from_plant UUID REFERENCES sys_plant(id),
    ship_from_sloc  UUID REFERENCES sys_storage_location(id),
    ship_from_address VARCHAR(200),

    -- 收货方
    ship_to_party   UUID REFERENCES bp_business_partner(id),
    ship_to_name    VARCHAR(100),
    ship_to_address VARCHAR(200),
    ship_to_city    VARCHAR(40),
    ship_to_postal  VARCHAR(10),
    ship_to_country UUID REFERENCES core_country(id),

    -- 物流
    carrier_id      UUID REFERENCES bp_business_partner(id), -- 承运商
    carrier_name    VARCHAR(100),
    transport_mode  VARCHAR(2),                -- 运输方式
    -- 01:公路 02:铁路 03:海运 04:空运 05:快递
    vehicle_number  VARCHAR(20),               -- 车牌号
    driver_name     VARCHAR(40),
    driver_phone    VARCHAR(20),

    -- 日期
    planned_ship_date DATE,                    -- 计划发货日期
    actual_ship_date DATE,                     -- 实际发货日期
    planned_arrival_date DATE,                 -- 计划到达日期
    actual_arrival_date DATE,                  -- 实际到达日期

    -- 状态
    shipment_status VARCHAR(2) DEFAULT '01',   -- 发货状态
    -- 01:待发货 02:已发货 03:运输中 04:已到达 05:已签收 06:已取消

    -- 金额
    freight_cost    DECIMAL(15,2),             -- 运费
    insurance_cost  DECIMAL(15,2),             -- 保险费
    currency_id     UUID REFERENCES core_currency(id),

    -- 备注
    remarks         TEXT,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, shipment_number)
);
```

### 5.2 发货明细 (scm_shipment_item)

```sql
CREATE TABLE scm_shipment_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 发货头
    shipment_id     UUID NOT NULL REFERENCES scm_shipment(id) ON DELETE CASCADE,

    -- 行号
    line_number     INTEGER NOT NULL,

    -- 物料
    product_id      UUID REFERENCES mm_material(id),
    product_code    VARCHAR(18),
    product_name    VARCHAR(100),

    -- 数量
    ordered_qty     DECIMAL(13,3),             -- 订单数量
    shipped_qty     DECIMAL(13,3),             -- 发货数量
    received_qty    DECIMAL(13,3),             -- 收货数量
    unit            VARCHAR(3),

    -- 包装
    package_type    VARCHAR(10),               -- 包装类型
    package_qty     INTEGER,                   -- 包装件数
    gross_weight    DECIMAL(13,3),             -- 毛重
    net_weight      DECIMAL(13,3),             -- 净重
    volume          DECIMAL(13,3),             -- 体积

    -- 批次
    batch_number    VARCHAR(20),
    serial_numbers  TEXT,                      -- 序列号(JSON数组)

    -- 仓库
    pick_status     VARCHAR(2),                -- 拣货状态
    -- 01:未拣货 02:已拣货 03:已打包 04:已装车

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 5.3 物流跟踪 (scm_tracking)

```sql
CREATE TABLE scm_tracking (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 关联
    shipment_id     UUID NOT NULL REFERENCES scm_shipment(id),

    -- 跟踪信息
    tracking_number VARCHAR(50),               -- 物流单号
    tracking_time   TIMESTAMP NOT NULL,        -- 跟踪时间

    -- 位置
    location        VARCHAR(100),              -- 当前位置
    latitude        DECIMAL(10,7),             -- 纬度
    longitude       DECIMAL(10,7),             -- 经度

    -- 状态
    tracking_status VARCHAR(2),                -- 跟踪状态
    -- 01:已揽收 02:运输中 03:中转 04:派送中 05:已签收 06:异常
    status_desc     VARCHAR(200),              -- 状态描述

    -- 操作
    operator        VARCHAR(40),               -- 操作人
    contact_phone   VARCHAR(20),               -- 联系电话

    -- 备注
    remarks         TEXT,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_scm_tracking_shipment ON scm_tracking (shipment_id);
CREATE INDEX idx_scm_tracking_time ON scm_tracking (tracking_time);
```

---

## 6. 供应链协同

### 6.1 供应商协同 (scm_supplier_collab)

```sql
CREATE TABLE scm_supplier_collab (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 协同标识
    collab_number   VARCHAR(12) NOT NULL,
    collab_type     VARCHAR(2),                -- 协同类型
    -- 01:需求预测共享 02:库存信息共享 03:订单协同 04:产能协同

    -- 供应商
    supplier_id     UUID NOT NULL REFERENCES bp_business_partner(id),
    supplier_name   VARCHAR(100),

    -- 物料
    product_id      UUID REFERENCES mm_material(id),
    product_group   VARCHAR(9),

    -- 共享内容
    share_forecast  BOOLEAN DEFAULT FALSE,     -- 共享预测
    share_inventory BOOLEAN DEFAULT FALSE,     -- 共享库存
    share_order     BOOLEAN DEFAULT FALSE,     -- 共享订单
    share_capacity  BOOLEAN DEFAULT FALSE,     -- 共享产能

    -- 时间窗口
    horizon_days    INTEGER DEFAULT 90,        -- 展望期(天)
    refresh_cycle   INTEGER DEFAULT 7,         -- 刷新周期(天)

    -- 供应商反馈
    allow_commit    BOOLEAN DEFAULT FALSE,     -- 允许承诺
    commit_horizon  INTEGER DEFAULT 30,        -- 承诺期(天)

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,
    start_date      DATE,
    end_date        DATE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, collab_number)
);
```

### 6.2 供应商库存承诺 (scm_supplier_commit)

```sql
CREATE TABLE scm_supplier_commit (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 供应商
    supplier_id     UUID NOT NULL REFERENCES bp_business_partner(id),

    -- 物料
    product_id      UUID NOT NULL REFERENCES mm_material(id),
    plant_id        UUID REFERENCES sys_plant(id),

    -- 日期
    commit_date     DATE NOT NULL,             -- 承诺日期

    -- 承诺数量
    committed_qty   DECIMAL(13,3) NOT NULL,    -- 承诺数量
    unit            VARCHAR(3),

    -- 状态
    commit_status   VARCHAR(2) DEFAULT '01',   -- 承诺状态
    -- 01:已承诺 02:部分交付 03:完全交付 04:已取消

    -- 实际
    delivered_qty   DECIMAL(13,3) DEFAULT 0,   -- 已交付数量
    open_qty        DECIMAL(13,3) GENERATED ALWAYS AS (
        committed_qty - delivered_qty
    ) STORED,

    -- 备注
    remarks         TEXT,

    -- 来源
    collab_id       UUID REFERENCES scm_supplier_collab(id),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    committed_at    TIMESTAMP,
    committed_by    UUID
);
```

### 6.3 VMI库存 (scm_vmi_inventory)

```sql
CREATE TABLE scm_vmi_inventory (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 供应商
    supplier_id     UUID NOT NULL REFERENCES bp_business_partner(id),

    -- 物料
    product_id      UUID NOT NULL REFERENCES mm_material(id),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),

    -- 库存
    on_hand_qty     DECIMAL(13,3) DEFAULT 0,   -- 现有库存
    min_qty         DECIMAL(13,3),             -- 最小库存
    max_qty         DECIMAL(13,3),             -- 最大库存
    unit            VARCHAR(3),

    -- 消耗
    consumption_qty DECIMAL(13,3) DEFAULT 0,   -- 消耗数量
    last_consumption_date DATE,                -- 最后消耗日期

    -- 补货
    replenishment_qty DECIMAL(13,3),           -- 补货数量
    last_replenishment_date DATE,              -- 最后补货日期

    -- 结算
    settlement_type VARCHAR(2),                -- 结算方式
    -- 01:按消耗 02:按入库 03:定期结算
    last_settlement_date DATE,

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (supplier_id, product_id, plant_id, sloc_id)
);
```

---

## 7. 供应链分析

### 7.1 供应链KPI (scm_kpi)

```sql
CREATE TABLE scm_kpi (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- KPI标识
    kpi_code        VARCHAR(10) NOT NULL,
    kpi_name        VARCHAR(100) NOT NULL,
    kpi_category    VARCHAR(2),                -- KPI类别
    -- 01:预测准确率 02:库存周转 03:订单履行 04:物流效率 05:供应商绩效

    -- 维度
    plant_id        UUID REFERENCES sys_plant(id),
    product_group   VARCHAR(9),
    supplier_id     UUID REFERENCES bp_business_partner(id),

    -- 期间
    period_type     VARCHAR(2),                -- 期间类型
    -- 01:日 02:周 03:月 04:季 05:年
    period_year     INTEGER NOT NULL,
    period_month    INTEGER,
    period_week     INTEGER,

    -- KPI值
    target_value    DECIMAL(15,2),             -- 目标值
    actual_value    DECIMAL(15,2),             -- 实际值
    variance_pct    DECIMAL(5,2),              -- 偏差%

    -- 趋势
    trend           VARCHAR(2),                -- 趋势
    -- 01:上升 02:下降 03:持平
    previous_value  DECIMAL(15,2),             -- 上期值
    yoy_change      DECIMAL(5,2),              -- 同比变化%
    mom_change      DECIMAL(5,2),              -- 环比变化%

    -- 评级
    rating          VARCHAR(1),                -- 评级
    -- A:优秀 B:良好 C:一般 D:差

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, kpi_code, period_year, period_month, period_week)
);
```

### 7.2 供应链预警 (scm_alert)

```sql
CREATE TABLE scm_alert (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 预警标识
    alert_number    VARCHAR(12) NOT NULL,
    alert_type      VARCHAR(2),                -- 预警类型
    -- 01:库存预警 02:供应短缺 03:订单延期 04:质量问题 05:价格波动
    alert_level     VARCHAR(2),                -- 预警级别
    -- 01:紧急 02:重要 03:一般 04:提示

    -- 关联对象
    product_id      UUID REFERENCES mm_material(id),
    plant_id        UUID REFERENCES sys_plant(id),
    supplier_id     UUID REFERENCES bp_business_partner(id),
    order_id        UUID,

    -- 预警内容
    title           VARCHAR(100) NOT NULL,
    description     TEXT,

    -- 阈值
    threshold_type  VARCHAR(2),                -- 阈值类型
    -- 01:低于最小值 02:高于最大值 03:偏离目标
    threshold_value DECIMAL(15,2),
    actual_value    DECIMAL(15,2),

    -- 状态
    alert_status    VARCHAR(2) DEFAULT '01',   -- 预警状态
    -- 01:新建 02:已确认 03:处理中 04:已解决 05:已关闭

    -- 处理
    assigned_to     UUID REFERENCES hr_employee(id),
    resolved_at     TIMESTAMP,
    resolved_by     UUID,
    resolution      TEXT,

    -- 时间
    alert_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date        DATE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, alert_number)
);

-- 索引
CREATE INDEX idx_scm_alert_type ON scm_alert (alert_type);
CREATE INDEX idx_scm_alert_status ON scm_alert (alert_status);
CREATE INDEX idx_scm_alert_date ON scm_alert (alert_date);
```

---

## 8. 视图定义

### 8.1 需求计划汇总视图

```sql
CREATE VIEW v_scm_demand_summary AS
SELECT
    dpi.tenant_id,
    dpi.plan_id,
    dp.plan_number,
    dp.plan_name,
    dpi.product_id,
    m.material_code,
    m.description AS product_name,
    dpi.plant_id,
    p.plant_code,
    p.name AS plant_name,

    dpi.period_year,
    dpi.period_month,

    SUM(dpi.demand_qty) AS total_demand,
    SUM(dpi.confirmed_qty) AS total_confirmed,
    SUM(dpi.planned_qty) AS total_planned,
    SUM(dpi.open_qty) AS total_open,

    COUNT(*) AS item_count

FROM scm_demand_plan_item dpi
JOIN scm_demand_plan dp ON dp.id = dpi.plan_id
LEFT JOIN mm_material m ON m.id = dpi.product_id
LEFT JOIN sys_plant p ON p.id = dpi.plant_id
GROUP BY dpi.tenant_id, dpi.plan_id, dp.plan_number, dp.plan_name,
         dpi.product_id, m.material_code, m.description,
         dpi.plant_id, p.plant_code, p.name,
         dpi.period_year, dpi.period_month;
```

### 8.2 供应计划分析视图

```sql
CREATE VIEW v_scm_supply_analysis AS
SELECT
    spi.tenant_id,
    spi.plan_id,
    sp.plan_number,
    sp.plan_name,
    spi.product_id,
    m.material_code,
    m.description AS product_name,
    spi.plant_id,
    p.plant_code,

    spi.plan_date,
    spi.period_year,
    spi.period_month,

    spi.gross_requirement,
    spi.scheduled_receipt,
    spi.on_hand,
    spi.safety_stock,
    spi.net_requirement,
    spi.planned_order,

    spi.supply_source,
    spi.item_status,
    spi.exception_type

FROM scm_supply_plan_item spi
JOIN scm_supply_plan sp ON sp.id = spi.plan_id
LEFT JOIN mm_material m ON m.id = spi.product_id
LEFT JOIN sys_plant p ON p.id = spi.plant_id;
```

### 8.3 库存优化视图

```sql
CREATE VIEW v_scm_inventory_opt AS
SELECT
    ss.tenant_id,
    ss.product_id,
    m.material_code,
    m.description AS product_name,
    ss.plant_id,
    p.plant_code,
    p.name AS plant_name,

    ss.avg_demand,
    ss.demand_variability,
    ss.avg_lead_time,
    ss.lead_time_variability,
    ss.service_level,
    ss.safety_stock_qty,

    ip.reorder_point,
    ip.reorder_quantity,
    ip.max_stock,
    ip.min_stock,

    COALESCE(ms.unrestricted_qty, 0) AS current_stock,
    COALESCE(ms.unrestricted_qty, 0) - ss.safety_stock_qty AS available_stock

FROM scm_safety_stock ss
LEFT JOIN mm_material m ON m.id = ss.product_id
LEFT JOIN sys_plant p ON p.id = ss.plant_id
LEFT JOIN scm_inventory_policy ip ON ip.product_id = ss.product_id AND ip.plant_id = ss.plant_id
LEFT JOIN mm_material_sloc ms ON ms.material_id = ss.product_id AND ms.plant_id = ss.plant_id
WHERE ss.is_active = TRUE;
```

---

## 9. 存储过程

### 9.1 计算安全库存

```sql
CREATE OR REPLACE FUNCTION scm_calculate_safety_stock(
    p_product_id UUID,
    p_plant_id UUID,
    p_service_level DECIMAL(5,2) DEFAULT 95.00
) RETURNS DECIMAL(13,3) AS $$
DECLARE
    v_avg_demand DECIMAL(13,3);
    v_demand_std DECIMAL(13,3);
    v_avg_lt DECIMAL(10,2);
    v_lt_std DECIMAL(10,2);
    v_z_score DECIMAL(6,4);
    v_safety_stock DECIMAL(13,3);
BEGIN
    -- 获取平均需求和标准差(最近90天)
    SELECT
        AVG(demand_qty),
        STDDEV(demand_qty)
    INTO v_avg_demand, v_demand_std
    FROM scm_demand_plan_item
    WHERE product_id = p_product_id
      AND plant_id = p_plant_id
      AND plan_date >= CURRENT_DATE - INTERVAL '90 days';

    -- 获取平均提前期和标准差
    SELECT
        AVG(planned_delivery_time),
        STDDEV(planned_delivery_time)
    INTO v_avg_lt, v_lt_std
    FROM mm_material_plant
    WHERE material_id = p_product_id AND plant_id = p_plant_id;

    -- 计算Z值(简化版，实际应查表)
    v_z_score := CASE
        WHEN p_service_level >= 99 THEN 2.33
        WHEN p_service_level >= 98 THEN 2.05
        WHEN p_service_level >= 95 THEN 1.65
        WHEN p_service_level >= 90 THEN 1.28
        WHEN p_service_level >= 85 THEN 1.04
        ELSE 0.84
    END;

    -- 计算安全库存: Z * SQRT(LT * σd² + d² * σLT²)
    v_safety_stock := v_z_score * SQRT(
        v_avg_lt * POWER(COALESCE(v_demand_std, 0), 2) +
        POWER(v_avg_demand, 2) * POWER(COALESCE(v_lt_std, 0), 2)
    );

    RETURN ROUND(v_safety_stock, 3);
END;
$$ LANGUAGE plpgsql;
```

### 9.2 运行补货检查

```sql
CREATE OR REPLACE FUNCTION scm_run_replenishment_check(
    p_plant_id UUID,
    p_user_id UUID
) RETURNS INTEGER AS $$
DECLARE
    v_replen_count INTEGER := 0;
    v_replen_number VARCHAR(12);
    v_record RECORD;
BEGIN
    -- 查找需要补货的物料
    FOR v_record IN
        SELECT
            ms.material_id,
            ms.plant_id,
            ms.sloc_id,
            ms.unrestricted_qty,
            ss.safety_stock_qty,
            COALESCE(ss.safety_stock_qty, 0) - ms.unrestricted_qty AS net_req
        FROM mm_material_sloc ms
        LEFT JOIN scm_safety_stock ss ON ss.product_id = ms.material_id
            AND ss.plant_id = ms.plant_id AND ss.is_active = TRUE
        WHERE ms.plant_id = p_plant_id
          AND ms.unrestricted_qty < COALESCE(ss.safety_stock_qty, 0)
    LOOP
        -- 生成补货单号
        v_replen_number := generate_business_code(
            (SELECT tenant_id FROM sys_plant WHERE id = p_plant_id),
            'RP', NULL, NULL
        );

        -- 创建补货记录
        INSERT INTO scm_replenishment (
            tenant_id, replen_number, replen_type,
            product_id, plant_id, sloc_id,
            current_stock, safety_stock, net_requirement,
            replen_qty, required_date, replen_status,
            created_by
        ) VALUES (
            (SELECT tenant_id FROM sys_plant WHERE id = p_plant_id),
            v_replen_number, '01',
            v_record.material_id, v_record.plant_id, v_record.sloc_id,
            v_record.unrestricted_qty, v_record.safety_stock_qty, v_record.net_req,
            v_record.net_req, CURRENT_DATE + INTERVAL '7 days', '01',
            p_user_id
        );

        v_replen_count := v_replen_count + 1;
    END LOOP;

    RETURN v_replen_count;
END;
$$ LANGUAGE plpgsql;
```

---

## 10. 索引策略

```sql
-- 预测
CREATE INDEX idx_scm_forecast_type ON scm_demand_forecast (forecast_type);
CREATE INDEX idx_scm_forecast_status ON scm_demand_forecast (forecast_status);
CREATE INDEX idx_scm_forecast_period ON scm_demand_forecast (start_date, end_date);

-- 预测明细
CREATE INDEX idx_scm_forecast_item_product ON scm_forecast_item (product_id);
CREATE INDEX idx_scm_forecast_item_date ON scm_forecast_item (forecast_date);
CREATE INDEX idx_scm_forecast_item_plant ON scm_forecast_item (plant_id);

-- 供应计划
CREATE INDEX idx_scm_supply_plan_status ON scm_supply_plan (plan_status);
CREATE INDEX idx_scm_supply_plan_type ON scm_supply_plan (plan_type);

-- 补货
CREATE INDEX idx_scm_replen_status ON scm_replenishment (replen_status);
CREATE INDEX idx_scm_replen_product ON scm_replenishment (product_id);
CREATE INDEX idx_scm_replen_required ON scm_replenishment (required_date);

-- 发货
CREATE INDEX idx_scm_shipment_status ON scm_shipment (shipment_status);
CREATE INDEX idx_scm_shipment_carrier ON scm_shipment (carrier_id);
CREATE INDEX idx_scm_shipment_dates ON scm_shipment (planned_ship_date, planned_arrival_date);

-- VMI
CREATE INDEX idx_scm_vmi_supplier ON scm_vmi_inventory (supplier_id);
CREATE INDEX idx_scm_vmi_product ON scm_vmi_inventory (product_id);

-- KPI
CREATE INDEX idx_scm_kpi_category ON scm_kpi (kpi_category);
CREATE INDEX idx_scm_kpi_period ON scm_kpi (period_year, period_month);
```

---

## 11. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-16 | 初始版本 |
