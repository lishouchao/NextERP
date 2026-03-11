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
  Modal,
  Tabs,
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  DollarOutlined,
} from '@ant-design/icons';

const { RangePicker } = DatePicker;

// 模拟应收账款数据
const mockReceivables = [
  { id: 1, customerCode: 'C001', customerName: '北京科技有限公司', invoiceNo: 'INV-2023-001', invoiceDate: '2023-12-01', amount: 50000, paidAmount: 20000, balance: 30000, dueDate: '2024-01-01', status: 1, overdueDays: 0 },
  { id: 2, customerCode: 'C002', customerName: '上海贸易公司', invoiceNo: 'INV-2023-002', invoiceDate: '2023-12-05', amount: 80000, paidAmount: 0, balance: 80000, dueDate: '2024-01-05', status: 1, overdueDays: 0 },
  { id: 3, customerCode: 'C003', customerName: '广州制造厂', invoiceNo: 'INV-2023-003', invoiceDate: '2023-11-15', amount: 120000, paidAmount: 120000, balance: 0, dueDate: '2023-12-15', status: 2, overdueDays: 0 },
  { id: 4, customerCode: 'C001', customerName: '北京科技有限公司', invoiceNo: 'INV-2023-004', invoiceDate: '2023-10-20', amount: 35000, paidAmount: 0, balance: 35000, dueDate: '2023-11-20', status: 3, overdueDays: 25 },
  { id: 5, customerCode: 'C004', customerName: '深圳电子公司', invoiceNo: 'INV-2023-005', invoiceDate: '2023-12-10', amount: 95000, paidAmount: 45000, balance: 50000, dueDate: '2024-01-10', status: 1, overdueDays: 0 },
];

export default function ReceivablesPage() {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState(mockReceivables);
  const [form] = Form.useForm();
  const [activeTab, setActiveTab] = useState('all');

  const filteredData = activeTab === 'all' ? data :
    activeTab === 'overdue' ? data.filter(d => d.status === 3) :
    activeTab === 'paid' ? data.filter(d => d.status === 2) :
    data.filter(d => d.status === 1);

  const columns = [
    { title: '客户编码', dataIndex: 'customerCode', key: 'customerCode', width: 100 },
    { title: '客户名称', dataIndex: 'customerName', key: 'customerName', width: 150 },
    { title: '发票号', dataIndex: 'invoiceNo', key: 'invoiceNo', width: 130 },
    { title: '发票日期', dataIndex: 'invoiceDate', key: 'invoiceDate', width: 110 },
    { title: '应收金额', dataIndex: 'amount', key: 'amount', width: 120, align: 'right' as const,
      render: (v: number) => v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 }),
    },
    { title: '已收金额', dataIndex: 'paidAmount', key: 'paidAmount', width: 120, align: 'right' as const,
      render: (v: number) => <span style={{ color: '#52c41a' }}>{v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</span>,
    },
    { title: '未收金额', dataIndex: 'balance', key: 'balance', width: 120, align: 'right' as const,
      render: (v: number) => <strong style={{ color: '#ff4d4f' }}>{v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</strong>,
    },
    { title: '到期日', dataIndex: 'dueDate', key: 'dueDate', width: 110 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80,
      render: (status: number) => {
        const map: Record<number, { color: string; text: string }> = {
          1: { color: 'blue', text: '未结清' },
          2: { color: 'green', text: '已结清' },
          3: { color: 'red', text: '已逾期' },
        };
        const s = map[status] || { color: 'default', text: '未知' };
        return <Tag color={s.color}>{s.text}</Tag>;
      },
    },
    { title: '逾期天数', dataIndex: 'overdueDays', key: 'overdueDays', width: 80, align: 'center' as const,
      render: (days: number) => days > 0 ? <Tag color="red">{days}天</Tag> : '-',
    },
    { title: '操作', key: 'action', width: 150, fixed: 'right' as const,
      render: () => (
        <Space>
          <Button type="link" size="small" icon={<DollarOutlined />}>收款</Button>
          <Button type="link" size="small">详情</Button>
        </Space>
      ),
    },
  ];

  const totalStats = {
    total: data.reduce((s, d) => s + d.amount, 0),
    paid: data.reduce((s, d) => s + d.paidAmount, 0),
    balance: data.reduce((s, d) => s + d.balance, 0),
    overdue: data.filter(d => d.status === 3).reduce((s, d) => s + d.balance, 0),
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="应收账款管理 (对标 SAP FBL5N)"
        extra={<Button type="primary" icon={<PlusOutlined />}>新建收款单</Button>}
      >
        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card size="small">
              <Statistic title="应收总额" value={totalStats.total} precision={2} prefix="¥" valueStyle={{ fontSize: 20 }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="已收金额" value={totalStats.paid} precision={2} prefix="¥" valueStyle={{ fontSize: 20, color: '#52c41a' }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="未收金额" value={totalStats.balance} precision={2} prefix="¥" valueStyle={{ fontSize: 20, color: '#ff4d4f' }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="逾期金额" value={totalStats.overdue} precision={2} prefix="¥" valueStyle={{ fontSize: 20, color: '#fa8c16' }} />
            </Card>
          </Col>
        </Row>

        {/* 搜索 */}
        <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="customerName" label="客户">
            <Input placeholder="客户名称" style={{ width: 150 }} />
          </Form.Item>
          <Form.Item name="dateRange" label="日期">
            <RangePicker />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select placeholder="全部" allowClear style={{ width: 100 }}
              options={[
                { value: 1, label: '未结清' },
                { value: 2, label: '已结清' },
                { value: 3, label: '已逾期' },
              ]} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SearchOutlined />}>查询</Button>
          </Form.Item>
        </Form>

        {/* 标签页 */}
        <Tabs activeKey={activeTab} onChange={setActiveTab} items={[
          { key: 'all', label: `全部 (${data.length})` },
          { key: 'unpaid', label: `未结清 (${data.filter(d => d.status === 1).length})` },
          { key: 'overdue', label: `已逾期 (${data.filter(d => d.status === 3).length})` },
          { key: 'paid', label: `已结清 (${data.filter(d => d.status === 2).length})` },
        ]} />

        <Table
          columns={columns}
          dataSource={filteredData}
          rowKey="id"
          loading={loading}
          size="small"
          scroll={{ x: 1300 }}
          pagination={{ defaultPageSize: 20, showSizeChanger: true }}
        />
      </Card>
    </div>
  );
}
