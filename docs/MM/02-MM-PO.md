# MM 采购管理设计

**模块**: Purchasing (采购管理)
**对标**: SAP MM - Purchasing (EBAN/EKKO/EKPO)
**版本**: 1.0
**更新日期**: 2026-03-14

---

## 1. 概述

### 1.1 业务范围

采购管理覆盖从需求提出到订单完成的完整采购流程：

| 功能 | 说明 | SAP 对标 |
|------|------|----------|
| 采购申请 | 需求提报、审批 | ME51N/ME52N/ME54N |
| 询价报价 | 供应商报价比较 | ME41/ME47/ME49 |
| 采购订单 | 订单创建、审批、跟踪 | ME21N/ME22N/ME29N |
| 框架协议 | 合同、计划协议 | ME31K/ME31L |
| 采购信息记录 | 物料-供应商价格 | ME11/ME12 |
| 货源清单 | 采购来源管理 | ME01/ME03 |

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    采购管理架构 - 对标 SAP MM-Purchasing                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         需求来源                                     │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ MRP计划  │  │ 手工申请 │  │ PR申请   │  │ 计划协议 │            │   │
│  │  │ MD01     │  │ ME51N    │  │          │  │ ME31L    │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         采购寻源                                     │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 货源清单 │  │ 信息记录 │  │ 询价报价 │  │ 供应商评估│            │   │
│  │  │ ME01     │  │ ME11     │  │ ME41     │  │ ME64     │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         采购执行                                     │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 采购订单 │  │ 框架协议 │  │ 订单审批 │  │ 订单跟踪 │            │   │
│  │  │ ME21N    │  │ ME31K    │  │ ME29N    │  │ ME2N     │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 采购申请

### 2.1 采购申请头 (对标 SAP EBAN - 部分字段)

| 字段 | 类型 | 说明 |
|------|------|------|
| pr_number | VARCHAR(10) | 采购申请号 |
| pr_type | VARCHAR(4) | 申请类型 (NB:标准) |
| created_on | DATE | 创建日期 |
| created_by | VARCHAR(12) | 创建人 |
| purchasing_group | VARCHAR(3) | 采购组 |
| purchasing_org | VARCHAR(4) | 采购组织 |
| plant | VARCHAR(4) | 工厂 |
| status | VARCHAR(1) | 状态 |
| total_value | DECIMAL(15,2) | 总价值 |
| currency | VARCHAR(3) | 货币 |
| header_text | VARCHAR(50) | 申请说明 |
| approval_status | VARCHAR(1) | 审批状态 |
| approved_by | VARCHAR(12) | 审批人 |
| approved_on | DATE | 审批日期 |
| delivery_date | DATE | 需求日期 |

### 2.2 采购申请项

| 字段 | 类型 | 说明 |
|------|------|------|
| pr_number | VARCHAR(10) | 采购申请号 |
| pr_item | INTEGER | 行号 |
| material | VARCHAR(18) | 物料编码 |
| short_text | VARCHAR(40) | 简短描述 |
| material_group | VARCHAR(9) | 物料组 |
| quantity | DECIMAL(13,3) | 数量 |
| unit | VARCHAR(3) | 单位 |
| price | DECIMAL(12,2) | 估价 |
| price_unit | DECIMAL(5,0) | 价格单位 |
| currency | VARCHAR(3) | 货币 |
| delivery_date | DATE | 交货日期 |
| plant | VARCHAR(4) | 工厂 |
| storage_location | VARCHAR(4) | 存储地点 |
| requisitioner | VARCHAR(12) | 需求人 |
| tracking_number | VARCHAR(10) | 跟踪号 |
| item_category | VARCHAR(1) | 项目类别 |
| account_assignment | VARCHAR(1) | 科目分配类别 |
| cost_center | VARCHAR(10) | 成本中心 |
| order_number | VARCHAR(12) | 订单号 |
| asset_number | VARCHAR(12) | 资产号 |
| wbs_element | VARCHAR(24) | WBS元素 |
| status | VARCHAR(1) | 状态 |
| deletion_flag | VARCHAR(1) | 删除标记 |

### 2.3 审批流程

```
采购申请审批流程:
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│   申请人提交                                                    │
│       │                                                         │
│       ▼                                                         │
│   ┌─────────────┐                                               │
│   │ 部门经理审批 │ ◄── 金额 < 10,000                            │
│   └──────┬──────┘                                               │
│          │                                                      │
│          ▼                                                      │
│   ┌─────────────┐                                               │
│   │ 采购部门审批 │ ◄── 金额 < 50,000                            │
│   └──────┬──────┘                                               │
│          │                                                      │
│          ▼                                                      │
│   ┌─────────────┐                                               │
│   │ 财务审批     │ ◄── 金额 < 200,000                           │
│   └──────┬──────┘                                               │
│          │                                                      │
│          ▼                                                      │
│   ┌─────────────┐                                               │
│   │ 总经理审批   │ ◄── 金额 >= 200,000                          │
│   └──────┬──────┘                                               │
│          │                                                      │
│          ▼                                                      │
│   审批通过/拒绝                                                 │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. 采购订单

### 3.1 采购订单头 (对标 SAP EKKO)

| 字段 | 类型 | 说明 |
|------|------|------|
| po_number | VARCHAR(10) | 采购订单号 |
| po_type | VARCHAR(4) | 订单类型 |
| vendor | VARCHAR(10) | 供应商 |
| purchasing_org | VARCHAR(4) | 采购组织 |
| purchasing_group | VARCHAR(3) | 采购组 |
| company_code | VARCHAR(4) | 公司代码 |
| currency | VARCHAR(3) | 货币 |
| exchange_rate | DECIMAL(9,5) | 汇率 |
| document_date | DATE | 凭证日期 |
| valid_from | DATE | 有效起始日 |
| valid_to | DATE | 有效截止日 |
| terms_of_payment | VARCHAR(4) | 付款条款 |
| incoterms1 | VARCHAR(3) | 国际贸易条款1 |
| incoterms2 | VARCHAR(28) | 国际贸易条款2 |
| sales_person | VARCHAR(30) | 销售员 |
| telephone | VARCHAR(16) | 电话 |
| status | VARCHAR(1) | 状态 |
| release_status | VARCHAR(1) | 审批状态 |
| created_on | DATE | 创建日期 |
| created_by | VARCHAR(12) | 创建人 |
| changed_on | DATE | 修改日期 |
| changed_by | VARCHAR(12) | 修改人 |
| header_text | VARCHAR(50) | 订单说明 |

### 3.2 采购订单项 (对标 SAP EKPO)

| 字段 | 类型 | 说明 |
|------|------|------|
| po_number | VARCHAR(10) | 采购订单号 |
| po_item | INTEGER | 行号 |
| material | VARCHAR(18) | 物料编码 |
| short_text | VARCHAR(40) | 简短描述 |
| material_group | VARCHAR(9) | 物料组 |
| quantity | DECIMAL(13,3) | 订单数量 |
| unit | VARCHAR(3) | 单位 |
| price | DECIMAL(12,2) | 净价 |
| price_unit | DECIMAL(5,0) | 价格单位 |
| currency | VARCHAR(3) | 货币 |
| net_value | DECIMAL(15,2) | 净值 |
| gross_value | DECIMAL(15,2) | 总值 |
| tax_code | VARCHAR(2) | 税码 |
| tax_amount | DECIMAL(14,2) | 税额 |
| plant | VARCHAR(4) | 工厂 |
| storage_location | VARCHAR(4) | 存储地点 |
| delivery_date | DATE | 交货日期 |
| quantity_delivered | DECIMAL(13,3) | 已交货数量 |
| quantity_invoiced | DECIMAL(13,3) | 已开票数量 |
| gr_non_valuated | VARCHAR(1) | 收货非评估 |
| gr_ind | VARCHAR(1) | 收货标识 |
| ir_ind | VARCHAR(1) | 发票标识 |
| item_category | VARCHAR(1) | 项目类别 |
| account_assignment_category | VARCHAR(1) | 科目分配类别 |
| final_invoice | VARCHAR(1) | 最终发票标识 |
| deletion_flag | VARCHAR(1) | 删除标记 |
| blocked_flag | VARCHAR(1) | 冻结标记 |

### 3.3 订单类型

| 类型 | 说明 | 用途 |
|------|------|------|
| NB | 标准采购订单 | 正常采购 |
| ZNB | 标准采购订单 | 内部定义 |
| UB | 库存转储订单 | 工厂间转储 |
| ZUB | 库存转储订单 | 内部定义 |
| ZCON | 寄售采购订单 | 寄售补货 |
| ZSUB | 外协采购订单 | 外包加工 |

### 3.4 科目分配类别

| 类别 | 说明 | 科目对象 |
|------|------|----------|
| A | 资产 | 资产编号 |
| C | 销售 | 销售订单 |
| F | 订单 | 生产订单 |
| K | 成本中心 | 成本中心 |
| P | 项目 | WBS元素 |
| S | 项目 | 项目定义 |

---

## 4. 框架协议

### 4.1 合同 (对标 SAP MM - Contract)

| 字段 | 类型 | 说明 |
|------|------|------|
| contract_number | VARCHAR(10) | 合同号 |
| contract_type | VARCHAR(4) | 合同类型 (WK/MK) |
| vendor | VARCHAR(10) | 供应商 |
| purchasing_org | VARCHAR(4) | 采购组织 |
| purchasing_group | VARCHAR(3) | 采购组 |
| validity_start | DATE | 有效起始日 |
| validity_end | DATE | 有效截止日 |
| target_value | DECIMAL(15,2) | 目标价值 |
| released_value | DECIMAL(15,2) | 已释放价值 |
| currency | VARCHAR(3) | 货币 |
| terms_of_payment | VARCHAR(4) | 付款条款 |
| incoterms1 | VARCHAR(3) | 国际贸易条款 |
| status | VARCHAR(1) | 状态 |

### 4.2 计划协议 (对标 SAP MM - Scheduling Agreement)

| 字段 | 类型 | 说明 |
|------|------|------|
| sa_number | VARCHAR(10) | 计划协议号 |
| sa_type | VARCHAR(4) | 协议类型 (LP/LU) |
| vendor | VARCHAR(10) | 供应商 |
| purchasing_org | VARCHAR(4) | 采购组织 |
| purchasing_group | VARCHAR(3) | 采购组 |
| validity_start | DATE | 有效起始日 |
| validity_end | DATE | 有效截止日 |
| agreement_date | DATE | 协议日期 |
| creation_date | DATE | 创建日期 |
| created_by | VARCHAR(12) | 创建人 |
| status | VARCHAR(1) | 状态 |

### 4.3 计划协议交货行

| 字段 | 类型 | 说明 |
|------|------|------|
| sa_number | VARCHAR(10) | 计划协议号 |
| sa_item | INTEGER | 行号 |
| delivery_date | DATE | 交货日期 |
| quantity | DECIMAL(13,3) | 数量 |
| quantity_delivered | DECIMAL(13,3) | 已交货数量 |
| statistics_relevant | VARCHAR(1) | 统计相关 |

---

## 5. 采购信息记录

### 5.1 信息记录 (对标 SAP EINA/EINE)

| 字段 | 类型 | 说明 |
|------|------|------|
| info_record_number | VARCHAR(10) | 信息记录号 |
| info_record_type | VARCHAR(1) | 类型 (0:标准/1:寄售/2:分包) |
| vendor | VARCHAR(10) | 供应商 |
| material | VARCHAR(18) | 物料编码 |
| purchasing_org | VARCHAR(4) | 采购组织 |
| plant | VARCHAR(4) | 工厂 |
| valid_from | DATE | 生效日期 |
| valid_to | DATE | 失效日期 |
| price | DECIMAL(12,2) | 净价 |
| price_unit | DECIMAL(5,0) | 价格单位 |
| currency | VARCHAR(3) | 货币 |
| planned_deliv_time | DECIMAL(3,0) | 计划交货时间 |
| minimum_order_qty | DECIMAL(13,3) | 最小订单量 |
| standard_order_qty | DECIMAL(13,3) | 标准订单量 |
| rounding_profile | VARCHAR(4) | 舍入配置 |
| tolerance_key_over | VARCHAR(4) | 超额容差 |
| tolerance_key_under | VARCHAR(4) | 欠交容差 |
| terms_of_payment | VARCHAR(4) | 付款条款 |
| incoterms1 | VARCHAR(3) | 国际贸易条款 |
| incoterms2 | VARCHAR(28) | 国际贸易条款2 |
| status | VARCHAR(1) | 状态 |

### 5.2 条件记录

```
价格条件结构:
┌─────────────────────────────────────────────────────────────────┐
│ 条件类型                                                        │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ PB00 - 采购价格 (基础价格)                                  │ │
│ │ RB00 - 折扣百分比                                           │ │
│ │ RA01 - 费用                                                 │ │
│ │ FRA1 - 运费                                                 │ │
│ │ ZP01 - 自定义附加费                                         │ │
│ └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│ 有效期控制:                                                     │
│ ┌─────────────────────────────────────────────────────────────┐ │
│ │ 有效起始: 2026-01-01                                        │ │
│ │ 有效截止: 2026-12-31                                        │ │
│ │                                                             │ │
│ │ 价格: 100.00 CNY / PC                                       │ │
│ │ 折扣: 2%                                                    │ │
│ │ 运费: 5.00 CNY / PC                                         │ │
│ │ ─────────────────                                           │ │
│ │ 净价: 103.00 CNY / PC                                       │ │
│ └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 6. 货源清单

### 6.1 货源清单记录 (对标 SAP EORD)

| 字段 | 类型 | 说明 |
|------|------|------|
| material | VARCHAR(18) | 物料编码 |
| plant | VARCHAR(4) | 工厂 |
| source_list_record | INTEGER | 记录号 |
| vendor | VARCHAR(10) | 供应商 |
| agreement | VARCHAR(10) | 合同/计划协议 |
| info_record | VARCHAR(10) | 信息记录 |
| validity_start | DATE | 有效起始日 |
| validity_end | DATE | 有效截止日 |
| priority | VARCHAR(1) | 优先级 |
| blocked | VARCHAR(1) | 冻结标识 |
| standard_purchase_org | VARCHAR(4) | 标准采购组织 |
| mpp_vendor | VARCHAR(10) | MPP供应商 |

### 6.2 货源确定规则

```
货源确定优先级:
┌─────────────────────────────────────────────────────────────────┐
│ 1. 货源清单 (Source List)                                       │
│    ↓                                                            │
│ 2. 合同/计划协议 (Outline Agreement)                            │
│    ↓                                                            │
│ 3. 采购信息记录 (Info Record)                                   │
│    ↓                                                            │
│ 4. 供应商配额安排 (Quota Arrangement)                           │
│    ↓                                                            │
│ 5. 无货源 (手工选择)                                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. 询价报价

### 7.1 询价 (对标 SAP ME41/ME42)

| 字段 | 类型 | 说明 |
|------|------|------|
| rfq_number | VARCHAR(10) | 询价号 |
| rfq_type | VARCHAR(4) | 询价类型 (AN) |
| purchasing_org | VARCHAR(4) | 采购组织 |
| purchasing_group | VARCHAR(3) | 采购组 |
| document_date | DATE | 凭证日期 |
| quotation_deadline | DATE | 报价截止日 |
| bid_invitation_date | DATE | 邀标日期 |
| status | VARCHAR(1) | 状态 |
| created_on | DATE | 创建日期 |
| created_by | VARCHAR(12) | 创建人 |

### 7.2 报价 (对标 SAP ME47)

| 字段 | 类型 | 说明 |
|------|------|------|
| rfq_number | VARCHAR(10) | 询价号 |
| vendor | VARCHAR(10) | 供应商 |
| quotation_number | VARCHAR(10) | 报价单号 |
| quotation_date | DATE | 报价日期 |
| validity_start | DATE | 有效起始日 |
| validity_end | DATE | 有效截止日 |
| price | DECIMAL(12,2) | 报价 |
| currency | VARCHAR(3) | 货币 |
| delivery_time | DECIMAL(3,0) | 交货时间(天) |
| terms_of_payment | VARCHAR(4) | 付款条款 |
| status | VARCHAR(1) | 状态 |

### 7.3 报价比较 (对标 SAP ME49)

```
报价比较表:
┌─────────────────────────────────────────────────────────────────────────────┐
│ 物料: 1000001 - 螺栓 M8x20                                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│              │  供应商A    │  供应商B    │  供应商C    │                     │
├──────────────┼─────────────┼─────────────┼─────────────┤                     │
│ 单价         │   1.00 CNY  │   0.95 CNY  │   1.10 CNY  │                     │
│ 交货时间     │   7 天      │   14 天     │   5 天      │                     │
│ 付款条款     │   30天      │   45天      │   30天      │                     │
│ MOQ          │   1000 PC   │   500 PC    │   2000 PC   │                     │
│ 运费         │   包含      │   包含      │   不含      │                     │
│ 质量等级     │   A         │   A         │   B         │                     │
├──────────────┼─────────────┼─────────────┼─────────────┤                     │
│ 综合评分     │   90        │   85        │   70        │                     │
│ 排名         │   1         │   2         │   3         │                     │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 8. 订单审批

### 8.1 审批策略 (对标 SAP MM Release Strategy)

| 策略 | 金额范围 | 审批级别 |
|------|----------|----------|
| 01 | < 10,000 | 采购经理 |
| 02 | 10,000 - 50,000 | 采购经理 → 财务经理 |
| 03 | 50,000 - 200,000 | 采购经理 → 财务经理 → 总监 |
| 04 | >= 200,000 | 采购经理 → 财务经理 → 总监 → 总经理 |

### 8.2 审批记录

| 字段 | 类型 | 说明 |
|------|------|------|
| po_number | VARCHAR(10) | 采购订单号 |
| release_code | VARCHAR(2) | 审批代码 |
| release_group | VARCHAR(4) | 审批组 |
| release_strategy | VARCHAR(4) | 审批策略 |
| release_status | VARCHAR(1) | 审批状态 |
| released_by | VARCHAR(12) | 审批人 |
| released_on | DATE | 审批日期 |
| release_time | TIME | 审批时间 |

---

## 9. 接口设计

### 9.1 采购申请接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/mm/purchase-requisitions | GET | 采购申请列表 |
| /api/mm/purchase-requisitions | POST | 创建采购申请 |
| /api/mm/purchase-requisitions/{id} | GET | 申请详情 |
| /api/mm/purchase-requisitions/{id} | PUT | 更新申请 |
| /api/mm/purchase-requisitions/{id}/approve | POST | 审批申请 |
| /api/mm/purchase-requisitions/{id}/reject | POST | 拒绝申请 |
| /api/mm/purchase-requisitions/{id}/close | POST | 关闭申请 |

### 9.2 采购订单接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/mm/purchase-orders | GET | 采购订单列表 |
| /api/mm/purchase-orders | POST | 创建采购订单 |
| /api/mm/purchase-orders/{id} | GET | 订单详情 |
| /api/mm/purchase-orders/{id} | PUT | 更新订单 |
| /api/mm/purchase-orders/{id}/release | POST | 订单审批 |
| /api/mm/purchase-orders/{id}/close | POST | 关闭订单 |
| /api/mm/purchase-orders/{id}/items | GET | 订单行项 |

### 9.3 框架协议接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/mm/contracts | GET/POST | 合同列表/创建 |
| /api/mm/contracts/{id} | GET/PUT | 合同详情/更新 |
| /api/mm/scheduling-agreements | GET/POST | 计划协议列表/创建 |
| /api/mm/scheduling-agreements/{id} | GET/PUT | 计划协议详情/更新 |

### 9.4 报表接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/mm/reports/po-analysis | GET | 采购订单分析 |
| /api/mm/reports/vendor-evaluation | GET | 供应商评估 |
| /api/mm/reports/price-comparison | GET | 价格比较 |
| /api/mm/reports/procurement-tracking | GET | 采购跟踪 |

---

## 10. 相关文档

- [MM 模块总览](./00-MM-OVERVIEW.md)
- [物料主数据](./01-MM-MASTER.md)
- [库存管理](./03-MM-IM.md)
- [发票校验](./04-MM-IV.md)

---

## 11. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
