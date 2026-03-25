# SD 模块一致性检查报告

**检查日期**: 2026-03-16
**检查范围**: SD 功能设计文档 vs 数据库设计文档
**状态**: ✅ 已同步

---

## 1. 检查结果摘要

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 销售订单头 | ✅ 一致 | sd_sales_order_hdr 结构完整 |
| 销售订单项 | ✅ 一致 | sd_sales_order_itm 结构完整 |
| 交货单头 | ✅ 一致 | sd_delivery_hdr 结构完整 |
| 交货单项 | ✅ 一致 | sd_delivery_itm 结构完整 |
| 开票凭证头 | ✅ 一致 | sd_billing_hdr 结构完整 |
| 开票凭证项 | ✅ 一致 | sd_billing_itm 结构完整 |
| 定价条件 | ✅ 一致 | sd_condition 结构完整 |
| 定价过程 | ✅ 一致 | sd_pricing_procedure 存在 |
| 客户信用主数据 | ✅ 一致 | sd_credit_master 结构完整 |
| 信用检查日志 | ✅ 一致 | sd_credit_check_log 结构完整 |
| 销售统计 | ✅ 一致 | sd_sales_statistics 存在 |

---

## 2. 详细对比分析

### 2.1 销售订单头 (sd_sales_order_hdr)

#### 数据库设计 vs 功能设计

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| order_number | ✅ | ✅ | 一致 |
| order_type | ✅ | ✅ | 一致 |
| sales_org_id | ✅ | ✅ | 一致 |
| distribution_channel | ✅ | ✅ | 一致 |
| division | ✅ | ✅ | 一致 |
| sold_to_party | ✅ | ✅ | 一致 |
| ship_to_party | ✅ | ✅ | 一致 |
| bill_to_party | ✅ | ✅ | 一致 |
| payer_party | ✅ | ✅ | 一致 |
| document_date | ✅ | ✅ | 一致 |
| requested_delivery_date | ✅ | ✅ | 一致 |
| net_value | ✅ | ✅ | 一致 |
| tax_amount | ✅ | ✅ | 一致 |
| gross_value | ✅ | ✅ | 一致 |
| order_status | ✅ | ✅ | 一致 |
| delivery_status | ✅ | ✅ | 一致 |
| billing_status | ✅ | ✅ | 一致 |
| delivery_block | ✅ | ✅ | 一致 |
| billing_block | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

### 2.2 销售订单项 (sd_sales_order_itm)

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| item_number | ✅ | ✅ | 一致 |
| material_id | ✅ | ✅ | 一致 |
| material_code | ✅ | ✅ | 一致 |
| description | ✅ | ✅ | 一致 |
| ordered_qty | ✅ | ✅ | 一致 |
| delivered_qty | ✅ | ✅ | 一致 |
| invoiced_qty | ✅ | ✅ | 一致 |
| sales_unit | ✅ | ✅ | 一致 |
| net_price | ✅ | ✅ | 一致 |
| net_value | ✅ | ✅ | 一致 |
| plant_id | ✅ | ✅ | 一致 |
| sloc_id | ✅ | ✅ | 一致 |
| item_category | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

### 2.3 交货单头 (sd_delivery_hdr)

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| delivery_number | ✅ | ✅ | 一致 |
| delivery_type | ✅ | ✅ | 一致 |
| sales_org_id | ✅ | ✅ | 一致 |
| sold_to_party | ✅ | ✅ | 一致 |
| ship_to_party | ✅ | ✅ | 一致 |
| document_date | ✅ | ✅ | 一致 |
| planned_gi_date | ✅ | ✅ | 一致 |
| actual_gi_date | ✅ | ✅ | 一致 |
| shipping_point | ✅ | ✅ | 一致 |
| delivery_status | ✅ | ✅ | 一致 |
| picking_status | ✅ | ✅ | 一致 |
| gi_status | ✅ | ✅ | 一致 |
| order_id | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

### 2.4 交货单项 (sd_delivery_itm)

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| item_number | ✅ | ✅ | 一致 |
| material_id | ✅ | ✅ | 一致 |
| delivery_qty | ✅ | ✅ | 一致 |
| picked_qty | ✅ | ✅ | 一致 |
| sales_unit | ✅ | ✅ | 一致 |
| batch_number | ✅ | ✅ | 一致 |
| plant_id | ✅ | ✅ | 一致 |
| sloc_id | ✅ | ✅ | 一致 |
| order_id | ✅ | ✅ | 一致 |
| order_item_id | ✅ | ✅ | 一致 |
| picking_status | ✅ | ✅ | 一致 |
| gi_status | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

### 2.5 开票凭证头 (sd_billing_hdr)

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| billing_number | ✅ | ✅ | 一致 |
| billing_type | ✅ | ✅ | 一致 |
| sales_org_id | ✅ | ✅ | 一致 |
| sold_to_party | ✅ | ✅ | 一致 |
| bill_to_party | ✅ | ✅ | 一致 |
| payer_party | ✅ | ✅ | 一致 |
| document_date | ✅ | ✅ | 一致 |
| billing_date | ✅ | ✅ | 一致 |
| net_value | ✅ | ✅ | 一致 |
| tax_amount | ✅ | ✅ | 一致 |
| gross_value | ✅ | ✅ | 一致 |
| payment_term | ✅ | ✅ | 一致 |
| payment_due_date | ✅ | ✅ | 一致 |
| billing_status | ✅ | ✅ | 一致 |
| delivery_id | ✅ | ✅ | 一致 |
| order_id | ✅ | ✅ | 一致 |
| accounting_doc_id | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

### 2.6 开票凭证项 (sd_billing_itm)

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| item_number | ✅ | ✅ | 一致 |
| material_id | ✅ | ✅ | 一致 |
| billed_qty | ✅ | ✅ | 一致 |
| sales_unit | ✅ | ✅ | 一致 |
| gross_price | ✅ | ✅ | 一致 |
| net_price | ✅ | ✅ | 一致 |
| net_value | ✅ | ✅ | 一致 |
| tax_code | ✅ | ✅ | 一致 |
| tax_amount | ✅ | ✅ | 一致 |
| gross_value | ✅ | ✅ | 一致 |
| cost_value | ✅ | ✅ | 一致 |
| delivery_id | ✅ | ✅ | 一致 |
| delivery_item_id | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

### 2.7 定价条件 (sd_condition)

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| condition_type | ✅ | ✅ | 一致 |
| condition_record | ✅ | ✅ | 一致 |
| condition_item | ✅ | ✅ | 一致 |
| amount | ✅ | ✅ | 一致 |
| rate | ✅ | ✅ | 一致 |
| price_unit | ✅ | ✅ | 一致 |
| currency_id | ✅ | ✅ | 一致 |
| calculation_type | ✅ | ✅ | 一致 |
| valid_from | ✅ | ✅ | 一致 |
| valid_to | ✅ | ✅ | 一致 |
| sales_org_id | ✅ | ✅ | 一致 |
| customer_id | ✅ | ✅ | 一致 |
| material_id | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

### 2.8 定价过程 (sd_pricing_procedure)

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| procedure_code | ✅ | ✅ | 一致 |
| procedure_name | ✅ | ✅ | 一致 |
| step_number | ✅ | ✅ | 一致 |
| condition_type | ✅ | ✅ | 一致 |
| description | ✅ | ✅ | 一致 |
| from_step | ✅ | ✅ | 一致 |
| to_step | ✅ | ✅ | 一致 |
| is_statistical | ✅ | ✅ | 一致 |
| is_subtotal | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

### 2.9 客户信用主数据 (sd_credit_master)

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| customer_id | ✅ | ✅ | 一致 |
| company_id | ✅ | ✅ | 一致 |
| credit_limit | ✅ | ✅ | 一致 |
| used_limit | ✅ | ✅ | 一致 |
| available_limit | ✅ | ✅ | 一致 |
| risk_class | ✅ | ✅ | 一致 |
| credit_group | ✅ | ✅ | 一致 |
| credit_status | ✅ | ✅ | 一致 |
| check_rule | ✅ | ✅ | 一致 |
| last_check_date | ✅ | ✅ | 一致 |
| next_check_date | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

### 2.10 信用检查日志 (sd_credit_check_log)

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| customer_id | ✅ | ✅ | 一致 |
| company_id | ✅ | ✅ | 一致 |
| check_type | ✅ | ✅ | 一致 |
| document_type | ✅ | ✅ | 一致 |
| document_id | ✅ | ✅ | 一致 |
| document_number | ✅ | ✅ | 一致 |
| check_amount | ✅ | ✅ | 一致 |
| check_result | ✅ | ✅ | 一致 |
| result_message | ✅ | ✅ | 一致 |
| credit_limit | ✅ | ✅ | 一致 |
| used_before | ✅ | ✅ | 一致 |
| used_after | ✅ | ✅ | 一致 |
| check_at | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

### 2.11 销售统计 (sd_sales_statistics)

| 字段 | 功能设计 | 数据库 | 状态 |
|------|----------|--------|------|
| stat_year | ✅ | ✅ | 一致 |
| stat_month | ✅ | ✅ | 一致 |
| stat_date | ✅ | ✅ | 一致 |
| company_id | ✅ | ✅ | 一致 |
| sales_org_id | ✅ | ✅ | 一致 |
| customer_id | ✅ | ✅ | 一致 |
| material_id | ✅ | ✅ | 一致 |
| plant_id | ✅ | ✅ | 一致 |
| order_qty/value | ✅ | ✅ | 一致 |
| delivery_qty/value | ✅ | ✅ | 一致 |
| billing_qty/value | ✅ | ✅ | 一致 |
| gross_profit | ✅ | ✅ | 一致 |
| profit_margin | ✅ | ✅ | 一致 |

**结论**: ✅ 完全一致

---

## 3. 功能设计与数据库表对照汇总

| 功能领域 | 功能设计文档 | 数据库表 | 状态 |
|----------|--------------|----------|------|
| 销售订单头 | 01-SD-SALES-ORDER.md | sd_sales_order_hdr | ✅ 一致 |
| 销售订单项 | 01-SD-SALES-ORDER.md | sd_sales_order_itm | ✅ 一致 |
| 交货单头 | 02-SD-DELIVERY.md | sd_delivery_hdr | ✅ 一致 |
| 交货单项 | 02-SD-DELIVERY.md | sd_delivery_itm | ✅ 一致 |
| 开票凭证头 | 03-SD-BILLING.md | sd_billing_hdr | ✅ 一致 |
| 开票凭证项 | 03-SD-BILLING.md | sd_billing_itm | ✅ 一致 |
| 定价条件 | 04-SD-PRICING.md | sd_condition | ✅ 一致 |
| 定价过程 | 04-SD-PRICING.md | sd_pricing_procedure | ✅ 一致 |
| 信用主数据 | 05-SD-CREDIT.md | sd_credit_master | ✅ 一致 |
| 信用检查日志 | 05-SD-CREDIT.md | sd_credit_check_log | ✅ 一致 |
| 销售统计 | 00-SD-OVERVIEW.md | sd_sales_statistics | ✅ 一致 |

---

## 4. SAP 对标确认

### 4.1 销售订单 (对标 SAP VBAK/VBAP)

| SAP 表 | NextERP 表 | 对标状态 |
|--------|------------|----------|
| VBAK | sd_sales_order_hdr | ✅ 完成 |
| VBAP | sd_sales_order_itm | ✅ 完成 |

### 4.2 交货单 (对标 SAP LIKP/LIPS)

| SAP 表 | NextERP 表 | 对标状态 |
|--------|------------|----------|
| LIKP | sd_delivery_hdr | ✅ 完成 |
| LIPS | sd_delivery_itm | ✅ 完成 |

### 4.3 开票 (对标 SAP VBRK/VBRP)

| SAP 表 | NextERP 表 | 对标状态 |
|--------|------------|----------|
| VBRK | sd_billing_hdr | ✅ 完成 |
| VBRP | sd_billing_itm | ✅ 完成 |

### 4.4 定价 (对标 SAP KONP)

| SAP 表 | NextERP 表 | 对标状态 |
|--------|------------|----------|
| KONP | sd_condition | ✅ 完成 |
| - | sd_pricing_procedure | ✅ 完成 |

---

## 5. 结论

**SD模块功能设计与数据库设计完全一致，无需补充修改。**

所有11个数据库表均在功能设计文档中有完整的功能说明：
- 销售订单：VA01/VA02/VA03
- 交货管理：VL01N/VL02N
- 开票管理：VF01/VF02
- 定价管理：VK11/VK12
- 信用管理：FD32

---

## 6. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-16 | 初始版本 - SD模块一致性检查完成 |
