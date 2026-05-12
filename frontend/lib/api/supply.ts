import api from './client';
import type { ApiResponse, PageResult } from '@/types';

const BASE_URL = '/api/v1/supply';

// ==================== Types ====================
export interface MaterialDTO {
  id: number;
  materialNumber: string;
  materialType: string;
  materialGroup: string;
  description: string;
  baseUom: string;
  crossPlantStatus: string;
  industrySector: string;
  validFrom: string;
  validTo: string;
  plant?: string;
  mrpType?: string;
  procurementType?: string;
  lotSize?: string;
  reorderPoint?: number;
  salesOrg?: string;
  distrChannel?: string;
  division?: string;
  valuationClass?: string;
  priceUnit?: number;
  standardPrice?: number;
  movingPrice?: number;
  tenantId?: number;
}

export interface CreateMaterialRequest {
  materialType: string;
  materialGroup: string;
  description: string;
  baseUom: string;
  crossPlantStatus?: string;
  industrySector?: string;
  validFrom?: string;
  validTo?: string;
  plant?: string;
  mrpType?: string;
  procurementType?: string;
  lotSize?: string;
  reorderPoint?: number;
  salesOrg?: string;
  distrChannel?: string;
  division?: string;
  valuationClass?: string;
  priceUnit?: number;
  standardPrice?: number;
  movingPrice?: number;
  tenantId: number;
}

export interface StockDTO {
  id: number;
  materialCode: string;
  materialName: string;
  category: string;
  warehouse: string;
  location: string;
  quantity: number;
  unit: string;
  safetyStock: number;
  maxStock: number;
  unitPrice: number;
  totalValue: number;
  status: number;
}

export interface MaterialDocDTO {
  id: number;
  movementNo: string;
  materialCode: string;
  materialName: string;
  movementType: string;
  movementTypeName: string;
  quantity: number;
  warehouse: string;
  referenceNo: string;
  createdAt: string;
  createdBy: string;
}

export interface PurchaseOrderDTO {
  id: number;
  poNumber: string;
  poType: string;
  vendorCode: string;
  vendorName: string;
  purchasingOrg: string;
  purchasingGroup: string;
  companyCode: string;
  currency: string;
  documentDate: string;
  validFrom: string;
  validTo: string;
  totalNetValue: number;
  totalTaxAmount: number;
  totalGrossValue: number;
  status: string;
  releaseStatus: string;
  items: PurchaseOrderItemDTO[];
  createdBy?: string;
  approvedBy?: string;
  approvedAt?: string;
}

export interface PurchaseOrderItemDTO {
  poItem: number;
  materialCode: string;
  shortText: string;
  quantity: number;
  unit: string;
  price: number;
  netValue: number;
  taxCode: string;
  taxAmount: number;
  plantCode: string;
  slocCode: string;
  deliveryDate: string;
  quantityDelivered: number;
  quantityInvoiced: number;
  itemCategory: string;
}

export interface CreatePurchaseOrderRequest {
  vendorCode: string;
  purchasingOrg: string;
  purchasingGroup?: string;
  companyCode?: string;
  currency?: string;
  documentDate?: string;
  validFrom?: string;
  validTo?: string;
  poType?: string;
  items: {
    materialCode: string;
    shortText: string;
    quantity: number;
    unit: string;
    price: number;
    plantCode: string;
    slocCode?: string;
    deliveryDate?: string;
    taxCode?: string;
  }[];
  tenantId: number;
}

export interface PurchaseReqDTO {
  id: number;
  prNumber: string;
  requesterName: string;
  department: string;
  priority: number;
  status: string;
  totalAmount: number;
  currency: string;
  requiredDate: string;
  createdAt: string;
  createdBy: string;
  items: PurchaseReqItemDTO[];
}

export interface PurchaseReqItemDTO {
  prItem: number;
  materialCode: string;
  shortText: string;
  quantity: number;
  unit: string;
  price: number;
  amount: number;
  plantCode: string;
  deliveryDate: string;
}

export interface CreatePurchaseReqRequest {
  requesterName: string;
  department?: string;
  priority?: number;
  requiredDate?: string;
  items: {
    materialCode: string;
    shortText: string;
    quantity: number;
    unit: string;
    price: number;
    plantCode: string;
    deliveryDate?: string;
  }[];
  tenantId: number;
}

// ==================== 物料 API ====================
export const materialApi = {
  // 分页查询物料列表
  getPage: (params: {
    tenantId: number;
    materialType?: string;
    current?: number;
    size?: number;
  }) =>
    api.post<unknown, ApiResponse<PageResult<MaterialDTO>>>(
      `${BASE_URL}/materials/page`,
      {},
      { params },
    ),

  // 搜索物料
  search: (params: {
    keyword: string;
    tenantId: number;
    current?: number;
    size?: number;
  }) =>
    api.get<unknown, ApiResponse<PageResult<MaterialDTO>>>(
      `${BASE_URL}/materials/search`,
      { params },
    ),

  // 获取物料详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<MaterialDTO>>(`${BASE_URL}/materials/${id}`),

  // 创建物料
  create: (data: CreateMaterialRequest) =>
    api.post<unknown, ApiResponse<number>>(`${BASE_URL}/materials`, data),

  // 更新物料
  update: (id: number, data: CreateMaterialRequest) =>
    api.put<unknown, ApiResponse<void>>(`${BASE_URL}/materials/${id}`, data),

  // 删除物料
  delete: (id: number) =>
    api.delete<unknown, ApiResponse<void>>(`${BASE_URL}/materials/${id}`),
};

// ==================== 库存 API ====================
export const inventoryApi = {
  // 查询库存
  getStock: (params: {
    tenantId: number;
    plantId?: number;
    materialId?: number;
    current?: number;
    size?: number;
  }) =>
    api.get<unknown, ApiResponse<PageResult<StockDTO>>>(
      `${BASE_URL}/inventory/stock`,
      { params },
    ),

  // 查询物料凭证
  getMaterialDocs: (params: {
    tenantId: number;
    current?: number;
    size?: number;
  }) =>
    api.get<unknown, ApiResponse<PageResult<MaterialDocDTO>>>(
      `${BASE_URL}/inventory/material-docs`,
      { params },
    ),

  // 收货
  postGoodsReceipt: (params: {
    poId: number;
    tenantId: number;
    movementType?: string;
  }) =>
    api.post<unknown, ApiResponse<number>>(
      `${BASE_URL}/inventory/goods-receipt`,
      {},
      { params },
    ),

  // 发货
  postGoodsIssue: (params: {
    deliveryId: number;
    tenantId: number;
    movementType?: string;
  }) =>
    api.post<unknown, ApiResponse<number>>(
      `${BASE_URL}/inventory/goods-issue`,
      {},
      { params },
    ),
};

// ==================== 采购订单 API ====================
export const purchaseOrderApi = {
  // 分页查询采购订单
  getPage: (params: {
    tenantId: number;
    status?: string;
    current?: number;
    size?: number;
  }) =>
    api.post<unknown, ApiResponse<PageResult<PurchaseOrderDTO>>>(
      `${BASE_URL}/purchase-orders/page`,
      {},
      { params },
    ),

  // 获取采购订单详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<PurchaseOrderDTO>>(
      `${BASE_URL}/purchase-orders/${id}`,
    ),

  // 创建采购订单
  create: (data: CreatePurchaseOrderRequest) =>
    api.post<unknown, ApiResponse<number>>(
      `${BASE_URL}/purchase-orders`,
      data,
    ),

  // 提交采购订单
  submit: (id: number) =>
    api.post<unknown, ApiResponse<void>>(
      `${BASE_URL}/purchase-orders/${id}/submit`,
    ),

  // 审批采购订单
  approve: (id: number, approvedBy: string) =>
    api.post<unknown, ApiResponse<void>>(
      `${BASE_URL}/purchase-orders/${id}/approve`,
      {},
      { params: { approvedBy } },
    ),

  // 关闭采购订单
  close: (id: number) =>
    api.post<unknown, ApiResponse<void>>(
      `${BASE_URL}/purchase-orders/${id}/close`,
    ),
};

// ==================== 采购申请 API ====================
export const purchaseReqApi = {
  // 分页查询采购申请
  getPage: (params: {
    tenantId: number;
    status?: string;
    current?: number;
    size?: number;
  }) =>
    api.post<unknown, ApiResponse<PageResult<PurchaseReqDTO>>>(
      `${BASE_URL}/purchase-reqs/page`,
      {},
      { params },
    ),

  // 获取采购申请详情
  getById: (id: number) =>
    api.get<unknown, ApiResponse<PurchaseReqDTO>>(
      `${BASE_URL}/purchase-reqs/${id}`,
    ),

  // 创建采购申请
  create: (data: CreatePurchaseReqRequest) =>
    api.post<unknown, ApiResponse<number>>(
      `${BASE_URL}/purchase-reqs`,
      data,
    ),

  // 提交采购申请
  submit: (id: number) =>
    api.post<unknown, ApiResponse<void>>(
      `${BASE_URL}/purchase-reqs/${id}/submit`,
    ),

  // 审批采购申请
  approve: (id: number, approvedBy: string) =>
    api.post<unknown, ApiResponse<void>>(
      `${BASE_URL}/purchase-reqs/${id}/approve`,
      {},
      { params: { approvedBy } },
    ),

  // 驳回采购申请
  reject: (id: number, rejectedBy: string, reason: string) =>
    api.post<unknown, ApiResponse<void>>(
      `${BASE_URL}/purchase-reqs/${id}/reject`,
      {},
      { params: { rejectedBy, reason } },
    ),
};
