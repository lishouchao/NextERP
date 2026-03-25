# SCM 模块一致性检查报告

**检查日期**: 2026-03-16
**数据库设计**: research/nexterp-database/docs/16-SCM-DESIGN.md
**关联模块**: MM, SD, PP, BP

---

## 1. 检查概述

本报告验证SCM模块数据库设计的内部一致性及与相关模块的引用一致性。

## 2. 数据库表检查

### 2.1 SCM模块表清单

| 序号 | 表名 | 说明 | 子模块 |
|------|------|------|--------|
| 1 | scm_demand_forecast | 需求预测 | 需求管理 |
| 2 | scm_forecast_item | 预测明细 | 需求管理 |
| 3 | scm_demand_plan | 需求计划 | 需求管理 |
| 4 | scm_demand_plan_item | 需求计划明细 | 需求管理 |
| 5 | scm_supply_plan | 供应计划 | 供应计划 |
| 6 | scm_supply_plan_item | 供应计划明细 | 供应计划 |
| 7 | scm_supply_network | 供应网络 | 供应计划 |
| 8 | scm_network_path | 供应网络路径 | 供应计划 |
| 9 | scm_inventory_policy | 库存策略 | 库存优化 |
| 10 | scm_safety_stock | 安全库存 | 库存优化 |
| 11 | scm_replenishment | 补货计划 | 库存优化 |
| 12 | scm_shipment | 发货单 | 物流管理 |
| 13 | scm_shipment_item | 发货明细 | 物流管理 |
| 14 | scm_tracking | 物流跟踪 | 物流管理 |
| 15 | scm_supplier_collab | 供应商协同 | 供应链协同 |
| 16 | scm_supplier_commit | 供应商库存承诺 | 供应链协同 |
| 17 | scm_vmi_inventory | VMI库存 | 供应链协同 |
| 18 | scm_kpi | 供应链KPI | 供应链分析 |
| 19 | scm_alert | 供应链预警 | 供应链分析 |

**检查结果**: ✅ 共19个表，命名规范一致 (scm_*)

## 3. 外键引用检查

### 3.1 引用核心模块

| SCM表 | 外键字段 | 引用表 | 引用模块 | 状态 |
|-------|----------|--------|----------|------|
| scm_forecast_item | product_id | mm_material | MM | ✅ 有效 |
| scm_forecast_item | plant_id | sys_plant | TENANT | ✅ 有效 |
| scm_forecast_item | customer_id | bp_business_partner | BP | ✅ 有效 |
| scm_forecast_item | region_id | core_region | CORE | ✅ 有效 |
| scm_forecast_item | currency_id | core_currency | CORE | ✅ 有效 |
| scm_demand_plan_item | product_id | mm_material | MM | ✅ 有效 |
| scm_demand_plan_item | plant_id | sys_plant | TENANT | ✅ 有效 |
| scm_demand_plan_item | customer_id | bp_business_partner | BP | ✅ 有效 |
| scm_supply_plan_item | product_id | mm_material | MM | ✅ 有效 |
| scm_supply_plan_item | plant_id | sys_plant | TENANT | ✅ 有效 |
| scm_supply_plan_item | supplier_id | bp_business_partner | BP | ✅ 有效 |
| scm_supply_network | plant_id | sys_plant | TENANT | ✅ 有效 |
| scm_supply_network | sloc_id | sys_storage_location | TENANT | ✅ 有效 |
| scm_supply_network | supplier_id | bp_business_partner | BP | ✅ 有效 |
| scm_supply_network | country_id | core_country | CORE | ✅ 有效 |
| scm_supply_network | region_id | core_region | CORE | ✅ 有效 |
| scm_inventory_policy | product_id | mm_material | MM | ✅ 有效 |
| scm_inventory_policy | plant_id | sys_plant | TENANT | ✅ 有效 |
| scm_inventory_policy | sloc_id | sys_storage_location | TENANT | ✅ 有效 |
| scm_safety_stock | product_id | mm_material | MM | ✅ 有效 |
| scm_safety_stock | plant_id | sys_plant | TENANT | ✅ 有效 |
| scm_safety_stock | sloc_id | sys_storage_location | TENANT | ✅ 有效 |
| scm_replenishment | product_id | mm_material | MM | ✅ 有效 |
| scm_replenishment | plant_id | sys_plant | TENANT | ✅ 有效 |
| scm_replenishment | sloc_id | sys_storage_location | TENANT | ✅ 有效 |
| scm_replenishment | source_plant_id | sys_plant | TENANT | ✅ 有效 |
| scm_replenishment | supplier_id | bp_business_partner | BP | ✅ 有效 |
| scm_shipment | ship_from_plant | sys_plant | TENANT | ✅ 有效 |
| scm_shipment | ship_from_sloc | sys_storage_location | TENANT | ✅ 有效 |
| scm_shipment | ship_to_party | bp_business_partner | BP | ✅ 有效 |
| scm_shipment | ship_to_country | core_country | CORE | ✅ 有效 |
| scm_shipment | carrier_id | bp_business_partner | BP | ✅ 有效 |
| scm_shipment | currency_id | core_currency | CORE | ✅ 有效 |
| scm_shipment_item | product_id | mm_material | MM | ✅ 有效 |
| scm_supplier_collab | supplier_id | bp_business_partner | BP | ✅ 有效 |
| scm_supplier_collab | product_id | mm_material | MM | ✅ 有效 |
| scm_supplier_commit | supplier_id | bp_business_partner | BP | ✅ 有效 |
| scm_supplier_commit | product_id | mm_material | MM | ✅ 有效 |
| scm_supplier_commit | plant_id | sys_plant | TENANT | ✅ 有效 |
| scm_vmi_inventory | supplier_id | bp_business_partner | BP | ✅ 有效 |
| scm_vmi_inventory | product_id | mm_material | MM | ✅ 有效 |
| scm_vmi_inventory | plant_id | sys_plant | TENANT | ✅ 有效 |
| scm_vmi_inventory | sloc_id | sys_storage_location | TENANT | ✅ 有效 |
| scm_kpi | plant_id | sys_plant | TENANT | ✅ 有效 |
| scm_kpi | supplier_id | bp_business_partner | BP | ✅ 有效 |
| scm_alert | product_id | mm_material | MM | ✅ 有效 |
| scm_alert | plant_id | sys_plant | TENANT | ✅ 有效 |
| scm_alert | supplier_id | bp_business_partner | BP | ✅ 有效 |
| scm_alert | assigned_to | hr_employee | HR | ✅ 有效 |

**检查结果**: ✅ 所有外键引用均指向有效表

## 4. 枚举值一致性检查

### 4.1 预测类型 (forecast_type)

| 代码 | 说明 | 使用表 |
|------|------|--------|
| 01 | 销售预测 | scm_demand_forecast |
| 02 | 需求预测 | scm_demand_forecast |
| 03 | 补货预测 | scm_demand_forecast |

### 4.2 预测方法 (forecast_method)

| 代码 | 说明 | 使用表 |
|------|------|--------|
| 01 | 移动平均 | scm_demand_forecast |
| 02 | 指数平滑 | scm_demand_forecast |
| 03 | ARIMA | scm_demand_forecast |
| 04 | 机器学习 | scm_demand_forecast |
| 05 | 人工判断 | scm_demand_forecast |

### 4.3 期间类型 (period_type)

| 代码 | 说明 | 使用表 |
|------|------|--------|
| 01 | 日 | scm_demand_forecast, scm_kpi |
| 02 | 周 | scm_demand_forecast, scm_kpi |
| 03 | 月 | scm_demand_forecast, scm_kpi |
| 04 | 季 | scm_demand_forecast, scm_kpi |
| 05 | 年 | scm_demand_forecast, scm_kpi |

### 4.4 计划类型 (plan_type)

| 代码 | 说明 | 使用表 |
|------|------|--------|
| 01 | S&OP计划 | scm_demand_plan |
| 02 | 需求计划 | scm_demand_plan |
| 03 | 分销计划 | scm_demand_plan |
| 01 | MPS主生产计划 | scm_supply_plan |
| 02 | MRP物料需求计划 | scm_supply_plan |
| 03 | DRP分销需求计划 | scm_supply_plan |

### 4.5 供应来源 (supply_source)

| 代码 | 说明 | 使用表 |
|------|------|--------|
| 01 | 自制 | scm_supply_plan_item |
| 02 | 外购 | scm_supply_plan_item |
| 03 | 调拨 | scm_supply_plan_item |
| 04 | 寄售 | scm_supply_plan_item |

### 4.6 运输方式 (transport_mode)

| 代码 | 说明 | 使用表 |
|------|------|--------|
| 01 | 公路 | scm_network_path, scm_shipment |
| 02 | 铁路 | scm_network_path, scm_shipment |
| 03 | 海运 | scm_network_path, scm_shipment |
| 04 | 空运 | scm_network_path, scm_shipment |
| 05 | 快递 | scm_network_path, scm_shipment |

### 4.7 发货类型 (shipment_type)

| 代码 | 说明 | 使用表 |
|------|------|--------|
| 01 | 销售发货 | scm_shipment |
| 02 | 调拨发货 | scm_shipment |
| 03 | 退货发货 | scm_shipment |
| 04 | 寄售发货 | scm_shipment |

### 4.8 发货状态 (shipment_status)

| 代码 | 说明 | 使用表 |
|------|------|--------|
| 01 | 待发货 | scm_shipment |
| 02 | 已发货 | scm_shipment |
| 03 | 运输中 | scm_shipment |
| 04 | 已到达 | scm_shipment |
| 05 | 已签收 | scm_shipment |
| 06 | 已取消 | scm_shipment |

### 4.9 跟踪状态 (tracking_status)

| 代码 | 说明 | 使用表 |
|------|------|--------|
| 01 | 已揽收 | scm_tracking |
| 02 | 运输中 | scm_tracking |
| 03 | 中转 | scm_tracking |
| 04 | 派送中 | scm_tracking |
| 05 | 已签收 | scm_tracking |
| 06 | 异常 | scm_tracking |

### 4.10 预警类型 (alert_type)

| 代码 | 说明 | 使用表 |
|------|------|--------|
| 01 | 库存预警 | scm_alert |
| 02 | 供应短缺 | scm_alert |
| 03 | 订单延期 | scm_alert |
| 04 | 质量问题 | scm_alert |
| 05 | 价格波动 | scm_alert |

### 4.11 预警级别 (alert_level)

| 代码 | 说明 | 使用表 |
|------|------|--------|
| 01 | 紧急 | scm_alert |
| 02 | 重要 | scm_alert |
| 03 | 一般 | scm_alert |
| 04 | 提示 | scm_alert |

### 4.12 KPI类别 (kpi_category)

| 代码 | 说明 | 使用表 |
|------|------|--------|
| 01 | 预测准确率 | scm_kpi |
| 02 | 库存周转 | scm_kpi |
| 03 | 订单履行 | scm_kpi |
| 04 | 物流效率 | scm_kpi |
| 05 | 供应商绩效 | scm_kpi |

## 5. 字段类型一致性检查

### 5.1 标准字段

| 字段 | 类型 | 说明 | 状态 |
|------|------|------|------|
| tenant_id | UUID | 租户ID | ✅ 一致 |
| created_at | TIMESTAMP | 创建时间 | ✅ 一致 |
| updated_at | TIMESTAMP | 更新时间 | ✅ 一致 |
| created_by | UUID | 创建人 | ✅ 一致 |

### 5.2 数量字段

| 字段 | 类型 | 说明 | 状态 |
|------|------|------|------|
| qty | DECIMAL(13,3) | 数量 | ✅ 一致 |
| amount | DECIMAL(15,2) | 金额 | ✅ 一致 |
| percentage | DECIMAL(5,2) | 百分比 | ✅ 一致 |

### 5.3 编号字段

| 字段 | 类型 | 说明 | 状态 |
|------|------|------|------|
| *_number | VARCHAR(12) | 业务单号 | ✅ 一致 |
| *_code | VARCHAR(10) | 编码 | ✅ 一致 |

## 6. 内部一致性检查

### 6.1 主从表关系

| 主表 | 从表 | 关联字段 | 状态 |
|------|------|----------|------|
| scm_demand_forecast | scm_forecast_item | forecast_id | ✅ 有效 |
| scm_demand_plan | scm_demand_plan_item | plan_id | ✅ 有效 |
| scm_supply_plan | scm_supply_plan_item | plan_id | ✅ 有效 |
| scm_shipment | scm_shipment_item | shipment_id | ✅ 有效 |
| scm_supplier_collab | scm_supplier_commit | collab_id | ✅ 有效 |

### 6.2 自引用关系

| 表 | 自引用字段 | 说明 | 状态 |
|------|------------|------|------|
| scm_demand_forecast | baseline_id | 预测基线版本 | ✅ 有效 |
| scm_supply_network | - | 无自引用 | ✅ 正常 |

### 6.3 计算字段

| 表 | 字段 | 计算逻辑 | 状态 |
|------|------|----------|------|
| scm_demand_plan_item | open_qty | demand_qty - confirmed_qty - planned_qty | ✅ 正确 |
| scm_forecast_item | forecast_amount | forecast_qty * unit_price | ⚠️ 非自动计算 |
| scm_supplier_commit | open_qty | committed_qty - delivered_qty | ✅ 正确 |

## 7. 视图检查

### 7.1 视图清单

| 视图名 | 说明 | 基表 | 状态 |
|--------|------|------|------|
| v_scm_demand_summary | 需求计划汇总 | scm_demand_plan_item, scm_demand_plan | ✅ 有效 |
| v_scm_supply_analysis | 供应计划分析 | scm_supply_plan_item, scm_supply_plan | ✅ 有效 |
| v_scm_inventory_opt | 库存优化视图 | scm_safety_stock, scm_inventory_policy | ✅ 有效 |

## 8. 存储过程检查

### 8.1 存储过程清单

| 函数名 | 说明 | 参数 | 状态 |
|--------|------|------|------|
| scm_calculate_safety_stock | 计算安全库存 | product_id, plant_id, service_level | ✅ 有效 |
| scm_run_replenishment_check | 运行补货检查 | plant_id, user_id | ✅ 有效 |

## 9. 检查总结

| 检查项 | 数量 | 状态 |
|--------|------|------|
| 数据库表 | 19 | ✅ 全部有效 |
| 外键引用 | 49 | ✅ 全部有效 |
| 枚举值 | 50+ | ✅ 全部一致 |
| 视图 | 3 | ✅ 全部有效 |
| 存储过程 | 2 | ✅ 全部有效 |

## 10. 结论

**✅ SCM模块数据库设计内部一致且与关联模块引用正确**

所有表命名规范、字段类型一致、外键引用有效、枚举值定义完整。

---

## 11. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-16 | 初始版本 |
