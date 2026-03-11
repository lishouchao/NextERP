/**
 * NextERP HR 模块类型定义
 * 对标 SAP S/4HANA HCM
 */

// ==================== 基础类型 ====================

/** 性别 */
export type Gender = 'M' | 'F';

/** 员工状态 */
export type EmployeeStatus = 'ACTIVE' | 'INACTIVE' | 'TERMINATED' | 'ON_LEAVE';

/** 组织单位类型 */
export type OrgUnitType = 'COMPANY' | 'BRANCH' | 'DEPARTMENT' | 'CENTER' | 'TEAM' | 'PROJECT';

/** 记录状态 */
export type RecordStatus = 'ACTIVE' | 'INACTIVE' | 'PLANNED';

/** 职务分类 */
export type JobCategory = 'M' | 'P' | 'S' | 'O'; // 管理/专业/支持/操作

/** 措施类型 */
export type ActionType =
  | 'HIRE'      // 录用
  | 'TERM'      // 离职
  | 'TRANS'     // 调动
  | 'PROM'      // 晋升
  | 'DEMO'      // 降职
  | 'CONF'      // 转正
  | 'RENEW'     // 合同续签
  | 'SUSP'      // 停薪留职
  | 'REIN'      // 复职
  | 'RETI';     // 退休

/** 假期类型 */
export type LeaveType =
  | 'ANNUAL'       // 年假
  | 'SICK'         // 病假
  | 'PERSONAL'     // 事假
  | 'MARRIAGE'     // 婚假
  | 'MATERNITY'    // 产假
  | 'PATERNITY'    // 陪产假
  | 'BEREAVEMENT'  // 丧假
  | 'COMPENSATORY' // 调休
  | 'OTHER';       // 其他

/** 审批状态 */
export type ApprovalStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

// ==================== 组织管理 (OM) ====================

/** 组织单位 */
export interface OrgUnit {
  id: string;
  code: string;
  name: string;
  shortName?: string;
  parentId: string | null;
  type: OrgUnitType;
  level: number;
  managerId?: string;
  managerName?: string;
  costCenterId?: string;
  legalEntityId?: string;
  location?: string;
  effectiveDate: string;
  endDate: string | null;
  status: RecordStatus;
  sortOrder: number;
  headcount: number;
  maxHeadcount: number;
  createdAt: string;
  updatedAt: string;
}

/** 职务 */
export interface Job {
  id: string;
  code: string;
  name: string;
  category: JobCategory;
  grade?: string;
  description?: string;
  requirements?: string;
  responsibilities?: string;
  status: RecordStatus;
  createdAt: string;
}

/** 职位 */
export interface Position {
  id: string;
  code: string;
  name: string;
  jobId: string;
  jobName: string;
  orgUnitId: string;
  orgUnitName: string;
  holderId?: string;
  holderName?: string;
  workCenterId?: string;
  costCenterId?: string;
  effectiveDate: string;
  endDate: string | null;
  status: 'VACANT' | 'FILLED' | 'FROZEN' | 'ABOLISHED';
  headcount: number;
  currentCount: number;
  description?: string;
  createdAt: string;
}

// ==================== 人事行政 (PA) ====================

/** 信息类型基类 */
export interface InfotypeRecord {
  pernr: string;           // 员工号
  infty: string;           // 信息类型编号
  begda: string;           // 开始日期
  endda: string;           // 结束日期
  seqnr: number;           // 序号
  status: 'CURRENT' | 'HISTORY' | 'FUTURE';
}

/** IT0001 组织分配 */
export interface IT0001 extends InfotypeRecord {
  infty: '0001';
  orgUnitId: string;
  orgUnitName: string;
  positionId: string;
  positionName: string;
  jobId: string;
  jobName: string;
  costCenterId?: string;
  managerId?: string;
  managerName?: string;
  employeeGroup: string;
  employeeSubGroup: string;
}

/** IT0002 个人数据 */
export interface IT0002 extends InfotypeRecord {
  infty: '0002';
  lastName: string;
  firstName: string;
  fullName: string;
  gender: Gender;
  birthDate: string;
  nationality?: string;
  ethnicity?: string;
  maritalStatus?: string;
  politicalStatus?: string;
  idType: string;
  idNumber: string;
}

/** 员工主数据 */
export interface Employee {
  pernr: string;
  status: EmployeeStatus;
  it0001: IT0001;
  it0002: IT0002;
  email?: string;
  phone?: string;
  hireDate: string;
  seniority: number;
}

// ==================== 时间管理 (PT) ====================

/** 假期余额 */
export interface LeaveBalance {
  pernr: string;
  leaveType: LeaveType;
  year: number;
  openingBalance: number;
  accrued: number;
  used: number;
  balance: number;
  expireDate?: string;
}

/** 请假申请 */
export interface LeaveRequest {
  id: string;
  pernr: string;
  leaveType: LeaveType;
  leaveTypeName: string;
  startDate: string;
  endDate: string;
  days: number;
  reason: string;
  status: ApprovalStatus;
  createdAt: string;
}

// ==================== 薪酬管理 (PY) ====================

/** 薪资结果 */
export interface PayrollResult {
  pernr: string;
  period: string;
  items: { code: string; name: string; amount: number }[];
  grossPay: number;
  totalDeduction: number;
  netPay: number;
  status: 'DRAFT' | 'CONFIRMED' | 'PAID';
}
