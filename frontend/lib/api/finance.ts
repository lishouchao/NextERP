import api from './client';
import type {
  FinAccount,
  FinVoucher,
  FinAccountingPeriod,
  FinAccountQuery,
  FinVoucherQuery,
  FinAccountFormData,
  FinVoucherFormData,
} from '@/types/finance';
import type { ApiResponse, PageResult } from '@/types';

const BASE_URL = '/api/v1/finance';

// ==================== 科目 API ====================
export const accountApi = {
  // 获取科目树
  getTree: () =>
    api.get<unknown, ApiResponse<FinAccount[]>>(`${BASE_URL}/accounts/tree`),

  // 获取科目列表
  getList: (params?: FinAccountQuery) =>
    api.get<unknown, ApiResponse<FinAccount[]>>(`${BASE_URL}/accounts`, { params }),

  // 获取科目详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<FinAccount>>(`${BASE_URL}/accounts/${id}`),

  // 创建科目
  create: (data: FinAccountFormData) =>
    api.post<unknown, ApiResponse<FinAccount>>(`${BASE_URL}/accounts`, data),

  // 更新科目
  update: (id: number, data: FinAccountFormData) =>
    api.put<unknown, ApiResponse<FinAccount>>(`${BASE_URL}/accounts/${id}`, data),

  // 删除科目
  delete: (id: number) =>
    api.delete<unknown, ApiResponse<void>>(`${BASE_URL}/accounts/${id}`),

  // 启用/禁用科目
  updateStatus: (id: number, status: number) =>
    api.patch<unknown, ApiResponse<FinAccount>>(`${BASE_URL}/accounts/${id}/status`, { status }),
};

// ==================== 凭证 API ====================
export const voucherApi = {
  // 获取凭证列表
  getList: (params?: FinVoucherQuery) =>
    api.get<unknown, ApiResponse<PageResult<FinVoucher>>>(`${BASE_URL}/vouchers`, { params }),

  // 获取凭证详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<FinVoucher>>(`${BASE_URL}/vouchers/${id}`),

  // 获取凭证最大号
  getMaxNo: (voucherWord: string, accountingPeriod: string) =>
    api.get<unknown, ApiResponse<number>>(`${BASE_URL}/vouchers/max-no`, {
      params: { voucherWord, accountingPeriod },
    }),

  // 创建凭证
  create: (data: FinVoucherFormData) =>
    api.post<unknown, ApiResponse<FinVoucher>>(`${BASE_URL}/vouchers`, data),

  // 更新凭证
  update: (id: number, data: FinVoucherFormData) =>
    api.put<unknown, ApiResponse<FinVoucher>>(`${BASE_URL}/vouchers/${id}`, data),

  // 删除凭证
  delete: (id: number) =>
    api.delete<unknown, ApiResponse<void>>(`${BASE_URL}/vouchers/${id}`),

  // 提交审核
  submitForApproval: (id: number) =>
    api.post<unknown, ApiResponse<FinVoucher>>(`${BASE_URL}/vouchers/${id}/submit`),

  // 审核
  approve: (id: number) =>
    api.post<unknown, ApiResponse<FinVoucher>>(`${BASE_URL}/vouchers/${id}/approve`),

  // 驳回
  reject: (id: number, reason: string) =>
    api.post<unknown, ApiResponse<FinVoucher>>(`${BASE_URL}/vouchers/${id}/reject`, { reason }),

  // 记账
  post: (id: number) =>
    api.post<unknown, ApiResponse<FinVoucher>>(`${BASE_URL}/vouchers/${id}/post`),

  // 反记账
  unpost: (id: number) =>
    api.post<unknown, ApiResponse<FinVoucher>>(`${BASE_URL}/vouchers/${id}/unpost`),

  // 批量审核
  batchApprove: (ids: number[]) =>
    api.post<unknown, ApiResponse<void>>(`${BASE_URL}/vouchers/batch-approve`, { ids }),

  // 批量记账
  batchPost: (ids: number[]) =>
    api.post<unknown, ApiResponse<void>>(`${BASE_URL}/vouchers/batch-post`, { ids }),
};

// ==================== 会计期间 API ====================
export const periodApi = {
  // 获取会计期间列表
  getList: (fiscalYear?: number) =>
    api.get<unknown, ApiResponse<FinAccountingPeriod[]>>(`${BASE_URL}/periods`, {
      params: { fiscalYear },
    }),

  // 获取当前期间
  getCurrentPeriod: () =>
    api.get<unknown, ApiResponse<FinAccountingPeriod>>(`${BASE_URL}/periods/current`),

  // 开启期间
  openPeriod: (id: number) =>
    api.post<unknown, ApiResponse<FinAccountingPeriod>>(`${BASE_URL}/periods/${id}/open`),

  // 结账
  closePeriod: (id: number) =>
    api.post<unknown, ApiResponse<FinAccountingPeriod>>(`${BASE_URL}/periods/${id}/close`),

  // 反结账
  reopenPeriod: (id: number) =>
    api.post<unknown, ApiResponse<FinAccountingPeriod>>(`${BASE_URL}/periods/${id}/reopen`),
};
