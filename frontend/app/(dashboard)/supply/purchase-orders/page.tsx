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
  Steps,
  Descriptions,
  message,
  Popconfirm,
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
  SendOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import { purchaseOrderApi } from '@/lib/api/supply';
import type { PurchaseOrderDTO } from '@/lib/api/supply';

// Default tenant ID
const DEFAULT_TENANT_ID = 1;

// 供应商
const mockVendors = [
  { code: 'V001', name: '华东钢铁集团' },
  { code: 'V002', name: '深圳电子科技' },
  { code: 'V003', name: '北京精密机械' },
  { code: 'V004', name: '苏州包装材料' },
  { code: 'V005', name: '广州化工集团' },
];

export default function PurchaseOrdersPage() {
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState<PurchaseOrderDTO[]>([]);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [activeTab, setActiveTab] = useState('all');
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<PurchaseOrderDTO | null>(null);
  const [form] = Form.useForm();

  // 状态配置
  const statusConfig: Record<string, { color: string; text: string; step: number }> = {
    '0': { color: 'default', text: '草稿', step: 0 },
    '1': { color: 'processing', text: '审批中', step: 1 },
    '2': { color: 'green', text: '已批准', step: 2 },
    '3': { color: 'blue', text: '已关闭', step: 3 },
  };

  const poTypeConfig: Record<string, string> = {
    'NB': '标准采购订单',
    'UB': '库存转移订单',
    'ZCON': '合同采购',
  };

  const fetchOrders = useCallback(async (page = currentPage, size = pageSize, status?: string) => {
    try {
      setLoading(true);
      const res = await purchaseOrderApi.getPage({
        tenantId: DEFAULT_TENANT_ID,
        status,
        current: page,
        size,
      });
      if (res.success && res.data) {
        setOrders(res.data.records);
        setTotal(res.data.total);
      }
    } catch (error) {
      console.error('Failed to fetch purchase orders:', error);
      message.error('获取采购订单失败');
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize]);

  useEffect(() => {
    fetchOrders();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // 按状态筛选
  const filteredOrders = activeTab === 'all' ? orders : orders.filter(o => {
    if (activeTab === 'draft') return o.status === '0';
    if (activeTab === 'pending') return o.status === '1';
    if (activeTab === 'approved') return o.status === '2';
    if (activeTab === 'closed') return o.status === '3';
    return true;
  });

  // 采购订单列
  const orderColumns = [
    {
      title: '采购订单号', dataIndex: 'poNumber', key: 'poNumber', width: 130, fixed: 'left' as const,
      render: (text: string, record: any) => (
        <a onClick={() => { setSelectedOrder(record); setDetailModalVisible(true); }}>{text}</a>
      ),
    },
    { title: '订单类型', dataIndex: 'poType', key: 'poType', width: 100, render: (t: string) => <Tag>{poTypeConfig[t] || t}</Tag> },
    { title: '供应商', dataIndex: 'vendorName', key: 'vendorName', width: 140 },
    { title: '采购组织', dataIndex: 'purchasingOrg', key: 'purchasingOrg', width: 90 },
    { title: '采购组', dataIndex: 'purchasingGroup', key: 'purchasingGroup', width: 80 },
    { title: '凭证日期', dataIndex: 'documentDate', key: 'documentDate', width: 100 },
    {
      title: '净价值', dataIndex: 'totalNetValue', key: 'totalNetValue', width: 120, align: 'right' as const,
      render: (v: number) => <span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{v.toLocaleString()}</span>,
    },
    {
      title: '含税金额', dataIndex: 'totalGrossValue', key: 'totalGrossValue', width: 120, align: 'right' as const,
      render: (v: number) => `¥${v.toLocaleString()}`,
    },
    {
      title: '状态', dataIndex: 'status', key: 'status', width: 90,
      render: (status: string) => {
        const config = statusConfig[status];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    {
      title: '操作', key: 'action', width: 200, fixed: 'right' as const,
      render: (_: unknown, record: any) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => { setSelectedOrder(record); setDetailModalVisible(true); }}>详情</Button>
          {record.status === '0' && (
            <>
              <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
              <Popconfirm title="确定提交审批？" onConfirm={async () => {
                try {
                  const res = await purchaseOrderApi.submit(record.id);
                  if (res.success) {
                    message.success('提交成功');
                    fetchOrders();
                  } else {
                    message.error(res.message || '提交失败');
                  }
                } catch {
                  message.error('提交失败');
                }
              }}>
                <Button type="link" size="small" icon={<SendOutlined />} style={{ color: '#faad14' }}>提交</Button>
              </Popconfirm>
            </>
          )}
          {record.status === '1' && (
            <>
              <Button type="link" size="small" icon={<CheckOutlined />} style={{ color: '#52c41a' }} onClick={async () => {
                try {
                  const res = await purchaseOrderApi.approve(record.id, 'current_user');
                  if (res.success) {
                    message.success('审批成功');
                    fetchOrders();
                  } else {
                    message.error(res.message || '审批失败');
                  }
                } catch {
                  message.error('审批失败');
                }
              }}>审批</Button>
              <Button type="link" size="small" danger icon={<CloseCircleOutlined />}>拒绝</Button>
            </>
          )}
          {record.status === '2' && (
            <Button type="link" size="small" icon={<ExportOutlined />} style={{ color: '#1890ff' }}>收货</Button>
          )}
        </Space>
      ),
    },
  ];

  // 统计
  const stats = {
    totalOrders: orders.length,
    pendingApproval: orders.filter(o => o.status === '1').length,
    totalValue: orders.reduce((s, o) => s + o.totalNetValue, 0),
    goodsReceived: orders.filter(o => o.items?.some(i => i.quantityDelivered > 0)).length,
  };

  const handleTabChange = (key: string) => {
    setActiveTab(key);
    setCurrentPage(1);
    const status = key === 'all' ? undefined : key === 'draft' ? '0' : key === 'pending' ? '1' : key === 'approved' ? '2' : key === 'closed' ? '3' : undefined;
    fetchOrders(1, pageSize, status);
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="采购订单管理 (对标 SAP ME21N/ME22N/ME23N)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => fetchOrders()}>刷新</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalVisible(true)}>
              新建采购订单
            </Button>
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={handleTabChange}
          items={[
            { key: 'all', label: <><ShoppingCartOutlined /> 全部订单</> },
            { key: 'draft', label: '草稿' },
            { key: 'pending', label: '待审批' },
            { key: 'approved', label: '已批准' },
            { key: 'closed', label: '已关闭' },
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
              <Statistic title="采购总额" value={stats.totalValue} prefix="¥" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="已收货" value={stats.goodsReceived} suffix="单" valueStyle={{ fontSize: 18, color: '#52c41a' }} />
            </Card>
          </Col>
        </Row>

        {/* 搜索表单 */}
        <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="poNumber" label="采购订单号">
            <Input placeholder="订单号" style={{ width: 130 }} />
          </Form.Item>
          <Form.Item name="vendorName" label="供应商">
            <Input placeholder="供应商" style={{ width: 130 }} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select placeholder="全部" allowClear style={{ width: 100 }}
              options={Object.entries(statusConfig).map(([k, v]) => ({ value: k, label: v.text }))} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SearchOutlined />} onClick={() => fetchOrders(1)}>查询</Button>
          </Form.Item>
        </Form>

        {/* 订单表格 */}
        <Table
          columns={orderColumns}
          dataSource={filteredOrders}
          rowKey="id"
          loading={loading}
          size="small"
          scroll={{ x: 1200 }}
          pagination={{
            current: currentPage,
            pageSize,
            total,
            showSizeChanger: true,
            onChange: (page, size) => {
              setCurrentPage(page);
              setPageSize(size);
              fetchOrders(page, size);
            },
          }}
        />
      </Card>

      {/* 新建采购订单弹窗 */}
      <Modal
        title="新建采购订单"
        open={createModalVisible}
        onCancel={() => setCreateModalVisible(false)}
        onOk={() => { message.success('采购订单创建成功'); setCreateModalVisible(false); fetchOrders(); }}
        width={900}
      >
        <Form layout="vertical">
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="供应商" required>
                <Select placeholder="选择供应商" showSearch optionFilterProp="label"
                  options={mockVendors.map(v => ({ value: v.code, label: v.name }))} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="采购组织" required>
                <Select placeholder="采购组织" options={[
                  { value: '1000', label: '1000 - 华东采购' },
                  { value: '2000', label: '2000 - 华北采购' },
                ]} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="采购组">
                <Select placeholder="采购组" options={[
                  { value: '001', label: '001 - 原材料组' },
                  { value: '002', label: '002 - 电子件组' },
                  { value: '003', label: '003 - 包装组' },
                ]} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="订单类型">
                <Select placeholder="订单类型" options={[
                  { value: 'NB', label: 'NB - 标准采购订单' },
                  { value: 'UB', label: 'UB - 库存转移' },
                ]} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="凭证日期">
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="付款条件">
                <Select placeholder="付款条件" options={[
                  { value: '0001', label: '0001 - 立即付款' },
                  { value: 'Z001', label: 'Z001 - 30天' },
                  { value: 'Z002', label: 'Z002 - 60天' },
                ]} />
              </Form.Item>
            </Col>
          </Row>
          <Card title="采购明细" size="small">
            <Table
              columns={[
                { title: '物料编码', dataIndex: 'materialCode', width: 110 },
                { title: '描述', dataIndex: 'shortText', width: 140 },
                { title: '数量', dataIndex: 'quantity', width: 80 },
                { title: '单位', dataIndex: 'unit', width: 60 },
                { title: '单价', dataIndex: 'price', width: 90, render: (v: number) => `¥${v.toFixed(2)}` },
                { title: '净值', dataIndex: 'netValue', width: 100, render: (v: number) => `¥${v.toLocaleString()}` },
                { title: '工厂', dataIndex: 'plantCode', width: 70 },
                { title: '交货日期', dataIndex: 'deliveryDate', width: 100 },
                { title: '操作', width: 60, render: () => <Button type="link" size="small" danger>删除</Button> },
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
        </Form>
      </Modal>

      {/* 订单详情弹窗 */}
      <Modal
        title={`采购订单详情 - ${selectedOrder?.poNumber}`}
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
                  { title: '创建' },
                  { title: '提交审批' },
                  { title: '已批准' },
                  { title: '已关闭' },
                ]}
              />
            </Card>

            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={16}>
                <Descriptions bordered size="small" column={2}>
                  <Descriptions.Item label="采购订单号">{selectedOrder.poNumber}</Descriptions.Item>
                  <Descriptions.Item label="订单类型">
                    <Tag>{poTypeConfig[selectedOrder.poType] || selectedOrder.poType}</Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="供应商">{selectedOrder.vendorName}</Descriptions.Item>
                  <Descriptions.Item label="采购组织">{selectedOrder.purchasingOrg}</Descriptions.Item>
                  <Descriptions.Item label="采购组">{selectedOrder.purchasingGroup}</Descriptions.Item>
                  <Descriptions.Item label="凭证日期">{selectedOrder.documentDate}</Descriptions.Item>
                  <Descriptions.Item label="有效期">{selectedOrder.validFrom} ~ {selectedOrder.validTo}</Descriptions.Item>
                  <Descriptions.Item label="状态">
                    <Tag color={statusConfig[selectedOrder.status]?.color}>
                      {statusConfig[selectedOrder.status]?.text}
                    </Tag>
                  </Descriptions.Item>
                </Descriptions>
              </Col>
              <Col span={8}>
                <Card size="small" style={{ background: '#f6f8fa' }}>
                  <Statistic title="净价值" value={selectedOrder.totalNetValue} prefix="¥" />
                  <div style={{ marginTop: 8 }}>
                    <Statistic title="含税金额" value={selectedOrder.totalGrossValue} prefix="¥" valueStyle={{ fontSize: 16, color: '#52c41a' }} />
                  </div>
                </Card>
              </Col>
            </Row>

            <Card title="采购明细" size="small">
              <Table
                columns={[
                  { title: '行号', dataIndex: 'poItem', width: 60 },
                  { title: '物料', dataIndex: 'materialCode', width: 100 },
                  { title: '描述', dataIndex: 'shortText', width: 140 },
                  { title: '数量', dataIndex: 'quantity', width: 80, render: (v: number, r: any) => `${v} ${r.unit}` },
                  { title: '单价', dataIndex: 'price', width: 90, align: 'right' as const, render: (v: number) => `¥${v.toFixed(2)}` },
                  { title: '净值', dataIndex: 'netValue', width: 100, align: 'right' as const, render: (v: number) => `¥${v.toLocaleString()}` },
                  { title: '工厂', dataIndex: 'plantCode', width: 60 },
                  { title: '交货日期', dataIndex: 'deliveryDate', width: 100 },
                  { title: '已收货', dataIndex: 'quantityDelivered', width: 80, render: (v: number, r: any) => `${v} ${r.unit}` },
                  { title: '已开票', dataIndex: 'quantityInvoiced', width: 80, render: (v: number, r: any) => `${v} ${r.unit}` },
                ]}
                dataSource={selectedOrder.items}
                rowKey="poItem"
                size="small"
                pagination={false}
                summary={(data) => {
                  const totalNet = data.reduce((sum, r) => sum + r.netValue, 0);
                  const totalTax = data.reduce((sum, r) => sum + r.taxAmount, 0);
                  return (
                    <>
                      <Table.Summary.Row>
                        <Table.Summary.Cell index={0} colSpan={5} align="right"><strong>合计:</strong></Table.Summary.Cell>
                        <Table.Summary.Cell index={1} align="right">
                          <strong style={{ color: '#1890ff' }}>¥{totalNet.toLocaleString()}</strong>
                        </Table.Summary.Cell>
                        <Table.Summary.Cell index={2} colSpan={3} />
                      </Table.Summary.Row>
                      <Table.Summary.Row>
                        <Table.Summary.Cell index={0} colSpan={5} align="right"><strong>税额:</strong></Table.Summary.Cell>
                        <Table.Summary.Cell index={1} align="right">
                          <strong style={{ color: '#faad14' }}>¥{totalTax.toLocaleString()}</strong>
                        </Table.Summary.Cell>
                        <Table.Summary.Cell index={2} colSpan={3} />
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
