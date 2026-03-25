# MM 仓库管理设计

**模块**: Warehouse Management (仓库管理)
**对标**: SAP LE-WM (Logistics Execution - Warehouse Management)
**版本**: 1.0
**更新日期**: 2026-03-14

---

## 1. 概述

### 1.1 功能范围

仓库管理 (WM) 提供精细化的仓位管理功能：

| 功能 | 说明 | SAP 对标 |
|------|------|----------|
| 仓位管理 | 仓位主数据、容量管理 | LS01N/LS02N |
| 入库处理 | 收货上架、上架策略 | LT01/LT06 |
| 出库处理 | 拣配、拣配策略 | LT03/LT10 |
| 库内移动 | 转储、整理 | LT01 |
| 盘点 | 仓位盘点 | LI01N/LI02N |
| 仓库任务 | 转运需求、转运订单 | LT01/LT02 |

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    仓库管理架构 - 对标 SAP LE-WM                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         仓库结构                                     │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 仓库号   │  │ 存储类型 │  │ 存储区域 │  │ 仓位     │            │   │
│  │  │ LGNUM    │  │ LGTYP    │  │ LGBER    │  │ LGPLA    │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         入库处理                                     │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 收货通知 │  │ 上架策略 │  │ 仓库任务 │  │ 上架确认 │            │   │
│  │  │ TR       │  │ Putaway  │  │ TO       │  │ Confirm  │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         出库处理                                     │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐            │   │
│  │  │ 出库请求 │  │ 拣配策略 │  │ 拣配任务 │  │ 拣配确认 │            │   │
│  │  │ TR       │  │ Picking  │  │ TO       │  │ Confirm  │            │   │
│  │  └──────────┘  └──────────┘  └──────────┘  └──────────┘            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. 仓库结构

### 2.1 仓库号 (对标 SAP LGNUM)

| 字段 | 类型 | 说明 |
|------|------|------|
| warehouse_number | VARCHAR(3) | 仓库号 |
| description | VARCHAR(50) | 描述 |
| plant | VARCHAR(4) | 工厂 |
| storage_location | VARCHAR(4) | 存储地点 |
| yard_management | VARCHAR(1) | 堆场管理 |
| door_management | VARCHAR(1) | 门管理 |
| status | VARCHAR(1) | 状态 |

### 2.2 存储类型 (对标 SAP T301)

| 字段 | 类型 | 说明 |
|------|------|------|
| warehouse_number | VARCHAR(3) | 仓库号 |
| storage_type | VARCHAR(3) | 存储类型 |
| description | VARCHAR(50) | 描述 |
| role | VARCHAR(1) | 角色 (S:标准/P:拣配/G:收货) |
| storage_type_category | VARCHAR(1) | 类型类别 |
| bin_type | VARCHAR(1) | 仓位类型 |
| stock_placement | VARCHAR(1) | 上架策略 |
| stock_removal | VARCHAR(1) | 拣配策略 |
| block_reason | VARCHAR(2) | 冻结原因 |
| capacity_check | VARCHAR(1) | 容量检查 |

### 2.3 存储类型列表

| 类型 | 名称 | 说明 |
|------|------|------|
| 001 | 货架存储 | 标准货架存储 |
| 002 | 地面存储 | 地面堆放区 |
| 003 | 高位货架 | 高位货架存储 |
| 010 | 暂存区 | 临时存储区 |
| 020 | 收货区 | 收货暂存 |
| 030 | 发货区 | 发货准备区 |
| 040 | 质检区 | 质量检验 |
| 050 | 冻结区 | 冻结库存 |
| 060 | 寄售区 | 供应商寄售 |
| 090 | 差异区 | 盘点差异 |
| 100 | 退货区 | 退货处理 |

### 2.4 存储区域 (对标 SAP T302)

| 字段 | 类型 | 说明 |
|------|------|------|
| warehouse_number | VARCHAR(3) | 仓库号 |
| storage_type | VARCHAR(3) | 存储类型 |
| storage_section | VARCHAR(4) | 存储区域 |
| description | VARCHAR(50) | 描述 |

### 2.5 仓位 (对标 SAP LQUA)

| 字段 | 类型 | 说明 |
|------|------|------|
| warehouse_number | VARCHAR(3) | 仓库号 |
| storage_type | VARCHAR(3) | 存储类型 |
| storage_bin | VARCHAR(10) | 仓位 |
| storage_section | VARCHAR(4) | 存储区域 |
| bin_type | VARCHAR(1) | 仓位类型 |
| material | VARCHAR(18) | 物料编码 |
| batch | VARCHAR(10) | 批次号 |
| special_stock | VARCHAR(1) | 特殊库存 |
| stock_category | VARCHAR(1) | 库存类别 |
| quantity | DECIMAL(13,3) | 数量 |
| unit | VARCHAR(3) | 单位 |
| quantity_available | DECIMAL(13,3) | 可用数量 |
| quantity_picking | DECIMAL(13,3) | 拣配中数量 |
| last_change_date | DATE | 最后修改日期 |
| posting_date | DATE | 过账日期 |
| blocked | VARCHAR(1) | 冻结标识 |
| stock_removal | VARCHAR(1) | 拣配控制 |

### 2.6 仓位编码

```
仓位编码结构:
┌─────────────────────────────────────────────────────────────────┐
│ 示例: 001-01-A-003                                              │
│                                                                 │
│ 001 : 存储类型 (高位货架)                                       │
│  01 : 走道 (第1走道)                                            │
│   A : 货架层 (A层)                                              │
│  003 : 仓位序号 (第3仓位)                                       │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│ 坐标系编码:                                                     │
│                                                                 │
│      ┌───┬───┬───┬───┬───┐                                     │
│   5  │   │   │   │   │   │                                     │
│      ├───┼───┼───┼───┼───┤                                     │
│   4  │   │   │   │   │   │                                     │
│      ├───┼───┼───┼───┼───┤                                     │
│   3  │   │   │   │   │   │                                     │
│      ├───┼───┼───┼───┼───┤                                     │
│   2  │   │   │ X │   │   │  ← 01-A-02-03 (走道-层-列-位)      │
│      ├───┼───┼───┼───┼───┤                                     │
│   1  │   │   │   │   │   │                                     │
│      └───┴───┴───┴───┴───┘                                     │
│        1   2   3   4   5                                        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. 仓库任务

### 3.1 转运需求 (对标 SAP LTBK/LTBP)

| 字段 | 类型 | 说明 |
|------|------|------|
| transfer_requirement | VARCHAR(10) | 转运需求号 |
| tr_type | VARCHAR(1) | 需求类型 (P:拣配/S:上架) |
| warehouse_number | VARCHAR(3) | 仓库号 |
| material_document | VARCHAR(10) | 物料凭证号 |
| fiscal_year | INTEGER | 会计年度 |
| plant | VARCHAR(4) | 工厂 |
| storage_location | VARCHAR(4) | 存储地点 |
| status | VARCHAR(1) | 状态 |
| created_date | DATE | 创建日期 |
| created_time | TIME | 创建时间 |
| created_by | VARCHAR(12) | 创建人 |

### 3.2 转运订单 (对标 SAP LTAK/LTAP)

**转运订单头:**

| 字段 | 类型 | 说明 |
|------|------|------|
| transfer_order | VARCHAR(10) | 转运订单号 |
| warehouse_number | VARCHAR(3) | 仓库号 |
| to_type | VARCHAR(1) | 订单类型 (P:拣配/S:上架) |
| transfer_requirement | VARCHAR(10) | 转运需求号 |
| material_document | VARCHAR(10) | 物料凭证号 |
| status | VARCHAR(2) | 状态 |
| priority | VARCHAR(1) | 优先级 |
| source_storage_type | VARCHAR(3) | 源存储类型 |
| source_storage_bin | VARCHAR(10) | 源仓位 |
| dest_storage_type | VARCHAR(3) | 目标存储类型 |
| dest_storage_bin | VARCHAR(10) | 目标仓位 |
| total_quantity | DECIMAL(13,3) | 总数量 |
| confirmed_quantity | DECIMAL(13,3) | 已确认数量 |
| created_date | DATE | 创建日期 |
| created_time | TIME | 创建时间 |
| confirmed_date | DATE | 确认日期 |
| confirmed_time | TIME | 确认时间 |
| confirmed_by | VARCHAR(12) | 确认人 |

**转运订单项:**

| 字段 | 类型 | 说明 |
|------|------|------|
| transfer_order | VARCHAR(10) | 转运订单号 |
| to_item | INTEGER | 行号 |
| material | VARCHAR(18) | 物料编码 |
| batch | VARCHAR(10) | 批次号 |
| quantity | DECIMAL(13,3) | 数量 |
| unit | VARCHAR(3) | 单位 |
| source_storage_type | VARCHAR(3) | 源存储类型 |
| source_storage_bin | VARCHAR(10) | 源仓位 |
| dest_storage_type | VARCHAR(3) | 目标存储类型 |
| dest_storage_bin | VARCHAR(10) | 目标仓位 |
| stock_category | VARCHAR(1) | 库存类别 |
| special_stock | VARCHAR(1) | 特殊库存 |
| status | VARCHAR(1) | 状态 |
| confirmed_quantity | DECIMAL(13,3) | 确认数量 |
| difference_quantity | DECIMAL(13,3) | 差异数量 |
| difference_reason | VARCHAR(4) | 差异原因 |

---

## 4. 上架策略

### 4.1 上架策略配置 (对标 SAP T30A)

| 策略 | 说明 | 逻辑 |
|------|------|------|
| A | 固定仓位 | 使用物料主数据定义的固定仓位 |
| P | 下一空仓 | 按仓位编码顺序找空仓 |
| I | 增加库存 | 合并到现有库存仓位 |
| C | 复杂策略 | 考虑物料特性、仓位类型等 |
| Z | 用户定义 | 自定义策略 |

### 4.2 上架流程

```
上架流程:
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│   1. 收货过账 (IM)                                              │
│      │ 生成物料凭证                                             │
│      │                                                          │
│      ▼                                                          │
│   2. 创建转运需求 (TR)                                          │
│      │ 系统自动或手工创建                                       │
│      │                                                          │
│      ▼                                                          │
│   3. 确定目标仓位                                               │
│      │ 执行上架策略                                             │
│      │                                                          │
│      ▼                                                          │
│   4. 创建转运订单 (TO)                                          │
│      │ 包含源仓位和目标仓位                                     │
│      │                                                          │
│      ▼                                                          │
│   5. 执行上架                                                   │
│      │ 移动物料到目标仓位                                       │
│      │                                                          │
│      ▼                                                          │
│   6. 确认TO                                                     │
│      │ 更新WM仓位库存                                           │
│      │                                                          │
│      ▼                                                          │
│   7. IM/WM同步                                                  │
│      更新IM存储地点库存                                         │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 5. 拣配策略

### 5.1 拣配策略配置 (对标 SAP T30B)

| 策略 | 说明 | 逻辑 |
|------|------|------|
| F | 先进先出 | FIFO，按入库日期拣配 |
| L | 后进先出 | LIFO，最后入库先拣配 |
| H | 最早失效 | FEFO，按有效期拣配 |
| P | 固定仓位 | 从固定仓位拣配 |
| Z | 用户定义 | 自定义策略 |

### 5.2 拣配流程

```
拣配流程:
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│   1. 出库请求                                                   │
│      │ 销售订单/发货单/预留等                                   │
│      │                                                          │
│      ▼                                                          │
│   2. 创建转运需求 (TR)                                          │
│      │ 系统自动或手工创建                                       │
│      │                                                          │
│      ▼                                                          │
│   3. 拣配策略执行                                               │
│      │ 确定拣配仓位                                             │
│      │                                                          │
│      ▼                                                          │
│   4. 创建拣配TO                                                 │
│      │ 源仓位 → 拣配区/发货区                                   │
│      │                                                          │
│      ▼                                                          │
│   5. 打印拣配单                                                 │
│      │ 包含仓位、物料、数量                                     │
│      │                                                          │
│      ▼                                                          │
│   6. 执行拣配                                                   │
│      │ 从源仓位拣选物料                                         │
│      │                                                          │
│      ▼                                                          │
│   7. 确认TO                                                     │
│      │ 更新仓位库存                                             │
│      │                                                          │
│      ▼                                                          │
│   8. 发货过账 (IM)                                              │
│      生成物料凭证，更新库存                                     │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 6. 容量管理

### 6.1 仓位容量 (对标 SAP LEIN)

| 字段 | 类型 | 说明 |
|------|------|------|
| warehouse_number | VARCHAR(3) | 仓库号 |
| storage_type | VARCHAR(3) | 存储类型 |
| storage_bin | VARCHAR(10) | 仓位 |
| max_weight | DECIMAL(13,3) | 最大承重 |
| max_volume | DECIMAL(13,3) | 最大体积 |
| weight_unit | VARCHAR(3) | 重量单位 |
| volume_unit | VARCHAR(3) | 体积单位 |
| max_pallets | INTEGER | 最大托盘数 |
| occupied_weight | DECIMAL(13,3) | 已用重量 |
| occupied_volume | DECIMAL(13,3) | 已用体积 |
| occupied_pallets | INTEGER | 已用托盘数 |
| utilization_percent | DECIMAL(5,2) | 利用率% |

### 6.2 容量检查

| 检查类型 | 说明 | 触发点 |
|----------|------|--------|
| 重量检查 | 检查重量限制 | 上架 |
| 体积检查 | 检查体积限制 | 上架 |
| 托盘检查 | 检查托盘限制 | 上架 |
| 数量检查 | 检查数量限制 | 上架 |

---

## 7. WM 盘点

### 7.1 WM 盘点凭证 (对标 SAP LIKP)

| 字段 | 类型 | 说明 |
|------|------|------|
| phys_inventory_number | VARCHAR(10) | 盘点凭证号 |
| warehouse_number | VARCHAR(3) | 仓库号 |
| storage_type | VARCHAR(3) | 存储类型 |
| storage_bin | VARCHAR(10) | 仓位 |
| count_date | DATE | 盘点日期 |
| status | VARCHAR(1) | 状态 |
| created_date | DATE | 创建日期 |
| created_by | VARCHAR(12) | 创建人 |

### 7.2 WM 盘点项目

| 字段 | 类型 | 说明 |
|------|------|------|
| phys_inventory_number | VARCHAR(10) | 盘点凭证号 |
| item | INTEGER | 行号 |
| material | VARCHAR(18) | 物料编码 |
| batch | VARCHAR(10) | 批次号 |
| book_quantity | DECIMAL(13,3) | 账面数量 |
| counted_quantity | DECIMAL(13,3) | 实盘数量 |
| difference_quantity | DECIMAL(13,3) | 差异数量 |
| unit | VARCHAR(3) | 单位 |
| counted_date | DATE | 盘点日期 |
| counted_by | VARCHAR(12) | 盘点人 |

---

## 8. 仓库监控

### 8.1 仓位使用率

```
仓位使用率分析:
┌─────────────────────────────────────────────────────────────────────────────┐
│ 仓库: 001 - 主仓库                                                          │
├─────────────────────────────────────────────────────────────────────────────┤
│ 存储类型    │ 总仓位  │ 已使用  │ 空仓位  │ 使用率  │ 容量利用率            │
├─────────────┼─────────┼─────────┼─────────┼─────────┼───────────────────────┤
│ 001 货架    │   500   │   420   │    80   │  84%    │  72%                  │
│ 002 地面    │   100   │    85   │    15   │  85%    │  65%                  │
│ 003 高位    │   200   │   180   │    20   │  90%    │  78%                  │
│ 010 暂存    │    50   │    35   │    15   │  70%    │  45%                  │
├─────────────┼─────────┼─────────┼─────────┼─────────┼───────────────────────┤
│ 合计        │   850   │   720   │   130   │  85%    │  72%                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 8.2 待处理任务

| 任务类型 | 待处理数 | 紧急数 | 今日到期 |
|----------|----------|--------|----------|
| 上架TO | 45 | 5 | 12 |
| 拣配TO | 78 | 8 | 25 |
| 转储TO | 12 | 0 | 5 |

---

## 9. 接口设计

### 9.1 仓位接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/wm/storage-bins | GET | 仓位列表 |
| /api/wm/storage-bins | POST | 创建仓位 |
| /api/wm/storage-bins/{id} | GET | 仓位详情 |
| /api/wm/storage-bins/{id}/stock | GET | 仓位库存 |
| /api/wm/storage-bins/{id}/block | POST | 冻结仓位 |

### 9.2 仓库任务接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/wm/transfer-requirements | GET/POST | 转运需求 |
| /api/wm/transfer-orders | GET/POST | 转运订单 |
| /api/wm/transfer-orders/{id}/confirm | POST | 确认TO |
| /api/wm/transfer-orders/{id}/cancel | POST | 取消TO |

### 9.3 上架/拣配接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/wm/putaway/create | POST | 创建上架任务 |
| /api/wm/putaway/strategy | POST | 执行上架策略 |
| /api/wm/picking/create | POST | 创建拣配任务 |
| /api/wm/picking/strategy | POST | 执行拣配策略 |

### 9.4 报表接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/wm/reports/bin-utilization | GET | 仓位利用率 |
| /api/wm/reports/pending-tasks | GET | 待处理任务 |
| /api/wm/reports/inventory-movement | GET | 库存移动分析 |

---

## 10. 相关文档

- [MM 模块总览](./00-MM-OVERVIEW.md)
- [库存管理](./03-MM-IM.md)

---

## 11. 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0 | 2026-03-14 | 初始版本 |
