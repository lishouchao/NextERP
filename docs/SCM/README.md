# SCM 供应链管理模块

**对标**: SAP SCM / Oracle SCM Cloud / Kinaxis
**版本**: 1.0
**更新日期**: 2026-03-16

---

## 模块概述

SCM (Supply Chain Management) 模块负责管理企业的供应链全流程，从需求预测到物流配送，提供端到端的供应链管理能力。

### 核心能力

| 能力 | 说明 | 对标 |
|------|------|------|
| 需求管理 | 需求预测与计划 | SAP DP |
| 供应计划 | MRP与产能规划 | SAP PP/DS |
| 库存优化 | 安全库存与补货 | SAP IO |
| 物流管理 | 发货与运输跟踪 | SAP TM |
| 供应链协同 | 供应商协同与VMI | SAP SNC |

---

## 文档索引

| 文档 | 说明 | 关键内容 |
|------|------|----------|
| [00-SCM-OVERVIEW.md](./00-SCM-OVERVIEW.md) | 模块总览 | 架构、S&OP流程、库存策略 |
| [01-SCM-DEMAND-PLANNING.md](./01-SCM-DEMAND-PLANNING.md) | 需求计划 | 需求预测、预测方法、准确率分析 |
| [02-SCM-SUPPLY-PLANNING.md](./02-SCM-SUPPLY-PLANNING.md) | 供应计划 | MRP计算、供应网络、产能规划 |
| [03-SCM-INVENTORY-OPTIMIZATION.md](./03-SCM-INVENTORY-OPTIMIZATION.md) | 库存优化 | 库存策略、安全库存、补货计划 |
| [04-SCM-LOGISTICS.md](./04-SCM-LOGISTICS.md) | 物流管理 | 发货管理、运输计划、物流跟踪 |
| [05-SCM-COLLABORATION.md](./05-SCM-COLLABORATION.md) | 供应链协同 | 供应商协同、VMI、CPFR |

---

## 数据模型

### 核心实体

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SCM 数据模型                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   需求管理                                                                   │
│   ┌─────────────┐     ┌─────────────┐                                     │
│   │ scm_demand │     │ scm_forecast │                                     │
│   │   _forecast │────►│    _item     │                                     │
│   └─────────────┘     └─────────────┘                                     │
│          │                                                                   │
│          ▼                                                                   │
│   ┌─────────────┐     ┌─────────────┐                                     │
│   │ scm_demand │     │ scm_demand  │                                     │
│   │   _plan    │────►│ _plan_item  │                                     │
│   └─────────────┘     └─────────────┘                                     │
│                                                                             │
│   供应计划                                                                   │
│   ┌─────────────┐     ┌─────────────┐                                     │
│   │ scm_supply │     │ scm_supply  │                                     │
│   │   _plan    │────►│ _plan_item  │                                     │
│   └─────────────┘     └─────────────┘                                     │
│          │                   │                                              │
│          ▼                   ▼                                              │
│   ┌─────────────┐     ┌─────────────┐                                     │
│   │ scm_supply │     │ scm_network │                                     │
│   │  _network  │────►│   _path     │                                     │
│   └─────────────┘     └─────────────┘                                     │
│                                                                             │
│   库存优化                                                                   │
│   ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                 │
│   │ scm_inventory│    │ scm_safety │     │scm_replenish│                 │
│   │   _policy   │     │   _stock   │     │    ment     │                 │
│   └─────────────┘     └─────────────┘     └─────────────┘                 │
│                                                                             │
│   物流管理                                                                   │
│   ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                 │
│   │ scm_shipment│────►│scm_shipment │────►│ scm_tracking│                 │
│   │             │     │    _item    │     │             │                 │
│   └─────────────┘     └─────────────┘     └─────────────┘                 │
│                                                                             │
│   供应链协同                                                                 │
│   ┌─────────────┐     ┌─────────────┐     ┌─────────────┐                 │
│   │scm_supplier │     │scm_supplier │     │scm_vmi_     │                 │
│   │  _collab    │────►│  _commit    │     │ inventory   │                 │
│   └─────────────┘     └─────────────┘     └─────────────┘                 │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 关键表清单

| 表名 | 说明 | 文档 |
|------|------|------|
| scm_demand_forecast | 需求预测 | 01-SCM-DEMAND-PLANNING.md |
| scm_forecast_item | 预测明细 | 01-SCM-DEMAND-PLANNING.md |
| scm_demand_plan | 需求计划 | 01-SCM-DEMAND-PLANNING.md |
| scm_demand_plan_item | 需求计划明细 | 01-SCM-DEMAND-PLANNING.md |
| scm_supply_plan | 供应计划 | 02-SCM-SUPPLY-PLANNING.md |
| scm_supply_plan_item | 供应计划明细 | 02-SCM-SUPPLY-PLANNING.md |
| scm_supply_network | 供应网络 | 02-SCM-SUPPLY-PLANNING.md |
| scm_network_path | 网络路径 | 02-SCM-SUPPLY-PLANNING.md |
| scm_inventory_policy | 库存策略 | 03-SCM-INVENTORY-OPTIMIZATION.md |
| scm_safety_stock | 安全库存 | 03-SCM-INVENTORY-OPTIMIZATION.md |
| scm_replenishment | 补货计划 | 03-SCM-INVENTORY-OPTIMIZATION.md |
| scm_shipment | 发货单 | 04-SCM-LOGISTICS.md |
| scm_shipment_item | 发货明细 | 04-SCM-LOGISTICS.md |
| scm_tracking | 物流跟踪 | 04-SCM-LOGISTICS.md |
| scm_supplier_collab | 供应商协同 | 05-SCM-COLLABORATION.md |
| scm_supplier_commit | 供应商承诺 | 05-SCM-COLLABORATION.md |
| scm_vmi_inventory | VMI库存 | 05-SCM-COLLABORATION.md |
| scm_kpi | 供应链KPI | 00-SCM-OVERVIEW.md |
| scm_alert | 供应链预警 | 00-SCM-OVERVIEW.md |

---

## 预测方法参考

| 方法 | 代码 | 说明 | 适用场景 |
|------|------|------|----------|
| 移动平均 | 01 | 简单平均 | 稳定需求 |
| 指数平滑 | 02 | 加权平均 | 有趋势需求 |
| 季节分解 | 03 | 季节调整 | 季节性需求 |
| ARIMA | 04 | 时间序列 | 复杂模式 |
| 机器学习 | 05 | ML模型 | 大数据量 |
| 人工判断 | 06 | 专家经验 | 新产品 |

---

## 运输方式参考

| 方式 | 代码 | 时效 | 成本 | 适用场景 |
|------|------|------|------|----------|
| 公路 | 01 | 1-3天 | 中 | 短途、门到门 |
| 铁路 | 02 | 3-7天 | 低 | 长途、大宗 |
| 海运 | 03 | 15-45天 | 极低 | 国际、大批量 |
| 空运 | 04 | 1-3天 | 高 | 紧急、高价值 |
| 快递 | 05 | 1-2天 | 极高 | 小件、紧急 |

---

## 与ERP集成

### 模块依赖关系

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SCM 与 ERP 集成                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   SCM 模块                           ERP 模块                                │
│  ┌─────────────┐                   ┌─────────────┐                          │
│  │ 需求预测     │◄─────────────────│ SD 销售订单  │                          │
│  │ Forecast    │    历史销售数据    │ Sales Order │                          │
│  └─────────────┘                   └─────────────┘                          │
│        │                                  │                                  │
│        ▼                                  ▼                                  │
│  ┌─────────────┐                   ┌─────────────┐                          │
│  │ 供应计划     │─────────────────►│ PP 生产计划  │                          │
│  │ Supply Plan │    计划订单        │ MRP/Planned │                          │
│  └─────────────┘                   └─────────────┘                          │
│        │                                  │                                  │
│        ▼                                  ▼                                  │
│  ┌─────────────┐                   ┌─────────────┐                          │
│  │ 库存优化     │◄────────────────►│ MM 库存管理  │                          │
│  │ Inventory   │    库存数据同步    │ Stock       │                          │
│  └─────────────┘                   └─────────────┘                          │
│        │                                  │                                  │
│        ▼                                  ▼                                  │
│  ┌─────────────┐                   ┌─────────────┐                          │
│  │ 物流管理     │─────────────────►│ WM 仓库管理  │                          │
│  │ Logistics   │    发货指令        │ Warehouse   │                          │
│  └─────────────┘                   └─────────────┘                          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## API 端点汇总

| 模块 | 基础路径 | 主要功能 |
|------|----------|----------|
| 预测 | /api/scm/forecasts | 预测CRUD、计算 |
| 需求计划 | /api/scm/demand-plans | 需求计划CRUD |
| 供应计划 | /api/scm/supply-plans | MRP运行、计划 |
| 库存策略 | /api/scm/inventory/policies | 策略CRUD |
| 安全库存 | /api/scm/inventory/safety-stock | 计算安全库存 |
| 补货 | /api/scm/inventory/replenishments | 补货CRUD |
| 发货 | /api/scm/shipments | 发货CRUD、跟踪 |
| 承运商 | /api/scm/carriers | 承运商管理 |
| 协同 | /api/scm/collaborations | 协同配置 |
| VMI | /api/scm/vmi | VMI库存管理 |
| 供应商门户 | /api/scm/supplier-portal | 供应商访问 |

---

## 相关文档

- [数据库设计](../../research/nexterp-database/docs/16-SCM-DESIGN.md)
- [架构决策](../ARCHITECTURE-DECISIONS.md)
- [MM物料管理](../MM/)
- [SD销售分销](../SD/)
- [PP生产计划](../PP/)

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-16 | 初始版本 |
