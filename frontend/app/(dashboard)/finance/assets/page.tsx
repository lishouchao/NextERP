'use client';

import { useState } from 'react';
import {
  Card,
  Table,
  Form,
  Input,
  Select,
  DatePicker,
  Button,
  Space,
  Row,
  Col,
  Statistic,
  Tag,
  Tabs,
  Modal,
  Progress,
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons';

// 模拟固定资产数据
const mockAssets = [
  { id: 1, assetCode: 'FA-2023-001', assetName: '办公电脑', category: '电子设备', deptName: '技术部', originalValue: 8000, salvageValue: 800, usefulLife: 36, usedMonths: 12, depreciationMethod: '直线法', monthlyDepreciation: 200, accumulatedDepreciation: 2400, netValue: 5600, status: 1 },
  { id: 2, assetCode: 'FA-2023-002', assetName: '生产设备A', category: '机器设备', deptName: '生产部', originalValue: 150000, salvageValue: 15000, usefulLife: 120, usedMonths: 24, depreciationMethod: '直线法', monthlyDepreciation: 1125, accumulatedDepreciation: 27000, netValue: 123000, status: 1 },
  { id: 3, assetCode: 'FA-2023-003', assetName: '办公家具', category: '办公设备', deptName: '行政部', originalValue: 30000, salvageValue: 3000, usefulLife: 60, usedMonths: 36, depreciationMethod: '直线法', monthlyDepreciation: 450, accumulatedDepreciation: 16200, netValue: 13800, status: 1 },
  { id: 4, assetCode: 'FA-2022-001', assetName: '运输车辆', category: '运输设备', deptName: '物流部', originalValue: 200000, salvageValue: 20000, usefulLife: 96, usedMonths: 48, depreciationMethod: '直线法', monthlyDepreciation: 1875, accumulatedDepreciation: 90000, netValue: 110000, status: 1 },
  { id: 5, assetCode: 'FA-2021-001', assetName: '厂房建筑', category: '房屋建筑', deptName: '行政部', originalValue: 2000000, salvageValue: 200000, usefulLife: 360, usedMonths: 72, depreciationMethod: '直线法', monthlyDepreciation: 5000, accumulatedDepreciation: 360000, netValue: 1640000, status: 1 },
];

export default function AssetsPage() {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState(mockAssets);
  const [form] = Form.useForm();
  const [modalVisible, setModalVisible] = useState(false);

  const columns = [
    { title: '资产编码', dataIndex: 'assetCode', key: 'assetCode', width: 120, fixed: 'left' as const },
    { title: '资产名称', dataIndex: 'assetName', key: 'assetName', width: 120 },
    { title: '资产类别', dataIndex: 'category', key: 'category', width: 100 },
    { title: '使用部门', dataIndex: 'deptName', key: 'deptName', width: 100 },
    { title: '原值', dataIndex: 'originalValue', key: 'originalValue', width: 120, align: 'right' as const,
      render: (v: number) => v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 }),
    },
    { title: '残值', dataIndex: 'salvageValue', key: 'salvageValue', width: 100, align: 'right' as const,
      render: (v: number) => v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 }),
    },
    { title: '使用年限(月)', dataIndex: 'usefulLife', key: 'usefulLife', width: 100, align: 'center' as const },
    { title: '已用月份', dataIndex: 'usedMonths', key: 'usedMonths', width: 90, align: 'center' as const },
    { title: '折旧方法', dataIndex: 'depreciationMethod', key: 'depreciationMethod', width: 90 },
    { title: '月折旧额', dataIndex: 'monthlyDepreciation', key: 'monthlyDepreciation', width: 100, align: 'right' as const,
      render: (v: number) => v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 }),
    },
    { title: '累计折旧', dataIndex: 'accumulatedDepreciation', key: 'accumulatedDepreciation', width: 120, align: 'right' as const,
      render: (v: number) => <span style={{ color: '#fa8c16' }}>{v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</span>,
    },
    { title: '净值', dataIndex: 'netValue', key: 'netValue', width: 120, align: 'right' as const,
      render: (v: number) => <strong style={{ color: '#1890ff' }}>{v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</strong>,
    },
    { title: '折旧进度', key: 'progress', width: 120,
      render: (_: unknown, record: any) => {
        const percent = Math.round((record.accumulatedDepreciation / (record.originalValue - record.salvageValue)) * 100);
        return <Progress percent={percent} size="small" />;
      },
    },
    { title: '操作', key: 'action', width: 150, fixed: 'right' as const,
      render: () => (
        <Space>
          <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
          <Button type="link" size="small">折旧</Button>
        </Space>
      ),
    },
  ];

  const totalStats = {
    originalValue: data.reduce((s, d) => s + d.originalValue, 0),
    accumulatedDepreciation: data.reduce((s, d) => s + d.accumulatedDepreciation, 0),
    netValue: data.reduce((s, d) => s + d.netValue, 0),
    count: data.length,
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="固定资产管理 (对标 SAP AS01)"
        extra={
          <Space>
            <Button>计提折旧</Button>
            <Button type="primary" icon={<PlusOutlined />}>新增资产</Button>
          </Space>
        }
      >
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card size="small">
              <Statistic title="资产数量" value={totalStats.count} suffix="项" valueStyle={{ fontSize: 20 }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="资产原值" value={totalStats.originalValue} precision={2} prefix="¥" valueStyle={{ fontSize: 20 }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="累计折旧" value={totalStats.accumulatedDepreciation} precision={2} prefix="¥" valueStyle={{ fontSize: 20, color: '#fa8c16' }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="资产净值" value={totalStats.netValue} precision={2} prefix="¥" valueStyle={{ fontSize: 20, color: '#1890ff' }} />
            </Card>
          </Col>
        </Row>

        <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="assetName" label="资产">
            <Input placeholder="资产名称/编码" style={{ width: 150 }} />
          </Form.Item>
          <Form.Item name="category" label="类别">
            <Select placeholder="全部" allowClear style={{ width: 120 }}
              options={[
                { value: '电子设备', label: '电子设备' },
                { value: '机器设备', label: '机器设备' },
                { value: '办公设备', label: '办公设备' },
                { value: '运输设备', label: '运输设备' },
                { value: '房屋建筑', label: '房屋建筑' },
              ]} />
          </Form.Item>
          <Form.Item name="deptName" label="部门">
            <Input placeholder="使用部门" style={{ width: 120 }} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SearchOutlined />}>查询</Button>
          </Form.Item>
        </Form>

        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          size="small"
          scroll={{ x: 1600 }}
          pagination={{ defaultPageSize: 20, showSizeChanger: true }}
        />
      </Card>
    </div>
  );
}
