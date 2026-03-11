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
  Timeline,
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  CheckOutlined,
  CloseOutlined,
  SyncOutlined,
  ShoppingCartOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;

// 模拟采购订单数据
const mockPurchaseOrders = [
  { id: 1, orderNo: 'PO-2023-001', supplierCode: 'SUP-001', supplierName: '原材料供应商A', orderDate: '2023-12-10', deliveryDate: '2023-12-20', totalAmount: 85000, currency: 'CNY', status: 3, items: 3, createdBy: '张三', approvedBy: '李四', approvedAt: '2023-12-11' },
  { id: 2, orderNo: 'PO-2023-002', supplierCode: 'SUP-002', supplierName: '设备供应商B', orderDate: '2023-12-12', deliveryDate: '2023-12-25', totalAmount: 156000, currency: 'CNY', status: 2, items: 2, createdBy: '张三', approvedBy: null, approvedAt: null },
  { id: 3, orderNo: 'PO-2023-003', supplierCode: 'SUP-003', supplierName: '包装材料公司', orderDate: '2023-12-14', deliveryDate: '2023-12-22', totalAmount: 28000, currency: 'CNY', status: 1, items: 5, createdBy: '李四', approvedBy: null, approvedAt: null },
  { id: 4, orderNo: 'PO-2023-004', supplierCode: 'SUP-001', supplierName: '原材料供应商A', orderDate: '2023-12-15', deliveryDate: '2023-12-28', totalAmount: 45000, currency: 'CNY', status: 0, items: 2, createdBy: '王五', approvedBy: null, approvedAt: null },
];

// 采购订单明细
const mockOrderDetails = [
  { id: 1, orderId: 1, materialCode: 'MAT-001', materialName: '原材料A', quantity: 1000, unit: 'KG', unitPrice: 25.50, amount: 25500, receivedQty: 1000, status: 'complete' },
  { id: 2, orderId: 1, materialCode: 'MAT-002', materialName: '原材料B', quantity: 500, unit: 'KG', unitPrice: 32.00, amount: 16000, receivedQty: 500, status: 'complete' },
  { id: 3, orderId: 1, materialCode: 'MAT-003', materialName: '包装材料', quantity: 2000, unit: 'PCS', unitPrice: 21.75, amount: 43500, receivedQty: 1500, status: 'partial' },
  { id: 4, orderId: 2, materialCode: 'MAT-006', materialName: '辅助材料E', quantity: 100, unit: 'L', unitPrice: 60.00, amount: 6000, receivedQty: 0, status: 'pending' },
  { id: 5, orderId: 2, materialCode: 'MAT-007', materialName: '备件F', quantity: 10, unit: 'SET', unitPrice: 15000.00, amount: 150000, receivedQty: 0, status: 'pending' },
];

// 供应商数据
const mockSuppliers = [
  { code: 'SUP-001', name: '原材料供应商A', contact: '赵经理', phone: '13800138001', address: '上海市浦东新区', rating: 5 },
  { code: 'SUP-002', name: '设备供应商B', contact: '钱经理', phone: '13900139002', address: '江苏省苏州市', rating: 4 },
  { code: 'SUP-003', name: '包装材料公司', contact: '孙经理', phone: '13700137003', address: '浙江省杭州市', rating: 4 },
];

export default function PurchasePage() {
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState(mockPurchaseOrders);
  const [activeTab, setActiveTab] = useState('orders');
  const [orderModalVisible, setOrderModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<typeof mockPurchaseOrders[0] | null>(null);
  const [form] = Form.useForm();

  // 状态配置
  const statusConfig: Record<number, { color: string; text: string; step: number }> = {
    0: { color: 'default', text: '草稿', step: 0 },
    1: { color: 'processing', text: '待审批', step: 1 },
    2: { color: 'orange', text: '已审批', step: 2 },
    3: { color: 'green', text: '已收货', step: 3 },
    4: { color: 'blue', text: '已完成', step: 4 },
    5: { color: 'red', text: '已取消', step: -1 },
  };

  // 采购订单列
  const orderColumns = [
    { title: '采购单号', dataIndex: 'orderNo', key: 'orderNo', width: 130, fixed: 'left' as const,
      render: (text: string, record) => (
        <a onClick={() => { setSelectedOrder(record); setDetailModalVisible(true); }}>{text}</a>
      ),
    },
    { title: '供应商', dataIndex: 'supplierName', key: 'supplierName', width: 140 },
    { title: '订单日期', dataIndex: 'orderDate', key: 'orderDate', width: 100 },
    { title: '交货日期', dataIndex: 'deliveryDate', key: 'deliveryDate', width: 100,
      render: (date: string, record) => {
        const isOverdue = dayjs(date).isBefore(dayjs(), 'day') && record.status < 3;
        return <span style={{ color: isOverdue ? '#ff4d4f' : 'inherit' }}>{date}</span>;
      },
    },
    { title: '订单金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120, align: 'right' as const,
      render: (v: number) => <span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{v.toLocaleString()}</span>,
    },
    { title: '物料数', dataIndex: 'items', key: 'items', width: 80, align: 'center' as const,
      render: (v: number) => <Badge count={v} showZero style={{ backgroundColor: '#52c41a' }} />,
    },
    { title: '状态', dataIndex: 'status', key: 'status', width: 90,
      render: (status: number) => {
        const config = statusConfig[status];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    { title: '创建人', dataIndex: 'createdBy', key: 'createdBy', width: 80 },
    { title: '操作', key: 'action', width: 200, fixed: 'right' as const,
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
        </Space>
      ),
    },
  ];

  // 供应商列
  const supplierColumns = [
    { title: '供应商编码', dataIndex: 'code', key: 'code', width: 120 },
    { title: '供应商名称', dataIndex: 'name', key: 'name', width: 150 },
    { title: '联系人', dataIndex: 'contact', key: 'contact', width: 100 },
    { title: '联系电话', dataIndex: 'phone', key: 'phone', width: 130 },
    { title: '地址', dataIndex: 'address', key: 'address', width: 200 },
    { title: '评分', dataIndex: 'rating', key: 'rating', width: 100,
      render: (rating: number) => (
        <Space>
          {Array.from({ length: 5 }).map((_, i) => (
            <span key={i} style={{ color: i < rating ? '#faad14' : '#d9d9d9' }}>★</span>
          ))}
        </Space>
      ),
    },
    { title: '操作', key: 'action', width: 120,
      render: () => (
        <Space>
          <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
          <Button type="link" size="small">历史订单</Button>
        </Space>
      ),
    },
  ];

  // 统计
  const stats = {
    totalOrders: orders.length,
    pendingApproval: orders.filter(o => o.status === 1).length,
    totalAmount: orders.reduce((s, o) => s + o.totalAmount, 0),
    completedOrders: orders.filter(o => o.status >= 3).length,
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="采购管理 (对标 SAP ME21N)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => setLoading(!loading)}>刷新</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setOrderModalVisible(true)}>
              新建采购单
            </Button>
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            { key: 'orders', label: <><ShoppingCartOutlined /> 采购订单</> },
            { key: 'suppliers', label: '供应商管理' },
            { key: 'analysis', label: '采购分析' },
          ]}
        />

        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card size="small">
              <Statistic title="采购订单" value={stats.totalOrders} suffix="单" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="待审批" value={stats.pendingApproval} suffix="单" valueStyle={{ fontSize: 18, color: '#faad14' }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="采购总额" value={stats.totalAmount} prefix="¥" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="已完成" value={stats.completedOrders} suffix="单" valueStyle={{ fontSize: 18, color: '#52c41a' }} />
            </Card>
          </Col>
        </Row>

        {/* 采购订单 */}
        {activeTab === 'orders' && (
          <>
            <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item name="orderNo" label="采购单号">
                <Input placeholder="采购单号" style={{ width: 130 }} />
              </Form.Item>
              <Form.Item name="supplierName" label="供应商">
                <Input placeholder="供应商名称" style={{ width: 130 }} />
              </Form.Item>
              <Form.Item name="dateRange" label="日期">
                <RangePicker style={{ width: 240 }} />
              </Form.Item>
              <Form.Item name="status" label="状态">
                <Select placeholder="全部" allowClear style={{ width: 100 }}
                  options={Object.entries(statusConfig).map(([k, v]) => ({ value: Number(k), label: v.text }))} />
              </Form.Item>
              <Form.Item>
                <Button type="primary" icon={<SearchOutlined />}>查询</Button>
              </Form.Item>
            </Form>

            <Table
              columns={orderColumns}
              dataSource={orders}
              rowKey="id"
              loading={loading}
              size="small"
              scroll={{ x: 1200 }}
              pagination={{ defaultPageSize: 20, showSizeChanger: true }}
            />
          </>
        )}

        {/* 供应商管理 */}
        {activeTab === 'suppliers' && (
          <>
            <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item name="keyword" label="搜索">
                <Input placeholder="编码/名称" style={{ width: 150 }} />
              </Form.Item>
              <Form.Item>
                <Button type="primary" icon={<SearchOutlined />}>查询</Button>
              </Form.Item>
              <Form.Item>
                <Button type="primary" icon={<PlusOutlined />}>新增供应商</Button>
              </Form.Item>
            </Form>

            <Table
              columns={supplierColumns}
              dataSource={mockSuppliers}
              rowKey="code"
              loading={loading}
              size="small"
              pagination={{ defaultPageSize: 20 }}
            />
          </>
        )}

        {/* 采购分析 */}
        {activeTab === 'analysis' && (
          <Row gutter={24}>
            <Col span={16}>
              <Card title="采购趋势" size="small">
                <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>
                  采购金额趋势图表 (待集成图表组件)
                </div>
              </Card>
            </Col>
            <Col span={8}>
              <Card title="供应商排名" size="small">
                {mockSuppliers.map((s, i) => (
                  <div key={s.code} style={{ padding: '8px 0', borderBottom: '1px solid #f0f0f0' }}>
                    <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                      <span><Tag>{i + 1}</Tag> {s.name}</span>
                      <span style={{ color: '#1890ff' }}>¥{(Math.random() * 100000).toFixed(0)}</span>
                    </Space>
                  </div>
                ))}
              </Card>
            </Col>
          </Row>
        )}
      </Card>

      {/* 新建采购单弹窗 */}
      <Modal
        title="新建采购单"
        open={orderModalVisible}
        onCancel={() => setOrderModalVisible(false)}
        onOk={() => { message.success('采购单创建成功'); setOrderModalVisible(false); }}
        width={800}
      >
        <Form layout="vertical">
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="供应商" required>
                <Select placeholder="选择供应商" options={mockSuppliers.map(s => ({ value: s.code, label: s.name }))} />
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
          <Card title="采购明细" size="small">
            <Table
              columns={[
                { title: '物料编码', dataIndex: 'materialCode', width: 120 },
                { title: '物料名称', dataIndex: 'materialName', width: 120 },
                { title: '数量', dataIndex: 'quantity', width: 80 },
                { title: '单位', dataIndex: 'unit', width: 60 },
                { title: '单价', dataIndex: 'unitPrice', width: 100 },
                { title: '金额', dataIndex: 'amount', width: 100 },
              ]}
              dataSource={[]}
              size="small"
              pagination={false}
              locale={{ emptyText: '点击下方按钮添加物料' }}
            />
            <Button type="dashed" block style={{ marginTop: 16 }} icon={<PlusOutlined />}>
              添加物料
            </Button>
          </Card>
          <Form.Item label="备注" style={{ marginTop: 16 }}>
            <Input.TextArea rows={2} placeholder="输入备注信息" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 订单详情弹窗 */}
      <Modal
        title={`采购单详情 - ${selectedOrder?.orderNo}`}
        open={detailModalVisible}
        onCancel={() => { setDetailModalVisible(false); setSelectedOrder(null); }}
        footer={[
          <Button key="close" onClick={() => { setDetailModalVisible(false); setSelectedOrder(null); }}>关闭</Button>,
        ]}
        width={900}
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
                  { title: '收货', status: selectedOrder.status >= 3 ? 'finish' : 'wait' },
                  { title: '完成', status: selectedOrder.status >= 4 ? 'finish' : 'wait' },
                ]}
              />
            </Card>

            <Descriptions bordered size="small" column={3} style={{ marginBottom: 16 }}>
              <Descriptions.Item label="采购单号">{selectedOrder.orderNo}</Descriptions.Item>
              <Descriptions.Item label="供应商">{selectedOrder.supplierName}</Descriptions.Item>
              <Descriptions.Item label="状态"><Tag color={statusConfig[selectedOrder.status]?.color}>{statusConfig[selectedOrder.status]?.text}</Tag></Descriptions.Item>
              <Descriptions.Item label="订单日期">{selectedOrder.orderDate}</Descriptions.Item>
              <Descriptions.Item label="交货日期">{selectedOrder.deliveryDate}</Descriptions.Item>
              <Descriptions.Item label="订单金额"><span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{selectedOrder.totalAmount.toLocaleString()}</span></Descriptions.Item>
              <Descriptions.Item label="创建人">{selectedOrder.createdBy}</Descriptions.Item>
              <Descriptions.Item label="审批人">{selectedOrder.approvedBy || '-'}</Descriptions.Item>
              <Descriptions.Item label="审批时间">{selectedOrder.approvedAt || '-'}</Descriptions.Item>
            </Descriptions>

            <Card title="采购明细" size="small">
              <Table
                columns={[
                  { title: '物料编码', dataIndex: 'materialCode', width: 100 },
                  { title: '物料名称', dataIndex: 'materialName', width: 120 },
                  { title: '订单数量', dataIndex: 'quantity', width: 100, render: (v, r: any) => `${v} ${r.unit}` },
                  { title: '单价', dataIndex: 'unitPrice', width: 100, align: 'right' as const, render: (v) => `¥${v.toFixed(2)}` },
                  { title: '金额', dataIndex: 'amount', width: 100, align: 'right' as const, render: (v) => `¥${v.toLocaleString()}` },
                  { title: '已收货', dataIndex: 'receivedQty', width: 100, render: (v, r: any) => `${v} ${r.unit}` },
                  {
                    title: '状态', dataIndex: 'status', width: 80,
                    render: (status: string) => {
                      const map: Record<string, { color: string; text: string }> = {
                        pending: { color: 'orange', text: '待收货' },
                        partial: { color: 'blue', text: '部分收货' },
                        complete: { color: 'green', text: '已收货' },
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
              />
            </Card>
          </>
        )}
      </Modal>
    </div>
  );
}
