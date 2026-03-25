# HR 模块数据库设计

**模块**: Human Resources (人力资源)
**对标**: SAP ECC HCM / S/4HANA Human Experience (HXM)
**版本**: 2.0
**更新日期**: 2026-03-14

---

## 1. 模块概述

### 1.1 SAP HCM 架构对标

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    NextERP HR Module - 对标 SAP HCM                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        OM 组织管理 (HRP1000/HRP1001)                  │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 组织单元 │  │   职位   │  │   职务   │  │   任务   │            │   │
│  │  │    O     │  │    S     │  │    C     │  │    T     │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  │                              │                                      │   │
│  │                    ┌─────────▼─────────┐                           │   │
│  │                    │  hr_relationship  │                           │   │
│  │                    │   (HRP1001风格)    │                           │   │
│  │                    └───────────────────┘                           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      PA 人事管理 (PA0000-PA9999)                     │   │
│  │                                                                      │   │
│  │   IT0000    IT0001    IT0002    IT0006    IT0008    IT0009          │   │
│  │  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐             │   │
│  │  │操作 │  │组织 │  │个人 │  │地址 │  │工资 │  │银行 │             │   │
│  │  └─────┘  └─────┘  └─────┘  └─────┘  └─────┘  └─────┘             │   │
│  │                                                                      │   │
│  │   IT0021   IT0022   IT0105   IT0588   IT0591   IT0406              │   │
│  │  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐  ┌─────┐             │   │
│  │  │家庭 │  │教育 │  │通讯 │  │公积金│ │社保 │  │税务 │             │   │
│  │  └─────┘  └─────┘  └─────┘  └─────┘  └─────┘  └─────┘             │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      PT 时间管理 (PA2000-PA2999)                     │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │IT2001    │  │IT2002    │  │IT2005    │  │IT0007    │            │   │
│  │  │缺勤记录  │  │出勤记录  │  │加班记录  │  │排班计划  │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      PY 薪酬管理 (HRPY_RT)                           │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │薪酬结果  │  │工资类型  │  │个税计算  │  │社保公积金│            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      RC 招聘管理 (PB40)                              │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │招聘需求  │  │候选人    │  │面试记录  │  │录用管理  │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      TM 培训管理 (PE)                                │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │课程目录  │  │培训班    │  │培训记录  │  │资格认证  │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      PM 绩效管理 (PA0380-PA0382)                     │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │绩效目标  │  │考核评估  │  │考核结果  │  │绩效历史  │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      SP 继任计划 (PP)                                │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │人才池    │  │继任者    │  │发展计划  │  │人才盘点  │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 业务范围与 SAP 对照表

| 子模块 | 说明 | SAP 表 | NextERP 表 |
|--------|------|--------|------------|
| **OM** 组织管理 | 组织架构、职位、职务、关系 | HRP1000, HRP1001 | hr_org_object, hr_relationship |
| **PA** 人事管理 | 员工主数据、个人信息 | PA0000-PA9999 | hr_employee, hr_it00xx_* |
| **PT** 时间管理 | 考勤、请假、加班、排班 | PA2001-PA2006 | hr_it200x_* |
| **PY** 薪酬管理 | 工资计算、社保、个税 | HRPY_RT, T5** | hr_payroll_*, hr_tax_* |
| **RC** 招聘管理 | 招聘、候选人、面试 | PB4000-PB4010 | hr_recruitment_* |
| **TM** 培训管理 | 课程、培训班、记录 | HRP1000(T), PE** | hr_training_* |
| **PM** 绩效管理 | 目标、考核、评估 | PA0380-PA0382 | hr_performance_* |
| **SP** 继任计划 | 人才池、继任者 | HRP1000(Q), PP** | hr_succession_* |

### 1.3 设计原则

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    NextERP HR 设计原则 (对标 SAP InfoType)                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. 时间有效性 (Time Validity)                                              │
│     ┌─────────────────────────────────────────────────────────────┐        │
│     │  valid_from ──────────────────────► valid_to                │        │
│     │  [2024-01-01 ─────────────────────── 9999-12-31]            │        │
│     │                                                             │        │
│     │  历史追溯: 保留所有历史版本                                   │        │
│     │  预测: 支持未来生效的记录                                     │        │
│     └─────────────────────────────────────────────────────────────┘        │
│                                                                             │
│  2. 时间约束 (Time Constraints) - SAP 风格                                  │
│     ┌─────────────────────────────────────────────────────────────┐        │
│     │  T1: 必须无间隙、无重叠 (如 IT0001 组织分配)                   │        │
│     │  T2: 可有间隙、不可重叠 (如 IT0008 基本工资)                   │        │
│     │  T3: 可重叠 (如 IT2001 缺勤记录)                              │        │
│     └─────────────────────────────────────────────────────────────┘        │
│                                                                             │
│  3. InfoType 架构                                                           │
│     ┌─────────────────────────────────────────────────────────────┐        │
│     │  4位编号: IT0001 = 组织分配, IT0002 = 个人数据               │        │
│     │  子类型:  subtype 用于区分同一 InfoType 的不同类型            │        │
│     │  对象ID:  employee_id 作为主键组成部分                        │        │
│     └─────────────────────────────────────────────────────────────┘        │
│                                                                             │
│  4. 多租户隔离                                                              │
│     ┌─────────────────────────────────────────────────────────────┐        │
│     │  tenant_id + RLS 行级安全策略                                 │        │
│     │  独立编号范围、独立薪酬体系                                    │        │
│     └─────────────────────────────────────────────────────────────┘        │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 组织管理 (OM) - 增强

### 2.1 OM 对象类型枚举

对标 SAP OTYPE (Object Type)

```sql
-- OM 对象类型 (SAP OTYPE)
CREATE TYPE hr_om_object_type AS ENUM (
    'O',  -- Organizational Unit  组织单元
    'S',  -- Position             职位
    'C',  -- Job                  职务
    'T',  -- Task                 任务
    'K',  -- Cost Center          成本中心
    'Q',  -- Qualification        资格
    'US', -- User                 用户
    'P'   -- Person               人员
);

-- OM 关系类型 (SAP RELAT)
CREATE TYPE hr_om_relation_type AS ENUM (
    '002', -- belongs to           隶属于
    '003', -- includes             包含
    '004', -- is holder of         担任
    '005', -- is subordinate       下级
    '007', -- describes            描述
    '008', -- holder of            持有者
    '009', -- line supervisor      直线汇报
    '010', -- cost center assign   成本中心分配
    '011', -- spec. of position    职位专指
    '012', -- has task             拥有任务
    '013', -- person to position   人员到职位
    '014', -- subordinate org      下级组织
    '015', -- org to cost center   组织到成本中心
    '020', -- matrix supervisor    矩阵汇报
    '030', -- has qualification    拥有资格
    '031', -- requires qualif.     需要资格
    '040', -- successor            继任者
    '041', -- participated in      参与了
    '042', -- reference position   参考职位
    '045', -- substitute           代理人
    '050', -- team leader          团队领导
    'A/B'  -- A/B relationship     A/B关系
);

-- OM 规划状态 (SAP PLVAR)
CREATE TYPE hr_om_plan_version AS ENUM (
    '01',  -- Current Plan         当前计划
    '02',  -- Organizational Plan  组织计划
    '03',  -- Test Plan            测试计划
    '99'   -- Archive              归档
);
```

### 2.2 通用 OM 对象表 (HRP1000 风格)

对标 SAP HRP1000 表结构

```sql
-- OM 对象主表 (对标 SAP HRP1000)
CREATE TABLE hr_om_object (
    -- 主键
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- SAP 风格对象标识
    plan_version    hr_om_plan_version DEFAULT '01',
    object_type     hr_om_object_type NOT NULL,
    object_id       VARCHAR(8) NOT NULL,     -- SAP Objid (8位)

    -- 多语言支持 (对标 SAP HRP1000 语言相关字段)
    language_iso    VARCHAR(2) DEFAULT 'zh',
    name            VARCHAR(100) NOT NULL,   -- STEXT 短文本
    long_text       TEXT,                    -- LTEXT 长文本

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    -- 时间有效性 (InfoType 风格)
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,
    updated_by      UUID,
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, plan_version, object_type, object_id)
);

-- 索引
CREATE INDEX idx_hr_om_object_type ON hr_om_object (tenant_id, object_type);
CREATE INDEX idx_hr_om_object_validity ON hr_om_object (tenant_id, valid_from, valid_to);
CREATE INDEX idx_hr_om_object_name ON hr_om_object (tenant_id, name);
```

### 2.3 组织单元详情表

```sql
-- 组织单元详情 (对标 SAP HRP1000 + 自定义扩展)
CREATE TABLE hr_om_org_unit_detail (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    object_id       UUID NOT NULL REFERENCES hr_om_object(id) ON DELETE CASCADE,

    -- 组织编码 (业务主键)
    org_code        VARCHAR(12) NOT NULL,

    -- 层级关系
    parent_object_id UUID REFERENCES hr_om_object(id),

    -- 组织属性
    org_category    VARCHAR(2),                -- 组织分类
    org_level       INTEGER DEFAULT 1,         -- 层级深度
    path            VARCHAR(500),              -- 层级路径 /ROOT/BR/DE

    -- 关联
    company_id      UUID REFERENCES sys_company(id),
    cost_center_id  UUID REFERENCES sys_cost_center(id),

    -- 编制
    headcount       INTEGER DEFAULT 0,         -- 当前人数
    max_headcount   INTEGER,                   -- 编制上限

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, org_code)
);

-- 索引
CREATE INDEX idx_hr_org_detail_parent ON hr_om_org_unit_detail (parent_object_id);
CREATE INDEX idx_hr_org_detail_company ON hr_om_org_unit_detail (company_id);
```

### 2.4 职务详情表

```sql
-- 职务详情 (对标 SAP HRP1000 OTYPE='C')
CREATE TABLE hr_om_job_detail (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    object_id       UUID NOT NULL REFERENCES hr_om_object(id) ON DELETE CASCADE,

    -- 职务编码
    job_code        VARCHAR(8) NOT NULL,

    -- 职务分类 (对标 SAP Job Family)
    job_family      VARCHAR(4),                -- 职务族
    job_function    VARCHAR(4),                -- 职能
    job_grade       VARCHAR(4),                -- 职级
    job_level       INTEGER,                   -- 职等

    -- 任职要求
    description     TEXT,                      -- 职责描述
    requirements    TEXT,                      -- 任职要求
    qualifications  JSONB,                     -- 所需资格

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, job_code)
);
```

### 2.5 职位详情表

```sql
-- 职位详情 (对标 SAP HRP1000 OTYPE='S')
CREATE TABLE hr_om_position_detail (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    object_id       UUID NOT NULL REFERENCES hr_om_object(id) ON DELETE CASCADE,

    -- 职位编码
    position_code   VARCHAR(8) NOT NULL,

    -- 关联
    job_object_id   UUID NOT NULL REFERENCES hr_om_object(id),  -- 关联职务
    org_object_id   UUID NOT NULL REFERENCES hr_om_object(id),  -- 所属组织

    -- 任职者 (当前)
    holder_object_id UUID REFERENCES hr_om_object(id),          -- 持有人
    holder_name     VARCHAR(80),

    -- 成本中心 (可覆盖)
    cost_center_id  UUID REFERENCES sys_cost_center(id),

    -- 编制
    headcount       INTEGER DEFAULT 1,         -- 编制数
    current_count   INTEGER DEFAULT 0,         -- 当前人数

    -- 职位状态
    position_status VARCHAR(2) DEFAULT 'VA',   -- VA:空缺 FI:已填充 FR:冻结 AB:废除

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, position_code)
);

-- 索引
CREATE INDEX idx_hr_position_job ON hr_om_position_detail (job_object_id);
CREATE INDEX idx_hr_position_org ON hr_om_position_detail (org_object_id);
CREATE INDEX idx_hr_position_holder ON hr_om_position_detail (holder_object_id);
```

### 2.6 通用关系表 (HRP1001 风格) - 核心增强

对标 SAP HRP1001 表结构，支持任意对象间的关系

```sql
-- OM 对象关系表 (对标 SAP HRP1001)
CREATE TABLE hr_om_relationship (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 规划版本
    plan_version    hr_om_plan_version DEFAULT '01',

    -- 关系类型
    relation_type   hr_om_relation_type NOT NULL,

    -- 前向对象 (A方向)
    object_type_a   hr_om_object_type NOT NULL,
    object_id_a     UUID NOT NULL REFERENCES hr_om_object(id),

    -- 后向对象 (B方向)
    object_type_b   hr_om_object_type NOT NULL,
    object_id_b     UUID NOT NULL REFERENCES hr_om_object(id),

    -- 比例 (用于分摊)
    percentage      DECIMAL(5,2) DEFAULT 100.00,

    -- 优先级 (用于多汇报线)
    priority        INTEGER DEFAULT 1,

    -- 主标识 (是否主要关系)
    is_primary      BOOLEAN DEFAULT TRUE,

    -- 时间有效性
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    -- 唯一约束
    UNIQUE (tenant_id, plan_version, relation_type,
            object_type_a, object_id_a, object_type_b, object_id_b, valid_from)
);

-- 索引 (对标 SAP HRP1001 索引结构)
CREATE INDEX idx_hr_rel_a_object ON hr_om_relationship
    (tenant_id, object_type_a, object_id_a, valid_from, valid_to);
CREATE INDEX idx_hr_rel_b_object ON hr_om_relationship
    (tenant_id, object_type_b, object_id_b, valid_from, valid_to);
CREATE INDEX idx_hr_rel_type ON hr_om_relationship
    (tenant_id, relation_type, valid_from);
```

### 2.7 关系视图 - 快速查询

```sql
-- 直线汇报关系视图
CREATE VIEW v_hr_line_reports AS
SELECT
    r.id,
    r.tenant_id,
    -- 上级 (A方向)
    a.object_id AS manager_object_id,
    a.object_type AS manager_type,
    p_a.name AS manager_name,
    -- 下级 (B方向)
    b.object_id AS subordinate_object_id,
    b.object_type AS subordinate_type,
    p_b.name AS subordinate_name,
    -- 有效期
    r.valid_from,
    r.valid_to,
    r.is_primary
FROM hr_om_relationship r
JOIN hr_om_object a ON a.id = r.object_id_a
JOIN hr_om_object b ON b.id = r.object_id_b
LEFT JOIN hr_om_object p_a ON p_a.id = r.object_id_a
LEFT JOIN hr_om_object p_b ON p_b.id = r.object_id_b
WHERE r.relation_type IN ('002', '003', '009', '014')
  AND CURRENT_DATE BETWEEN r.valid_from AND r.valid_to
  AND a.status = 'ACTIVE' AND b.status = 'ACTIVE';

-- 职位-人员关系视图 (对标 SAP HRP1001 A/B 008/013)
CREATE VIEW v_hr_position_holder AS
SELECT
    r.id,
    r.tenant_id,
    pos.object_id AS position_id,
    pos.name AS position_name,
    ps.position_code,
    ps.position_status,
    per.object_id AS person_id,
    per.name AS person_name,
    e.employee_number,
    r.valid_from,
    r.valid_to,
    r.percentage,
    r.is_primary
FROM hr_om_relationship r
JOIN hr_om_object pos ON pos.id = r.object_id_a AND pos.object_type = 'S'
JOIN hr_om_object per ON per.id = r.object_id_b AND per.object_type = 'P'
JOIN hr_om_position_detail ps ON ps.object_id = pos.id
LEFT JOIN hr_employee e ON e.om_object_id = per.id
WHERE r.relation_type = '008'  -- holder of
  AND CURRENT_DATE BETWEEN r.valid_from AND r.valid_to
  AND pos.status = 'ACTIVE';
```

### 2.8 OM 组织架构完整视图

```sql
-- 组织树形结构视图 (对标 SAP HRP1000/HRP1001 递归查询)
CREATE VIEW v_hr_org_tree AS
WITH RECURSIVE org_hierarchy AS (
    -- 根节点 (顶级组织)
    SELECT
        o.id AS object_id,
        o.object_type,
        o.object_id AS org_code,
        o.name,
        od.parent_object_id,
        od.org_level,
        od.org_code,
        od.company_id,
        od.headcount,
        od.max_headcount,
        1 AS depth,
        ARRAY[o.name] AS path_names,
        ARRAY[o.id] AS path_ids
    FROM hr_om_object o
    JOIN hr_om_org_unit_detail od ON od.object_id = o.id
    WHERE od.parent_object_id IS NULL
      AND o.object_type = 'O'
      AND o.status = 'ACTIVE'
      AND CURRENT_DATE BETWEEN o.valid_from AND o.valid_to

    UNION ALL

    -- 递归子节点
    SELECT
        o.id,
        o.object_type,
        o.object_id AS org_code,
        o.name,
        od.parent_object_id,
        od.org_level,
        od.org_code,
        od.company_id,
        od.headcount,
        od.max_headcount,
        h.depth + 1,
        h.path_names || o.name,
        h.path_ids || o.id
    FROM hr_om_object o
    JOIN hr_om_org_unit_detail od ON od.object_id = o.id
    JOIN org_hierarchy h ON od.parent_object_id = h.object_id
    WHERE o.object_type = 'O'
      AND o.status = 'ACTIVE'
      AND CURRENT_DATE BETWEEN o.valid_from AND o.valid_to
)
SELECT * FROM org_hierarchy;

-- 职位-组织-职务完整视图
CREATE VIEW v_hr_position_full AS
SELECT
    pos_obj.id AS position_object_id,
    pos_obj.object_id AS position_code,
    pos_obj.name AS position_name,
    pos_d.position_status,
    pos_d.headcount,
    pos_d.current_count,

    -- 所属职务
    job_obj.id AS job_object_id,
    job_obj.name AS job_name,
    job_d.job_code,
    job_d.job_family,
    job_d.job_grade,

    -- 所属组织
    org_obj.id AS org_object_id,
    org_obj.name AS org_name,
    org_d.org_code,
    org_d.org_level,

    -- 当前任职者
    holder_obj.id AS holder_object_id,
    holder_obj.name AS holder_name,
    e.employee_number,

    -- 有效期
    pos_obj.valid_from,
    pos_obj.valid_to

FROM hr_om_object pos_obj
JOIN hr_om_position_detail pos_d ON pos_d.object_id = pos_obj.id
JOIN hr_om_object job_obj ON job_obj.id = pos_d.job_object_id
LEFT JOIN hr_om_job_detail job_d ON job_d.object_id = job_obj.id
JOIN hr_om_object org_obj ON org_obj.id = pos_d.org_object_id
LEFT JOIN hr_om_org_unit_detail org_d ON org_d.object_id = org_obj.id
LEFT JOIN hr_om_object holder_obj ON holder_obj.id = pos_d.holder_object_id
LEFT JOIN hr_employee e ON e.om_object_id = holder_obj.id

WHERE pos_obj.object_type = 'S'
  AND pos_obj.status = 'ACTIVE'
  AND CURRENT_DATE BETWEEN pos_obj.valid_from AND pos_obj.valid_to;
```

---

## 3. 人事管理 (PA) - 增强

### 3.0 员工-用户关联说明

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                     员工与系统用户的关联 (对标 SAP PA0105)                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  设计原则:                                                                   │
│  1. hr_employee 不直接引用 auth_user/sys_user (避免循环依赖)                │
│  2. 通过 employee_number 值匹配建立逻辑关联                                 │
│  3. sys_user.employee_number = hr_employee.employee_number                 │
│                                                                             │
│  ┌───────────────────┐              ┌───────────────────┐                  │
│  │ 00-CORE           │              │ 07-HR             │                  │
│  │                   │              │                   │                  │
│  │ sys_user          │              │ hr_employee       │                  │
│  │ ┌───────────────┐ │              │ ┌───────────────┐ │                  │
│  │ │employee_number│─┼──────────────┼─┤employee_number│ │                  │
│  │ │ = '10000001'  │ │  值相等      │ │ = '10000001'  │ │                  │
│  │ └───────────────┘ │  无外键      │ └───────────────┘ │                  │
│  │                   │              │                   │                  │
│  └───────────────────┘              └───────────────────┘                  │
│                                                                             │
│  查询员工对应的系统用户:                                                     │
│  SELECT e.*, u.id AS sys_user_id, u.display_name                           │
│  FROM hr_employee e                                                         │
│  LEFT JOIN sys_user u ON u.tenant_id = e.tenant_id                         │
│                      AND u.employee_number = e.employee_number;            │
│                                                                             │
│  业务场景:                                                                   │
│  • 一个员工可以有多个系统账号 (多租户、代理登录)                             │
│  • 外部用户可以没有员工记录 (user_type='EX')                                │
│  • 一线员工可以没有系统账号 (仅有 hr_employee 记录)                         │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.1 员工主数据表 (增强)

```sql
-- 员工主数据 (对标 SAP PA0001 + 扩展)
-- 注意: 不引用 auth_user/sys_user，通过 employee_number 逻辑关联
CREATE TABLE hr_employee (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 员工编号 (与 sys_user.employee_number 对应)
    employee_number VARCHAR(8) NOT NULL,

    -- OM 对象关联
    om_object_id    UUID REFERENCES hr_om_object(id),  -- 关联 OM 对象 (P类型)

    -- 状态 (对标 SAP IT0000)
    employee_status VARCHAR(2) DEFAULT 'AC',   -- AC:在职 IN:休假 TE:离职 RE:退休
    action_reason   VARCHAR(4),                -- 离职原因

    -- 基本信息 (冗余自 IT0002，便于快速查询)
    full_name       VARCHAR(80),
    gender          gender,
    birth_date      DATE,
    id_number       VARCHAR(20),

    -- 组织信息 (冗余自 IT0001)
    org_unit_id     UUID,
    position_id     UUID,
    job_id          UUID,
    cost_center_id  UUID,

    -- 联系方式
    email           VARCHAR(100),
    email_work      VARCHAR(100),
    phone           VARCHAR(50),
    mobile          VARCHAR(50),

    -- 入职信息
    hire_date       DATE NOT NULL,
    seniority       DECIMAL(5,1),              -- 司龄 (年)
    probation_end   DATE,                      -- 试用期结束
    original_hire   DATE,                      -- 最初入职日

    -- 离职信息
    termination_date DATE,
    termination_type VARCHAR(2),               -- 01:辞职 02:辞退 03:合同到期 04:退休

    status          general_status DEFAULT 'ACTIVE',

    -- 审计 (created_by 指向 sys_user.id，但无物理外键约束)
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,                      -- 逻辑关联 sys_user.id
    updated_by      UUID,                      -- 逻辑关联 sys_user.id
    version         INTEGER DEFAULT 0,

    UNIQUE (tenant_id, employee_number)
);

-- 索引
CREATE INDEX idx_hr_employee_status ON hr_employee (tenant_id, employee_status);
CREATE INDEX idx_hr_employee_org ON hr_employee (tenant_id, org_unit_id);
CREATE INDEX idx_hr_employee_position ON hr_employee (tenant_id, position_id);
CREATE INDEX idx_hr_employee_hire ON hr_employee (tenant_id, hire_date);
CREATE INDEX idx_hr_employee_om ON hr_employee (om_object_id);
CREATE INDEX idx_hr_employee_number ON hr_employee (tenant_id, employee_number);
```

### 3.2 IT0000 操作/状态

```sql
-- IT0000 操作/状态记录 (对标 SAP PA0000)
CREATE TABLE hr_it0000_actions (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '0000',
    subtype         VARCHAR(4) DEFAULT '',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 操作类型 (对标 SAP MASSN)
    action_type     VARCHAR(4) NOT NULL,       -- 01:招聘 02:离职 03:调岗 04:转正
    action_reason   VARCHAR(4),                -- 操作原因

    -- 状态变更
    employee_status VARCHAR(2) NOT NULL,       -- 操作后状态

    -- 备注
    remarks         TEXT,

    -- 审批
    approval_status approval_status DEFAULT 'APPROVED',
    approved_by     UUID,
    approved_at     TIMESTAMP,

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    PRIMARY KEY (employee_id, valid_from)
);

-- 操作类型枚举说明
COMMENT ON COLUMN hr_it0000_actions.action_type IS '
01 - 招聘入职 (Hiring)
02 - 离职 (Termination)
03 - 调动/调岗 (Transfer)
04 - 转正 (Probation Complete)
05 - 晋升 (Promotion)
06 - 降职 (Demotion)
07 - 停薪留职 (Leave Without Pay)
08 - 复职 (Reinstatement)
09 - 退休 (Retirement)
10 - 合同续签 (Contract Renewal)
';
```

### 3.3 IT0001 组织分配 (增强)

```sql
-- IT0001 组织分配 (对标 SAP PA0001)
CREATE TABLE hr_it0001_org_assignment (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '0001',
    subtype         VARCHAR(4) DEFAULT '',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 组织信息
    org_unit_id     UUID REFERENCES hr_om_object(id),
    org_unit_name   VARCHAR(100),
    position_id     UUID REFERENCES hr_om_object(id),
    position_name   VARCHAR(100),
    job_id          UUID REFERENCES hr_om_object(id),
    job_name        VARCHAR(100),

    -- 成本中心
    cost_center_id  UUID REFERENCES sys_cost_center(id),
    cost_center_name VARCHAR(100),

    -- 汇报关系
    manager_id      UUID REFERENCES hr_employee(id),
    manager_name    VARCHAR(80),

    -- 员工分类 (对标 SAP PERSG/PERSK)
    employee_group  VARCHAR(1),                -- 员工组 1:正式 2:合同 3:实习
    employee_subgroup VARCHAR(2),              -- 员工子组

    -- 公司范围 (对标 SAP WERKS/BTRTL)
    company_id      UUID REFERENCES sys_company(id),
    personnel_area  VARCHAR(4),                -- 人事范围
    personnel_subarea VARCHAR(4),              -- 人事子范围

    -- 审计
    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, valid_from)
);

-- 时间无重叠约束 (T1约束)
ALTER TABLE hr_it0001_org_assignment
ADD CONSTRAINT uk_hr_it0001_no_overlap
EXCLUDE USING GIST (
    employee_id WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);

-- 索引
CREATE INDEX idx_hr_it0001_org ON hr_it0001_org_assignment (org_unit_id);
CREATE INDEX idx_hr_it0001_position ON hr_it0001_org_assignment (position_id);
CREATE INDEX idx_hr_it0001_manager ON hr_it0001_org_assignment (manager_id);
CREATE INDEX idx_hr_it0001_validity ON hr_it0001_org_assignment
    (employee_id, valid_from DESC, valid_to ASC);
```

### 3.4 IT0002 个人数据

```sql
-- IT0002 个人数据 (对标 SAP PA0002)
CREATE TABLE hr_it0002_personal_data (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '0002',
    subtype         VARCHAR(4) DEFAULT '',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 姓名 (对标 SAP NACHN/VORNA)
    last_name       VARCHAR(40) NOT NULL,
    first_name      VARCHAR(40) NOT NULL,
    full_name       VARCHAR(80) GENERATED ALWAYS AS (
        first_name || ' ' || last_name
    ) STORED,

    -- 曾用名
    former_name     VARCHAR(80),

    -- 基本信息
    gender          gender NOT NULL,
    birth_date      DATE NOT NULL,
    nationality     VARCHAR(3),                -- 国籍 (ISO 3166-1)
    ethnicity       VARCHAR(3),                -- 民族
    native_language VARCHAR(3),                -- 母语

    -- 婚姻状况 (对标 SAP FAMST)
    marital_status  VARCHAR(1),                -- 1:未婚 2:已婚 3:丧偶 4:离婚
    marriage_date   DATE,

    -- 身份证
    id_type         VARCHAR(4) DEFAULT '01',   -- 01:身份证 02:护照 03:港澳通行证
    id_number       VARCHAR(20) NOT NULL,
    id_issue_date   DATE,
    id_issue_place  VARCHAR(100),
    id_expiry_date  DATE,

    -- 户籍 (中国本地化)
    native_place    VARCHAR(100),              -- 籍贯
    household_type  VARCHAR(1),                -- 1:城镇 2:农村
    household_address TEXT,

    -- 政治面貌
    political_status VARCHAR(2),               -- 01:中共党员 02:团员 03:民主党派 04:群众
    party_join_date DATE,

    -- 照片
    photo_url       VARCHAR(500),

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, valid_from)
);
```

### 3.5 IT0006 地址信息

```sql
-- IT0006 地址信息 (对标 SAP PA0006)
CREATE TABLE hr_it0006_address (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '0006',
    subtype         VARCHAR(4) NOT NULL,       -- 1:永久 2:临时 3:邮寄 4:紧急
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 地址信息
    country         VARCHAR(3),                -- 国家
    province        VARCHAR(50),               -- 省/州
    city            VARCHAR(50),               -- 城市
    district        VARCHAR(50),               -- 区/县
    street          VARCHAR(200),              -- 街道地址
    postal_code     VARCHAR(10),               -- 邮编

    -- 联系方式
    phone           VARCHAR(50),
    mobile          VARCHAR(50),
    email           VARCHAR(100),

    -- 紧急联系人 (subtype=4)
    contact_name    VARCHAR(80),
    contact_relation VARCHAR(20),
    contact_phone   VARCHAR(50),

    -- 是否默认
    is_default      BOOLEAN DEFAULT FALSE,

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, subtype, valid_from)
);

-- 索引
CREATE INDEX idx_hr_it0006_default ON hr_it0006_address (employee_id, is_default) WHERE is_default = TRUE;
```

### 3.6 IT0008 基本工资 (增强)

```sql
-- 工资类型定义表 (对标 SAP T512W)
CREATE TABLE hr_wage_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    code            VARCHAR(4) NOT NULL,       -- 工资类型编码
    name            VARCHAR(100) NOT NULL,

    -- 分类 (对标 SAP LGART)
    category        VARCHAR(2),                -- BA:基本 AL:津贴 BO:奖金 DE:扣款

    -- 属性
    is_taxable      BOOLEAN DEFAULT TRUE,
    is_pension_base BOOLEAN DEFAULT TRUE,
    is_medical_base BOOLEAN DEFAULT TRUE,
    is_fund_base    BOOLEAN DEFAULT TRUE,
    is_bonus        BOOLEAN DEFAULT FALSE,

    -- 计算方式
    calc_type       VARCHAR(2) DEFAULT 'MO',   -- MO:月 FI:固定 HO:小时 DA:日
    amount          DECIMAL(15,2),             -- 默认金额

    status          general_status DEFAULT 'ACTIVE',

    UNIQUE (tenant_id, code)
);

-- IT0008 基本工资 (对标 SAP PA0008)
CREATE TABLE hr_it0008_basic_pay (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '0008',
    subtype         VARCHAR(4) DEFAULT '',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 薪酬等级 (对标 SAP TRFAR/TRFGB/TRFGR)
    pay_type        VARCHAR(2),                -- 薪酬类型
    pay_area        VARCHAR(2),                -- 薪酬区域
    pay_grade       VARCHAR(4),                -- 薪等
    pay_level       VARCHAR(2),                -- 薪级

    -- 货币
    currency_id     UUID REFERENCES core_currency(id),

    -- 总额 (由明细汇总)
    total_amount    DECIMAL(15,2) DEFAULT 0,

    -- 年薪
    annual_salary   DECIMAL(15,2),

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version         INTEGER DEFAULT 0,

    PRIMARY KEY (employee_id, valid_from)
);

-- 时间无重叠约束 (T2约束 - 可有间隙)
ALTER TABLE hr_it0008_basic_pay
ADD CONSTRAINT uk_hr_it0008_no_overlap
EXCLUDE USING GIST (
    employee_id WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);

-- 工资项明细 (对标 SAP PA0008 工资项数组)
CREATE TABLE hr_it0008_wage_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    basic_pay_id    UUID NOT NULL,

    employee_id     UUID NOT NULL,             -- 冗余，便于查询

    wage_type_id    UUID NOT NULL REFERENCES hr_wage_type(id),
    wage_type_code  VARCHAR(4) NOT NULL,
    wage_type_name  VARCHAR(100),

    amount          DECIMAL(15,2) NOT NULL,
    currency_id     UUID,
    calc_type       VARCHAR(2) DEFAULT 'MO',

    -- 顺序
    seq_no          INTEGER DEFAULT 1,

    is_fixed        BOOLEAN DEFAULT TRUE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (basic_pay_id, wage_type_id)
);

-- 更新总工资的触发器
CREATE OR REPLACE FUNCTION hr_update_pay_total()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE hr_it0008_basic_pay
    SET total_amount = (
        SELECT COALESCE(SUM(amount), 0)
        FROM hr_it0008_wage_item
        WHERE basic_pay_id = COALESCE(NEW.basic_pay_id, OLD.basic_pay_id)
    )
    WHERE employee_id = COALESCE(NEW.employee_id, OLD.employee_id)
      AND valid_from = (
          SELECT valid_from FROM hr_it0008_basic_pay
          WHERE employee_id = COALESCE(NEW.employee_id, OLD.employee_id)
          LIMIT 1
      );
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_hr_wage_item_total
AFTER INSERT OR UPDATE OR DELETE ON hr_it0008_wage_item
FOR EACH ROW EXECUTE FUNCTION hr_update_pay_total();
```

### 3.7 IT0009 银行信息

```sql
-- IT0009 银行信息 (对标 SAP PA0009)
CREATE TABLE hr_it0009_bank_details (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '0009',
    subtype         VARCHAR(4) NOT NULL,       -- 0:主账户 1:其他账户
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 银行信息
    bank_code       VARCHAR(20) NOT NULL,      -- 银行编码
    bank_name       VARCHAR(100),
    branch_name     VARCHAR(100),              -- 支行名称

    -- 账户信息
    account_number  VARCHAR(30) NOT NULL,
    account_name    VARCHAR(80),               -- 户名 (默认员工姓名)
    account_type    VARCHAR(2) DEFAULT '01',   -- 01:借记 02:信用卡

    -- 用途
    payment_method  VARCHAR(2) DEFAULT 'TR',   -- TR:转账 CH:支票 CA:现金

    -- 是否主账户
    is_primary      BOOLEAN DEFAULT FALSE,

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, subtype, valid_from)
);

-- 索引
CREATE INDEX idx_hr_it0009_primary ON hr_it0009_bank_details (employee_id, is_primary) WHERE is_primary = TRUE;
```

### 3.8 IT0021 家庭成员

```sql
-- IT0021 家庭成员/受抚养人 (对标 SAP PA0021)
CREATE TABLE hr_it0021_family (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '0021',
    subtype         VARCHAR(4) NOT NULL,       -- 01:配偶 02:子女 03:父母 04:兄弟姐妹
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 家庭成员信息
    last_name       VARCHAR(40),
    first_name      VARCHAR(40),
    full_name       VARCHAR(80),

    gender          gender,
    birth_date      DATE,
    id_number       VARCHAR(20),

    -- 工作信息
    employer        VARCHAR(100),
    occupation      VARCHAR(50),

    -- 抚养关系
    is_dependent    BOOLEAN DEFAULT FALSE,
    dependent_start DATE,
    dependent_end   DATE,

    -- 联系方式
    phone           VARCHAR(50),

    -- 备注
    remarks         TEXT,

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, subtype, valid_from)
);
```

### 3.9 IT0022 教育背景

```sql
-- IT0022 教育背景 (对标 SAP PA0022)
CREATE TABLE hr_it0022_education (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '0022',
    subtype         VARCHAR(4) NOT NULL,       -- 01:高中 02:大专 03:本科 04:硕士 05:博士
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 学校信息
    school_name     VARCHAR(100) NOT NULL,
    school_country  VARCHAR(3),
    school_type     VARCHAR(2),                -- 01:普通 02:985 03:211 04:双一流

    -- 专业信息
    major           VARCHAR(100),
    degree          VARCHAR(4),                -- 学位

    -- 时间
    start_date      DATE,
    end_date        DATE,
    is_graduated    BOOLEAN DEFAULT TRUE,

    -- 成绩
    gpa             DECIMAL(3,2),

    -- 证书
    certificate_no  VARCHAR(50),

    -- 是否最高学历
    is_highest      BOOLEAN DEFAULT FALSE,

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, subtype, valid_from)
);

-- 索引
CREATE INDEX idx_hr_it0022_highest ON hr_it0022_education (employee_id, is_highest) WHERE is_highest = TRUE;
```

### 3.10 IT0105 通讯方式

```sql
-- IT0105 通讯方式 (对标 SAP PA0105)
CREATE TABLE hr_it0105_communication (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '0105',
    subtype         VARCHAR(4) NOT NULL,       -- 0001:系统用户 0010:邮箱 0020:手机
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 通讯信息
    comm_id         VARCHAR(100) NOT NULL,     -- 通讯ID (用户名/邮箱/手机号)
    comm_method     VARCHAR(4),                -- 通讯方式

    -- 系统用户 (subtype=0001)
    system_user     VARCHAR(50),               -- 系统用户名
    system_id       UUID,                      -- 关联系统用户

    -- 备注
    remarks         TEXT,

    -- 是否主联系方式
    is_primary      BOOLEAN DEFAULT FALSE,

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, subtype, valid_from)
);

-- 索引
CREATE INDEX idx_hr_it0105_email ON hr_it0105_communication (employee_id, subtype, is_primary)
    WHERE subtype = '0010' AND is_primary = TRUE;
```

---

## 4. 时间管理 (PT) - 增强

### 4.1 时间管理架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    时间管理 (PT) 架构 - 对标 SAP PT                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ IT0007      │     │ IT2002      │     │ IT2005      │                   │
│  │ 排班计划    │────►│ 出勤记录    │◄────│ 加班记录    │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│         │                   │                   │                          │
│         │                   │                   │                          │
│         ▼                   ▼                   ▼                          │
│  ┌──────────────────────────────────────────────────────────────────┐      │
│  │                      工时汇总 (Time Evaluation)                    │      │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │      │
│  │  │ 月度汇总 │  │ 年度汇总 │  │ 假期余额 │  │ 加班余额 │         │      │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │      │
│  └──────────────────────────────────────────────────────────────────┘      │
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ IT2001      │     │ IT2003      │     │ IT2006      │                   │
│  │ 缺勤记录    │────►│ 出差记录    │────►│ 工时记录    │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 4.2 IT0007 排班计划

```sql
-- IT0007 排班计划 (对标 SAP PA0007)
CREATE TABLE hr_it0007_work_schedule (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '0007',
    subtype         VARCHAR(4) DEFAULT '',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 工作时间规则 (对标 SAP SCHNW)
    work_rule_id    UUID NOT NULL,             -- 工时规则
    work_rule_code  VARCHAR(4),
    work_rule_name  VARCHAR(100),

    -- 工时类型
    schedule_type   VARCHAR(2) DEFAULT 'FT',   -- FT:全职 PT:兼职 SE:轮班

    -- 标准工时
    weekly_hours    DECIMAL(4,1),              -- 周工作小时
    daily_hours     DECIMAL(3,1),              -- 日工作小时

    -- 工作日
    work_days       VARCHAR(7) DEFAULT 'YYYYYNN', -- 周一至周日 Y/N

    -- 弹性工时
    is_flexible     BOOLEAN DEFAULT FALSE,
    flex_core_start TIME,                      -- 核心工作时间开始
    flex_core_end   TIME,                      -- 核心工作时间结束
    flex_start_earliest TIME,                  -- 最早打卡时间
    flex_end_latest TIME,                      -- 最晚打卡时间

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, valid_from)
);

-- 班次定义 (对标 SAP SCHTP)
CREATE TABLE hr_work_shift (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    shift_code      VARCHAR(4) NOT NULL,       -- 班次编码
    shift_name      VARCHAR(50) NOT NULL,

    -- 工作时间
    work_start      TIME NOT NULL,             -- 上班时间
    work_end        TIME NOT NULL,             -- 下班时间

    -- 休息时间
    break_start     TIME,
    break_end       TIME,
    break_minutes   INTEGER,

    -- 工时
    work_hours      DECIMAL(3,1),              -- 工作小时

    -- 跨天
    is_overnight    BOOLEAN DEFAULT FALSE,

    -- 允许打卡范围
    clock_in_early  INTEGER DEFAULT 30,        -- 可提前打卡分钟
    clock_in_late   INTEGER DEFAULT 0,         -- 迟到宽限分钟
    clock_out_late  INTEGER DEFAULT 180,       -- 可延后打卡分钟

    status          general_status DEFAULT 'ACTIVE',

    UNIQUE (tenant_id, shift_code)
);

-- 班次规则分配
CREATE TABLE hr_employee_shift (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    employee_id     UUID NOT NULL REFERENCES hr_employee(id),
    shift_id        UUID NOT NULL REFERENCES hr_work_shift(id),

    -- 生效日期
    effective_date  DATE NOT NULL,

    -- 轮班
    is_rotating     BOOLEAN DEFAULT FALSE,
    rotation_group  VARCHAR(10),               -- 轮班组

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (employee_id, effective_date)
);
```

### 4.3 IT2001 缺勤记录 (增强)

```sql
-- IT2001 缺勤记录 (对标 SAP PA2001)
CREATE TABLE hr_it2001_absence (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '2001',
    subtype         VARCHAR(4) NOT NULL,       -- 假期类型编码

    -- 时间
    valid_from      DATE NOT NULL,             -- 开始日期
    valid_to        DATE NOT NULL,             -- 结束日期
    start_time      TIME,
    end_time        TIME,

    -- 天数/小时
    days            DECIMAL(5,1) NOT NULL,
    hours           DECIMAL(5,2),

    -- 假期类型
    leave_type_id   UUID REFERENCES hr_leave_type(id),
    leave_type_code VARCHAR(4),
    leave_type_name VARCHAR(100),

    -- 扣减配额
    quota_year      INTEGER,
    quota_deducted  DECIMAL(5,1) DEFAULT 0,

    -- 原因
    reason          TEXT,
    attachment_url  VARCHAR(500),

    -- 审批
    approval_status approval_status DEFAULT 'PENDING',
    approved_by     UUID,
    approved_at     TIMESTAMP,
    approval_remark TEXT,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, employee_id, valid_from, subtype)
);

-- 索引
CREATE INDEX idx_hr_it2001_employee ON hr_it2001_absence (employee_id, valid_from);
CREATE INDEX idx_hr_it2001_type ON hr_it2001_absence (leave_type_id, valid_from);
CREATE INDEX idx_hr_it2001_approval ON hr_it2001_absence (approval_status);
```

### 4.4 IT2002 出勤记录

```sql
-- IT2002 出勤/打卡记录 (对标 SAP PA2002)
CREATE TABLE hr_it2002_attendance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '2002',
    subtype         VARCHAR(4) DEFAULT '',

    -- 日期
    record_date     DATE NOT NULL,

    -- 班次
    shift_id        UUID REFERENCES hr_work_shift(id),
    shift_code      VARCHAR(4),

    -- 打卡时间
    clock_in        TIMESTAMP,                 -- 上班打卡
    clock_out       TIMESTAMP,                 -- 下班打卡

    -- 打卡地点
    clock_in_location VARCHAR(100),
    clock_out_location VARCHAR(100),
    clock_in_gps    VARCHAR(50),               -- GPS坐标
    clock_out_gps   VARCHAR(50),

    -- 计算工时
    work_hours      DECIMAL(4,2),              -- 实际工作小时
    break_hours     DECIMAL(3,2),              -- 休息小时
    net_work_hours  DECIMAL(4,2),              -- 净工作小时

    -- 异常标记
    is_late         BOOLEAN DEFAULT FALSE,
    late_minutes    INTEGER DEFAULT 0,
    is_early_leave  BOOLEAN DEFAULT FALSE,
    early_minutes   INTEGER DEFAULT 0,
    is_absent       BOOLEAN DEFAULT FALSE,

    -- 调整
    adjustment_hours DECIMAL(3,1),
    adjustment_reason TEXT,

    -- 来源
    source          VARCHAR(2) DEFAULT 'AP',   -- AP:APP BI:生物识别 MA:手动

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (employee_id, record_date)
);

-- 索引 (支持分区)
CREATE INDEX idx_hr_it2002_date ON hr_it2002_attendance (record_date);
CREATE INDEX idx_hr_it2002_employee_date ON hr_it2002_attendance (employee_id, record_date);

-- 分区建议 (按月)
-- CREATE TABLE hr_it2002_attendance_202603 PARTITION OF hr_it2002_attendance
--     FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');
```

### 4.5 IT2005 加班记录

```sql
-- IT2005 加班记录 (对标 SAP PA2005)
CREATE TABLE hr_it2005_overtime (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '2005',
    subtype         VARCHAR(4) DEFAULT '',     -- 加班类型

    -- 时间
    overtime_date   DATE NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,

    -- 时长
    hours           DECIMAL(4,2) NOT NULL,
    break_hours     DECIMAL(3,2) DEFAULT 0,

    -- 加班类型 (对标 SAP AWART)
    overtime_type   VARCHAR(4) NOT NULL,       -- 01:工作日 02:周末 03:法定假日
    compensation_type VARCHAR(2) DEFAULT 'PY', -- PY:调薪 TM:调休 BF:混合

    -- 调休
    comp_hours      DECIMAL(4,2),              -- 调休小时
    comp_expiry     DATE,                      -- 调休过期日

    -- 原因
    reason          TEXT NOT NULL,

    -- 审批
    approval_status approval_status DEFAULT 'PENDING',
    approved_by     UUID,
    approved_at     TIMESTAMP,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (tenant_id, employee_id, overtime_date, start_time)
);
```

### 4.6 假期类型与配额 (增强)

```sql
-- 假期类型 (对标 SAP T556A)
CREATE TABLE hr_leave_type (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    code            VARCHAR(4) NOT NULL,       -- 假期编码
    name            VARCHAR(100) NOT NULL,

    -- 配额
    has_quota       BOOLEAN DEFAULT TRUE,
    default_quota   DECIMAL(5,1),              -- 默认额度 (天/年)
    quota_unit      VARCHAR(2) DEFAULT 'DA',   -- DA:天 HO:小时

    -- 结转
    carry_over      BOOLEAN DEFAULT FALSE,
    max_carry_over  DECIMAL(5,1),
    carry_expiry_months INTEGER DEFAULT 3,

    -- 有效期
    valid_months    INTEGER DEFAULT 12,

    -- 工资
    is_paid         BOOLEAN DEFAULT TRUE,
    pay_percentage  DECIMAL(5,2) DEFAULT 100.00,

    -- 扣减规则
    deduct_order    INTEGER DEFAULT 1,

    status          general_status DEFAULT 'ACTIVE',

    UNIQUE (tenant_id, code)
);

-- 假期配额 (对标 SAP PA2006)
CREATE TABLE hr_leave_quota (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,
    leave_type_id   UUID NOT NULL REFERENCES hr_leave_type(id),

    -- 年度
    quota_year      INTEGER NOT NULL,

    -- 额度
    opening_balance DECIMAL(5,1) DEFAULT 0,    -- 期初/结转
    accrued         DECIMAL(5,1) DEFAULT 0,    -- 本期获得
    adjusted        DECIMAL(5,1) DEFAULT 0,    -- 调整
    used            DECIMAL(5,1) DEFAULT 0,    -- 已使用
    expired         DECIMAL(5,1) DEFAULT 0,    -- 已过期

    -- 计算余额
    balance         DECIMAL(5,1) GENERATED ALWAYS AS (
        opening_balance + accrued + adjusted - used - expired
    ) STORED,

    -- 有效期
    valid_from      DATE NOT NULL,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',
    expire_date     DATE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (employee_id, leave_type_id, quota_year)
);

-- 索引
CREATE INDEX idx_hr_leave_quota_balance ON hr_leave_quota (employee_id, quota_year, balance);
```

### 4.7 工时汇总表

```sql
-- 月度工时汇总 (对标 SAP PT 平衡表)
CREATE TABLE hr_time_balance_monthly (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    employee_id     UUID NOT NULL REFERENCES hr_employee(id),

    -- 期间
    period_year     INTEGER NOT NULL,
    period_month    INTEGER NOT NULL,

    -- 应出勤
    scheduled_days  DECIMAL(5,1),              -- 应出勤天数
    scheduled_hours DECIMAL(6,1),              -- 应出勤小时

    -- 实际
    actual_days     DECIMAL(5,1),              -- 实际出勤天数
    actual_hours    DECIMAL(6,1),              -- 实际工作小时

    -- 加班
    overtime_hours  DECIMAL(5,1) DEFAULT 0,
    overtime_paid   DECIMAL(5,1) DEFAULT 0,    -- 已支付加班
    overtime_comp   DECIMAL(5,1) DEFAULT 0,    -- 调休加班

    -- 缺勤
    absence_days    DECIMAL(5,1) DEFAULT 0,
    absence_hours   DECIMAL(5,1) DEFAULT 0,
    paid_leave_days DECIMAL(5,1) DEFAULT 0,
    unpaid_leave_days DECIMAL(5,1) DEFAULT 0,

    -- 迟到早退
    late_count      INTEGER DEFAULT 0,
    late_minutes    INTEGER DEFAULT 0,
    early_count     INTEGER DEFAULT 0,
    early_minutes   INTEGER DEFAULT 0,

    -- 状态
    status          VARCHAR(10) DEFAULT 'OPEN', -- OPEN/CLOSED/LOCKED

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (employee_id, period_year, period_month)
);

-- 索引
CREATE INDEX idx_hr_time_balance_period ON hr_time_balance_monthly (period_year, period_month);
```

---

## 5. 薪酬管理 (PY) - 增强

### 5.1 薪酬架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    薪酬管理 (PY) 架构 - 对标 SAP HRPY                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         薪酬要素 (Payroll Elements)                  │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 基本工资 │  │ 津贴补贴 │  │ 奖金     │  │ 扣款     │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         薪酬计算 (Payroll Calculation)              │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 社保     │  │ 公积金   │  │ 个税     │  │ 实发     │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         薪酬结果 (Payroll Result)                   │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 薪酬明细 │  │ 银行支付 │  │ 会计凭证 │  │ 报表     │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 5.2 薪酬期间控制

```sql
-- 薪酬期间 (对标 SAP T549A)
CREATE TABLE hr_payroll_period (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    period_year     INTEGER NOT NULL,
    period_month    INTEGER NOT NULL,
    period_key      VARCHAR(7) GENERATED ALWAYS AS (
        LPAD(period_year::TEXT, 4, '0') || LPAD(period_month::TEXT, 2, '0')
    ) STORED,

    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    pay_date        DATE,

    period_status   VARCHAR(10) DEFAULT 'OPEN',

    payroll_run_id  UUID,
    posted_at       TIMESTAMP,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, period_year, period_month)
);

-- 薪酬运行日志
CREATE TABLE hr_payroll_run (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    period_year     INTEGER NOT NULL,
    period_month    INTEGER NOT NULL,

    run_type        VARCHAR(2) DEFAULT 'RE',
    run_number      INTEGER DEFAULT 1,
    run_status      VARCHAR(10) DEFAULT 'RUNNING',

    employee_count  INTEGER DEFAULT 0,
    total_gross     DECIMAL(18,2) DEFAULT 0,
    total_net       DECIMAL(18,2) DEFAULT 0,

    started_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMP,
    error_message   TEXT,

    created_by      UUID
);
```

### 5.3 薪酬结果

```sql
-- 薪酬结果主表 (对标 SAP HRPY_RT)
CREATE TABLE hr_payroll_result (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    payroll_run_id  UUID REFERENCES hr_payroll_run(id),
    period_year     INTEGER NOT NULL,
    period_month    INTEGER NOT NULL,
    period_key      VARCHAR(7) NOT NULL,

    gross_pay       DECIMAL(15,2) DEFAULT 0,
    total_deduction DECIMAL(15,2) DEFAULT 0,
    net_pay         DECIMAL(15,2) DEFAULT 0,

    taxable_income  DECIMAL(15,2) DEFAULT 0,
    tax_deduction   DECIMAL(15,2) DEFAULT 0,
    tax_amount      DECIMAL(15,2) DEFAULT 0,

    social_personal DECIMAL(15,2) DEFAULT 0,
    social_company  DECIMAL(15,2) DEFAULT 0,
    fund_personal   DECIMAL(15,2) DEFAULT 0,
    fund_company    DECIMAL(15,2) DEFAULT 0,

    currency_id     UUID REFERENCES core_currency(id),
    result_status   VARCHAR(10) DEFAULT 'CALCULATED',

    bank_account_id UUID,
    payment_date    DATE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      UUID,

    UNIQUE (employee_id, period_key)
);

CREATE INDEX idx_hr_payroll_result_period ON hr_payroll_result (tenant_id, period_year, period_month);

-- 薪酬明细项
CREATE TABLE hr_payroll_item (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    result_id       UUID NOT NULL REFERENCES hr_payroll_result(id) ON DELETE CASCADE,

    wage_type_id    UUID REFERENCES hr_wage_type(id),
    wage_type_code  VARCHAR(4) NOT NULL,
    wage_type_name  VARCHAR(100) NOT NULL,

    category        VARCHAR(2) NOT NULL,
    item_type       VARCHAR(1) NOT NULL,
    amount          DECIMAL(15,2) NOT NULL,

    base_amount     DECIMAL(15,2),
    rate            DECIMAL(10,4),

    is_taxable      BOOLEAN DEFAULT TRUE,
    seq_no          INTEGER DEFAULT 1,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_hr_payroll_item_result ON hr_payroll_item (result_id);
```

### 5.4 社保公积金配置 (中国本地化)

```sql
-- 社保政策
CREATE TABLE hr_social_policy (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    policy_name     VARCHAR(100) NOT NULL,
    policy_code     VARCHAR(10) NOT NULL,
    province        VARCHAR(50),
    city            VARCHAR(50),

    valid_from      DATE NOT NULL,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (company_id, policy_code, valid_from)
);

-- 社保险种配置
CREATE TABLE hr_social_insurance_config (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_id       UUID NOT NULL REFERENCES hr_social_policy(id) ON DELETE CASCADE,

    insurance_type  VARCHAR(4) NOT NULL,
    insurance_name  VARCHAR(100) NOT NULL,

    base_min        DECIMAL(10,2),
    base_max        DECIMAL(10,2),
    company_rate    DECIMAL(5,4) NOT NULL,
    personal_rate   DECIMAL(5,4) NOT NULL,

    valid_from      DATE NOT NULL,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 公积金配置
CREATE TABLE hr_housing_fund_config (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    company_id      UUID NOT NULL REFERENCES sys_company(id),

    fund_name       VARCHAR(100) NOT NULL,
    fund_code       VARCHAR(10) NOT NULL,
    province        VARCHAR(50),
    city            VARCHAR(50),

    base_min        DECIMAL(10,2),
    base_max        DECIMAL(10,2),
    company_rate    DECIMAL(5,4) NOT NULL,
    personal_rate   DECIMAL(5,4) NOT NULL,

    valid_from      DATE NOT NULL,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (company_id, fund_code, valid_from)
);

-- IT0591 社保信息
CREATE TABLE hr_it0591_social_insurance (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,
    infotype        VARCHAR(4) DEFAULT '0591',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    policy_id       UUID REFERENCES hr_social_policy(id),
    pension_base    DECIMAL(10,2),
    medical_base    DECIMAL(10,2),
    unemployment_base DECIMAL(10,2),

    pension_personal DECIMAL(10,2),
    medical_personal DECIMAL(10,2),
    unemployment_personal DECIMAL(10,2),

    pension_company DECIMAL(10,2),
    medical_company DECIMAL(10,2),

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (employee_id, valid_from)
);

-- IT0588 公积金信息
CREATE TABLE hr_it0588_housing_fund (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,
    infotype        VARCHAR(4) DEFAULT '0588',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    fund_config_id  UUID REFERENCES hr_housing_fund_config(id),
    fund_base       DECIMAL(10,2),
    fund_personal   DECIMAL(10,2),
    fund_company    DECIMAL(10,2),
    fund_account    VARCHAR(30),

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (employee_id, valid_from)
);
```

### 5.5 个税配置 (中国本地化)

```sql
-- 个税税率表
CREATE TABLE hr_tax_rate_cn (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    tax_type        VARCHAR(4) NOT NULL,
    tax_name        VARCHAR(100) NOT NULL,
    level_no        INTEGER NOT NULL,
    lower_limit     DECIMAL(15,2) NOT NULL,
    upper_limit     DECIMAL(15,2),
    tax_rate        DECIMAL(5,4) NOT NULL,
    quick_deduction DECIMAL(15,2) DEFAULT 0,

    valid_from      DATE NOT NULL,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 专项附加扣除
CREATE TABLE hr_it0406_tax_deduction_cn (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,
    infotype        VARCHAR(4) DEFAULT '0406',
    subtype         VARCHAR(4) NOT NULL,
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    deduction_type  VARCHAR(4) NOT NULL,
    deduction_name  VARCHAR(100),
    deduction_amount DECIMAL(10,2) NOT NULL,

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (employee_id, subtype, valid_from)
);
```

---

## 6. 招聘管理 (RC) - 新增

### 6.1 招聘需求

```sql
-- 招聘需求 (对标 SAP PB4000)
CREATE TABLE hr_recruitment_requisition (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    requisition_no  VARCHAR(20) NOT NULL,

    position_id     UUID REFERENCES hr_om_object(id),
    job_id          UUID REFERENCES hr_om_object(id),
    org_unit_id     UUID REFERENCES hr_om_object(id),

    job_title       VARCHAR(100) NOT NULL,
    job_description TEXT,
    requirements    TEXT,

    headcount       INTEGER NOT NULL DEFAULT 1,
    filled_count    INTEGER DEFAULT 0,

    salary_min      DECIMAL(15,2),
    salary_max      DECIMAL(15,2),
    expected_date   DATE,

    recruit_type    VARCHAR(2) DEFAULT 'NE',
    approval_status approval_status DEFAULT 'PENDING',

    requisition_status VARCHAR(2) DEFAULT 'OP',
    requester_id    UUID REFERENCES hr_employee(id),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, requisition_no)
);

-- 招聘职位发布
CREATE TABLE hr_job_posting (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    posting_no      VARCHAR(20) NOT NULL,

    requisition_id  UUID REFERENCES hr_recruitment_requisition(id),
    posting_title   VARCHAR(200) NOT NULL,
    job_description TEXT,

    channels        JSONB,
    publish_date    DATE,
    expire_date     DATE,
    work_location   VARCHAR(200),

    posting_status  VARCHAR(2) DEFAULT 'DR',
    view_count      INTEGER DEFAULT 0,
    apply_count     INTEGER DEFAULT 0,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, posting_no)
);
```

### 6.2 候选人管理

```sql
-- 候选人
CREATE TABLE hr_candidate (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    full_name       VARCHAR(80) NOT NULL,
    gender          gender,
    birth_date      DATE,
    email           VARCHAR(100),
    mobile          VARCHAR(50),

    education       VARCHAR(4),
    school          VARCHAR(100),
    work_years      INTEGER,
    current_company VARCHAR(100),

    expected_salary_min DECIMAL(15,2),
    expected_salary_max DECIMAL(15,2),
    expected_city   VARCHAR(50),

    source          VARCHAR(20),
    resume_url      VARCHAR(500),
    resume_text     TEXT,

    candidate_status VARCHAR(2) DEFAULT 'NE',
    employee_id     UUID REFERENCES hr_employee(id),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 候选人申请
CREATE TABLE hr_candidate_application (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    candidate_id    UUID NOT NULL REFERENCES hr_candidate(id),
    posting_id      UUID NOT NULL REFERENCES hr_job_posting(id),

    application_status VARCHAR(2) DEFAULT 'NE',
    applied_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    current_stage   VARCHAR(4),

    recruiter_id    UUID REFERENCES hr_employee(id),
    rating          DECIMAL(2,1),
    notes           TEXT,

    UNIQUE (candidate_id, posting_id)
);
```

### 6.3 面试与Offer

```sql
-- 面试安排
CREATE TABLE hr_interview_schedule (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    application_id  UUID NOT NULL REFERENCES hr_candidate_application(id),

    round_no        INTEGER NOT NULL,
    round_name      VARCHAR(50),
    interview_type  VARCHAR(2) DEFAULT 'PE',

    scheduled_date  DATE NOT NULL,
    scheduled_time  TIME NOT NULL,
    duration        INTEGER DEFAULT 60,
    location        VARCHAR(200),

    interviewer_id  UUID NOT NULL REFERENCES hr_employee(id),
    interview_status VARCHAR(2) DEFAULT 'SC',
    result          VARCHAR(2),
    feedback        TEXT,
    rating          DECIMAL(2,1),

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Offer
CREATE TABLE hr_offer (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    offer_no        VARCHAR(20) NOT NULL,

    application_id  UUID NOT NULL REFERENCES hr_candidate_application(id),
    candidate_id    UUID NOT NULL REFERENCES hr_candidate(id),

    position_id     UUID REFERENCES hr_om_object(id),
    job_title       VARCHAR(100) NOT NULL,
    monthly_salary  DECIMAL(15,2) NOT NULL,
    annual_salary   DECIMAL(15,2),

    probation_months INTEGER DEFAULT 3,
    expected_join_date DATE NOT NULL,
    valid_until     DATE,

    offer_status    VARCHAR(2) DEFAULT 'PE',
    approved_by     UUID,
    sent_at         TIMESTAMP,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, offer_no)
);
```

---

## 7. 培训管理 (TM) - 新增

### 7.1 培训架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    培训管理 (TM) 架构 - 对标 SAP PE                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ 课程目录    │────►│ 培训班次    │────►│ 学员报名    │                   │
│  │ course      │     │ class       │     │ enrollment  │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│         │                   │                   │                          │
│         │                   │                   │                          │
│         ▼                   ▼                   ▼                          │
│  ┌──────────────────────────────────────────────────────────────────┐      │
│  │                        培训执行 (Training Execution)               │      │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │      │
│  │  │ 签到记录 │  │ 培训评估 │  │ 考试成绩 │  │ 证书发放 │         │      │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │      │
│  └──────────────────────────────────────────────────────────────────┘      │
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ 培训记录    │────►│ 资格认证    │────►│ 技能矩阵    │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 7.2 课程目录

```sql
-- 课程目录 (对标 SAP HRP1000 OTYPE='E')
CREATE TABLE hr_training_course (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    course_code     VARCHAR(10) NOT NULL,
    course_name     VARCHAR(200) NOT NULL,

    -- 分类
    category_id     UUID,
    category_name   VARCHAR(100),

    -- 类型
    course_type     VARCHAR(2) DEFAULT 'IN',   -- IN:内部 EX:外部 ON:在线

    -- 内容
    description     TEXT,
    objectives      TEXT,
    target_audience TEXT,
    prerequisites   TEXT,

    -- 时长
    duration_hours  DECIMAL(5,1),
    duration_days   DECIMAL(3,1),

    -- 费用
    cost            DECIMAL(10,2),
    currency_id     UUID REFERENCES core_currency(id),

    -- 供应商
    vendor_id       UUID REFERENCES bp_business_partner(id),
    vendor_name     VARCHAR(100),

    -- 课件
    material_url    VARCHAR(500),

    -- 状态
    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, course_code)
);

-- 课程分类
CREATE TABLE hr_training_category (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    category_code   VARCHAR(10) NOT NULL,
    category_name   VARCHAR(100) NOT NULL,
    parent_id       UUID REFERENCES hr_training_category(id),

    level           INTEGER DEFAULT 1,
    path            VARCHAR(200),

    status          general_status DEFAULT 'ACTIVE',

    UNIQUE (tenant_id, category_code)
);
```

### 7.3 培训班次

```sql
-- 培训班次 (对标 SAP PE事件管理)
CREATE TABLE hr_training_class (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    class_code      VARCHAR(20) NOT NULL,
    class_name      VARCHAR(200) NOT NULL,

    -- 课程
    course_id       UUID NOT NULL REFERENCES hr_training_course(id),
    course_name     VARCHAR(200),

    -- 时间地点
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    start_time      TIME,
    end_time        TIME,
    location        VARCHAR(200),
    room            VARCHAR(50),

    -- 在线链接
    online_link     VARCHAR(500),
    online_platform VARCHAR(50),

    -- 容量
    capacity        INTEGER NOT NULL,
    enrolled_count  INTEGER DEFAULT 0,
    completed_count INTEGER DEFAULT 0,

    -- 讲师
    instructor_id   UUID REFERENCES hr_employee(id),
    instructor_name VARCHAR(80),
    external_instructor VARCHAR(80),

    -- 费用
    unit_cost       DECIMAL(10,2),
    total_cost      DECIMAL(12,2),

    -- 状态
    class_status    VARCHAR(2) DEFAULT 'PL',   -- PL:计划 RE:报名中 RU:进行中 CO:完成 CA:取消

    -- 报名截止
    enroll_deadline DATE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, class_code)
);

-- 索引
CREATE INDEX idx_hr_training_class_dates ON hr_training_class (start_date, end_date);
CREATE INDEX idx_hr_training_class_status ON hr_training_class (class_status);
```

### 7.4 培训报名与记录

```sql
-- 培训报名
CREATE TABLE hr_training_enrollment (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    class_id        UUID NOT NULL REFERENCES hr_training_class(id),
    employee_id     UUID NOT NULL REFERENCES hr_employee(id),

    -- 报名信息
    enroll_date     DATE NOT NULL DEFAULT CURRENT_DATE,
    enroll_status   VARCHAR(2) DEFAULT 'PE',   -- PE:待审 AP:已批准 RE:已拒绝 CA:取消

    -- 审批
    approved_by     UUID,
    approved_at     TIMESTAMP,

    -- 完成
    completion_status VARCHAR(2) DEFAULT 'EN', -- EN:已报名 AT:已出席 CO:已通过 FA:未通过
    attendance_rate DECIMAL(5,2),              -- 出勤率
    score           DECIMAL(5,1),              -- 成绩

    -- 评估
    evaluation_score DECIMAL(2,1),
    evaluation_comment TEXT,

    -- 证书
    certificate_no  VARCHAR(50),
    certificate_date DATE,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (class_id, employee_id)
);

-- 索引
CREATE INDEX idx_hr_enrollment_employee ON hr_training_enrollment (employee_id);

-- 培训签到记录
CREATE TABLE hr_training_attendance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    enrollment_id   UUID NOT NULL REFERENCES hr_training_enrollment(id),
    class_id        UUID NOT NULL REFERENCES hr_training_class(id),
    employee_id     UUID NOT NULL REFERENCES hr_employee(id),

    -- 签到日期
    attendance_date DATE NOT NULL,

    -- 签到签退
    check_in_time   TIMESTAMP,
    check_out_time  TIMESTAMP,

    -- 状态
    attendance_status VARCHAR(2) DEFAULT 'PR', -- PR:出勤 AB:缺勤 LA:迟到 EA:早退

    -- 备注
    remarks         TEXT,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (class_id, employee_id, attendance_date)
);
```

### 7.5 资格认证

```sql
-- 资格认证 (对标 SAP HRP1000 OTYPE='Q')
CREATE TABLE hr_qualification (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    qual_code       VARCHAR(10) NOT NULL,
    qual_name       VARCHAR(200) NOT NULL,

    -- 分类
    category_id     UUID,
    qual_type       VARCHAR(2),                -- CE:证书 SK:技能 LA:执照

    -- 描述
    description     TEXT,

    -- 有效期
    validity_months INTEGER,
    requires_renewal BOOLEAN DEFAULT FALSE,

    -- 评估标准
    evaluation_criteria TEXT,

    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, qual_code)
);

-- 员工资格 (对标 SAP IT0024)
CREATE TABLE hr_it0024_qualification (
    employee_id     UUID NOT NULL REFERENCES hr_employee(id) ON DELETE CASCADE,

    infotype        VARCHAR(4) DEFAULT '0024',
    subtype         VARCHAR(4) DEFAULT '',
    valid_from      DATE NOT NULL DEFAULT CURRENT_DATE,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    -- 资格信息
    qual_id         UUID NOT NULL REFERENCES hr_qualification(id),
    qual_code       VARCHAR(10),
    qual_name       VARCHAR(200),

    -- 级别
    proficiency_level VARCHAR(2),              -- 01-05: 1=初级 5=专家

    -- 获取方式
    acquisition_type VARCHAR(2),               -- TR:培训 EX:经验 CE:认证
    acquisition_date DATE,

    -- 证书
    certificate_no  VARCHAR(50),
    certificate_org VARCHAR(100),

    -- 过期
    expiry_date     DATE,

    tenant_id       UUID NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (employee_id, qual_id, valid_from)
);
```

---

## 8. 绩效管理 (PM) - 新增

### 8.1 绩效架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    绩效管理 (PM) 架构 - 对标 SAP PA0380                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ 绩效周期    │────►│ 目标设定    │────►│ 目标追踪    │                   │
│  │ period      │     │ goal        │     │ tracking    │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│         │                   │                   │                          │
│         │                   │                   │                          │
│         ▼                   ▼                   ▼                          │
│  ┌──────────────────────────────────────────────────────────────────┐      │
│  │                        绩效评估 (Performance Review)               │      │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │      │
│  │  │ 自评     │  │ 上级评   │  │ 360度评估│  │ 校准会议 │         │      │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │      │
│  └──────────────────────────────────────────────────────────────────┘      │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ 绩效结果    │────►│ 绩效等级    │────►│ 结果应用    │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 8.2 绩效周期

```sql
-- 绩效周期
CREATE TABLE hr_performance_period (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    period_code     VARCHAR(10) NOT NULL,
    period_name     VARCHAR(100) NOT NULL,

    -- 年度
    period_year     INTEGER NOT NULL,
    period_type     VARCHAR(2) DEFAULT 'AN',   -- AN:年度 HY:半年 QU:季度 MO:月度

    -- 日期
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,

    -- 阶段控制
    goal_start      DATE,
    goal_end        DATE,
    review_start    DATE,
    review_end      DATE,

    -- 状态
    period_status   VARCHAR(2) DEFAULT 'DR',   -- DR:草稿 AC:激活 CL:关闭

    -- 模板
    template_id     UUID,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, period_code)
);
```

### 8.3 绩效目标

```sql
-- 绩效目标 (对标 SAP PA0380)
CREATE TABLE hr_performance_goal (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 员工与周期
    employee_id     UUID NOT NULL REFERENCES hr_employee(id),
    period_id       UUID NOT NULL REFERENCES hr_performance_period(id),

    -- 目标信息
    goal_category   VARCHAR(4),                -- 目标类别
    goal_title      VARCHAR(200) NOT NULL,
    goal_description TEXT,

    -- 权重
    weight          DECIMAL(5,2) NOT NULL,     -- 权重百分比

    -- 目标值
    target_value    DECIMAL(15,2),
    target_unit     VARCHAR(20),
    target_operator VARCHAR(2) DEFAULT 'GE',   -- GE:>= LE:<= EQ:=

    -- 实际值
    actual_value    DECIMAL(15,2),
    achievement_rate DECIMAL(5,2),             -- 完成率

    -- 评分
    self_rating     DECIMAL(3,1),              -- 自评分数
    manager_rating  DECIMAL(3,1),              -- 上级评分
    final_rating    DECIMAL(3,1),              -- 最终分数

    -- 状态
    goal_status     VARCHAR(2) DEFAULT 'DR',   -- DR:草稿 SU:提交 AP:已批 CO:完成

    -- 上级评论
    manager_comment TEXT,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_hr_goal_employee_period ON hr_performance_goal (employee_id, period_id);
```

### 8.4 绩效评估

```sql
-- 绩效评估主表
CREATE TABLE hr_performance_review (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    employee_id     UUID NOT NULL REFERENCES hr_employee(id),
    period_id       UUID NOT NULL REFERENCES hr_performance_period(id),

    -- 评分汇总
    goals_avg_rating DECIMAL(3,1),             -- 目标平均分
    competency_rating DECIMAL(3,1),            -- 能力评分
    overall_rating  DECIMAL(3,1),              -- 综合评分

    -- 等级
    performance_level VARCHAR(2),              -- AA:卓越 AA:优秀 BB:良好 CC:合格 DD:待改进 EE:不合格

    -- 评语
    strengths       TEXT,                      -- 优势
    improvements    TEXT,                      -- 待改进
    development_plan TEXT,                     -- 发展计划

    -- 审批
    review_status   VARCHAR(2) DEFAULT 'DR',   -- DR:草稿 SU:提交 AP:已批
    reviewer_id     UUID REFERENCES hr_employee(id),
    reviewed_at     TIMESTAMP,

    -- 校准
    calibrated_rating DECIMAL(3,1),
    calibrated_level VARCHAR(2),
    calibrated_by   UUID,
    calibrated_at   TIMESTAMP,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (employee_id, period_id)
);

-- 360度评估
CREATE TABLE hr_360_evaluation (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    review_id       UUID NOT NULL REFERENCES hr_performance_review(id),

    -- 被评人
    subject_id      UUID NOT NULL REFERENCES hr_employee(id),

    -- 评估人
    evaluator_id    UUID NOT NULL REFERENCES hr_employee(id),
    evaluator_type  VARCHAR(2) NOT NULL,       -- SE:自评 MG:上级 PE:同级 SU:下级

    -- 评分
    competency_scores JSONB,                   -- 能力评分详情
    overall_rating  DECIMAL(3,1),

    -- 评语
    feedback        TEXT,

    -- 状态
    eval_status     VARCHAR(2) DEFAULT 'PE',   -- PE:待评 CO:完成

    submitted_at    TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (review_id, evaluator_id)
);
```

### 8.5 绩效结果

```sql
-- 绩效结果 (对标 SAP PA0382)
CREATE TABLE hr_performance_result (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    employee_id     UUID NOT NULL REFERENCES hr_employee(id),
    period_id       UUID NOT NULL REFERENCES hr_performance_period(id),
    review_id       UUID NOT NULL REFERENCES hr_performance_review(id),

    -- 年度
    period_year     INTEGER NOT NULL,

    -- 评分
    final_rating    DECIMAL(3,1) NOT NULL,
    performance_level VARCHAR(2) NOT NULL,

    -- 排名
    rank_in_team    INTEGER,
    rank_in_org     INTEGER,
    percentile      DECIMAL(5,2),

    -- 奖金系数
    bonus_factor    DECIMAL(3,2),

    -- 调薪建议
    salary_increase_pct DECIMAL(5,2),

    -- 晋升建议
    promotion_recommend VARCHAR(2),            -- PR:推荐 HO:保留 NO:不推荐
    promotion_target VARCHAR(100),

    -- 历史记录
    history         JSONB,                     -- 历年绩效摘要

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (employee_id, period_id)
);

-- 索引
CREATE INDEX idx_hr_result_period ON hr_performance_result (period_year);
CREATE INDEX idx_hr_result_level ON hr_performance_result (performance_level);
```

---

## 9. 继任计划 (SP) - 新增

### 9.1 继任计划架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    继任计划 (SP) 架构 - 对标 SAP PP                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                   │
│  │ 关键职位    │────►│ 继任者池    │────►│ 发展计划    │                   │
│  │ key_position│     │ successor   │     │ dev_plan    │                   │
│  └─────────────┘     └─────────────┘     └─────────────┘                   │
│         │                   │                   │                          │
│         │                   │                   │                          │
│         ▼                   ▼                   ▼                          │
│  ┌──────────────────────────────────────────────────────────────────┐      │
│  │                        人才盘点 (Talent Review)                    │      │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │      │
│  │  │ 九宫格   │  │ 人才地图 │  │ 风险评估 │  │ 保留策略 │         │      │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │      │
│  └──────────────────────────────────────────────────────────────────┘      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 9.2 人才池

```sql
-- 人才池定义
CREATE TABLE hr_talent_pool (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    pool_code       VARCHAR(10) NOT NULL,
    pool_name       VARCHAR(100) NOT NULL,

    -- 类型
    pool_type       VARCHAR(2) DEFAULT 'SU',   -- SU:继任 HI:高潜 EX:专家 LE:领导力

    -- 目标职位
    target_level    VARCHAR(4),                -- 目标层级

    -- 容量
    capacity        INTEGER,
    current_count   INTEGER DEFAULT 0,

    -- 负责人
    owner_id        UUID REFERENCES hr_employee(id),

    status          general_status DEFAULT 'ACTIVE',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, pool_code)
);

-- 人才池成员
CREATE TABLE hr_talent_pool_member (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    pool_id         UUID NOT NULL REFERENCES hr_talent_pool(id),
    employee_id     UUID NOT NULL REFERENCES hr_employee(id),

    -- 加入时间
    join_date       DATE NOT NULL DEFAULT CURRENT_DATE,

    -- 准备度
    readiness       VARCHAR(2) DEFAULT 'NO',   -- RE:就绪 NE:1年内 LO:1-2年 LO2:2年以上

    -- 九宫格位置
    performance_level VARCHAR(2),              -- 绩效等级
    potential_level VARCHAR(2),                -- 潜力等级
    grid_position   VARCHAR(2),                -- 九宫格位置

    -- 风险
    flight_risk     VARCHAR(2) DEFAULT 'LO',   -- HI:高 ME:中 LO:低
    retention_risk  VARCHAR(2),

    -- 发展建议
    development_focus TEXT,

    -- 状态
    member_status   VARCHAR(2) DEFAULT 'AC',   -- AC:活跃 PR:晋升 RE:移出

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (pool_id, employee_id)
);
```

### 9.3 继任者

```sql
-- 继任计划 (对标 SAP PP)
CREATE TABLE hr_succession_plan (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    -- 关键职位
    position_id     UUID NOT NULL REFERENCES hr_om_object(id),
    position_name   VARCHAR(100),

    -- 当前任职者
    current_holder_id UUID REFERENCES hr_employee(id),
    holder_name     VARCHAR(80),

    -- 风险评估
    vacancy_risk    VARCHAR(2),                -- 空缺风险
    bench_strength  VARCHAR(2),                -- 板凳深度

    -- 状态
    plan_status     VARCHAR(2) DEFAULT 'AC',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (tenant_id, position_id)
);

-- 继任者
CREATE TABLE hr_successor (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    plan_id         UUID NOT NULL REFERENCES hr_succession_plan(id),
    employee_id     UUID NOT NULL REFERENCES hr_employee(id),
    employee_name   VARCHAR(80),

    -- 继任顺序
    succession_order INTEGER NOT NULL DEFAULT 1,

    -- 准备度
    readiness       VARCHAR(2) DEFAULT 'NO',   -- RE:就绪 NE:1年内 LO:长期

    -- 评估
    strength        TEXT,
    development_area TEXT,

    -- 发展计划
    development_plan_id UUID,

    -- 状态
    successor_status VARCHAR(2) DEFAULT 'AC',

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (plan_id, employee_id)
);
```

### 9.4 发展计划

```sql
-- 发展计划
CREATE TABLE hr_development_plan (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    employee_id     UUID NOT NULL REFERENCES hr_employee(id),

    -- 计划周期
    plan_year       INTEGER NOT NULL,
    start_date      DATE NOT NULL,
    end_date        DATE,

    -- 目标职位
    target_position_id UUID REFERENCES hr_om_object(id),
    target_position_name VARCHAR(100),

    -- 目标能力
    target_competencies JSONB,

    -- 状态
    plan_status     VARCHAR(2) DEFAULT 'AC',

    -- 审批
    approved_by     UUID,
    approved_at     TIMESTAMP,

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (employee_id, plan_year)
);

-- 发展活动
CREATE TABLE hr_development_activity (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,

    plan_id         UUID NOT NULL REFERENCES hr_development_plan(id),

    -- 活动类型
    activity_type   VARCHAR(2) NOT NULL,       -- TR:培训 CO:辅导 RO:轮岗 PR:项目 SE:自学

    -- 活动内容
    activity_name   VARCHAR(200) NOT NULL,
    description     TEXT,

    -- 时间
    planned_start   DATE,
    planned_end     DATE,
    actual_start    DATE,
    actual_end      DATE,

    -- 状态
    activity_status VARCHAR(2) DEFAULT 'PL',   -- PL:计划 IP:进行 CO:完成 CA:取消

    -- 评估
    effectiveness   VARCHAR(2),                -- 有效性评估

    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 10. 存储过程

### 10.1 员工入职

```sql
-- 员工入职 (对标 SAP PA40 Hiring Action)
CREATE OR REPLACE FUNCTION hr_hire_employee(
    p_tenant_id UUID,
    p_employee_number VARCHAR,
    p_hire_date DATE,
    p_first_name VARCHAR,
    p_last_name VARCHAR,
    p_gender gender,
    p_birth_date DATE,
    p_org_object_id UUID,
    p_position_object_id UUID,
    p_user_id UUID
) RETURNS UUID AS $$
DECLARE
    v_employee_id UUID;
    v_person_object_id UUID;
BEGIN
    -- 创建 OM 人员对象 (OTYPE='P')
    INSERT INTO hr_om_object (
        tenant_id, object_type, object_id, name, valid_from, created_by
    ) VALUES (
        p_tenant_id, 'P', p_employee_number,
        p_first_name || ' ' || p_last_name,
        p_hire_date, p_user_id
    ) RETURNING id INTO v_person_object_id;

    -- 创建员工主数据
    INSERT INTO hr_employee (
        tenant_id, employee_number, om_object_id,
        full_name, gender, birth_date,
        hire_date, employee_status,
        created_by, updated_by
    ) VALUES (
        p_tenant_id, p_employee_number, v_person_object_id,
        p_first_name || ' ' || p_last_name, p_gender, p_birth_date,
        p_hire_date, 'AC',
        p_user_id, p_user_id
    ) RETURNING id INTO v_employee_id;

    -- 创建 IT0000 操作记录
    INSERT INTO hr_it0000_actions (
        employee_id, tenant_id, valid_from,
        action_type, employee_status, approval_status
    ) VALUES (
        v_employee_id, p_tenant_id, p_hire_date,
        '01', 'AC', 'APPROVED'
    );

    -- 创建 IT0001 组织分配
    INSERT INTO hr_it0001_org_assignment (
        employee_id, tenant_id, valid_from,
        org_unit_id, position_id, approval_status
    ) VALUES (
        v_employee_id, p_tenant_id, p_hire_date,
        p_org_object_id, p_position_object_id, 'APPROVED'
    );

    -- 创建 IT0002 个人数据
    INSERT INTO hr_it0002_personal_data (
        employee_id, tenant_id, valid_from,
        first_name, last_name, gender, birth_date
    ) VALUES (
        v_employee_id, p_tenant_id, p_hire_date,
        p_first_name, p_last_name, p_gender, p_birth_date
    );

    -- 创建 OM 关系 (职位-人员 HRP1001 008)
    INSERT INTO hr_om_relationship (
        tenant_id, relation_type,
        object_type_a, object_id_a,
        object_type_b, object_id_b,
        valid_from, created_by
    ) VALUES (
        p_tenant_id, '008',
        'S', p_position_object_id,
        'P', v_person_object_id,
        p_hire_date, p_user_id
    );

    -- 更新职位状态
    UPDATE hr_om_position_detail
    SET holder_object_id = v_person_object_id,
        holder_name = p_first_name || ' ' || p_last_name,
        position_status = 'FI',
        current_count = current_count + 1
    WHERE object_id = p_position_object_id;

    RETURN v_employee_id;
END;
$$ LANGUAGE plpgsql;
```

### 10.2 InfoType 时间分割

```sql
-- InfoType 时间分割 (对标 SAP Time Constraint 逻辑)
CREATE OR REPLACE FUNCTION hr_split_infotype(
    p_table_name VARCHAR,
    p_employee_id UUID,
    p_valid_from DATE,
    p_valid_to DATE,
    p_subtype VARCHAR DEFAULT ''
) RETURNS BOOLEAN AS $$
DECLARE
    v_existing RECORD;
    v_sql TEXT;
BEGIN
    -- 查找重叠记录
    v_sql := format('
        SELECT valid_from, valid_to
        FROM %I
        WHERE employee_id = $1
          AND (subtype = $2 OR $2 = '''')
          AND valid_from <= $3
          AND valid_to >= $4
        LIMIT 1
    ', p_table_name);

    EXECUTE v_sql INTO v_existing
    USING p_employee_id, p_subtype, p_valid_to, p_valid_from;

    IF FOUND THEN
        -- 情况1: 新记录在现有记录中间 (分割)
        IF v_existing.valid_from < p_valid_from AND v_existing.valid_to > p_valid_to THEN
            -- 缩短原记录
            v_sql := format('
                UPDATE %I SET valid_to = $1 WHERE employee_id = $2 AND valid_from = $3
            ', p_table_name);
            EXECUTE v_sql USING p_valid_from - 1, p_employee_id, v_existing.valid_from;

            -- 创建后半部分记录
            v_sql := format('
                INSERT INTO %I (employee_id, valid_from, valid_to, subtype, tenant_id, created_at)
                SELECT employee_id, $1, valid_to, subtype, tenant_id, CURRENT_TIMESTAMP
                FROM %I WHERE employee_id = $2 AND valid_from = $3
            ', p_table_name, p_table_name);
            EXECUTE v_sql USING p_valid_to + 1, p_employee_id, v_existing.valid_from;

        -- 情况2: 新记录覆盖后半部分
        ELSIF v_existing.valid_from < p_valid_from THEN
            v_sql := format('
                UPDATE %I SET valid_to = $1 WHERE employee_id = $2 AND valid_from = $3
            ', p_table_name);
            EXECUTE v_sql USING p_valid_from - 1, p_employee_id, v_existing.valid_from;

        -- 情况3: 新记录覆盖前半部分
        ELSIF v_existing.valid_to > p_valid_to THEN
            v_sql := format('
                UPDATE %I SET valid_from = $1 WHERE employee_id = $2 AND valid_from = $3
            ', p_table_name);
            EXECUTE v_sql USING p_valid_to + 1, p_employee_id, v_existing.valid_from;

        -- 情况4: 完全覆盖 (删除)
        ELSE
            v_sql := format('
                DELETE FROM %I WHERE employee_id = $1 AND valid_from = $2
            ', p_table_name);
            EXECUTE v_sql USING p_employee_id, v_existing.valid_from;
        END IF;
    END IF;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
```

### 10.3 薪酬计算 (简化版)

```sql
-- 薪酬计算 (对标 SAP RPCALCU0)
CREATE OR REPLACE FUNCTION hr_calculate_payroll(
    p_tenant_id UUID,
    p_period_year INTEGER,
    p_period_month INTEGER,
    p_user_id UUID
) RETURNS UUID AS $$
DECLARE
    v_run_id UUID;
    v_emp RECORD;
    v_result_id UUID;
    v_gross DECIMAL(15,2);
    v_deduction DECIMAL(15,2);
    v_net DECIMAL(15,2);
BEGIN
    -- 创建运行记录
    INSERT INTO hr_payroll_run (
        tenant_id, period_year, period_month, created_by
    ) VALUES (
        p_tenant_id, p_period_year, p_period_month, p_user_id
    ) RETURNING id INTO v_run_id;

    -- 遍历在职员工
    FOR v_emp IN
        SELECT e.id AS employee_id, e.full_name
        FROM hr_employee e
        WHERE e.tenant_id = p_tenant_id
          AND e.employee_status = 'AC'
          AND e.hire_date <= make_date(p_period_year, p_period_month, 28)
    LOOP
        -- 计算应发 (简化: 从 IT0008 获取基本工资)
        SELECT COALESCE(SUM(total_amount), 0) INTO v_gross
        FROM hr_it0008_basic_pay
        WHERE employee_id = v_emp.employee_id
          AND CURRENT_DATE BETWEEN valid_from AND valid_to;

        -- 计算扣款 (简化: 社保+公积金+个税)
        SELECT COALESCE(social_personal, 0) + COALESCE(fund_personal, 0) INTO v_deduction
        FROM hr_it0591_social_insurance
        WHERE employee_id = v_emp.employee_id
          AND CURRENT_DATE BETWEEN valid_from AND valid_to;

        v_deduction := COALESCE(v_deduction, 0);
        v_net := v_gross - v_deduction;

        -- 个税计算 (简化)
        IF v_gross - 5000 - v_deduction > 0 THEN
            v_net := v_net - ((v_gross - 5000 - v_deduction) * 0.1);
        END IF;

        -- 创建薪酬结果
        INSERT INTO hr_payroll_result (
            tenant_id, employee_id, payroll_run_id,
            period_year, period_month, period_key,
            gross_pay, total_deduction, net_pay,
            created_by
        ) VALUES (
            p_tenant_id, v_emp.employee_id, v_run_id,
            p_period_year, p_period_month,
            LPAD(p_period_year::TEXT, 4, '0') || LPAD(p_period_month::TEXT, 2, '0'),
            v_gross, v_deduction, v_net,
            p_user_id
        ) RETURNING id INTO v_result_id;

        -- 创建薪酬明细 (工资项)
        INSERT INTO hr_payroll_item (result_id, wage_type_code, wage_type_name, category, item_type, amount)
        SELECT v_result_id, wt.code, wt.name, wt.category,
               CASE WHEN wt.category = 'DE' THEN 'D' ELSE 'E' END, wi.amount
        FROM hr_it0008_basic_pay bp
        JOIN hr_it0008_wage_item wi ON wi.basic_pay_id = bp.employee_id
        JOIN hr_wage_type wt ON wt.id = wi.wage_type_id
        WHERE bp.employee_id = v_emp.employee_id
          AND CURRENT_DATE BETWEEN bp.valid_from AND bp.valid_to;

    END LOOP;

    -- 更新运行状态
    UPDATE hr_payroll_run
    SET run_status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP
    WHERE id = v_run_id;

    RETURN v_run_id;
END;
$$ LANGUAGE plpgsql;
```

### 10.4 个税计算 (中国)

```sql
-- 个税计算 (中国综合所得)
CREATE OR REPLACE FUNCTION hr_calculate_tax_cn(
    p_taxable_income DECIMAL(15,2),
    p_tax_deduction DECIMAL(15,2) DEFAULT 0
) RETURNS DECIMAL(15,2) AS $$
DECLARE
    v_taxable DECIMAL(15,2);
    v_tax DECIMAL(15,2);
    v_rate DECIMAL(5,4);
    v_quick DECIMAL(15,2);
BEGIN
    -- 计算应纳税所得额
    v_taxable := p_taxable_income - 5000 - p_tax_deduction;

    IF v_taxable <= 0 THEN
        RETURN 0;
    END IF;

    -- 根据级距确定税率和速算扣除数
    SELECT tax_rate, quick_deduction INTO v_rate, v_quick
    FROM hr_tax_rate_cn
    WHERE tax_type = '01'
      AND lower_limit <= v_taxable
      AND (upper_limit IS NULL OR upper_limit >= v_taxable)
      AND CURRENT_DATE BETWEEN valid_from AND valid_to
    ORDER BY lower_limit DESC
    LIMIT 1;

    -- 计算税额
    v_tax := v_taxable * v_rate - COALESCE(v_quick, 0);

    RETURN GREATEST(v_tax, 0);
END;
$$ LANGUAGE plpgsql;
```

---

## 11. 分析视图

### 11.1 员工360度视图

```sql
-- 员工360度完整视图
CREATE VIEW v_hr_employee_360 AS
SELECT
    e.id,
    e.employee_number,
    e.full_name,
    e.gender,
    e.birth_date,
    e.hire_date,
    EXTRACT(YEAR FROM AGE(CURRENT_DATE, e.hire_date))::INTEGER AS years_of_service,

    -- IT0001 组织信息
    org.org_unit_name,
    org.position_name,
    org.job_name,
    org.manager_name,
    org.employee_group,

    -- IT0008 薪酬
    pay.total_amount AS current_salary,
    pay.pay_grade,
    pay.pay_level,

    -- 状态
    e.employee_status,

    -- 绩效
    perf.final_rating AS latest_rating,
    perf.performance_level AS latest_perf_level,

    -- 人才池
    tp.pool_name AS talent_pool,
    tpm.readiness,
    tpm.grid_position

FROM hr_employee e
LEFT JOIN hr_it0001_org_assignment org
    ON org.employee_id = e.id AND CURRENT_DATE BETWEEN org.valid_from AND org.valid_to
LEFT JOIN hr_it0008_basic_pay pay
    ON pay.employee_id = e.id AND CURRENT_DATE BETWEEN pay.valid_from AND pay.valid_to
LEFT JOIN LATERAL (
    SELECT final_rating, performance_level
    FROM hr_performance_result
    WHERE employee_id = e.id
    ORDER BY period_year DESC
    LIMIT 1
) perf ON TRUE
LEFT JOIN hr_talent_pool_member tpm ON tpm.employee_id = e.id AND tpm.member_status = 'AC'
LEFT JOIN hr_talent_pool tp ON tp.id = tpm.pool_id

WHERE e.status = 'ACTIVE';
```

### 11.2 人员编制分析

```sql
-- 组织编制分析视图
CREATE VIEW v_hr_headcount_analysis AS
SELECT
    o.id AS org_id,
    o.object_id AS org_code,
    o.name AS org_name,
    od.org_level,
    od.max_headcount AS approved_headcount,
    od.headcount AS actual_headcount,
    od.max_headcount - od.headcount AS variance,

    -- 在职状态分布
    COUNT(CASE WHEN e.employee_status = 'AC' THEN 1 END) AS active_count,
    COUNT(CASE WHEN e.employee_status = 'IN' THEN 1 END) AS on_leave_count,
    COUNT(CASE WHEN e.employee_status = 'TE' THEN 1 END) AS terminated_count,

    -- 性别分布
    COUNT(CASE WHEN e.gender = 'M' THEN 1 END) AS male_count,
    COUNT(CASE WHEN e.gender = 'F' THEN 1 END) AS female_count,

    -- 年龄分布
    COUNT(CASE WHEN EXTRACT(YEAR FROM AGE(e.birth_date)) < 30 THEN 1 END) AS under_30,
    COUNT(CASE WHEN EXTRACT(YEAR FROM AGE(e.birth_date)) BETWEEN 30 AND 40 THEN 1 END) AS age_30_40,
    COUNT(CASE WHEN EXTRACT(YEAR FROM AGE(e.birth_date)) BETWEEN 40 AND 50 THEN 1 END) AS age_40_50,
    COUNT(CASE WHEN EXTRACT(YEAR FROM AGE(e.birth_date)) > 50 THEN 1 END) AS over_50

FROM hr_om_object o
JOIN hr_om_org_unit_detail od ON od.object_id = o.id
LEFT JOIN hr_employee e ON e.org_unit_id = o.id AND e.status = 'ACTIVE'

WHERE o.object_type = 'O' AND o.status = 'ACTIVE'
GROUP BY o.id, o.object_id, o.name, od.org_level, od.max_headcount, od.headcount;
```

### 11.3 招聘漏斗分析

```sql
-- 招聘漏斗分析视图
CREATE VIEW v_hr_recruitment_funnel AS
SELECT
    jp.id AS posting_id,
    jp.posting_no,
    jp.posting_title,
    jp.publish_date,

    -- 漏斗各阶段数量
    jp.view_count,
    jp.apply_count,
    COUNT(DISTINCT CASE WHEN ca.application_status >= 'SC' THEN ca.id END) AS screened,
    COUNT(DISTINCT CASE WHEN ca.application_status >= 'IN' THEN ca.id END) AS interviewed,
    COUNT(DISTINCT CASE WHEN ca.application_status >= 'OF' THEN ca.id END) AS offered,
    COUNT(DISTINCT CASE WHEN ca.application_status = 'AC' THEN ca.id END) AS accepted,

    -- 转化率
    CASE WHEN jp.view_count > 0
         THEN ROUND(100.0 * jp.apply_count / jp.view_count, 2) END AS apply_rate,
    CASE WHEN jp.apply_count > 0
         THEN ROUND(100.0 * COUNT(DISTINCT CASE WHEN ca.application_status >= 'OF' THEN ca.id END) / jp.apply_count, 2) END AS offer_rate

FROM hr_job_posting jp
LEFT JOIN hr_candidate_application ca ON ca.posting_id = jp.id
WHERE jp.posting_status IN ('PU', 'CL')
GROUP BY jp.id, jp.posting_no, jp.posting_title, jp.publish_date, jp.view_count, jp.apply_count;
```

---

## 12. 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|----------|
| 1.0 | 2026-03-14 | NextERP Team | 初始版本 |
| 2.0 | 2026-03-14 | NextERP Team | 对标 SAP ECC/S/4HANA HCM 完整增强版 |

### 变更记录

**v2.0 主要增强:**

1. **组织管理 (OM) 增强**
   - 新增 HRP1000/HRP1001 风格的通用对象表和关系表
   - 支持任意对象间的关系定义
   - 支持矩阵汇报和多汇报线

2. **人事管理 (PA) 增强**
   - 新增 IT0000 操作记录
   - 新增 IT0006 地址信息
   - 新增 IT0009 银行信息
   - 新增 IT0021 家庭成员
   - 新增 IT0022 教育背景
   - 新增 IT0105 通讯方式

3. **时间管理 (PT) 增强**
   - 新增 IT0007 排班计划
   - 新增 IT2002 出勤记录
   - 新增 IT2005 加班记录
   - 新增月度工时汇总

4. **薪酬管理 (PY) 增强**
   - 新增薪酬期间控制
   - 新增薪酬运行日志
   - 新增社保公积金配置 (中国本地化)
   - 新增个税税率表 (中国本地化)
   - 新增 IT0406 专项附加扣除

5. **招聘管理 (RC) 新增**
   - 招聘需求
   - 招聘职位发布
   - 候选人管理
   - 面试安排
   - Offer 管理

6. **培训管理 (TM) 新增**
   - 课程目录
   - 培训班次
   - 培训报名与签到
   - 资格认证

7. **绩效管理 (PM) 新增**
   - 绩效周期
   - 绩效目标
   - 绩效评估
   - 360度评估
   - 绩效结果

8. **继任计划 (SP) 新增**
   - 人才池
   - 继任者
   - 发展计划

9. **存储过程**
   - 员工入职
   - InfoType 时间分割
   - 薪酬计算
   - 个税计算

10. **分析视图**
    - 员工360度视图
    - 人员编制分析
    - 招聘漏斗分析
---
