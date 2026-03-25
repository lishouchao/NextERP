# MM 物料主数据设计

**模块**: Material Master (物料主数据)
**对标**: SAP MM - Material Master (MARA/MARC/MBEW)
**版本**: 1.0
**更新日期**: 2026-03-14

---

## 1. 概述

### 1.1 设计原则

物料主数据采用**多视图架构**，不同部门维护不同视图的数据：

| 视图 | 维护部门 | 层级 | SAP 对标表 |
|------|----------|------|------------|
| 基本数据 | 主数据团队 | 客户端 | MARA |
| 销售 | 销售部门 | 销售组织+分销渠道 | MVKE |
| 采购 | 采购部门 | 工厂 | MARC |
| MRP | 计划部门 | 工厂 | MARC |
| 会计 | 财务部门 | 评估范围 (工厂) | MBEW |
| 成本 | 成本会计 | 评估范围 (工厂) | MBEW |
| 仓库 | 仓库管理 | 工厂+存储地点 | MLGN/MLGT |
| 质量 | 质量部门 | 工厂 | MARC/QMAT |

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    物料主数据多视图架构                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│    ┌──────────────────────────────────────────────────────────────────┐    │
│    │                     客户端层级 (Client Level)                     │    │
│    │  ┌─────────────────────────────────────────────────────────────┐ │    │
│    │  │ 基本数据视图                                                 │ │    │
│    │  │ 物料编码、描述、基本单位、物料组、物料类型                    │ │    │
│    │  │ (mm_material)                                               │ │    │
│    │  └─────────────────────────────────────────────────────────────┘ │    │
│    └──────────────────────────────────────────────────────────────────┘    │
│                                    │                                        │
│                    ┌───────────────┼───────────────┐                       │
│                    ▼               ▼               ▼                       │
│    ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐        │
│    │ 销售视图         │  │ 工厂视图         │  │ 评估视图         │        │
│    │ (销售组织层级)   │  │ (工厂层级)       │  │ (评估范围层级)   │        │
│    │ ┌──────────────┐ │  │ ┌──────────────┐ │  │ ┌──────────────┐ │        │
│    │ │ 销售         │ │  │ │ 采购         │ │  │ │ 会计         │ │        │
│    │ │ MVKE         │ │  │ │ MARC         │ │  │ │ MBEW         │ │        │
│    │ └──────────────┘ │  │ └──────────────┘ │  │ └──────────────┘ │        │
│    │ ┌──────────────┐ │  │ ┌──────────────┐ │  │ ┌──────────────┐ │        │
│    │ │ 销售一般     │ │  │ │ MRP          │ │  │ │ 成本         │ │        │
│    │ │ 销售:工厂    │ │  │ │ 仓库         │ │  │ │ (评估类)     │ │        │
│    │ └──────────────┘ │  │ │ 质量         │ │  │ └──────────────┘ │        │
│    └──────────────────┘  │ └──────────────┘ │  └──────────────────┘        │
│                          └──────────────────┘                               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 物料基本数据

### 2.1 物料主记录 (对标 SAP MARA)

| 字段 | 类型 | 说明 |
|------|------|------|
| material_number | VARCHAR(18) | 物料编码 |
| material_type | VARCHAR(4) | 物料类型 (ROH/HALB/FERT/VERP) |
| industry_sector | VARCHAR(1) | 行业部门 (M:机械 C:化工 R:零售) |
| material_group | VARCHAR(9) | 物料组 |
| description | VARCHAR(40) | 物料描述 |
| description_en | VARCHAR(40) | 英文描述 |
| base_uom | VARCHAR(3) | 基本单位 |
| order_uom | VARCHAR(3) | 订单单位 |
| gross_weight | DECIMAL(13,3) | 毛重 |
| net_weight | DECIMAL(13,3) | 净重 |
| weight_unit | VARCHAR(3) | 重量单位 |
| volume | DECIMAL(13,3) | 体积 |
| volume_unit | VARCHAR(3) | 体积单位 |
| ean_upc | VARCHAR(18) | EAN/UPC条码 |
| old_mat_no | VARCHAR(40) | 旧物料号 |
| lab_office | VARCHAR(3) | 实验室/办公室 |
| division | VARCHAR(2) | 产品组 |
| product_hierarchy | VARCHAR(18) | 产品层次 |
| cross_plant_status | VARCHAR(1) | 跨工厂状态 |
| valid_from | DATE | 生效日期 |
| valid_to | DATE | 失效日期 |
| created_date | DATE | 创建日期 |
| created_by | VARCHAR(12) | 创建人 |
| last_change_date | DATE | 最后修改日期 |
| changed_by | VARCHAR(12) | 修改人 |

### 2.2 物料类型

| 类型 | 名称 | 说明 |
|------|------|------|
| ROH | 原材料 | Raw Material |
| HALB | 半成品 | Semi-finished Product |
| FERT | 成品 | Finished Product |
| VERP | 包装材料 | Packaging Material |
| HIBE | 易耗品 | Operating Supply |
| DIEN | 服务 | Service |
| NLAG | 非库存物料 | Non-stock Material |
| UNBW | 非估值物料 | Non-valuated Material |
| LEER | 空容器 | Empties |
| FRAS | 备件 | Spare Parts |

### 2.3 物料组

```
物料组层次结构:
┌─────────────────────────────────────────────────────────────────┐
│ 物料组 (Material Group)                                         │
│                                                                 │
│ 001          机械设备                                           │
│ ├── 001001   电机设备                                           │
│ │   ├── 001001001  交流电机                                     │
│ │   └── 001001002  直流电机                                     │
│ ├── 001002   泵类设备                                           │
│ │   ├── 001002001  离心泵                                       │
│ │   └── 001002002  螺杆泵                                       │
│ └── ...                                                         │
│                                                                 │
│ 002          电子元器件                                         │
│ ├── 002001   电阻电容                                           │
│ ├── 002002   集成电路                                           │
│ └── ...                                                         │
│                                                                 │
│ 003          化工原料                                           │
│ ...                                                             │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. 工厂视图

### 3.1 物料-工厂数据 (对标 SAP MARC)

| 字段 | 类型 | 说明 |
|------|------|------|
| material_number | VARCHAR(18) | 物料编码 |
| plant | VARCHAR(4) | 工厂 |
| status_plant | VARCHAR(1) | 工厂状态 |
| abc_indicator | VARCHAR(1) | ABC分类 |
| critical_part | VARCHAR(1) | 关键部件标识 |
| mrp_type | VARCHAR(4) | MRP类型 |
| mrp_controller | VARCHAR(3) | MRP控制员 |
| lot_size_procedure | VARCHAR(4) | 批量过程 |
| min_lot_size | DECIMAL(13,3) | 最小批量 |
| max_lot_size | DECIMAL(13,3) | 最大批量 |
| fixed_lot_size | DECIMAL(13,3) | 固定批量 |
| rounding_profile | VARCHAR(4) | 舍入配置 |
| safety_stock | DECIMAL(13,3) | 安全库存 |
| min_safety_stock | DECIMAL(13,3) | 最小安全库存 |
| reorder_point | DECIMAL(13,3) | 再订货点 |
| planned_deliv_time | DECIMAL(3,0) | 计划交货时间(天) |
| gr_processing_time | DECIMAL(2,0) | 收货处理时间 |
| safety_time_profile | VARCHAR(3) | 安全时间配置 |
| planning_time_fence | VARCHAR(3) | 计划时栅 |
| planning_material | VARCHAR(18) | 计划物料 |
| planning_plant | VARCHAR(4) | 计划工厂 |
| production_scheduler | VARCHAR(3) | 生产调度员 |
| production_supervisor | VARCHAR(3) | 生产主管 |
| total_repl_lead_time | DECIMAL(3,0) | 总补货提前期 |
| procurement_type | VARCHAR(1) | 采购类型 (E:外部/F:内部/X:两者) |
| special_procurement | VARCHAR(2) | 特殊采购类型 |
| issuing_plant | VARCHAR(4) | 发货工厂 |
| storage_location | VARCHAR(4) | 存储地点 |
| backflush | VARCHAR(1) | 倒冲标识 |
| availability_check | VARCHAR(2) | 可用性检查 |
| serial_number_profile | VARCHAR(4) | 序列号配置 |
| batch_management | VARCHAR(1) | 批次管理 |
| profit_center | VARCHAR(10) | 利润中心 |
| commodity_code | VARCHAR(17) | 商品代码 |
| ctrl_code_for_intrastat | VARCHAR(1) | INTRASTAT控制码 |
| country_of_origin | VARCHAR(3) | 原产国 |

### 3.2 ABC分类

| 分类 | 说明 | 管理策略 |
|------|------|----------|
| A | 高价值/高用量 | 严格控制，低安全库存 |
| B | 中等价值/用量 | 适度控制 |
| C | 低价值/高用量 | 简化控制，高安全库存 |
| D | 低周转物料 | 评估是否淘汰 |

---

## 4. 销售视图

### 4.1 物料-销售组织数据 (对标 SAP MVKE)

| 字段 | 类型 | 说明 |
|------|------|------|
| material_number | VARCHAR(18) | 物料编码 |
| sales_org | VARCHAR(4) | 销售组织 |
| distr_channel | VARCHAR(2) | 分销渠道 |
| status_sales | VARCHAR(1) | 销售状态 |
| delivering_plant | VARCHAR(4) | 发货工厂 |
| sales_unit | VARCHAR(3) | 销售单位 |
| min_order_qty | DECIMAL(13,3) | 最小订单量 |
| min_deliv_qty | DECIMAL(13,3) | 最小发货量 |
| max_deliv_qty | DECIMAL(13,3) | 最大发货量 |
| delivery_unit | DECIMAL(13,3) | 交货单位 |
| delivery_time_rule | VARCHAR(2) | 交货时间规则 |
| rounding_profile | VARCHAR(4) | 舍入配置 |
| sales_status | VARCHAR(1) | 销售状态 |
| sales_status_valid_from | DATE | 销售状态生效日 |
| pricing_group | VARCHAR(2) | 定价组 |
| price_group | VARCHAR(2) | 价格组 |
| cust_price_group | VARCHAR(2) | 客户价格组 |
| item_category_group | VARCHAR(4) | 项目类别组 |
| account_assignment_group | VARCHAR(2) | 科目分配组 |
| product_hierarchy | VARCHAR(18) | 产品层次 |
| material_pricing_group | VARCHAR(2) | 物料定价组 |
| material_group_1 | VARCHAR(3) | 物料组1 |
| material_group_2 | VARCHAR(3) | 物料组2 |
| material_group_3 | VARCHAR(3) | 物料组3 |
| material_group_4 | VARCHAR(3) | 物料组4 |
| material_group_5 | VARCHAR(3) | 物料组5 |
| gen_item_category_group | VARCHAR(4) | 通用项目类别组 |

### 4.2 分销渠道

| 渠道 | 说明 |
|------|------|
| 01 | 批发 |
| 02 | 零售 |
| 03 | 直销 |
| 04 | 代理 |
| 05 | 电商 |

---

## 5. 采购视图

### 5.1 物料-采购数据

| 字段 | 类型 | 说明 |
|------|------|------|
| material_number | VARCHAR(18) | 物料编码 |
| plant | VARCHAR(4) | 工厂 |
| purchasing_group | VARCHAR(3) | 采购组 |
| purchasing_value_key | VARCHAR(4) | 采购值键 |
| planned_deliv_time | DECIMAL(3,0) | 计划交货时间 |
| tolerance_key_overdeliv | VARCHAR(4) | 超额交货容差 |
| tolerance_key_underdeliv | VARCHAR(4) | 欠交货容差 |
| unit_of_issue | VARCHAR(3) | 发料单位 |
| qty_per_unit_of_issue | DECIMAL(13,3) | 发料单位数量 |
| safety_time_period_profile | VARCHAR(3) | 安全时间期间配置 |
| source_list_requirement | VARCHAR(1) | 采购货源清单需求 |
| pricing_ref_material | VARCHAR(18) | 定价参考物料 |
| freight_group | VARCHAR(4) | 运费组 |
| loading_group | VARCHAR(4) | 装载组 |
| purchasing_status | VARCHAR(1) | 采购状态 |

### 5.2 采购组

```
采购组织架构:
┌─────────────────────────────────────────────────────────────────┐
│ 采购组织                                                        │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 采购组 (Purchasing Group)                                  │ │
│  │ - 负责特定物料类别的采购                                    │ │
│  │ - 维护供应商关系                                            │ │
│  │ - 执行采购活动                                              │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  示例:                                                          │
│  P01 - 原材料采购组                                             │
│  P02 - 设备采购组                                               │
│  P03 - MRO采购组                                                │
│  P04 - 服务采购组                                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## 6. 会计视图

### 6.1 物料评估数据 (对标 SAP MBEW)

| 字段 | 类型 | 说明 |
|------|------|------|
| material_number | VARCHAR(18) | 物料编码 |
| valuation_area | VARCHAR(4) | 评估范围 (工厂) |
| valuation_type | VARCHAR(10) | 评估类型 |
| valuation_category | VARCHAR(1) | 评估类别 |
| price_control | VARCHAR(1) | 价格控制 (S:标准 V:移动平均) |
| moving_price | DECIMAL(12,2) | 移动平均价 |
| standard_price | DECIMAL(12,2) | 标准价 |
| valuation_class | VARCHAR(4) | 评估类 |
| price_unit | DECIMAL(5,0) | 价格单位 |
| lifo_valuation | VARCHAR(1) | LIFO评估 |
| devaluation_year | VARCHAR(4) | 贬值年度 |
| future_price | DECIMAL(12,2) | 未来价格 |
| future_price_valid_from | DATE | 未来价格生效日 |
| valid_from | DATE | 生效日期 |
| ml_active | VARCHAR(1) | 物料分类账激活 |
| ml_settlement_type | VARCHAR(1) | ML结算类型 |
| currency | VARCHAR(3) | 货币 |
| price_determination | VARCHAR(1) | 价格确定 |

### 6.2 评估类

| 评估类 | 说明 | 科目 |
|--------|------|------|
| 3000 | 原材料 | 原材料科目 |
| 3020 | 半成品 | 半成品科目 |
| 3030 | 成品 | 成品科目 |
| 3040 | 包装材料 | 包装材料科目 |
| 3050 | 易耗品 | 易耗品科目 |
| 3100 | 外部采购贸易商品 | 贸易商品科目 |

### 6.3 价格控制对比

```
标准价 vs 移动平均价:
┌─────────────────────────────────────────────────────────────────┐
│ 标准价 (Standard Price - S)                                     │
├─────────────────────────────────────────────────────────────────┤
│ 特点:                                                           │
│ - 库存按固定标准价评估                                          │
│ - 采购价格差异计入价格差异科目                                  │
│ - 期间内价格不变                                                │
│                                                                 │
│ 适用场景:                                                       │
│ - 原材料价格波动小                                              │
│ - 成本控制严格                                                  │
│ - 需要稳定的成本核算                                            │
│                                                                 │
│ 收货: 借: 库存 (标准价)  贷: GR/IR (实际价)                     │
│       差异计入价格差异科目                                       │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│ 移动平均价 (Moving Average Price - V)                           │
├─────────────────────────────────────────────────────────────────┤
│ 特点:                                                           │
│ - 库存按实际采购价评估                                          │
│ - 价格随采购自动更新                                            │
│ - 库存价值反映真实成本                                          │
│                                                                 │
│ 适用场景:                                                       │
│ - 价格波动大                                                    │
│ - 需要实时成本                                                  │
│ - 贸易商品                                                      │
│                                                                 │
│ 收货: 借: 库存 (实际价)  贷: GR/IR (实际价)                     │
│       无价格差异                                                │
└─────────────────────────────────────────────────────────────────┘
```

---

## 7. 批次管理

### 7.1 批次主数据 (对标 SAP MCHA)

| 字段 | 类型 | 说明 |
|------|------|------|
| material_number | VARCHAR(18) | 物料编码 |
| plant | VARCHAR(4) | 工厂 |
| batch_number | VARCHAR(10) | 批次号 |
| batch_status | VARCHAR(1) | 批次状态 |
| valid_from | DATE | 生效日期 |
| valid_to | DATE | 失效日期 |
| shelf_life_expiration | DATE | 有效期至 |
| production_date | DATE | 生产日期 |
| country_of_origin | VARCHAR(3) | 原产国 |
| supplier_batch | VARCHAR(15) | 供应商批次 |
| inspection_lot | VARCHAR(12) | 检验批 |
| free_usage_date | DATE | 自由使用日期 |
| next_inspection_date | DATE | 下次检验日期 |
| created_on | DATE | 创建日期 |
| changed_on | DATE | 修改日期 |

### 7.2 批次分类

| 类别 | 说明 | 特征 |
|------|------|------|
| 等级 | 质量等级 | A/B/C |
| 产地 | 生产地点 | 工厂代码 |
| 颜色 | 外观特征 | 颜色代码 |
| 尺寸 | 规格特征 | 尺寸范围 |

---

## 8. 单位转换

### 8.1 计量单位转换 (对标 SAP MARM)

| 字段 | 类型 | 说明 |
|------|------|------|
| material_number | VARCHAR(18) | 物料编码 |
| from_uom | VARCHAR(3) | 源单位 |
| to_uom | VARCHAR(3) | 目标单位 |
| numerator | DECIMAL(13,3) | 分子 |
| denominator | DECIMAL(13,3) | 分母 |
| is_rounding | VARCHAR(1) | 是否舍入 |

### 8.2 常用单位转换示例

```
单位转换示例:
┌─────────────────────────────────────────────────────────────────┐
│ 物料: 螺栓 M8x20                                                │
│                                                                 │
│ 基本单位: PC (个)                                               │
│ 订单单位: BOX (盒)                                              │
│                                                                 │
│ 转换关系:                                                       │
│ 1 BOX = 100 PC                                                  │
│ 分子: 100                                                       │
│ 分母: 1                                                         │
│                                                                 │
│ 发货单位: KAR (卡)                                              │
│ 1 KAR = 10 BOX = 1000 PC                                        │
│ 分子: 1000                                                      │
│ 分母: 1                                                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## 9. 物料扩展

### 9.1 扩展流程

```
物料扩展流程:
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│   1. 创建物料 (基本数据)                                        │
│      │                                                          │
│      ▼                                                          │
│   2. 扩展到工厂 (采购/MRP/会计视图)                             │
│      │                                                          │
│      ▼                                                          │
│   3. 扩展到存储地点 (仓库视图)                                  │
│      │                                                          │
│      ▼                                                          │
│   4. 扩展到销售组织 (销售视图)                                  │
│      │                                                          │
│      ▼                                                          │
│   5. 维护采购信息记录                                           │
│      │                                                          │
│      ▼                                                          │
│   6. 维护货源清单                                               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 9.2 物料状态控制

| 状态 | 说明 | 影响 |
|------|------|------|
| A | 激活 | 所有事务可用 |
| B | 新建 | 仅显示，不可用 |
| C | 冻结 | 不可用于新业务 |
| D | 删除标记 | 待删除 |
| E | 审核 | 等待审核 |

---

## 10. 接口设计

### 10.1 物料主数据接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/mm/materials | GET | 物料列表查询 |
| /api/mm/materials | POST | 创建物料 |
| /api/mm/materials/{id} | GET | 物料详情 |
| /api/mm/materials/{id} | PUT | 更新物料 |
| /api/mm/materials/{id} | DELETE | 删除物料 |
| /api/mm/materials/{id}/extend | POST | 扩展物料视图 |
| /api/mm/materials/{id}/views/{view} | GET | 获取视图数据 |
| /api/mm/materials/{id}/views/{view} | PUT | 更新视图数据 |
| /api/mm/materials/search | GET | 物料搜索 |
| /api/mm/materials/by-number/{number} | GET | 按物料号查询 |

### 10.2 批量操作接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/mm/materials/batch-create | POST | 批量创建 |
| /api/mm/materials/batch-update | POST | 批量更新 |
| /api/mm/materials/batch-extend | POST | 批量扩展 |
| /api/mm/materials/import | POST | 导入物料 |

---

## 11. 相关文档

- [MM 模块总览](./00-MM-OVERVIEW.md)
- [采购管理](./02-MM-PO.md)
- [库存管理](./03-MM-IM.md)

---

## 12. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
