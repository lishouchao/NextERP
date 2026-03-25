# NextERP 数据库设计文档

**版本**: 1.0
**日期**: 2026-03-14
**数据库**: PostgreSQL 15+
**架构师**: NextERP Team

---

## 1. 文档概述

### 1.1 目的

本文档定义 NextERP 企业资源规划系统的数据库架构设计，涵盖：
- 数据库架构与设计原则
- 逻辑数据模型与物理数据模型
- 详细表结构定义
- 索引策略与分区策略
- 安全模型与性能优化

### 1.2 范围

| 模块 | 文档 | 说明 | 对标 SAP |
|------|------|------|----------|
| CORE | [00-CORE-DESIGN.md](./00-CORE-DESIGN.md) | 核心基础数据 | - |
| TENANT | [01-TENANT-DESIGN.md](./01-TENANT-DESIGN.md) | 多租户与组织架构 | 公司代码/工厂 |
| BP | [02-BP-DESIGN.md](./02-BP-DESIGN.md) | 业务伙伴 | S/4HANA BP |
| FI | [03-FICO-DESIGN.md](./03-FICO-DESIGN.md) | 财务会计 | ECC FI/CO + ACDOCA |
| MM | [04-MM-DESIGN.md](./04-MM-DESIGN.md) | 物料管理 | ECC MM |
| SD | [05-SD-DESIGN.md](./05-SD-DESIGN.md) | 销售分销 | ECC SD |
| PP | [06-PP-DESIGN.md](./06-PP-DESIGN.md) | 生产计划 | ECC PP |
| HR | [07-HR-DESIGN.md](./07-HR-DESIGN.md) | 人力资源 | ECC HCM |
| AM | [08-AM-DESIGN.md](./08-AM-DESIGN.md) | 资产管理 | ECC AA |
| PS | [09-PS-DESIGN.md](./09-PS-DESIGN.md) | 项目系统 | ECC PS |
| QM | [10-QM-DESIGN.md](./10-QM-DESIGN.md) | 质量管理 | ECC QM |
| PM | [11-PM-DESIGN.md](./11-PM-DESIGN.md) | 工厂维护 | ECC PM |
| WM | [12-WM-DESIGN.md](./12-WM-DESIGN.md) | 仓库管理 | ECC WM |
| CO | [13-CO-DESIGN.md](./13-CO-DESIGN.md) | 管理会计 | ECC CO |
| TR | [14-TR-DESIGN.md](./14-TR-DESIGN.md) | 资金管理 | ECC TR |
| CRM | [15-CRM-DESIGN.md](./15-CRM-DESIGN.md) | 客户关系管理 | SAP CRM |
| SCM | [16-SCM-DESIGN.md](./16-SCM-DESIGN.md) | 供应链管理 | SAP SCM |

### 1.3 设计原则

```
┌─────────────────────────────────────────────────────────────────────┐
│                      NextERP 数据库设计原则                          │
├─────────────────────────────────────────────────────────────────────┤
│  1. 多租户架构     - tenant_id 隔离 + RLS 行级安全                   │
│  2. 时间有效性     - valid_from/valid_to (SAP InfoType 风格)        │
│  3. 审计追踪       - created_at/updated_at/created_by/updated_by    │
│  4. 乐观锁         - version 字段防止并发冲突                        │
│  5. 软删除         - is_deleted 标记，保留历史数据                   │
│  6. UUID 主键      - gen_random_uuid() 分布式友好                   │
│  7. 分区表         - 按年度/租户分区，提升查询性能                   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. 架构概览

### 2.1 技术栈

| 组件 | 选型 | 说明 |
|------|------|------|
| 数据库 | PostgreSQL 15+ | 企业级开源关系数据库 |
| 主键 | UUID v4 | 分布式环境友好 |
| 分区 | RANGE/LIST | 按年度、租户分区 |
| 安全 | RLS | 行级安全策略 |
| 扩展 | pgcrypto, uuid-ossp | 加密、UUID生成 |

### 2.2 Schema 结构图

```
                    ┌─────────────────────────────────────────┐
                    │              NextERP Database            │
                    └─────────────────────────────────────────┘
                                       │
        ┌──────────────┬───────────────┼───────────────┬──────────────┐
        ▼              ▼               ▼               ▼              ▼
   ┌─────────┐   ┌──────────┐   ┌──────────┐   ┌─────────┐   ┌─────────┐
   │ 00-core │   │ 01-tenant│   │ 02-bp    │   │ 03-fico │   │ 04-mm   │
   │ 核心基础 │   │ 组织架构 │   │ 业务伙伴 │   │ 财务会计 │   │ 物料管理 │
   └─────────┘   └──────────┘   └──────────┘   └─────────┘   └─────────┘
        │              │               │               │              │
        └──────────────┴───────────────┴───────────────┴──────────────┘
                                       │
        ┌──────────────┬───────────────┼───────────────┐
        ▼              ▼               ▼               ▼
   ┌─────────┐   ┌──────────┐   ┌──────────┐   ┌─────────┐
   │ 07-hr   │   │ 08-sd    │   │ 09-pp    │   │ 99-views│
   │ 人力资源 │   │ 销售分销 │   │ 生产计划 │   │ 视图函数 │
   └─────────┘   └──────────┘   └──────────┘   └─────────┘
```

### 2.3 模块依赖关系

```
00-core (基础类型、枚举、函数)
    │
    ├──► 01-tenant (租户、公司、成本中心)
    │       │
    │       └──► 02-business-partner (客户、供应商)
    │               │
    │               ├──► 03-fi-co (凭证、余额)
    │               │
    │               ├──► 04-mm (物料、采购)
    │               │
    │               ├──► 07-hr (员工、组织)
    │               │
    │               └──► 08-sd (销售订单)
    │
    └──► 99-views (报表视图)
```

---

## 3. 核心数据类型

### 3.1 枚举类型定义

```sql
-- 通用状态
CREATE TYPE general_status AS ENUM ('ACTIVE', 'INACTIVE', 'DELETED');

-- 审批状态
CREATE TYPE approval_status AS ENUM ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'CANCELLED');

-- 性别
CREATE TYPE gender AS ENUM ('M', 'F', 'O');  -- 男/女/其他

-- 凭证状态
CREATE TYPE document_status AS ENUM ('DRAFT', 'PENDING', 'POSTED', 'REVERSED', 'ARCHIVED');
```

### 3.2 标准字段约定

每个业务表应包含以下标准字段：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | UUID | 主键 |
| tenant_id | UUID | 租户ID (多租户隔离) |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |
| created_by | UUID | 创建人 |
| updated_by | UUID | 更新人 |
| version | INTEGER | 版本号 (乐观锁) |
| is_deleted | BOOLEAN | 软删除标记 |

---

## 4. 模块详细设计

详细设计文档按模块分开：

| 文档 | 模块 | 主要内容 |
|------|------|----------|
| [00-CORE-DESIGN.md](./00-CORE-DESIGN.md) | 核心基础 | 枚举类型、国家/货币/语言、编号范围、审计触发器、用户权限 |
| [01-TENANT-DESIGN.md](./01-TENANT-DESIGN.md) | 多租户 | 租户、公司代码、工厂、成本中心、利润中心 |
| [02-BP-DESIGN.md](./02-BP-DESIGN.md) | 业务伙伴 | BP主数据、角色、地址、银行账户、客户/供应商属性 |
| [03-FICO-DESIGN.md](./03-FICO-DESIGN.md) | 财务会计 | 会计科目、凭证、余额、过账、冲销 |
| [04-MM-DESIGN.md](./04-MM-DESIGN.md) | 物料管理 | 物料主数据、采购申请、采购订单、库存移动 |
| [05-SD-DESIGN.md](./05-SD-DESIGN.md) | 销售分销 | 销售订单、交货单、开票、定价、信用管理 |
| [06-PP-DESIGN.md](./06-PP-DESIGN.md) | 生产计划 | BOM、工艺路线、工作中心、生产订单、MRP |
| [07-HR-DESIGN.md](./07-HR-DESIGN.md) | 人力资源 | 组织管理、员工主数据、InfoType架构、薪酬 |
| [08-AM-DESIGN.md](./08-AM-DESIGN.md) | 资产管理 | 资产分类、资产主数据、折旧、资产业务 |
| [09-PS-DESIGN.md](./09-PS-DESIGN.md) | 项目系统 | 项目定义、WBS、网络活动、里程碑、项目预算 |
| [10-QM-DESIGN.md](./10-QM-DESIGN.md) | 质量管理 | 检验计划、检验批、检验结果、使用决策 |
| [11-PM-DESIGN.md](./11-PM-DESIGN.md) | 工厂维护 | 功能位置、设备、维护订单、预防性维护 |
| [12-WM-DESIGN.md](./12-WM-DESIGN.md) | 仓库管理 | 仓库号、存储类型、仓位、转运需求、转运单 |
| [13-CO-DESIGN.md](./13-CO-DESIGN.md) | 管理会计 | 成本要素、成本中心、内部订单、利润中心、产品成本 |
| [14-TR-DESIGN.md](./14-TR-DESIGN.md) | 资金管理 | 银行账户、银行对账、现金头寸、流动性预测 |
| [15-CRM-DESIGN.md](./15-CRM-DESIGN.md) | 客户关系 | 线索、商机、客户360°、活动管理、销售管道 |
| [16-SCM-DESIGN.md](./16-SCM-DESIGN.md) | 供应链 | 需求预测、供应计划、库存优化、物流管理、供应商协同 |

---

## 5. 索引策略

### 5.1 索引类型使用规范

| 索引类型 | 使用场景 | 示例 |
|----------|----------|------|
| B-Tree | 等值查询、范围查询 | 默认类型 |
| Hash | 仅等值查询 | `WHERE code = 'XXX'` |
| GIN | JSONB、数组、全文搜索 | JSONB 字段 |
| GiST | 范围、几何、排除约束 | 时间重叠检查 |

### 5.2 索引命名规范

```
idx_{表名}_{字段名}          -- 普通索引
uk_{表名}_{字段名}           -- 唯一索引
fk_{表名}_{字段名}           -- 外键索引
```

---

## 6. 分区策略

### 6.1 分区表设计

| 表名 | 分区方式 | 分区键 | 说明 |
|------|----------|--------|------|
| fi_journal_entry_hdr | RANGE | fiscal_year | 按会计年度分区 |
| fi_journal_entry_itm | RANGE | fiscal_year | 按会计年度分区 |
| mm_material_document | RANGE | fiscal_year | 按会计年度分区 |
| hr_time_record | RANGE | record_date | 按日期分区 |

### 6.2 分区管理

```sql
-- 自动创建新年度分区 (建议使用 pg_partman 扩展)
CREATE TABLE fi_journal_entry_hdr_2027
    PARTITION OF fi_journal_entry_hdr
    FOR VALUES FROM (2027) TO (2028);
```

---

## 7. 安全模型

### 7.1 行级安全 (RLS)

```sql
-- 启用 RLS
ALTER TABLE hr_employee ENABLE ROW LEVEL SECURITY;

-- 创建策略：用户只能看到自己租户的数据
CREATE POLICY tenant_isolation ON hr_employee
    USING (tenant_id = current_setting('app.current_tenant_id')::UUID);
```

### 7.2 数据加密

- 敏感字段使用 pgcrypto 加密
- 连接使用 SSL/TLS
- 密码使用 bcrypt 哈希

---

## 8. 性能优化

### 8.1 查询优化建议

1. **使用覆盖索引** - 减少回表查询
2. **避免 SELECT *** - 只查询需要的字段
3. **合理使用 JOIN** - 避免过多表关联
4. **使用 EXPLAIN ANALYZE** - 分析执行计划

### 8.2 连接池配置

```
最小连接数: 10
最大连接数: 100
连接超时: 30s
空闲超时: 300s
```

---

## 9. 备份与恢复

### 9.1 备份策略

| 类型 | 频率 | 保留期 |
|------|------|--------|
| 全量备份 | 每日 | 30 天 |
| 增量备份 | 每小时 | 7 天 |
| WAL 归档 | 实时 | 7 天 |

### 9.2 恢复流程

```bash
# PITR 恢复到指定时间点
pg_restore --target-time="2026-03-14 10:00:00" nexterp_backup.tar
```

---

## 10. 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|----------|
| 1.0 | 2026-03-14 | NextERP Team | 初始版本 |

---

## 附录

### A. 参考资料

- SAP ECC 6.0 数据库设计
- SAP S/4HANA ACDOCA 统一日记账
- PostgreSQL 15 官方文档

### B. 术语表

| 术语 | 说明 |
|------|------|
| InfoType | SAP HR 信息类型，带时间有效性的数据结构 |
| ACDOCA | S/4HANA 统一日记账表 |
| BP | Business Partner 业务伙伴 |
| RLS | Row Level Security 行级安全 |
