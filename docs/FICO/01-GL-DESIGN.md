# GL 总账功能设计

**模块**: General Ledger (总账)
**对标**: SAP FI-GL (Financial Accounting - General Ledger)
**版本**: 1.0
**更新日期**: 2026-03-14

---

## 1. 模块概述

### 1.1 功能范围

总账 (GL) 模块是财务会计的核心，提供：

- **会计科目** - 科目主数据、科目层级
- **会计凭证** - 凭证录入、审核、过账
- **期间处理** - 月结、年结
- **报表查询** - 科目余额、凭证查询

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    GL 总账架构 - 对标 SAP FI-GL                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         主数据 (Master Data)                         │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 会计科目 │  │ 会计年度 │  │ 凭证类型 │  │ 过账期间 │            │   │
│  │  │ SKA1/SKB1│  │  T009    │  │  T003    │  │  T001B   │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         业务交易 (Transactions)                      │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 凭证录入 │  │ 凭证审核 │  │ 凭证过账 │  │ 凭证冲销 │            │   │
│  │  │ FB01/F-02│  │  FBV0    │  │  FB08    │  │  FBRA    │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         期末处理 (Closing)                           │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 汇率评估 │  │ 重组凭证 │  │ 期间结账 │  │ 年度结转 │            │   │
│  │  │  F.05    │  │  F.101   │  │  OB52    │  │  F.16    │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         报表查询 (Reporting)                         │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 科目余额 │  │ 总账明细 │  │ 资产负债表│ │ 损益表   │            │   │
│  │  │  FS10N   │  │  FBL3N   │  │  S_PL0***│  │ S_PL0*** │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 会计科目主数据

### 2.1 科目表层级 (对标 SAP SKA1)

| 字段 | 类型 | 说明 |
|------|------|------|
| chart_of_accounts | VARCHAR(4) | 科目表 |
| account_number | VARCHAR(10) | 科目编号 |
| account_name | VARCHAR(50) | 科目名称 (短) |
| account_name_long | VARCHAR(100) | 科目名称 (长) |
| account_group | VARCHAR(4) | 科目组 |
| account_type | VARCHAR(1) | 科目类型 (B=资产负债/P=损益) |
| functional_area | VARCHAR(4) | 功能范围 |
| trading_partner | VARCHAR(6) | 交易伙伴 |

### 2.2 公司代码层级 (对标 SAP SKB1)

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| account_number | VARCHAR(10) | 科目编号 |
| currency | VARCHAR(3) | 科目货币 |
| recon_account | VARCHAR(1) | 统驭科目类型 |
| open_item_mgmt | BOOLEAN | 未清项管理 |
| line_item_mgmt | BOOLEAN | 行项目显示 |
| field_status_grp | VARCHAR(4) | 字段状态组 |
| tax_category | VARCHAR(2) | 税务类别 |
| house_bank | VARCHAR(5) | 银行标识 (银行科目) |
| interest_ind | VARCHAR(2) | 利息标识 |

### 2.3 科目组 (对标 SAP T077S)

| 代码 | 名称 | 科目范围 |
|------|------|----------|
| ASST | 资产科目 | 10000000-19999999 |
| LIAB | 负债科目 | 20000000-29999999 |
| EQTY | 权益科目 | 30000000-39999999 |
| REVE | 收入科目 | 40000000-49999999 |
| EXPE | 费用科目 | 50000000-59999999 |
| OTHS | 其他科目 | 90000000-99999999 |

---

## 3. 会计凭证

### 3.1 凭证头 (对标 SAP BKPF)

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| document_type | VARCHAR(2) | 凭证类型 |
| document_number | VARCHAR(10) | 凭证编号 |
| fiscal_year | INTEGER | 会计年度 |
| document_date | DATE | 凭证日期 |
| posting_date | DATE | 过账日期 |
| period | INTEGER | 期间 |
| reference | VARCHAR(16) | 参考号 |
| doc_header_text | VARCHAR(50) | 凭证抬头文本 |
| currency | VARCHAR(3) | 凭证货币 |
| exchange_rate | DECIMAL(12,6) | 汇率 |
| translation_date | DATE | 换算日期 |
| reversal_reason | VARCHAR(2) | 冲销原因 |
| reversal_reference | VARCHAR(10) | 冲销参考 |
| status | VARCHAR(2) | 凭证状态 |

### 3.2 凭证行 (对标 SAP BSEG)

| 字段 | 类型 | 说明 |
|------|------|------|
| item_number | INTEGER | 行号 |
| gl_account | VARCHAR(10) | 科目 |
| posting_key | VARCHAR(2) | 记账码 |
| debit_credit | VARCHAR(1) | 借/贷 (D/C) |
| amount | DECIMAL(18,2) | 金额 |
| local_amount | DECIMAL(18,2) | 本位币金额 |
| group_amount | DECIMAL(18,2) | 集团货币金额 |
| tax_code | VARCHAR(2) | 税码 |
| tax_amount | DECIMAL(18,2) | 税额 |
| cost_center | VARCHAR(10) | 成本中心 |
| profit_center | VARCHAR(10) | 利润中心 |
| functional_area | VARCHAR(4) | 功能范围 |
| segment | VARCHAR(10) | 段 |
| business_area | VARCHAR(4) | 业务范围 |
| trading_partner | VARCHAR(6) | 交易伙伴 |
| baseline_date | DATE | 基准日期 |
| payment_terms | VARCHAR(4) | 付款条款 |
| due_date | DATE | 到期日 |
| assignment | VARCHAR(18) | 分配号 |
| line_item_text | VARCHAR(50) | 行项目文本 |
| value_date | DATE | 起息日 |
| house_bank | VARCHAR(5) | 银行标识 |
| bank_account | VARCHAR(18) | 银行账户 |

### 3.3 记账码 (对标 SAP TBSL)

| 代码 | 名称 | 借/贷 | 科目类型 |
|------|------|-------|----------|
| 01 | 客户发票 | 借 | D |
| 11 | 客户贷项 | 贷 | D |
| 31 | 供应商发票 | 贷 | K |
| 41 | 供应商贷项 | 借 | K |
| 40 | 总账借方 | 借 | S |
| 50 | 总账贷方 | 贷 | S |
| 70 | 资产借方 | 借 | A |
| 75 | 资产贷方 | 贷 | A |

---

## 4. 期末处理

### 4.1 外币评估 (对标 SAP F.05)

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| evaluation_date | DATE | 评估日期 |
| currency_type | VARCHAR(2) | 货币类型 |
| gl_account | VARCHAR(10) | 科目 |
| evaluation_method | VARCHAR(1) | 评估方法 |
| exchange_rate | DECIMAL(12,6) | 评估汇率 |
| book_balance | DECIMAL(18,2) | 账面余额 |
| evaluated_balance | DECIMAL(18,2) | 评估后余额 |
| valuation_diff | DECIMAL(18,2) | 评估差异 |
| unrealized_gain | DECIMAL(18,2) | 未实现收益 |
| unrealized_loss | DECIMAL(18,2) | 未实现损失 |

### 4.2 期间结账 (对标 SAP OB52)

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| account_type | VARCHAR(1) | 科目类型 |
| fiscal_year | INTEGER | 会计年度 |
| period_from | INTEGER | 开放期间起始 |
| period_to | INTEGER | 开放期间结束 |
| auth_group | VARCHAR(3) | 授权组 |

### 4.3 科目余额 (对标 SAP GLT0)

| 字段 | 类型 | 说明 |
|------|------|------|
| company_code | VARCHAR(4) | 公司代码 |
| gl_account | VARCHAR(10) | 科目 |
| fiscal_year | INTEGER | 会计年度 |
| period | INTEGER | 期间 |
| debit_total | DECIMAL(18,2) | 借方合计 |
| credit_total | DECIMAL(18,2) | 贷方合计 |
| balance | DECIMAL(18,2) | 余额 |
| currency | VARCHAR(3) | 货币 |

---

## 5. 凭证处理流程

### 5.1 凭证录入流程

```
1. 凭证录入 (FB01/F-02)
   ├── 输入凭证头信息
   │   ├── 公司代码
   │   ├── 凭证类型
   │   ├── 凭证日期
   │   └── 过账日期
   │
   ├── 输入凭证行信息
   │   ├── 科目
   │   ├── 记账码
   │   ├── 金额
   │   └── 成本对象
   │
   └── 系统检查
       ├── 期间是否开放
       ├── 借贷是否平衡
       ├── 必填字段检查
       └── 税务计算

2. 凭证审核 (FBV0)
   └── 审核人确认凭证

3. 凭证过账
   └── 更新科目余额

4. 凭证查询 (FBL3N/FS10N)
   └── 查询凭证和余额
```

### 5.2 凭证冲销流程

```
1. 冲销原因选择 (对标 SAP T041C)
   ├── 01 - 对冲记账
   ├── 02 - 负记账
   ├── 03 - 实际负记账
   └── 04 - STORNO

2. 冲销方式
   ├── 单个冲销 (FB08)
   │   └── 冲销单张凭证
   │
   └── 批量冲销 (F.80/FBRA)
       └── 冲销多张凭证
```

---

## 6. 接口设计

### 6.1 科目管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/gl/accounts | GET/POST | 科目列表/创建 |
| /api/gl/accounts/{id} | GET/PUT | 科目详情/更新 |
| /api/gl/accounts/{id}/balance | GET | 科目余额 |
| /api/gl/accounts/tree | GET | 科目树结构 |

### 6.2 凭证管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/gl/journal-entries | GET/POST | 凭证列表/创建 |
| /api/gl/journal-entries/{id} | GET | 凭证详情 |
| /api/gl/journal-entries/{id}/post | POST | 过账凭证 |
| /api/gl/journal-entries/{id}/reverse | POST | 冲销凭证 |
| /api/gl/journal-entries/{id}/lines | GET | 凭证行项目 |
| /api/gl/journal-entries/park | POST | 预制凭证 |

### 6.3 期间管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/gl/periods | GET | 期间列表 |
| /api/gl/periods/{period}/open | POST | 开放期间 |
| /api/gl/periods/{period}/close | POST | 关闭期间 |
| /api/gl/periods/status | GET | 期间状态 |

### 6.4 评估接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/gl/valuation/foreign-currency | POST | 外币评估 |
| /api/gl/valuation/valuation-result | GET | 评估结果 |
| /api/gl/valuation/post | POST | 过账评估凭证 |

---

## 7. 报表设计

### 7.1 标准报表

| 报表 | 说明 | SAP 对标 |
|------|------|----------|
| 科目余额表 | 按科目显示借贷余额 | FS10N |
| 总账明细账 | 科目行项目明细 | FBL3N |
| 科目汇总表 | 多期间汇总 | S_ALR_870123** |
| 试算平衡表 | 借贷平衡检查 | S_ALR_870123** |
| 资产负债表 | B/S 报表 | S_PL0_86000028 |
| 损益表 | P&L 报表 | S_PL0_86000029 |

---

## 8. 相关文档

- [FICO 模块总览](./00-FICO-OVERVIEW.md)
- [AP 应付账款](./02-AP-DESIGN.md)
- [AR 应收账款](./03-AR-DESIGN.md)

---

## 9. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
