# SAP 通用数据库概念

## 集团 (Client) 概念

SAP 系统使用 Client（集团）作为数据隔离的基本单位。大多数业务表都包含 `MANDT` 字段作为第一个主键字段。

### Client 相关字段

| 字段名 | 数据元素 | 说明 |
|--------|---------|------|
| MANDT | MANDT | 集团/租户 ID，3位数字 |

### Client 依赖表 vs Client 独立表

```sql
-- Client 依赖表 (大多数业务表)
SELECT * FROM MARA WHERE MANDT = '800';

-- Client 独立表 (跨集团共享)
-- 如: T000 (集团定义), T002 (语言代码)
SELECT * FROM T000;
```

## 主键命名规范

SAP 表的主键通常命名为 `~K` 或使用业务字段：

```sql
-- 示例：物料主数据
MARA ~ MARA  -- 物料号
-- 实际主键: MANDT, MATNR

-- 示例：供应商主数据
LFA1 ~ LFA1  -- 供应商号
-- 实际主键: MANDT, LIFNR
```

## 号码范围 (Number Ranges)

SAP 使用号码范围对象管理业务单据的编号。

### 相关表

| 表名 | 说明 |
|------|------|
| NRIV | 号码范围间隔 |
| TNRO | 号码范围对象定义 |
| NRIVTOTYPE | 号码范围类型 |

### 号码范围对象示例

```
RF_BELEG  - 会计凭证
M_BESTELL - 采购订单
AUFTRAG   - 生产订单
MATNR     - 物料号
PARTNER   - 业务伙伴
```

## SAP 数据类型映射

| SAP 类型 | ABAP 类型 | SQL 类型 | 说明 |
|---------|----------|---------|------|
| CHAR | C | VARCHAR | 字符型 |
| NUMC | N | VARCHAR | 数字字符型 |
| DATS | D | DATE | 日期 |
| TIMS | T | TIME | 时间 |
| DEC | P | DECIMAL | 十进制数 |
| FLTP | F | DOUBLE | 浮点数 |
| INT1 | - | TINYINT | 1字节整数 |
| INT2 | - | SMALLINT | 2字节整数 |
| INT4 | I | INTEGER | 4字节整数 |
| CURR | P | DECIMAL | 货币金额 |
| QUAN | P | DECIMAL | 数量 |
| UNIT | C | VARCHAR | 单位 |
| CUKY | C | VARCHAR | 货币代码 |

## 有效期管理 (Time Dependency)

SAP 使用 `BEGDA` (开始日期) 和 `ENDDA` (结束日期) 管理记录的时间有效性。

### 时间切片模式

```
|---- 记录1 ----|---- 记录2 ----|---- 记录3 ----|
01.01.2024      01.04.2024      01.07.2024      31.12.9999
```

### 标准查询模式

```sql
SELECT *
FROM PA0001
WHERE PERNR = '00000001'
  AND BEGDA <= '20241215'
  AND ENDDA >= '20241215';
```

## 语言处理

SAP 支持多语言，文本表通常包含 `SPRAS` 字段。

### 语言相关表

| 表名 | 说明 |
|------|------|
| T002 | 语言代码定义 |
| T002T | 语言名称 (多语言) |

## 货币处理

SAP 使用 `WAERS` 字段存储货币代码，金额字段需要关联货币代码。

### 货币相关表

| 表名 | 说明 |
|------|------|
| TCURC | 货币代码 |
| TCURR | 汇率 |
| TCURF | 转换因子 |
| TCURN | ISO 货币代码 |

## 单位处理

SAP 使用 `MEINS` 存储计量单位。

### 单位相关表

| 表名 | 说明 |
|------|------|
| T006 | 计量单位定义 |
| T006A | 分配到内部单位 |
| T006T | 单位文本 |
