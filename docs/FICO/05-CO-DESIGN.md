# CO 成本管理功能设计

**模块**: Controlling (管理会计/成本控制)
**对标**: SAP CO (Controlling)
**版本**: 1.0
**更新日期**: 2026-03-14

---

## 1. 模块概述

### 1.1 功能范围

成本控制 (CO) 模块提供内部管理会计功能，包括：

- **成本中心会计 (CCA)** - 成本归集、分配、分摊
- **利润中心会计 (PCA)** - 利润归集、内部定价
- **盈利能力分析 (PA)** - 贡献边际、市场细分
- **产品成本控制 (PC)** - 成本估算、在制品
- **内部订单 (IO)** - 项目成本、投资订单

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    CO 成本管理架构 - 对标 SAP CO                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      主数据 (Master Data)                            │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 成本中心 │  │ 利润中心 │  │ 功能范围 │  │ 内部订单 │            │   │
│  │  │ CSKS/CSKU│  │ CEPC     │  │ TFKB     │  │ AUFK     │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      成本中心会计 (CCA)                              │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 费用归集 │  │ 成本分配 │  │ 成本分摊 │  │ 作业分配 │            │   │
│  │  │ COSS/COSP│  │  KSU1    │  │  KSV1    │  │  KB21N   │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      利润中心会计 (PCA)                              │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 利润归集 │  │ 转移定价 │  │ 利润分配 │  │ 利润余额 │            │   │
│  │  │ CE2****  │  │  KEJ2    │  │  KEU2    │  │  KE24    │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      盈利能力分析 (PA)                               │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 利润分析 │  │ 市场细分 │  │ 贡献边际 │  │ 销售利润 │            │   │
│  │  │ CE1****  │  │  KE30    │  │  KE24    │  │  KE24    │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 组织架构

### 2.1 成本控制范围 (对标 SAP TKA01)

| 字段 | 类型 | 说明 |
|------|------|------|
| controlling_area | VARCHAR(4) | 成本控制范围 |
| name | VARCHAR(50) | 名称 |
| currency | VARCHAR(3) | 货币 |
| chart_of_accounts | VARCHAR(4) | 科目表 |
| fiscal_year_variant | VARCHAR(2) | 会计年度变式 |
| company_codes | ARRAY | 关联公司代码 |

### 2.2 成本中心 (对标 SAP CSKS)

| 字段 | 类型 | 说明 |
|------|------|------|
| controlling_area | VARCHAR(4) | 成本控制范围 |
| cost_center | VARCHAR(10) | 成本中心代码 |
| name | VARCHAR(40) | 名称 |
| company_code | VARCHAR(4) | 公司代码 |
| profit_center | VARCHAR(10) | 利润中心 |
| cost_center_type | VARCHAR(1) | 成本中心类型 |
| person_responsible | VARCHAR(12) | 负责人 |
| standard_hierarchy | VARCHAR(12) | 标准层次 |
| hierarchy_area | VARCHAR(12) | 层级区域 |
| valid_from | DATE | 生效日期 |
| valid_to | DATE | 失效日期 |

### 2.3 成本中心类型

| 代码 | 说明 |
|------|------|
| F | 生产 (Production) |
| H | 服务 (Service) |
| V | 销售 (Sales) |
| A | 行政 (Admin) |
| I | 间接生产 (Indirect Production) |

### 2.4 利润中心 (对标 SAP CEPC)

| 字段 | 类型 | 说明 |
|------|------|------|
| controlling_area | VARCHAR(4) | 成本控制范围 |
| profit_center | VARCHAR(10) | 利润中心代码 |
| name | VARCHAR(40) | 名称 |
| company_code | VARCHAR(4) | 公司代码 |
| profit_center_type | VARCHAR(1) | 利润中心类型 |
| segment | VARCHAR(10) | 报告分部 |
| standard_hierarchy | VARCHAR(12) | 标准层次 |
| valid_from | DATE | 生效日期 |
| valid_to | DATE | 失效日期 |

---

## 3. 成本中心会计 (CCA)

### 3.1 成本行项目 (对标 SAP COSS/COSP)

| 字段 | 类型 | 说明 |
|------|------|------|
| controlling_area | VARCHAR(4) | 成本控制范围 |
| cost_center | VARCHAR(10) | 成本中心 |
| fiscal_year | INTEGER | 会计年度 |
| period | INTEGER | 期间 |
| cost_element | VARCHAR(10) | 成本要素 |
| cost_element_type | VARCHAR(1) | 要素类型 (1=初级/2=次级) |
| gl_account | VARCHAR(10) | 科目 |
| amount | DECIMAL(18,2) | 金额 |
| currency | VARCHAR(3) | 货币 |
| quantity | DECIMAL(15,3) | 数量 |
| unit | VARCHAR(3) | 单位 |
| posting_date | DATE | 过账日期 |
| document_number | VARCHAR(10) | 凭证编号 |
| line_item | INTEGER | 行号 |
| value_type | VARCHAR(2) | 值类型 |
| version | VARCHAR(3) | 版本 |

### 3.2 成本要素 (对标 SAP CSKA/CSKB)

| 字段 | 类型 | 说明 |
|------|------|------|
| cost_element | VARCHAR(10) | 成本要素 |
| name | VARCHAR(40) | 名称 |
| cost_element_type | VARCHAR(1) | 类型 |
| category | VARCHAR(1) | 类别 |

**成本要素类别**:
| 代码 | 说明 |
|------|------|
| 01 | 初级成本/收入 |
| 11 | 收入 |
| 12 | 销售扣除 |
| 21 | 内部分配 |
| 22 | 作业分配 |
| 31 | 订单/项目结算 |
| 41 | 结构性成本 |

### 3.3 成本分配/分摊

**分配 (Distribution)** - 仅分配初级成本要素
**分摊 (Assessment)** - 使用次级成本要素

| 字段 | 类型 | 说明 |
|------|------|------|
| cycle | VARCHAR(10) | 循环 |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| sender_cost_center | VARCHAR(10) | 发送方成本中心 |
| receiver_cost_center | VARCHAR(10) | 接收方成本中心 |
| cost_element | VARCHAR(10) | 成本要素 |
| amount | DECIMAL(18,2) | 分配金额 |
| percentage | DECIMAL(5,2) | 分配比例 |
| tracing_factor | VARCHAR(3) | 追溯因子 |

---

## 4. 利润中心会计 (PCA)

### 4.1 利润中心凭证 (对标 SAP CE2****)

| 字段 | 类型 | 说明 |
|------|------|------|
| controlling_area | VARCHAR(4) | 成本控制范围 |
| profit_center | VARCHAR(10) | 利润中心 |
| fiscal_year | INTEGER | 会计年度 |
| period | INTEGER | 期间 |
| document_number | VARCHAR(10) | 凭证编号 |
| posting_date | DATE | 过账日期 |
| value_field | VARCHAR(4) | 值字段 |
| amount | DECIMAL(18,2) | 金额 |
| currency | VARCHAR(3) | 货币 |
| quantity | DECIMAL(15,3) | 数量 |
| material | VARCHAR(18) | 物料 |
| customer | VARCHAR(10) | 客户 |
| sales_org | VARCHAR(4) | 销售组织 |

### 4.2 转移定价

| 字段 | 类型 | 说明 |
|------|------|------|
| transfer_type | VARCHAR(2) | 转移类型 |
| sender_profit_center | VARCHAR(10) | 发送方利润中心 |
| receiver_profit_center | VARCHAR(10) | 接收方利润中心 |
| material | VARCHAR(18) | 物料 |
| quantity | DECIMAL(15,3) | 数量 |
| transfer_price | DECIMAL(15,2) | 转移价格 |
| transfer_value | DECIMAL(18,2) | 转移价值 |
| valuation_method | VARCHAR(3) | 评估方法 |

---

## 5. 盈利能力分析 (CO-PA)

### 5.1 特征字段 (对标 SAP CE4****)

| 特征 | 说明 |
|------|------|
| 产品 | 产品编码 |
| 产品组 | 产品层级 |
| 客户 | 客户编码 |
| 客户组 | 客户分类 |
| 销售组织 | 销售组织 |
| 分销渠道 | 渠道 |
| 产品组 | 产品组 |
| 地区 | 销售区域 |
| 国家 | 国家 |

### 5.2 值字段 (对标 SAP CE1****)

| 值字段 | 说明 |
|--------|------|
| VV001 | 销售收入 |
| VV002 | 销售折扣 |
| VV003 | 销售成本 |
| VV004 | 销售毛利 |
| VV010 | 运费 |
| VV020 | 佣金 |
| VV030 | 贡献边际 I |
| VV040 | 固定成本 |
| VV050 | 贡献边际 II |
| VV060 | 销售利润 |

### 5.3 贡献边际分析

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    贡献边际分析表                                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  销售收入                     VV001          1,000,000                      │
│  (-) 销售折扣                  VV002          (   50,000)                   │
│  ──────────────────────────────────────────────────                         │
│  净销售收入                                    950,000                      │
│                                                                             │
│  (-) 销售成本                  VV003          (  500,000)                   │
│  ──────────────────────────────────────────────────                         │
│  销售毛利                     VV004            450,000                      │
│                                                                             │
│  (-) 运费                     VV010            (  30,000)                   │
│  (-) 佣金                     VV020            (  20,000)                   │
│  ──────────────────────────────────────────────────                         │
│  贡献边际 I                   VV030            400,000                      │
│                                                                             │
│  (-) 固定成本                  VV040           ( 250,000)                   │
│  ──────────────────────────────────────────────────                         │
│  贡献边际 II                  VV050            150,000                      │
│                                                                             │
│  (-) 公司费用                                                  (  80,000)  │
│  ──────────────────────────────────────────────────                         │
│  销售利润                     VV060             70,000                      │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 6. 产品成本控制 (CO-PC)

### 6.1 成本估算 (对标 SAP KEKO/KHS)

| 字段 | 类型 | 说明 |
|------|------|------|
| costing_type | VARCHAR(4) | 成本核算类型 |
| costing_variant | VARCHAR(4) | 成本核算变式 |
| costing_date | DATE | 成本核算日期 |
| material | VARCHAR(18) | 物料 |
| plant | VARCHAR(4) | 工厂 |
| lot_size | DECIMAL(15,3) | 批量 |
| total_cost | DECIMAL(18,2) | 总成本 |
| material_cost | DECIMAL(18,2) | 材料成本 |
| labor_cost | DECIMAL(18,2) | 人工成本 |
| machine_cost | DECIMAL(18,2) | 机器成本 |
| overhead_cost | DECIMAL(18,2) | 制造费用 |
| currency | VARCHAR(3) | 货币 |
| status | VARCHAR(2) | 状态 |

### 6.2 成本构成结构

| 代码 | 说明 |
|------|------|
| Z01 | 直接材料 |
| Z02 | 直接人工 |
| Z03 | 机器工时 |
| Z04 | 制造费用 |
| Z05 | 外协加工 |
| Z06 | 特殊直接成本 |

---

## 7. 接口设计

### 7.1 成本中心接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/co/cost-centers | GET/POST | 成本中心列表/创建 |
| /api/co/cost-centers/{id} | GET/PUT | 成本中心详情/更新 |
| /api/co/cost-centers/{id}/costs | GET | 成本明细 |
| /api/co/cost-centers/{id}/balance | GET | 成本余额 |

### 7.2 利润中心接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/co/profit-centers | GET/POST | 利润中心列表/创建 |
| /api/co/profit-centers/{id} | GET/PUT | 利润中心详情/更新 |
| /api/co/profit-centers/{id}/profit | GET | 利润明细 |

### 7.3 分配分摊接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/co/allocation/distribute | POST | 成本分配 |
| /api/co/allocation/assess | POST | 成本分摊 |
| /api/co/allocation/cycles | GET/POST | 分配循环 |

### 7.4 报表接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/co/reports/cost-center-report | GET | 成本中心报表 |
| /api/co/reports/profit-center-report | GET | 利润中心报表 |
| /api/co/reports/profitability-analysis | GET | 盈利能力分析 |

---

## 8. 相关文档

- [FICO 模块总览](./00-FICO-OVERVIEW.md)
- [GL 总账](./01-GL-DESIGN.md)
- [AA 资产会计](./04-AA-DESIGN.md)

---

## 9. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
