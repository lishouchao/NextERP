'use client';

import { useState } from 'react';
import {
  Card,
  Table,
  Form,
  Input,
  Select,
  Button,
  Space,
  Row,
  Col,
  Statistic,
  Tag,
  Modal,
  InputNumber,
  DatePicker,
  Tabs,
  Badge,
  Steps,
  Descriptions,
  message,
  Tooltip,
  Divider,
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  CheckOutlined,
  DollarOutlined,
  FileTextOutlined,
  AccountBookOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;

// 开票类型配置
const billingTypeConfig: Record<string, { text: string; color: string }> = {
  F1: { text: '发票', color: 'blue' },
  F2: { text: '贷项凭证', color: 'orange' },
  G2: { text: '贷记备忘', color: 'purple' },
  S1: { text: '取消发票', color: 'red' },
};

// 开票状态配置
const billingStatusConfig: Record<string, { text: string; color: string; step: number }> = {
  '01': { text: '已创建', color: 'default', step: 0 },
  '02': { text: '已过账', color: 'green', step: 1 },
  '03': { text: '已取消', color: 'red', step: -1 },
};

// 模拟开票数据
const mockBillings = [
  {
    id: 1,
    billingNumber: 'BILL-2024-001',
    billingType: 'F1',
    deliveryNumber: 'DN-2024-001',
    salesOrder: 'SO-2023-001',
    customerCode: 'CUST-001',
    customerName: '北京科技有限公司',
    billingDate: '2024-01-10',
    netValue: 125000.00,
    taxAmount: 16250.00,
    grossValue: 141250.00,
    currency: 'CNY',
    billingStatus: '02',
    accountingDoc: 'AC-2024-00105',
    paymentTerms: 'ZT01 - 30天净额',
    items: [
      { materialCode: 'MAT-001', materialName: '精密轴承 A-100', quantity: 100, unit: 'PCS', unitPrice: 450.00, netAmount: 45000.00, taxRate: 13, taxAmount: 5850.00 },
      { materialCode: 'MAT-002', materialName: '密封组件 S-200', quantity: 200, unit: 'PCS', unitPrice: 125.00, netAmount: 25000.00, taxRate: 13, taxAmount: 3250.00 },
      { materialCode: 'MAT-003', materialName: '配件C', quantity: 500, unit: 'SET', unitPrice: 110.00, netAmount: 55000.00, taxRate: 13, taxAmount: 7150.00 },
    ],
    createdBy: '财务A',
    createdAt: '2024-01-10 14:30:00',
    postedBy: '财务主管',
    postedAt: '2024-01-11 09:00:00',
  },
  {
    id: 2,
    billingNumber: 'BILL-2024-002',
    billingType: 'F1',
    deliveryNumber: 'DN-2024-002',
    salesOrder: 'SO-2023-002',
    customerCode: 'CUST-002',
    customerName: '上海贸易集团',
    billingDate: '2024-01-15',
    netValue: 256000.00,
    taxAmount: 33280.00,
    grossValue: 289280.00,
    currency: 'CNY',
    billingStatus: '01',
    accountingDoc: null,
    paymentTerms: 'ZT02 - 60天净额',
    items: [
      { materialCode: 'MAT-001', materialName: '精密轴承 A-100', quantity: 150, unit: 'PCS', unitPrice: 480.00, netAmount: 72000.00, taxRate: 13, taxAmount: 9360.00 },
      { materialCode: 'MAT-004', materialName: '工业电机 M-500', quantity: 50, unit: 'SET', unitPrice: 3200.00, netAmount: 160000.00, taxRate: 13, taxAmount: 20800.00 },
      { materialCode: 'MAT-005', materialName: '服务费', quantity: 1, unit: 'ITEM', unitPrice: 24000.00, netAmount: 24000.00, taxRate: 13, taxAmount: 3120.00 },
    ],
    createdBy: '财务A',
    createdAt: '2024-01-15 10:20:00',
    postedBy: null,
    postedAt: null,
  },
  {
    id: 3,
    billingNumber: 'BILL-2024-003',
    billingType: 'G2',
    deliveryNumber: 'DN-2024-003',
    salesOrder: 'SO-2023-006',
    customerCode: 'CUST-005',
    customerName: '杭州网络科技',
    billingDate: '2024-01-18',
    netValue: 18600.00,
    taxAmount: 2418.00,
    grossValue: 21018.00,
    currency: 'CNY',
    billingStatus: '02',
    accountingDoc: 'AC-2024-00178',
    paymentTerms: 'ZT01 - 30天净额',
    items: [
      { materialCode: 'MAT-003', materialName: '传感器模组 T-150', quantity: 20, unit: 'PCS', unitPrice: 930.00, netAmount: 18600.00, taxRate: 13, taxAmount: 2418.00 },
    ],
    createdBy: '财务B',
    createdAt: '2024-01-18 11:00:00',
    postedBy: '财务主管',
    postedAt: '2024-01-18 15:00:00',
  },
  {
    id: 4,
    billingNumber: 'BILL-2024-004',
    billingType: 'F1',
    deliveryNumber: 'DN-2024-004',
    salesOrder: 'SO-2023-003',
    customerCode: 'CUST-003',
    customerName: '广州制造企业',
    billingDate: '2024-01-20',
    netValue: 88000.00,
    taxAmount: 11440.00,
    grossValue: 99440.00,
    currency: 'CNY',
    billingStatus: '01',
    accountingDoc: null,
    paymentTerms: 'ZT03 - 45天净额',
    items: [
      { materialCode: 'MAT-006', materialName: '液压阀门 H-800', quantity: 30, unit: 'PCS', unitPrice: 1800.00, netAmount: 54000.00, taxRate: 13, taxAmount: 7020.00 },
      { materialCode: 'MAT-007', materialName: '连接法兰 F-400', quantity: 60, unit: 'PCS', unitPrice: 566.67, netAmount: 34000.00, taxRate: 13, taxAmount: 4420.00 },
    ],
    createdBy: '财务A',
    createdAt: '2024-01-20 09:15:00',
    postedBy: null,
    postedAt: null,
  },
  {
    id: 5,
    billingNumber: 'BILL-2024-005',
    billingType: 'F2',
    deliveryNumber: null,
    salesOrder: 'SO-2023-005',
    customerCode: 'CUST-004',
    customerName: '深圳电子公司',
    billingDate: '2024-01-22',
    netValue: 15200.00,
    taxAmount: 1976.00,
    grossValue: 17176.00,
    currency: 'CNY',
    billingStatus: '03',
    accountingDoc: null,
    paymentTerms: 'ZT02 - 60天净额',
    items: [
      { materialCode: 'MAT-008', materialName: '电源模块 PM-600', quantity: 40, unit: 'PCS', unitPrice: 380.00, netAmount: 15200.00, taxRate: 13, taxAmount: 1976.00 },
    ],
    createdBy: '财务B',
    createdAt: '2024-01-22 13:45:00',
    postedBy: null,
    postedAt: null,
    cancelReason: '客户退货，取消开票',
  },
  {
    id: 6,
    billingNumber: 'BILL-2024-006',
    billingType: 'F1',
    deliveryNumber: 'DN-2024-005',
    salesOrder: null,
    customerCode: 'CUST-004',
    customerName: '深圳电子公司',
    billingDate: '2024-01-25',
    netValue: 380000.00,
    taxAmount: 49400.00,
    grossValue: 429400.00,
    currency: 'CNY',
    billingStatus: '02',
    accountingDoc: 'AC-2024-00250',
    paymentTerms: 'ZT02 - 60天净额',
    items: [
      { materialCode: 'MAT-008', materialName: '电源模块 PM-600', quantity: 200, unit: 'PCS', unitPrice: 1900.00, netAmount: 380000.00, taxRate: 13, taxAmount: 49400.00 },
    ],
    createdBy: '财务A',
    createdAt: '2024-01-25 16:00:00',
    postedBy: '财务主管',
    postedAt: '2024-01-26 08:30:00',
  },
];

// 可选交货单
const mockDeliveries = [
  { deliveryNumber: 'DN-2024-001', customerName: '北京科技有限公司', status: '已完成' },
  { deliveryNumber: 'DN-2024-002', customerName: '上海贸易集团', status: '拣配中' },
  { deliveryNumber: 'DN-2024-004', customerName: '广州制造企业', status: '已创建' },
  { deliveryNumber: 'DN-2024-005', customerName: '深圳电子公司', status: '已完成' },
  { deliveryNumber: 'DN-2024-006', customerName: '北京科技有限公司', status: '拣配中' },
];

export default function BillingPage() {
  const [loading, setLoading] = useState(false);
  const [billings, setBillings] = useState(mockBillings);
  const [activeTab, setActiveTab] = useState('all');
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedBilling, setSelectedBilling] = useState<typeof mockBillings[0] | null>(null);
  const [form] = Form.useForm();

  // 按状态筛选
  const filteredBillings = activeTab === 'all' ? billings : billings.filter(b => {
    if (activeTab === 'created') return b.billingStatus === '01';
    if (activeTab === 'posted') return b.billingStatus === '02';
    if (activeTab === 'cancelled') return b.billingStatus === '03';
    return true;
  });

  // 统计
  const stats = {
    total: billings.length,
    totalNetValue: billings.reduce((s, b) => s + b.netValue, 0),
    postedAmount: billings.filter(b => b.billingStatus === '02').reduce((s, b) => s + b.netValue, 0),
    pendingCount: billings.filter(b => b.billingStatus === '01').length,
    totalTax: billings.reduce((s, b) => s + b.taxAmount, 0),
    totalGross: billings.reduce((s, b) => s + b.grossValue, 0),
  };

  // 开票列表列
  const columns = [
    {
      title: '开票编号',
      dataIndex: 'billingNumber',
      key: 'billingNumber',
      width: 140,
      fixed: 'left' as const,
      render: (text: string, record: typeof mockBillings[0]) => (
        <a onClick={() => { setSelectedBilling(record); setDetailModalVisible(true); }}>{text}</a>
      ),
    },
    {
      title: '开票类型',
      dataIndex: 'billingType',
      key: 'billingType',
      width: 110,
      render: (type: string) => {
        const config = billingTypeConfig[type];
        return <Tag color={config?.color}>{type} - {config?.text}</Tag>;
      },
    },
    {
      title: '客户名称',
      dataIndex: 'customerName',
      key: 'customerName',
      width: 150,
    },
    {
      title: '开票日期',
      dataIndex: 'billingDate',
      key: 'billingDate',
      width: 110,
    },
    {
      title: '净值',
      dataIndex: 'netValue',
      key: 'netValue',
      width: 120,
      align: 'right' as const,
      render: (v: number) => <span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{v.toLocaleString()}</span>,
    },
    {
      title: '税额',
      dataIndex: 'taxAmount',
      key: 'taxAmount',
      width: 100,
      align: 'right' as const,
      render: (v: number) => `¥${v.toLocaleString()}`,
    },
    {
      title: '含税总额',
      dataIndex: 'grossValue',
      key: 'grossValue',
      width: 130,
      align: 'right' as const,
      render: (v: number) => <span style={{ fontWeight: 'bold' }}>¥{v.toLocaleString()}</span>,
    },
    {
      title: '来源交货单',
      dataIndex: 'deliveryNumber',
      key: 'deliveryNumber',
      width: 130,
      render: (v: string | null) => v || '-',
    },
    {
      title: '开票状态',
      dataIndex: 'billingStatus',
      key: 'billingStatus',
      width: 90,
      render: (status: string) => {
        const config = billingStatusConfig[status];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      fixed: 'right' as const,
      render: (_: unknown, record: typeof mockBillings[0]) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => { setSelectedBilling(record); setDetailModalVisible(true); }}>
            详情
          </Button>
          {record.billingStatus === '01' && (
            <>
              <Tooltip title="过账到财务">
                <Button type="link" size="small" icon={<CheckOutlined />} style={{ color: '#52c41a' }}>
                  过账
                </Button>
              </Tooltip>
              <Tooltip title="取消开票">
                <Button type="link" size="small" danger icon={<CloseCircleOutlined />}>
                  取消
                </Button>
              </Tooltip>
            </>
          )}
          {record.billingStatus === '02' && (
            <Button type="link" size="small" icon={<FileTextOutlined />}>
              凭证
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card
        title="开票管理 (对标 SAP VF01/VF02)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => setLoading(!loading)}>刷新</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalVisible(true)}>
              新建开票
            </Button>
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            { key: 'all', label: <><DollarOutlined /> 全部开票</> },
            { key: 'created', label: <>已创建</> },
            { key: 'posted', label: <>已过账</> },
            { key: 'cancelled', label: <>已取消</> },
          ]}
        />

        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={4}>
            <Card size="small">
              <Statistic title="开票总数" value={stats.total} suffix="单" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={5}>
            <Card size="small">
              <Statistic title="净值总额" value={stats.totalNetValue} prefix="¥" valueStyle={{ fontSize: 18, color: '#1890ff' }} />
            </Card>
          </Col>
          <Col span={5}>
            <Card size="small">
              <Statistic title="已过账金额" value={stats.postedAmount} prefix="¥" valueStyle={{ fontSize: 18, color: '#52c41a' }} />
            </Card>
          </Col>
          <Col span={5}>
            <Card size="small">
              <Statistic title="税额合计" value={stats.totalTax} prefix="¥" valueStyle={{ fontSize: 18, color: '#faad14' }} />
            </Card>
          </Col>
          <Col span={5}>
            <Card size="small">
              <Statistic title="待过账" value={stats.pendingCount} suffix="单" valueStyle={{ fontSize: 18, color: '#ff4d4f' }} />
            </Card>
          </Col>
        </Row>

        {/* 搜索表单 */}
        <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="billingNumber" label="开票编号">
            <Input placeholder="开票编号" style={{ width: 140 }} />
          </Form.Item>
          <Form.Item name="customerName" label="客户">
            <Input placeholder="客户名称" style={{ width: 130 }} />
          </Form.Item>
          <Form.Item name="billingType" label="开票类型">
            <Select placeholder="全部" allowClear style={{ width: 140 }}
              options={Object.entries(billingTypeConfig).map(([k, v]) => ({ value: k, label: `${k} - ${v.text}` }))} />
          </Form.Item>
          <Form.Item name="dateRange" label="日期">
            <RangePicker style={{ width: 240 }} />
          </Form.Item>
          <Form.Item name="billingStatus" label="状态">
            <Select placeholder="全部" allowClear style={{ width: 100 }}
              options={Object.entries(billingStatusConfig).map(([k, v]) => ({ value: k, label: v.text }))} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SearchOutlined />}>查询</Button>
          </Form.Item>
        </Form>

        {/* 开票表格 */}
        <Table
          columns={columns}
          dataSource={filteredBillings}
          rowKey="id"
          loading={loading}
          size="small"
          scroll={{ x: 1400 }}
          pagination={{ defaultPageSize: 20, showSizeChanger: true }}
        />
      </Card>

      {/* 新建开票弹窗 */}
      <Modal
        title="新建开票"
        open={createModalVisible}
        onCancel={() => setCreateModalVisible(false)}
        onOk={() => { message.success('开票创建成功'); setCreateModalVisible(false); }}
        width={800}
      >
        <Form layout="vertical">
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="开票类型" required>
                <Select placeholder="选择开票类型"
                  options={Object.entries(billingTypeConfig).map(([k, v]) => ({ value: k, label: `${k} - ${v.text}` }))} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="来源交货单">
                <Select placeholder="选择交货单（可选）" showSearch optionFilterProp="label"
                  options={mockDeliveries.map(d => ({ value: d.deliveryNumber, label: `${d.deliveryNumber} - ${d.customerName}` }))} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="开票日期" required>
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="客户">
                <Input placeholder="客户自动带出" disabled />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="付款条件">
                <Select placeholder="选择付款条件" options={[
                  { value: 'ZT01', label: 'ZT01 - 30天净额' },
                  { value: 'ZT02', label: 'ZT02 - 60天净额' },
                  { value: 'ZT03', label: 'ZT03 - 45天净额' },
                  { value: 'ZT04', label: 'ZT04 - 现金付款' },
                ]} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="币种">
                <Select placeholder="选择币种" defaultValue="CNY" options={[
                  { value: 'CNY', label: 'CNY - 人民币' },
                  { value: 'USD', label: 'USD - 美元' },
                  { value: 'EUR', label: 'EUR - 欧元' },
                ]} />
              </Form.Item>
            </Col>
          </Row>
          <Card title="开票行项目" size="small">
            <Table
              columns={[
                { title: '物料编码', dataIndex: 'materialCode', width: 110 },
                { title: '物料描述', dataIndex: 'materialName', width: 130 },
                { title: '数量', dataIndex: 'quantity', width: 80 },
                { title: '单位', dataIndex: 'unit', width: 60 },
                { title: '单价', dataIndex: 'unitPrice', width: 100, align: 'right' as const, render: (v: number) => `¥${v.toFixed(2)}` },
                { title: '净值', dataIndex: 'netAmount', width: 110, align: 'right' as const, render: (v: number) => `¥${v.toLocaleString()}` },
                { title: '税率', dataIndex: 'taxRate', width: 70, render: (v: number) => `${v}%` },
                { title: '操作', width: 60, render: () => <Button type="link" size="small" danger>删除</Button> },
              ]}
              dataSource={[]}
              size="small"
              pagination={false}
              locale={{ emptyText: '请先选择交货单或手动添加项目' }}
            />
            <Button type="dashed" block style={{ marginTop: 16 }} icon={<PlusOutlined />}>
              添加行项目
            </Button>
          </Card>
        </Form>
      </Modal>

      {/* 开票详情弹窗 */}
      <Modal
        title={`开票详情 - ${selectedBilling?.billingNumber}`}
        open={detailModalVisible}
        onCancel={() => { setDetailModalVisible(false); setSelectedBilling(null); }}
        footer={[
          <Button key="print">打印发票</Button>,
          <Button key="export" icon={<FileTextOutlined />}>导出</Button>,
          <Button key="close" type="primary" onClick={() => { setDetailModalVisible(false); setSelectedBilling(null); }}>关闭</Button>,
        ]}
        width={1000}
      >
        {selectedBilling && (
          <>
            {/* 流程步骤 */}
            <Card size="small" style={{ marginBottom: 16 }}>
              <Steps
                size="small"
                current={billingStatusConfig[selectedBilling.billingStatus]?.step === -1 ? 0 : (billingStatusConfig[selectedBilling.billingStatus]?.step || 0)}
                status={selectedBilling.billingStatus === '03' ? 'error' : 'process'}
                items={[
                  { title: '创建', description: selectedBilling.createdAt },
                  { title: '过账', status: selectedBilling.billingStatus === '02' ? 'finish' : selectedBilling.billingStatus === '03' ? 'error' : 'wait',
                    description: selectedBilling.postedAt || '-' },
                ]}
              />
            </Card>

            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={16}>
                <Descriptions bordered size="small" column={2}>
                  <Descriptions.Item label="开票编号">{selectedBilling.billingNumber}</Descriptions.Item>
                  <Descriptions.Item label="开票类型">
                    <Tag color={billingTypeConfig[selectedBilling.billingType]?.color}>
                      {selectedBilling.billingType} - {billingTypeConfig[selectedBilling.billingType]?.text}
                    </Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="客户">{selectedBilling.customerName}</Descriptions.Item>
                  <Descriptions.Item label="开票日期">{selectedBilling.billingDate}</Descriptions.Item>
                  <Descriptions.Item label="来源交货">{selectedBilling.deliveryNumber || '-'}</Descriptions.Item>
                  <Descriptions.Item label="来源订单">{selectedBilling.salesOrder || '-'}</Descriptions.Item>
                  <Descriptions.Item label="付款条件">{selectedBilling.paymentTerms}</Descriptions.Item>
                  <Descriptions.Item label="币种">{selectedBilling.currency}</Descriptions.Item>
                  <Descriptions.Item label="开票状态">
                    <Tag color={billingStatusConfig[selectedBilling.billingStatus]?.color}>
                      {billingStatusConfig[selectedBilling.billingStatus]?.text}
                    </Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="创建人">{selectedBilling.createdBy}</Descriptions.Item>
                </Descriptions>
              </Col>
              <Col span={8}>
                <Card size="small" style={{ background: '#f6f8fa' }}>
                  <Statistic title="净值" value={selectedBilling.netValue} prefix="¥" valueStyle={{ color: '#1890ff' }} />
                  <Divider style={{ margin: '12px 0' }} />
                  <Statistic title="税额" value={selectedBilling.taxAmount} prefix="¥" valueStyle={{ fontSize: 16, color: '#faad14' }} />
                  <Divider style={{ margin: '12px 0' }} />
                  <Statistic title="含税总额" value={selectedBilling.grossValue} prefix="¥" valueStyle={{ fontSize: 20, color: '#52c41a' }} />
                </Card>
              </Col>
            </Row>

            {/* 会计信息 */}
            {selectedBilling.billingStatus === '02' && (
              <Card title="会计信息" size="small" style={{ marginBottom: 16 }}>
                <Descriptions bordered size="small" column={3}>
                  <Descriptions.Item label="会计凭证">{selectedBilling.accountingDoc}</Descriptions.Item>
                  <Descriptions.Item label="过账人">{selectedBilling.postedBy}</Descriptions.Item>
                  <Descriptions.Item label="过账时间">{selectedBilling.postedAt}</Descriptions.Item>
                  <Descriptions.Item label="借方科目">1122 - 应收账款</Descriptions.Item>
                  <Descriptions.Item label="贷方科目">6001 - 主营业务收入</Descriptions.Item>
                  <Descriptions.Item label="税金科目">2221 - 应交税费-应交增值税</Descriptions.Item>
                </Descriptions>
              </Card>
            )}

            {selectedBilling.billingStatus === '03' && (
              <Card title="取消信息" size="small" style={{ marginBottom: 16 }}>
                <Descriptions bordered size="small" column={1}>
                  <Descriptions.Item label="取消原因">{(selectedBilling as typeof selectedBilling & { cancelReason?: string }).cancelReason || '-'}</Descriptions.Item>
                </Descriptions>
              </Card>
            )}

            {/* 开票明细 */}
            <Card title="开票行项目" size="small">
              <Table
                columns={[
                  { title: '物料编码', dataIndex: 'materialCode', width: 110 },
                  { title: '物料描述', dataIndex: 'materialName', width: 140 },
                  { title: '数量', dataIndex: 'quantity', width: 80, align: 'right' as const,
                    render: (v: number, r: typeof selectedBilling.items[0]) => `${v} ${r.unit}`,
                  },
                  { title: '单价', dataIndex: 'unitPrice', width: 100, align: 'right' as const,
                    render: (v: number) => `¥${v.toFixed(2)}`,
                  },
                  { title: '净值', dataIndex: 'netAmount', width: 110, align: 'right' as const,
                    render: (v: number) => <span style={{ color: '#1890ff' }}>¥{v.toLocaleString()}</span>,
                  },
                  { title: '税率', dataIndex: 'taxRate', width: 70, align: 'center' as const,
                    render: (v: number) => `${v}%`,
                  },
                  { title: '税额', dataIndex: 'taxAmount', width: 100, align: 'right' as const,
                    render: (v: number) => `¥${v.toLocaleString()}`,
                  },
                ]}
                dataSource={selectedBilling.items}
                rowKey="materialCode"
                size="small"
                pagination={false}
                summary={(data) => {
                  const totalNet = data.reduce((sum, r) => sum + r.netAmount, 0);
                  const totalTax = data.reduce((sum, r) => sum + r.taxAmount, 0);
                  return (
                    <>
                      <Table.Summary.Row>
                        <Table.Summary.Cell index={0} colSpan={4} align="right"><strong>合计:</strong></Table.Summary.Cell>
                        <Table.Summary.Cell index={1} align="right">
                          <strong style={{ color: '#1890ff' }}>¥{totalNet.toLocaleString()}</strong>
                        </Table.Summary.Cell>
                        <Table.Summary.Cell index={2} />
                        <Table.Summary.Cell index={3} align="right">
                          <strong style={{ color: '#faad14' }}>¥{totalTax.toLocaleString()}</strong>
                        </Table.Summary.Cell>
                      </Table.Summary.Row>
                    </>
                  );
                }}
              />
            </Card>
          </>
        )}
      </Modal>
    </div>
  );
}
