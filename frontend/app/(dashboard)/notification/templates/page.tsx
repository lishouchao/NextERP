'use client';

import { useState, useEffect } from 'react';
import api from '@/lib/api/client';

interface Template {
  id: number;
  templateCode: string;
  templateName: string;
  notificationType: string;
  titleTemplate: string;
  contentTemplate: string;
  variables: string;
  status: number;
  remark: string;
}

export default function NotificationTemplatePage() {
  const [templates, setTemplates] = useState<Template[]>([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState<Partial<Template>>({
    notificationType: 'system',
    status: 1
  });

  useEffect(() => {
    fetchTemplates();
  }, []);

  const fetchTemplates = async () => {
    try {
      const tenantId = localStorage.getItem('tenantId') || '1';
      const response = await api.get(`/notification/v1/templates?tenantId=${tenantId}`);
      setTemplates(response.data || []);
    } catch (error) {
      console.error('获取模板列表失败', error);
    } finally {
      setLoading(false);
    }
  };

  const saveTemplate = async () => {
    if (!form.templateCode || !form.templateName) {
      alert('请填写模板编码和名称');
      return;
    }

    try {
      const tenantId = localStorage.getItem('tenantId') || '1';
      if (form.id) {
        await api.put(`/notification/v1/templates/${form.id}`, { ...form, tenantId: Number(tenantId) });
      } else {
        await api.post('/notification/v1/templates', { ...form, tenantId: Number(tenantId) });
      }
      setEditing(false);
      setForm({ notificationType: 'system', status: 1 });
      fetchTemplates();
    } catch (error) {
      console.error('保存模板失败', error);
      alert('保存模板失败');
    }
  };

  const deleteTemplate = async (id: number) => {
    if (!confirm('确定删除此模板？')) return;
    try {
      await api.delete(`/notification/v1/templates/${id}`);
      fetchTemplates();
    } catch (error) {
      console.error('删除模板失败', error);
    }
  };

  const getTypeLabel = (type: string) => {
    const labels: Record<string, string> = { system: '系统', email: '邮件', sms: '短信', push: '推送' };
    return labels[type] || type;
  };

  if (loading) {
    return <div className="p-6">加载中...</div>;
  }

  return (
    <div className="p-6">
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">通知模板管理</h1>
          <p className="text-gray-500">共 {templates.length} 个模板</p>
        </div>
        <button
          onClick={() => { setEditing(true); setForm({ notificationType: 'system', status: 1 }); }}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
        >
          新建模板
        </button>
      </div>

      {/* 编辑表单 */}
      {editing && (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="text-lg font-semibold mb-4">{form.id ? '编辑模板' : '新建模板'}</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">模板编码 *</label>
              <input
                type="text"
                value={form.templateCode || ''}
                onChange={(e) => setForm({ ...form, templateCode: e.target.value })}
                placeholder="例如: ORDER_APPROVED"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">模板名称 *</label>
              <input
                type="text"
                value={form.templateName || ''}
                onChange={(e) => setForm({ ...form, templateName: e.target.value })}
                placeholder="例如: 订单审批通知"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">通知类型</label>
              <select
                value={form.notificationType || 'system'}
                onChange={(e) => setForm({ ...form, notificationType: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              >
                <option value="system">系统通知</option>
                <option value="email">邮件</option>
                <option value="sms">短信</option>
                <option value="push">推送</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">状态</label>
              <select
                value={form.status || 1}
                onChange={(e) => setForm({ ...form, status: Number(e.target.value) })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              >
                <option value={1}>启用</option>
                <option value={0}>禁用</option>
              </select>
            </div>
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">标题模板</label>
              <input
                type="text"
                value={form.titleTemplate || ''}
                onChange={(e) => setForm({ ...form, titleTemplate: e.target.value })}
                placeholder="例如: ${processName}审批通知"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              />
              <p className="text-xs text-gray-500 mt-1">{"使用 ${variableName} 作为变量占位符"}</p>
            </div>
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">内容模板</label>
              <textarea
                value={form.contentTemplate || ''}
                onChange={(e) => setForm({ ...form, contentTemplate: e.target.value })}
                rows={4}
                placeholder="例如: ${initiator}提交的${processName}需要您审批，请及时处理。"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              />
            </div>
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">备注</label>
              <input
                type="text"
                value={form.remark || ''}
                onChange={(e) => setForm({ ...form, remark: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              />
            </div>
          </div>
          <div className="flex gap-2 mt-4">
            <button
              onClick={saveTemplate}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              保存
            </button>
            <button
              onClick={() => { setEditing(false); setForm({ notificationType: 'system', status: 1 }); }}
              className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
            >
              取消
            </button>
          </div>
        </div>
      )}

      {/* 模板列表 */}
      <div className="bg-white rounded-lg shadow">
        <table className="min-w-full">
          <thead>
            <tr className="border-b">
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">模板编码</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">模板名称</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">类型</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">状态</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">操作</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {templates.map((template) => (
              <tr key={template.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap font-mono text-sm">{template.templateCode}</td>
                <td className="px-6 py-4 whitespace-nowrap">{template.templateName}</td>
                <td className="px-6 py-4 whitespace-nowrap">{getTypeLabel(template.notificationType)}</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {template.status === 1 ? (
                    <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-800">启用</span>
                  ) : (
                    <span className="px-2 py-1 text-xs rounded-full bg-red-100 text-red-800">禁用</span>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right">
                  <button
                    onClick={() => { setForm(template); setEditing(true); }}
                    className="text-blue-600 hover:text-blue-800 mr-4"
                  >
                    编辑
                  </button>
                  <button
                    onClick={() => deleteTemplate(template.id)}
                    className="text-red-600 hover:text-red-800"
                  >
                    删除
                  </button>
                </td>
              </tr>
            ))}
            {templates.length === 0 && (
              <tr>
                <td colSpan={5} className="px-6 py-4 text-center text-gray-500">暂无模板</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
