import api from './client';
import type { ApiResponse, PageResult } from '@/types';

// ==================== URL 前缀 ====================
const CUSTOMER_URL = '/api/v1/sales/customers';
const SD_URL = '/api/sd';

// ==================== 客户 API ====================
export const customerApi = {
  // 分页查询客户
  getPage: (params: { tenantId: number; status?: number; current?: number; size?: number }) =>
    api.post<unknown, ApiResponse<PageResult<any>>>(`${CUSTOMER_URL}/page`, null, { params }),

  // 获取客户详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<any>>(`${CUSTOMER_URL}/${id}`),

  // 创建客户
  create: (data: any) =>
    api.post<unknown, ApiResponse<number>>(`${CUSTOMER_URL}`, data),

  // 更新客户
  update: (id: number, data: any) =>
    api.put<unknown, ApiResponse<any>>(`${CUSTOMER_URL}/${id}`, data),

  // 删除客户
  delete: (id: number) =>
    api.delete<unknown, ApiResponse<void>>(`${CUSTOMER_URL}/${id}`),

  // 启用/禁用客户
  updateStatus: (id: number, status: number) =>
    api.put<unknown, ApiResponse<void>>(`${CUSTOMER_URL}/${id}/status`, null, { params: { status } }),

  // 查询启用状态的客户
  listActive: (tenantId: number) =>
    api.get<unknown, ApiResponse<any[]>>(`${CUSTOMER_URL}/active`, { params: { tenantId } }),

  // 根据分类查询客户
  listByCategory: (categoryId: number, tenantId: number) =>
    api.get<unknown, ApiResponse<any[]>>(`${CUSTOMER_URL}/category/${categoryId}`, { params: { tenantId } }),

  // 根据类型查询客户
  listByType: (customerType: number, tenantId: number) =>
    api.get<unknown, ApiResponse<any[]>>(`${CUSTOMER_URL}/type/${customerType}`, { params: { tenantId } }),

  // 根据销售员查询客户
  listBySalesPerson: (salesPersonId: number, tenantId: number) =>
    api.get<unknown, ApiResponse<any[]>>(`${CUSTOMER_URL}/sales-person/${salesPersonId}`, { params: { tenantId } }),

  // 搜索客户
  search: (keyword: string, tenantId: number) =>
    api.get<unknown, ApiResponse<any[]>>(`${CUSTOMER_URL}/search`, { params: { keyword, tenantId } }),
};

// ==================== 销售订单 API ====================
export const salesOrderApi = {
  // 分页查询销售订单
  getList: (params: { tenantId: number; orderStatus?: string; current?: number; size?: number }) =>
    api.get<unknown, ApiResponse<PageResult<any>>>(`${SD_URL}/orders`, { params }),

  // 获取订单详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<any>>(`${SD_URL}/orders/${id}`),

  // 创建销售订单
  create: (data: any) =>
    api.post<unknown, ApiResponse<number>>(`${SD_URL}/orders`, data),

  // 更新销售订单
  update: (id: number, data: any) =>
    api.put<unknown, ApiResponse<void>>(`${SD_URL}/orders/${id}`, data),

  // 删除销售订单
  delete: (id: number) =>
    api.delete<unknown, ApiResponse<void>>(`${SD_URL}/orders/${id}`),

  // 提交销售订单
  submit: (id: number) =>
    api.post<unknown, ApiResponse<void>>(`${SD_URL}/orders/${id}/submit`),

  // 审批销售订单
  approve: (id: number, approvedBy: string) =>
    api.post<unknown, ApiResponse<void>>(`${SD_URL}/orders/${id}/approve`, null, { params: { approvedBy } }),

  // 拒绝销售订单
  reject: (id: number, rejectedBy: string, reason: string) =>
    api.post<unknown, ApiResponse<void>>(`${SD_URL}/orders/${id}/reject`, null, { params: { rejectedBy, reason } }),

  // 信用检查
  creditCheck: (id: number) =>
    api.post<unknown, ApiResponse<any>>(`${SD_URL}/orders/${id}/credit-check`),

  // 可用性检查
  availabilityCheck: (id: number) =>
    api.get<unknown, ApiResponse<Record<string, any>>>(`${SD_URL}/orders/${id}/availability`),
};

// ==================== 交货 API ====================
export const deliveryApi = {
  // 分页查询交货单
  getList: (params: { tenantId: number; deliveryStatus?: string; current?: number; size?: number }) =>
    api.get<unknown, ApiResponse<PageResult<any>>>(`${SD_URL}/deliveries`, { params }),

  // 获取交货单详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<any>>(`${SD_URL}/deliveries/${id}`),

  // 创建交货单
  create: (data: any) =>
    api.post<unknown, ApiResponse<number>>(`${SD_URL}/deliveries`, data),

  // 更新交货单
  update: (id: number, data: any) =>
    api.put<unknown, ApiResponse<void>>(`${SD_URL}/deliveries/${id}`, data),

  // 拣货
  pick: (id: number, pickItems: any[]) =>
    api.post<unknown, ApiResponse<void>>(`${SD_URL}/deliveries/${id}/pick`, pickItems),

  // 发货过账
  postGoodsIssue: (id: number, actualGiDate: string) =>
    api.post<unknown, ApiResponse<void>>(`${SD_URL}/deliveries/${id}/post-gi`, null, { params: { actualGiDate } }),

  // 取消交货单
  cancel: (id: number) =>
    api.post<unknown, ApiResponse<void>>(`${SD_URL}/deliveries/${id}/cancel`),
};

// ==================== 开票 API ====================
export const billingApi = {
  // 分页查询开票单
  getList: (params: { tenantId: number; billingStatus?: string; current?: number; size?: number }) =>
    api.get<unknown, ApiResponse<PageResult<any>>>(`${SD_URL}/billings`, { params }),

  // 获取开票单详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<any>>(`${SD_URL}/billings/${id}`),

  // 创建开票单
  create: (data: any) =>
    api.post<unknown, ApiResponse<number>>(`${SD_URL}/billings`, data),

  // 更新开票单
  update: (id: number, data: any) =>
    api.put<unknown, ApiResponse<void>>(`${SD_URL}/billings/${id}`, data),

  // 过账
  post: (id: number) =>
    api.post<unknown, ApiResponse<void>>(`${SD_URL}/billings/${id}/post`),

  // 取消
  cancel: (id: number) =>
    api.post<unknown, ApiResponse<void>>(`${SD_URL}/billings/${id}/cancel`),

  // 开票预览
  preview: (deliveryId: number) =>
    api.post<unknown, ApiResponse<any>>(`${SD_URL}/billings/preview`, null, { params: { deliveryId } }),
};

// ==================== 信用管理 API ====================
export const creditApi = {
  // 获取客户信用主数据
  getCreditMaster: (customerId: number, companyId: number) =>
    api.get<unknown, ApiResponse<any>>(`${SD_URL}/credit/${customerId}`, { params: { companyId } }),

  // 更新客户信用主数据
  updateCreditMaster: (customerId: number, params: { companyId: number; creditLimit?: number; riskClass?: string }) =>
    api.put<unknown, ApiResponse<void>>(`${SD_URL}/credit/${customerId}`, null, { params }),

  // 执行信用检查
  performCreditCheck: (data: any) =>
    api.post<unknown, ApiResponse<any>>(`${SD_URL}/credit/check`, data),

  // 查询信用检查日志
  getCreditLogs: (params: { tenantId: number; customerId?: number; current?: number; size?: number }) =>
    api.get<unknown, ApiResponse<PageResult<any>>>(`${SD_URL}/credit/logs`, { params }),

  // 查询被冻结的订单
  getBlockedOrders: (tenantId: number) =>
    api.get<unknown, ApiResponse<any[]>>(`${SD_URL}/credit/blocked-orders`, { params: { tenantId } }),

  // 释放被冻结的订单
  releaseBlockedOrder: (orderId: number, releasedBy: string) =>
    api.post<unknown, ApiResponse<void>>(`${SD_URL}/credit/release/${orderId}`, null, { params: { releasedBy } }),
};

// ==================== 条件记录 API ====================
export const conditionApi = {
  // 分页查询条件记录
  getList: (params: { tenantId: number; conditionType?: string; current?: number; size?: number }) =>
    api.get<unknown, ApiResponse<PageResult<any>>>(`${SD_URL}/conditions`, { params }),

  // 获取条件记录详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<any>>(`${SD_URL}/conditions/${id}`),

  // 创建条件记录
  create: (data: any) =>
    api.post<unknown, ApiResponse<number>>(`${SD_URL}/conditions`, data),

  // 更新条件记录
  update: (id: number, data: any) =>
    api.put<unknown, ApiResponse<void>>(`${SD_URL}/conditions/${id}`, data),

  // 删除条件记录
  delete: (id: number) =>
    api.delete<unknown, ApiResponse<void>>(`${SD_URL}/conditions/${id}`),
};

// ==================== 定价 API ====================
export const pricingApi = {
  // 定价预览
  preview: (params: { tenantId: number; customerId: number; materialId: number; qty: number; pricingDate?: string }) =>
    api.post<unknown, ApiResponse<Record<string, any>>>(`${SD_URL}/pricing/preview`, null, { params }),

  // 查询定价过程列表
  listPricingProcedures: () =>
    api.get<unknown, ApiResponse<any[]>>(`${SD_URL}/pricing/pricing-procedures`),
};
