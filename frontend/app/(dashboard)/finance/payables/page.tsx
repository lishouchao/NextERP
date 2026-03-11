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
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  DollarOutlined,
} from '@ant-design/icons';

const { RangePicker } = DatePicker;

// 模拟应付账款数据
const mockPayables = [
  { id: 1, supplierCode: 'S001', supplierName: '原材料供应商A', invoiceNo: 'PUR-2023-001', invoiceDate: '2023-12-01', amount: 80000, paidAmount: 30000, balance: 50000, dueDate: '2024-01-01', status: 1 },
  { id: 2, supplierCode: 'S002', supplierName: '设备供应商B', invoiceNo: 'PUR-2023-002', invoiceDate: '2023-12-05', amount: 150000, paidAmount: 0, balance: 150000, dueDate: '2024-01-05', status: 1 },
  { id: 3, supplierCode: 'S003', supplierName: '包装材料公司', invoiceNo: 'PUR-2023-003', invoiceDate: '2023-11-15', amount: 25000, paidAmount: 25000, balance: 0, dueDate: '2023-12-15', status: 2 },
  { id: 4, supplierCode: 'S001', supplierName: '原材料供应商A', invoiceNo: 'PUR-2023-004', invoiceDate: '2023-12-10', amount: 60000, paidAmount: 20000, balance: 40000, dueDate: '2024-01-10', status: 1 },
];

export default function PayablesPage() {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState(mockPayables);
  const [form] = Form.useForm();
  const [activeTab, setActiveTab] = useState('all');

  const filteredData = activeTab === 'all' ? data :
    activeTab === 'paid' ? data.filter(d => d.status === 2) :
    data.filter(d => d.status === 1);

  const columns = [
    { title: '供应商编码', dataIndex: 'supplierCode', key: 'supplierCode', width: 100 },
    { title: '供应商名称', dataIndex: 'supplierName', key: 'supplierName', width: 150 },
    { title: '发票号', dataIndex: 'invoiceNo', key: 'invoiceNo', width: 130 },
    { title: '发票日期', dataIndex: 'invoiceDate', key: 'invoiceDate', width: 110 },
    { title: '应付金额', dataIndex: 'amount', key: 'amount', width: 120, align: 'right' as const,
      render: (v: number) => v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 }),
    },
    { title: '已付金额', dataIndex: 'paidAmount', key: 'paidAmount', width: 120, align: 'right' as const,
      render: (v: number) => <span style={{ color: '#52c41a' }}>{v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</span>,
    },
    { title: '未付金额', dataIndex: 'balance', key: 'balance', width: 120, align: 'right' as const,
      render: (v: number) => <strong style={{ color: '#ff4d4f' }}>{v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</strong>,
    },
    { title: '到期日', dataIndex: 'dueDate', key: 'dueDate', width: 110 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80,
      render: (status: number) => {
        const map: Record<number, { color: string; text: string }> = {
          1: { color: 'blue', text: '未结清' },
          2: { color: 'green', text: '已结清' },
        };
        const s = map[status] || { color: 'default', text: '未知' };
        return <Tag color={s.color}>{s.text}</Tag>;
      },
    },
    { title: '操作', key: 'action', width: 150, fixed: 'right' as const,
      render: () => (
        <Space>
          <Button type="link" size="small" icon={<DollarOutlined />}>付款</Button>
          <Button type="link" size="small">详情</Button>
        </Space>
      ),
    },
  ];

  const totalStats = {
    total: data.reduce((s, d) => s + d.amount, 0),
    paid: data.reduce((s, d) => s + d.paidAmount, 0),
    balance: data.reduce((s, d) => s + d.balance, 0),
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="应付账款管理 (对标 SAP FBL1N)"
        extra={<Button type="primary" icon={<PlusOutlined />}>新建付款单</Button>}
      >
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={8}>
            <Card size="small">
              <Statistic title="应付总额" value={totalStats.total} precision={2} prefix="¥" valueStyle={{ fontSize: 20 }} />
            </Card>
          </Col>
          <Col span={8}>
            <Card size="small">
              <Statistic title="已付金额" value={totalStats.paid} precision={2} prefix="¥" valueStyle={{ fontSize: 20, color: '#52c41a' }} />
            </Card>
          </Col>
          <Col span={8}>
            <Card size="small">
              <Statistic title="未付金额" value={totalStats.balance} precision={2} prefix="¥" valueStyle={{ fontSize: 20, color: '#ff4d4f' }} />
            </Card>
          </Col>
        </Row>

        <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="supplierName" label="供应商">
            <Input placeholder="供应商名称" style={{ width: 150 }} />
          </Form.Item>
          <Form.Item name="dateRange" label="日期">
            <RangePicker />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SearchOutlined />}>查询</Button>
          </Form.Item>
        </Form>

        <Tabs activeKey={activeTab} onChange={setActiveTab} items={[
          { key: 'all', label: `全部 (${data.length})` },
          { key: 'unpaid', label: `未结清 (${data.filter(d => d.status === 1).length})` },
          { key: 'paid', label: `已结清 (${data.filter(d => d.status === 2).length})` },
        ]} />

        <Table
          columns={columns}
          dataSource={filteredData}
          rowKey="id"
          loading={loading}
          size="small"
          scroll={{ x: 1200 }}
          pagination={{ defaultPageSize: 20, showSizeChanger: true }}
        />
      </Card>
    </div>
  );
}
