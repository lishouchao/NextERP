'use client';

import { useState, useEffect } from 'react';
import api from '@/lib/api/client';

interface ReportData {
  reportDate: string;
  period: string;
  groupedAccounts: Map<number, any[]>;
  typeTotals: Map<number, number>;
  totalAssets: number;
  totalLiabilities: number;
  totalEquity: number;
  totalCost: number;
  totalProfit: number;
}

export default function FinancialReportPage() {
  const [reportType, setReportType] = useState<string>('balance_sheet');
  const [period, setPeriod] = useState<string>('');
  const [reportData, setReportData] = useState<ReportData | null>(null);
  const [loading, setLoading] = useState(false);

  const generateReport = async () => {
    if (!period) {
      alert('请选择会计期间');
      return;
    }

    setLoading(true);
    try {
      const tenantId = localStorage.getItem('tenantId') || '1';
      const response = await api.get(`/report/v1/financial/${reportType}`, {
        params: { tenantId, period }
      });
      setReportData(response.data);
    } catch (error) {
      console.error('生成报表失败', error);
      alert('生成报表失败');
    } finally {
      setLoading(false);
    }
  };

  const exportToExcel = async () => {
    if (!period) {
      alert('请选择会计期间');
      return;
    }

    try {
      const tenantId = localStorage.getItem('tenantId') || '1';
      const response = await api.post(`/report/v1/financial/export`, {
        reportType,
        tenantId,
        period
      }, {
        responseType: 'blob'
      });

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${reportType}_${period}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error('导出失败', error);
      alert('导出失败');
    }
  };

  const reportTypes = [
    { value: 'balance_sheet', label: '资产负债表' },
    { value: 'trial_balance', label: '试算平衡表' },
    { value: 'income_statement', label: '利润表' },
    { value: 'cash_flow', label: '现金流量表' },
    { value: 'account_balance', label: '科目余额表' }
  ];

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">财务报表</h1>
        <p className="text-gray-500">生成和导出财务报表</p>
      </div>

      {/* 查询条件 */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              报表类型
            </label>
            <select
              value={reportType}
              onChange={(e) => setReportType(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
            >
              {reportTypes.map(type => (
                <option key={type.value} value={type.value}>
                  {type.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              会计期间
            </label>
            <input
              type="month"
              value={period}
              onChange={(e) => setPeriod(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg"
            />
          </div>
          <div className="flex items-end gap-2">
            <button
              onClick={generateReport}
              disabled={loading}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
            >
              {loading ? '生成中...' : '生成报表'}
            </button>
            <button
              onClick={exportToExcel}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
            >
              导出Excel
            </button>
          </div>
        </div>
      </div>

      {/* 报表展示区域 */}
      {reportData && (
        <div className="bg-white rounded-lg shadow p-6">
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead>
                <tr className="border-b">
                  <th className="px-4 py-2 text-left">科目编码</th>
                  <th className="px-4 py-2 text-left">科目名称</th>
                  <th className="px-4 py-2 text-right">期初余额</th>
                  <th className="px-4 py-2 text-right">本期借方</th>
                  <th className="px-4 py-2 text-right">本期贷方</th>
                  <th className="px-4 py-2 text-right">期末余额</th>
                </tr>
              </thead>
              <tbody>
                {/* 报表数据行 */}
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-gray-500">
                    报表数据将在此显示
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )}

      {!reportData && (
        <div className="bg-white rounded-lg shadow p-12 text-center text-gray-500">
          请选择报表类型和会计期间，点击&ldquo;生成报表&rdquo;按钮
        </div>
      )}
    </div>
  );
}
