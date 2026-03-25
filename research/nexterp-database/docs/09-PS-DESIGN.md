# PS 模块数据库设计

**模块**: Project System (项目系统)
**对标**: SAP ECC PS (CJ20N/CJ02)
**版本**: 1.0

---

## 1. 模块概述

### 1.1 业务范围

| 子模块 | 说明 | SAP 对标 |
|--------|------|----------|
| 项目定义 | 项目主数据 | CJ01/CJ02 |
| WBS | 工作分解结构 | CJ20N |
| 网络 | 网络活动/工序 | CN21/CN22 |
| 里程碑 | 项目里程碑 | CJ20N |
| 项目预算 | 预算管理 | CJ30/CJ32 |

### 1.2 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                     PS Module Architecture                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    项目层次结构                          │    │
│  │                                                          │    │
│  │  ┌─────────────┐                                        │    │
│  │  │ 项目定义    │ (Project Definition)                   │    │
│  │  │ ps_project  │                                        │    │
│  │  └─────────────┘                                        │    │
│  │         │                                               │    │
│  │         ▼                                               │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │          WBS (工作分解结构)                       │    │    │
│  │  │  ┌─────────┐  ┌─────────┐  ┌─────────┐          │    │    │
│  │  │  │ WBS 1   │  │ WBS 2   │  │ WBS 3   │          │    │    │
│  │  │  │ ├─1.1   │  │ ├─2.1   │  │ ├─3.1   │          │    │    │
│  │  │  │ └─1.2   │  │ └─2.2   │  │ └─3.2   │          │    │    │
│  │  │  └─────────┘  └─────────┘  └─────────┘          │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │         │                                               │    │
│  │         ▼                                               │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │          网络/活动 (Network/Activity)             │    │    │
│  │  │  ┌─────────┐  ┌─────────┐  ┌─────────┐          │    │    │
│  │  │  │ 活动1   │──►│ 活动2   │──►│ 活动3   │          │    │    │
│  │  │  └─────────┘  └─────────┘  └─────────┘          │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│        ┌─────────────────────┼─────────────────────┐            │
│        ▼                     ▼                     ▼            │
│  ┌──────────┐          ┌──────────┐          ┌──────────┐      │
│  │ 项目预算 │          │  里程碑  │          │ 项目凭证 │      │
│  │ps_budget │          │ps_milestone│        │ ps_doc   │      │
│  └──────────┘          └──────────┘          └──────────┘      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. 项目定义

### 2.1 项目主数据 (ps_project)

对标 SAP PROJ

```sql
CREATE TABLE ps_project (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 项目标识
    project_number  VARCHAR(24) NOT NULL,      -- 项目定义 (外部/内部)
    project_name    VARCHAR(100) NOT NULL,     -- 项目名称
    description     TEXT,                      -- 描述

    -- 项目类型
    project_type    VARCHAR(2) NOT NULL,       -- 项目类型
    -- 01:投资项目 02:客户项目 03:内部项目 04:研发项目 05:维护项目

    -- 项目档案
    project_profile VARCHAR(7),                -- 项目档案

    -- 组织
    company_id      UUID NOT NULL REFERENCES sys_company(id),
    business_area   VARCHAR(4),                -- 业务范围
    profit_center_id UUID REFERENCES sys_profit_center(id),
    plant_id        UUID REFERENCES sys_plant(id),
    cost_center_id  UUID REFERENCES sys_cost_center(id),

    -- 负责人
    project_manager UUID REFERENCES hr_employee(id),
    responsible_id  UUID REFERENCES hr_employee(id),

    -- 日期
    planned_start_date DATE,
    planned_finish_date DATE,
    actual_start_date DATE,
    actual_finish_date DATE,

    -- 客户/投资
    customer_id     UUID REFERENCES bp_business_partner(id),
    investment_program VARCHAR(24),             -- 投资程序
    investment_reason VARCHAR(4),              -- 投资原因

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 预算
    total_budget    DECIMAL(15,2) DEFAULT 0,   -- 总预算
    released_budget DECIMAL(15,2) DEFAULT 0,   -- 已释放预算
    committed_cost  DECIMAL(15,2) DEFAULT 0,   -- 承诺成本
    actual_cost     DECIMAL(15,2) DEFAULT 0,   -- 实际成本

    -- 状态
    project_status  VARCHAR(2) DEFAULT '01',   -- 01:创建 02:释放 03:完成 04:TECO 05:关闭
    system_status   VARCHAR(20),               -- 系统状态 (CRTD/REL/TECO/CLO)

    -- 进度
    progress_percent DECIMAL(5,2) DEFAULT 0,   -- 进度%

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, project_number)
);
```

### 2.2 项目类型配置 (ps_project_type)

```sql
CREATE TABLE ps_project_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    type_code       VARCHAR(2) NOT NULL,       -- 类型代码
    type_name       VARCHAR(100) NOT NULL,     -- 类型名称

    -- 项目档案
    project_profile VARCHAR(7),                -- 默认项目档案

    -- 计划配置
    plan_profile    VARCHAR(7),                -- 计划档案

    -- 预算配置
    budget_profile  VARCHAR(7),                -- 预算档案

    -- 结算规则
    settlement_rule VARCHAR(2),                -- 结算规则
    -- 01:自动结算 02:手动结算

    -- 状态管理
    status_profile  VARCHAR(7),                -- 状态档案

    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, type_code)
);
```

---

## 3. WBS 工作分解结构

### 3.1 WBS 元素 (ps_wbs_element)

对标 SAP PRPS

```sql
CREATE TABLE ps_wbs_element (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 项目关联
    project_id      UUID NOT NULL REFERENCES ps_project(id) ON DELETE CASCADE,
    project_number  VARCHAR(24),

    -- WBS 标识
    wbs_number      VARCHAR(24) NOT NULL,      -- WBS 元素编号
    wbs_name        VARCHAR(100) NOT NULL,     -- WBS 名称
    description     TEXT,

    -- 层级
    parent_id       UUID REFERENCES ps_wbs_element(id),
    level           INTEGER DEFAULT 1,
    path            VARCHAR(200),              -- 层级路径
    sort_order      INTEGER,                   -- 排序号

    -- WBS 类型
    wbs_type        VARCHAR(2),                -- WBS 类型
    -- 01:汇总 02:记账 03:计划 04:开票

    -- WBS 分类
    wbs_category    VARCHAR(2),                -- WBS 分类
    -- 01:标准 02:里程碑 03:开票 04:统计

    -- 组织
    company_id      UUID REFERENCES sys_company(id),
    profit_center_id UUID REFERENCES sys_profit_center(id),
    plant_id        UUID REFERENCES sys_plant(id),
    cost_center_id  UUID REFERENCES sys_cost_center(id),

    -- 日期
    planned_start_date DATE,
    planned_finish_date DATE,
    actual_start_date DATE,
    actual_finish_date DATE,

    -- 持续时间
    duration_days   INTEGER,                   -- 持续天数

    -- 负责人
    responsible_id  UUID REFERENCES hr_employee(id),

    -- 成本
    planned_cost    DECIMAL(15,2) DEFAULT 0,   -- 计划成本
    committed_cost  DECIMAL(15,2) DEFAULT 0,   -- 承诺成本
    actual_cost     DECIMAL(15,2) DEFAULT 0,   -- 实际成本
    currency_id     UUID REFERENCES core_currency(id),

    -- 预算
    budget          DECIMAL(15,2) DEFAULT 0,   -- 预算

    -- 收入 (开票WBS)
    planned_revenue DECIMAL(15,2) DEFAULT 0,   -- 计划收入
    actual_revenue  DECIMAL(15,2) DEFAULT 0,   -- 实际收入

    -- 进度
    progress_percent DECIMAL(5,2) DEFAULT 0,   -- 进度%
    progress_method VARCHAR(2),                -- 进度方法
    -- 01:手动 02:工作量 03:成本 04:里程碑

    -- 状态
    wbs_status      VARCHAR(2) DEFAULT '01',   -- 01:创建 02:释放 03:完成
    system_status   VARCHAR(20),

    -- 结算
    settlement_rule VARCHAR(2),
    settlement_receiver VARCHAR(24),           -- 结算接收方

    -- 里程碑
    has_milestone   BOOLEAN DEFAULT FALSE,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, wbs_number)
);
```

### 3.2 WBS 层级视图

```sql
CREATE VIEW v_ps_wbs_hierarchy AS
WITH RECURSIVE wbs_tree AS (
    -- 根节点
    SELECT
        id, project_id, wbs_number, wbs_name,
        parent_id, level, path, sort_order,
        ARRAY[wbs_number] AS wbs_path,
        ARRAY[wbs_name] AS name_path
    FROM ps_wbs_element
    WHERE parent_id IS NULL

    UNION ALL

    -- 递归
    SELECT
        w.id, w.project_id, w.wbs_number, w.wbs_name,
        w.parent_id, w.level, w.path, w.sort_order,
        t.wbs_path || w.wbs_number,
        t.name_path || w.wbs_name
    FROM ps_wbs_element w
    JOIN wbs_tree t ON w.parent_id = t.id
)
SELECT * FROM wbs_tree
ORDER BY wbs_path;
```

---

## 4. 网络与活动

### 4.1 网络 (ps_network)

对标 SAP AUFK (网络类型)

```sql
CREATE TABLE ps_network (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 网络信息
    network_number  VARCHAR(12) NOT NULL,      -- 网络号
    network_type    VARCHAR(4) NOT NULL,       -- 网络类型
    -- N01:标准网络 N02:维护网络 N03:工程网络

    -- 描述
    description     VARCHAR(100),              -- 描述

    -- 项目关联
    project_id      UUID REFERENCES ps_project(id),
    wbs_id          UUID REFERENCES ps_wbs_element(id), -- 负责WBS

    -- 组织
    company_id      UUID REFERENCES sys_company(id),
    plant_id        UUID REFERENCES sys_plant(id),
    work_center_id  UUID REFERENCES pp_work_center(id),

    -- 日期
    planned_start_date DATE,
    planned_finish_date DATE,
    actual_start_date DATE,
    actual_finish_date DATE,

    -- 状态
    network_status  VARCHAR(2) DEFAULT '01',   -- 01:创建 02:释放 03:完成

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, network_number)
);
```

### 4.2 网络活动 (ps_network_activity)

对标 SAP AFVC

```sql
CREATE TABLE ps_network_activity (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 网络关联
    network_id      UUID NOT NULL REFERENCES ps_network(id) ON DELETE CASCADE,
    network_number  VARCHAR(12),

    -- 活动信息
    activity_number VARCHAR(4) NOT NULL,       -- 活动号 (如 0010, 0020)
    activity_type   VARCHAR(2) NOT NULL,       -- 活动类型
    -- 01:内部处理 02:外部采购 03:服务 04:成本

    -- 描述
    description     VARCHAR(100) NOT NULL,

    -- WBS 关联
    wbs_id          UUID REFERENCES ps_wbs_element(id),

    -- 工作中心
    work_center_id  UUID REFERENCES pp_work_center(id),
    work_center_code VARCHAR(8),

    -- 日期
    planned_start_date DATE,
    planned_finish_date DATE,
    actual_start_date DATE,
    actual_finish_date DATE,
    duration_days   INTEGER,                   -- 持续天数

    -- 工作量
    planned_work    DECIMAL(10,2),             -- 计划工时
    actual_work     DECIMAL(10,2) DEFAULT 0,   -- 实际工时
    work_unit       VARCHAR(3) DEFAULT 'H',    -- 工时单位

    -- 成本
    planned_cost    DECIMAL(15,2) DEFAULT 0,
    actual_cost     DECIMAL(15,2) DEFAULT 0,

    -- 控制码
    control_key     VARCHAR(4),                -- 控制码
    -- PS01:内部处理 PS02:外部处理 PS03:服务采购

    -- 状态
    activity_status VARCHAR(2) DEFAULT '01',   -- 01:未开始 02:处理中 03:完成

    -- 确认
    confirmation_count INTEGER DEFAULT 0,      -- 确认次数

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (network_id, activity_number)
);
```

### 4.3 活动关系 (ps_activity_relation)

对标 SAP AFAB

```sql
CREATE TABLE ps_activity_relation (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 网络
    network_id      UUID NOT NULL REFERENCES ps_network(id),

    -- 前驱活动
    predecessor_id  UUID NOT NULL REFERENCES ps_network_activity(id),
    predecessor_number VARCHAR(4),

    -- 后继活动
    successor_id    UUID NOT NULL REFERENCES ps_network_activity(id),
    successor_number VARCHAR(4),

    -- 关系类型
    relation_type   VARCHAR(2) NOT NULL,       -- 关系类型
    -- FS:完成-开始 SS:开始-开始 FF:完成-完成 SF:开始-完成

    -- 时间间隔
    lag_days        INTEGER DEFAULT 0,         -- 间隔天数

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (network_id, predecessor_id, successor_id)
);
```

---

## 5. 里程碑

### 5.1 里程碑 (ps_milestone)

对标 SAP MILESTONE

```sql
CREATE TABLE ps_milestone (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- WBS/活动关联
    wbs_id          UUID REFERENCES ps_wbs_element(id),
    activity_id     UUID REFERENCES ps_network_activity(id),
    network_id      UUID REFERENCES ps_network(id),

    -- 里程碑信息
    milestone_number VARCHAR(4) NOT NULL,      -- 里程碑号
    milestone_name  VARCHAR(100) NOT NULL,     -- 里程碑名称
    description     TEXT,

    -- 日期
    planned_date    DATE NOT NULL,             -- 计划日期
    actual_date     DATE,                      -- 实际日期

    -- 进度
    milestone_percent DECIMAL(5,2),            -- 里程碑进度%

    -- 使用
    usage_type      VARCHAR(2),                -- 用途
    -- 01:进度 02:开票 03:触发 04:结算

    -- 开票相关
    billing_rule    VARCHAR(2),                -- 开票规则
    billing_percent DECIMAL(5,2),              -- 开票比例%
    billing_value   DECIMAL(15,2),             -- 开票金额

    -- 状态
    is_completed    BOOLEAN DEFAULT FALSE,
    completed_at    TIMESTAMP,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, COALESCE(wbs_id::TEXT, activity_id::TEXT), milestone_number)
);
```

---

## 6. 项目预算

### 6.1 预算头 (ps_budget_hdr)

```sql
CREATE TABLE ps_budget_hdr (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 项目
    project_id      UUID NOT NULL REFERENCES ps_project(id),
    project_number  VARCHAR(24),

    -- 预算类型
    budget_type     VARCHAR(2) NOT NULL,       -- 预算类型
    -- 01:原始预算 02:补充预算 03:预算转移

    -- 预算年度
    fiscal_year     INTEGER NOT NULL,

    -- 金额
    total_budget    DECIMAL(15,2) NOT NULL,    -- 总预算
    released_budget DECIMAL(15,2) DEFAULT 0,   -- 已释放预算
    used_budget     DECIMAL(15,2) DEFAULT 0,   -- 已使用预算

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 状态
    budget_status   VARCHAR(2) DEFAULT '01',   -- 01:草稿 02:已审批 03:已释放

    -- 审批
    approved_by     UUID,
    approved_at     TIMESTAMP,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (project_id, fiscal_year, budget_type)
);
```

### 6.2 预算明细 (ps_budget_item)

```sql
CREATE TABLE ps_budget_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 预算头
    budget_id       UUID NOT NULL REFERENCES ps_budget_hdr(id) ON DELETE CASCADE,

    -- WBS
    wbs_id          UUID REFERENCES ps_wbs_element(id),
    wbs_number      VARCHAR(24),

    -- 成本要素
    cost_element    VARCHAR(10),               -- 成本要素

    -- 金额 (按期间)
    period_01       DECIMAL(15,2) DEFAULT 0,
    period_02       DECIMAL(15,2) DEFAULT 0,
    period_03       DECIMAL(15,2) DEFAULT 0,
    period_04       DECIMAL(15,2) DEFAULT 0,
    period_05       DECIMAL(15,2) DEFAULT 0,
    period_06       DECIMAL(15,2) DEFAULT 0,
    period_07       DECIMAL(15,2) DEFAULT 0,
    period_08       DECIMAL(15,2) DEFAULT 0,
    period_09       DECIMAL(15,2) DEFAULT 0,
    period_10       DECIMAL(15,2) DEFAULT 0,
    period_11       DECIMAL(15,2) DEFAULT 0,
    period_12       DECIMAL(15,2) DEFAULT 0,

    -- 年度总计
    total_amount    DECIMAL(15,2) DEFAULT 0,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 6.3 预算可用性控制 (ps_availability_control)

```sql
CREATE TABLE ps_availability_control (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 项目
    project_id      UUID NOT NULL REFERENCES ps_project(id),

    -- 控制参数
    tolerance_limit_warning DECIMAL(5,2),      -- 警告容忍度%
    tolerance_limit_error DECIMAL(5,2),        -- 错误容忍度%

    -- 控制范围
    control_scope   VARCHAR(2),                -- 控制范围
    -- 01:整体 02:年度 03:WBS

    -- 操作
    action_type     VARCHAR(2),                -- 超预算操作
    -- 01:警告 02:错误 03:阻止

    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (project_id)
);
```

---

## 7. 项目凭证

### 7.1 项目凭证 (ps_document)

```sql
CREATE TABLE ps_document (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 凭证信息
    document_number VARCHAR(12) NOT NULL,
    document_date   DATE NOT NULL DEFAULT CURRENT_DATE,
    posting_date    DATE NOT NULL DEFAULT CURRENT_DATE,

    -- 项目关联
    project_id      UUID NOT NULL REFERENCES ps_project(id),
    wbs_id          UUID REFERENCES ps_wbs_element(id),
    network_id      UUID REFERENCES ps_network(id),
    activity_id     UUID REFERENCES ps_network_activity(id),

    -- 凭证类型
    document_type   VARCHAR(4) NOT NULL,       -- 凭证类型

    -- 成本
    amount          DECIMAL(15,2) NOT NULL,    -- 金额
    currency_id     UUID REFERENCES core_currency(id),

    -- 成本要素
    cost_element    VARCHAR(10),

    -- 描述
    description     VARCHAR(100),

    -- 来源
    source_type     VARCHAR(2),                -- 来源类型
    -- FI:财务 MM:物料 SD:销售 PP:生产
    source_document VARCHAR(20),
    source_id       UUID,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, document_number)
);
```

---

## 8. 活动确认

对标 SAP CAT2/CN25

### 8.1 活动确认 (ps_activity_confirmation)

```sql
CREATE TABLE ps_activity_confirmation (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 确认信息
    confirmation_number VARCHAR(12) NOT NULL,     -- 确认编号
    confirmation_date DATE NOT NULL DEFAULT CURRENT_DATE,

    -- 网络活动关联
    network_id      UUID NOT NULL REFERENCES ps_network(id),
    activity_id     UUID NOT NULL REFERENCES ps_network_activity(id),
    activity_number VARCHAR(4),

    -- 确认类型
    confirmation_type VARCHAR(2) NOT NULL,        -- 确认类型
    -- 01:工时确认 02:完成确认 03:部分确认 04:最终确认

    -- 工时
    actual_work     DECIMAL(10,2),                -- 实际工时
    remaining_work  DECIMAL(10,2),                -- 剩余工时
    work_unit       VARCHAR(3) DEFAULT 'H',       -- 工时单位

    -- 进度
    progress_percent DECIMAL(5,2),                -- 完成百分比

    -- 日期
    actual_start    DATE,                         -- 实际开始
    actual_finish   DATE,                         -- 实际完成

    -- 最终确认
    final_confirmation BOOLEAN DEFAULT FALSE,

    -- 成本
    actual_cost     DECIMAL(15,2),                -- 实际成本

    -- 确认人
    confirmed_by    UUID REFERENCES hr_employee(id),

    -- 备注
    remark          TEXT,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, confirmation_number)
);
```

---

## 9. 项目结算

对标 SAP COBR/CJ88

### 9.1 结算规则 (ps_settlement_rule)

```sql
CREATE TABLE ps_settlement_rule (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- WBS关联
    wbs_id          UUID NOT NULL REFERENCES ps_wbs_element(id),
    wbs_number      VARCHAR(24),

    -- 规则信息
    rule_number     VARCHAR(3) NOT NULL,          -- 规则号
    settlement_type VARCHAR(2) NOT NULL,          -- 结算类型
    -- 01:自动结算 02:手动结算 03:按比例 04:100%

    -- 接收方
    receiver_type   VARCHAR(2) NOT NULL,          -- 接收方类型
    -- 01:成本中心 02:订单 03:网络 04:WBS 05:资产 06:销售订单 07:利润中心 08:G/L科目
    receiver_id     UUID,                         -- 接收方ID
    receiver_code   VARCHAR(24),                  -- 接收方编码

    -- 比例
    settlement_percent DECIMAL(5,2) DEFAULT 100,  -- 结算比例%

    -- 有效期
    valid_from      DATE,
    valid_to        DATE DEFAULT '9999-12-31',

    -- 状态
    is_active       BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (wbs_id, rule_number)
);
```

### 9.2 结算凭证 (ps_settlement_doc)

```sql
CREATE TABLE ps_settlement_doc (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 凭证信息
    settlement_number VARCHAR(12) NOT NULL,       -- 结算凭证号
    settlement_date DATE NOT NULL DEFAULT CURRENT_DATE,
    fiscal_year     INTEGER NOT NULL,
    fiscal_period   INTEGER NOT NULL,

    -- 项目关联
    project_id      UUID NOT NULL REFERENCES ps_project(id),
    wbs_id          UUID REFERENCES ps_wbs_element(id),

    -- 金额
    settlement_amount DECIMAL(15,2) NOT NULL,     -- 结算金额
    currency_id     UUID REFERENCES core_currency(id),

    -- 接收方
    rule_id         UUID REFERENCES ps_settlement_rule(id),
    receiver_type   VARCHAR(2),
    receiver_id     UUID,
    receiver_code   VARCHAR(24),

    -- FI凭证
    fi_document_id  UUID,                         -- 关联FI凭证
    fi_document_number VARCHAR(10),

    -- 状态
    settlement_status VARCHAR(2) DEFAULT '01',    -- 01:已创建 02:已过账 03:已冲销

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    posted_at       TIMESTAMP,
    posted_by       UUID,

    UNIQUE (tenant_id, settlement_number, fiscal_year)
);
```

---

## 10. 视图定义

### 10.1 项目概览视图

```sql
CREATE VIEW v_ps_project_overview AS
SELECT
    p.project_number,
    p.project_name,
    p.project_type,
    pt.type_name AS project_type_name,
    p.planned_start_date,
    p.planned_finish_date,
    p.actual_start_date,
    p.actual_finish_date,
    p.total_budget,
    p.released_budget,
    p.committed_cost,
    p.actual_cost,
    p.progress_percent,
    p.project_status,
    emp.full_name AS project_manager,
    c.company_code,
    c.company_name

FROM ps_project p
LEFT JOIN ps_project_type pt ON pt.type_code = p.project_type
LEFT JOIN sys_company c ON c.id = p.company_id
LEFT JOIN hr_employee emp ON emp.id = p.project_manager
ORDER BY p.project_number;
```

### 10.2 WBS 成本汇总视图

```sql
CREATE VIEW v_ps_wbs_cost_summary AS
SELECT
    w.project_id,
    p.project_number,
    p.project_name,
    w.wbs_number,
    w.wbs_name,
    w.level,
    w.planned_cost,
    w.committed_cost,
    w.actual_cost,
    w.budget,
    w.budget - w.actual_cost AS budget_variance,
    CASE WHEN w.budget > 0
         THEN ROUND((w.actual_cost / w.budget) * 100, 2)
         ELSE 0 END AS cost_percent,
    w.progress_percent,
    w.responsible_id,
    emp.full_name AS responsible_name

FROM ps_wbs_element w
JOIN ps_project p ON p.id = w.project_id
LEFT JOIN hr_employee emp ON emp.id = w.responsible_id
ORDER BY w.wbs_number;
```

---

## 11. 存储过程

### 11.1 计算项目进度

```sql
CREATE OR REPLACE FUNCTION ps_calculate_progress(
    p_project_id UUID
) RETURNS DECIMAL AS $$
DECLARE
    v_total_weight DECIMAL(15,2) := 0;
    v_total_progress DECIMAL(15,2) := 0;
    v_wbs RECORD;
BEGIN
    -- 遍历所有记账WBS
    FOR v_wbs IN
        SELECT id, planned_cost, progress_percent
        FROM ps_wbs_element
        WHERE project_id = p_project_id
          AND wbs_type = '02'  -- 记账WBS
    LOOP
        v_total_weight := v_total_weight + COALESCE(v_wbs.planned_cost, 0);
        v_total_progress := v_total_progress +
            (COALESCE(v_wbs.planned_cost, 0) * COALESCE(v_wbs.progress_percent, 0));
    END LOOP;

    IF v_total_weight > 0 THEN
        RETURN ROUND(v_total_progress / v_total_weight, 2);
    ELSE
        RETURN 0;
    END IF;
END;
$$ LANGUAGE plpgsql;
```

### 11.2 项目完成确认

```sql
CREATE OR REPLACE FUNCTION ps_complete_project(
    p_project_id UUID,
    p_user_id UUID
) RETURNS BOOLEAN AS $$
DECLARE
    v_project RECORD;
BEGIN
    -- 获取项目信息
    SELECT * INTO v_project
    FROM ps_project WHERE id = p_project_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION '项目不存在';
    END IF;

    -- 检查是否可以完成
    IF EXISTS (
        SELECT 1 FROM ps_wbs_element
        WHERE project_id = p_project_id
          AND wbs_status NOT IN ('03', '04')
    ) THEN
        RAISE EXCEPTION '存在未完成的WBS元素';
    END IF;

    -- 更新项目状态
    UPDATE ps_project
    SET project_status = '03',
        actual_finish_date = COALESCE(actual_finish_date, CURRENT_DATE),
        progress_percent = 100,
        updated_at = CURRENT_TIMESTAMP,
        updated_by = p_user_id
    WHERE id = p_project_id;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
```

---

## 12. 索引策略

```sql
-- 项目
CREATE INDEX idx_ps_project_number ON ps_project (tenant_id, project_number);
CREATE INDEX idx_ps_project_type ON ps_project (project_type);
CREATE INDEX idx_ps_project_status ON ps_project (project_status);
CREATE INDEX idx_ps_project_dates ON ps_project (planned_start_date, planned_finish_date);
CREATE INDEX idx_ps_project_company ON ps_project (company_id);

-- WBS
CREATE INDEX idx_ps_wbs_project ON ps_wbs_element (project_id);
CREATE INDEX idx_ps_wbs_parent ON ps_wbs_element (parent_id);
CREATE INDEX idx_ps_wbs_number ON ps_wbs_element (tenant_id, wbs_number);

-- 网络
CREATE INDEX idx_ps_network_project ON ps_network (project_id);
CREATE INDEX idx_ps_network_number ON ps_network (tenant_id, network_number);

-- 活动
CREATE INDEX idx_ps_activity_network ON ps_network_activity (network_id);
CREATE INDEX idx_ps_activity_wbs ON ps_network_activity (wbs_id);

-- 活动确认
CREATE INDEX idx_ps_conf_network ON ps_activity_confirmation (network_id);
CREATE INDEX idx_ps_conf_activity ON ps_activity_confirmation (activity_id);
CREATE INDEX idx_ps_conf_date ON ps_activity_confirmation (confirmation_date);

-- 预算
CREATE INDEX idx_ps_budget_project ON ps_budget_hdr (project_id, fiscal_year);
CREATE INDEX idx_ps_budget_item_wbs ON ps_budget_item (wbs_id);

-- 里程碑
CREATE INDEX idx_ps_milestone_wbs ON ps_milestone (wbs_id);
CREATE INDEX idx_ps_milestone_activity ON ps_milestone (activity_id);

-- 结算
CREATE INDEX idx_ps_settle_rule_wbs ON ps_settlement_rule (wbs_id);
CREATE INDEX idx_ps_settle_doc_project ON ps_settlement_doc (project_id);
CREATE INDEX idx_ps_settle_doc_wbs ON ps_settlement_doc (wbs_id);
CREATE INDEX idx_ps_settle_doc_date ON ps_settlement_doc (settlement_date);

-- 凭证
CREATE INDEX idx_ps_doc_project ON ps_document (project_id);
CREATE INDEX idx_ps_doc_wbs ON ps_document (wbs_id);
CREATE INDEX idx_ps_doc_date ON ps_document (posting_date);
```

---

## 13. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
| 1.1 | 2026-03-14 | 补充活动确认、结算规则、结算凭证表 |
