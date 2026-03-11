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
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  CheckOutlined,
  ShoppingCartOutlined,
  ExportOutlined,
  CopyOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;

// 模拟销售订单数据
const mockSalesOrders = [
  { id: 1, orderNo: 'SO-2023-001', customerCode: 'CUST-001', customerName: '北京科技有限公司', orderDate: '2023-12-10', deliveryDate: '2023-12-20', totalAmount: 125000, currency: 'CNY', status: 3, items: 5, createdBy: '销售员A', approvedBy: '经理B', approvedAt: '2023-12-11', paymentStatus: 'paid' },
  { id: 2, orderNo: 'SO-2023-002', customerCode: 'CUST-002', customerName: '上海贸易集团', orderDate: '2023-12-12', deliveryDate: '2023-12-25', totalAmount: 256000, currency: 'CNY', status: 2, items: 3, createdBy: '销售员A', approvedBy: null, approvedAt: null, paymentStatus: 'unpaid' },
  { id: 3, orderNo: 'SO-2023-003', customerCode: 'CUST-003', customerName: '广州制造企业', orderDate: '2023-12-14', deliveryDate: '2023-12-22', totalAmount: 88000, currency: 'CNY', status: 1, items: 2, createdBy: '销售员B', approvedBy: null, approvedAt: null, paymentStatus: 'unpaid' },
  { id: 4, orderNo: 'SO-2023-004', customerCode: 'CUST-001', customerName: '北京科技有限公司', orderDate: '2023-12-15', deliveryDate: '2023-12-28', totalAmount: 45000, currency: 'CNY', status: 0, items: 4, createdBy: '销售员C', approvedBy: null, approvedAt: null, paymentStatus: 'unpaid' },
  { id: 5, orderNo: 'SO-2023-005', customerCode: 'CUST-004', customerName: '深圳电子公司', orderDate: '2023-12-16', deliveryDate: '2023-12-30', totalAmount: 380000, currency: 'CNY', status: 4, items: 8, createdBy: '销售员A', approvedBy: '经理B', approvedAt: '2023-12-17', paymentStatus: 'partial' },
  { id: 6, orderNo: 'SO-2023-006', customerCode: 'CUST-005', customerName: '杭州网络科技', orderDate: '2023-12-17', deliveryDate: '2023-12-24', totalAmount: 67000, currency: 'CNY', status: 5, items: 2, createdBy: '销售员B', approvedBy: null, approvedAt: null, paymentStatus: 'refunded' },
];

// 销售订单明细
const mockOrderDetails = [
  { id: 1, orderId: 1, productCode: 'PROD-001', productName: '产品A', quantity: 100, unit: 'PCS', unitPrice: 450.00, amount: 45000, deliveredQty: 100, status: 'complete' },
  { id: 2, orderId: 1, productCode: 'PROD-002', productName: '产品B', quantity: 200, unit: 'PCS', unitPrice: 125.00, amount: 25000, deliveredQty: 200, status: 'complete' },
  { id: 3, orderId: 1, productCode: 'PROD-003', productName: '配件C', quantity: 500, unit: 'SET', unitPrice: 110.00, amount: 55000, deliveredQty: 500, status: 'complete' },
  { id: 4, orderId: 2, productCode: 'PROD-001', productName: '产品A', quantity: 150, unit: 'PCS', unitPrice: 480.00, amount: 72000, deliveredQty: 0, status: 'pending' },
  { id: 5, orderId: 2, productCode: 'PROD-004', productName: '产品D', quantity: 50, unit: 'PCS', unitPrice: 3200.00, amount: 160000, deliveredQty: 0, status: 'pending' },
  { id: 6, orderId: 2, productCode: 'PROD-005', productName: '服务费', quantity: 1, unit: 'ITEM', unitPrice: 24000.00, amount: 24000, deliveredQty: 0, status: 'pending' },
];

// 客户数据
const mockCustomers = [
  { code: 'CUST-001', name: '北京科技有限公司', contact: '李总', phone: '13800138001', creditLimit: 500000 },
  { code: 'CUST-002', name: '上海贸易集团', contact: '王总', phone: '13900139002', creditLimit: 800000 },
  { code: 'CUST-003', name: '广州制造企业', contact: '张总', phone: '13700137003', creditLimit: 300000 },
  { code: 'CUST-004', name: '深圳电子公司', contact: '陈总', phone: '13600136004', creditLimit: 600000 },
  { code: 'CUST-005', name: '杭州网络科技', contact: '赵总', phone: '13500135005', creditLimit: 200000 },
];

export default function SalesOrdersPage() {
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState(mockSalesOrders);
  const [activeTab, setActiveTab] = useState('all');
  const [orderModalVisible, setOrderModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<typeof mockSalesOrders[0] | null>(null);
  const [form] = Form.useForm();

  // 状态配置
  const statusConfig: Record<number, { color: string; text: string; step: number }> = {
    0: { color: 'default', text: '草稿', step: 0 },
    1: { color: 'processing', text: '待审批', step: 1 },
    2: { color: 'orange', text: '已审批', step: 2 },
    3: { color: 'green', text: '已发货', step: 3 },
    4: { color: 'blue', text: '已完成', step: 4 },
    5: { color: 'red', text: '已取消', step: -1 },
  };

  // 付款状态
  const paymentStatusConfig: Record<string, { color: string; text: string }> = {
    unpaid: { color: 'orange', text: '未付款' },
    partial: { color: 'blue', text: '部分付款' },
    paid: { color: 'green', text: '已付款' },
    refunded: { color: 'red', text: '已退款' },
  };

  // 按状态筛选
  const filteredOrders = activeTab === 'all' ? orders : orders.filter(o => {
    if (activeTab === 'pending') return o.status === 1;
    if (activeTab === 'approved') return o.status === 2;
    if (activeTab === 'shipped') return o.status === 3;
    if (activeTab === 'completed') return o.status === 4;
    return true;
  });

  // 销售订单列
  const orderColumns = [
    { title: '销售单号', dataIndex: 'orderNo', key: 'orderNo', width: 130, fixed: 'left' as const,
      render: (text: string, record) => (
        <a onClick={() => { setSelectedOrder(record); setDetailModalVisible(true); }}>{text}</a>
      ),
    },
    { title: '客户', dataIndex: 'customerName', key: 'customerName', width: 140 },
    { title: '订单日期', dataIndex: 'orderDate', key: 'orderDate', width: 100 },
    { title: '交货日期', dataIndex: 'deliveryDate', key: 'deliveryDate', width: 100,
      render: (date: string, record) => {
        const isOverdue = dayjs(date).isBefore(dayjs(), 'day') && record.status < 4 && record.status !== 5;
        return <span style={{ color: isOverdue ? '#ff4d4f' : 'inherit' }}>{date}</span>;
      },
    },
    { title: '订单金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120, align: 'right' as const,
      render: (v: number) => <span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{v.toLocaleString()}</span>,
    },
    { title: '物料数', dataIndex: 'items', key: 'items', width: 80, align: 'center' as const,
      render: (v: number) => <Badge count={v} showZero style={{ backgroundColor: '#722ed1' }} />,
    },
    { title: '订单状态', dataIndex: 'status', key: 'status', width: 90,
      render: (status: number) => {
        const config = statusConfig[status];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    { title: '付款状态', dataIndex: 'paymentStatus', key: 'paymentStatus', width: 90,
      render: (status: string) => {
        const config = paymentStatusConfig[status];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    { title: '创建人', dataIndex: 'createdBy', key: 'createdBy', width: 90 },
    { title: '操作', key: 'action', width: 220, fixed: 'right' as const,
      render: (_: unknown, record) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => { setSelectedOrder(record); setDetailModalVisible(true); }}>详情</Button>
          {record.status === 0 && (
            <>
              <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
              <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
            </>
          )}
          {record.status === 1 && (
            <Button type="link" size="small" icon={<CheckOutlined />} style={{ color: '#52c41a' }}>审批</Button>
          )}
          {record.status === 2 && (
            <Button type="link" size="small" icon={<ExportOutlined />} style={{ color: '#1890ff' }}>发货</Button>
          )}
          {record.status >= 0 && record.status < 4 && (
            <Button type="link" size="small" icon={<CopyOutlined />}>复制</Button>
          )}
        </Space>
      ),
    },
  ];

  // 统计
  const stats = {
    totalOrders: orders.length,
    pendingApproval: orders.filter(o => o.status === 1).length,
    totalAmount: orders.reduce((s, o) => s + o.totalAmount, 0),
    completedOrders: orders.filter(o => o.status >= 4).length,
    unpaidAmount: orders.filter(o => o.paymentStatus !== 'paid' && o.paymentStatus !== 'refunded')
      .reduce((s, o) => s + o.totalAmount, 0),
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="销售订单管理 (对标 SAP VA01/VA02/VA03)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => setLoading(!loading)}>刷新</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setOrderModalVisible(true)}>
              新建销售单
            </Button>
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            { key: 'all', label: <><ShoppingCartOutlined /> 全部订单</> },
            { key: 'pending', label: <>待审批</> },
            { key: 'approved', label: <>已审批</> },
            { key: 'shipped', label: <>已发货</> },
            { key: 'completed', label: <>已完成</> },
          ]}
        />

        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={4}>
            <Card size="small">
              <Statistic title="销售订单" value={stats.totalOrders} suffix="单" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="待审批" value={stats.pendingApproval} suffix="单" valueStyle={{ fontSize: 18, color: '#faad14' }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="销售总额" value={stats.totalAmount} prefix="¥" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="已完成" value={stats.completedOrders} suffix="单" valueStyle={{ fontSize: 18, color: '#52c41a' }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="待收款" value={stats.unpaidAmount} prefix="¥" valueStyle={{ fontSize: 18, color: '#ff4d4f' }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="回款率" value={((stats.totalAmount - stats.unpaidAmount) / stats.totalAmount * 100).toFixed(1)} suffix="%" valueStyle={{ fontSize: 18, color: '#1890ff' }} />
            </Card>
          </Col>
        </Row>

        {/* 搜索表单 */}
        <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="orderNo" label="销售单号">
            <Input placeholder="销售单号" style={{ width: 130 }} />
          </Form.Item>
          <Form.Item name="customerName" label="客户">
            <Input placeholder="客户名称" style={{ width: 130 }} />
          </Form.Item>
          <Form.Item name="dateRange" label="日期">
            <RangePicker style={{ width: 240 }} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select placeholder="全部" allowClear style={{ width: 100 }}
              options={Object.entries(statusConfig).map(([k, v]) => ({ value: Number(k), label: v.text }))} />
          </Form.Item>
          <Form.Item name="paymentStatus" label="付款">
            <Select placeholder="全部" allowClear style={{ width: 100 }}
              options={Object.entries(paymentStatusConfig).map(([k, v]) => ({ value: k, label: v.text }))} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SearchOutlined />}>查询</Button>
          </Form.Item>
        </Form>

        {/* 订单表格 */}
        <Table
          columns={orderColumns}
          dataSource={filteredOrders}
          rowKey="id"
          loading={loading}
          size="small"
          scroll={{ x: 1400 }}
          pagination={{ defaultPageSize: 20, showSizeChanger: true }}
        />
      </Card>

      {/* 新建销售单弹窗 */}
      <Modal
        title="新建销售单"
        open={orderModalVisible}
        onCancel={() => setOrderModalVisible(false)}
        onOk={() => { message.success('销售单创建成功'); setOrderModalVisible(false); }}
        width={900}
      >
        <Form layout="vertical">
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="客户" required>
                <Select
                  placeholder="选择客户"
                  showSearch
                  optionFilterProp="label"
                  options={mockCustomers.map(c => ({ value: c.code, label: c.name }))}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="订单日期" required>
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="交货日期" required>
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Card title="销售明细" size="small">
            <Table
              columns={[
                { title: '产品编码', dataIndex: 'productCode', width: 100 },
                { title: '产品名称', dataIndex: 'productName', width: 120 },
                { title: '数量', dataIndex: 'quantity', width: 80 },
                { title: '单位', dataIndex: 'unit', width: 60 },
                { title: '单价', dataIndex: 'unitPrice', width: 100, render: (v) => `¥${v.toFixed(2)}` },
                { title: '金额', dataIndex: 'amount', width: 100, render: (v) => `¥${v.toLocaleString()}` },
                { title: '操作', width: 60, render: () => <Button type="link" size="small" danger>删除</Button> },
              ]}
              dataSource={[]}
              size="small"
              pagination={false}
              locale={{ emptyText: '点击下方按钮添加产品' }}
            />
            <Button type="dashed" block style={{ marginTop: 16 }} icon={<PlusOutlined />}>
              添加产品
            </Button>
          </Card>
          <Row gutter={16} style={{ marginTop: 16 }}>
            <Col span={8}>
              <Form.Item label="付款方式">
                <Select placeholder="选择付款方式" options={[
                  { value: 'cash', label: '现金' },
                  { value: 'transfer', label: '银行转账' },
                  { value: 'credit', label: '信用账期' },
                ]} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="交货方式">
                <Select placeholder="选择交货方式" options={[
                  { value: 'delivery', label: '送货' },
                  { value: 'pickup', label: '自提' },
                  { value: 'express', label: '快递' },
                ]} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="备注">
                <Input placeholder="输入备注" />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>

      {/* 订单详情弹窗 */}
      <Modal
        title={`销售单详情 - ${selectedOrder?.orderNo}`}
        open={detailModalVisible}
        onCancel={() => { setDetailModalVisible(false); setSelectedOrder(null); }}
        footer={[
          <Button key="print">打印</Button>,
          <Button key="export" icon={<ExportOutlined />}>导出</Button>,
          <Button key="close" type="primary" onClick={() => { setDetailModalVisible(false); setSelectedOrder(null); }}>关闭</Button>,
        ]}
        width={1000}
      >
        {selectedOrder && (
          <>
            <Card size="small" style={{ marginBottom: 16 }}>
              <Steps
                size="small"
                current={statusConfig[selectedOrder.status]?.step || 0}
                items={[
                  { title: '创建', status: 'finish' },
                  { title: '审批', status: selectedOrder.status >= 2 ? 'finish' : 'wait' },
                  { title: '发货', status: selectedOrder.status >= 3 ? 'finish' : 'wait' },
                  { title: '完成', status: selectedOrder.status >= 4 ? 'finish' : 'wait' },
                ]}
              />
            </Card>

            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={16}>
                <Descriptions bordered size="small" column={2}>
                  <Descriptions.Item label="销售单号">{selectedOrder.orderNo}</Descriptions.Item>
                  <Descriptions.Item label="客户">{selectedOrder.customerName}</Descriptions.Item>
                  <Descriptions.Item label="订单日期">{selectedOrder.orderDate}</Descriptions.Item>
                  <Descriptions.Item label="交货日期">{selectedOrder.deliveryDate}</Descriptions.Item>
                  <Descriptions.Item label="订单状态">
                    <Tag color={statusConfig[selectedOrder.status]?.color}>{statusConfig[selectedOrder.status]?.text}</Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="付款状态">
                    <Tag color={paymentStatusConfig[selectedOrder.paymentStatus]?.color}>
                      {paymentStatusConfig[selectedOrder.paymentStatus]?.text}
                    </Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="创建人">{selectedOrder.createdBy}</Descriptions.Item>
                  <Descriptions.Item label="审批人">{selectedOrder.approvedBy || '-'}</Descriptions.Item>
                </Descriptions>
              </Col>
              <Col span={8}>
                <Card size="small" style={{ background: '#f6f8fa' }}>
                  <Statistic title="订单金额" value={selectedOrder.totalAmount} prefix="¥" />
                </Card>
              </Col>
            </Row>

            <Card title="销售明细" size="small">
              <Table
                columns={[
                  { title: '产品编码', dataIndex: 'productCode', width: 100 },
                  { title: '产品名称', dataIndex: 'productName', width: 120 },
                  { title: '订单数量', dataIndex: 'quantity', width: 100, render: (v, r: any) => `${v} ${r.unit}` },
                  { title: '单价', dataIndex: 'unitPrice', width: 100, align: 'right' as const, render: (v) => `¥${v.toFixed(2)}` },
                  { title: '金额', dataIndex: 'amount', width: 100, align: 'right' as const, render: (v) => `¥${v.toLocaleString()}` },
                  { title: '已发货', dataIndex: 'deliveredQty', width: 100, render: (v, r: any) => `${v} ${r.unit}` },
                  {
                    title: '状态', dataIndex: 'status', width: 80,
                    render: (status: string) => {
                      const map: Record<string, { color: string; text: string }> = {
                        pending: { color: 'orange', text: '待发货' },
                        partial: { color: 'blue', text: '部分发货' },
                        complete: { color: 'green', text: '已发货' },
                      };
                      const s = map[status] || { color: 'default', text: status };
                      return <Tag color={s.color}>{s.text}</Tag>;
                    },
                  },
                ]}
                dataSource={mockOrderDetails.filter(d => d.orderId === selectedOrder.id)}
                rowKey="id"
                size="small"
                pagination={false}
                summary={(data) => {
                  const total = data.reduce((sum, r) => sum + r.amount, 0);
                  return (
                    <Table.Summary.Row>
                      <Table.Summary.Cell index={0} colSpan={4} align="right">
                        <strong>合计:</strong>
                      </Table.Summary.Cell>
                      <Table.Summary.Cell index={1} align="right">
                        <strong style={{ color: '#1890ff' }}>¥{total.toLocaleString()}</strong>
                      </Table.Summary.Cell>
                      <Table.Summary.Cell index={2} colSpan={2} />
                    </Table.Summary.Row>
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
