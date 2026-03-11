# SAP ECC 到 S/4HANA 迁移文档

## 文档索引

| 文档 | 说明 |
|------|------|
| [ecc-vs-s4hana-comparison.md](./ecc-vs-s4hana-comparison.md) | **完整对比分析** - 架构、模块、性能全方位对比 |
| [quick-reference.md](./quick-reference.md) | **快速参考** - 表变化状态一览表 |
| [table-changes.md](./table-changes.md) | **表变化详情** - 废弃表、新增表、字段变化 |

## 核心变化速览

### 1. 架构变化

```
ECC (AnyDB)                    S/4HANA (HANA)
────────────                   ──────────────
行存储                    →    列存储
聚簇表                    →    透明表
聚合表                    →    实时计算
多表关联                  →    Universal Journal
```

### 2. 关键表变化

| 功能 | ECC | S/4HANA |
|------|-----|---------|
| 财务统一 | 多表 (GLT0, COEP, etc.) | ACDOCA |
| 条件 | KONV (聚簇) | PRCD_ELEMENTS |
| 客户/供应商 | KNA1/LFA1 | BUT000 (BP) |
| HR 薪酬 | PCL2 (聚簇) | HRPY_* 系列 |

### 3. 迁移检查清单

- [ ] 运行 SAP Readiness Check
- [ ] 识别使用聚簇表的代码
- [ ] 识别使用聚合表的代码
- [ ] 规划 BP 模型迁移
- [ ] 评估 KONV → PRCD_ELEMENTS 影响
- [ ] 测试自定义报表
- [ ] 规划 Fiori 应用部署

## 按模块迁移指南

### FI/CO 迁移

1. **必做**: GLT0/COSP/COSS → ACDOCA
2. **必做**: 聚簇表 BSEG 转透明表
3. **建议**: 客户/供应商 → BP 模型
4. **考虑**: 自定义报表重写为 CDS View

### HR/HCM 迁移

1. **自动**: PCL2 → HRPY_* 透明表
2. **兼容**: 信息类型表 (PAnnnn) 保持不变
3. **建议**: 使用 CDS Views 替代 ABAP 报表

### MM/SD 迁移

1. **必做**: KONV → PRCD_ELEMENTS
2. **可选**: FSCM 信贷管理迁移
3. **兼容**: 大部分主数据表保留

### PP 迁移

1. **建议**: 使用 MRP Live (MD01N)
2. **兼容**: BOM/工艺路线表保留
3. **可选**: 启用 PP/DS

## 性能提升预期

| 场景 | ECC 时间 | S/4HANA 时间 | 提升倍数 |
|------|---------|-------------|---------|
| 总账查询 | 5-10s | <1s | 10x |
| MRP 运行 | 5-10min | 30-60s | 10x |
| 余额计算 | 30-60s | 1-3s | 20x |

## 迁移工具

```
RFINS_MIG_START        - 财务迁移启动器
CVI_EI_INBOUND_MAIN    - BP 迁移
COND_A_MIGRATION       - 条件迁移
RP_UPG_CONVERT_PCL2    - HR 聚簇转换
SAP_READINESS_CHECK    - 就绪检查
```

## 相关资源

- SAP S/4HANA Simplification List
- SAP Note 2233065 (System Conversion)
- SAP Note 2643115 (FI/CO Migration)
- SAP Help Portal
