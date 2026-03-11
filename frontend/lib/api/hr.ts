import api from './client';
import type { Department, Employee, EmployeeQuery, ApiResponse, PageResult } from '@/types/hr';
import type { PageRequest } from '@/types';

const BASE_URL = '/api/v1/hr';

// ==================== 部门 API ====================
export const departmentApi = {
  // 获取部门树
  getTree: () =>
    api.get<unknown, ApiResponse<Department[]>>(`${BASE_URL}/departments/tree`),

  // 获取部门列表
  getList: (params?: PageRequest) =>
    api.get<unknown, ApiResponse<PageResult<Department>>>(`${BASE_URL}/departments`, { params }),

  // 获取部门详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<Department>>(`${BASE_URL}/departments/${id}`),

  // 创建部门
  create: (data: Partial<Department>) =>
    api.post<unknown, ApiResponse<Department>>(`${BASE_URL}/departments`, data),

  // 更新部门
  update: (id: number, data: Partial<Department>) =>
    api.put<unknown, ApiResponse<Department>>(`${BASE_URL}/departments/${id}`, data),

  // 删除部门
  delete: (id: number) =>
    api.delete<unknown, ApiResponse<void>>(`${BASE_URL}/departments/${id}`),
};

// ==================== 员工 API ====================
export const employeeApi = {
  // 获取员工列表
  getList: (params?: EmployeeQuery) =>
    api.get<unknown, ApiResponse<PageResult<Employee>>>(`${BASE_URL}/employees`, { params }),

  // 获取员工详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<Employee>>(`${BASE_URL}/employees/${id}`),

  // 创建员工
  create: (data: Partial<Employee>) =>
    api.post<unknown, ApiResponse<Employee>>(`${BASE_URL}/employees`, data),

  // 更新员工
  update: (id: number, data: Partial<Employee>) =>
    api.put<unknown, ApiResponse<Employee>>(`${BASE_URL}/employees/${id}`, data),

  // 删除员工
  delete: (id: number) =>
    api.delete<unknown, ApiResponse<void>>(`${BASE_URL}/employees/${id}`),
};
