// ==================== 财务模块类型定义 ====================

/**
 * 会计科目
 */
export interface FinAccount {
  id: number;
  accountCode: string;
  accountName: string;
  accountType: number; // 1-资产 2-负债 3-所有者权益 4-成本 5-损益
  accountDirection: number; // 1-借方 2-贷方
  parentId: number | null;
  accountLevel: number;
  isLeaf: boolean;
  isCash: boolean;
  isBank: boolean;
  isQuantity: boolean;
  quantityUnit?: string;
  isForeignCurrency: boolean;
  currency?: string;
  isAuxiliary: boolean;
  auxiliaryType?: string;
  openingBalance?: number;
  currentDebit?: number;
  currentCredit?: number;
  yearDebit?: number;
  yearCredit?: number;
  endingBalance?: number;
  status: number;
  remark?: string;
  children?: FinAccount[];
}

/**
 * 凭证分录
 */
export interface FinVoucherEntry {
  id: number;
  voucherId: number;
  lineNo: number;
  accountId: number;
  accountCode: string;
  accountName: string;
  summary: string;
  debitAmount: number;
  creditAmount: number;
  quantity?: number;
  unitPrice?: number;
  currency?: string;
  exchangeRate?: number;
  originalAmount?: number;
  auxiliaryId?: number;
  auxiliaryName?: string;
}

/**
 * 凭证
 */
export interface FinVoucher {
  id: number;
  voucherNo: string;
  voucherWord: string;
  voucherDate: string;
  accountingPeriod: string;
  voucherType: number; // 1-收款 2-付款 3-转账
  attachmentCount: number;
  debitAmount: number;
  creditAmount: number;
  createdById: number;
  createdByName: string;
  approvedById?: number;
  approvedByName?: string;
  approvedAt?: string;
  postedById?: number;
  postedByName?: string;
  postedAt?: string;
  voucherStatus: number; // 0-草稿 1-待审核 2-已审核 3-已记账 4-已驳回
  rejectReason?: string;
  summary?: string;
  remark?: string;
  sourceType?: string;
  sourceId?: number;
  entries: FinVoucherEntry[];
}

/**
 * 会计期间
 */
export interface FinAccountingPeriod {
  id: number;
  periodCode: string;
  periodName: string;
  fiscalYear: number;
  periodNumber: number;
  startDate: string;
  endDate: string;
  periodStatus: number; // 0-未开启 1-已开启 2-已结账
  isAdjustmentPeriod: boolean;
  closingAt?: string;
  closingBy?: string;
}

// ==================== 查询参数 ====================

export interface FinAccountQuery {
  accountCode?: string;
  accountName?: string;
  accountType?: number;
  status?: number;
}

export interface FinVoucherQuery {
  voucherNo?: string;
  voucherDateStart?: string;
  voucherDateEnd?: string;
  accountingPeriod?: string;
  voucherType?: number;
  voucherStatus?: number;
  current?: number;
  size?: number;
}

// ==================== 表单数据 ====================

export interface FinAccountFormData {
  accountCode: string;
  accountName: string;
  accountType: number;
  accountDirection: number;
  parentId?: number;
  isCash?: boolean;
  isBank?: boolean;
  isQuantity?: boolean;
  quantityUnit?: string;
  isForeignCurrency?: boolean;
  currency?: string;
  isAuxiliary?: boolean;
  auxiliaryType?: string;
  openingBalance?: number;
  remark?: string;
}

export interface FinVoucherFormData {
  voucherWord: string;
  voucherDate: string;
  voucherType: number;
  attachmentCount?: number;
  summary?: string;
  remark?: string;
  entries: {
    accountId: number;
    summary: string;
    debitAmount: number;
    creditAmount: number;
  }[];
}

// ==================== 枚举常量 ====================

export const ACCOUNT_TYPE_OPTIONS = [
  { value: 1, label: '资产' },
  { value: 2, label: '负债' },
  { value: 3, label: '所有者权益' },
  { value: 4, label: '成本' },
  { value: 5, label: '损益' },
];

export const ACCOUNT_DIRECTION_OPTIONS = [
  { value: 1, label: '借方' },
  { value: 2, label: '贷方' },
];

export const VOUCHER_TYPE_OPTIONS = [
  { value: 1, label: '收款凭证' },
  { value: 2, label: '付款凭证' },
  { value: 3, label: '转账凭证' },
];

export const VOUCHER_STATUS_OPTIONS = [
  { value: 0, label: '草稿', color: 'default' },
  { value: 1, label: '待审核', color: 'orange' },
  { value: 2, label: '已审核', color: 'blue' },
  { value: 3, label: '已记账', color: 'green' },
  { value: 4, label: '已驳回', color: 'red' },
];

export const VOUCHER_WORD_OPTIONS = [
  { value: '记', label: '记账凭证' },
  { value: '收', label: '收款凭证' },
  { value: '付', label: '付款凭证' },
  { value: '转', label: '转账凭证' },
];
