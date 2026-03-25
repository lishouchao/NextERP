# WM 模块数据库设计

**模块**: Warehouse Management (仓库管理)
**对标**: SAP ECC WM (LL01/LL02/MB1B)
**版本**: 1.0

---

## 1. 模块概述

### 1.1 业务范围

| 子模块 | 说明 | SAP 对标 |
|--------|------|----------|
| 仓库号 | 仓库主数据 | SPRO |
| 存储类型 | 存储区域定义 | OMLC |
| 仓位 | 具体仓位管理 | LS01N/LS02N |
| 转运需求 | 仓库需求 | LB10/LB11 |
| 转运单 | 仓库移动 | LT01/LT02 |
| 仓位档案 | 仓位固定 | LQUA |

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                     WM Module Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    仓库结构                              │    │
│  │                                                          │    │
│  │  ┌─────────────┐                                        │    │
│  │  │  仓库号     │ (Warehouse Number)                     │    │
│  │  │ wm_warehouse│                                        │    │
│  │  └─────────────┘                                        │    │
│  │         │                                               │    │
│  │         ▼                                               │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │              存储类型 (Storage Type)              │    │    │
│  │  │  ┌─────────┐  ┌─────────┐  ┌─────────┐          │    │    │
│  │  │  │ 001     │  │ 002     │  │ 916     │          │    │    │
│  │  │  │高架库   │  │平库     │  │收货暂存 │          │    │    │
│  │  │  └─────────┘  └─────────┘  └─────────┘          │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │         │                                               │    │
│  │         ▼                                               │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │              仓位 (Storage Bin)                   │    │    │
│  │  │  ┌─────────────────────────────────────────────┐│    │    │
│  │  │  │ A-01-01  A-01-02  A-01-03 ...               ││    │    │
│  │  │  │ A-02-01  A-02-02  A-02-03 ...               ││    │    │
│  │  │  │ ...                                        ││    │    │
│  │  │  └─────────────────────────────────────────────┘│    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│        ┌─────────────────────┼─────────────────────┐            │
│        ▼                     ▼                     ▼            │
│  ┌──────────┐          ┌──────────┐          ┌──────────┐      │
│  │ 转运需求 │          │  转运单  │          │ 仓位置换 │      │
│  │wm_tr_req │          │ wm_to    │          │ wm_stock │      │
│  └──────────┘          └──────────┘          └──────────┘      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 与 MM 模块集成

```
┌─────────────────────────────────────────────────────────────────┐
│                     WM & MM Integration                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   MM 模块                           WM 模块                      │
│  ┌─────────────┐                   ┌─────────────┐              │
│  │ 库存地点    │◄─────────────────►│ 仓库号      │              │
│  │ sloc_id     │    一对多         │ warehouse_id│              │
│  └─────────────┘                   └─────────────┘              │
│        │                                  │                      │
│        ▼                                  ▼                      │
│  ┌─────────────┐                   ┌─────────────┐              │
│  │ 物料凭证    │       触发        │ 转运需求    │              │
│  │ mm_material │─────────────────►│ wm_tr_req   │              │
│  │ _document   │                   └─────────────┘              │
│  └─────────────┘                         │                      │
│                                          ▼                      │
│                                   ┌─────────────┐              │
│                                   │  转运单     │              │
│                                   │ wm_to       │              │
│                                   └─────────────┘              │
│                                          │                      │
│                                          ▼                      │
│                                   ┌─────────────┐              │
│                                   │ 仓位库存    │              │
│                                   │ wm_bin_stock│              │
│                                   └─────────────┘              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 仓库号与存储类型

### 2.1 仓库号 (wm_warehouse)

对标 SAP T300

```sql
CREATE TABLE wm_warehouse (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 仓库号
    warehouse_number VARCHAR(3) NOT NULL UNIQUE, -- 仓库号 (如 001)
    warehouse_name  VARCHAR(100) NOT NULL,       -- 仓库名称
    description     TEXT,

    -- 关联库存地点
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),

    -- 仓库类型
    warehouse_type  VARCHAR(2),                -- 仓库类型
    -- 01:标准仓库 02:高货架仓库 03:自动仓库 04:越库仓库

    -- 地址
    country_id      UUID REFERENCES core_country(id),
    region_id       UUID REFERENCES core_region(id),
    city            VARCHAR(40),
    street          VARCHAR(60),
    postal_code     VARCHAR(10),

    -- 仓库参数
    verification_profile VARCHAR(4),           -- 验证档案
    picking_strategy VARCHAR(2),               -- 拣配策略
    -- 01:FIFO 02:LIFO 03:固定仓位 04:随机

    -- 状态
    warehouse_status general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (plant_id, warehouse_number)
);
```

### 2.2 存储类型 (wm_storage_type)

对标 SAP T301

```sql
CREATE TABLE wm_storage_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 仓库
    warehouse_id    UUID NOT NULL REFERENCES wm_warehouse(id) ON DELETE CASCADE,
    warehouse_number VARCHAR(3),

    -- 存储类型
    storage_type_code VARCHAR(3) NOT NULL,     -- 存储类型代码
    storage_type_name VARCHAR(100) NOT NULL,   -- 存储类型名称
    description     TEXT,

    -- 类型分类
    storage_type_category VARCHAR(1),          -- 存储类型分类
    -- 1:标准 2:过账变更 3:生产供应 4:质检 5:退货 6:外部

    -- 仓位结构
    bin_structure   VARCHAR(2),                -- 仓位结构
    -- 01:行-架-层 02:区域-行-层 03:自定义

    -- 仓位模板
    bin_template    VARCHAR(10),               -- 仓位命名模板

    -- 仓位数量
    bin_count       INTEGER DEFAULT 0,         -- 仓位数

    -- 容量
    max_capacity    DECIMAL(13,3),             -- 最大容量
    capacity_unit   VARCHAR(3),                -- 容量单位

    -- 策略
    putaway_strategy VARCHAR(2),               -- 上架策略
    -- 01:固定仓位 02:空仓位 03:混合仓位 04:就近上架
    picking_strategy VARCHAR(2),               -- 拣配策略

    -- 堆叠
    is_bulk_storage BOOLEAN DEFAULT FALSE,     -- 散装存储
    stacking_factor INTEGER DEFAULT 1,         -- 堆叠因子

    -- 状态
    st_type_status  general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (warehouse_id, storage_type_code)
);
```

**常用存储类型**:

| 代码 | 名称 | 类型分类 | 说明 |
|------|------|----------|------|
| 001 | 高架库 | 1 | 标准高架存储 |
| 002 | 平库 | 1 | 地面存储 |
| 003 | 托盘位 | 1 | 托盘存储 |
| 100 | 收货区 | 2 | 收货暂存 |
| 916 | GR Zone | 2 | 过账变更区 |
| 922 | GI Zone | 2 | 发货区 |
| 902 | 发货暂存 | 2 | 出库暂存 |
| 021 | 质检区 | 4 | 质量检验 |
| 003 | 退货区 | 5 | 退货存储 |

---

## 3. 仓位管理

### 3.1 仓位主数据 (wm_storage_bin)

对标 SAP LAGP

```sql
CREATE TABLE wm_storage_bin (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 仓库/存储类型
    warehouse_id    UUID NOT NULL REFERENCES wm_warehouse(id),
    warehouse_number VARCHAR(3),
    storage_type_id UUID NOT NULL REFERENCES wm_storage_type(id),
    storage_type_code VARCHAR(3),

    -- 仓位号
    bin_code        VARCHAR(10) NOT NULL,      -- 仓位号 (如 A-01-01)
    bin_description VARCHAR(100),              -- 仓位描述

    -- 仓位坐标
    row_number      VARCHAR(4),                -- 行
    stack_level     VARCHAR(4),                -- 架/列
    level_number    VARCHAR(4),                -- 层

    -- 仓位类型
    bin_type        VARCHAR(2),                -- 仓位类型
    -- 01:标准 02:小仓位 03:大仓位 04:地面位

    -- 容量
    max_weight      DECIMAL(13,3),             -- 最大重量
    weight_unit     VARCHAR(3),
    max_volume      DECIMAL(13,3),             -- 最大体积
    volume_unit     VARCHAR(3),
    max_pallets     INTEGER DEFAULT 1,         -- 最大托盘数

    -- 占用
    used_weight     DECIMAL(13,3) DEFAULT 0,   -- 已用重量
    used_volume     DECIMAL(13,3) DEFAULT 0,   -- 已用体积
    pallet_count    INTEGER DEFAULT 0,         -- 托盘数

    -- 固定物料
    is_fixed        BOOLEAN DEFAULT FALSE,     -- 固定仓位
    fixed_material_id UUID REFERENCES mm_material(id),

    -- 阻止
    putaway_blocked  BOOLEAN DEFAULT FALSE,    -- 上架阻止
    picking_blocked  BOOLEAN DEFAULT FALSE,    -- 拣配阻止
    blocked_reason   VARCHAR(50),              -- 阻止原因

    -- 状态
    bin_status      VARCHAR(2) DEFAULT '01',   -- 01:空 02:部分 03:满
    is_empty        BOOLEAN DEFAULT TRUE,

    -- 最后移动
    last_movement_date DATE,
    last_movement_type VARCHAR(3),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (warehouse_id, storage_type_code, bin_code)
);
```

### 3.2 仓位库存 (wm_bin_stock)

对标 SAP LQUA

```sql
CREATE TABLE wm_bin_stock (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 仓位
    warehouse_id    UUID NOT NULL REFERENCES wm_warehouse(id),
    storage_type_id UUID REFERENCES wm_storage_type(id),
    storage_bin_id  UUID NOT NULL REFERENCES wm_storage_bin(id),
    bin_code        VARCHAR(10),

    -- 物料
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    -- 批次
    batch_number    VARCHAR(10),
    valuation_type  VARCHAR(10),

    -- 特殊库存
    special_stock   VARCHAR(1),                -- 特殊库存标识
    -- E:销售订单 K:寄售 O:分包订单
    special_stock_number VARCHAR(18),

    -- 数量
    quantity        DECIMAL(13,3) NOT NULL,    -- 库存数量
    base_unit       VARCHAR(3) NOT NULL,       -- 基本单位

    -- 金额
    value           DECIMAL(15,2),             -- 库存金额

    -- 重量/体积
    weight          DECIMAL(13,3),
    weight_unit     VARCHAR(3),
    volume          DECIMAL(13,3),
    volume_unit     VARCHAR(3),

    -- 入库日期
    gr_date         DATE DEFAULT CURRENT_DATE, -- 入库日期

    -- 最后盘点
    last_count_date DATE,
    last_count_quantity DECIMAL(13,3),

    -- 状态
    stock_type      VARCHAR(2) DEFAULT '01',   -- 01:非限制 02:质检 03:冻结
    stock_status    VARCHAR(2) DEFAULT '01',   -- 01:可用 02:拣配中 03:锁定

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_wm_bin_stock_bin ON wm_bin_stock (storage_bin_id);
CREATE INDEX idx_wm_bin_stock_material ON wm_bin_stock (material_id, plant_id);
CREATE INDEX idx_wm_bin_stock_batch ON wm_bin_stock (material_id, batch_number);
```

---

## 4. 转运需求

### 4.1 转运需求 (wm_transfer_requirement)

对标 SAP LTAK

```sql
CREATE TABLE wm_transfer_requirement (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- TR 号
    tr_number       VARCHAR(12) NOT NULL,      -- TR号
    tr_item         INTEGER NOT NULL,          -- TR项

    -- 仓库
    warehouse_id    UUID NOT NULL REFERENCES wm_warehouse(id),
    warehouse_number VARCHAR(3),

    -- 物料
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),

    -- 批次
    batch_number    VARCHAR(10),

    -- 数量
    quantity        DECIMAL(13,3) NOT NULL,    -- 需求数量
    base_unit       VARCHAR(3) NOT NULL,
    quantity_processed DECIMAL(13,3) DEFAULT 0, -- 已处理数量

    -- 来源/目标
    source_st_type  VARCHAR(3),                -- 源存储类型
    source_bin      VARCHAR(10),               -- 源仓位
    dest_st_type    VARCHAR(3),                -- 目标存储类型
    dest_bin        VARCHAR(10),               -- 目标仓位

    -- 移动类型
    movement_type   VARCHAR(3) NOT NULL,       -- 移动类型
    -- 101:上架 201:拣配 309:仓位转移 311:存储类型转移
    movement_reason VARCHAR(4),                -- 移动原因

    -- 来源单据
    source_type     VARCHAR(2),                -- 来源类型
    -- MM:物料凭证 PO:采购订单 SO:销售订单 PR:生产订单
    source_document VARCHAR(20),               -- 来源单据
    source_item     INTEGER,

    -- 优先级
    priority        VARCHAR(2) DEFAULT '03',   -- 01:紧急 02:高 03:正常 04:低

    -- 日期
    created_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    required_date   DATE,                      -- 需求日期

    -- 状态
    tr_status       VARCHAR(2) DEFAULT '01',   -- 01:未处理 02:已创建TO 03:已完成 04:取消
    is_processed    BOOLEAN DEFAULT FALSE,

    -- 关联TO
    to_id           UUID,                      -- 关联的转运单ID
    to_number       VARCHAR(10),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, tr_number, tr_item)
);
```

---

## 5. 转运单

### 5.1 转运单头 (wm_transfer_order_hdr)

对标 SAP LTAK

```sql
CREATE TABLE wm_transfer_order_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- TO 号
    to_number       VARCHAR(10) NOT NULL,      -- TO号

    -- 仓库
    warehouse_id    UUID NOT NULL REFERENCES wm_warehouse(id),
    warehouse_number VARCHAR(3),

    -- TO 类型
    to_type         VARCHAR(2) NOT NULL,       -- TO类型
    -- 01:上架 02:拣配 03:内部转储 04:补货

    -- 来源
    tr_id           UUID REFERENCES wm_transfer_requirement(id),
    tr_number       VARCHAR(12),

    -- 来源单据
    source_type     VARCHAR(2),
    source_document VARCHAR(20),

    -- 日期
    created_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    confirmed_date  DATE,                      -- 确认日期

    -- 操作人
    assigned_to     UUID REFERENCES hr_employee(id), -- 分配人员

    -- 状态
    to_status       VARCHAR(2) DEFAULT '01',   -- 01:创建 02:处理中 03:已确认 04:取消
    system_status   VARCHAR(10),               -- 系统状态

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confirmed_at    TIMESTAMP,
    confirmed_by    UUID,

    UNIQUE (tenant_id, to_number)
);
```

### 5.2 转运单项 (wm_transfer_order_item)

对标 SAP LTAP

```sql
CREATE TABLE wm_transfer_order_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- TO头
    header_id       UUID NOT NULL REFERENCES wm_transfer_order_hdr(id) ON DELETE CASCADE,
    to_number       VARCHAR(10),
    to_item         INTEGER NOT NULL,          -- TO项

    -- 物料
    material_id     UUID NOT NULL REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    plant_id        UUID REFERENCES sys_plant(id),

    -- 批次
    batch_number    VARCHAR(10),

    -- 数量
    target_qty      DECIMAL(13,3) NOT NULL,    -- 目标数量
    picked_qty      DECIMAL(13,3) DEFAULT 0,   -- 拣配数量
    difference_qty  DECIMAL(13,3) DEFAULT 0,   -- 差异数量
    base_unit       VARCHAR(3) NOT NULL,

    -- 来源仓位
    source_st_type  VARCHAR(3),
    source_bin_id   UUID REFERENCES wm_storage_bin(id),
    source_bin      VARCHAR(10),

    -- 目标仓位
    dest_st_type    VARCHAR(3),
    dest_bin_id     UUID REFERENCES wm_storage_bin(id),
    dest_bin        VARCHAR(10),

    -- 移动类型
    movement_type   VARCHAR(3),

    -- 重量/体积
    weight          DECIMAL(13,3),
    weight_unit     VARCHAR(3),
    volume          DECIMAL(13,3),
    volume_unit     VARCHAR(3),

    -- 状态
    item_status     VARCHAR(2) DEFAULT '01',   -- 01:未开始 02:已拣配 03:已确认

    -- TR关联
    tr_id           UUID REFERENCES wm_transfer_requirement(id),

    -- 确认
    confirmed_at    TIMESTAMP,
    confirmed_by    UUID,

    -- 差异原因
    difference_reason VARCHAR(4),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (header_id, to_item)
);
```

---

## 6. 策略配置

### 6.1 上架策略 (wm_putaway_strategy)

```sql
CREATE TABLE wm_putaway_strategy (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 策略编码
    strategy_code   VARCHAR(2) NOT NULL,
    strategy_name   VARCHAR(100) NOT NULL,

    -- 策略类型
    strategy_type   VARCHAR(2),                -- 策略类型
    -- 01:固定仓位 02:空仓位 03:混合仓位 04:就近上架

    -- 物料组
    material_group  VARCHAR(9),

    -- 优先级存储类型
    priority_st_type_01 VARCHAR(3),
    priority_st_type_02 VARCHAR(3),
    priority_st_type_03 VARCHAR(3),

    -- 容量检查
    capacity_check  BOOLEAN DEFAULT TRUE,

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, strategy_code)
);
```

### 6.2 拣配策略 (wm_picking_strategy)

```sql
CREATE TABLE wm_picking_strategy (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 策略编码
    strategy_code   VARCHAR(2) NOT NULL,
    strategy_name   VARCHAR(100) NOT NULL,

    -- 策略类型
    strategy_type   VARCHAR(2),                -- 策略类型
    -- 01:FIFO 02:LIFO 03:先到期先出 04:固定仓位

    -- 物料组
    material_group  VARCHAR(9),

    -- 批次选择
    batch_selection VARCHAR(2),                -- 批次选择规则
    -- 01:按到期日 02:按入库日 03:按批次号

    -- 部分拣配
    allow_partial   BOOLEAN DEFAULT TRUE,

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, strategy_code)
);
```

---

## 7. 存储过程

### 7.1 创建转运单

```sql
CREATE OR REPLACE FUNCTION wm_create_transfer_order(
    p_tr_id UUID,
    p_user_id UUID
) RETURNS UUID AS $$
DECLARE
    v_tr RECORD;
    v_to_id UUID;
    v_to_number VARCHAR(10);
    v_to_item INTEGER := 0;
BEGIN
    -- 获取TR信息
    SELECT * INTO v_tr
    FROM wm_transfer_requirement WHERE id = p_tr_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION '转运需求不存在';
    END IF;

    -- 生成TO号
    v_to_number := generate_business_code(v_tr.tenant_id, 'TO', NULL, NULL);

    -- 创建TO头
    INSERT INTO wm_transfer_order_hdr (
        tenant_id, to_number, warehouse_id,
        to_type, tr_id, tr_number,
        created_date, assigned_to
    ) VALUES (
        v_tr.tenant_id, v_to_number, v_tr.warehouse_id,
        '03', p_tr_id, v_tr.tr_number,
        CURRENT_DATE, p_user_id
    ) RETURNING id INTO v_to_id;

    -- 创建TO项
    v_to_item := v_to_item + 10;
    INSERT INTO wm_transfer_order_item (
        tenant_id, header_id, to_number, to_item,
        material_id, material_code, plant_id,
        batch_number, target_qty, base_unit,
        source_st_type, source_bin,
        dest_st_type, dest_bin,
        movement_type, tr_id
    ) VALUES (
        v_tr.tenant_id, v_to_id, v_to_number, v_to_item,
        v_tr.material_id, v_tr.material_code, v_tr.plant_id,
        v_tr.batch_number, v_tr.quantity, v_tr.base_unit,
        v_tr.source_st_type, v_tr.source_bin,
        v_tr.dest_st_type, v_tr.dest_bin,
        v_tr.movement_type, p_tr_id
    );

    -- 更新TR状态
    UPDATE wm_transfer_requirement
    SET tr_status = '02',
        is_processed = TRUE,
        to_id = v_to_id,
        to_number = v_to_number
    WHERE id = p_tr_id;

    RETURN v_to_id;
END;
$$ LANGUAGE plpgsql;
```

### 7.2 确认转运单

```sql
CREATE OR REPLACE FUNCTION wm_confirm_transfer_order(
    p_to_id UUID,
    p_confirmed_qty DECIMAL,
    p_user_id UUID
) RETURNS BOOLEAN AS $$
DECLARE
    v_to RECORD;
    v_to_item RECORD;
BEGIN
    -- 获取TO信息
    SELECT * INTO v_to
    FROM wm_transfer_order_hdr WHERE id = p_to_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION '转运单不存在';
    END IF;

    -- 更新TO项
    UPDATE wm_transfer_order_item
    SET picked_qty = p_confirmed_qty,
        item_status = '03',
        confirmed_at = CURRENT_TIMESTAMP,
        confirmed_by = p_user_id
    WHERE header_id = p_to_id;

    -- 更新仓位库存
    FOR v_to_item IN
        SELECT * FROM wm_transfer_order_item WHERE header_id = p_to_id
    LOOP
        -- 扣减源仓位
        UPDATE wm_bin_stock
        SET quantity = quantity - v_to_item.picked_qty,
            updated_at = CURRENT_TIMESTAMP
        WHERE storage_bin_id = v_to_item.source_bin_id
          AND material_id = v_to_item.material_id;

        -- 增加目标仓位
        INSERT INTO wm_bin_stock (
            tenant_id, warehouse_id, storage_type_id, storage_bin_id, bin_code,
            material_id, material_code, plant_id,
            batch_number, quantity, base_unit, gr_date
        ) SELECT
            v_to.tenant_id, v_to.warehouse_id,
            (SELECT id FROM wm_storage_type WHERE warehouse_id = v_to.warehouse_id AND storage_type_code = v_to_item.dest_st_type),
            v_to_item.dest_bin_id, v_to_item.dest_bin,
            v_to_item.material_id, v_to_item.material_code, v_to_item.plant_id,
            v_to_item.batch_number, v_to_item.picked_qty, v_to_item.base_unit, CURRENT_DATE
        ON CONFLICT DO NOTHING;

        UPDATE wm_bin_stock
        SET quantity = quantity + v_to_item.picked_qty,
            updated_at = CURRENT_TIMESTAMP
        WHERE storage_bin_id = v_to_item.dest_bin_id
          AND material_id = v_to_item.material_id;
    END LOOP;

    -- 更新TO状态
    UPDATE wm_transfer_order_hdr
    SET to_status = '03',
        confirmed_date = CURRENT_DATE,
        confirmed_at = CURRENT_TIMESTAMP,
        confirmed_by = p_user_id
    WHERE id = p_to_id;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
```

---

## 8. 视图定义

### 8.1 仓位库存查询视图

```sql
CREATE VIEW v_wm_bin_stock AS
SELECT
    w.warehouse_number,
    w.warehouse_name,
    st.storage_type_code,
    st.storage_type_name,
    sb.bin_code,
    sb.bin_status,
    sb.is_empty,
    m.material_code,
    m.description AS material_desc,
    bs.batch_number,
    bs.quantity,
    bs.base_unit,
    bs.value,
    bs.stock_type,
    bs.stock_status,
    bs.gr_date,
    p.plant_code,
    p.plant_name

FROM wm_bin_stock bs
JOIN wm_warehouse w ON w.id = bs.warehouse_id
LEFT JOIN wm_storage_type st ON st.id = bs.storage_type_id
LEFT JOIN wm_storage_bin sb ON sb.id = bs.storage_bin_id
JOIN mm_material m ON m.id = bs.material_id
JOIN sys_plant p ON p.id = bs.plant_id
WHERE bs.quantity > 0
ORDER BY w.warehouse_number, st.storage_type_code, sb.bin_code;
```

### 8.2 待处理转运单视图

```sql
CREATE VIEW v_wm_pending_to AS
SELECT
    h.to_number,
    h.to_type,
    h.created_date,
    w.warehouse_number,
    w.warehouse_name,
    i.to_item,
    i.material_code,
    i.batch_number,
    i.target_qty,
    i.picked_qty,
    i.base_unit,
    i.source_bin,
    i.dest_bin,
    i.item_status,
    emp.full_name AS assigned_to_name,
    CASE
        WHEN i.item_status = '01' THEN '待处理'
        WHEN i.item_status = '02' THEN '已拣配'
        WHEN i.item_status = '03' THEN '已确认'
        ELSE '未知'
    END AS status_text

FROM wm_transfer_order_hdr h
JOIN wm_warehouse w ON w.id = h.warehouse_id
JOIN wm_transfer_order_item i ON i.header_id = h.id
LEFT JOIN hr_employee emp ON emp.id = h.assigned_to
WHERE h.to_status IN ('01', '02')
ORDER BY h.created_date, h.to_number;
```

---

## 9. 索引策略

```sql
-- 仓库
CREATE INDEX idx_wm_wh_number ON wm_warehouse (tenant_id, warehouse_number);
CREATE INDEX idx_wm_wh_plant ON wm_warehouse (plant_id);

-- 存储类型
CREATE INDEX idx_wm_st_warehouse ON wm_storage_type (warehouse_id);

-- 仓位
CREATE INDEX idx_wm_bin_warehouse ON wm_storage_bin (warehouse_id);
CREATE INDEX idx_wm_bin_st_type ON wm_storage_bin (storage_type_id);
CREATE INDEX idx_wm_bin_code ON wm_storage_bin (warehouse_id, storage_type_code, bin_code);

-- 仓位库存
CREATE INDEX idx_wm_stock_bin ON wm_bin_stock (storage_bin_id);
CREATE INDEX idx_wm_stock_material ON wm_bin_stock (material_id, plant_id);
CREATE INDEX idx_wm_stock_batch ON wm_bin_stock (material_id, batch_number);

-- 转运需求
CREATE INDEX idx_wm_tr_number ON wm_transfer_requirement (tenant_id, tr_number);
CREATE INDEX idx_wm_tr_status ON wm_transfer_requirement (tr_status);
CREATE INDEX idx_wm_tr_material ON wm_transfer_requirement (material_id);

-- 转运单
CREATE INDEX idx_wm_to_number ON wm_transfer_order_hdr (tenant_id, to_number);
CREATE INDEX idx_wm_to_status ON wm_transfer_order_hdr (to_status);
CREATE INDEX idx_wm_to_item_header ON wm_transfer_order_item (header_id);
```

---

## 10. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
