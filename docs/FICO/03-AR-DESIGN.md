# AR 应收账款功能设计

**模块**: Accounts Receivable (应收账款)
**对标**: SAP FI-AR (Financial Accounting - Accounts Receivable)
**版本**: 1.0
**更新日期**: 2026-03-14

---

## 1. 模块概述

### 1.1 功能范围

应收账款 (AR) 模块管理企业与客户的财务关系，包括：

- **客户主数据** - 客户基本信息、银行信息
- **客户发票** - 发票生成、过账
- **收款处理** - 收款录入、收款核销
- **信用管理** - 信用额度、信用检查
- **催收管理** - 催收策略、催收信函

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AR 应收账款架构 - 对标 SAP FI-AR                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         主数据 (Master Data)                         │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 客户主档 │  │ 信用主档 │  │ 银行信息 │  │ 付款条款 │            │   │
│  │  │ KNA1/KNB1│  │  UKMBP   │  │  KNBK    │  │  T052    │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         发票处理 (Billing)                           │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 销售开票 │  │ 贷项凭证 │  │ 预收账款 │  │ 利息计算 │            │   │
│  │  │  VF01    │  │  VF11    │  │  F-29    │  │  F.44    │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         收款处理 (Cash Receipt)                      │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 收款录入 │  │ 自动核销 │  │ 手工核销 │  │ 部分核销 │            │   │
│  │  │  F-28    │  │  F.28    │  │  F-32    │  │  F-32    │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         信用管理 (Credit Management)                 │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 信用检查 │  │ 信用额度 │  │ 风险类别 │  │ 信用冻结 │            │   │
│  │  │  VKM1    │  │  FD32    │  │  FD32    │  │  VKM3    │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 客户主数据

### 2.1 通用数据 (对标 SAP KNA1)

| 字段 | 类型 | 说明 |
|------|------|------|
| customer_number | VARCHAR(10) | 客户编号 |
| customer_name | VARCHAR(100) | 客户名称 |
| search_term | VARCHAR(20) | 检索项 |
| customer_type | VARCHAR(4) | 客户类型 |
| industry | VARCHAR(4) | 行业 |
| country | VARCHAR(3) | 国家 |
| region | VARCHAR(3) | 地区 |
| city | VARCHAR(40) | 城市 |
| postal_code | VARCHAR(10) | 邮编 |
| street | VARCHAR(60) | 街道 |
| tax_number | VARCHAR(18) | 税号 |
| customer_group | VARCHAR(4) | 客户组 |

### 2.2 公司代码数据 (对标 SAP KNB1)

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| customer_number | VARCHAR(10) | 客户编号 |
| reconcil_account | VARCHAR(10) | 统驭科目 |
| sort_key | VARCHAR(3) | 排序码 |
| payment_terms | VARCHAR(4) | 付款条款 |
| payment_method | VARCHAR(1) | 付款方式 |
| payment_block | VARCHAR(1) | 付款冻结 |
| dunning_area | VARCHAR(2) | 催收范围 |
| dunning_block | VARCHAR(1) | 催收冻结 |
| head_office | VARCHAR(10) | 总部 |
| alternative_payer | VARCHAR(10) | 备选付款人 |

### 2.3 销售数据 (对标 SAP KNVV)

| 字段 | 类型 | 说明 |
|------|------|------|
| sales_org | VARCHAR(4) | 销售组织 |
| distribution_channel | VARCHAR(2) | 分销渠道 |
| division | VARCHAR(2) | 产品组 |
| customer_group | VARCHAR(2) | 客户组 |
| sales_district | VARCHAR(6) | 销售区域 |
| currency | VARCHAR(3) | 货币 |
| price_group | VARCHAR(2) | 价格组 |
| price_list_type | VARCHAR(2) | 价格表类型 |
| incoterms | VARCHAR(3) | 国际贸易条款 |
| incoterms2 | VARCHAR(28) | 国际贸易条款2 |

---

## 3. 信用管理 (对标 SAP FSCM-CM)

### 3.1 信用主数据

| 字段 | 类型 | 说明 |
|------|------|------|
| customer_number | VARCHAR(10) | 客户编号 |
| credit_control_area | VARCHAR(4) | 信用控制范围 |
| credit_limit | DECIMAL(15,2) | 信用额度 |
| credit_used | DECIMAL(15,2) | 已用额度 |
| credit_available | DECIMAL(15,2) | 可用额度 |
| risk_category | VARCHAR(4) | 风险类别 |
| credit_group | VARCHAR(4) | 信用组 |
| credit_status | VARCHAR(1) | 信用状态 |
| last_review_date | DATE | 上次审核日期 |
| next_review_date | DATE | 下次审核日期 |
| currency | VARCHAR(3) | 币种 |

### 3.2 信用检查规则

| 检查点 | 触发条件 | 动作 |
|--------|----------|------|
| 订单创建 | 信用使用 > 额度 | 警告/冻结 |
| 发货过账 | 信用使用 > 额度 | 警告/冻结 |
| 发票过账 | 信用使用 > 额度 | 记录 |

### 3.3 风险类别

| 类别 | 说明 | 额度建议 |
|------|------|----------|
| LOW | 低风险客户 | 高额度 |
| MED | 中风险客户 | 中等额度 |
| HIGH | 高风险客户 | 低额度 |
| CRIT | 临界客户 | 严格限制 |

---

## 4. 客户发票

### 4.1 发票凭证 (对标 SAP BSID/BSAD)

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| customer_number | VARCHAR(10) | 客户编号 |
| document_type | VARCHAR(2) | 凭证类型 (DR/DG) |
| document_number | VARCHAR(10) | 凭证编号 |
| document_date | DATE | 凭证日期 |
| posting_date | DATE | 过账日期 |
| baseline_date | DATE | 基准日期 |
| payment_terms | VARCHAR(4) | 付款条款 |
| due_date | DATE | 到期日 |
| invoice_amount | DECIMAL(18,2) | 发票金额 |
| tax_amount | DECIMAL(18,2) | 税额 |
| tax_code | VARCHAR(2) | 税码 |
| sales_order | VARCHAR(10) | 销售订单 |
| delivery | VARCHAR(10) | 发货单 |
| billing_type | VARCHAR(4) | 开票类型 |
| invoice_status | VARCHAR(2) | 发票状态 |
| clearing_status | VARCHAR(1) | 清账状态 |
| clearing_date | DATE | 清账日期 |
| clearing_doc | VARCHAR(10) | 清账凭证 |

---

## 5. 收款处理

### 5.1 收款凭证

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| customer_number | VARCHAR(10) | 客户编号 |
| document_type | VARCHAR(2) | 凭证类型 (DZ) |
| receipt_number | VARCHAR(10) | 收款单号 |
| receipt_date | DATE | 收款日期 |
| posting_date | DATE | 过账日期 |
| receipt_amount | DECIMAL(18,2) | 收款金额 |
| payment_method | VARCHAR(1) | 收款方式 |
| house_bank | VARCHAR(5) | 银行标识 |
| bank_account | VARCHAR(18) | 银行账户 |
| reference | VARCHAR(16) | 参考号 |
| receipt_status | VARCHAR(2) | 收款状态 |

### 5.2 核销规则

```
收款核销规则:
1. 按到期日排序 (先到期先核销)
2. 优先核销有折扣的发票
3. 支持部分核销
4. 支持汇兑损益处理
5. 支持批量核销
```

---

## 6. 催收管理

### 6.1 催收等级 (对标 SAP T006)

| 级别 | 天数 | 催收动作 |
|------|------|----------|
| 0 | 0 | 友好提醒 |
| 1 | 15 | 第一次催收 |
| 2 | 30 | 第二次催收 |
| 3 | 45 | 最终催收 |
| 4 | 60 | 法律行动 |

### 6.2 催收记录

| 字段 | 类型 | 说明 |
|------|------|------|
| customer_number | VARCHAR(10) | 客户编号 |
| dunning_level | INTEGER | 催收级别 |
| dunning_date | DATE | 催收日期 |
| dunning_amount | DECIMAL(15,2) | 催收金额 |
| dunning_block | VARCHAR(1) | 催收冻结 |
| letter_sent | BOOLEAN | 信函已发送 |
| response_status | VARCHAR(2) | 响应状态 |

---

## 7. 接口设计

### 7.1 客户接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/ar/customers | GET/POST | 客户列表/创建 |
| /api/ar/customers/{id} | GET/PUT | 客户详情/更新 |
| /api/ar/customers/{id}/credit | GET/PUT | 信用额度 |

### 7.2 发票接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/ar/invoices | GET/POST | 发票列表/创建 |
| /api/ar/invoices/{id} | GET | 发票详情 |
| /api/ar/invoices/{id}/post | POST | 过账发票 |
| /api/ar/invoices/open-items | GET | 未清项列表 |

### 7.3 收款接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/ar/receipts | GET/POST | 收款列表/创建 |
| /api/ar/receipts/{id} | GET | 收款详情 |
| /api/ar/receipts/{id}/clear | POST | 核销 |
| /api/ar/receipts/auto-clear | POST | 自动核销 |

### 7.4 信用接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/ar/credit/check | POST | 信用检查 |
| /api/ar/credit/release | POST | 信用释放 |
| /api/ar/credit/blocks | GET | 信用冻结列表 |

### 7.5 报表接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/ar/aging | GET | 账龄分析 |
| /api/ar/customer-balance | GET | 客户余额 |
| /api/ar/collection-forecast | GET | 回款预测 |

---

## 8. 相关文档

- [FICO 模块总览](./00-FICO-OVERVIEW.md)
- [GL 总账](./01-GL-DESIGN.md)
- [AP 应付账款](./02-AP-DESIGN.md)

---

## 9. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
