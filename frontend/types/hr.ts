// 部门
export interface Department {
  id: number;
  deptName: string;
  deptCode: string;
  parentId: number | null;
  parentName?: string;
  leaderId: number | null;
  leaderName?: string;
  phone: string | null;
  email: string | null;
  sort: number;
  status: number;
  createdAt?: string;
  updatedAt?: string;
}

// 员工
export interface Employee {
  id: number;
  employeeNo: string;
  employeeName: string;
  englishName: string | null;
  gender: number;
  genderName?: string;
  birthDate: string | null;
  nation: string | null;
  idCard: string | null;
  nativePlace: string | null;
  politicalStatus: string | null;
  maritalStatus: number | null;
  maritalStatusName?: string;
  education: number | null;
  educationName?: string;
  phone: string | null;
  email: string | null;
  deptId: number | null;
  deptName?: string;
  positionId: number | null;
  positionName?: string;
  hireDate: string | null;
  workStatus: number;
  workStatusName?: string;
  status: number;
  createdAt?: string;
  updatedAt?: string;
}

// 部门树形节点
export interface DepartmentTreeNode extends Department {
  children?: DepartmentTreeNode[];
}

// 员工查询参数
export interface EmployeeQueryParams {
  current?: number;
  size?: number;
  employeeNo?: string;
  employeeName?: string;
  deptId?: number;
  workStatus?: number;
  status?: number;
}

// 部门查询参数
export interface DepartmentQueryParams {
  deptName?: string;
  status?: number;
}
