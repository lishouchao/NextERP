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

// 模拟采购订单数据
const mockPurchaseOrders = [
  { id: 1, poNumber: '45000001', poType: 'NB', vendorCode: 'V001', vendorName: '华东钢铁集团', purchasingOrg: '1000', purchasingGroup: '001', companyCode: '1000', currency: 'CNY', documentDate: '2026-03-10', validFrom: '2026-03-10', validTo: '2026-06-30', totalNetValue: 256000, totalTaxAmount: 33280, totalGrossValue: 289280, status: '2', releaseStatus: '2', items: [
    { poItem: 10, materialCode: 'ROH-001', shortText: '冷轧钢板 Q235B', quantity: 5000, unit: 'KG', price: 32.00, netValue: 160000, taxCode: 'V1', taxAmount: 20800, plantCode: '1000', slocCode: '0001', deliveryDate: '2026-03-25', quantityDelivered: 5000, quantityInvoiced: 5000, itemCategory: '0' },
    { poItem: 20, materialCode: 'ROH-002', shortText: '不锈钢管 304', quantity: 2000, unit: 'KG', price: 48.00, netValue: 96000, taxCode: 'V1', taxAmount: 12480, plantCode: '1000', slocCode: '0001', deliveryDate: '2026-03-28', quantityDelivered: 2000, quantityInvoiced: 1000, itemCategory: '0' },
  ]},
  { id: 2, poNumber: '45000002', poType: 'NB', vendorCode: 'V002', vendorName: '深圳电子科技', purchasingOrg: '1000', purchasingGroup: '002', companyCode: '1000', currency: 'CNY', documentDate: '2026-03-12', validFrom: '2026-03-12', validTo: '2026-09-30', totalNetValue: 128000, totalTaxAmount: 16640, totalGrossValue: 144640, status: '2', releaseStatus: '2', items: [
    { poItem: 10, materialCode: 'HALB-001', shortText: 'PCB电路板 A型', quantity: 1000, unit: 'PCS', price: 85.00, netValue: 85000, taxCode: 'V1', taxAmount: 11050, plantCode: '2000', slocCode: '0001', deliveryDate: '2026-03-30', quantityDelivered: 600, quantityInvoiced: 0, itemCategory: '0' },
    { poItem: 20, materialCode: 'HALB-002', shortText: '电源模块 DC-12V', quantity: 500, unit: 'PCS', price: 86.00, netValue: 43000, taxCode: 'V1', taxAmount: 5590, plantCode: '2000', slocCode: '0001', deliveryDate: '2026-04-05', quantityDelivered: 0, quantityInvoiced: 0, itemCategory: '0' },
  ]},
  { id: 3, poNumber: '45000003', poType: 'NB', vendorCode: 'V003', vendorName: '北京精密机械', purchasingOrg: '1000', purchasingGroup: '001', companyCode: '1000', currency: 'CNY', documentDate: '2026-03-14', validFrom: '2026-03-14', validTo: '2026-12-31', totalNetValue: 580000, totalTaxAmount: 75400, totalGrossValue: 655400, status: '1', releaseStatus: '1', items: [
    { poItem: 10, materialCode: 'FERT-001', shortText: '精密轴承 6205-2RS', quantity: 2000, unit: 'PCS', price: 145.00, netValue: 290000, taxCode: 'V1', taxAmount: 37700, plantCode: '1000', slocCode: '0002', deliveryDate: '2026-04-10', quantityDelivered: 0, quantityInvoiced: 0, itemCategory: '0' },
    { poItem: 20, materialCode: 'FERT-002', shortText: '伺服电机 SM-750W', quantity: 100, unit: 'PCS', price: 2900.00, netValue: 290000, taxCode: 'V1', taxAmount: 37700, plantCode: '1000', slocCode: '0002', deliveryDate: '2026-04-15', quantityDelivered: 0, quantityInvoiced: 0, itemCategory: '0' },
  ]},
  { id: 4, poNumber: '45000004', poType: 'NB', vendorCode: 'V001', vendorName: '华东钢铁集团', purchasingOrg: '1000', purchasingGroup: '001', companyCode: '1000', currency: 'CNY', documentDate: '2026-03-15', validFrom: '2026-03-15', validTo: '2026-06-30', totalNetValue: 78000, totalTaxAmount: 10140, totalGrossValue: 88140, status: '0', releaseStatus: '0', items: [
    { poItem: 10, materialCode: 'ROH-003', shortText: '铝合金型材 6063', quantity: 3000, unit: 'KG', price: 26.00, netValue: 78000, taxCode: 'V1', taxAmount: 10140, plantCode: '1000', slocCode: '0001', deliveryDate: '2026-04-20', quantityDelivered: 0, quantityInvoiced: 0, itemCategory: '0' },
  ]},
  { id: 5, poNumber: '45000005', poType: 'UB', vendorCode: 'V004', vendorName: '苏州包装材料', purchasingOrg: '1000', purchasingGroup: '003', companyCode: '1000', currency: 'CNY', documentDate: '2026-03-16', validFrom: '2026-03-16', validTo: '2026-06-30', totalNetValue: 35000, totalTaxAmount: 4550, totalGrossValue: 39550, status: '2', releaseStatus: '2', items: [
    { poItem: 10, materialCode: 'VERP-001', shortText: '瓦楞纸箱 50x40x30', quantity: 5000, unit: 'PCS', price: 7.00, netValue: 35000, taxCode: 'V1', taxAmount: 4550, plantCode: '1000', slocCode: '0003', deliveryDate: '2026-03-25', quantityDelivered: 5000, quantityInvoiced: 5000, itemCategory: '0' },
  ]},
];

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
  const [orders, setOrders] = useState(mockPurchaseOrders);
  const [activeTab, setActiveTab] = useState('all');
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<typeof mockPurchaseOrders[0] | null>(null);
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
      render: (text: string, record) => (
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
      render: (_: unknown, record) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => { setSelectedOrder(record); setDetailModalVisible(true); }}>详情</Button>
          {record.status === '0' && (
            <>
              <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
              <Popconfirm title="确定提交审批？">
                <Button type="link" size="small" icon={<SendOutlined />} style={{ color: '#faad14' }}>提交</Button>
              </Popconfirm>
            </>
          )}
          {record.status === '1' && (
            <>
              <Button type="link" size="small" icon={<CheckOutlined />} style={{ color: '#52c41a' }}>审批</Button>
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
    goodsReceived: orders.filter(o => o.items.some(i => i.quantityDelivered > 0)).length,
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="采购订单管理 (对标 SAP ME21N/ME22N/ME23N)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => setLoading(!loading)}>刷新</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalVisible(true)}>
              新建采购订单
            </Button>
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
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
          scroll={{ x: 1200 }}
          pagination={{ defaultPageSize: 20, showSizeChanger: true }}
        />
      </Card>

      {/* 新建采购订单弹窗 */}
      <Modal
        title="新建采购订单"
        open={createModalVisible}
        onCancel={() => setCreateModalVisible(false)}
        onOk={() => { message.success('采购订单创建成功'); setCreateModalVisible(false); }}
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
