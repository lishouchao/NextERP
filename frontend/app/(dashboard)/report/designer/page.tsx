'use client';

import { useState } from 'react';
import api from '@/lib/api/client';

interface ReportConfig {
  reportCode: string;
  reportName: string;
  reportType: string;
  datasourceType: string;
  datasourceConfig: Record<string, any>;
  reportConfig: Record<string, any>;
  reportGroup: string;
  sortOrder: number;
  remark: string;
}

export default function ReportDesignerPage() {
  const [config, setConfig] = useState<ReportConfig>({
    reportCode: '',
    reportName: '',
    reportType: 'table',
    datasourceType: 'sql',
    datasourceConfig: { sql: '' },
    reportConfig: { columns: [] },
    reportGroup: '',
    sortOrder: 0,
    remark: ''
  });
  const [saving, setSaving] = useState(false);
  const [sqlPreview, setSqlPreview] = useState('');

  const handleSave = async () => {
    if (!config.reportCode || !config.reportName) {
      alert('请填写报表编码和名称');
      return;
    }

    setSaving(true);
    try {
      const tenantId = localStorage.getItem('tenantId') || '1';
      await api.post('/report/v1/management', {
        ...config,
        tenantId: Number(tenantId)
      });
      alert('报表保存成功');
    } catch (error) {
      console.error('保存报表失败', error);
      alert('保存报表失败');
    } finally {
      setSaving(false);
    }
  };

  const addColumn = () => {
    const columns = config.reportConfig.columns || [];
    setConfig({
      ...config,
      reportConfig: {
        ...config.reportConfig,
        columns: [...columns, { field: '', title: '' }]
      }
    });
  };

  const updateColumn = (index: number, field: string, value: string) => {
    const columns = [...(config.reportConfig.columns || [])];
    columns[index] = { ...columns[index], [field]: value };
    setConfig({
      ...config,
      reportConfig: {
        ...config.reportConfig,
        columns
      }
    });
  };

  const removeColumn = (index: number) => {
    const columns = [...(config.reportConfig.columns || [])];
    columns.splice(index, 1);
    setConfig({
      ...config,
      reportConfig: {
        ...config.reportConfig,
        columns
      }
    });
  };

  const testSql = async () => {
    if (!config.datasourceConfig.sql) {
      alert('请输入SQL语句');
      return;
    }

    try {
      const tenantId = localStorage.getItem('tenantId') || '1';
      const response = await api.post(`/report/v1/management/test`, {
        sql: config.datasourceConfig.sql,
        tenantId: Number(tenantId)
      });
      setSqlPreview(JSON.stringify(response.data, null, 2));
      alert('SQL测试成功');
    } catch (error) {
      console.error('SQL测试失败', error);
      alert('SQL测试失败');
    }
  };

  return (
    <div className="p-6">
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">报表设计器</h1>
          <p className="text-gray-500">创建和配置自定义报表</p>
        </div>
        <button
          onClick={handleSave}
          disabled={saving}
          className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400"
        >
          {saving ? '保存中...' : '保存报表'}
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 基本信息 */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">基本信息</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                报表编码 *
              </label>
              <input
                type="text"
                value={config.reportCode}
                onChange={(e) => setConfig({ ...config, reportCode: e.target.value })}
                placeholder="例如: RPT_SALES_001"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                报表名称 *
              </label>
              <input
                type="text"
                value={config.reportName}
                onChange={(e) => setConfig({ ...config, reportName: e.target.value })}
                placeholder="例如: 销售统计报表"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  报表类型
                </label>
                <select
                  value={config.reportType}
                  onChange={(e) => setConfig({ ...config, reportType: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                >
                  <option value="table">表格</option>
                  <option value="chart">图表</option>
                  <option value="pivot">透视表</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  数据源类型
                </label>
                <select
                  value={config.datasourceType}
                  onChange={(e) => setConfig({ ...config, datasourceType: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                >
                  <option value="sql">SQL查询</option>
                  <option value="api">API接口</option>
                </select>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  分组
                </label>
                <input
                  type="text"
                  value={config.reportGroup}
                  onChange={(e) => setConfig({ ...config, reportGroup: e.target.value })}
                  placeholder="例如: 财务报表"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  排序
                </label>
                <input
                  type="number"
                  value={config.sortOrder}
                  onChange={(e) => setConfig({ ...config, sortOrder: Number(e.target.value) })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg"
                />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                备注
              </label>
              <textarea
                value={config.remark}
                onChange={(e) => setConfig({ ...config, remark: e.target.value })}
                rows={2}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg"
              />
            </div>
          </div>
        </div>

        {/* 数据源配置 */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">数据源配置</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                SQL语句
              </label>
              <textarea
                value={config.datasourceConfig.sql}
                onChange={(e) => setConfig({
                  ...config,
                  datasourceConfig: { ...config.datasourceConfig, sql: e.target.value }
                })}
                rows={6}
                placeholder="SELECT * FROM table_name WHERE tenant_id = ${tenantId}"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg font-mono text-sm"
              />
              <p className="text-xs text-gray-500 mt-1">
                {"使用 ${param} 作为参数占位符"}
              </p>
            </div>
            <button
              onClick={testSql}
              className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700"
            >
              测试SQL
            </button>
          </div>
        </div>

        {/* 列配置 */}
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold">列配置</h2>
            <button
              onClick={addColumn}
              className="px-3 py-1 bg-blue-600 text-white rounded text-sm hover:bg-blue-700"
            >
              + 添加列
            </button>
          </div>
          <div className="space-y-3">
            {(config.reportConfig.columns || []).map((column: any, index: number) => (
              <div key={index} className="flex gap-2 items-start">
                <input
                  type="text"
                  value={column.field}
                  onChange={(e) => updateColumn(index, 'field', e.target.value)}
                  placeholder="字段名"
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm"
                />
                <input
                  type="text"
                  value={column.title}
                  onChange={(e) => updateColumn(index, 'title', e.target.value)}
                  placeholder="显示标题"
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm"
                />
                <button
                  onClick={() => removeColumn(index)}
                  className="px-2 py-2 text-red-600 hover:bg-red-50 rounded"
                >
                  ✕
                </button>
              </div>
            ))}
            {(!config.reportConfig.columns || config.reportConfig.columns.length === 0) && (
              <p className="text-gray-500 text-sm">点击&ldquo;添加列&rdquo;配置报表列</p>
            )}
          </div>
        </div>

        {/* SQL预览 */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold mb-4">SQL测试结果</h2>
          <pre className="bg-gray-50 p-4 rounded-lg text-sm overflow-auto max-h-64">
            {sqlPreview || '点击"测试SQL"查看结果'}
          </pre>
        </div>
      </div>
    </div>
  );
}
