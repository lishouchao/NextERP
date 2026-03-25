# AP 应付账款功能设计

**模块**: Accounts Payable (应付账款)
**对标**: SAP FI-AP (Financial Accounting - Accounts Payable)
**版本**: 1.0
**更新日期**: 2026-03-14

---

## 1. 模块概述

### 1.1 功能范围

应付账款 (AP) 模块管理企业与供应商的财务关系，包括：

- **供应商主数据** - 供应商基本信息、银行信息
- **供应商发票** - 发票录入、校验、过账
- **付款处理** - 付款申请、付款执行、付款核销
- **预付账款** - 预付款、预付核销
- **账龄分析** - 应付账龄、付款预测

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AP 应付账款架构 - 对标 SAP FI-AP                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         主数据 (Master Data)                         │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐                           │   │
│  │  │ 供应商主档│  │ 银行信息 │  │ 付款条款 │                           │   │
│  │  │ LFA1/LFB1│  │  LFBK    │  │  T052    │                           │   │
│  │  └──────────┘  └──────────┘  └──────────┘                           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         发票处理 (Invoice Processing)                │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 发票录入 │  │ 三单匹配 │  │ 发票校验 │  │ 贷项凭证 │            │   │
│  │  │ MIRO/FB60│  │  三单匹配 │  │  校验    │  │  FB60    │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         付款处理 (Payment Processing)                │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 付款建议 │  │ 付款执行 │  │ 手工付款 │  │ 核销处理 │            │   │
│  │  │  F110    │  │  F110    │  │  F-53    │  │  F-44    │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 供应商主数据

### 2.1 通用数据 (对标 SAP LFA1)

| 字段 | 类型 | 说明 |
|------|------|------|
| vendor_number | VARCHAR(10) | 供应商编号 |
| vendor_name | VARCHAR(100) | 供应商名称 |
| search_term | VARCHAR(20) | 检索项 |
| vendor_type | VARCHAR(4) | 供应商类型 |
| industry | VARCHAR(4) | 行业 |
| country | VARCHAR(3) | 国家 |
| region | VARCHAR(3) | 地区 |
| city | VARCHAR(40) | 城市 |
| postal_code | VARCHAR(10) | 邮编 |
| street | VARCHAR(60) | 街道 |
| tax_number | VARCHAR(18) | 税号 |
| tax_number2 | VARCHAR(18) | 税号2 |
| legal_form | VARCHAR(4) | 法律形式 |
| vendor_group | VARCHAR(4) | 供应商组 |

### 2.2 公司代码数据 (对标 SAP LFB1)

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| vendor_number | VARCHAR(10) | 供应商编号 |
| reconcil_account | VARCHAR(10) | 统驭科目 |
| sort_key | VARCHAR(3) | 排序码 |
| payment_terms | VARCHAR(4) | 付款条款 |
| payment_method | VARCHAR(1) | 付款方式 |
| payment_block | VARCHAR(1) | 付款冻结 |
| house_bank | VARCHAR(5) | 银行标识 |
| head_office | VARCHAR(10) | 总部 |
| alternative_payee | VARCHAR(10) | 备选收款人 |
| dunning_area | VARCHAR(2) | 催收范围 |
| dunning_block | VARCHAR(1) | 催收冻结 |
| account_group | VARCHAR(4) | 科目组 |

### 2.3 采购数据 (对标 SAP LFM1)

| 字段 | 类型 | 说明 |
|------|------|------|
| purchasing_org | VARCHAR(4) | 采购组织 |
| vendor_number | VARCHAR(10) | 供应商编号 |
| order_currency | VARCHAR(3) | 订单货币 |
| gr_based_iv | BOOLEAN | 收货校验发票 |
| tax_jurisdiction | VARCHAR(15) | 税务管辖 |

### 2.4 银行信息 (对标 SAP LFBK)

| 字段 | 类型 | 说明 |
|------|------|------|
| vendor_number | VARCHAR(10) | 供应商编号 |
| bank_key | VARCHAR(15) | 银行代码 |
| bank_account | VARCHAR(18) | 银行账号 |
| bank_control_key | VARCHAR(2) | 银行控制码 |
| iban | VARCHAR(34) | IBAN |
| bank_name | VARCHAR(60) | 银行名称 |
| swift_code | VARCHAR(11) | SWIFT代码 |

---

## 3. 供应商发票

### 3.1 发票凭证 (对标 SAP BSEG + BSIK/BSAK)

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| vendor_number | VARCHAR(10) | 供应商编号 |
| document_type | VARCHAR(2) | 凭证类型 (KR/KG) |
| document_number | VARCHAR(10) | 凭证编号 |
| document_date | DATE | 凭证日期 |
| posting_date | DATE | 过账日期 |
| baseline_date | DATE | 基准日期 |
| payment_terms | VARCHAR(4) | 付款条款 |
| due_date | DATE | 到期日 |
| invoice_amount | DECIMAL(18,2) | 发票金额 |
| tax_amount | DECIMAL(18,2) | 税额 |
| tax_code | VARCHAR(2) | 税码 |
| reference | VARCHAR(16) | 参考号 (发票号) |
| assignment | VARCHAR(18) | 分配号 |
| doc_header_text | VARCHAR(50) | 凭证抬头文本 |
| purchase_order | VARCHAR(10) | 采购订单号 |
| goods_receipt | VARCHAR(10) | 收货单号 |
| invoice_status | VARCHAR(2) | 发票状态 |
| clearing_status | VARCHAR(1) | 清账状态 |
| clearing_date | DATE | 清账日期 |
| clearing_doc | VARCHAR(10) | 清账凭证 |

### 3.2 发票校验 (三单匹配)

```
采购订单 (PO) ────► 收货单 (GR) ────► 发票 (Invoice)
     │                │                  │
     │    数量        │     数量         │
     └──── 100 ◄──────┴──── 100 ◄────────┴── 100 ✓
     │                │                  │
     │    单价        │                  │    单价
     └──── 10.00 ◄────┴──────────────────┴── 10.00 ✓
                      │                  │
                      │                  │    金额
                      └──────────────────┴── 1000.00 ✓

匹配规则:
1. 发票数量 ≤ 收货数量
2. 发票单价 ≤ PO单价 (或PO价格允许容差内)
3. 发票金额 = 数量 × 单价
```

### 3.3 发票容差 (对标 SAP OMR6)

| 容差类型 | 限制 | 说明 |
|----------|------|------|
| 金额差异 | ±1.00 | 发票金额与PO金额差异 |
| 数量差异 | ±5% | 发票数量与GR数量差异 |
| 价格差异 | ±5% | 发票价格与PO价格差异 |
| 日期差异 | 30天 | 发票日期与GR日期差异 |

---

## 4. 付款处理

### 4.1 付款条款 (对标 SAP T052)

| 代码 | 名称 | 条款 |
|------|------|------|
| ZB01 | 立即付款 | 净0天 |
| ZB14 | 14天付款 | 净14天 |
| ZB30 | 30天付款 | 净30天 |
| ZB21 | 2/10净30 | 10天内2%折扣，30天全额 |
| ZB15 | 1.5/14净45 | 14天内1.5%折扣，45天全额 |

### 4.2 付款方式 (对标 SAP T042Z)

| 代码 | 名称 | 说明 |
|------|------|------|
| E | 电子付款 | 银行转账 |
| C | 支票 | 纸质支票 |
| B | 银行汇票 | 银行票据 |
| W | 现金 | 现金付款 |

### 4.3 付款建议 (对标 SAP F110)

| 字段 | 类型 | 说明 |
|------|------|------|
| run_id | VARCHAR(10) | 运行ID |
| run_date | DATE | 运行日期 |
| company_code | VARCHAR(4) | 公司代码 |
| currency | VARCHAR(3) | 货币 |
| payment_method | VARCHAR(1) | 付款方式 |
| posting_date | DATE | 过账日期 |
| value_date | DATE | 起息日 |
| next_payment_date | DATE | 下次付款日期 |
| total_amount | DECIMAL(18,2) | 付款总额 |
| vendor_count | INTEGER | 供应商数量 |
| invoice_count | INTEGER | 发票数量 |
| run_status | VARCHAR(2) | 运行状态 |

### 4.4 付款凭证

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| vendor_number | VARCHAR(10) | 供应商编号 |
| document_type | VARCHAR(2) | 凭证类型 (KZ) |
| payment_method | VARCHAR(1) | 付款方式 |
| house_bank | VARCHAR(5) | 银行标识 |
| bank_account | VARCHAR(18) | 银行账户 |
| payment_amount | DECIMAL(18,2) | 付款金额 |
| payment_date | DATE | 付款日期 |
| value_date | DATE | 起息日 |
| check_number | VARCHAR(10) | 支票号 |
| payment_status | VARCHAR(2) | 付款状态 |

---

## 5. 核销处理

### 5.1 核销类型

| 类型 | 说明 |
|------|------|
| 发票-付款核销 | 正常核销 |
| 发票-贷项核销 | 贷项凭证核销 |
| 发票-发票核销 | 反向发票核销 |
| 预付-发票核销 | 预付款核销 |

### 5.2 核销规则

```
付款核销规则:
1. 按到期日排序 (先到期先核销)
2. 优先核销有折扣的发票
3. 支持部分核销
4. 支持汇兑损益处理
```

---

## 6. 账龄分析

### 6.1 账龄区间

| 区间 | 天数范围 | 说明 |
|------|----------|------|
| 1 | 0-30 | 当前 |
| 2 | 31-60 | 逾期1个月 |
| 3 | 61-90 | 逾期2个月 |
| 4 | 91-180 | 逾期3-6个月 |
| 5 | 181-365 | 逾期6-12个月 |
| 6 | 365+ | 逾期1年以上 |

### 6.2 账龄报表 (对标 SAP S_ALR_870120**)

```
供应商账龄分析表
┌────────────┬────────┬────────┬────────┬────────┬────────┬────────┐
│  供应商    │ 当前   │ 1-30天 │ 31-60天│ 61-90天│ >90天  │  合计  │
├────────────┼────────┼────────┼────────┼────────┼────────┼────────┤
│ 100001     │ 10,000 │  5,000 │    -   │    -   │    -   │ 15,000 │
│ 100002     │    -   │  8,000 │  3,000 │    -   │    -   │ 11,000 │
│ 100003     │ 20,000 │    -   │    -   │  5,000 │  2,000 │ 27,000 │
├────────────┼────────┼────────┼────────┼────────┼────────┼────────┤
│  合计      │ 30,000 │ 13,000 │  3,000 │  5,000 │  2,000 │ 53,000 │
└────────────┴────────┴────────┴────────┴────────┴────────┴────────┘
```

---

## 7. 接口设计

### 7.1 供应商接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/ap/vendors | GET/POST | 供应商列表/创建 |
| /api/ap/vendors/{id} | GET/PUT | 供应商详情/更新 |
| /api/ap/vendors/{id}/bank-accounts | GET/POST | 银行账户 |

### 7.2 发票接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/ap/invoices | GET/POST | 发票列表/创建 |
| /api/ap/invoices/{id} | GET | 发票详情 |
| /api/ap/invoices/{id}/verify | POST | 发票校验 |
| /api/ap/invoices/{id}/post | POST | 过账发票 |
| /api/ap/invoices/open-items | GET | 未清项列表 |

### 7.3 付款接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/ap/payments/proposal | POST | 生成付款建议 |
| /api/ap/payments/proposal/{id} | GET | 付款建议详情 |
| /api/ap/payments/proposal/{id}/execute | POST | 执行付款 |
| /api/ap/payments/manual | POST | 手工付款 |
| /api/ap/payments/{id}/clear | POST | 核销 |

### 7.4 报表接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/ap/aging | GET | 账龄分析 |
| /api/ap/vendor-balance | GET | 供应商余额 |
| /api/ap/payment-forecast | GET | 付款预测 |

---

## 8. 相关文档

- [FICO 模块总览](./00-FICO-OVERVIEW.md)
- [GL 总账](./01-GL-DESIGN.md)
- [AR 应收账款](./03-AR-DESIGN.md)

---

## 9. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
