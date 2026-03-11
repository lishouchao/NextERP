# NextERP + PostgreSQL 数据库设计选型指南

## 问题分析

**核心问题**: NextERP 使用 PostgreSQL，应该借鉴 ECC 还是 S/4HANA 的数据库设计？

**关键因素**:
- PostgreSQL 是传统关系型数据库（OLTP 优化）
- S/4HANA 针对内存列式数据库（HANA）设计
- ECC 针对传统行式数据库设计

## 数据库特性对比

### PostgreSQL vs SAP HANA vs AnyDB

| 特性 | PostgreSQL | SAP HANA | AnyDB (Oracle等) |
|------|------------|----------|------------------|
| 存储方式 | 行存储为主 | 列存储 | 行存储 |
| 内存使用 | 可配置 | 全内存 | 磁盘优先 |
| JSON 支持 | ✓ JSONB | ✓ | 有限 |
| 表继承 | ✓ | ✗ | ✗ |
| 分区表 | ✓ | ✓ | ✓ |
| 物化视图 | ✓ | ✓ | ✓ |
| 列存储 | 有限(扩展) | 原生 | 有限 |

### 与 SAP 版本匹配度

```
┌─────────────────────────────────────────────────────────────────┐
│                    数据库设计匹配度分析                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   PostgreSQL                                                    │
│   ────────────                                                  │
│       │                                                         │
│       │  ┌─────────────────┐    ┌─────────────────┐            │
│       │  │     ECC 6.0     │    │   S/4HANA       │            │
│       │  │   (AnyDB 设计)  │    │  (HANA 优化)    │            │
│       │  └────────┬────────┘    └────────┬────────┘            │
│       │           │                      │                      │
│       │      匹配度: 80%            匹配度: 40%                 │
│       │           │                      │                      │
│       │    • 行存储适配 ✓          • 列存储不匹配 ✗             │
│       │    • 范式化设计 ✓          • 宽表性能差 ✗               │
│       │    • 索引策略 ✓            • 内存优先不匹配 ✗           │
│       │    • 聚合表需要 ✓          • 实时计算压力大 ✗           │
│       │                           • 概念可借鉴 ✓                │
│       │                                                          │
│       ▼                                                          │
│   ┌─────────────────────────────────────────────────────┐      │
│   │              推荐策略：混合借鉴                        │      │
│   │  • 基础架构：ECC (关系型数据库设计)                    │      │
│   │  • 现代概念：S/4HANA (BP模型、统一视图)               │      │
│   │  • PostgreSQL 优化：利用特有功能                      │      │
│   └─────────────────────────────────────────────────────┘      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## 详细对比分析

### 1. 表结构设计

#### S/4HANA ACDOCA (Universal Journal)

```sql
-- S/4HANA 超级宽表 (200+ 字段)
CREATE TABLE ACDOCA (
    RCLNT       VARCHAR(3),
    RBUKRS      VARCHAR(4),
    RYEAR       NUMERIC(4),
    RACCT       VARCHAR(10),
    RCNTR       VARCHAR(10),
    RPRCTR      VARCHAR(10),
    -- ... 200+ 字段
    HSL         DECIMAL(23,2),  -- 本位币金额
    TSL         DECIMAL(23,2),  -- 交易货币金额
    KSL         DECIMAL(23,2),  -- 成本控制货币金额
    OSL         DECIMAL(23,2),  -- 对象货币金额
    CSL         DECIMAL(23,2),  -- 利润中心货币金额
    -- ... 更多金额字段
);
```

**PostgreSQL 问题**:
- ❌ 宽表在行存储中性能差
- ❌ 大量 NULL 值浪费空间
- ❌ 索引策略复杂
- ❌ 查询优化器难以处理

#### ECC 多表设计

```sql
-- ECC 分离表设计
CREATE TABLE BKPF (          -- 凭证头
    BUKRS VARCHAR(4),        -- 公司代码
    BELNR VARCHAR(10),       -- 凭证号
    GJAHR NUMERIC(4),        -- 年度
    BLART VARCHAR(2),        -- 凭证类型
    BUDAT DATE,              -- 过账日期
    WAERS VARCHAR(5),        -- 货币
    PRIMARY KEY (BUKRS, BELNR, GJAHR)
);

CREATE TABLE BSEG (          -- 凭证段
    BUKRS VARCHAR(4),
    BELNR VARCHAR(10),
    GJAHR NUMERIC(4),
    BUZEI NUMERIC(3),        -- 行号
    HKONT VARCHAR(10),       -- 科目
    DMBTR DECIMAL(23,2),     -- 金额
    KUNNR VARCHAR(10),       -- 客户
    LIFNR VARCHAR(10),       -- 供应商
    PRIMARY KEY (BUKRS, BELNR, GJAHR, BUZEI)
);

CREATE TABLE GLT0 (          -- 余额表
    RCLNT VARCHAR(3),
    RBUKRS VARCHAR(4),
    RACCT VARCHAR(10),
    RYEAR NUMERIC(4),
    HSL01 DECIMAL(23,2),     -- 期间1余额
    HSL02 DECIMAL(23,2),     -- 期间2余额
    -- ...
    PRIMARY KEY (RCLNT, RBUKRS, RACCT, RYEAR)
);
```

**PostgreSQL 优势**:
- ✅ 范式化设计适合行存储
- ✅ 索引效率高
- ✅ 查询优化器友好
- ✅ 空间利用高效

### 2. 业务伙伴 (BP) 模型

**推荐采用 S/4HANA 的 BP 模型**（数据库无关的现代设计）

```sql
-- 业务伙伴通用数据 (借鉴 S/4HANA)
CREATE TABLE but000 (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    partner_number  VARCHAR(10) UNIQUE NOT NULL,
    partner_type    VARCHAR(2) NOT NULL,  -- 1=组织, 2=个人
    category        VARCHAR(1),            -- 客户/供应商/员工

    -- 个人信息
    first_name      VARCHAR(40),
    last_name       VARCHAR(40),
    full_name       VARCHAR(80) GENERATED ALWAYS AS
        (COALESCE(first_name || ' ', '') || last_name) STORED,

    -- 组织信息
    organization_name VARCHAR(80),

    -- 通用信息
    tax_id          VARCHAR(20),
    legal_entity    VARCHAR(4),

    -- 审计字段
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),

    -- 租户
    tenant_id       UUID NOT NULL
);

-- BP 角色 (客户/供应商/员工等)
CREATE TABLE but100 (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    partner_id      UUID NOT NULL REFERENCES but000(id),
    role_type       VARCHAR(4) NOT NULL,  -- FLCU00=客户, FLVN00=供应商

    valid_from      DATE NOT NULL,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',

    tenant_id       UUID NOT NULL
);

-- BP 地址
CREATE TABLE but020 (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    partner_id      UUID NOT NULL REFERENCES but000(id),
    address_type    VARCHAR(2),  -- 默认/开票/发货
    street          VARCHAR(60),
    city            VARCHAR(40),
    postal_code     VARCHAR(10),
    country         VARCHAR(3),
    region          VARCHAR(3),

    tenant_id       UUID NOT NULL
);

-- 客户扩展数据
CREATE TABLE customer_ext (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    partner_id      UUID NOT NULL REFERENCES but000(id),
    company_code    VARCHAR(4),
    sales_org       VARCHAR(4),
    distribution_channel VARCHAR(2),
    credit_limit    DECIMAL(15,2),

    tenant_id       UUID NOT NULL
);

-- 供应商扩展数据
CREATE TABLE supplier_ext (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    partner_id      UUID NOT NULL REFERENCES but000(id),
    company_code    VARCHAR(4),
    purchasing_org  VARCHAR(4),
    payment_terms   VARCHAR(4),

    tenant_id       UUID NOT NULL
);
```

### 3. HR 信息类型架构

**推荐采用 ECC 的信息类型架构**（成熟稳定）

```sql
-- 信息类型基础表 (PostgreSQL 表继承实现)
CREATE TABLE infotype_base (
    pernr           VARCHAR(8) NOT NULL,      -- 员工号
    infty           VARCHAR(4) NOT NULL,      -- 信息类型
    subtype         VARCHAR(4),
    object_id       VARCHAR(2),
    lock_indicator  BOOLEAN DEFAULT FALSE,
    valid_from      DATE NOT NULL,
    valid_to        DATE NOT NULL DEFAULT '9999-12-31',
    sequence_number NUMERIC(3) DEFAULT 0,

    -- 审计
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(50),
    updated_by      VARCHAR(50),

    tenant_id       UUID NOT NULL,

    PRIMARY KEY (pernr, infty, valid_from, sequence_number)
);

-- IT0001 组织分配 (继承基础表)
CREATE TABLE pa0001 (
    -- 继承基础字段 (PostgreSQL 表继承或手动复制)

    -- IT0001 特有字段
    org_unit_id     UUID,
    position_id     UUID,
    job_id          UUID,
    cost_center_id  UUID,
    company_code    VARCHAR(4),
    personnel_area  VARCHAR(4),
    employee_group  VARCHAR(1),
    employee_subgroup VARCHAR(2),

    PRIMARY KEY (pernr, valid_from)
) INHERITS (infotype_base);  -- PostgreSQL 表继承

-- IT0002 个人数据
CREATE TABLE pa0002 (
    last_name       VARCHAR(40) NOT NULL,
    first_name      VARCHAR(40) NOT NULL,
    full_name       VARCHAR(80) GENERATED ALWAYS AS
        (first_name || ' ' || last_name) STORED,
    gender          CHAR(1) CHECK (gender IN ('M', 'F')),
    birth_date      DATE,
    nationality     VARCHAR(3),
    id_type         VARCHAR(4),
    id_number       VARCHAR(20),

    PRIMARY KEY (pernr, valid_from)
) INHERITS (infotype_base);

-- IT0008 基本工资
CREATE TABLE pa0008 (
    pay_type        VARCHAR(2),
    pay_area        VARCHAR(2),
    pay_grade       VARCHAR(4),
    pay_level       VARCHAR(2),
    currency        VARCHAR(3),
    -- 工资项 (使用 JSONB 存储多个工资项)
    wage_items      JSONB,  -- [{"type": "1000", "amount": 10000}, ...]

    PRIMARY KEY (pernr, valid_from)
) INHERITS (infotype_base);
```

### 4. 聚合表 vs 实时计算

**PostgreSQL 场景下推荐保留聚合表**（与 ECC 相似）

```sql
-- 余额汇总表 (ECC 风格，PostgreSQL 优化)
CREATE TABLE account_balance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_code    VARCHAR(4) NOT NULL,
    account_number  VARCHAR(10) NOT NULL,
    fiscal_year     NUMERIC(4) NOT NULL,
    currency        VARCHAR(3) NOT NULL,

    -- 12个期间余额 (PostgreSQL 数组)
    period_balance  DECIMAL(15,2)[12],  -- [p01, p02, ..., p12]
    period_debit    DECIMAL(15,2)[12],
    period_credit   DECIMAL(15,2)[12],

    -- 累计
    year_balance    DECIMAL(15,2),
    year_debit      DECIMAL(15,2),
    year_credit     DECIMAL(15,2),

    tenant_id       UUID NOT NULL,

    UNIQUE (company_code, account_number, fiscal_year, currency)
);

-- 创建物化视图加速常用查询
CREATE MATERIALIZED VIEW mv_customer_balance AS
SELECT
    c.partner_id,
    c.company_code,
    SUM(CASE WHEN d.credit_debit = 'D' THEN d.amount ELSE -d.amount END) AS balance
FROM customer_ext c
JOIN journal_entry_item d ON d.customer_id = c.partner_id
GROUP BY c.partner_id, c.company_code;

-- 定期刷新
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_customer_balance;
```

## 推荐的混合设计策略

### 策略总结

```
┌─────────────────────────────────────────────────────────────────┐
│                NextERP + PostgreSQL 推荐设计策略                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  从 ECC 借鉴 (适合 PostgreSQL):                                 │
│  ─────────────────────────────                                 │
│  ✅ 范式化表设计                                                │
│  ✅ 分离的交易表和余额表                                        │
│  ✅ 信息类型架构 (HR)                                           │
│  ✅ BOM/工艺路线表结构                                          │
│  ✅ 索引策略                                                    │
│                                                                 │
│  从 S/4HANA 借鉴 (现代设计概念):                                │
│  ─────────────────────────────                                 │
│  ✅ BP 业务伙伴模型                                             │
│  ✅ 时间有效性管理 (BEGDA/ENDDA)                                │
│  ✅ 多语言支持架构                                              │
│  ✅ 租户隔离设计                                                │
│  ⚠️ CDS View 概念 (用 PostgreSQL 视图实现)                      │
│  ❌ ACDOCA 宽表 (不适合 PostgreSQL)                             │
│  ❌ 实时计算替代聚合表 (PostgreSQL 性能压力)                    │
│                                                                 │
│  PostgreSQL 特有优化:                                           │
│  ────────────────────────                                       │
│  ✅ JSONB 存储灵活数据                                          │
│  ✅ 表继承实现信息类型                                          │
│  ✅ 数组类型存储期间数据                                        │
│  ✅ 物化视图加速报表                                            │
│  ✅ UUID 主键                                                   │
│  ✅ 生成列 (GENERATED ALWAYS AS)                                │
│  ✅ 分区表处理大数据                                            │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 模块级建议

| 模块 | 推荐借鉴 | 原因 |
|------|---------|------|
| FI/CO | **ECC 为主** | 范式化设计适合 PostgreSQL |
| HR/HCM | **ECC 为主** | 信息类型架构成熟 |
| MM | **ECC 结构** | 物料/采购表设计经典 |
| SD | **ECC 结构** | 销售/交货表设计经典 |
| PP | **ECC 结构** | BOM/工艺路线表经典 |
| BP | **S/4HANA** | 现代统一业务伙伴模型 |
| 租户 | **S/4HANA 概念** | 多租户隔离 |

## PostgreSQL 优化建议

### 1. 利用 JSONB

```sql
-- 灵活扩展字段使用 JSONB
CREATE TABLE material_ext (
    id              UUID PRIMARY KEY,
    material_id     UUID NOT NULL,
    attributes      JSONB,  -- 动态属性
    -- 查询示例: attributes->>'color' = 'red'
    -- 索引: CREATE INDEX ON material_ext USING GIN (attributes)
);
```

### 2. 利用分区表

```sql
-- 大表分区 (如凭证表)
CREATE TABLE journal_entry (
    id              UUID,
    company_code    VARCHAR(4),
    entry_date      DATE,
    fiscal_year     NUMERIC(4),
    -- ...
) PARTITION BY RANGE (fiscal_year);

CREATE TABLE journal_entry_2024 PARTITION OF journal_entry
    FOR VALUES FROM (2024) TO (2025);
```

### 3. 利用生成列

```sql
-- 自动计算字段
CREATE TABLE employee (
    id              UUID PRIMARY KEY,
    first_name      VARCHAR(40),
    last_name       VARCHAR(40),
    full_name       VARCHAR(80) GENERATED ALWAYS AS
        (first_name || ' ' || last_name) STORED
);
```

### 4. 利用 CTE 优化复杂查询

```sql
-- 替代 ECC 的复杂 ABAP 逻辑
WITH period_balance AS (
    SELECT
        account_number,
        SUM(CASE WHEN EXTRACT(MONTH FROM entry_date) = 1 THEN amount ELSE 0 END) AS p01,
        SUM(CASE WHEN EXTRACT(MONTH FROM entry_date) = 2 THEN amount ELSE 0 END) AS p02,
        -- ...
    FROM journal_entry_item
    WHERE fiscal_year = 2024
    GROUP BY account_number
)
SELECT * FROM period_balance;
```

## 总结建议

### 核心原则

1. **基础架构**: 借鉴 ECC 6.0（适合传统关系型数据库）
2. **现代概念**: 借鉴 S/4HANA（BP模型、时间有效性）
3. **PostgreSQL 优化**: 利用特有功能（JSONB、分区、物化视图）

### 不建议采用的 S/4HANA 设计

- ❌ ACDOCA 超级宽表
- ❌ 完全消除聚合表
- ❌ 依赖内存计算的实时聚合

### 建议采用的 S/4HANA 设计

- ✅ BP 业务伙伴模型
- ✅ 时间有效性管理
- ✅ CDS View 概念（用 PostgreSQL 视图）
- ✅ OData API 设计理念

---

**结论**: NextERP + PostgreSQL 应以 **ECC 6.0 设计为基础**，选择性采用 **S/4HANA 的现代概念**，并充分利用 **PostgreSQL 特有功能**进行优化。
