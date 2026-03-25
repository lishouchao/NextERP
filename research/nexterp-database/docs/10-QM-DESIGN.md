# QM 模块数据库设计

**模块**: Quality Management (质量管理)
**对标**: SAP ECC QM (QALS/QAVE/QAMR)
**版本**: 1.0

---

## 1. 模块概述

### 1.1 业务范围

| 子模块 | 说明 | SAP 对标 |
|--------|------|----------|
| 检验计划 | 检验特性与检验方法 | QP01/QP02 |
| 检验批 | 来料/过程/成品检验 | QA01/QA02 |
| 检验结果 | 结果录入与记录 | QE01/QE02 |
| 检验决策 | 使用决策 | QA11/QA12 |
| 质量通知 | 质量问题处理 | QM01/QM02 |

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                     QM Module Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    检验计划                              │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │    │
│  │  │ 检验特性    │  │ 检验方法    │  │ 采样过程    │      │    │
│  │  │qm_character │  │ qm_method   │  │ qm_sampling │      │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    检验执行                              │    │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐      │    │
│  │  │  检验批     │─►│  检验结果   │─►│  使用决策   │      │    │
│  │  │ qm_inspection│ │ qm_result   │  │ qm_decision │      │    │
│  │  │    _lot     │  │             │  │             │      │    │
│  │  └─────────────┘  └─────────────┘  └─────────────┘      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│                              ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    质量通知                              │    │
│  │                     qm_notification                       │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 检验计划

### 2.1 检验特性 (qm_characteristic)

对标 SAP QPMZ

```sql
CREATE TABLE qm_characteristic (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 特性信息
    char_code       VARCHAR(8) NOT NULL,       -- 特性代码
    char_name       VARCHAR(100) NOT NULL,     -- 特性名称
    description     TEXT,

    -- 类型
    char_type       VARCHAR(2) NOT NULL,       -- 特性类型
    -- 01:定量 02:定性

    -- 定量特性参数
    target_value    DECIMAL(15,4),             -- 目标值
    lower_limit     DECIMAL(15,4),             -- 下限
    upper_limit     DECIMAL(15,4),             -- 上限
    unit            VARCHAR(3),                -- 单位

    -- 定性特性
    code_group      VARCHAR(8),                -- 代码组
    selected_set    VARCHAR(4),                -- 选择集

    -- 检验方法
    method_id       UUID REFERENCES qm_inspection_method(id),

    -- 采样
    sampling_procedure VARCHAR(8),             -- 采样过程
    sample_quantity DECIMAL(13,3),             -- 样本量

    -- 控制参数
    is_measured_value_required BOOLEAN DEFAULT TRUE, -- 必须录入测量值
    is_summary_characteristic BOOLEAN DEFAULT FALSE, -- 汇总特性
    isdestructive   BOOLEAN DEFAULT FALSE,     -- 破坏性检验

    -- 评分
    scoring_method  VARCHAR(2),                -- 评分方法
    -- 01:接受/拒绝 02:评分 03:属性计数

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, char_code)
);
```

### 2.2 检验方法 (qm_inspection_method)

对标 SAP QPMZ

```sql
CREATE TABLE qm_inspection_method (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 方法信息
    method_code     VARCHAR(8) NOT NULL,       -- 方法代码
    method_name     VARCHAR(100) NOT NULL,     -- 方法名称
    description     TEXT,

    -- 设备
    equipment_required BOOLEAN DEFAULT FALSE,  -- 需要设备
    measuring_equipment VARCHAR(20),           -- 测量设备

    -- 时间
    inspection_time DECIMAL(8,2),              -- 检验时间 (分钟)

    -- 文档
    work_instruction TEXT,                     -- 作业指导书
    document_id     UUID,                      -- 关联文档

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, method_code)
);
```

### 2.3 采样过程 (qm_sampling_procedure)

对标 SAP QPAC

```sql
CREATE TABLE qm_sampling_procedure (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 采样信息
    sampling_code   VARCHAR(8) NOT NULL,       -- 采样代码
    sampling_name   VARCHAR(100) NOT NULL,     -- 采样名称

    -- 采样类型
    sampling_type   VARCHAR(2) NOT NULL,       -- 采样类型
    -- 01:固定样本量 02:百分比 03:AQL抽样

    -- 固定样本量
    fixed_sample_qty DECIMAL(13,3),            -- 固定样本量

    -- 百分比
    sample_percent  DECIMAL(5,2),              -- 采样百分比
    min_sample_qty  DECIMAL(13,3),             -- 最小样本量
    max_sample_qty  DECIMAL(13,3),             -- 最大样本量

    -- AQL
    aql_level       VARCHAR(3),                -- AQL水平
    inspection_level VARCHAR(2),               -- 检验水平
    -- G1-G4:一般检验 S1-S4:特殊检验

    -- 评估代码
    valuation_mode  VARCHAR(2),                -- 评估模式
    -- 01:属性检验 02:变量检验 03:手动评估

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, sampling_code)
);
```

---

## 3. 检验批

### 3.1 检验批 (qm_inspection_lot)

对标 SAP QALS

```sql
CREATE TABLE qm_inspection_lot (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 检验批信息
    lot_number      VARCHAR(12) NOT NULL,      -- 检验批号
    lot_type        VARCHAR(3) NOT NULL,       -- 检验类型
    -- 01:来料检验 02:过程检验 03:成品检验 04:库存检验

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    batch_number    VARCHAR(10),

    -- 数量
    lot_quantity    DECIMAL(13,3) NOT NULL,    -- 批次数量
    sample_quantity DECIMAL(13,3),             -- 样本数量
    inspected_quantity DECIMAL(13,3) DEFAULT 0, -- 已检数量
    accepted_quantity DECIMAL(13,3) DEFAULT 0,  -- 接受数量
    rejected_quantity DECIMAL(13,3) DEFAULT 0,  -- 拒收数量
    unit            VARCHAR(3) NOT NULL,

    -- 组织
    plant_id        UUID NOT NULL REFERENCES sys_plant(id),
    sloc_id         UUID REFERENCES sys_storage_location(id),

    -- 来源
    source_type     VARCHAR(2),                -- 来源类型
    -- PO:采购订单 PR:生产订单 SD:销售退货 ST:库存转移
    source_document VARCHAR(20),               -- 来源单据号
    source_item     INTEGER,                   -- 来源行号
    source_id       UUID,                      -- 来源ID

    -- 供应商/客户
    vendor_id       UUID REFERENCES bp_business_partner(id),
    customer_id     UUID REFERENCES bp_business_partner(id),

    -- 日期
    created_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    inspection_due_date DATE,                  -- 检验到期日
    inspection_date DATE,                      -- 实际检验日期

    -- 状态
    lot_status      VARCHAR(2) DEFAULT '01',   -- 01:创建 02:在检 03:完成 04:取消
    system_status   VARCHAR(10),               -- 系统状态 (REL/CALC/CLO)

    -- 决策
    decision_made   BOOLEAN DEFAULT FALSE,     -- 已做决策
    decision_result VARCHAR(2),                -- 决策结果
    -- AC:接受 RJ:拒收 PR:条件接受

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, lot_number)
);
```

### 3.2 检验批特性 (qm_inspection_char)

```sql
CREATE TABLE qm_inspection_char (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 检验批
    lot_id          UUID NOT NULL REFERENCES qm_inspection_lot(id) ON DELETE CASCADE,
    lot_number      VARCHAR(12),

    -- 特性
    char_id         UUID REFERENCES qm_characteristic(id),
    char_code       VARCHAR(8),
    char_name       VARCHAR(100),

    -- 序号
    char_number     INTEGER NOT NULL,          -- 特性序号

    -- 参数 (复制自检验特性)
    char_type       VARCHAR(2),                -- 特性类型
    target_value    DECIMAL(15,4),             -- 目标值
    lower_limit     DECIMAL(15,4),             -- 下限
    upper_limit     DECIMAL(15,4),             -- 上限
    unit            VARCHAR(3),

    -- 样本
    sample_quantity DECIMAL(13,3),             -- 样本量
    samples_planned INTEGER,                   -- 计划样本数

    -- 结果摘要
    measured_samples INTEGER DEFAULT 0,        -- 已测样本数
    accepted_samples INTEGER DEFAULT 0,        -- 接受样本数
    rejected_samples INTEGER DEFAULT 0,        -- 拒收样本数

    -- 评估
    evaluation_result VARCHAR(2),              -- 评估结果
    -- A:接受 R:拒收
    is_evaluated    BOOLEAN DEFAULT FALSE,

    -- 状态
    char_status     VARCHAR(2) DEFAULT '01',   -- 01:未检 02:检验中 03:完成

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (lot_id, char_number)
);
```

---

## 4. 检验结果

### 4.1 检验结果 (qm_inspection_result)

对标 SAP QAMR

```sql
CREATE TABLE qm_inspection_result (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 检验批
    lot_id          UUID NOT NULL REFERENCES qm_inspection_lot(id),
    lot_number      VARCHAR(12),

    -- 特性
    insp_char_id    UUID NOT NULL REFERENCES qm_inspection_char(id),
    char_code       VARCHAR(8),
    char_number     INTEGER,

    -- 样本
    sample_number   INTEGER NOT NULL,          -- 样本号

    -- 测量值
    measured_value  DECIMAL(15,4),             -- 测量值
    original_value  VARCHAR(30),               -- 原始值
    unit            VARCHAR(3),

    -- 编码结果 (定性特性)
    code_value      VARCHAR(4),                -- 代码值
    code_text       VARCHAR(40),               -- 代码描述

    -- 评估
    evaluation_result VARCHAR(2),              -- 评估结果
    -- A:接受 R:拒收
    is_out_of_spec  BOOLEAN DEFAULT FALSE,     -- 超标

    -- 检验信息
    inspection_date DATE DEFAULT CURRENT_DATE,
    inspector_id    UUID REFERENCES hr_employee(id),
    equipment_id    UUID,                      -- 检验设备

    -- 备注
    remarks         TEXT,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (lot_id, char_number, sample_number)
);
```

---

## 5. 使用决策

### 5.1 使用决策 (qm_usage_decision)

对标 SAP QAVE

```sql
CREATE TABLE qm_usage_decision (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 检验批
    lot_id          UUID NOT NULL REFERENCES qm_inspection_lot(id),
    lot_number      VARCHAR(12),

    -- 决策信息
    decision_code   VARCHAR(4) NOT NULL,       -- 决策代码
    decision_text   VARCHAR(100),              -- 决策文本
    decision_result VARCHAR(2) NOT NULL,       -- 决策结果
    -- AC:接受 RJ:拒收 PR:条件接受 SC:报废

    -- 数量处理
    accepted_qty    DECIMAL(13,3),             -- 接受数量
    rejected_qty    DECIMAL(13,3),             -- 拒收数量
    scrapped_qty    DECIMAL(13,3),             -- 报废数量
    rework_qty      DECIMAL(13,3),             -- 返工数量

    -- 库存过账
    is_stock_posted BOOLEAN DEFAULT FALSE,     -- 已过账到库存
    movement_type   VARCHAR(3),                -- 移动类型
    material_doc_id UUID,                      -- 物料凭证ID

    -- 日期
    decision_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    decision_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 决策人
    decision_by     UUID REFERENCES hr_employee(id),

    -- 备注
    remarks         TEXT,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (lot_id)
);
```

**决策代码枚举**:

| 代码 | 结果 | 说明 |
|------|------|------|
| A01 | AC | 接受 - 库存转入非限制 |
| A02 | AC | 接受 - 库存转入质检 |
| R01 | RJ | 拒收 - 退货给供应商 |
| R02 | RJ | 拒收 - 转入冻结库存 |
| R03 | SC | 报废 |
| P01 | PR | 条件接受 - 让步接收 |

---

## 6. 质量通知

### 6.1 质量通知 (qm_notification)

对标 SAP QMEL

```sql
CREATE TABLE qm_notification (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 通知信息
    notification_number VARCHAR(12) NOT NULL,  -- 通知单号
    notification_type VARCHAR(3) NOT NULL,     -- 通知类型
    -- Q1:客户投诉 Q2:内部问题 Q3:供应商问题

    -- 描述
    short_text      VARCHAR(100) NOT NULL,     -- 简短描述
    long_text       TEXT,                      -- 详细描述

    -- 优先级
    priority        VARCHAR(2),                -- 优先级
    -- 1:紧急 2:高 3:中 4:低

    -- 物料
    material_id     UUID REFERENCES mm_material(id),
    material_code   VARCHAR(18),
    batch_number    VARCHAR(10),

    -- 来源
    lot_id          UUID REFERENCES qm_inspection_lot(id),
    lot_number      VARCHAR(12),

    -- 相关方
    customer_id     UUID REFERENCES bp_business_partner(id),
    vendor_id       UUID REFERENCES bp_business_partner(id),

    -- 组织
    plant_id        UUID REFERENCES sys_plant(id),
    cost_center_id  UUID REFERENCES sys_cost_center(id),

    -- 日期
    reported_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date        DATE,                      -- 截止日期
    completion_date DATE,                      -- 完成日期

    -- 状态
    notification_status VARCHAR(2) DEFAULT '01', -- 01:创建 02:处理中 03:完成 04:关闭
    system_status   VARCHAR(10),               -- 系统状态 (OSNO/NOPN/QUTX)

    -- 责任人
    responsible_id  UUID REFERENCES hr_employee(id),

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, notification_number)
);
```

### 6.2 通知项目 (qm_notification_item)

```sql
CREATE TABLE qm_notification_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 通知
    notification_id UUID NOT NULL REFERENCES qm_notification(id) ON DELETE CASCADE,
    notification_number VARCHAR(12),

    -- 项目
    item_number     INTEGER NOT NULL,          -- 项目号

    -- 问题
    problem_type    VARCHAR(4),                -- 问题类型
    problem_text    VARCHAR(100),              -- 问题描述

    -- 对象
    object_type     VARCHAR(10),               -- 对象类型
    object_id       UUID,                      -- 对象ID

    -- 数量
    quantity        DECIMAL(13,3),
    unit            VARCHAR(3),

    -- 状态
    item_status     VARCHAR(2) DEFAULT '01',   -- 01:未处理 02:处理中 03:完成

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (notification_id, item_number)
);
```

### 6.3 通知任务 (qm_notification_task)

```sql
CREATE TABLE qm_notification_task (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 通知/项目
    notification_id UUID NOT NULL REFERENCES qm_notification(id),
    item_id         UUID REFERENCES qm_notification_item(id),

    -- 任务
    task_number     INTEGER NOT NULL,          -- 任务号
    task_type       VARCHAR(4),                -- 任务类型
    -- CAC:纠正措施 PAC:预防措施 IMM:立即措施

    -- 描述
    task_text       VARCHAR(100) NOT NULL,     -- 任务描述

    -- 责任
    responsible_id  UUID REFERENCES hr_employee(id),
    department      VARCHAR(50),

    -- 日期
    planned_start   DATE,
    planned_finish  DATE,
    actual_start    DATE,
    actual_finish   DATE,

    -- 状态
    task_status     VARCHAR(2) DEFAULT '01',   -- 01:未开始 02:进行中 03:完成 04:验证

    -- 验证
    verified_by     UUID,
    verified_at     TIMESTAMP,
    verification_result VARCHAR(2),            -- EF:有效 NE:无效

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (notification_id, task_number)
);
```

---

## 7. 视图定义

### 7.1 检验批状态视图

```sql
CREATE VIEW v_qm_lot_status AS
SELECT
    l.lot_number,
    l.lot_type,
    l.material_code,
    l.batch_number,
    l.lot_quantity,
    l.sample_quantity,
    l.inspected_quantity,
    l.accepted_quantity,
    l.rejected_quantity,
    l.unit,
    l.lot_status,
    l.decision_result,
    p.plant_code,
    p.plant_name,
    v.name AS vendor_name,
    l.created_date,
    l.inspection_due_date

FROM qm_inspection_lot l
LEFT JOIN sys_plant p ON p.id = l.plant_id
LEFT JOIN bp_business_partner v ON v.id = l.vendor_id
ORDER BY l.created_date DESC;
```

### 7.2 质量统计视图

```sql
CREATE VIEW v_qm_statistics AS
SELECT
    DATE_TRUNC('month', l.created_date) AS month,
    l.plant_id,
    p.plant_code,
    l.lot_type,

    COUNT(*) AS total_lots,
    SUM(CASE WHEN l.decision_result = 'AC' THEN 1 ELSE 0 END) AS accepted_lots,
    SUM(CASE WHEN l.decision_result = 'RJ' THEN 1 ELSE 0 END) AS rejected_lots,

    ROUND(
        SUM(CASE WHEN l.decision_result = 'AC' THEN 1 ELSE 0 END)::DECIMAL /
        NULLIF(COUNT(*), 0) * 100, 2
    ) AS acceptance_rate,

    SUM(l.lot_quantity) AS total_quantity,
    SUM(l.accepted_quantity) AS total_accepted,
    SUM(l.rejected_quantity) AS total_rejected

FROM qm_inspection_lot l
LEFT JOIN sys_plant p ON p.id = l.plant_id
WHERE l.lot_status = '03'
GROUP BY DATE_TRUNC('month', l.created_date), l.plant_id, p.plant_code, l.lot_type;
```

---

## 8. 索引策略

```sql
-- 检验批
CREATE INDEX idx_qm_lot_number ON qm_inspection_lot (tenant_id, lot_number);
CREATE INDEX idx_qm_lot_material ON qm_inspection_lot (material_id);
CREATE INDEX idx_qm_lot_plant ON qm_inspection_lot (plant_id);
CREATE INDEX idx_qm_lot_status ON qm_inspection_lot (lot_status);
CREATE INDEX idx_qm_lot_date ON qm_inspection_lot (created_date);
CREATE INDEX idx_qm_lot_source ON qm_inspection_lot (source_type, source_document);

-- 检验特性
CREATE INDEX idx_qm_lot_char_lot ON qm_inspection_char (lot_id);

-- 检验结果
CREATE INDEX idx_qm_result_lot ON qm_inspection_result (lot_id);
CREATE INDEX idx_qm_result_char ON qm_inspection_result (insp_char_id);

-- 使用决策
CREATE INDEX idx_qm_decision_lot ON qm_usage_decision (lot_id);

-- 质量通知
CREATE INDEX idx_qm_notif_number ON qm_notification (tenant_id, notification_number);
CREATE INDEX idx_qm_notif_type ON qm_notification (notification_type);
CREATE INDEX idx_qm_notif_status ON qm_notification (notification_status);
CREATE INDEX idx_qm_notif_date ON qm_notification (reported_date);
```

---

## 9. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
