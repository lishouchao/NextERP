# AA 资产会计功能设计

**模块**: Asset Accounting (资产会计)
**对标**: SAP FI-AA (Financial Accounting - Asset Accounting)
**版本**: 1.0
**更新日期**: 2026-03-14

---

## 1. 模块概述

### 1.1 功能范围

资产会计 (AA) 模块管理企业固定资产的全生命周期，包括：

- **资产主数据** - 资产卡片、资产分类
- **资产购置** - 外购、自建、赠予
- **资产折旧** - 折旧计算、折旧范围
- **资产转移** - 公司间转移、成本中心转移
- **资产处置** - 出售、报废、清理
- **资产盘点** - 盘点计划、盘点差异

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AA 资产会计架构 - 对标 SAP FI-AA                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         主数据 (Master Data)                         │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 资产主档 │  │ 资产分类 │  │ 折旧范围 │  │ 折旧码   │            │   │
│  │  │ ANLA/ANLC│  │  ANKT    │  │  T093    │  │  T093    │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         资产业务 (Asset Transactions)                │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 资产购置 │  │ 资产转移 │  │ 资产折旧 │  │ 资产处置 │            │   │
│  │  │  AS01    │  │  ABUMN   │  │  AFAB    │  │  ABAVN   │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         报表查询 (Reporting)                         │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 资产清单 │  │ 折旧明细 │  │ 资产余额 │  │ 资产明细 │            │   │
│  │  │ S_ALR_***│  │ S_ALR_***│  │ S_ALR_***│  │ S_ALR_***│            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 资产主数据

### 2.1 资产主记录 (对标 SAP ANLA)

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| asset_number | VARCHAR(12) | 资产编号 |
| sub_number | VARCHAR(4) | 子编号 |
| asset_class | VARCHAR(8) | 资产分类 |
| asset_name | VARCHAR(100) | 资产名称 |
| asset_name2 | VARCHAR(100) | 资产名称2 |
| description | TEXT | 描述 |
| serial_number | VARCHAR(18) | 序列号 |
| inventory_number | VARCHAR(25) | 库存编号 |
| asset_type | VARCHAR(2) | 资产类型 |
| asset_status | VARCHAR(2) | 资产状态 |
| capitalization_date | DATE | 资本化日期 |
| deactivation_date | DATE | 停用日期 |
| acquisition_date | DATE | 购置日期 |
| cost_center | VARCHAR(10) | 成本中心 |
| profit_center | VARCHAR(10) | 利润中心 |
| business_area | VARCHAR(4) | 业务范围 |
| location | VARCHAR(10) | 资产位置 |
| room | VARCHAR(8) | 房间 |
| plant | VARCHAR(4) | 工厂 |
| vendor_number | VARCHAR(10) | 供应商 |
| manufacturer | VARCHAR(30) | 制造商 |
| construction_year | INTEGER | 建造年份 |

### 2.2 资产分类 (对标 SAP ANKT)

| 代码 | 名称 | 说明 |
|------|------|------|
| 1000 | 建筑物 | 不动产 |
| 1100 | 机器设备 | 生产设备 |
| 1200 | 办公设备 | 办公资产 |
| 1300 | 运输工具 | 车辆 |
| 1400 | 电子设备 | 电脑等 |
| 1500 | 无形资产 | 专利/版权 |
| 1600 | 土地 | 土地使用权 |
| 1700 | 在建工程 | CIP |
| 1800 | 租赁资产 | 使用权资产 |

### 2.3 资产状态

| 状态 | 说明 |
|------|------|
| AC | 活跃 (Active) |
| DP | 已处置 (Disposed) |
| IP | 在建 (In Progress) |
| BL | 冻结 (Blocked) |

---

## 3. 资产价值 (对标 SAP ANLC)

### 3.1 核心价值字段

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| asset_number | VARCHAR(12) | 资产编号 |
| sub_number | VARCHAR(4) | 子编号 |
| fiscal_year | INTEGER | 会计年度 |
| depreciation_area | VARCHAR(2) | 折旧范围 |
| acquisition_value | DECIMAL(18,2) | 购置价值 |
| cum_acquisition | DECIMAL(18,2) | 累计购置价值 |
| ord_depreciation | DECIMAL(18,2) | 累计折旧 |
| planned_depr | DECIMAL(18,2) | 计划折旧 |
| special_depr | DECIMAL(18,2) | 特别折旧 |
| revaluation | DECIMAL(18,2) | 重估价值 |
| book_value | DECIMAL(18,2) | 账面净值 |
| net_book_value | DECIMAL(18,2) | 净账面价值 |
| scrap_value | DECIMAL(18,2) | 残值 |
| depr_start_date | DATE | 折旧开始日期 |

### 3.2 折旧范围 (对标 SAP T093)

| 代码 | 名称 | 说明 |
|------|------|------|
| 01 | 账面折旧 | 会计准则 |
| 10 | 税务折旧 | 税法要求 |
| 20 | 集团折旧 | 合并报表 |
| 30 | IFRS折旧 | 国际准则 |
| 40 | 管理折旧 | 内部管理 |

---

## 4. 折旧计算

### 4.1 折旧方法 (对标 SAP T093)

| 代码 | 名称 | 公式 |
|------|------|------|
| LINR | 直线法 | (原值-残值)/使用年限 |
| DECL | 余额递减法 | 净值×折旧率 |
| DBLV | 双倍余额递减 | 净值×(2/使用年限) |
| SYD | 年数总和法 | (原值-残值)×剩余年限/年数总和 |
| UNIT | 工作量法 | 单位折旧×使用量 |
| JP-SL | 日本直线法 | 特殊处理 |

### 4.2 折旧计算示例

```
直线法折旧示例:
┌─────────────────────────────────────────────────────────────┐
│ 资产原值: 100,000                                           │
│ 残值: 5,000                                                 │
│ 使用年限: 10年                                              │
│                                                             │
│ 年折旧额 = (100,000 - 5,000) / 10 = 9,500                   │
│ 月折旧额 = 9,500 / 12 = 791.67                              │
│                                                             │
│ 年度   年初净值    年折旧    年末净值                        │
│ ─────  ─────────  ────────  ─────────                       │
│   1    100,000     9,500     90,500                         │
│   2     90,500     9,500     81,000                         │
│   3     81,000     9,500     71,500                         │
│   ...                                                      │
│  10     14,500     9,500      5,000 (残值)                  │
└─────────────────────────────────────────────────────────────┘
```

### 4.3 折旧运行 (对标 SAP AFAB)

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| fiscal_year | INTEGER | 会计年度 |
| period | INTEGER | 期间 |
| depreciation_area | VARCHAR(2) | 折旧范围 |
| run_date | DATE | 运行日期 |
| run_status | VARCHAR(2) | 运行状态 |
| total_depreciation | DECIMAL(18,2) | 折旧总额 |
| asset_count | INTEGER | 资产数量 |

---

## 5. 资产业务

### 5.1 资产购置 (对标 SAP ANEP)

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| asset_number | VARCHAR(12) | 资产编号 |
| sub_number | VARCHAR(4) | 子编号 |
| transaction_type | VARCHAR(3) | 交易类型 |
| document_number | VARCHAR(10) | 凭证编号 |
| posting_date | DATE | 过账日期 |
| acquisition_value | DECIMAL(18,2) | 购置价值 |
| accumulated_depr | DECIMAL(18,2) | 累计折旧 |
| book_value | DECIMAL(18,2) | 账面价值 |
| vendor_number | VARCHAR(10) | 供应商 |
| reference | VARCHAR(16) | 参考号 |

### 5.2 交易类型

| 代码 | 说明 |
|------|------|
| 100 | 外部购置 |
| 110 | 自建资产 |
| 120 | 赠予 |
| 130 | 租赁 |
| 200 | 转移 (从) |
| 210 | 转移 (到) |
| 300 | 出售 |
| 310 | 报废 |
| 320 | 清理 |

### 5.3 资产转移 (对标 SAP ABUMN)

| 字段 | 类型 | 说明 |
|------|------|------|
| transfer_id | UUID | 转移ID |
| from_company | VARCHAR(4) | 原公司代码 |
| to_company | VARCHAR(4) | 目标公司代码 |
| from_asset | VARCHAR(12) | 原资产编号 |
| to_asset | VARCHAR(12) | 目标资产编号 |
| transfer_date | DATE | 转移日期 |
| transfer_type | VARCHAR(2) | 转移类型 |
| book_value | DECIMAL(18,2) | 转移账面价值 |
| accumulated_depr | DECIMAL(18,2) | 转移累计折旧 |
| from_cost_center | VARCHAR(10) | 原成本中心 |
| to_cost_center | VARCHAR(10) | 目标成本中心 |

### 5.4 资产处置 (对标 SAP ABAVN)

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| asset_number | VARCHAR(12) | 资产编号 |
| sub_number | VARCHAR(4) | 子编号 |
| disposal_date | DATE | 处置日期 |
| disposal_type | VARCHAR(2) | 处置类型 |
| disposal_reason | TEXT | 处置原因 |
| book_value | DECIMAL(18,2) | 处置账面价值 |
| accumulated_depr | DECIMAL(18,2) | 累计折旧 |
| revenue | DECIMAL(18,2) | 处置收入 |
| gain_loss | DECIMAL(18,2) | 处置损益 |
| customer_number | VARCHAR(10) | 买方客户 |

---

## 6. 接口设计

### 6.1 资产主数据接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/aa/assets | GET/POST | 资产列表/创建 |
| /api/aa/assets/{id} | GET/PUT | 资产详情/更新 |
| /api/aa/assets/{id}/values | GET | 资产价值 |
| /api/aa/assets/{id}/depreciation | GET | 折旧历史 |

### 6.2 资产业务接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/aa/acquisition | POST | 资产购置 |
| /api/aa/transfer | POST | 资产转移 |
| /api/aa/disposal | POST | 资产处置 |
| /api/aa/revaluation | POST | 资产重估 |

### 6.3 折旧接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/aa/depreciation/run | POST | 运行折旧 |
| /api/aa/depreciation/preview | POST | 折旧预览 |
| /api/aa/depreciation/post | POST | 过账折旧 |

### 6.4 报表接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/aa/reports/asset-list | GET | 资产清单 |
| /api/aa/reports/depreciation-list | GET | 折旧明细 |
| /api/aa/reports/asset-balance | GET | 资产余额表 |

---

## 7. 相关文档

- [FICO 模块总览](./00-FICO-OVERVIEW.md)
- [GL 总账](./01-GL-DESIGN.md)
- [CO 成本管理](./05-CO-DESIGN.md)

---

## 8. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
