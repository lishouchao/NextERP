# ECC 到 S/4HANA 数据库迁移变化

## 概述

本文档记录 SAP ERP ECC 6.0 到 S/4HANA 的数据库结构变化，包括被废弃的表、新增的表和结构变化。

## 通用变化

### 聚簇表转换为透明表

ECC 中的聚簇表在 S/4HANA 中转换为透明表以充分利用 HANA 的性能优势。

| ECC 聚簇表 | S/4HANA 处理方式 |
|-----------|-----------------|
| BSEG | 转换为透明表 |
| BKORM | 转换为透明表 |
| KONV | 拆分为 PRCD_ELEMENTS |
| PCL1 | 转换为透明表 |
| PCL2 | 转换为透明表 |

### 聚合表消除

以下聚合表在 S/4HANA 中被消除，改为使用 CDS Views：

| ECC 表 | 说明 | S/4HANA 替代方案 |
|--------|------|-----------------|
| BSID | 应收未清项 | FQ 构件 + CDS View |
| BSAD | 应收已清项 | FQ 构件 + CDS View |
| BSIK | 应付未清项 | FQ 构件 + CDS View |
| BSAK | 应付已清项 | FQ 构件 + CDS View |
| GLT0 | 总账余额 | FAGLFLEXT 或 CDS View |
| GLT3 | 总账余额 | CDS View |
| KNC1 | 客户余额 | CDS View |
| LFC1 | 供应商余额 | CDS View |

### 字段长度变化

| 字段 | ECC 长度 | S/4HANA 长度 | 影响范围 |
|------|---------|-------------|---------|
| 科目代码 | 10位 | 10位 | 无变化 |
| 成本控制范围 | 4位 | 4位 | 无变化 |
| 会计凭证号 | 10位 | 10位 | 无变化 |
| 总账科目 | 10位 | 10位 | 无变化 |

## 财务会计 (FI/CO) 变化

### 新总账 (New GL) 强制

S/4HANA 强制使用新总账 (New GL)，经典总账表不再使用。

| 废弃表 | 替代表 |
|--------|--------|
| GLT0 | FAGLFLEXT |
| GLT3 | FAGLFLEXT |
| GLT2 | 不适用 (使用 CDS View) |

### 统驭 ledger 变化

| ECC | S/4HANA |
|-----|---------|
| 多个平行 ledger | 强制使用 Fiori |
| 表结构分离 | Universal Journal (ACDOCA) |

### ACDOCA (Universal Journal)

S/4HANA 核心财务表，整合了 GL, CO, AA, ML：

```sql
-- ACDOCA 主要字段
RCLNT       -- 集团
RCLNT       -- 客户端
RBUKRS      -- 公司代码
RYEAR       -- 会计年度
RACCT       -- 科目
RCNTR       -- 成本中心
RPRCTR      -- 利润中心
RFUNCAREA   -- 功能范围
RFAREA      -- 功能范围
RDOCNUM     -- 凭证号
RLINENO     -- 行项目
RACDOCA     -- Universal Journal
HSL         -- 本位币金额
TSL         -- 交易货币金额
KSL         -- 成本控制货币金额
OSL         -- 对象货币金额
```

## 物料管理 (MM) 变化

### 条件表变化

| ECC 表 | S/4HANA 处理 |
|--------|-------------|
| KONV | 拆分为 PRCD_ELEMENTS |
| A004 | 保留 |
| A005 | 保留 |

### 新增表

| 表名 | 说明 |
|------|------|
| PRCD_ELEMENTS | 定价元素 |
| CKMLHD | 物料 Ledger 头 |
| CKMLPP | 物料 Ledger 期间 |

## 销售分销 (SD) 变化

### 业务伙伴 (BP) 强制

S/4HANA 强制使用业务伙伴 (BP) 概念，客户和供应商主数据整合到 BUT000。

| ECC 表 | S/4HANA 变化 |
|--------|-------------|
| KNA1 | 保留但建议迁移到 BP |
| KNB1 | 保留但建议迁移到 BP |
| LFA1 | 保留但建议迁移到 BP |
| LFB1 | 保留但建议迁移到 BP |

### BP 核心表

| 表名 | 说明 |
|------|------|
| BUT000 | 业务伙伴通用数据 |
| BUT020 | BP 地址 |
| BUT021 | BP 地址使用 |
| BUT0ID | BP 标识 |
| BUT100 | BP 角色 |
| CVI_CUST_LINK | 客户-BP 链接 |
| CVI_VEND_LINK | 供应商-BP 链接 |

## 生产计划 (PP) 变化

### PP/DS (详细计划) 变化

| ECC | S/4HANA |
|-----|---------|
| 基于库存的 MRP | 基于需求的 MRP |
| 长期计划可选 | 集成 PP/DS |

### 新增表

| 表名 | 说明 |
|------|------|
| PPH_ORDER | 计划订单头 |
| PPH_ORDER_OPER | 计划订单工序 |
| PPH_DEMAND | 需求记录 |
| PPH_SUPPLY | 供应记录 |

## 人力资源 (HR/HCM) 变化

详见:
- [ECC HR 文档](../ecc/hr/README.md)
- [S/4HANA HR 文档](../s4hana/hr/README.md)

### 关键变化总结

1. 聚簇表 PCL1/PCL2 转换为透明表
2. 新增 CDS Views 提供优化访问
3. Cloud 版本提供 OData API
4. 新增灵活用工、远程工作等信息类型

## 废弃表完整清单

以下是部分重要的废弃表清单：

### 财务模块

| 表名 | 说明 | 替代方案 |
|------|------|---------|
| GLT0 | 总账余额 | FAGLFLEXT/ACDOCA |
| GLT1 | 总账余额 | FAGLFLEXT/ACDOCA |
| GLT2 | 总账余额 | FAGLFLEXT/ACDOCA |
| GLT3 | 总账余额 | FAGLFLEXT/ACDOCA |
| GLPCT | 利润中心余额 | ACDOCA |
| GLPCT | 利润中心余额 | CDS View |
| COEJ | 成本要素期间 | COEP/ACDOCA |
| COSP | 成本总计 | COEP/ACDOCA |
| COSS | 成本总计 | COEP/ACDOCA |

### SD/MM 模块

| 表名 | 说明 | 替代方案 |
|------|------|---------|
| KONV | 条件项 | PRCD_ELEMENTS |
| KONP | 条件 | PRCD_ELEMENTS |
| STXH | STXH | STXH (保留) |

## 新增表完整清单

以下是 S/4HANA 新增的核心表：

### 通用

| 表名 | 说明 |
|------|------|
| ACDOCA | Universal Journal |
| ACDOCC | 调整凭证 |
| ACDOCA_INCL | Journal 包含表 |

### 财务

| 表名 | 说明 |
|------|------|
| FAGL_MIG_AMT | 迁移金额 |
| FAGL_MIG_CC | 迁移公司代码 |
| FAGL_MIG_LDGRP | 迁移 Ledger 组 |
| FAGL_MIG_STATUS | 迁移状态 |

### 业务伙伴

| 表名 | 说明 |
|------|------|
| BUT000 | 业务伙伴通用数据 |
| BUT020 | BP 地址 |
| BUT100 | BP 角色 |
| CVI_CUST_LINK | 客户-BP 链接 |
| CVI_VEND_LINK | 供应商-BP 链接 |

## 索引策略变化

### ECC 索引策略

ECC 依赖大量二级索引提高性能。

### S/4HANA 索引策略

S/4HANA 利用 HANA 内存计算能力，减少索引数量：

1. 减少二级索引
2. 使用列存储
3. 使用 CDS View 替代索引视图

## 代码适配

### 需要适配的 ABAP 代码

```abap
" ECC - 使用聚合表
SELECT * FROM BSID
  WHERE BUKRS = '1000'
    AND KUNNR = '0000000001'.

" S/4HANA - 使用 CDS View
SELECT * FROM I_ArrearsPayableItem
  WHERE CompanyCode = '1000'
    AND Customer = '0000000001'.
```

### 需要适配的 SQL

```sql
-- ECC - 使用 KONV
SELECT KSCHL, KBETR
FROM KONV
WHERE KNUMV = '0000001234';

-- S/4HANA - 使用 PRCD_ELEMENTS
SELECT ConditionType, ConditionRateValue
FROM PRCD_ELEMENTS
WHERE ConditionDocument = '0000001234';
```

## 参考资源

- SAP S/4HANA Simplification List: https://help.sap.com/docs/SAP_S4HANA_ON-PREMISE
- SAP Note 2233065 - S/4HANA System Conversion
- SAP Note 2198845 - S/4HANA Finance Migration
