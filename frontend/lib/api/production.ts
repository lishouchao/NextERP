import api from './client';
import type { ApiResponse, PageResult } from '@/types';

const BASE_URL = '/api/v1/production';

// ==================== Types ====================
export interface ProBomDTO {
  id: number;
  bomCode: string;
  productCode: string;
  productName: string;
  version: string;
  status: number;
  baseQty: number;
  unit: string;
  bomType?: number;
  effectiveFrom: string;
  effectiveTo: string | null;
  components: number;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  tenantId?: number;
}

export interface BomComponentDTO {
  id: number;
  bomId: number;
  seq: number;
  materialCode: string;
  materialName: string;
  quantity: number;
  unit: string;
  scrapRate: number;
  effectiveFrom: string;
  effectiveTo: string | null;
  status: number;
}

export interface CreateBomRequest {
  bomCode: string;
  productCode: string;
  productName: string;
  version: string;
  baseQty: number;
  unit: string;
  bomType?: number;
  effectiveFrom?: string;
  effectiveTo?: string;
  tenantId: number;
}

export interface ProProductionOrderDTO {
  id: number;
  orderNo: string;
  productCode: string;
  productName: string;
  quantity: number;
  completedQty: number;
  unit: string;
  plannedStart: string;
  plannedEnd: string;
  actualStart: string | null;
  actualEnd: string | null;
  status: number;
  priority: number;
  workshop: string;
  workCenter: string;
  workshopId?: number;
  createdBy: string;
  confirmedBy: string | null;
  confirmedAt: string | null;
  tenantId?: number;
}

export interface CreateProductionOrderRequest {
  orderNo?: string;
  productCode: string;
  productName?: string;
  quantity: number;
  unit?: string;
  plannedStart: string;
  plannedEnd: string;
  priority?: number;
  workshop?: string;
  workCenter?: string;
  workshopId?: number;
  tenantId: number;
}

export interface ProRoutingDTO {
  id: number;
  routingCode: string;
  routingName: string;
  routingType: number;
  status: number;
  productCode: string;
  productName: string;
  operations: RoutingOperationDTO[];
  tenantId?: number;
}

export interface RoutingOperationDTO {
  seq: number;
  operationName: string;
  workCenter: string;
  setupTime: number;
  runTime: number;
  unit: string;
}

export interface CreateRoutingRequest {
  routingCode: string;
  routingName: string;
  routingType?: number;
  productCode: string;
  productName?: string;
  operations: {
    seq: number;
    operationName: string;
    workCenter: string;
    setupTime: number;
    runTime: number;
    unit?: string;
  }[];
  tenantId: number;
}

export interface ProOperationRecordDTO {
  id: number;
  productionOrderId: number;
  orderNo: string;
  sequenceNo: number;
  operationName: string;
  workCenter: string;
  workerId: number | null;
  workerName: string | null;
  status: number;
  completedQty: number;
  qualifiedQty: number;
  scrappedQty: number;
  actualManHours: number;
  actualMachineHours: number;
  startTime: string | null;
  endTime: string | null;
  tenantId?: number;
}

export interface CreateOperationRecordRequest {
  productionOrderId: number;
  sequenceNo: number;
  operationName: string;
  workCenter: string;
  tenantId: number;
}

export interface UpdateOperationRecordRequest {
  completedQty?: number;
  qualifiedQty?: number;
  scrappedQty?: number;
  actualManHours?: number;
  actualMachineHours?: number;
}

export interface ProductionProgressDTO {
  productionOrderId: number;
  totalOperations: number;
  completedOperations: number;
  progressPercent: number;
}

// ==================== BOM API ====================
export const bomApi = {
  // 分页查询BOM
  getPage: (params: {
    tenantId: number;
    bomType?: number;
    status?: number;
    current?: number;
    size?: number;
  }) =>
    api.post<unknown, ApiResponse<PageResult<ProBomDTO>>>(
      `${BASE_URL}/boms/page`,
      {},
      { params },
    ),

  // 获取BOM详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<ProBomDTO>>(`${BASE_URL}/boms/${id}`),

  // 创建BOM
  create: (data: CreateBomRequest) =>
    api.post<unknown, ApiResponse<number>>(`${BASE_URL}/boms`, data),

  // 更新BOM
  update: (id: number, data: CreateBomRequest) =>
    api.put<unknown, ApiResponse<void>>(`${BASE_URL}/boms/${id}`, data),

  // 删除BOM
  delete: (id: number) =>
    api.delete<unknown, ApiResponse<void>>(`${BASE_URL}/boms/${id}`),

  // 启用BOM
  activate: (id: number) =>
    api.post<unknown, ApiResponse<void>>(`${BASE_URL}/boms/${id}/activate`),

  // 停用BOM
  deactivate: (id: number) =>
    api.post<unknown, ApiResponse<void>>(`${BASE_URL}/boms/${id}/deactivate`),
};

// ==================== 生产订单 API ====================
export const productionOrderApi = {
  // 分页查询生产订单
  getPage: (params: {
    tenantId: number;
    status?: number;
    workshopId?: number;
    current?: number;
    size?: number;
  }) =>
    api.post<unknown, ApiResponse<PageResult<ProProductionOrderDTO>>>(
      `${BASE_URL}/orders/page`,
      {},
      { params },
    ),

  // 获取生产订单详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<ProProductionOrderDTO>>(
      `${BASE_URL}/orders/${id}`,
    ),

  // 创建生产订单
  create: (data: CreateProductionOrderRequest) =>
    api.post<unknown, ApiResponse<number>>(`${BASE_URL}/orders`, data),

  // 更新生产订单
  update: (id: number, data: CreateProductionOrderRequest) =>
    api.put<unknown, ApiResponse<void>>(`${BASE_URL}/orders/${id}`, data),

  // 删除生产订单
  delete: (id: number) =>
    api.delete<unknown, ApiResponse<void>>(`${BASE_URL}/orders/${id}`),

  // 审核生产订单
  approve: (id: number, approvedBy: string) =>
    api.post<unknown, ApiResponse<void>>(
      `${BASE_URL}/orders/${id}/approve`,
      {},
      { params: { approvedBy } },
    ),

  // 生产订单开工
  start: (id: number) =>
    api.post<unknown, ApiResponse<void>>(`${BASE_URL}/orders/${id}/start`),

  // 生产订单完工
  complete: (
    id: number,
    completedQty: number,
    scrappedQty: number,
  ) =>
    api.post<unknown, ApiResponse<void>>(
      `${BASE_URL}/orders/${id}/complete`,
      {},
      { params: { completedQty, scrappedQty } },
    ),

  // 关闭生产订单
  close: (id: number) =>
    api.post<unknown, ApiResponse<void>>(`${BASE_URL}/orders/${id}/close`),

  // 取消生产订单
  cancel: (id: number) =>
    api.post<unknown, ApiResponse<void>>(`${BASE_URL}/orders/${id}/cancel`),
};

// ==================== 工艺路线 API ====================
export const routingApi = {
  // 分页查询工艺路线
  getPage: (params: {
    tenantId: number;
    routingType?: number;
    status?: number;
    current?: number;
    size?: number;
  }) =>
    api.post<unknown, ApiResponse<PageResult<ProRoutingDTO>>>(
      `${BASE_URL}/routings/page`,
      {},
      { params },
    ),

  // 获取工艺路线详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<ProRoutingDTO>>(
      `${BASE_URL}/routings/${id}`,
    ),

  // 创建工艺路线
  create: (data: CreateRoutingRequest) =>
    api.post<unknown, ApiResponse<number>>(`${BASE_URL}/routings`, data),

  // 更新工艺路线
  update: (id: number, data: CreateRoutingRequest) =>
    api.put<unknown, ApiResponse<void>>(`${BASE_URL}/routings/${id}`, data),

  // 删除工艺路线
  delete: (id: number) =>
    api.delete<unknown, ApiResponse<void>>(`${BASE_URL}/routings/${id}`),

  // 启用工艺路线
  activate: (id: number) =>
    api.post<unknown, ApiResponse<void>>(
      `${BASE_URL}/routings/${id}/activate`,
    ),

  // 停用工艺路线
  deactivate: (id: number) =>
    api.post<unknown, ApiResponse<void>>(
      `${BASE_URL}/routings/${id}/deactivate`,
    ),
};

// ==================== 工序执行记录 API ====================
export const operationRecordApi = {
  // 分页查询工序执行记录
  getPage: (params: {
    tenantId: number;
    status?: number;
    workCenterId?: number;
    workerId?: number;
    current?: number;
    size?: number;
  }) =>
    api.post<unknown, ApiResponse<PageResult<ProOperationRecordDTO>>>(
      `${BASE_URL}/operation-records/page`,
      {},
      { params },
    ),

  // 获取工序执行记录详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<ProOperationRecordDTO>>(
      `${BASE_URL}/operation-records/${id}`,
    ),

  // 创建工序执行记录
  create: (data: CreateOperationRecordRequest) =>
    api.post<unknown, ApiResponse<number>>(
      `${BASE_URL}/operation-records`,
      data,
    ),

  // 更新工序执行记录
  update: (id: number, data: UpdateOperationRecordRequest) =>
    api.put<unknown, ApiResponse<void>>(
      `${BASE_URL}/operation-records/${id}`,
      data,
    ),

  // 获取指定生产订单的工序记录
  getByOrderId: (productionOrderId: number) =>
    api.get<unknown, ApiResponse<ProOperationRecordDTO[]>>(
      `${BASE_URL}/operation-records/order/${productionOrderId}`,
    ),

  // 获取生产进度
  getProgress: (productionOrderId: number) =>
    api.get<unknown, ApiResponse<ProductionProgressDTO>>(
      `${BASE_URL}/operation-records/order/${productionOrderId}/progress`,
    ),

  // 工序开工
  start: (id: number, workerId: number, workerName: string) =>
    api.post<unknown, ApiResponse<void>>(
      `${BASE_URL}/operation-records/${id}/start`,
      {},
      { params: { workerId, workerName } },
    ),

  // 工序完工
  complete: (
    id: number,
    completedQty: number,
    qualifiedQty: number,
    scrappedQty: number,
    actualManHours: number,
    actualMachineHours: number,
  ) =>
    api.post<unknown, ApiResponse<void>>(
      `${BASE_URL}/operation-records/${id}/complete`,
      {},
      {
        params: {
          completedQty,
          qualifiedQty,
          scrappedQty,
          actualManHours,
          actualMachineHours,
        },
      },
    ),
};
