# NextERP 数据库设计

## 概述

NextERP 数据库设计基于 PostgreSQL，采用 **ECC 为主、S/4HANA 概念补充** 的混合策略，充分利用 PostgreSQL 特有功能进行优化。

## 设计原则

### 1. 架构选择

| 方面 | 选择 | 原因 |
|------|------|------|
| 表结构 | ECC 范式化 | PostgreSQL 行存储适配 |
| 业务伙伴 | S/4HANA BP 模型 | 现代统一设计 |
| 时间有效性 | S/4HANA 风格 | BEGDA/ENDDA |
| 聚合表 | 保留 | PostgreSQL 性能需要 |
| 宽表 | 避免 | PG 行存储不适合 |

### 2. PostgreSQL 特性利用

- **UUID** - 主键使用 UUID
- **JSONB** - 灵活扩展字段
- **数组类型** - 期间数据存储
- **表继承** - 信息类型实现
- **生成列** - 自动计算字段
- **分区表** - 大数据量处理
- **物化视图** - 报表加速

### 3. 多租户设计

```sql
-- 所有表包含 tenant_id
tenant_id UUID NOT NULL

-- 行级安全策略
CREATE POLICY tenant_isolation ON table_name
    USING (tenant_id = current_setting('app.current_tenant')::UUID);
```

## 目录结构

```
nexterp-database/
├── README.md                    # 本文档
├── DATABASE-DESIGN.md           # 数据库设计文档
├── OPTIMIZATION-ANALYSIS.md     # 优化分析文档
├── OPTIMIZATION-SUMMARY.md      # 优化总结文档
├── schema/                      # 数据库模式定义
│   ├── 00-core.sql             # 核心表和通用函数
│   ├── 01-tenant.sql           # 多租户
│   ├── 02-business-partner.sql # 业务伙伴
│   ├── 03-fi-co.sql            # 财务会计
│   ├── 04-mm.sql               # 物料管理
│   ├── 05-sd.sql               # 销售分销
│   ├── 06-pp.sql               # 生产计划
│   ├── 07-hr.sql               # 人力资源
│   ├── 08-workflow.sql         # 工作流
│   ├── 99-views.sql            # 视图定义
│   └── optimizations/          # 优化版模式
│       ├── 00-core-optimized.sql
│       ├── 01-tenant-optimized.sql
│       ├── 02-business-partner-optimized.sql
│       ├── 03-fi-co-optimized.sql
│       ├── 04-mm-optimized.sql
│       ├── 07-hr-optimized.sql
│       └── 99-views-optimized.sql
├── diagrams/                    # ER 图
│   └── erd.md                  # Mermaid ERD
└── migrations/                  # 迁移脚本
    └── V1__initial_schema.sql  # 初始化脚本
```

## 优化版 Schema

优化版 schema 提供以下增强功能：

| 优化项 | 说明 |
|--------|------|
| 表分区 | 会计凭证、采购订单、薪酬结果按年度分区 |
| 统一审计 | 自动处理 created_at/updated_at/version |
| 乐观锁 | 防止并发更新冲突 |
| 时间约束 | 排他约束防止时间重叠 |
| 全文搜索 | 业务伙伴、物料全文搜索支持 |
| 生成列 | 可用库存、开放数量自动计算 |
| RLS | 行级安全多租户隔离 |
| 库存快照 | 月度库存快照优化报表 |

详细说明请参考 [OPTIMIZATION-SUMMARY.md](./OPTIMIZATION-SUMMARY.md)

## 模块概览

### 核心模块

| 模块 | 说明 | 参考 |
|------|------|------|
| Core | 通用表、枚举、函数 | - |
| Tenant | 多租户、公司、工厂 | SAP T001 |
| BP | 业务伙伴 (客户/供应商) | SAP BUT000 |
| FI/CO | 财务会计、成本控制 | SAP BKPF/BSEG |
| MM | 物料、采购、库存 | SAP MARA/EKKO |
| SD | 销售、交货、开票 | SAP VBAK/LIKP |
| PP | BOM、工艺、生产订单 | SAP STKO/AUFK |
| HR | 组织、员工、薪酬 | SAP PAxxxx |
| Workflow | 审批流程 | - |

## 表命名规范

### 命名规则

```
{module}_{entity}[_{suffix}]

module: bp, fi, mm, sd, pp, hr, wf, sys
entity: 主数据名 (partner, material, order, etc.)
suffix: _hdr (头), _itm (项), _bal (余额), _ext (扩展)
```

### 示例

| 表名 | 说明 |
|------|------|
| `bp_partner` | 业务伙伴主表 |
| `bp_partner_role` | 业务伙伴角色 |
| `fi_journal_entry_hdr` | 凭证头 |
| `fi_journal_entry_itm` | 凭证项 |
| `mm_material` | 物料主数据 |
| `mm_purchase_order_hdr` | 采购订单头 |
| `sd_sales_order_hdr` | 销售订单头 |
| `pp_bom_hdr` | BOM 头 |
| `hr_employee` | 员工主数据 |
| `hr_infotype_0001` | 信息类型 0001 |

## 字段命名规范

### 通用字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `id` | UUID | 主键 |
| `code` | VARCHAR | 业务编码 |
| `name` | VARCHAR | 名称 |
| `description` | TEXT | 描述 |
| `tenant_id` | UUID | 租户 ID |
| `valid_from` | DATE | 有效起始日 |
| `valid_to` | DATE | 有效截止日 |
| `status` | VARCHAR(20) | 状态 |
| `created_at` | TIMESTAMP | 创建时间 |
| `updated_at` | TIMESTAMP | 更新时间 |
| `created_by` | UUID | 创建人 |
| `updated_by` | UUID | 更新人 |
| `version` | INTEGER | 乐观锁版本 |

### 金额字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `amount` | DECIMAL(23,2) | 金额 |
| `amount_dc` | DECIMAL(23,2) | 本位币金额 |
| `currency` | VARCHAR(3) | 货币代码 |

### 数量字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `quantity` | DECIMAL(18,6) | 数量 |
| `uom` | VARCHAR(3) | 单位 |

## 索引策略

### 主键索引

```sql
-- UUID 主键
id UUID PRIMARY KEY DEFAULT gen_random_uuid()
```

### 业务键索引

```sql
-- 唯一业务编码
CREATE UNIQUE INDEX idx_{table}_code ON {table} (tenant_id, code)
    WHERE valid_to >= CURRENT_DATE;
```

### 查询优化索引

```sql
-- 常用查询字段
CREATE INDEX idx_{table}_{column} ON {table} (tenant_id, {column});

-- 复合索引
CREATE INDEX idx_{table}_query ON {table} (tenant_id, status, valid_from);
```

## 快速开始

### 1. 创建数据库

```sql
CREATE DATABASE nexterp
    WITH
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8';
```

### 2. 启用扩展

```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";    -- 模糊搜索
CREATE EXTENSION IF NOT EXISTS "btree_gist"; -- 排他约束（优化版需要）
```

### 3. 执行初始化脚本

#### 原始版 Schema

```bash
psql -d nexterp -f schema/00-core.sql
psql -d nexterp -f schema/01-tenant.sql
psql -d nexterp -f schema/02-business-partner.sql
# ... 按顺序执行
```

#### 优化版 Schema（推荐用于生产环境）

```bash
# 1. 核心函数（必须首先执行）
psql -d nexterp -f schema/optimizations/00-core-optimized.sql

# 2. 基础表
psql -d nexterp -f schema/optimizations/01-tenant-optimized.sql
psql -d nexterp -f schema/optimizations/02-business-partner-optimized.sql

# 3. 业务模块
psql -d nexterp -f schema/optimizations/03-fi-co-optimized.sql
psql -d nexterp -f schema/optimizations/04-mm-optimized.sql
psql -d nexterp -f schema/optimizations/07-hr-optimized.sql

# 4. 视图
psql -d nexterp -f schema/optimizations/99-views-optimized.sql
```

## 参考文档

- [数据库设计文档](./DATABASE-DESIGN.md) - 完整的数据库设计说明
- [优化分析](./OPTIMIZATION-ANALYSIS.md) - 优化问题分析
- [优化总结](./OPTIMIZATION-SUMMARY.md) - 已实施的优化说明
- [PostgreSQL 设计指南](../sap-database/nexterp-postgresql-design-guide.md)
- [SAP ECC vs S/4HANA 对比](../sap-database/migration/ecc-vs-s4hana-comparison.md)
- [SAP ECC HR 数据库设计](../sap-database/ecc/hr/README.md)
- [SAP ECC FI/CO 数据库设计](../sap-database/ecc/fi-co/README.md)
