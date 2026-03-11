# ECC vs S/4HANA 表变化快速参考

## 核心表变化一览

### 财务会计 (FI)

| ECC 表 | S/4HANA | 状态 | 说明 |
|--------|---------|------|------|
| BKPF | BKPF | ✅ 保留 | 会计凭证头 |
| BSEG | BSEG | ⚠️ 透明表 | 聚簇表转透明表 |
| GLT0 | ACDOCA | ❌ 废弃 | 总账余额 → Universal Journal |
| FAGLFLEXT | ACDOCA | ❌ 废弃 | 新总账汇总 → Universal Journal |
| BSID | FQ Views | ❌ 废弃 | 应收未清项 |
| BSAD | FQ Views | ❌ 废弃 | 应收已清项 |
| BSIK | FQ Views | ❌ 废弃 | 应付未清项 |
| BSAK | FQ Views | ❌ 废弃 | 应付已清项 |
| KNC1 | ACDOCA | ❌ 废弃 | 客户余额 |
| LFC1 | ACDOCA | ❌ 废弃 | 供应商余额 |

### 管理会计 (CO)

| ECC 表 | S/4HANA | 状态 | 说明 |
|--------|---------|------|------|
| COEP | ACDOCA + View | ⚠️ 整合 | 成本控制行项目 |
| COEJ | ACDOCA | ❌ 废弃 | 成本控制期间总计 |
| COSP | ACDOCA | ❌ 废弃 | 成本控制总计 (主) |
| COSS | ACDOCA | ❌ 废弃 | 成本控制总计 (次) |
| COOI | ACDOCA + View | ⚠️ 整合 | 成本控制对象订单 |

### 资产会计 (AA)

| ECC 表 | S/4HANA | 状态 | 说明 |
|--------|---------|------|------|
| ANLA | ANLA | ✅ 保留 | 资产主数据 |
| ANLB | ANLB | ✅ 保留 | 折旧条款 |
| ANLC | ACDOCA + View | ⚠️ 整合 | 资产值字段 |
| ANEP | ACDOCA + View | ⚠️ 整合 | 资产行项目 |

### 业务伙伴 (BP)

| ECC 表 | S/4HANA | 状态 | 说明 |
|--------|---------|------|------|
| KNA1 | BUT000 + KNA1 | ⚠️ 建议迁移 | 客户通用 → BP 通用 |
| KNB1 | BUT000 + CVI | ⚠️ 建议迁移 | 客户公司代码 |
| KNVV | BUT000 + CVI | ⚠️ 建议迁移 | 客户销售范围 |
| LFA1 | BUT000 + LFA1 | ⚠️ 建议迁移 | 供应商通用 → BP 通用 |
| LFB1 | BUT000 + CVI | ⚠️ 建议迁移 | 供应商公司代码 |
| LFM1 | BUT000 + CVI | ⚠️ 建议迁移 | 供应商采购组织 |
| BUT000 | BUT000 | ✅ 新增 | BP 通用数据 |
| BUT100 | BUT100 | ✅ 新增 | BP 角色 |
| CVI_CUST_LINK | CVI_CUST_LINK | ✅ 新增 | 客户-BP 链接 |
| CVI_VEND_LINK | CVI_VEND_LINK | ✅ 新增 | 供应商-BP 链接 |

### 物料管理 (MM)

| ECC 表 | S/4HANA | 状态 | 说明 |
|--------|---------|------|------|
| MARA | MARA | ✅ 保留 | 物料主数据通用 |
| MARC | MARC | ✅ 保留 | 物料主数据工厂 |
| MARD | MARD | ✅ 保留 | 物料主数据库存地点 |
| MBEW | MBEW | ✅ 保留 | 物料评估 |
| EKKO | EKKO | ✅ 保留 | 采购订单头 |
| EKPO | EKPO | ✅ 保留 | 采购订单项 |
| MKPF | MKPF | ✅ 保留 | 物料凭证头 |
| MSEG | MSEG | ✅ 保留 | 物料凭证段 |
| KONV | PRCD_ELEMENTS | ❌ 废弃 | 条件项 → 条件元素 |

### 销售分销 (SD)

| ECC 表 | S/4HANA | 状态 | 说明 |
|--------|---------|------|------|
| VBAK | VBAK | ✅ 保留 | 销售凭证头 |
| VBAP | VBAP | ✅ 保留 | 销售凭证项 |
| VBEP | VBEP | ✅ 保留 | 销售凭证计划行 |
| VBKD | VBKD | ✅ 保留 | 销售凭证业务数据 |
| VBPA | VBPA | ✅ 保留 | 销售凭证合作伙伴 |
| LIKP | LIKP | ✅ 保留 | 交货凭证头 |
| LIPS | LIPS | ✅ 保留 | 交货凭证项 |
| VBRK | VBRK | ✅ 保留 | 开票凭证头 |
| VBRP | VBRP | ✅ 保留 | 开票凭证项 |
| KONV | PRCD_ELEMENTS | ❌ 废弃 | 条件项 |

### 人力资源 (HR/HCM)

| ECC 表 | S/4HANA | 状态 | 说明 |
|--------|---------|------|------|
| PA0000-PA9999 | 同 ECC | ✅ 保留 | 信息类型表 |
| HRP1000 | HRP1000 | ✅ 保留 | OM 对象信息 |
| HRP1001 | HRP1001 | ✅ 保留 | OM 关系信息 |
| PCL1 | PCL1 | ⚠️ 透明表 | HR 聚簇 1 |
| PCL2 | HRPY_* 系列 | ⚠️ 拆分 | HR 聚簇 2 拆分为透明表 |
| HRPY_RGDIR | HRPY_RGDIR | ✅ 新增 | 薪酬结果目录 |
| HRPY_RT | HRPY_RT | ✅ 新增 | 薪酬结果 |
| HRPY_WPBP | HRPY_WPBP | ✅ 新增 | 工作地/基本工资 |

### 生产计划 (PP)

| ECC 表 | S/4HANA | 状态 | 说明 |
|--------|---------|------|------|
| MAST | MAST | ✅ 保留 | 物料 BOM |
| STKO | STKO | ✅ 保留 | BOM 头 |
| STPO | STPO | ✅ 保留 | BOM 项目 |
| MAPL | MAPL | ✅ 保留 | 物料任务清单 |
| PLKO | PLKO | ✅ 保留 | 任务清单头 |
| PLPO | PLPO | ✅ 保留 | 任务清单工序 |
| AUFK | AUFK | ✅ 保留 | 订单主记录 |
| AFKO | AFKO | ✅ 保留 | 订单头数据 |
| AFPO | AFPO | ✅ 保留 | 订单项 |
| RESB | RESB | ✅ 保留 | 预留/相关需求 |
| PPH_ORDER | PPH_ORDER | ✅ 新增 | PP/DS 计划订单 |
| PPH_DEMAND | PPH_DEMAND | ✅ 新增 | PP/DS 需求 |

## 废弃表完整清单

### 财务相关

```
GLT0, GLT1, GLT2, GLT3, GLT4
FAGLFLEXT (使用 ACDOCA)
KNC1, KNC2, LFC1, LFC2 (使用 ACDOCA)
BSID, BSAD, BSIK, BSAK (使用 FQ Views + CDS)
COSP, COSS, COEJ (使用 ACDOCA)
```

### 条件相关

```
KONV (使用 PRCD_ELEMENTS)
```

### HR 相关

```
PCL2 (使用 HRPY_* 透明表系列)
```

## 状态图例

| 符号 | 状态 | 迁移操作 |
|------|------|---------|
| ✅ | 保留 | 无需操作 |
| ⚠️ | 变化 | 需要评估和可能的代码修改 |
| ❌ | 废弃 | 必须迁移到新表/视图 |
| ✅ 新增 | 新表 | 可选使用，提供新功能 |

## 迁移工具

| 迁移场景 | 工具/事务 |
|---------|----------|
| 财务迁移 | RFINS_MIG_START |
| BP 迁移 | CVI_EI_INBOUND_MAIN |
| 条件迁移 | COND_A_MIGRATION |
| HR 聚簇转换 | RP_UPG_CONVERT_PCL2 |

## 相关文档

- [完整对比分析](./ecc-vs-s4hana-comparison.md)
- [表变化详情](./table-changes.md)
- [ECC FI/CO 设计](../ecc/fi-co/README.md)
- [S/4HANA FI/CO 设计](../s4hana/fi-co/README.md)
