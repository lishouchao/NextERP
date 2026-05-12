'use client';

import { useState, useEffect } from 'react';
import api from '@/lib/api/client';

interface Report {
  id: number;
  reportCode: string;
  reportName: string;
  reportType: string;
  reportGroup: string;
  status: number;
  sortOrder: number;
}

export default function ReportListPage() {
  const [reports, setReports] = useState<Report[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedGroup, setSelectedGroup] = useState<string>('');

  useEffect(() => {
    fetchReports();
  }, [selectedGroup]);

  const fetchReports = async () => {
    try {
      const tenantId = localStorage.getItem('tenantId') || '1';
      const params = new URLSearchParams({ tenantId, current: '1', size: '100' });
      if (selectedGroup) {
        params.append('reportGroup', selectedGroup);
      }
      const response = await api.get(`/report/v1/management?${params}`);
      setReports(response.data.records || []);
    } catch (error) {
      console.error('获取报表列表失败', error);
    } finally {
      setLoading(false);
    }
  };

  const exportReport = async (reportCode: string) => {
    try {
      const tenantId = localStorage.getItem('tenantId') || '1';
      const response = await api.post(`/report/v1/reports/${reportCode}/export?tenantId=${tenantId}`, {}, {
        responseType: 'blob'
      });

      // 创建下载链接
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${reportCode}_${Date.now()}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error('导出报表失败', error);
      alert('导出报表失败');
    }
  };

  const getReportTypeLabel = (type: string) => {
    switch (type) {
      case 'table':
        return <span className="px-2 py-1 text-xs rounded-full bg-blue-100 text-blue-800">表格</span>;
      case 'chart':
        return <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">图表</span>;
      case 'pivot':
        return <span className="px-2 py-1 text-xs rounded-full bg-purple-100 text-purple-800">透视表</span>;
      default:
        return <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">{type}</span>;
    }
  };

  const groups = Array.from(new Set(reports.map(r => r.reportGroup).filter(Boolean)));

  if (loading) {
    return <div className="p-6">加载中...</div>;
  }

  return (
    <div className="p-6">
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">报表中心</h1>
          <p className="text-gray-500">共 {reports.length} 个报表</p>
        </div>
      </div>

      {/* 分组筛选 */}
      {groups.length > 0 && (
        <div className="mb-4 flex gap-2">
          <button
            onClick={() => setSelectedGroup('')}
            className={`px-4 py-2 rounded-lg text-sm ${!selectedGroup ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700'}`}
          >
            全部
          </button>
          {groups.map(group => (
            <button
              key={group}
              onClick={() => setSelectedGroup(group)}
              className={`px-4 py-2 rounded-lg text-sm ${selectedGroup === group ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700'}`}
            >
              {group}
            </button>
          ))}
        </div>
      )}

      {/* 报表列表 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {reports.map((report) => (
          <div key={report.id} className="bg-white rounded-lg shadow p-6">
            <div className="flex justify-between items-start mb-4">
              <h3 className="text-lg font-semibold">{report.reportName}</h3>
              {getReportTypeLabel(report.reportType)}
            </div>
            <p className="text-gray-600 text-sm mb-2">
              <span className="font-medium">编码:</span> {report.reportCode}
            </p>
            {report.reportGroup && (
              <p className="text-gray-600 text-sm mb-4">
                <span className="font-medium">分组:</span> {report.reportGroup}
              </p>
            )}
            <div className="flex gap-2">
              <button
                onClick={() => exportReport(report.reportCode)}
                disabled={report.status !== 1}
                className={`flex-1 px-4 py-2 rounded-lg text-sm ${
                  report.status === 1
                    ? 'bg-green-600 text-white hover:bg-green-700'
                    : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                }`}
              >
                导出Excel
              </button>
              <button className="px-4 py-2 border border-gray-300 rounded-lg text-sm hover:bg-gray-50">
                查看数据
              </button>
            </div>
          </div>
        ))}
      </div>

      {reports.length === 0 && (
        <div className="text-center py-12 text-gray-500">
          暂无报表
        </div>
      )}
    </div>
  );
}
