'use client';

import { useState, useEffect } from 'react';
import api from '@/lib/api/client';

interface ProcessInstance {
  processInstanceId: string;
  processDefinitionName: string;
  businessKey: string;
  startTime: string;
  status: string;
  initiator?: string;
}

export default function MyRequestsPage() {
  const [instances, setInstances] = useState<ProcessInstance[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchInstances();
  }, []);

  const fetchInstances = async () => {
    try {
      const tenantId = localStorage.getItem('tenantId') || '1';
      const response = await api.get(`/workflow/v1/monitor/instances?tenantId=${tenantId}`);
      setInstances(response.data);
    } catch (error) {
      console.error('获取流程实例失败', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'running':
        return <span className="px-2 py-1 text-xs rounded-full bg-blue-100 text-blue-800">进行中</span>;
      case 'finished':
        return <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">已完成</span>;
      case 'suspended':
        return <span className="px-2 py-1 text-xs rounded-full bg-yellow-100 text-yellow-800">已挂起</span>;
      default:
        return <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">{status}</span>;
    }
  };

  if (loading) {
    return <div className="p-6">加载中...</div>;
  }

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">我的申请</h1>
        <p className="text-gray-500">共 {instances.length} 条流程记录</p>
      </div>

      <div className="bg-white rounded-lg shadow">
        <table className="min-w-full">
          <thead>
            <tr className="border-b">
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">流程名称</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">业务编号</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">发起时间</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {instances.map((instance) => (
              <tr key={instance.processInstanceId} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">{instance.processDefinitionName}</td>
                <td className="px-6 py-4 whitespace-nowrap">{instance.businessKey}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {new Date(instance.startTime).toLocaleString('zh-CN')}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">{getStatusBadge(instance.status)}</td>
                <td className="px-6 py-4 whitespace-nowrap text-right">
                  <button className="text-blue-600 hover:text-blue-800 mr-4">
                    查看
                  </button>
                  {instance.status === 'running' && (
                    <button className="text-red-600 hover:text-red-800">
                      撤销
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {instances.length === 0 && (
              <tr>
                <td colSpan={5} className="px-6 py-4 text-center text-gray-500">
                  暂无流程记录
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
