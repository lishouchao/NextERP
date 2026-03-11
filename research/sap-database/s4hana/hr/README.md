# SAP S/4HANA - HR/HCM 模块数据库设计

## 概述

S/4HANA HR/HCM 在保留 ECC 信息类型架构的基础上，引入了 HANA 优化和新特性。S/4HANA 提供两种部署选项：
1. **SAP S/4HANA, on-premise edition** - 本地部署
2. **SAP S/4HANA Cloud** - 云端版本

## S/4HANA vs ECC 主要变化

### 架构变化

| 特性 | ECC 6.0 | S/4HANA |
|------|---------|---------|
| 数据库 | AnyDB (Oracle, SQL Server, etc.) | SAP HANA |
| 聚簇表 | PCL1, PCL2, PCL3, PCL4 | 透明表或 CDS View |
| 索引 | 大量二级索引 | 减少，依赖 HANA 性能 |
| 聚合表 | 存在 (如 BSID, BSAD) | 消除，使用 CDS View |
| Fiori 应用 | 有限 | 全面覆盖 |

### HR 聚簇表变化

S/4HANA 将传统的聚簇表 (PCL1, PCL2) 转换为透明表以提高 HANA 性能：

| ECC 聚簇表 | S/4HANA 透明表 | 说明 |
|------------|---------------|------|
| PCL2 | HRPY_RGDIR | 薪酬目录 |
| PCL2 | HRPY_RT | 薪酬结果 |
| PCL2 | HRPY_WPBP | 工作地/基本工资 |
| PCL2 | HRPY_C1 | 薪酬常量 |
| PCL2 | HRPY_LS | 亏损/收益 |

### 新增核心表

#### HRPY_RGDIR (薪酬结果目录)

```sql
CREATE TABLE HRPY_RGDIR (
    MANDT       MANDT,          -- 集团
    PERNO       PERNR_D,        -- 员工号
    SEQNR       NUMC08,         -- 序号
    INPER       CHAR06,         -- 薪酬期间
    INPTY       CHAR02,         -- 薪酬类型
    PAYTY       CHAR01,         -- 支付类型
    PAYID       CHAR02,         -- 支付标识
    BONDT       DATS,           -- 奖金日期
    RUNDT       DATS,           -- 运行日期
    RUNTM       TIMS,           -- 运行时间
    VOIDN       CHAR04,         -- 作废指示符
    VOIDR       CHAR01,         -- 作废原因
    FPPER       CHAR06,         -- 会计期间
    FPBEG       DATS,           -- 期间开始
    FPEND       DATS,           -- 期间结束
    IPEND       DATS,           -- 薪酬结束日期
    ABKRS       CHAR02,         -- 薪酬范围
    REASON      CHAR04,         -- 原因
    RELD1       CHAR01,         -- 释放状态 1
    RELD2       CHAR01,         -- 释放状态 2
    RELD3       CHAR01,         -- 释放状态 3
    CORS        CHAR01,         -- 更正运行
    DIFF        CHAR01,         -- 差异运行
    REVERSED    CHAR01,         -- 冲销
    PAYDT       DATS,           -- 支付日期
    OPTYP       CHAR02,         -- 操作类型
    ACTIV       CHAR04,         -- 活动标识
    MANUE       CHAR01,         -- 手动处理
    ACTIV_DATE  DATS,           -- 活动日期
    PRIMARY KEY (MANDT, PERNO, SEQNR)
);
```

## 核心信息类型 (与 ECC 兼容)

S/4HANA 保留了 ECC 的信息类型表结构 (PAxxxx)，确保向上兼容。

### S/4HANA 特有信息类型

| 信息类型 | 表名 | 说明 | S/4HANA 新增 |
|---------|------|------|-------------|
| IT3210 | PA3210 | 人才市场配置 | ✓ |
| IT3211 | PA3211 | 人才市场候选人 | ✓ |
| IT3212 | PA3212 | 人才市场申请 | ✓ |
| IT3220 | PA3220 | 灵活劳动力 | ✓ |
| IT3221 | PA3221 | 灵活工时合同 | ✓ |
| IT3230 | PA3230 | 数字技能 | ✓ |
| IT3231 | PA3231 | 认证徽章 | ✓ |
| IT3240 | PA3240 | 远程工作协议 | ✓ |
| IT3241 | PA3241 | 混合工作安排 | ✓ |
| IT3250 | PA3250 | 员工体验调查 | ✓ |
| IT3260 | PA3260 | DEI (多元公平包容) | ✓ |

### 员工中央服务 (ECS) 新表

| 表名 | 说明 |
|------|------|
| ECSC_COMPANY | 公司信息 |
| ECSC_EMPLOYEE | 员工信息 |
| ECSC_ORGUNIT | 组织单位 |
| ECSC_POSITION | 职位 |
| ECSC_JOB | 职务 |
| ECSC_WORKLOCATION | 工作地点 |
| ECSC_PAYRANGE | 薪酬范围 |
| ECSC_PAYGRADE | 薪酬等级 |
| ECSC_PAYCOMP | 薪酬组件 |

## CDS Views (核心数据服务)

S/4HANA 使用 CDS Views 提供业务对象视图层。

### 主要 HR CDS Views

| CDS View | 说明 |
|----------|------|
| I_Employee | 员工基本信息 |
| I_EmployeeBasicData | 员工基础数据 |
| I_EmployeeOrganizationalAssgmt | 员工组织分配 |
| I_EmployeePersonalData | 员工个人数据 |
| I_EmployeeTimeSheetData | 员工时间表 |
| I_OrganizationUnit | 组织单位 |
| I_Position | 职位 |
| I_Job | 职务 |
| I_WorkAgreement | 工作协议 |
| I_WorkSchedule | 工作计划 |
| I_TimeSheetEntry | 时间表条目 |
| I_AbsenceRecord | 缺勤记录 |
| I_AttendanceRecord | 出勤记录 |
| I_PayrollResult | 薪酬结果 |
| I_Payslip | 工资条 |

### I_Employee CDS View 示例

```sql
@AbapCatalog.sqlViewName: 'I_EMPLOYEE'
@AccessControl.authorizationCheck: #CHECK
@EndUserText.label: 'Employee'
@VDM.viewType: #CONSUMPTION

define view I_Employee
    as select from PA0001 as OrgAssignment
    inner join   PA0002 as PersonalData on
        OrgAssignment.PERNR = PersonalData.PERNR and
        OrgAssignment.BEGDA <= $session.system_date and
        OrgAssignment.ENDDA >= $session.system_date and
        PersonalData.BEGDA <= $session.system_date and
        PersonalData.ENDDA >= $session.system_date
{
    @EndUserText.label: 'Personnel Number'
    key OrgAssignment.PERNR as PersonnelNumber,

    @EndUserText.label: 'Employee Name'
    concat_with_space(
        PersonalData.VORNA,
        PersonalData.NACHN, 1
    ) as EmployeeFullName,

    @EndUserText.label: 'First Name'
    PersonalData.VORNA as FirstName,

    @EndUserText.label: 'Last Name'
    PersonalData.NACHN as LastName,

    @EndUserText.label: 'Gender'
    case PersonalData.GESCH
        when '1' then 'Male'
        when '2' then 'Female'
        else 'Unknown'
    end as Gender,

    @EndUserText.label: 'Birth Date'
    PersonalData.GBDAT as BirthDate,

    @EndUserText.label: 'Organization Unit'
    OrgAssignment.ORGEH as OrganizationUnit,

    @EndUserText.label: 'Position'
    OrgAssignment.PLANS as Position,

    @EndUserText.label: 'Job'
    OrgAssignment.STELL as Job,

    @EndUserText.label: 'Company Code'
    OrgAssignment.BUKRS as CompanyCode,

    @EndUserText.label: 'Personnel Area'
    OrgAssignment.WERKS as PersonnelArea,

    @EndUserText.label: 'Employee Group'
    OrgAssignment.PERSG as EmployeeGroup,

    @EndUserText.label: 'Employee Subgroup'
    OrgAssignment.PERSK as EmployeeSubgroup,

    @EndUserText.label: 'Valid From'
    OrgAssignment.BEGDA as ValidFrom,

    @EndUserText.label: 'Valid To'
    OrgAssignment.ENDDA as ValidTo
}
where
    OrgAssignment.BEGDA <= $session.system_date and
    OrgAssignment.ENDDA >= $session.system_date
```

## S/4HANA Cloud 差异

### Cloud 特有表

| 表名 | 说明 |
|------|------|
| SAGRHCM_EC_INTEGRATION | EC 集成配置 |
| SAGRHCM_EC_MSGLOG | EC 消息日志 |
| SAGRHCM_EC_MAP | EC 字段映射 |

### Cloud API (OData)

S/4HANA Cloud 通过 OData API 暴露 HR 数据：

| API 名称 | 说明 |
|---------|------|
| API_Employee | 员工主数据 API |
| API_OrgUnit | 组织单位 API |
| API_Position | 职位 API |
| API_Job | 职务 API |
| API_WorkSchedule | 工作计划 API |
| API_TimeSheet | 时间表 API |
| API_Payroll | 薪酬 API |

### API_Employee OData 示例

```
GET /sap/opu/odata/sap/API_EMPLOYEE/
    Employee(personnelNumber='00000001')
    ?$expand=to_OrgAssignment,to_PersonalData,to_BasicPay
```

## 中国本地化信息类型

S/4HANA 支持中国本地化的信息类型：

| 信息类型 | 表名 | 说明 |
|---------|------|------|
| IT0591 | PA0591 | 社会保险 - 中国 |
| IT0592 | PA0592 | 住房公积金 - 中国 |
| IT0593 | PA0593 | 商业保险 - 中国 |
| IT0594 | PA0594 | 工伤保险 - 中国 |
| IT0595 | PA0595 | 生育保险 - 中国 |
| IT0596 | PA0596 | 失业保险 - 中国 |
| IT0597 | PA0597 | 补充公积金 - 中国 |
| IT0598 | PA0598 | 年金 - 中国 |
| IT0599 | PA0599 | 补充医疗保险 - 中国 |

### 中国个人所得税 (IT0224)

```sql
-- PA0224 中国个人所得税字段
MANDT      MANDT           -- 集团
PERNR      PERNR_D         -- 员工号
SUBTY      SUBTY           -- 子类型
OBJPS      OBJPS           -- 对象标识
SPRPS      SPRPS           -- 锁定标识
ENDDA      ENDDA           -- 结束日期
BEGDA      BEGDA           -- 开始日期
SEQNR      SEQNR           -- 记录号
AEDTM      AEDAT_D         -- 更改日期
UNAME      UNAME           -- 用户名
TAXID      TAXID_CN        -- 税务标识号
TAXAR      TAXAR_CN        -- 税务区域
TAXCL      TAXCL_CN        -- 税务分类
TAXEX      TAXEX_CN        -- 免税额
TAXYR      TAXYR_CN        -- 税务年度
DEDUT      DEDUT_CN        -- 扣除项
DED01      DED01_CN        -- 专项扣除 1 (子女教育)
DED02      DED02_CN        -- 专项扣除 2 (继续教育)
DED03      DED03_CN        -- 专项扣除 3 (大病医疗)
DED04      DED04_CN        -- 专项扣除 4 (住房贷款利息)
DED05      DED05_CN        -- 专项扣除 5 (住房租金)
DED06      DED06_CN        -- 专项扣除 6 (赡养老人)
DED07      DED07_CN        -- 专项扣除 7 (3岁以下婴幼儿照护)
```

## 性能优化表

S/4HANA 提供了优化访问的专用表：

| 表名 | 说明 |
|------|------|
| HRPY_WPBP_INDEX | WPBP 索引表 |
| HRPY_DIR_INDEX | 薪酬目录索引 |
| HRPY_RESULT_INDEX | 薪酬结果索引 |

## SAP Fiori HR 应用

S/4HANA 通过 Fiori 应用提供 HR 功能：

| 应用名称 | 事务码 (ECC) | Fiori ID |
|---------|-------------|----------|
| 管理员工主数据 | PA20/PA30 | F1637 |
| 管理组织结构 | PPOME | F2168 |
| 执行人事措施 | PA40 | F1638 |
| 时间表录入 | CAT2 | F2680 |
| 审批请假 | PTARQ | F0403 |
| 薪酬概览 | PC_PAYRESULT | F2292 |
| 管理工资项 | SM30 (V_T510) | F2256 |

## 迁移考虑

### ECC 到 S/4HANA HR 迁移

1. **信息类型兼容性** - 大部分信息类型保持兼容
2. **聚簇表转换** - PCL2 数据需要转换到透明表
3. **CDS Views** - 需要重写报表使用 CDS Views
4. **Fiori 适配** - 自定义事务需要转换为 Fiori 应用
5. **API 替换** - BAPI 替换为 OData API

### 迁移工具

- **SAP Readiness Check** - 评估迁移就绪状态
- **Simplification List** - 简化清单
- **Custom Code Migration** - 自定义代码迁移

## 参考资源

- SAP S/4HANA Help: https://help.sap.com/s4hana
- SAP S/4HANA Cloud API Hub: https://api.sap.com/package/s4hanacloudallmodules
- HCM Simplification List: https://help.sap.com/docs/SAP_S4HANA_ON-PREMISE
