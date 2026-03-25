# NextERP 数据库设计文档

## 文档信息

| 项目 | 内容 |
|------|------|
| 版本 | 1.0 |
| 数据库 | PostgreSQL 15+ |
| 更新日期 | 2026-03-13 |

---

## 1. 概述

### 1.1 设计理念

NextERP 数据库设计借鉴 SAP ERP 系统的设计思想，结合 PostgreSQL 特性进行优化：

- **SAP ECC 基础**: 采用 ECC 的范式化表结构，适合 PostgreSQL 行存储
- **S/4HANA 概念**: 引入统一业务伙伴模型、时间有效性管理
- **PostgreSQL 优化**: 利用分区表、JSONB、生成列、全文搜索等特性

### 1.2 架构特点

```
┌─────────────────────────────────────────────────────────────┐
│                      NextERP 架构                            │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   Core      │  │   Tenant    │  │    BP       │  基础层  │
│  │  (核心)     │  │  (多租户)   │  │ (业务伙伴)  │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   FI/CO     │  │     MM      │  │     SD      │  业务层  │
│  │  (财务)     │  │  (物料)     │  │  (销售)     │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │     PP      │  │     HR      │  │  Workflow   │         │
│  │  (生产)     │  │  (人力)     │  │  (工作流)   │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. 多租户设计

### 2.1 租户隔离策略

采用**共享数据库、共享 Schema** 模式，通过 `tenant_id` 和行级安全(RLS)实现隔离。

```sql
-- 所有业务表包含 tenant_id
tenant_id UUID NOT NULL REFERENCES sys_tenant(id)

-- 行级安全策略
CREATE POLICY tenant_isolation ON table_name
    USING (tenant_id = current_setting('app.current_tenant', TRUE)::UUID);
```

### 2.2 组织架构

```
sys_tenant (租户)
    │
    ├── sys_company (公司代码)
    │       │
    │       ├── sys_plant (工厂)
    │       │       │
    │       │       └── sys_storage_location (库存地点)
    │       │
    │       ├── sys_sales_organization (销售组织)
    │       │       │
    │       │       └── sys_sales_area (销售范围)
    │       │
    │       └── sys_purchasing_organization (采购组织)
    │
    └── sys_controlling_area (成本控制范围)
            │
            ├── sys_cost_center (成本中心)
            └── sys_profit_center (利润中心)
```

---

## 3. 核心数据模型

### 3.1 业务伙伴 (Business Partner)

借鉴 SAP S/4HANA 的统一业务伙伴模型，客户、供应商、员工共享主数据。

```
bp_partner (业务伙伴主表)
    │
    ├── bp_partner_role (角色)
    │       │
    │       ├── FLCU00: 客户
    │       ├── FLVN00: 供应商
    │       └── BUR011: 员工
    │
    ├── bp_address (地址)
    ├── bp_bank_account (银行账户)
    ├── bp_contact_person (联系人)
    │
    ├── bp_customer_company (客户-公司数据)
    ├── bp_customer_sales (客户-销售数据)
    │
    ├── bp_supplier_company (供应商-公司数据)
    └── bp_supplier_purchasing (供应商-采购数据)
```

**关键设计**:

| 字段 | 类型 | 说明 |
|------|------|------|
| partner_type | VARCHAR(2) | 1=组织, 2=个人, 3=组 |
| valid_from/to | DATE | 时间有效性 |
| search_vector | TSVECTOR | 全文搜索向量 |

### 3.2 财务会计 (FI/CO)

参考 SAP BKPF/BSEG 凭证结构。

```
fi_chart_of_accounts (科目表)
    │
    ├── fi_account_group (科目组)
    │
    └── fi_gl_account (总账科目)
            │
            ├── fi_account_balance (科目余额)
            │
            └── fi_journal_entry_hdr (凭证头) [分区表]
                    │
                    └── fi_journal_entry_itm (凭证项)
```

**凭证表结构**:

```
fi_journal_entry_hdr (凭证头)
├── id              UUID        主键
├── fiscal_year     INTEGER     分区键
├── company_id      UUID        公司
├── document_number VARCHAR(10) 凭证号
├── document_date   DATE        凭证日期
├── posting_date    DATE        过账日期
├── period          INTEGER     期间
├── currency_id     UUID        货币
├── status          ENUM        状态
└── ...

fi_journal_entry_itm (凭证项)
├── id              UUID        主键
├── header_id       UUID        凭证头
├── fiscal_year     INTEGER     年度
├── line_item       INTEGER     行号
├── account_id      UUID        科目
├── debit_credit    ENUM        借贷标识
├── amount          DECIMAL     金额
├── cost_center_id  UUID        成本中心
└── ...
```

**余额表结构** (优化版):

```sql
-- 使用独立字段存储各期间余额
period_01_balance, period_01_debit, period_01_credit,
period_02_balance, period_02_debit, period_02_credit,
...
period_12_balance, period_12_debit, period_12_credit,
year_balance, year_debit, year_credit
```

### 3.3 物料管理 (MM)

```
mm_material_type (物料类型)
mm_material_group (物料组)
    │
    └── mm_material (物料主数据)
            │
            ├── mm_material_plant (物料-工厂数据)
            ├── mm_material_valuation (物料评估)
            └── mm_material_storage (物料库存)

mm_purchase_requisition_hdr (采购申请头)
    └── mm_purchase_requisition_itm (采购申请项)

mm_purchase_order_hdr (采购订单头) [分区表]
    └── mm_purchase_order_itm (采购订单项)

mm_material_document_hdr (物料凭证头) [分区表]
    └── mm_material_document_itm (物料凭证项)
```

### 3.4 人力资源 (HR)

采用 SAP Infotype 架构，按信息类型组织数据。

```
hr_org_unit (组织单位)
hr_job (职务)
hr_position (职位)
    │
    └── hr_employee (员工主数据)
            │
            ├── hr_it0001_org_assignment (IT0001 组织分配)
            ├── hr_it0002_personal_data (IT0002 个人数据)
            ├── hr_it0006_address (IT0006 地址)
            ├── hr_it0008_basic_pay (IT0008 基本工资)
            │       └── hr_it0008_wage_item (工资项明细)
            ├── hr_it0009_bank_details (IT0009 银行信息)
            ├── hr_it0016_contract (IT0016 合同)
            ├── hr_it0021_family (IT0021 家庭成员)
            ├── hr_it0022_education (IT0022 教育经历)
            │
            └── hr_it2001_absence (IT2001 请假)

hr_wage_type (工资类型定义)
hr_leave_type (请假类型)
hr_leave_quota (请假额度)

hr_payroll_result (薪酬结果) [分区表]
    └── hr_payroll_item (薪酬项明细)
```

---

## 4. 时间有效性管理

### 4.1 设计模式

所有主数据和 HR 信息类型都支持时间有效性：

```sql
valid_from DATE NOT NULL DEFAULT CURRENT_DATE,
valid_to DATE NOT NULL DEFAULT '9999-12-31'
```

### 4.2 排他约束

使用 PostgreSQL 排他约束防止时间重叠：

```sql
ALTER TABLE hr_it0008_basic_pay
ADD CONSTRAINT uk_hr_it0008_no_overlap
EXCLUDE USING GIST (
    employee_id WITH =,
    daterange(valid_from, COALESCE(valid_to, '9999-12-31'), '[]') WITH &&
);
```

### 4.3 有效记录查询

```sql
-- 查询当前有效记录
SELECT * FROM hr_it0008_basic_pay
WHERE employee_id = :emp_id
  AND valid_from <= CURRENT_DATE
  AND valid_to >= CURRENT_DATE;

-- 使用 LATERAL 优化关联查询
SELECT e.*, pay.*
FROM hr_employee e
LEFT JOIN LATERAL (
    SELECT * FROM hr_it0008_basic_pay
    WHERE employee_id = e.id
      AND valid_from <= CURRENT_DATE
      AND valid_to >= CURRENT_DATE
    LIMIT 1
) pay ON TRUE;
```

---

## 5. 表分区策略

### 5.1 分区表清单

| 表名 | 分区键 | 分区类型 | 说明 |
|------|--------|----------|------|
| fi_journal_entry_hdr | fiscal_year | RANGE | 会计凭证 |
| mm_purchase_order_hdr | fiscal_year | RANGE | 采购订单 |
| mm_material_document_hdr | fiscal_year | RANGE | 物料凭证 |
| hr_payroll_result | payroll_year | RANGE | 薪酬结果 |
| mm_inventory_snapshot | snapshot_year | RANGE | 库存快照 |

### 5.2 分区示例

```sql
CREATE TABLE fi_journal_entry_hdr (
    id UUID NOT NULL,
    fiscal_year INTEGER NOT NULL,
    ...
    PRIMARY KEY (fiscal_year, id)
) PARTITION BY RANGE (fiscal_year);

-- 创建分区
CREATE TABLE fi_journal_entry_hdr_2024
    PARTITION OF fi_journal_entry_hdr
    FOR VALUES FROM (2024) TO (2025);

CREATE TABLE fi_journal_entry_hdr_2025
    PARTITION OF fi_journal_entry_hdr
    FOR VALUES FROM (2025) TO (2026);

-- 默认分区
CREATE TABLE fi_journal_entry_hdr_default
    PARTITION OF fi_journal_entry_hdr DEFAULT;
```

### 5.3 分区维护

```sql
-- 年度维护：创建新年分区
CREATE TABLE fi_journal_entry_hdr_2026
    PARTITION OF fi_journal_entry_hdr
    FOR VALUES FROM (2026) TO (2027);

-- 归档旧数据
ALTER TABLE fi_journal_entry_hdr_2020 DETACH PARTITION;
```

---

## 6. 审计与乐观锁

### 6.1 审计字段

所有业务表包含标准审计字段：

```sql
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
created_by UUID,
updated_by UUID,
version INTEGER DEFAULT 0
```

### 6.2 统一审计触发器

```sql
CREATE OR REPLACE FUNCTION audit_trigger_func()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;

    IF TG_OP = 'INSERT' THEN
        NEW.created_at = CURRENT_TIMESTAMP;
        NEW.version = 0;
    END IF;

    IF TG_OP = 'UPDATE' THEN
        -- 乐观锁检查
        IF OLD.version IS DISTINCT FROM NEW.version THEN
            RAISE EXCEPTION '乐观锁冲突: 表 %, ID %', TG_TABLE_NAME, OLD.id;
        END IF;
        NEW.version = OLD.version + 1;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 应用触发器
CREATE TRIGGER trigger_audit
    BEFORE INSERT OR UPDATE ON table_name
    FOR EACH ROW EXECUTE FUNCTION audit_trigger_func();
```

---

## 7. 全文搜索

### 7.1 搜索向量

```sql
-- 业务伙伴搜索向量
search_vector TSVECTOR GENERATED ALWAYS AS (
    setweight(to_tsvector('simple', COALESCE(full_name, '')), 'A') ||
    setweight(to_tsvector('simple', COALESCE(organization_name, '')), 'A') ||
    setweight(to_tsvector('simple', COALESCE(search_term, '')), 'B') ||
    setweight(to_tsvector('simple', COALESCE(partner_number, '')), 'C')
) STORED
```

### 7.2 GIN 索引

```sql
CREATE INDEX idx_bp_partner_search_vector
    ON bp_partner USING GIN (search_vector);
```

### 7.3 搜索函数

```sql
CREATE OR REPLACE FUNCTION search_bp_partner(
    p_tenant_id UUID,
    p_query TEXT,
    p_limit INTEGER DEFAULT 20
) RETURNS TABLE (...) AS $$
BEGIN
    RETURN QUERY
    SELECT ...
    FROM bp_partner bp
    WHERE bp.tenant_id = p_tenant_id
      AND bp.search_vector @@ plainto_tsquery('simple', p_query)
    ORDER BY ts_rank(bp.search_vector, plainto_tsquery('simple', p_query)) DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;
```

---

## 8. 生成列

### 8.1 自动计算字段

```sql
-- 可用库存
available_stock DECIMAL(18,6) GENERATED ALWAYS AS (
    unrestricted_stock - in_transit_stock
) STORED

-- 开放数量
open_quantity DECIMAL(18,6) GENERATED ALWAYS AS (
    quantity - quantity_delivered
) STORED

-- 综合评分
overall_score DECIMAL(3,2) GENERATED ALWAYS AS (
    (COALESCE(quality_score, 0) +
     COALESCE(delivery_score, 0) +
     COALESCE(price_score, 0)) / 3
) STORED

-- 全名
full_name VARCHAR(80) GENERATED ALWAYS AS (
    COALESCE(NULLIF(first_name, '') || ' ', '') ||
    COALESCE(last_name, '')
) STORED
```

---

## 9. 枚举类型

```sql
-- 通用状态
CREATE TYPE general_status AS ENUM (
    'ACTIVE', 'INACTIVE', 'DRAFT', 'PENDING',
    'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED', 'CLOSED'
);

-- 借贷标识
CREATE TYPE debit_credit AS ENUM ('D', 'C');

-- 性别
CREATE TYPE gender AS ENUM ('M', 'F', 'O');

-- 审批状态
CREATE TYPE approval_status AS ENUM (
    'DRAFT', 'PENDING', 'APPROVED',
    'REJECTED', 'CANCELLED', 'WITHDRAWN'
);
```

---

## 10. 索引策略

### 10.1 索引类型

| 类型 | 使用场景 | 示例 |
|------|----------|------|
| B-Tree | 等值查询、范围查询 | `tenant_id`, `code` |
| GIN | 全文搜索、JSONB | `search_vector` |
| GiST | 排他约束、几何 | 时间重叠检查 |

### 10.2 索引命名规范

```sql
-- 单列索引
idx_{table}_{column}

-- 复合索引
idx_{table}_{col1}_{col2}

-- 唯一索引
uk_{table}_{columns}

-- 条件索引
idx_{table}_{column}_active WHERE status = 'ACTIVE'
```

### 10.3 关键索引

```sql
-- 租户隔离
CREATE INDEX idx_{table}_tenant ON {table} (tenant_id);

-- 时间有效性
CREATE INDEX idx_{table}_valid ON {table} (tenant_id, valid_from, valid_to);

-- 业务编码
CREATE UNIQUE INDEX idx_{table}_code ON {table} (tenant_id, code);

-- 全文搜索
CREATE INDEX idx_{table}_search ON {table} USING GIN (search_vector);
```

---

## 11. 视图设计

### 11.1 业务视图

借鉴 SAP CDS View 概念，创建业务语义视图：

```sql
-- 客户视图
CREATE VIEW v_customer AS
SELECT p.*, cc.*, cs.*, a.*
FROM bp_partner p
JOIN bp_partner_role r ON ...
LEFT JOIN bp_customer_company cc ON ...
LEFT JOIN bp_customer_sales cs ON ...
LEFT JOIN bp_address a ON ...;

-- 员工视图
CREATE VIEW v_employee AS
SELECT e.*, org.*, pos.*, pay.*
FROM hr_employee e
LEFT JOIN LATERAL (...) org ON TRUE
LEFT JOIN LATERAL (...) pos ON TRUE
LEFT JOIN LATERAL (...) pay ON TRUE;
```

### 11.2 物化视图

用于报表加速：

```sql
CREATE MATERIALIZED VIEW mv_customer_balance AS
SELECT partner_id, company_id, fiscal_year,
       year_balance, year_debit, year_credit
FROM bp_partner p
JOIN fi_partner_balance pb ON ...
WHERE p.status = 'ACTIVE';

-- 定时刷新
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_customer_balance;
```

---

## 12. 数据字典

### 12.1 模块前缀

| 前缀 | 模块 | 说明 |
|------|------|------|
| `core_` | Core | 核心基础数据 |
| `sys_` | System | 系统配置、组织 |
| `bp_` | Business Partner | 业务伙伴 |
| `fi_` | Financial | 财务会计 |
| `co_` | Controlling | 成本控制 |
| `mm_` | Material Management | 物料管理 |
| `sd_` | Sales Distribution | 销售分销 |
| `pp_` | Production Planning | 生产计划 |
| `hr_` | Human Resources | 人力资源 |
| `wf_` | Workflow | 工作流 |

### 12.2 表后缀

| 后缀 | 说明 | 示例 |
|------|------|------|
| `_hdr` | 单据头 | `fi_journal_entry_hdr` |
| `_itm` | 单据项 | `fi_journal_entry_itm` |
| `_ext` | 扩展表 | `bp_customer_company` |
| `_bal` | 余额表 | `fi_account_balance` |
| `_type` | 类型表 | `mm_material_type` |
| `_group` | 分组表 | `mm_material_group` |

### 12.3 常用字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `id` | UUID | 主键 |
| `tenant_id` | UUID | 租户ID |
| `code` | VARCHAR | 业务编码 |
| `name` | VARCHAR | 名称 |
| `name_en` | VARCHAR | 英文名 |
| `description` | TEXT | 描述 |
| `status` | ENUM | 状态 |
| `valid_from` | DATE | 有效起始 |
| `valid_to` | DATE | 有效截止 |
| `currency_id` | UUID | 货币 |
| `amount` | DECIMAL(23,2) | 金额 |
| `quantity` | DECIMAL(18,6) | 数量 |
| `uom_id` | UUID | 单位 |

---

## 13. 安全设计

### 13.1 行级安全 (RLS)

```sql
-- 启用 RLS
ALTER TABLE sys_company ENABLE ROW LEVEL SECURITY;

-- 创建策略
CREATE POLICY rls_sys_company_tenant ON sys_company
    USING (tenant_id = current_setting('app.current_tenant', TRUE)::UUID);

-- 应用程序连接时设置
SET app.current_tenant = '租户UUID';
```

### 13.2 字段级加密

敏感数据使用 pgcrypto 加密：

```sql
-- 启用扩展
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 加密存储
INSERT INTO table (sensitive_data)
VALUES (pgp_sym_encrypt('敏感信息', '密钥'));

-- 解密查询
SELECT pgp_sym_decrypt(sensitive_data, '密钥') FROM table;
```

---

## 14. 性能优化建议

### 14.1 查询优化

1. **使用分区裁剪**: 查询时包含分区键条件
2. **避免 SELECT ***: 只查询需要的字段
3. **使用 LATERAL**: 优化 N+1 查询
4. **使用 EXISTS**: 替代 IN 子查询

### 14.2 索引优化

1. **选择性高的字段优先**: 高区分度字段建索引
2. **复合索引顺序**: 等值字段在前，范围字段在后
3. **部分索引**: 对活跃数据建索引

### 14.3 分区维护

```sql
-- 定期创建新分区
-- 年度开始前创建新年分区

-- 归档旧分区
ALTER TABLE fi_journal_entry_hdr_2020 DETACH PARTITION;

-- 清理默认分区数据
-- 定期检查并迁移到正确分区
```

---

## 15. 部署清单

### 15.1 必需扩展

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";     -- UUID 生成
CREATE EXTENSION IF NOT EXISTS "pg_trgm";       -- 模糊搜索
CREATE EXTENSION IF NOT EXISTS "btree_gist";    -- 排他约束
CREATE EXTENSION IF NOT EXISTS "pgcrypto";      -- 加密（可选）
```

### 15.2 初始化顺序

```bash
# 1. 核心函数
psql -f 00-core-optimized.sql

# 2. 基础表
psql -f 01-tenant-optimized.sql
psql -f 02-business-partner-optimized.sql

# 3. 业务模块
psql -f 03-fi-co-optimized.sql
psql -f 04-mm-optimized.sql
psql -f 07-hr-optimized.sql

# 4. 视图
psql -f 99-views-optimized.sql
```

---

## 附录 A: ER 图

详细的 ER 图请参考 [diagrams/erd.md](./diagrams/erd.md)。

## 附录 B: 与 SAP 表对照

| NextERP 表 | SAP ECC 表 | 说明 |
|------------|------------|------|
| bp_partner | BUT000 | 业务伙伴 |
| fi_gl_account | SKA1/SKB1 | 总账科目 |
| fi_journal_entry_hdr | BKPF | 凭证头 |
| fi_journal_entry_itm | BSEG | 凭证项 |
| fi_account_balance | GLT0 | 科目余额 |
| mm_material | MARA | 物料主数据 |
| mm_material_plant | MARC | 物料工厂 |
| mm_purchase_order_hdr | EKKO | 采购订单头 |
| mm_purchase_order_itm | EKPO | 采购订单项 |
| hr_employee | PA0001 | 员工主数据 |
| hr_it0008_basic_pay | PA0008 | 基本工资 |

## 附录 C: 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0 | 2026-03-13 | 初始版本，包含核心模块设计 |
