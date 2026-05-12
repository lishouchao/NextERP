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
  Tooltip,
  Timeline,
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  CheckOutlined,
  TruckOutlined,
  ExportOutlined,
  SendOutlined,
  CloseCircleOutlined,
  InboxOutlined,
  RightOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import { deliveryApi } from '@/lib/api/sales';

const { RangePicker } = DatePicker;

// 默认租户ID
const DEFAULT_TENANT_ID = 1;

// 交货类型配置
const deliveryTypeConfig: Record<string, { text: string; color: string }> = {
  LF: { text: '出库交货', color: 'blue' },
  LR: { text: '退货交货', color: 'orange' },
  LO: { text: '无订单交货', color: 'purple' },
  NL: { text: '补货交货', color: 'cyan' },
};

// 拣配状态配置
const pickingStatusConfig: Record<string, { text: string; color: string }> = {
  A: { text: '未拣配', color: 'default' },
  B: { text: '部分拣配', color: 'processing' },
  C: { text: '完全拣配', color: 'green' },
};

// 发货过账状态配置
const giStatusConfig: Record<string, { text: string; color: string }> = {
  A: { text: '未过账', color: 'default' },
  B: { text: '已过账', color: 'green' },
};

// 交货状态配置
const deliveryStatusConfig: Record<string, { text: string; color: string; step: number }> = {
  '01': { text: '已创建', color: 'default', step: 0 },
  '02': { text: '拣配中', color: 'processing', step: 1 },
  '03': { text: '已发货', color: 'blue', step: 2 },
  '04': { text: '已完成', color: 'green', step: 3 },
};

// 装运点
const shippingPoints = [
  { value: 'SP01', label: 'SP01 - 主仓库' },
  { value: 'SP02', label: 'SP02 - 副仓库' },
  { value: 'SP03', label: 'SP03 - 外协仓库' },
];

export default function DeliveriesPage() {
  const [loading, setLoading] = useState(false);
  const [deliveries, setDeliveries] = useState<any[]>([]);
  const [activeTab, setActiveTab] = useState('all');
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedDelivery, setSelectedDelivery] = useState<any | null>(null);
  const [form] = Form.useForm();

  // 加载交货单数据
  const fetchDeliveries = useCallback(async () => {
    setLoading(true);
    try {
      const res = await deliveryApi.getList({ tenantId: DEFAULT_TENANT_ID, current: 1, size: 100 });
      if (res.data) {
        setDeliveries(res.data.records || []);
      }
    } catch (error) {
      console.error('获取交货单列表失败:', error);
      message.error('获取交货单列表失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDeliveries();
  }, [fetchDeliveries]);

  // 按状态筛选
  const filteredDeliveries = activeTab === 'all' ? deliveries : deliveries.filter(d => {
    if (activeTab === 'pending') return d.deliveryStatus === '01';
    if (activeTab === 'picking') return d.deliveryStatus === '02';
    if (activeTab === 'shipped') return d.deliveryStatus === '03' || d.deliveryStatus === '04';
    return true;
  });

  // 统计
  const stats = {
    total: deliveries.length,
    pendingPicking: deliveries.filter(d => d.pickingStatus === 'A').length,
    pendingGi: deliveries.filter(d => d.giStatus === 'A' && d.pickingStatus === 'C').length,
    completed: deliveries.filter(d => d.deliveryStatus === '04').length,
  };

  // 交货列表列
  const columns = [
    {
      title: '交货单号',
      dataIndex: 'deliveryNumber',
      key: 'deliveryNumber',
      width: 140,
      fixed: 'left' as const,
      render: (text: string, record: any) => (
        <a onClick={() => { setSelectedDelivery(record); setDetailModalVisible(true); }}>{text}</a>
      ),
    },
    {
      title: '交货类型',
      dataIndex: 'deliveryType',
      key: 'deliveryType',
      width: 100,
      render: (type: string) => {
        const config = deliveryTypeConfig[type];
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
      title: '单据日期',
      dataIndex: 'documentDate',
      key: 'documentDate',
      width: 110,
    },
    {
      title: '计划发货日',
      dataIndex: 'plannedGiDate',
      key: 'plannedGiDate',
      width: 110,
      render: (date: string, record: any) => {
        const isOverdue = dayjs(date).isBefore(dayjs(), 'day') && record.deliveryStatus !== '04';
        return <span style={{ color: isOverdue ? '#ff4d4f' : 'inherit' }}>{date}</span>;
      },
    },
    {
      title: '总重量',
      dataIndex: 'totalWeight',
      key: 'totalWeight',
      width: 100,
      align: 'right' as const,
      render: (v: number, record: any) => `${v?.toFixed(1) || '0.0'} ${record.weightUnit}`,
    },
    {
      title: '拣配状态',
      dataIndex: 'pickingStatus',
      key: 'pickingStatus',
      width: 100,
      render: (status: string) => {
        const config = pickingStatusConfig[status];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    {
      title: '发货过账',
      dataIndex: 'giStatus',
      key: 'giStatus',
      width: 90,
      render: (status: string) => {
        const config = giStatusConfig[status];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    {
      title: '交货状态',
      dataIndex: 'deliveryStatus',
      key: 'deliveryStatus',
      width: 100,
      render: (status: string) => {
        const config = deliveryStatusConfig[status];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    {
      title: '装运点',
      dataIndex: 'shippingPoint',
      key: 'shippingPoint',
      width: 90,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right' as const,
      render: (_: unknown, record: any) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => { setSelectedDelivery(record); setDetailModalVisible(true); }}>
            详情
          </Button>
          {record.pickingStatus === 'A' && (
            <Tooltip title="确认拣配">
              <Button type="link" size="small" icon={<InboxOutlined />} style={{ color: '#1890ff' }} onClick={async () => {
                try {
                  await deliveryApi.pick(record.id, []);
                  message.success('拣配成功');
                  fetchDeliveries();
                } catch (error) { message.error('拣配失败'); }
              }}>
                拣配
              </Button>
            </Tooltip>
          )}
          {record.pickingStatus === 'C' && record.giStatus === 'A' && (
            <Tooltip title="发货过账">
              <Button type="link" size="small" icon={<SendOutlined />} style={{ color: '#52c41a' }} onClick={async () => {
                try {
                  await deliveryApi.postGoodsIssue(record.id, dayjs().format('YYYY-MM-DD'));
                  message.success('发货过账成功');
                  fetchDeliveries();
                } catch (error) { message.error('发货过账失败'); }
              }}>
                过账
              </Button>
            </Tooltip>
          )}
          {record.deliveryStatus !== '04' && (
            <Tooltip title="取消交货">
              <Button type="link" size="small" danger icon={<CloseCircleOutlined />} onClick={async () => {
                try {
                  await deliveryApi.cancel(record.id);
                  message.success('交货单已取消');
                  fetchDeliveries();
                } catch (error) { message.error('取消交货单失败'); }
              }}>
                取消
              </Button>
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card
        title="交货管理 (对标 SAP VL01N/VL02N)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={fetchDeliveries}>刷新</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalVisible(true)}>
              新建交货单
            </Button>
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            { key: 'all', label: <><TruckOutlined /> 全部交货</> },
            { key: 'pending', label: <>待处理</> },
            { key: 'picking', label: <>拣配中</> },
            { key: 'shipped', label: <>已发货</> },
          ]}
        />

        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card size="small">
              <Statistic title="交货总数" value={stats.total} suffix="单" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="待拣配" value={stats.pendingPicking} suffix="单" valueStyle={{ fontSize: 18, color: '#faad14' }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="待发货过账" value={stats.pendingGi} suffix="单" valueStyle={{ fontSize: 18, color: '#1890ff' }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="已完成" value={stats.completed} suffix="单" valueStyle={{ fontSize: 18, color: '#52c41a' }} />
            </Card>
          </Col>
        </Row>

        {/* 搜索表单 */}
        <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="deliveryNumber" label="交货单号">
            <Input placeholder="交货单号" style={{ width: 140 }} />
          </Form.Item>
          <Form.Item name="customerName" label="客户">
            <Input placeholder="客户名称" style={{ width: 130 }} />
          </Form.Item>
          <Form.Item name="deliveryType" label="交货类型">
            <Select placeholder="全部" allowClear style={{ width: 140 }}
              options={Object.entries(deliveryTypeConfig).map(([k, v]) => ({ value: k, label: `${k} - ${v.text}` }))} />
          </Form.Item>
          <Form.Item name="dateRange" label="日期">
            <RangePicker style={{ width: 240 }} />
          </Form.Item>
          <Form.Item name="pickingStatus" label="拣配">
            <Select placeholder="全部" allowClear style={{ width: 110 }}
              options={Object.entries(pickingStatusConfig).map(([k, v]) => ({ value: k, label: v.text }))} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SearchOutlined />}>查询</Button>
          </Form.Item>
        </Form>

        {/* 交货表格 */}
        <Table
          columns={columns}
          dataSource={filteredDeliveries}
          rowKey="id"
          loading={loading}
          size="small"
          scroll={{ x: 1500 }}
          pagination={{ defaultPageSize: 20, showSizeChanger: true }}
        />
      </Card>

      {/* 新建交货单弹窗 */}
      <Modal
        title="新建交货单"
        open={createModalVisible}
        onCancel={() => setCreateModalVisible(false)}
        onOk={async () => {
          try {
            message.success('交货单创建成功');
            setCreateModalVisible(false);
            fetchDeliveries();
          } catch (error) {
            message.error('创建交货单失败');
          }
        }}
        width={900}
      >
        <Form layout="vertical">
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="交货类型" required>
                <Select placeholder="选择交货类型" options={Object.entries(deliveryTypeConfig).map(([k, v]) => ({ value: k, label: `${k} - ${v.text}` }))} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="来源订单">
                <Select placeholder="选择销售订单（可选）" showSearch optionFilterProp="label"
                  options={[]} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="装运点" required>
                <Select placeholder="选择装运点" options={shippingPoints} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="单据日期" required>
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="计划发货日期" required>
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="客户">
                <Input placeholder="客户自动带出" disabled />
              </Form.Item>
            </Col>
          </Row>
          <Card title="交货行项目" size="small">
            <Table
              columns={[
                { title: '物料编码', dataIndex: 'materialCode', width: 110 },
                { title: '物料描述', dataIndex: 'materialName', width: 140 },
                { title: '订单数量', dataIndex: 'orderQty', width: 90 },
                { title: '交货数量', dataIndex: 'deliveryQty', width: 90,
                  render: () => <InputNumber size="small" min={0} style={{ width: 80 }} />,
                },
                { title: '单位', dataIndex: 'unit', width: 60 },
                { title: '批次', dataIndex: 'batch', width: 120,
                  render: () => <Input size="small" placeholder="批次号" />,
                },
                { title: '操作', width: 60, render: () => <Button type="link" size="small" danger>删除</Button> },
              ]}
              dataSource={[]}
              size="small"
              pagination={false}
              locale={{ emptyText: '请先选择销售订单或手动添加物料' }}
            />
            <Button type="dashed" block style={{ marginTop: 16 }} icon={<PlusOutlined />}>
              添加物料
            </Button>
          </Card>
        </Form>
      </Modal>

      {/* 交货详情弹窗 */}
      <Modal
        title={`交货单详情 - ${selectedDelivery?.deliveryNumber}`}
        open={detailModalVisible}
        onCancel={() => { setDetailModalVisible(false); setSelectedDelivery(null); }}
        footer={[
          <Button key="print">打印交货单</Button>,
          <Button key="close" type="primary" onClick={() => { setDetailModalVisible(false); setSelectedDelivery(null); }}>关闭</Button>,
        ]}
        width={1000}
      >
        {selectedDelivery && (
          <>
            {/* 流程步骤 */}
            <Card size="small" style={{ marginBottom: 16 }}>
              <Steps
                size="small"
                current={deliveryStatusConfig[selectedDelivery.deliveryStatus]?.step || 0}
                items={[
                  { title: '创建', description: selectedDelivery.createdAt },
                  { title: '拣配', status: selectedDelivery.pickingStatus === 'C' ? 'finish' : selectedDelivery.pickingStatus === 'B' ? 'process' : 'wait' },
                  { title: '发货', status: selectedDelivery.giStatus === 'B' ? 'finish' : 'wait' },
                  { title: '完成', status: selectedDelivery.deliveryStatus === '04' ? 'finish' : 'wait' },
                ]}
              />
            </Card>

            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={16}>
                <Descriptions bordered size="small" column={2}>
                  <Descriptions.Item label="交货单号">{selectedDelivery.deliveryNumber}</Descriptions.Item>
                  <Descriptions.Item label="交货类型">
                    <Tag color={deliveryTypeConfig[selectedDelivery.deliveryType]?.color}>
                      {selectedDelivery.deliveryType} - {deliveryTypeConfig[selectedDelivery.deliveryType]?.text}
                    </Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="客户">{selectedDelivery.customerName}</Descriptions.Item>
                  <Descriptions.Item label="来源订单">{selectedDelivery.salesOrder || '无'}</Descriptions.Item>
                  <Descriptions.Item label="单据日期">{selectedDelivery.documentDate}</Descriptions.Item>
                  <Descriptions.Item label="计划发货日">{selectedDelivery.plannedGiDate}</Descriptions.Item>
                  <Descriptions.Item label="实际发货日">{selectedDelivery.actualGiDate || '-'}</Descriptions.Item>
                  <Descriptions.Item label="装运点">{selectedDelivery.shippingPoint}</Descriptions.Item>
                  <Descriptions.Item label="总重量">{`${selectedDelivery.totalWeight} ${selectedDelivery.weightUnit}`}</Descriptions.Item>
                  <Descriptions.Item label="创建人">{selectedDelivery.createdBy}</Descriptions.Item>
                </Descriptions>
              </Col>
              <Col span={8}>
                <Card size="small" style={{ background: '#f6f8fa', height: '100%' }}>
                  <Space direction="vertical" style={{ width: '100%' }} size="middle">
                    <div>
                      <span style={{ color: '#8c8c8c', fontSize: 12 }}>拣配状态</span><br />
                      <Tag color={pickingStatusConfig[selectedDelivery.pickingStatus]?.color} style={{ marginTop: 4 }}>
                        {pickingStatusConfig[selectedDelivery.pickingStatus]?.text}
                      </Tag>
                    </div>
                    <div>
                      <span style={{ color: '#8c8c8c', fontSize: 12 }}>发货过账</span><br />
                      <Tag color={giStatusConfig[selectedDelivery.giStatus]?.color} style={{ marginTop: 4 }}>
                        {giStatusConfig[selectedDelivery.giStatus]?.text}
                      </Tag>
                    </div>
                    <div>
                      <span style={{ color: '#8c8c8c', fontSize: 12 }}>交货状态</span><br />
                      <Tag color={deliveryStatusConfig[selectedDelivery.deliveryStatus]?.color} style={{ marginTop: 4 }}>
                        {deliveryStatusConfig[selectedDelivery.deliveryStatus]?.text}
                      </Tag>
                    </div>
                  </Space>
                </Card>
              </Col>
            </Row>

            {/* 交货明细 */}
            <Card title="交货行项目" size="small">
              <Table
                columns={[
                  { title: '物料编码', dataIndex: 'materialCode', width: 110 },
                  { title: '物料描述', dataIndex: 'materialName', width: 150 },
                  { title: '订单数量', dataIndex: 'orderQty', width: 100, align: 'right' as const,
                    render: (v: number, r: any) => `${v} ${r.unit}`,
                  },
                  { title: '已交数量', dataIndex: 'deliveredQty', width: 100, align: 'right' as const,
                    render: (v: number, r: any) => `${v} ${r.unit}`,
                  },
                  { title: '批次', dataIndex: 'batch', width: 120, render: (v: string) => v || '-' },
                  {
                    title: '状态', width: 80,
                    render: (_: unknown, r: any) => {
                      if (r.deliveredQty === 0) return <Tag color="default">未拣配</Tag>;
                      if (r.deliveredQty < r.orderQty) return <Tag color="processing">部分拣配</Tag>;
                      return <Tag color="green">完全拣配</Tag>;
                    },
                  },
                ]}
                dataSource={selectedDelivery.items}
                rowKey="materialCode"
                size="small"
                pagination={false}
                summary={(data) => {
                  const totalWeight = selectedDelivery.totalWeight;
                  return (
                    <Table.Summary.Row>
                      <Table.Summary.Cell index={0} colSpan={3} align="right"><strong>总重量:</strong></Table.Summary.Cell>
                      <Table.Summary.Cell index={1} colSpan={3}>
                        <strong style={{ color: '#1890ff' }}>{totalWeight} {selectedDelivery.weightUnit}</strong>
                      </Table.Summary.Cell>
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
