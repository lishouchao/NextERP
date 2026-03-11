/**
 * HR 模块模拟数据
 */

import type { OrgUnit, Job, Position, Employee, LeaveRequest, LeaveBalance } from '@/types/hr';

// ==================== 组织数据 ====================

export const mockOrgUnits: OrgUnit[] = [
  { id: '1', code: 'OU001', name: '总公司', shortName: '总部', parentId: null, type: 'COMPANY', level: 1, managerId: 'EMP001', managerName: '张伟', effectiveDate: '2018-01-01', endDate: null, status: 'ACTIVE', sortOrder: 1, headcount: 45, maxHeadcount: 50, createdAt: '2018-01-01', updatedAt: '2024-01-01', costCenterId: 'CC001' },
  { id: '2', code: 'OU002', name: '技术部', shortName: '技术', parentId: '1', type: 'DEPARTMENT', level: 2, managerId: 'EMP001', managerName: '张伟', effectiveDate: '2018-01-01', endDate: null, status: 'ACTIVE', sortOrder: 1, headcount: 15, maxHeadcount: 20, createdAt: '2018-01-01', updatedAt: '2024-01-01' },
  { id: '3', code: 'OU003', name: '人力资源部', shortName: 'HR', parentId: '1', type: 'DEPARTMENT', level: 2, managerId: 'EMP002', managerName: '李娜', effectiveDate: '2018-01-01', endDate: null, status: 'ACTIVE', sortOrder: 2, headcount: 5, maxHeadcount: 8, createdAt: '2018-01-01', updatedAt: '2024-01-01' },
  { id: '4', code: 'OU004', name: '财务部', shortName: '财务', parentId: '1', type: 'DEPARTMENT', level: 2, managerId: 'EMP004', managerName: '赵敏', effectiveDate: '2018-01-01', endDate: null, status: 'ACTIVE', sortOrder: 3, headcount: 8, maxHeadcount: 10, createdAt: '2018-01-01', updatedAt: '2024-01-01' },
  { id: '5', code: 'OU005', name: '销售部', shortName: '销售', parentId: '1', type: 'DEPARTMENT', level: 2, managerId: 'EMP005', managerName: '刘强', effectiveDate: '2018-01-01', endDate: null, status: 'ACTIVE', sortOrder: 4, headcount: 12, maxHeadcount: 15, createdAt: '2018-01-01', updatedAt: '2024-01-01' },
  { id: '6', code: 'OU006', name: '前端开发组', shortName: '前端', parentId: '2', type: 'TEAM', level: 3, managerId: 'EMP003', managerName: '王磊', effectiveDate: '2019-06-01', endDate: null, status: 'ACTIVE', sortOrder: 1, headcount: 8, maxHeadcount: 10, createdAt: '2019-06-01', updatedAt: '2024-01-01' },
  { id: '7', code: 'OU007', name: '后端开发组', shortName: '后端', parentId: '2', type: 'TEAM', level: 3, managerId: 'EMP007', managerName: '周杰', effectiveDate: '2019-06-01', endDate: null, status: 'ACTIVE', sortOrder: 2, headcount: 7, maxHeadcount: 10, createdAt: '2019-06-01', updatedAt: '2024-01-01' },
  { id: '8', code: 'OU008', name: '测试组', shortName: '测试', parentId: '2', type: 'TEAM', level: 3, managerId: 'EMP011', managerName: '李明', effectiveDate: '2020-01-15', endDate: null, status: 'ACTIVE', sortOrder: 3, headcount: 5, maxHeadcount: 8, createdAt: '2020-01-15', updatedAt: '2024-01-01' },
  { id: '9', code: 'OU009', name: '华东销售组', shortName: '华东', parentId: '5', type: 'TEAM', level: 3, managerId: 'EMP006', managerName: '陈芳', effectiveDate: '2021-03-08', endDate: null, status: 'ACTIVE', sortOrder: 1, headcount: 6, maxHeadcount: 8, createdAt: '2021-03-08', updatedAt: '2024-01-01' },
  { id: '10', code: 'OU010', name: '华南销售组', shortName: '华南', parentId: '5', type: 'TEAM', level: 3, managerId: 'EMP012', managerName: '黄强', effectiveDate: '2021-03-08', endDate: null, status: 'ACTIVE', sortOrder: 2, headcount: 6, maxHeadcount: 8, createdAt: '2021-03-08', updatedAt: '2024-01-01' },
  { id: '11', code: 'OU011', name: '行政部', shortName: '行政', parentId: '1', type: 'DEPARTMENT', level: 2, managerId: 'EMP010', managerName: '孙丽', effectiveDate: '2018-01-01', endDate: null, status: 'ACTIVE', sortOrder: 5, headcount: 4, maxHeadcount: 6, createdAt: '2018-01-01', updatedAt: '2024-01-01' },
];

// ==================== 职务数据 ====================

export const mockJobs: Job[] = [
  { id: '1', code: 'JOB001', name: '技术总监', category: 'M', grade: 'M4', description: '负责技术团队管理和架构设计', requirements: '10年以上开发经验', status: 'ACTIVE', createdAt: '2018-01-01' },
  { id: '2', code: 'JOB002', name: '技术经理', category: 'M', grade: 'M3', description: '负责技术团队日常管理', requirements: '5年以上开发经验', status: 'ACTIVE', createdAt: '2018-01-01' },
  { id: '3', code: 'JOB003', name: '高级工程师', category: 'P', grade: 'P6', description: '负责核心模块开发', requirements: '3年以上开发经验', status: 'ACTIVE', createdAt: '2018-01-01' },
  { id: '4', code: 'JOB004', name: '工程师', category: 'P', grade: 'P5', description: '负责功能模块开发', requirements: '1年以上开发经验', status: 'ACTIVE', createdAt: '2018-01-01' },
  { id: '5', code: 'JOB005', name: 'HR经理', category: 'M', grade: 'M3', description: '负责人力资源管理工作', requirements: '5年以上HR经验', status: 'ACTIVE', createdAt: '2018-01-01' },
  { id: '6', code: 'JOB006', name: 'HR专员', category: 'S', grade: 'S2', description: '负责招聘/薪酬等模块', requirements: '1年以上HR经验', status: 'ACTIVE', createdAt: '2018-01-01' },
  { id: '7', code: 'JOB007', name: '财务主管', category: 'M', grade: 'M2', description: '负责财务核算管理', requirements: '3年以上财务经验', status: 'ACTIVE', createdAt: '2018-01-01' },
  { id: '8', code: 'JOB008', name: '销售总监', category: 'M', grade: 'M4', description: '负责销售团队管理', requirements: '8年以上销售经验', status: 'ACTIVE', createdAt: '2018-01-01' },
  { id: '9', code: 'JOB009', name: '销售经理', category: 'M', grade: 'M2', description: '负责销售业务拓展', requirements: '3年以上销售经验', status: 'ACTIVE', createdAt: '2018-01-01' },
  { id: '10', code: 'JOB010', name: '测试工程师', category: 'P', grade: 'P5', description: '负责软件测试工作', requirements: '2年以上测试经验', status: 'ACTIVE', createdAt: '2018-01-01' },
];

// ==================== 职位数据 ====================

export const mockPositions: Position[] = [
  { id: '1', code: 'POS001', name: '技术总监', jobId: '1', jobName: '技术总监', orgUnitId: '2', orgUnitName: '技术部', holderId: 'EMP001', holderName: '张伟', effectiveDate: '2018-06-01', endDate: null, status: 'FILLED', headcount: 1, currentCount: 1, createdAt: '2018-06-01' },
  { id: '2', code: 'POS002', name: '前端负责人', jobId: '2', jobName: '技术经理', orgUnitId: '6', orgUnitName: '前端开发组', holderId: 'EMP003', holderName: '王磊', effectiveDate: '2019-06-01', endDate: null, status: 'FILLED', headcount: 1, currentCount: 1, createdAt: '2019-06-01' },
  { id: '3', code: 'POS003', name: '后端负责人', jobId: '2', jobName: '技术经理', orgUnitId: '7', orgUnitName: '后端开发组', holderId: 'EMP007', holderName: '周杰', effectiveDate: '2019-06-01', endDate: null, status: 'FILLED', headcount: 1, currentCount: 1, createdAt: '2019-06-01' },
  { id: '4', code: 'POS004', name: '高级前端工程师', jobId: '3', jobName: '高级工程师', orgUnitId: '6', orgUnitName: '前端开发组', effectiveDate: '2024-01-01', endDate: null, status: 'VACANT', headcount: 2, currentCount: 1, createdAt: '2024-01-01' },
  { id: '5', code: 'POS005', name: '前端工程师', jobId: '4', jobName: '工程师', orgUnitId: '6', orgUnitName: '前端开发组', effectiveDate: '2024-01-01', endDate: null, status: 'VACANT', headcount: 5, currentCount: 4, createdAt: '2024-01-01' },
  { id: '6', code: 'POS006', name: 'HR经理', jobId: '5', jobName: 'HR经理', orgUnitId: '3', orgUnitName: '人力资源部', holderId: 'EMP002', holderName: '李娜', effectiveDate: '2019-03-15', endDate: null, status: 'FILLED', headcount: 1, currentCount: 1, createdAt: '2019-03-15' },
  { id: '7', code: 'POS007', name: '销售总监', jobId: '8', jobName: '销售总监', orgUnitId: '5', orgUnitName: '销售部', holderId: 'EMP005', holderName: '刘强', effectiveDate: '2017-09-01', endDate: null, status: 'FILLED', headcount: 1, currentCount: 1, createdAt: '2017-09-01' },
  { id: '8', code: 'POS008', name: '测试负责人', jobId: '2', jobName: '技术经理', orgUnitId: '8', orgUnitName: '测试组', holderId: 'EMP011', holderName: '李明', effectiveDate: '2020-01-15', endDate: null, status: 'FILLED', headcount: 1, currentCount: 1, createdAt: '2020-01-15' },
  { id: '9', code: 'POS009', name: '财务主管', jobId: '7', jobName: '财务主管', orgUnitId: '4', orgUnitName: '财务部', holderId: 'EMP004', holderName: '赵敏', effectiveDate: '2020-07-01', endDate: null, status: 'FILLED', headcount: 1, currentCount: 1, createdAt: '2020-07-01' },
  { id: '10', code: 'POS010', name: '华东销售经理', jobId: '9', jobName: '销售经理', orgUnitId: '9', orgUnitName: '华东销售组', holderId: 'EMP006', holderName: '陈芳', effectiveDate: '2021-03-08', endDate: null, status: 'FILLED', headcount: 1, currentCount: 1, createdAt: '2021-03-08' },
];

// ==================== 员工数据 ====================

export const mockEmployees: Employee[] = [
  { pernr: 'EMP001', status: 'ACTIVE', it0001: { pernr: 'EMP001', infty: '0001', begda: '2018-06-01', endda: '9999-12-31', seqnr: 0, status: 'CURRENT', orgUnitId: '2', orgUnitName: '技术部', positionId: '1', positionName: '技术总监', jobId: '1', jobName: '技术总监', employeeGroup: '正式', employeeSubGroup: '管理层' }, it0002: { pernr: 'EMP001', infty: '0002', begda: '1985-03-15', endda: '9999-12-31', seqnr: 0, status: 'CURRENT', lastName: '张', firstName: '伟', fullName: '张伟', gender: 'M', birthDate: '1985-03-15', idType: '身份证', idNumber: '11010119850315****' }, email: 'zhangwei@company.com', phone: '13800138001', hireDate: '2018-06-01', seniority: 6 },
  { pernr: 'EMP002', status: 'ACTIVE', it0001: { pernr: 'EMP002', infty: '0001', begda: '2019-03-15', endda: '9999-12-31', seqnr: 0, status: 'CURRENT', orgUnitId: '3', orgUnitName: '人力资源部', positionId: '6', positionName: 'HR经理', jobId: '5', jobName: 'HR经理', employeeGroup: '正式', employeeSubGroup: '管理层' }, it0002: { pernr: 'EMP002', infty: '0002', begda: '1990-07-22', endda: '9999-12-31', seqnr: 0, status: 'CURRENT', lastName: '李', firstName: '娜', fullName: '李娜', gender: 'F', birthDate: '1990-07-22', idType: '身份证', idNumber: '11010119900722****' }, email: 'lina@company.com', phone: '13900139002', hireDate: '2019-03-15', seniority: 5 },
  { pernr: 'EMP003', status: 'ACTIVE', it0001: { pernr: 'EMP003', infty: '0001', begda: '2020-01-10', endda: '9999-12-31', seqnr: 0, status: 'CURRENT', orgUnitId: '6', orgUnitName: '前端开发组', positionId: '2', positionName: '前端负责人', jobId: '2', jobName: '技术经理', managerId: 'EMP001', managerName: '张伟', employeeGroup: '正式', employeeSubGroup: '技术' }, it0002: { pernr: 'EMP003', infty: '0002', begda: '1992-11-08', endda: '9999-12-31', seqnr: 0, status: 'CURRENT', lastName: '王', firstName: '磊', fullName: '王磊', gender: 'M', birthDate: '1992-11-08', idType: '身份证', idNumber: '11010119921108****' }, email: 'wanglei@company.com', phone: '13700137003', hireDate: '2020-01-10', seniority: 4 },
  { pernr: 'EMP004', status: 'ACTIVE', it0001: { pernr: 'EMP004', infty: '0001', begda: '2020-07-01', endda: '9999-12-31', seqnr: 0, status: 'CURRENT', orgUnitId: '4', orgUnitName: '财务部', positionId: '9', positionName: '财务主管', jobId: '7', jobName: '财务主管', employeeGroup: '正式', employeeSubGroup: '财务' }, it0002: { pernr: 'EMP004', infty: '0002', begda: '1995-05-30', endda: '9999-12-31', seqnr: 0, status: 'CURRENT', lastName: '赵', firstName: '敏', fullName: '赵敏', gender: 'F', birthDate: '1995-05-30', idType: '身份证', idNumber: '11010119950530****' }, email: 'zhaomin@company.com', phone: '13600136004', hireDate: '2020-07-01', seniority: 4 },
  { pernr: 'EMP005', status: 'ACTIVE', it0001: { pernr: 'EMP005', infty: '0001', begda: '2017-09-01', endda: '9999-12-31', seqnr: 0, status: 'CURRENT', orgUnitId: '5', orgUnitName: '销售部', positionId: '7', positionName: '销售总监', jobId: '8', jobName: '销售总监', employeeGroup: '正式', employeeSubGroup: '管理层' }, it0002: { pernr: 'EMP005', infty: '0002', begda: '1988-09-12', endda: '9999-12-31', seqnr: 0, status: 'CURRENT', lastName: '刘', firstName: '强', fullName: '刘强', gender: 'M', birthDate: '1988-09-12', idType: '身份证', idNumber: '11010119880912****' }, email: 'liuqiang@company.com', phone: '13500135005', hireDate: '2017-09-01', seniority: 7 },
  { pernr: 'EMP006', status: 'ACTIVE', it0001: { pernr: 'EMP006', infty: '0001', begda: '2021-03-08', endda: '9999-12-31', seqnr: 0, status: 'CURRENT', orgUnitId: '9', orgUnitName: '华东销售组', positionId: '10', positionName: '华东销售经理', jobId: '9', jobName: '销售经理', managerId: 'EMP005', managerName: '刘强', employeeGroup: '正式', employeeSubGroup: '销售' }, it0002: { pernr: 'EMP006', infty: '0002', begda: '1993-02-28', endda: '9999-12-31', seqnr: 0, status: 'CURRENT', lastName: '陈', firstName: '芳', fullName: '陈芳', gender: 'F', birthDate: '1993-02-28', idType: '身份证', idNumber: '11010119930228****' }, email: 'chenfang@company.com', phone: '13400134006', hireDate: '2021-03-08', seniority: 3 },
];

// ==================== 假期数据 ====================

export const mockLeaveBalances: LeaveBalance[] = [
  { pernr: 'EMP001', leaveType: 'ANNUAL', year: 2024, openingBalance: 5, accrued: 10, used: 6, balance: 9 },
  { pernr: 'EMP001', leaveType: 'SICK', year: 2024, openingBalance: 0, accrued: 12, used: 2, balance: 10 },
  { pernr: 'EMP003', leaveType: 'ANNUAL', year: 2024, openingBalance: 3, accrued: 10, used: 8, balance: 5 },
];

export const mockLeaveRequests: LeaveRequest[] = [
  { id: 'LR001', pernr: 'EMP003', leaveType: 'ANNUAL', leaveTypeName: '年假', startDate: '2024-03-20', endDate: '2024-03-22', days: 3, reason: '个人事务', status: 'APPROVED', createdAt: '2024-03-15' },
  { id: 'LR002', pernr: 'EMP006', leaveType: 'SICK', leaveTypeName: '病假', startDate: '2024-03-18', endDate: '2024-03-18', days: 1, reason: '身体不适', status: 'PENDING', createdAt: '2024-03-17' },
];
