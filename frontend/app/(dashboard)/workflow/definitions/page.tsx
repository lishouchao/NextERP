'use client';

import { useState, useEffect } from 'react';
import api from '@/lib/api/client';

interface ProcessDefinition {
  id: number;
  processKey: string;
  processName: string;
  version: number;
  description?: string;
  category?: string;
  status: number;
  enabled: boolean;
}

export default function ProcessDefinitionsPage() {
  const [definitions, setDefinitions] = useState<ProcessDefinition[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDefinitions();
  }, []);

  const fetchDefinitions = async () => {
    try {
      const tenantId = localStorage.getItem('tenantId') || '1';
      const response = await api.get(`/workflow/v1/definitions/published?tenantId=${tenantId}`);
      setDefinitions(response.data);
    } catch (error) {
      console.error('获取流程定义失败', error);
    } finally {
      setLoading(false);
    }
  };

  const startProcess = async (processKey: string) => {
    try {
      const userId = localStorage.getItem('userId') || '1';
      await api.post('/workflow/v1/process/start', {
        processDefinitionKey: processKey,
        businessKey: `BIZ-${Date.now()}`,
        variables: {},
        initiator: userId
      });
      alert('流程启动成功');
    } catch (error) {
      console.error('启动流程失败', error);
      alert('启动流程失败');
    }
  };

  const getStatusBadge = (status: number) => {
    switch (status) {
      case 0:
        return <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">草稿</span>;
      case 1:
        return <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">已发布</span>;
      case 2:
        return <span className="px-2 py-1 text-xs rounded-full bg-red-100 text-red-800">已归档</span>;
      default:
        return <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-800">未知</span>;
    }
  };

  if (loading) {
    return <div className="p-6">加载中...</div>;
  }

  return (
    <div className="p-6">
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">流程定义</h1>
          <p className="text-gray-500">共 {definitions.length} 个流程定义</p>
        </div>
        <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
          新建流程
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {definitions.map((definition) => (
          <div key={definition.id} className="bg-white rounded-lg shadow p-6">
            <div className="flex justify-between items-start mb-4">
              <h3 className="text-lg font-semibold">{definition.processName}</h3>
              {getStatusBadge(definition.status)}
            </div>
            <p className="text-gray-600 text-sm mb-2">
              <span className="font-medium">流程Key:</span> {definition.processKey}
            </p>
            <p className="text-gray-600 text-sm mb-2">
              <span className="font-medium">版本:</span> v{definition.version}
            </p>
            {definition.category && (
              <p className="text-gray-600 text-sm mb-4">
                <span className="font-medium">分类:</span> {definition.category}
              </p>
            )}
            {definition.description && (
              <p className="text-gray-500 text-sm mb-4 line-clamp-2">
                {definition.description}
              </p>
            )}
            <div className="flex gap-2">
              <button
                onClick={() => startProcess(definition.processKey)}
                disabled={!definition.enabled}
                className={`flex-1 px-4 py-2 rounded-lg text-sm ${
                  definition.enabled
                    ? 'bg-blue-600 text-white hover:bg-blue-700'
                    : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                }`}
              >
                发起流程
              </button>
              <button className="px-4 py-2 border border-gray-300 rounded-lg text-sm hover:bg-gray-50">
                查看详情
              </button>
            </div>
          </div>
        ))}
      </div>

      {definitions.length === 0 && (
        <div className="text-center py-12 text-gray-500">
          暂无流程定义
        </div>
      )}
    </div>
  );
}
