'use client';

import { useState, useEffect } from 'react';
import api from '@/lib/api/client';

interface Task {
  taskId: string;
  taskName: string;
  assignee: string;
  createTime: string;
  processInstanceId: string;
  businessKey?: string;
  processDefinitionName?: string;
}

export default function MyTasksPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTasks();
  }, []);

  const fetchTasks = async () => {
    try {
      const userId = localStorage.getItem('userId') || '1';
      const response = await api.get(`/workflow/v1/tasks/todo?userId=${userId}`);
      setTasks(response.data);
    } catch (error) {
      console.error('获取任务列表失败', error);
    } finally {
      setLoading(false);
    }
  };

  const completeTask = async (taskId: string) => {
    try {
      await api.post('/workflow/v1/task/complete', {
        taskId,
        variables: {},
        comment: '同意',
        approvalResult: 'approved'
      });
      fetchTasks();
    } catch (error) {
      console.error('完成任务失败', error);
    }
  };

  if (loading) {
    return <div className="p-6">加载中...</div>;
  }

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">我的待办</h1>
        <p className="text-gray-500">共 {tasks.length} 条待办任务</p>
      </div>

      <div className="bg-white rounded-lg shadow">
        <table className="min-w-full">
          <thead>
            <tr className="border-b">
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">任务名称</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">流程名称</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">业务编号</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">创建时间</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {tasks.map((task) => (
              <tr key={task.taskId} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">{task.taskName}</td>
                <td className="px-6 py-4 whitespace-nowrap">{task.processDefinitionName}</td>
                <td className="px-6 py-4 whitespace-nowrap">{task.businessKey || '-'}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {new Date(task.createTime).toLocaleString('zh-CN')}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right">
                  <button
                    onClick={() => completeTask(task.taskId)}
                    className="text-blue-600 hover:text-blue-800 mr-4"
                  >
                    处理
                  </button>
                </td>
              </tr>
            ))}
            {tasks.length === 0 && (
              <tr>
                <td colSpan={5} className="px-6 py-4 text-center text-gray-500">
                  暂无待办任务
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
