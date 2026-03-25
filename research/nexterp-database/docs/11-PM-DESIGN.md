# PM 模块数据库设计

**模块**: Plant Maintenance (工厂维护/设备管理)
**对标**: SAP ECC PM (IW31/IW32/IW33)
**版本**: 1.0

---

## 1. 模块概述

### 1.1 业务范围

| 子模块 | 说明 | SAP 对标 |
|--------|------|----------|
| 设备主数据 | 设备信息管理 | IE01/IE02/IE03 |
| 功能位置 | 设备安装位置 | IL01/IL02 |
| 维护订单 | 维修工单管理 | IW31/IW32 |
| 预防性维护 | 定期保养计划 | IP01/IP02 |
| 维护通知 | 故障报告 | IW21/IW22 |

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                     PM Module Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    设备管理                              │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │    │
│  │  │  功能位置   │◄─┤   设备      │◄─┤ 设备分类   │      │    │
│  │  │pm_functional│  │ pm_equipment│  │ pm_equip_   │      │    │
│  │  │  _location  │  │             │  │   class     │      │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│        ┌─────────────────────┼─────────────────────┐            │
│        │                     │                     │            │
│        ▼                     ▼                     ▼            │
│  ┌──────────┐          ┌──────────┐          ┌──────────┐      │
│  │ 维护通知 │          │ 维护订单 │          │预防性维护│      │
│  │ pm_maint │          │ pm_maint │          │ pm_prev_ │      │
│  │ _notif   │          │  _order  │          │ maintenance│    │
│  └──────────┘          └──────────┘          └──────────┘      │
│        │                     │                     │            │
│        └─────────────────────┴─────────────────────┘            │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    维护历史                              │    │
│  │                  pm_maintenance_history                   │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 功能位置

### 2.1 功能位置 (pm_functional_location)

对标 SAP IFLOT

```sql
CREATE TABLE pm_functional_location (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 位置信息
    location_code   VARCHAR(12) NOT NULL,      -- 功能位置代码
    description     VARCHAR(100) NOT NULL,     -- 描述

    -- 层级
    parent_id       UUID REFERENCES pm_functional_location(id),
    level           INTEGER DEFAULT 1,
    path            VARCHAR(200),              -- 层级路径

    -- 组织
    plant_id        UUID REFERENCES sys_plant(id),
    cost_center_id  UUID REFERENCES sys_cost_center(id),

    -- 类别
    category        VARCHAR(2),                -- 功能位置类别
    -- 01:建筑物 02:系统 03:设备组 04:具体设备

    -- 状态
    location_status VARCHAR(2) DEFAULT '01',   -- 01:安装 02:拆除 03:停用
    is_active       BOOLEAN DEFAULT TRUE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, location_code)
);
```

---

## 3. 设备主数据

### 3.1 设备主表 (pm_equipment)

对标 SAP EQUI

```sql
CREATE TABLE pm_equipment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 设备标识
    equipment_number VARCHAR(18) NOT NULL,     -- 设备编号
    description     VARCHAR(100) NOT NULL,     -- 设备描述
    description_2   VARCHAR(100),              -- 描述2

    -- 分类
    equipment_type  VARCHAR(10),               -- 设备类型
    object_type     VARCHAR(10),               -- 对象类型
    -- EQ:设备 FL:功能位置

    -- 功能位置
    functional_loc_id UUID REFERENCES pm_functional_location(id),
    functional_loc_code VARCHAR(12),
    installation_date DATE,                    -- 安装日期
    dismantling_date DATE,                     -- 拆除日期

    -- 组织
    plant_id        UUID REFERENCES sys_plant(id),
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    work_center_id  UUID REFERENCES pp_work_center(id),

    -- 制造商
    manufacturer    VARCHAR(100),              -- 制造商
    model_number    VARCHAR(50),               -- 型号
    serial_number   VARCHAR(50),               -- 序列号
    manufacture_date DATE,                     -- 制造日期
    warranty_expire_date DATE,                 -- 保修到期日

    -- 技术参数
    technical_data  JSONB,                     -- 技术数据 (JSON)
    weight          DECIMAL(10,3),             -- 重量
    weight_unit     VARCHAR(3),                -- 重量单位
    dimension       VARCHAR(50),               -- 尺寸 (LxWxH)
    power_rating    VARCHAR(20),               -- 额定功率

    -- 位置
    location        VARCHAR(30),               -- 物理位置
    room            VARCHAR(10),               -- 房间

    -- 责任人
    responsible_id  UUID REFERENCES hr_employee(id),
    department      VARCHAR(50),

    -- 资产关联
    asset_id        UUID REFERENCES am_asset(id), -- 关联固定资产

    -- 维护信息
    last_maintenance_date DATE,                -- 上次维护日期
    next_maintenance_date DATE,                -- 下次维护日期
    maintenance_plan_id UUID,                  -- 维护计划
    maintenance_strategy VARCHAR(2),           -- 维护策略
    -- 01:故障维修 02:预防性维护 03:预测性维护

    -- 运行状态
    equipment_status VARCHAR(2) DEFAULT '01',   -- 01:运行 02:停机 03:维修 04:报废
    is_critical      BOOLEAN DEFAULT FALSE,    -- 关键设备
    is_running       BOOLEAN DEFAULT TRUE,     -- 运行中

    -- 运行统计
    total_run_hours DECIMAL(10,2) DEFAULT 0,   -- 累计运行小时
    total_breakdown_count INTEGER DEFAULT 0,   -- 累计故障次数
    total_maintenance_count INTEGER DEFAULT 0, -- 累计维护次数

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, equipment_number)
);
```

### 3.2 设备分类 (pm_equipment_class)

```sql
CREATE TABLE pm_equipment_class (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 分类信息
    class_code      VARCHAR(8) NOT NULL,       -- 分类代码
    class_name      VARCHAR(100) NOT NULL,     -- 分类名称
    description     TEXT,

    -- 层级
    parent_id       UUID REFERENCES pm_equipment_class(id),
    level           INTEGER DEFAULT 1,

    -- 维护策略
    default_strategy VARCHAR(2),               -- 默认维护策略
    default_interval INTEGER,                  -- 默认维护间隔 (天)

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, class_code)
);
```

---

## 4. 维护通知

### 4.1 维护通知 (pm_maintenance_notification)

对标 SAP QMEL (PM类型)

```sql
CREATE TABLE pm_maintenance_notification (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 通知信息
    notification_number VARCHAR(12) NOT NULL,  -- 通知单号
    notification_type VARCHAR(3) NOT NULL,     -- 通知类型
    -- M1:故障报告 M2:活动报告 M3:维护申请 M4:停机报告

    -- 描述
    short_text      VARCHAR(100) NOT NULL,     -- 简短描述
    long_text       TEXT,                      -- 详细描述

    -- 优先级
    priority        VARCHAR(2),                -- 优先级
    -- 1:紧急 2:高 3:中 4:低

    -- 设备/位置
    equipment_id    UUID REFERENCES pm_equipment(id),
    equipment_number VARCHAR(18),
    functional_loc_id UUID REFERENCES pm_functional_location(id),
    functional_loc_code VARCHAR(12),

    -- 故障信息
    breakdown_date  TIMESTAMP,                 -- 故障时间
    breakdown_duration DECIMAL(8,2),           -- 停机时长 (小时)
    failure_cause   VARCHAR(4),                -- 故障原因代码
    failure_cause_text VARCHAR(100),           -- 故障原因描述
    failure_effect  VARCHAR(4),                -- 故障影响代码

    -- 组织
    plant_id        UUID REFERENCES sys_plant(id),
    work_center_id  UUID REFERENCES pp_work_center(id),

    -- 关联订单
    order_id        UUID,                      -- 关联维护订单

    -- 日期
    reported_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date        DATE,                      -- 截止日期
    completion_date DATE,                      -- 完成日期

    -- 状态
    notification_status VARCHAR(2) DEFAULT '01', -- 01:创建 02:处理中 03:完成 04:关闭
    system_status   VARCHAR(10),               -- 系统状态 (OSNO/NOPC/QUTX)

    -- 报告人
    reported_by     UUID REFERENCES hr_employee(id),
    responsible_id  UUID REFERENCES hr_employee(id),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, notification_number)
);
```

---

## 5. 维护订单

### 5.1 维护订单头 (pm_maintenance_order_hdr)

对标 SAP AUFK (PM订单)

```sql
CREATE TABLE pm_maintenance_order_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 订单信息
    order_number    VARCHAR(12) NOT NULL,      -- 订单号
    order_type      VARCHAR(4) NOT NULL,       -- 订单类型
    -- PM01:纠正性维护 PM02:预防性维护 PM03:改造 PM04:紧急维修

    -- 描述
    description     VARCHAR(100) NOT NULL,     -- 描述

    -- 设备/位置
    equipment_id    UUID REFERENCES pm_equipment(id),
    equipment_number VARCHAR(18),
    functional_loc_id UUID REFERENCES pm_functional_location(id),
    functional_loc_code VARCHAR(12),

    -- 组织
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    work_center_id  UUID REFERENCES pp_work_center(id),
    cost_center_id  UUID REFERENCES sys_cost_center(id),

    -- 关联通知
    notification_id UUID REFERENCES pm_maintenance_notification(id),
    notification_number VARCHAR(12),

    -- 日期
    order_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    planned_start_date DATE,
    planned_finish_date DATE,
    actual_start_date DATE,
    actual_finish_date DATE,

    -- 优先级
    priority        VARCHAR(2),

    -- 成本
    planned_cost    DECIMAL(15,2) DEFAULT 0,   -- 计划成本
    actual_cost     DECIMAL(15,2) DEFAULT 0,   -- 实际成本
    currency_id     UUID REFERENCES core_currency(id),

    -- 状态
    order_status    VARCHAR(2) DEFAULT '01',   -- 01:创建 02:审批 03:下达 04:执行 05:完成 06:关闭
    system_status   VARCHAR(10),               -- CRTD/REL/TECO/CLO

    -- 审批
    approval_status approval_status DEFAULT 'DRAFT',
    approved_by     UUID,
    approved_at     TIMESTAMP,

    -- 完成确认
    is_completed    BOOLEAN DEFAULT FALSE,
    completed_by    UUID,
    completed_at    TIMESTAMP,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, order_number)
);
```

### 5.2 维护订单操作 (pm_maintenance_order_op)

```sql
CREATE TABLE pm_maintenance_order_op (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 订单
    order_id        UUID NOT NULL REFERENCES pm_maintenance_order_hdr(id) ON DELETE CASCADE,
    order_number    VARCHAR(12),

    -- 操作
    operation_number VARCHAR(4) NOT NULL,      -- 操作号
    operation_text  VARCHAR(100),              -- 操作描述

    -- 工作中心
    work_center_id  UUID REFERENCES pp_work_center(id),
    work_center_code VARCHAR(8),

    -- 工时
    planned_work    DECIMAL(8,2),              -- 计划工时
    actual_work     DECIMAL(8,2) DEFAULT 0,    -- 实际工时
    work_unit       VARCHAR(3) DEFAULT 'H',    -- 工时单位

    -- 日期
    planned_start_date DATE,
    planned_finish_date DATE,
    actual_start_date DATE,
    actual_finish_date DATE,

    -- 状态
    operation_status VARCHAR(2) DEFAULT '01',  -- 01:未开始 02:执行中 03:完成

    -- 控制码
    control_key     VARCHAR(4),                -- 控制码
    -- PM01:内部处理 PM02:外部处理 PM03:服务合同

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (order_id, operation_number)
);
```

### 5.3 维护订单物料 (pm_maintenance_order_mat)

```sql
CREATE TABLE pm_maintenance_order_mat (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 订单
    order_id        UUID NOT NULL REFERENCES pm_maintenance_order_hdr(id) ON DELETE CASCADE,

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    description     VARCHAR(100),

    -- 数量
    required_qty    DECIMAL(13,3) NOT NULL,    -- 需求数量
    withdrawn_qty   DECIMAL(13,3) DEFAULT 0,   -- 已领数量
    unit            VARCHAR(3) NOT NULL,

    -- 仓库
    plant_id        UUID REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),

    -- 成本
    unit_price      DECIMAL(15,2),
    total_cost      DECIMAL(15,2),

    -- 状态
    item_status     VARCHAR(2) DEFAULT '01',   -- 01:需求 02:已领 03:已用

    -- 关联操作
    operation_number VARCHAR(4),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 6. 预防性维护

### 6.1 维护计划 (pm_maintenance_plan)

对标 SAP MPLA

```sql
CREATE TABLE pm_maintenance_plan (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 计划信息
    plan_number     VARCHAR(10) NOT NULL,      -- 计划号
    plan_name       VARCHAR(100) NOT NULL,     -- 计划名称
    description     TEXT,

    -- 类型
    plan_type       VARCHAR(2) NOT NULL,       -- 计划类型
    -- 01:时间基础 02:性能基础 03:统计基础

    -- 周期
    cycle_length    INTEGER NOT NULL,          -- 周期长度
    cycle_unit      VARCHAR(1) NOT NULL,       -- 周期单位
    -- D:天 W:周 M:月 Y:年

    -- 起始
    start_date      DATE NOT NULL,
    end_date        DATE DEFAULT '9999-12-31',

    -- 提前
    lead_time       INTEGER DEFAULT 0,         -- 提前天数
    tolerance       INTEGER DEFAULT 0,         -- 容差天数

    -- 订单类型
    order_type      VARCHAR(4) NOT NULL,       -- 默认订单类型

    -- 状态
    plan_status     VARCHAR(2) DEFAULT '01',   -- 01:创建 02:激活 03:停用
    is_active       BOOLEAN DEFAULT TRUE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, plan_number)
);
```

### 6.2 维护项目 (pm_maintenance_item)

```sql
CREATE TABLE pm_maintenance_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 计划
    plan_id         UUID NOT NULL REFERENCES pm_maintenance_plan(id) ON DELETE CASCADE,
    plan_number     VARCHAR(10),

    -- 设备/位置
    equipment_id    UUID REFERENCES pm_equipment(id),
    equipment_number VARCHAR(18),
    functional_loc_id UUID REFERENCES pm_functional_location(id),
    functional_loc_code VARCHAR(12),

    -- 维护任务清单
    task_list_id    UUID,

    -- 上次执行
    last_completion_date DATE,                 -- 上次完成日期
    last_counter_value DECIMAL(15,2),          -- 上次计数器值

    -- 下次计划
    next_due_date   DATE,                      -- 下次到期日
    next_call_date  DATE,                      -- 下次调用日 (减去提前期)

    -- 计数器 (性能基础)
    counter_type    VARCHAR(2),                -- 计数器类型
    -- 01:运行小时 02:产量 03:里程
    cycle_counter   DECIMAL(15,2),             -- 周期计数

    -- 状态
    item_status     VARCHAR(2) DEFAULT '01',   -- 01:激活 02:停用

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 7. 维护历史

### 7.1 维护历史 (pm_maintenance_history)

```sql
CREATE TABLE pm_maintenance_history (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 设备
    equipment_id    UUID NOT NULL REFERENCES pm_equipment(id),
    equipment_number VARCHAR(18),

    -- 维护类型
    maintenance_type VARCHAR(2),               -- 维护类型
    -- 01:预防性 02:纠正性 03:改造 04:紧急

    -- 订单
    order_id        UUID REFERENCES pm_maintenance_order_hdr(id),
    order_number    VARCHAR(12),

    -- 日期
    maintenance_date DATE NOT NULL,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    duration_hours  DECIMAL(8,2),

    -- 描述
    description     TEXT,

    -- 故障
    failure_cause   VARCHAR(4),
    failure_cause_text VARCHAR(100),

    -- 工作
    work_performed  TEXT,

    -- 成本
    total_cost      DECIMAL(15,2),
    currency_id     UUID REFERENCES core_currency(id),

    -- 人员
    performed_by    UUID REFERENCES hr_employee(id),

    -- 状态
    is_warranty     BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 8. 视图定义

### 8.1 设备维护状态视图

```sql
CREATE VIEW v_pm_equipment_status AS
SELECT
    e.equipment_number,
    e.description,
    e.equipment_status,
    e.is_critical,
    e.last_maintenance_date,
    e.next_maintenance_date,
    e.total_run_hours,
    e.total_breakdown_count,
    e.total_maintenance_count,
    p.plant_code,
    p.plant_name,
    fl.location_code AS functional_location,
    c.cost_center_code,
    emp.full_name AS responsible_person,
    CASE
        WHEN e.next_maintenance_date < CURRENT_DATE THEN '逾期'
        WHEN e.next_maintenance_date <= CURRENT_DATE + INTERVAL '7 days' THEN '即将到期'
        ELSE '正常'
    END AS maintenance_status

FROM pm_equipment e
LEFT JOIN sys_plant p ON p.id = e.plant_id
LEFT JOIN pm_functional_location fl ON fl.id = e.functional_loc_id
LEFT JOIN sys_cost_center c ON c.id = e.cost_center_id
LEFT JOIN hr_employee emp ON emp.id = e.responsible_id
WHERE e.is_active = TRUE;
```

### 8.2 维护订单跟踪视图

```sql
CREATE VIEW v_pm_order_tracking AS
SELECT
    o.order_number,
    o.order_type,
    o.description,
    o.priority,
    o.equipment_number,
    o.order_date,
    o.planned_start_date,
    o.planned_finish_date,
    o.actual_start_date,
    o.actual_finish_date,
    o.order_status,
    o.planned_cost,
    o.actual_cost,
    n.notification_number,
    n.notification_type,
    p.plant_code,
    p.plant_name,
    wc.work_center_code,
    wc.work_center_name

FROM pm_maintenance_order_hdr o
LEFT JOIN pm_maintenance_notification n ON n.id = o.notification_id
LEFT JOIN sys_plant p ON p.id = o.plant_id
LEFT JOIN pp_work_center wc ON wc.id = o.work_center_id
ORDER BY o.order_date DESC;
```

---

## 9. 索引策略

```sql
-- 设备
CREATE INDEX idx_pm_equipment_number ON pm_equipment (tenant_id, equipment_number);
CREATE INDEX idx_pm_equipment_func_loc ON pm_equipment (functional_loc_id);
CREATE INDEX idx_pm_equipment_plant ON pm_equipment (plant_id);
CREATE INDEX idx_pm_equipment_status ON pm_equipment (equipment_status);
CREATE INDEX idx_pm_equipment_next_maint ON pm_equipment (next_maintenance_date);

-- 功能位置
CREATE INDEX idx_pm_fl_parent ON pm_functional_location (parent_id);
CREATE INDEX idx_pm_fl_plant ON pm_functional_location (plant_id);

-- 维护通知
CREATE INDEX idx_pm_notif_number ON pm_maintenance_notification (tenant_id, notification_number);
CREATE INDEX idx_pm_notif_equipment ON pm_maintenance_notification (equipment_id);
CREATE INDEX idx_pm_notif_status ON pm_maintenance_notification (notification_status);

-- 维护订单
CREATE INDEX idx_pm_order_number ON pm_maintenance_order_hdr (tenant_id, order_number);
CREATE INDEX idx_pm_order_equipment ON pm_maintenance_order_hdr (equipment_id);
CREATE INDEX idx_pm_order_status ON pm_maintenance_order_hdr (order_status);
CREATE INDEX idx_pm_order_date ON pm_maintenance_order_hdr (order_date);

-- 预防性维护
CREATE INDEX idx_pm_plan_next_due ON pm_maintenance_item (next_due_date);
```

---

## 10. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
