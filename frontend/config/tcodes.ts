/**
 * SAP 事务码配置
 * 保持与 SAP ERP 事务码一致，方便用户快速导航
 */

export interface TCode {
  code: string;          // 事务码
  name: string;          // 功能名称
  path: string;          // 路由路径
  module: string;        // 所属模块
  description?: string;  // 描述
}

/**
 * 事务码映射表
 * 事务码不区分大小写
 */
export const TCODES: TCode[] = [
  // ==================== 财务模块 (FI) ====================
  // 会计科目
  { code: 'FS00', name: '编辑科目', path: '/finance/accounts', module: 'finance', description: '集中科目主记录维护' },
  { code: 'FSP0', name: '科目显示', path: '/finance/accounts', module: 'finance', description: '显示科目主记录' },
  { code: 'FSS0', name: '科目创建', path: '/finance/accounts', module: 'finance', description: '创建科目主记录' },

  // 凭证
  { code: 'FB50', name: '输入总账凭证', path: '/finance/vouchers', module: 'finance', description: '输入总账凭证' },
  { code: 'FB01', name: '输入凭证', path: '/finance/vouchers', module: 'finance', description: '输入凭证' },
  { code: 'FB60', name: '输入供应商发票', path: '/finance/vouchers', module: 'finance', description: '输入供应商发票' },
  { code: 'FB65', name: '输入供应商贷项凭证', path: '/finance/vouchers', module: 'finance', description: '输入供应商贷项凭证' },
  { code: 'FB70', name: '输入客户发票', path: '/finance/vouchers', module: 'finance', description: '输入客户发票' },
  { code: 'FB75', name: '输入客户贷项凭证', path: '/finance/vouchers', module: 'finance', description: '输入客户贷项凭证' },

  // 总账查询
  { code: 'FBL3N', name: '总账行项目显示', path: '/finance/ledger', module: 'finance', description: '总账科目行项目显示' },
  { code: 'FS10N', name: '总账余额显示', path: '/finance/ledger', module: 'finance', description: '总账科目余额显示' },
  { code: 'FAGLL03', name: '总账行项目显示(新)', path: '/finance/ledger', module: 'finance', description: '总账科目行项目显示(新)' },

  // 应收账款
  { code: 'FBL5N', name: '客户行项目显示', path: '/finance/receivables', module: 'finance', description: '客户行项目显示' },
  { code: 'FD10N', name: '客户余额显示', path: '/finance/receivables', module: 'finance', description: '客户余额显示' },
  { code: 'FD03', name: '客户显示', path: '/finance/receivables', module: 'finance', description: '显示客户主记录' },

  // 应付账款
  { code: 'FBL1N', name: '供应商行项目显示', path: '/finance/payables', module: 'finance', description: '供应商行项目显示' },
  { code: 'FK10N', name: '供应商余额显示', path: '/finance/payables', module: 'finance', description: '供应商余额显示' },
  { code: 'FK03', name: '供应商显示', path: '/finance/payables', module: 'finance', description: '显示供应商主记录' },

  // 固定资产
  { code: 'AS01', name: '创建资产', path: '/finance/assets', module: 'finance', description: '创建资产主记录' },
  { code: 'AS02', name: '更改资产', path: '/finance/assets', module: 'finance', description: '更改资产主记录' },
  { code: 'AS03', name: '显示资产', path: '/finance/assets', module: 'finance', description: '显示资产主记录' },
  { code: 'AW01N', name: '资产浏览器', path: '/finance/assets', module: 'finance', description: '资产浏览器' },
  { code: 'AFAB', name: '折旧运行', path: '/finance/assets', module: 'finance', description: '执行折旧运行' },

  // 期间管理
  { code: 'OB52', name: '期间维护', path: '/finance/periods', module: 'finance', description: '维护会计期间' },
  { code: 'S_ALR_87003642', name: '会计年度变式', path: '/finance/periods', module: 'finance', description: '维护会计年度变式' },

  // 财务报表
  { code: 'F.01', name: '资产负债表', path: '/finance/reports', module: 'finance', description: '资产负债表' },
  { code: 'F.02', name: '利润表', path: '/finance/reports', module: 'finance', description: '利润表' },
  { code: 'S_PL0_86000028', name: '财务报表', path: '/finance/reports', module: 'finance', description: '财务报表总览' },
  { code: 'F.08', name: '总账余额表', path: '/finance/reports', module: 'finance', description: '总账科目余额表' },
  { code: 'FAGLB03', name: '总账余额显示(新)', path: '/finance/reports', module: 'finance', description: '总账余额显示(新)' },

  // ==================== 人力资源模块 (HR) ====================
  { code: 'PA20', name: '显示HR主数据', path: '/hrm/employees', module: 'hrm', description: '显示员工主数据' },
  { code: 'PA30', name: '维护HR主数据', path: '/hrm/employees', module: 'hrm', description: '维护员工主数据' },
  { code: 'PA40', name: '人事措施', path: '/hrm/employees', module: 'hrm', description: '执行人事措施' },
  { code: 'PP01', name: '人事管理', path: '/hrm/employees', module: 'hrm', description: '人事管理' },
  { code: 'PP02', name: '显示人事管理', path: '/hrm/employees', module: 'hrm', description: '显示人事管理' },

  // 组织管理
  { code: 'PPOME', name: '组织结构', path: '/hrm/departments', module: 'hrm', description: '组织管理(含结构)' },
  { code: 'PPOCE', name: '创建组织单位', path: '/hrm/departments', module: 'hrm', description: '创建组织单位' },
  { code: 'PPOSE', name: '显示组织结构', path: '/hrm/departments', module: 'hrm', description: '显示组织结构' },

  // 考勤
  { code: 'PT60', name: '时间管理', path: '/hrm/attendance', module: 'hrm', description: '时间管理' },
  { code: 'PT50', name: '时间数据维护', path: '/hrm/attendance', module: 'hrm', description: '维护时间数据' },

  // 薪资
  { code: 'PC00_M00_CALC', name: '薪资计算', path: '/hrm/payroll', module: 'hrm', description: '执行薪资计算' },
  { code: 'PC_PAYRESULT', name: '薪资结果显示', path: '/hrm/payroll', module: 'hrm', description: '显示薪资结果' },

  // ==================== 供应链模块 (MM) ====================
  // 物料管理
  { code: 'MM01', name: '创建物料', path: '/supply/inventory', module: 'supply', description: '创建物料主数据' },
  { code: 'MM02', name: '更改物料', path: '/supply/inventory', module: 'supply', description: '更改物料主数据' },
  { code: 'MM03', name: '显示物料', path: '/supply/inventory', module: 'supply', description: '显示物料主数据' },
  { code: 'MMBE', name: '库存概览', path: '/supply/inventory', module: 'supply', description: '库存概览' },
  { code: 'MB52', name: '库存清单', path: '/supply/inventory', module: 'supply', description: '仓库库存清单' },
  { code: 'MB51', name: '物料凭证清单', path: '/supply/inventory', module: 'supply', description: '物料凭证清单' },
  { code: 'MIGO', name: '货物移动', path: '/supply/inventory', module: 'supply', description: '货物移动/库存过账' },
  { code: 'MI01', name: '库存盘点', path: '/supply/inventory', module: 'supply', description: '创建盘点凭证' },

  // 采购管理
  { code: 'ME21N', name: '创建采购订单', path: '/supply/purchase', module: 'supply', description: '创建采购订单' },
  { code: 'ME22N', name: '更改采购订单', path: '/supply/purchase', module: 'supply', description: '更改采购订单' },
  { code: 'ME23N', name: '显示采购订单', path: '/supply/purchase', module: 'supply', description: '显示采购订单' },
  { code: 'ME51N', name: '创建采购申请', path: '/supply/purchase', module: 'supply', description: '创建采购申请' },
  { code: 'ME2N', name: '采购订单清单', path: '/supply/purchase', module: 'supply', description: '按PO号的采购订单清单' },

  // 供应商管理
  { code: 'XK01', name: '创建供应商', path: '/supply/purchase', module: 'supply', description: '创建供应商(集中)' },
  { code: 'XK02', name: '更改供应商', path: '/supply/purchase', module: 'supply', description: '更改供应商(集中)' },
  { code: 'XK03', name: '显示供应商', path: '/supply/purchase', module: 'supply', description: '显示供应商(集中)' },
  { code: 'MK01', name: '创建供应商(采购)', path: '/supply/purchase', module: 'supply', description: '创建供应商(采购视图)' },

  // ==================== 销售模块 (SD) ====================
  { code: 'VA01', name: '创建销售订单', path: '/sales/orders', module: 'sales', description: '创建销售订单' },
  { code: 'VA02', name: '更改销售订单', path: '/sales/orders', module: 'sales', description: '更改销售订单' },
  { code: 'VA03', name: '显示销售订单', path: '/sales/orders', module: 'sales', description: '显示销售订单' },
  { code: 'VA05', name: '销售订单清单', path: '/sales/orders', module: 'sales', description: '销售订单清单' },

  // 客户
  { code: 'VD01', name: '创建客户', path: '/sales/customers', module: 'sales', description: '创建客户(销售区域)' },
  { code: 'VD02', name: '更改客户', path: '/sales/customers', module: 'sales', description: '更改客户(销售区域)' },
  { code: 'VD03', name: '显示客户', path: '/sales/customers', module: 'sales', description: '显示客户(销售区域)' },
  { code: 'XD01', name: '创建客户(集中)', path: '/sales/customers', module: 'sales', description: '创建客户(集中)' },
  { code: 'XD02', name: '更改客户(集中)', path: '/sales/customers', module: 'sales', description: '更改客户(集中)' },
  { code: 'XD03', name: '显示客户(集中)', path: '/sales/customers', module: 'sales', description: '显示客户(集中)' },

  // 交货
  { code: 'VL01N', name: '创建交货', path: '/sales/delivery', module: 'sales', description: '创建外向交货' },
  { code: 'VL02N', name: '更改交货', path: '/sales/delivery', module: 'sales', description: '更改外向交货' },
  { code: 'VL03N', name: '显示交货', path: '/sales/delivery', module: 'sales', description: '显示外向交货' },
  { code: 'VL10B', name: '交货清单', path: '/sales/delivery', module: 'sales', description: '交货清单' },

  // 发票
  { code: 'VF01', name: '创建开票凭证', path: '/sales/billing', module: 'sales', description: '创建开票凭证' },
  { code: 'VF02', name: '更改开票凭证', path: '/sales/billing', module: 'sales', description: '更改开票凭证' },
  { code: 'VF03', name: '显示开票凭证', path: '/sales/billing', module: 'sales', description: '显示开票凭证' },
  { code: 'VF05N', name: '开票凭证清单', path: '/sales/billing', module: 'sales', description: '开票凭证清单' },

  // ==================== 生产模块 (PP) ====================
  { code: 'MM01', name: '创建物料', path: '/production/materials', module: 'production', description: '创建物料主数据' },
  { code: 'CS01', name: '创建BOM', path: '/production/bom', module: 'production', description: '创建物料清单' },
  { code: 'CS02', name: '更改BOM', path: '/production/bom', module: 'production', description: '更改物料清单' },
  { code: 'CS03', name: '显示BOM', path: '/production/bom', module: 'production', description: '显示物料清单' },
  { code: 'CA01', name: '创建工艺路线', path: '/production/routing', module: 'production', description: '创建工艺路线' },
  { code: 'CA02', name: '更改工艺路线', path: '/production/routing', module: 'production', description: '更改工艺路线' },
  { code: 'CA03', name: '显示工艺路线', path: '/production/routing', module: 'production', description: '显示工艺路线' },
  { code: 'CO01', name: '创建生产订单', path: '/production/orders', module: 'production', description: '创建生产订单' },
  { code: 'CO02', name: '更改生产订单', path: '/production/orders', module: 'production', description: '更改生产订单' },
  { code: 'CO03', name: '显示生产订单', path: '/production/orders', module: 'production', description: '显示生产订单' },
  { code: 'CO11N', name: '工单确认', path: '/production/confirmation', module: 'production', description: '工单确认' },
  { code: 'MD01', name: 'MRP运行', path: '/production/mrp', module: 'production', description: 'MRP运行' },
  { code: 'MD04', name: '库存需求清单', path: '/production/mrp', module: 'production', description: '库存/需求清单' },

  // ==================== 系统管理 ====================
  { code: 'SU01', name: '用户维护', path: '/settings/users', module: 'settings', description: '维护用户' },
  { code: 'SU01D', name: '用户显示', path: '/settings/users', module: 'settings', description: '显示用户' },
  { code: 'PFCG', name: '角色维护', path: '/settings/roles', module: 'settings', description: '维护角色' },
  { code: 'SUIM', name: '用户信息系统', path: '/settings/users', module: 'settings', description: '用户信息系统' },
  { code: 'SM01', name: '事务码锁', path: '/settings/system', module: 'settings', description: '锁定/解锁事务码' },

  // ==================== 仪表盘 ====================
  { code: 'HOME', name: '首页', path: '/dashboard', module: 'common', description: '返回首页' },
  { code: 'DASHBOARD', name: '仪表盘', path: '/dashboard', module: 'common', description: '仪表盘' },
];

/**
 * 事务码查找映射 (大写)
 */
export const TCODE_MAP: Map<string, TCode> = new Map(
  TCODES.map(t => [t.code.toUpperCase(), t])
);

/**
 * 根据事务码查找路由
 * @param code 事务码 (不区分大小写)
 * @returns TCode 或 undefined
 */
export function findTCode(code: string): TCode | undefined {
  return TCODE_MAP.get(code.toUpperCase());
}

/**
 * 获取模块下所有事务码
 * @param module 模块名称
 * @returns TCode[]
 */
export function getTCodeByModule(module: string): TCode[] {
  return TCODES.filter(t => t.module === module);
}

/**
 * 搜索事务码
 * @param keyword 搜索关键词 (匹配事务码和名称)
 * @returns TCode[]
 */
export function searchTCode(keyword: string): TCode[] {
  const lower = keyword.toLowerCase();
  return TCODES.filter(t =>
    t.code.toLowerCase().includes(lower) ||
    t.name.toLowerCase().includes(lower) ||
    t.description?.toLowerCase().includes(lower)
  );
}
