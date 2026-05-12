'use client';

import { useState, useEffect, useCallback } from 'react';
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
import { purchaseReqApi, purchaseOrderApi } from '@/lib/api/supply';
import type { PurchaseReqDTO, PurchaseOrderDTO } from '@/lib/api/supply';

const { RangePicker } = DatePicker;

// Default tenant ID
const DEFAULT_TENANT_ID = 1;

// 供应商数据
const mockSuppliers = [
  { code: 'SUP-001', name: '原材料供应商A', contact: '赵经理', phone: '13800138001', address: '上海市浦东新区', rating: 5 },
  { code: 'SUP-002', name: '设备供应商B', contact: '钱经理', phone: '13900139002', address: '江苏省苏州市', rating: 4 },
  { code: 'SUP-003', name: '包装材料公司', contact: '孙经理', phone: '13700137003', address: '浙江省杭州市', rating: 4 },
];

export default function PurchasePage() {
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState<PurchaseOrderDTO[]>([]);
  const [totalOrders, setTotalOrders] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [activeTab, setActiveTab] = useState('orders');
  const [orderModalVisible, setOrderModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<PurchaseOrderDTO | null>(null);
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

  const fetchOrders = useCallback(async (page = currentPage, size = pageSize) => {
    try {
      setLoading(true);
      const res = await purchaseOrderApi.getPage({
        tenantId: DEFAULT_TENANT_ID,
        current: page,
        size,
      });
      if (res.success && res.data) {
        setOrders(res.data.records);
        setTotalOrders(res.data.total);
      }
    } catch (error) {
      console.error('Failed to fetch purchase orders:', error);
      message.error('获取采购订单失败');
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize]);

  useEffect(() => {
    if (activeTab === 'orders') {
      fetchOrders();
    }
  }, [activeTab]); // eslint-disable-line react-hooks/exhaustive-deps

  // 采购订单列
  const orderColumns = [
    { title: '采购单号', dataIndex: 'poNumber', key: 'poNumber', width: 130, fixed: 'left' as const,
      render: (text: string, record: any) => (
        <a onClick={() => { setSelectedOrder(record); setDetailModalVisible(true); }}>{text}</a>
      ),
    },
    { title: '供应商', dataIndex: 'vendorName', key: 'vendorName', width: 140 },
    { title: '订单日期', dataIndex: 'documentDate', key: 'documentDate', width: 100 },
    { title: '交货日期', dataIndex: 'validTo', key: 'validTo', width: 100,
      render: (date: string, record: any) => {
        const isOverdue = dayjs(date).isBefore(dayjs(), 'day') && record.status < '3';
        return <span style={{ color: isOverdue ? '#ff4d4f' : 'inherit' }}>{date}</span>;
      },
    },
    { title: '订单金额', dataIndex: 'totalNetValue', key: 'totalNetValue', width: 120, align: 'right' as const,
      render: (v: number) => <span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{v.toLocaleString()}</span>,
    },
    { title: '物料数', dataIndex: 'items', key: 'items', width: 80, align: 'center' as const,
      render: (v: any[]) => <Badge count={v?.length || 0} showZero style={{ backgroundColor: '#52c41a' }} />,
    },
    { title: '状态', dataIndex: 'status', key: 'status', width: 90,
      render: (status: string) => {
        const config = statusConfig[Number(status)];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    { title: '操作', key: 'action', width: 200, fixed: 'right' as const,
      render: (_: unknown, record: any) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => { setSelectedOrder(record); setDetailModalVisible(true); }}>详情</Button>
          {record.status === '0' && (
            <>
              <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
              <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
            </>
          )}
          {record.status === '1' && (
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
    pendingApproval: orders.filter(o => o.status === '1').length,
    totalAmount: orders.reduce((s, o) => s + o.totalNetValue, 0),
    completedOrders: orders.filter(o => Number(o.status) >= 3).length,
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="采购管理 (对标 SAP ME21N)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => fetchOrders()}>刷新</Button>
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
              pagination={{
                current: currentPage,
                pageSize,
                total: totalOrders,
                showSizeChanger: true,
                onChange: (page, size) => {
                  setCurrentPage(page);
                  setPageSize(size);
                  fetchOrders(page, size);
                },
              }}
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
        onOk={() => { message.success('采购单创建成功'); setOrderModalVisible(false); fetchOrders(); }}
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
        title={`采购单详情 - ${selectedOrder?.poNumber}`}
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
                current={statusConfig[Number(selectedOrder.status)]?.step || 0}
                items={[
                  { title: '创建', status: 'finish' },
                  { title: '审批', status: Number(selectedOrder.status) >= 2 ? 'finish' : 'wait' },
                  { title: '收货', status: Number(selectedOrder.status) >= 3 ? 'finish' : 'wait' },
                  { title: '完成', status: Number(selectedOrder.status) >= 4 ? 'finish' : 'wait' },
                ]}
              />
            </Card>

            <Descriptions bordered size="small" column={3} style={{ marginBottom: 16 }}>
              <Descriptions.Item label="采购单号">{selectedOrder.poNumber}</Descriptions.Item>
              <Descriptions.Item label="供应商">{selectedOrder.vendorName}</Descriptions.Item>
              <Descriptions.Item label="状态"><Tag color={statusConfig[Number(selectedOrder.status)]?.color}>{statusConfig[Number(selectedOrder.status)]?.text}</Tag></Descriptions.Item>
              <Descriptions.Item label="订单日期">{selectedOrder.documentDate}</Descriptions.Item>
              <Descriptions.Item label="交货日期">{selectedOrder.validTo}</Descriptions.Item>
              <Descriptions.Item label="订单金额"><span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{selectedOrder.totalNetValue.toLocaleString()}</span></Descriptions.Item>
              <Descriptions.Item label="创建人">{selectedOrder.createdBy || '-'}</Descriptions.Item>
              <Descriptions.Item label="审批人">{selectedOrder.approvedBy || '-'}</Descriptions.Item>
              <Descriptions.Item label="审批时间">{selectedOrder.approvedAt || '-'}</Descriptions.Item>
            </Descriptions>

            <Card title="采购明细" size="small">
              <Table
                columns={[
                  { title: '物料编码', dataIndex: 'materialCode', width: 100 },
                  { title: '物料名称', dataIndex: 'shortText', width: 120 },
                  { title: '订单数量', dataIndex: 'quantity', width: 100, render: (v, r: any) => `${v} ${r.unit}` },
                  { title: '单价', dataIndex: 'price', width: 100, align: 'right' as const, render: (v) => `¥${v.toFixed(2)}` },
                  { title: '金额', dataIndex: 'netValue', width: 100, align: 'right' as const, render: (v) => `¥${v.toLocaleString()}` },
                  { title: '已收货', dataIndex: 'quantityDelivered', width: 100, render: (v, r: any) => `${v} ${r.unit}` },
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
                dataSource={selectedOrder.items || []}
                rowKey="poItem"
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
