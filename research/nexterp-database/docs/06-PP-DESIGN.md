# PP 模块数据库设计

**模块**: Production Planning (生产计划)
**对标**: SAP ECC PP (BOM/ROUT/PROD/PPC)
**版本**: 1.0

---

## 1. 模块概述

### 1.1 业务范围

| 子模块 | 说明 | SAP 对标 |
|--------|------|----------|
| 物料清单 | BOM 管理 | CS01/CS02/CS03 |
| 工艺路线 | 生产工序 | CA01/CA02/CA03 |
| 工作中心 | 产能管理 | CR01/CR02 |
| 生产订单 | 订单管理 | CO01/CO02/CO03 |
| MRP | 物料需求计划 | MD01/MD02 |

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                     PP Module Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    基础数据                              │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │    │
│  │  │  物料清单   │  │  工艺路线   │  │  工作中心   │      │    │
│  │  │  pp_bom     │  │  pp_routing │  │ pp_work_ctr │      │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    生产执行                              │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │    │
│  │  │ 生产订单    │  │ 生产确认    │  │ 产能计划    │      │    │
│  │  │ pp_prod_ord │  │ pp_confirm  │  │ pp_capacity │      │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    物料需求计划                          │    │
│  │                     pp_mrp                               │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 物料清单 (BOM)

### 2.1 BOM 头 (pp_bom_hdr)

对标 SAP MAST/STKO

```sql
CREATE TABLE pp_bom_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- BOM 信息
    bom_number      VARCHAR(10) NOT NULL,      -- BOM 编号
    bom_type        VARCHAR(1) NOT NULL,       -- BOM 类型
    -- M:生产 E:工程 P:生产(工厂) W:维修

    -- 物料
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    -- 用途
    bom_usage       VARCHAR(1) DEFAULT '1',    -- BOM 用途
    -- 1:生产 2:工程设计 3:通用

    -- 版本
    bom_status      VARCHAR(2) DEFAULT '01',   -- BOM 状态
    -- 01:激活 02:冻结 03:删除
    version         VARCHAR(4),                -- 版本号
    alternative_bom VARCHAR(2),                -- 替代 BOM

    -- 数量
    base_qty        DECIMAL(13,3) DEFAULT 1,   -- 基本数量
    base_unit       VARCHAR(3),                -- 基本单位

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 文本
    description     VARCHAR(100),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,

    UNIQUE (tenant_id, bom_number)
);
```

### 2.2 BOM 项 (pp_bom_itm)

对标 SAP STPO

```sql
CREATE TABLE pp_bom_itm (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 关联
    bom_id          UUID NOT NULL REFERENCES pp_bom_hdr(id) ON DELETE CASCADE,

    -- 项目
    item_number     VARCHAR(4) NOT NULL,       -- 项目编号
    item_category   VARCHAR(1) NOT NULL,       -- 项目类别
    -- L:库存项目 N:非库存项目 R:可变项目 T:文本项目 D:文档项目

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    description     VARCHAR(100),              -- 组件描述

    -- 数量
    component_qty   DECIMAL(13,3) NOT NULL,    -- 组件数量
    unit            VARCHAR(3) NOT NULL,       -- 单位

    -- 固定/变动
    is_fixed_qty    BOOLEAN DEFAULT FALSE,     -- 固定数量
    fixed_qty       DECIMAL(13,3),             -- 固定数量值

    -- 废品
    scrap_factor    DECIMAL(5,2) DEFAULT 0,    -- 废品率%

    -- 有效期
    valid_from      DATE DEFAULT CURRENT_DATE,
    valid_to        DATE DEFAULT '9999-12-31',

    -- 生产/采购
    is_phantom      BOOLEAN DEFAULT FALSE,     -- 虚拟件
    is_bulk         BOOLEAN DEFAULT FALSE,     -- 散装物料
    is_co_product   BOOLEAN DEFAULT FALSE,     -- 联产品
    production_plant UUID REFERENCES sys_plant(id), -- 生产工厂
    special_procurement VARCHAR(2),            -- 特殊采购

    -- 分配
    sort_string     VARCHAR(10),               -- 排序字符串

    -- 成本
    is_cost_relevant BOOLEAN DEFAULT TRUE,     -- 与成本相关

    -- 文本
    item_text       VARCHAR(100),

    -- 状态
    is_deleted      BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (bom_id, item_number)
);
```

---

## 3. 工作中心

### 3.1 工作中心 (pp_work_center)

对标 SAP CRHD

```sql
CREATE TABLE pp_work_center (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 工作中心信息
    work_center_code VARCHAR(8) NOT NULL,      -- 工作中心代码
    work_center_name VARCHAR(100) NOT NULL,    -- 工作中心名称

    -- 类型
    work_center_type VARCHAR(2) NOT NULL,      -- 工作中心类型
    -- 01:工作中心 02:生产线 03:人员 04:设备

    -- 组织
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    cost_center_id  UUID REFERENCES sys_cost_center(id),

    -- 产能
    capacity_category VARCHAR(2),              -- 产能类别
    -- 01:机器 02:人员 03:生产线
    capacity        DECIMAL(13,3) DEFAULT 1,   -- 产能
    capacity_unit   VARCHAR(3),                -- 产能单位
    efficiency_factor DECIMAL(5,2) DEFAULT 100, -- 效率因子%

    -- 时间
    operating_time  DECIMAL(8,2),              -- 运行时间 (小时/天)
    break_time      DECIMAL(5,2),              -- 休息时间 (小时)

    -- 标准值
    standard_value_unit VARCHAR(3),            -- 标准值单位

    -- 状态
    wc_status       VARCHAR(2) DEFAULT '01',   -- 01:可用 02:维护 03:停用
    status          general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (plant_id, work_center_code)
);
```

### 3.2 工作中心产能 (pp_work_center_capacity)

对标 SAP KAKO

```sql
CREATE TABLE pp_work_center_capacity (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    work_center_id  UUID NOT NULL REFERENCES pp_work_center(id),

    -- 产能信息
    capacity_code   VARCHAR(8),                -- 产能代码
    capacity_name   VARCHAR(100),              -- 产能名称
    capacity_category VARCHAR(2),              -- 产能类别

    -- 产能
    available_capacity DECIMAL(13,3),          -- 可用产能
    capacity_unit   VARCHAR(3),                -- 产能单位

    -- 工作时间
    start_time      TIME DEFAULT '08:00:00',
    end_time        TIME DEFAULT '17:00:00',
    operating_hours DECIMAL(5,2),              -- 运行小时数

    -- 效率
    efficiency_rate DECIMAL(5,2) DEFAULT 100,  -- 效率%
    utilization_rate DECIMAL(5,2) DEFAULT 100, -- 利用率%

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (work_center_id, capacity_category, valid_from)
);
```

---

## 4. 工艺路线

### 4.1 工艺路线头 (pp_routing_hdr)

对标 SAP MAPL/PLKO

```sql
CREATE TABLE pp_routing_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 工艺路线信息
    routing_number  VARCHAR(10) NOT NULL,      -- 工艺路线号
    routing_type    VARCHAR(1) NOT NULL,       -- 工艺路线类型
    -- N:标准工艺路线 R:维修工艺路线 Q:检验工艺路线

    -- 描述
    description     VARCHAR(100),              -- 工艺路线描述

    -- 用途
    task_list_usage VARCHAR(1) DEFAULT '1',    -- 任务清单用途
    -- 1:生产 2:维修 3:检验

    -- 物料关联
    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    plant_id        UUID REFERENCES sys_plant(id),

    -- 状态
    routing_status  VARCHAR(2) DEFAULT '01',   -- 01:创建 02:激活 03:冻结

    -- 有效期
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 标准值
    total_setup_time DECIMAL(10,2),            -- 总准备时间
    total_machine_time DECIMAL(10,2),          -- 总机器时间
    total_labor_time DECIMAL(10,2),            -- 总人工时间

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, routing_number)
);
```

### 4.2 工艺路线工序 (pp_routing_operation)

对标 SAP PLPO

```sql
CREATE TABLE pp_routing_operation (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 关联
    routing_id      UUID NOT NULL REFERENCES pp_routing_hdr(id) ON DELETE CASCADE,

    -- 工序信息
    operation_number VARCHAR(4) NOT NULL,      -- 工序号 (如 0010, 0020)
    operation_text  VARCHAR(100),              -- 工序描述

    -- 工作中心
    work_center_id  UUID REFERENCES pp_work_center(id),
    work_center_code VARCHAR(8),

    -- 标准值
    setup_time      DECIMAL(10,2) DEFAULT 0,   -- 准备时间
    machine_time    DECIMAL(10,2) DEFAULT 0,   -- 机器时间
    labor_time      DECIMAL(10,2) DEFAULT 0,   -- 人工时间
    time_unit       VARCHAR(3) DEFAULT 'MIN',  -- 时间单位 (MIN/HR)

    -- 数量
    base_qty        DECIMAL(13,3) DEFAULT 1,   -- 基本数量

    -- 控制码
    control_key     VARCHAR(4),                -- 控制码
    -- PP01:工序计划 PP02:外部处理 PP03:时间票据

    -- 成本
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    activity_type   VARCHAR(6),                -- 作业类型

    -- 排序
    sort_order      INTEGER,

    -- 有效期
    valid_from      DATE DEFAULT CURRENT_DATE,
    valid_to        DATE DEFAULT '9999-12-31',

    -- 状态
    is_deleted      BOOLEAN DEFAULT FALSE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (routing_id, operation_number)
);
```

---

## 5. 生产订单

### 5.1 生产订单头 (pp_production_order_hdr)

对标 SAP AUFK/AFKO

```sql
CREATE TABLE pp_production_order_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 订单信息
    order_number    VARCHAR(12) NOT NULL,      -- 订单号
    order_type      VARCHAR(4) NOT NULL,       -- 订单类型
    -- PP01:标准生产订单 PP02:重处理订单 PP03: refurbishment

    -- 物料
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    description     VARCHAR(100),

    -- 数量
    order_qty       DECIMAL(13,3) NOT NULL,    -- 订单数量
    confirmed_qty   DECIMAL(13,3) DEFAULT 0,   -- 已确认数量
    delivered_qty   DECIMAL(13,3) DEFAULT 0,   -- 已交货数量
    scrapped_qty    DECIMAL(13,3) DEFAULT 0,   -- 报废数量
    unit            VARCHAR(3) NOT NULL,

    -- 日期
    order_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    planned_start_date DATE,                   -- 计划开始日期
    planned_finish_date DATE,                  -- 计划完成日期
    actual_start_date DATE,                    -- 实际开始日期
    actual_finish_date DATE,                   -- 实际完成日期

    -- 组织
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    work_center_id  UUID REFERENCES pp_work_center(id),

    -- BOM/工艺路线
    bom_id          UUID REFERENCES pp_bom_hdr(id),
    routing_id      UUID REFERENCES pp_routing_hdr(id),

    -- 成本
    planned_cost    DECIMAL(15,2),             -- 计划成本
    actual_cost     DECIMAL(15,2) DEFAULT 0,   -- 实际成本
    variance_cost   DECIMAL(15,2) DEFAULT 0,   -- 差异成本
    currency_id     UUID REFERENCES core_currency(id),

    -- 状态
    order_status    VARCHAR(2) DEFAULT '01',   -- 01:创建 02:已审批 03:已下达 04:在产 05:完成 06:TECO
    system_status   VARCHAR(10),               -- 系统状态 (CRTD/REL/CNF/DLV/TECO)

    -- 来源
    source_type     VARCHAR(2),                -- 来源类型
    -- 01:手工 02:MRP 03:销售订单
    source_document VARCHAR(20),               -- 来源单据

    -- 审批
    approval_status approval_status DEFAULT 'DRAFT',
    approved_by     UUID,
    approved_at     TIMESTAMP,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, order_number)
);
```

### 5.2 生产订单工序 (pp_production_order_op)

```sql
CREATE TABLE pp_production_order_op (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 关联
    order_id        UUID NOT NULL REFERENCES pp_production_order_hdr(id) ON DELETE CASCADE,

    -- 工序信息
    operation_number VARCHAR(4) NOT NULL,
    operation_text  VARCHAR(100),

    -- 工作中心
    work_center_id  UUID REFERENCES pp_work_center(id),
    work_center_code VARCHAR(8),

    -- 标准值
    setup_time      DECIMAL(10,2),
    machine_time    DECIMAL(10,2),
    labor_time      DECIMAL(10,2),

    -- 计划
    planned_start_date DATE,
    planned_finish_date DATE,
    planned_qty     DECIMAL(13,3),

    -- 实际
    actual_start_date DATE,
    actual_finish_date DATE,
    actual_setup_time DECIMAL(10,2),
    actual_machine_time DECIMAL(10,2),
    actual_labor_time DECIMAL(10,2),
    confirmed_qty   DECIMAL(13,3) DEFAULT 0,

    -- 状态
    operation_status VARCHAR(2) DEFAULT '01',  -- 01:未开始 02:处理中 03:已完成
    confirmation_number INTEGER DEFAULT 0,     -- 确认次数

    -- 排序
    sort_order      INTEGER,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (order_id, operation_number)
);
```

---

## 6. 生产确认

### 6.1 生产确认 (pp_production_confirmation)

对标 SAP AFRU

```sql
CREATE TABLE pp_production_confirmation (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 确认信息
    confirmation_number VARCHAR(12) NOT NULL,  -- 确认号
    confirmation_type VARCHAR(2),              -- 确认类型
    -- 01:最终确认 02:部分确认 03:返工确认

    -- 订单
    order_id        UUID NOT NULL REFERENCES pp_production_order_hdr(id),
    order_number    VARCHAR(12),
    operation_number VARCHAR(4),               -- 工序号

    -- 数量
    yield_qty       DECIMAL(13,3) NOT NULL,    -- 产量
    scrap_qty       DECIMAL(13,3) DEFAULT 0,   -- 报废量
    rework_qty      DECIMAL(13,3) DEFAULT 0,   -- 返工量
    unit            VARCHAR(3) NOT NULL,

    -- 时间
    setup_time      DECIMAL(10,2),             -- 准备时间
    machine_time    DECIMAL(10,2),             -- 机器时间
    labor_time      DECIMAL(10,2),             -- 人工时间
    time_unit       VARCHAR(3) DEFAULT 'MIN',

    -- 日期
    confirmation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    posting_date    DATE NOT NULL DEFAULT CURRENT_DATE,

    -- 工作中心
    work_center_id  UUID REFERENCES pp_work_center(id),

    -- 员工
    employee_id     UUID REFERENCES hr_employee(id),

    -- 成本
    actual_cost     DECIMAL(15,2),

    -- 文本
    remarks         TEXT,

    -- 状态
    is_reversed     BOOLEAN DEFAULT FALSE,
    reversed_confirmation_id UUID,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, confirmation_number)
);
```

---

## 7. 物料需求计划 (MRP)

### 7.1 MRP 运行记录 (pp_mrp_run)

```sql
CREATE TABLE pp_mrp_run (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 运行信息
    run_number      VARCHAR(10) NOT NULL,      -- 运行号
    run_type        VARCHAR(2),                -- 运行类型
    -- 01:全厂 MRP 02:单物料 MRP 03:计划文件

    -- 范围
    plant_id        UUID REFERENCES sys_plant(id),
    mrp_controller  VARCHAR(3),                -- MRP控制者

    -- 时间
    run_date        DATE NOT NULL DEFAULT CURRENT_DATE,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    duration_seconds INTEGER,

    -- 参数
    planning_mode   VARCHAR(1),                -- 计划模式
    -- 1:净变化 2:再生 3:计划
    create_purchase_req BOOLEAN DEFAULT TRUE,  -- 创建采购申请
    create_planned_order BOOLEAN DEFAULT TRUE, -- 创建计划订单

    -- 结果
    materials_processed INTEGER DEFAULT 0,     -- 处理物料数
    exceptions_found INTEGER DEFAULT 0,        -- 异常数
    purchase_req_created INTEGER DEFAULT 0,    -- 采购申请数
    planned_orders_created INTEGER DEFAULT 0,  -- 计划订单数

    -- 状态
    run_status      VARCHAR(2) DEFAULT '01',   -- 01:运行中 02:完成 03:错误

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, run_number)
);
```

### 7.2 MRP 结果 (pp_mrp_result)

```sql
CREATE TABLE pp_mrp_result (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 运行
    run_id          UUID REFERENCES pp_mrp_run(id),

    -- 物料
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    -- MRP 数据
    mrp_date        DATE NOT NULL,             -- MRP 日期

    -- 需求
    gross_requirement DECIMAL(13,3),           -- 毛需求
    scheduled_receipt DECIMAL(13,3),           -- 计划接收
    available_qty   DECIMAL(13,3),             -- 可用量

    -- 计划
    planned_receipt DECIMAL(13,3),             -- 计划接收量
    planned_release DATE,                      -- 计划下达日期
    planned_order_qty DECIMAL(13,3),           -- 计划订单数量

    -- 例外
    exception_code  VARCHAR(2),                -- 例外代码
    -- 01:库存不足 02:订单延迟 03:产能不足
    exception_text  VARCHAR(100),

    -- 建议
    action_code     VARCHAR(2),                -- 建议代码
    -- 01:创建订单 02:修改订单 03:取消订单 04:催料

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 8. 存储过程

### 8.1 创建生产订单

```sql
CREATE OR REPLACE FUNCTION pp_create_production_order(
    p_tenant_id UUID,
    p_order_type VARCHAR,
    p_material_id UUID,
    p_plant_id UUID,
    p_order_qty DECIMAL,
    p_unit VARCHAR,
    p_user_id UUID
) RETURNS UUID AS $$
DECLARE
    v_order_id UUID;
    v_order_number VARCHAR(12);
    v_bom_id UUID;
    v_routing_id UUID;
    v_bom_itm RECORD;
    v_routing_op RECORD;
    v_seq INTEGER := 0;
BEGIN
    -- 生成订单号
    v_order_number := generate_business_code(p_tenant_id, 'PO', NULL, NULL);

    -- 查找有效 BOM
    SELECT id INTO v_bom_id
    FROM pp_bom_hdr
    WHERE material_id = p_material_id
      AND plant_id = p_plant_id
      AND CURRENT_DATE BETWEEN valid_from AND valid_to
      AND bom_status = '01'
    LIMIT 1;

    -- 查找有效工艺路线
    SELECT id INTO v_routing_id
    FROM pp_routing_hdr
    WHERE material_id = p_material_id
      AND plant_id = p_plant_id
      AND CURRENT_DATE BETWEEN valid_from AND valid_to
      AND routing_status = '02'
    LIMIT 1;

    -- 创建订单头
    INSERT INTO pp_production_order_hdr (
        tenant_id, order_number, order_type,
        material_id, plant_id,
        order_qty, unit,
        bom_id, routing_id,
        created_by, updated_by
    ) VALUES (
        p_tenant_id, v_order_number, p_order_type,
        p_material_id, p_plant_id,
        p_order_qty, p_unit,
        v_bom_id, v_routing_id,
        p_user_id, p_user_id
    ) RETURNING id INTO v_order_id;

    -- 复制 BOM 项目到订单 (可选)
    -- ...

    -- 复制工艺路线工序到订单
    IF v_routing_id IS NOT NULL THEN
        FOR v_routing_op IN
            SELECT * FROM pp_routing_operation
            WHERE routing_id = v_routing_id
            ORDER BY sort_order
        LOOP
            v_seq := v_seq + 1;
            INSERT INTO pp_production_order_op (
                tenant_id, order_id,
                operation_number, operation_text,
                work_center_id, work_center_code,
                setup_time, machine_time, labor_time,
                sort_order
            ) VALUES (
                p_tenant_id, v_order_id,
                v_routing_op.operation_number, v_routing_op.operation_text,
                v_routing_op.work_center_id, v_routing_op.work_center_code,
                v_routing_op.setup_time, v_routing_op.machine_time, v_routing_op.labor_time,
                v_seq
            );
        END LOOP;
    END IF;

    RETURN v_order_id;
END;
$$ LANGUAGE plpgsql;
```

### 8.2 生产确认过账

```sql
CREATE OR REPLACE FUNCTION pp_post_confirmation(
    p_order_id UUID,
    p_yield_qty DECIMAL,
    p_scrap_qty DECIMAL DEFAULT 0,
    p_operation_number VARCHAR DEFAULT NULL,
    p_user_id UUID
) RETURNS UUID AS $$
DECLARE
    v_confirmation_id UUID;
    v_confirmation_number VARCHAR(12);
    v_tenant_id UUID;
    v_order RECORD;
BEGIN
    -- 获取订单信息
    SELECT * INTO v_order
    FROM pp_production_order_hdr WHERE id = p_order_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION '生产订单不存在';
    END IF;

    v_tenant_id := v_order.tenant_id;

    -- 生成确认号
    v_confirmation_number := generate_business_code(v_tenant_id, 'CF', NULL, NULL);

    -- 创建确认记录
    INSERT INTO pp_production_confirmation (
        tenant_id, confirmation_number,
        order_id, order_number, operation_number,
        yield_qty, scrap_qty, unit,
        confirmation_date, posting_date,
        created_by
    ) VALUES (
        v_tenant_id, v_confirmation_number,
        p_order_id, v_order.order_number, p_operation_number,
        p_yield_qty, p_scrap_qty, v_order.unit,
        CURRENT_DATE, CURRENT_DATE,
        p_user_id
    ) RETURNING id INTO v_confirmation_id;

    -- 更新订单数量
    UPDATE pp_production_order_hdr
    SET confirmed_qty = confirmed_qty + p_yield_qty,
        scrapped_qty = scrapped_qty + p_scrap_qty,
        order_status = CASE
            WHEN confirmed_qty + p_yield_qty >= order_qty THEN '05'  -- 完成
            WHEN confirmed_qty + p_yield_qty > 0 THEN '04'            -- 在产
            ELSE order_status
        END,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_order_id;

    -- 更新工序数量
    IF p_operation_number IS NOT NULL THEN
        UPDATE pp_production_order_op
        SET confirmed_qty = confirmed_qty + p_yield_qty,
            operation_status = CASE
                WHEN confirmed_qty + p_yield_qty >= (SELECT order_qty FROM pp_production_order_hdr WHERE id = p_order_id) THEN '03'
                WHEN confirmed_qty + p_yield_qty > 0 THEN '02'
                ELSE operation_status
            END
        WHERE order_id = p_order_id
          AND operation_number = p_operation_number;
    END IF;

    RETURN v_confirmation_id;
END;
$$ LANGUAGE plpgsql;
```

---

## 9. 视图定义

### 9.1 生产订单跟踪视图

```sql
CREATE VIEW v_pp_order_tracking AS
SELECT
    o.order_number,
    o.order_date,
    o.order_type,
    m.material_code,
    m.description AS material_desc,
    o.order_qty,
    o.confirmed_qty,
    o.delivered_qty,
    o.scrapped_qty,
    o.unit,
    CASE WHEN o.confirmed_qty >= o.order_qty THEN '完成'
         WHEN o.confirmed_qty > 0 THEN '在产'
         ELSE '未开始' END AS progress_status,
    ROUND(o.confirmed_qty / NULLIF(o.order_qty, 0) * 100, 2) AS completion_pct,
    o.planned_start_date,
    o.planned_finish_date,
    o.actual_start_date,
    o.actual_finish_date,
    o.order_status,
    p.plant_code,
    p.plant_name

FROM pp_production_order_hdr o
JOIN mm_material m ON m.id = o.material_id
JOIN sys_plant p ON p.id = o.plant_id
ORDER BY o.order_date DESC;
```

### 9.2 BOM 展开视图

```sql
CREATE VIEW v_pp_bom_explosion AS
WITH RECURSIVE bom_tree AS (
    -- 顶层物料
    SELECT
        h.id AS bom_id,
        h.material_id,
        m.material_code,
        m.description,
        i.id AS item_id,
        i.material_id AS component_id,
        i.material_code AS component_code,
        i.description AS component_desc,
        i.component_qty,
        i.unit,
        1 AS level,
        ARRAY[h.id] AS path

    FROM pp_bom_hdr h
    JOIN mm_material m ON m.id = h.material_id
    JOIN pp_bom_itm i ON i.bom_id = h.id

    UNION ALL

    -- 递归展开
    SELECT
        sub_h.id,
        tree.component_id,
        tree.component_code,
        tree.component_desc,
        sub_i.id,
        sub_i.material_id,
        sub_i.material_code,
        sub_i.description,
        tree.component_qty * sub_i.component_qty,
        sub_i.unit,
        tree.level + 1,
        tree.path || sub_h.id

    FROM bom_tree tree
    JOIN pp_bom_hdr sub_h ON sub_h.material_id = tree.component_id
    JOIN pp_bom_itm sub_i ON sub_i.bom_id = sub_h.id
    WHERE tree.level < 10  -- 限制递归深度
)
SELECT * FROM bom_tree
ORDER BY path, level;
```

---

## 10. 索引策略

```sql
-- BOM
CREATE INDEX idx_pp_bom_material ON pp_bom_hdr (material_id, plant_id);
CREATE INDEX idx_pp_bom_validity ON pp_bom_hdr (valid_from, valid_to);
CREATE INDEX idx_pp_bom_itm_bom ON pp_bom_itm (bom_id);
CREATE INDEX idx_pp_bom_itm_material ON pp_bom_itm (material_id);

-- 工作中心
CREATE INDEX idx_pp_wc_plant ON pp_work_center (plant_id);
CREATE INDEX idx_pp_wc_code ON pp_work_center (plant_id, work_center_code);

-- 工艺路线
CREATE INDEX idx_pp_routing_material ON pp_routing_hdr (material_id, plant_id);
CREATE INDEX idx_pp_routing_op_routing ON pp_routing_operation (routing_id);

-- 生产订单
CREATE INDEX idx_pp_po_number ON pp_production_order_hdr (tenant_id, order_number);
CREATE INDEX idx_pp_po_material ON pp_production_order_hdr (material_id);
CREATE INDEX idx_pp_po_plant ON pp_production_order_hdr (plant_id);
CREATE INDEX idx_pp_po_status ON pp_production_order_hdr (order_status);
CREATE INDEX idx_pp_po_date ON pp_production_order_hdr (order_date);

-- 确认
CREATE INDEX idx_pp_cf_order ON pp_production_confirmation (order_id);
CREATE INDEX idx_pp_cf_date ON pp_production_confirmation (confirmation_date);
```

---

## 11. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
