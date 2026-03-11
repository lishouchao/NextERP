# NextERP HR 模块 vs SAP S/4 HANA HCM 对比分析

> 分析日期: 2026-03-11
> 文档版本: v1.0

## 一、SAP S/4 HANA HCM 核心模块概览

| 模块 | SAP事务码 | 核心功能 |
|------|-----------|----------|
| **人事行政 (PA)** | PA20/PA30/PA40 | 员工主数据、人事措施、信息类型管理 |
| **组织管理 (OM)** | PPOME/PPOCE/PO10 | 组织架构、职位、职务、工作中心 |
| **时间管理 (PT)** | PT60/PT50/CATS | 考勤、缺勤、工时表、排班计划 |
| **薪酬管理 (PY)** | PC00/PC_PAYRESULT | 薪资计算、税务、社保、公积金 |
| **人才管理 (TM)** | PB10/PB20 | 招聘、绩效、培训、继任计划 |
| **人员发展 (PD)** | PP51/PP52 | 资质、职业规划、发展计划 |

---

## 二、NextERP HR 模块现状

### 已实现功能

| 功能 | 事务码 | 页面路径 | 状态 |
|------|--------|----------|------|
| 员工基础信息管理 | PA20/PA30 | `/hrm/employees` | ✅ 已实现 |
| 部门组织架构展示 | PPOME | `/hrm/departments` | ✅ 已实现 |

### 待开发功能

| 功能 | 事务码 | 页面路径 | 状态 |
|------|--------|----------|------|
| 时间管理 | PT60/PT50 | `/hrm/attendance` | ❌ 待开发 |
| 薪酬管理 | PC00 | `/hrm/payroll` | ❌ 待开发 |

---

## 三、设计不足详细分析

### 3.1 组织管理 (OM) 架构不完整

SAP 组织管理采用多实体关联架构，而 NextERP 目前仅有简化的部门层级结构。

#### 缺失的关键实体

| 缺失项 | SAP实现 | NextERP现状 | 影响范围 |
|--------|---------|-------------|----------|
| **职位 (Position)** | 独立实体，可多人持有同一职位 | 未实现 | 无法管理岗位编制、一人多岗 |
| **职务 (Job)** | 职位的基础描述，可复用 | 未实现 | 无法标准化岗位体系 |
| **工作中心 (Work Center)** | 工作地点/成本中心关联 | 未实现 | 无法关联生产/考勤 |
| **组织单位类型** | 公司/部门/小组/项目组 | 仅层级区分 | 无法区分组织性质 |
| **编制管理** | Headcount预算、冻结、释放 | 简单静态字段 | 无法动态管理编制 |
| **汇报关系** | 虚线/实线多维度汇报 | 仅单一上级 | 无法处理矩阵式管理 |

#### 建议数据模型

```
组织单位 (Organization Unit)
├── 职位 (Position) [1:N]
│   ├── 持有者 (员工/Employee) [1:N]
│   └── 成本中心 (Cost Center) [N:1]
├── 职务 (Job) [N:1] - 可复用的岗位描述
└── 工作中心 (Work Center) [N:1]
```

---

### 3.2 人事行政 (PA) 信息类型缺失

SAP 使用 **信息类型 (Infotype)** 概念，按有效期管理员工数据，这是 HCM 的核心设计模式。

#### 信息类型对比

| 信息类型 | 描述 | NextERP现状 | 重要程度 |
|----------|------|-------------|----------|
| IT0000 | 措施 (雇佣/离职/调岗) | ❌ 缺失 | 🔴 高 |
| IT0001 | 组织分配 (部门/职位) | ⚠️ 单一字段 | 🔴 高 |
| IT0002 | 个人数据 (姓名/性别) | ✅ 已实现 | - |
| IT0006 | 地址信息 | ❌ 缺失 | 🟡 中 |
| IT0007 | 工时计划 | ❌ 缺失 | 🔴 高 |
| IT0008 | 基本工资 | ⚠️ 单一字段 | 🔴 高 |
| IT0014 | 经常性支付/扣减 | ❌ 缺失 | 🔴 高 |
| IT0015 | 附加支付 | ❌ 缺失 | 🟡 中 |
| IT0019 | 任务监控 (试用期/合同到期) | ❌ 缺失 | 🟡 中 |
| IT0021 | 家庭成员 | ❌ 缺失 | 🟢 低 |
| IT0022 | 教育/资质 | ❌ 缺失 | 🟡 中 |
| IT0105 | 通信方式 (邮箱/电话) | ⚠️ 部分实现 | 🟡 中 |

#### 信息类型核心特性

```
信息类型记录结构:
{
  pernr: string,        // 员工号
  infty: string,        // 信息类型编号
  begda: Date,          // 开始日期 (有效期起)
  endda: Date,          // 结束日期 (有效期止)
  data: { ... }         // 具体数据字段
}
```

**关键设计原则:**
- 时间有效性: 同一员工同一信息类型可有多条记录，按日期区间有效
- 历史追溯: 保留完整变更历史
- 数据隔离: 不同信息类型独立管理，便于权限控制

---

### 3.3 人事流程 (Personnel Actions) 缺失

SAP 的人事措施是预定义的业务流程，一个措施会自动更新多个信息类型。

#### 标准人事措施类型

| 措施类型 | SAP事务码 | 触发的信息类型更新 | NextERP现状 |
|----------|-----------|-------------------|-------------|
| 雇佣 (Hiring) | PA40 | IT0000→IT0001→IT0002→IT0006→IT0007→IT0008 | ❌ 缺失 |
| 离职 (Termination) | PA40 | IT0000→IT0001(结束日期) | ❌ 缺失 |
| 调岗 (Transfer) | PA40 | IT0001(新组织分配) | ❌ 缺失 |
| 晋升 (Promotion) | PA40 | IT0001(新职位)→IT0008(新薪资) | ❌ 缺失 |
| 合同续签 (Contract Renewal) | PA40 | IT0016(合同信息) | ❌ 缺失 |
| 试用期转正 (Probation) | PA40 | IT0000(措施)→IT0019(任务完成) | ❌ 缺失 |

#### 建议实现方式

```typescript
interface PersonnelAction {
  actionType: 'HIRE' | 'TERMINATE' | 'TRANSFER' | 'PROMOTE' | 'RENEW';
  pernr: string;
  effectiveDate: Date;
  status: 'DRAFT' | 'PENDING' | 'APPROVED' | 'COMPLETED';
  infotypeChanges: InfotypeChange[];
  workflowInstanceId?: string;
}

interface InfotypeChange {
  infty: string;
  operation: 'CREATE' | 'UPDATE' | 'DELIMIT';
  newData: Record<string, any>;
}
```

---

### 3.4 时间管理 (PT) 完全缺失

时间管理是 HR 模块的重要组成部分，直接影响薪资计算。

#### 核心功能清单

| 功能 | SAP事务码 | 功能说明 | 业务场景 |
|------|-----------|----------|----------|
| 考勤记录 | CAT2 | 工时表录入 | 员工打卡、工时填报 |
| 请假申请 | PT_ARQ_REQUEST | 缺勤申请审批 | 年假、病假、事假 |
| 排班计划 | PT60 | 轮班/排班管理 | 倒班制企业 |
| 加班管理 | PT60 | 加班申请与补偿 | 加班费计算 |
| 时间评估 | PT60 | 自动计算工时 | 工时合规检查 |
| 考勤机集成 | PT60 | 刷卡数据导入 | 硬件对接 |

#### 建议数据模型

```typescript
interface AttendanceRecord {
  id: string;
  pernr: string;
  date: Date;
  clockIn?: DateTime;
  clockOut?: DateTime;
  workHours: number;
  overtimeHours: number;
  absenceType?: string;
  status: 'NORMAL' | 'LATE' | 'EARLY_LEAVE' | 'ABSENT';
}

interface LeaveRequest {
  id: string;
  pernr: string;
  leaveType: 'ANNUAL' | 'SICK' | 'PERSONAL' | 'MATERNITY' | 'MARRIAGE';
  startDate: Date;
  endDate: Date;
  days: number;
  reason: string;
  status: 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED';
  approvalChain: ApprovalStep[];
}

interface WorkSchedule {
  id: string;
  name: string;
  workDays: number[];        // 1-7 周一到周日
  startTime: string;
  endTime: string;
  breakMinutes: number;
}
```

---

### 3.5 薪酬管理 (PY) 完全缺失

薪酬管理是 HR 模块中最复杂的部分，涉及多方计算规则。

#### 核心功能清单

| 功能 | SAP实现 | 功能说明 |
|------|---------|----------|
| 薪资结构 | Pay Scale | 基本工资+岗位工资+绩效+津贴+补贴 |
| 社保公积金 | SI/HF | 五险一金计算规则，各地政策差异 |
| 个税计算 | Tax | 累计预扣法，专项附加扣除 |
| 薪资核算运行 | Payroll Run | 批量计算、 Retroactive Accounting |
| 薪资发放 | Bank Transfer | 银行接口、工资卡管理 |
| 薪资单 | Payslip | 员工查询、历史记录 |
| 年终奖 | Bonus | 全年一次性奖金计算 |

#### 薪资项结构

```typescript
interface PayrollItem {
  code: string;
  name: string;
  category: 'EARNING' | 'DEDUCTION' | 'TAX' | 'BENEFIT';
  calculationType: 'FIXED' | 'FORMULA' | 'TABLE';
  amount?: number;
  formula?: string;
  taxable: boolean;
  socialInsuranceBase: boolean;
}

interface PayrollResult {
  pernr: string;
  period: string;           // 2026-03
  items: {
    code: string;
    amount: number;
  }[];
  grossPay: number;         // 应发合计
  totalDeduction: number;   // 扣款合计
  netPay: number;           // 实发合计
}
```

---

### 3.6 数据模型设计不足

#### 当前 NextERP 简化模型

```typescript
// 问题: 平铺式结构，无历史记录，无有效期
interface Employee {
  empNo: string;
  name: string;
  department: string;
  position: string;
  level: string;
  salary: number;
  manager: string;
  // ...
}
```

#### SAP 标准模型 (建议改进)

```typescript
interface Employee {
  pernr: string;              // 员工号 (唯一标识)
  status: 'ACTIVE' | 'INACTIVE' | 'TERMINATED';

  // 信息类型集合 - 按有效期管理
  infotypes: {
    IT0000: PersonnelAction[];    // 人事措施
    IT0001: OrgAssignment[];      // 组织分配
    IT0002: PersonalData[];       // 个人数据
    IT0006: Address[];            // 地址
    IT0007: WorkSchedule[];       // 工时计划
    IT0008: BasicPay[];           // 基本工资
    IT0014: RecurringPayment[];   // 经常性支付
    IT0022: Education[];          // 教育经历
    IT0105: Communication[];      // 通信方式
  };

  // 关联实体
  positions: PositionAssignment[];  // 岗位分配 (支持一人多岗)
  costCenters: CostCenterAssignment[];
}

// 信息类型基类
interface InfotypeRecord {
  pernr: string;
  infty: string;
  begda: Date;       // 有效期开始
  endda: Date;       // 有效期结束 (12/31/9999 表示当前有效)
  seqnr: number;     // 序号
}

// 组织分配示例
interface OrgAssignment extends InfotypeRecord {
  bukrs: string;     // 公司代码
  werks: string;     // 人事范围
  persg: string;     // 员工组
  persk: string;     // 员工子组
  btrtl: string;     // 人事子范围
  abkrs: string;     // 薪资范围
  ansvh: string;     // 工时比例
  plans: string;     // 职位
  stell: string;     // 职务
  orgeh: string;     // 组织单位
  kostl: string;     // 成本中心
}
```

---

### 3.7 审批工作流缺失

SAP 集成工作流引擎，支持复杂的业务审批流程。

#### 需要工作流的业务场景

| 业务场景 | 审批链路 | 备注 |
|----------|----------|------|
| 入职审批 | HR → 部门负责人 → HR总监 | 需验证编制 |
| 调岗审批 | 员工 → 原部门 → 新部门 → HR | 跨部门协调 |
| 离职审批 | 员工 → 部门 → HR → IT(交接) | 交接清单 |
| 请假审批 | 员工 → 直属上级 → (HR) | 根据天数决定 |
| 薪资调整 | HR → 部门 → 财务 → CEO | 敏感操作 |
| 加班申请 | 员工 → 部门 → HR | 审核加班必要性 |

#### 建议工作流设计

```typescript
interface WorkflowInstance {
  id: string;
  businessKey: string;
  processType: 'HIRING' | 'TRANSFER' | 'LEAVE' | 'PAYROLL_ADJUST';
  status: 'RUNNING' | 'COMPLETED' | 'TERMINATED';
  currentNode: string;
  history: WorkflowHistory[];
}

interface WorkflowNode {
  id: string;
  name: string;
  assigneeType: 'ROLE' | 'USER' | 'DEPT_HEAD';
  assignee: string;
  action: 'APPROVE' | 'REJECT' | 'RETURN';
}
```

---

## 四、改进建议与优先级

### 优先级矩阵

| 优先级 | 模块 | 具体改进 | 预计工作量 |
|--------|------|----------|------------|
| **P0** | OM | 完善职位、职务实体，支持一人多岗 | 2 周 |
| **P0** | PA | 实现信息类型概念，支持有效期管理 | 3 周 |
| **P1** | PA | 实现人事措施流程 (入职/离职/调岗) | 2 周 |
| **P1** | Workflow | 集成工作流引擎 | 2 周 |
| **P1** | PT | 开发考勤、请假、加班管理 | 4 周 |
| **P2** | PY | 开发薪资计算引擎 | 4 周 |
| **P2** | PY | 开发社保、个税计算规则 | 2 周 |
| **P2** | TM | 开发招聘管理 | 3 周 |
| **P2** | TM | 开发绩效管理 | 3 周 |
| **P3** | PD | 开发培训管理 | 2 周 |
| **P3** | PD | 开发资质证书管理 | 1 周 |

### 建议实施路线图

```
Phase 1 (1-2月): 基础架构升级
├── 重构数据模型，引入信息类型
├── 完善组织管理实体
└── 实现基础工作流

Phase 2 (2-3月): 核心流程
├── 人事措施流程化
├── 考勤请假管理
└── 员工自助服务

Phase 3 (3-4月): 薪酬模块
├── 薪资结构配置
├── 计算规则引擎
└── 社保个税规则

Phase 4 (4-5月): 人才管理
├── 招聘管理
├── 绩效管理
└── 培训管理
```

---

## 五、技术架构建议

### 后端架构

```
backend-modulith/
└── src/main/java/com/nexterp/business/hrm/
    ├── domain/
    │   ├── model/
    │   │   ├── employee/
    │   │   │   ├── Employee.java
    │   │   │   ├── InfotypeRecord.java
    │   │   │   ├── IT0001_OrgAssignment.java
    │   │   │   ├── IT0002_PersonalData.java
    │   │   │   └── ...
    │   │   ├── organization/
    │   │   │   ├── OrgUnit.java
    │   │   │   ├── Position.java
    │   │   │   └── Job.java
    │   │   └── attendance/
    │   │       ├── AttendanceRecord.java
    │   │       └── LeaveRequest.java
    │   └── repository/
    ├── application/
    │   └── service/
    │       ├── EmployeeService.java
    │       ├── InfotypeService.java
    │       ├── PersonnelActionService.java
    │       └── PayrollService.java
    └── interfaces/
        ├── controller/
        └── dto/
```

### 前端架构

```
frontend/app/(dashboard)/hrm/
├── employees/
│   ├── page.tsx           # 员工列表
│   ├── [id]/
│   │   └── page.tsx       # 员工详情
│   └── components/
│       ├── InfotypeTabs.tsx
│       └── ActionWizard.tsx
├── organization/
│   ├── page.tsx           # 组织架构
│   └── components/
│       ├── OrgTree.tsx
│       └── PositionCard.tsx
├── attendance/
│   ├── page.tsx           # 考勤管理
│   └── leave/
│       └── page.tsx       # 请假管理
├── payroll/
│   ├── page.tsx           # 薪资管理
│   └── calculator/
│       └── page.tsx       # 薪资计算
└── recruitment/
    └── page.tsx           # 招聘管理
```

---

## 六、附录

### SAP HCM 常用事务码速查

| 事务码 | 名称 | 模块 |
|--------|------|------|
| PA20 | 显示HR主数据 | PA |
| PA30 | 维护HR主数据 | PA |
| PA40 | 人事措施 | PA |
| PP01 | 人事管理 | OM |
| PP02 | 显示人事管理 | OM |
| PPOME | 组织管理 | OM |
| PPOCE | 创建组织单位 | OM |
| PT60 | 时间管理 | PT |
| CAT2 | 工时表 | PT |
| PC00_M00_CALC | 薪资计算 | PY |
| PC_PAYRESULT | 薪资结果 | PY |
| PB10 | 创建招聘广告 | TM |
| S_AHR_61016322 | 员工清单 | 报表 |

### 参考资源

- [SAP S/4HANA Human Capital Management 官方文档](https://help.sap.com/viewer/p/SAP_S4HANA_ON-PREMISE)
- [SAP HCM 信息类型参考](https://help.sap.com/saphelp_erp60_sp/helpdata/en/42/22c61348733e17e10000000a1553f6/content.htm)

---

*文档维护者: NextERP 开发团队*
*最后更新: 2026-03-11*
