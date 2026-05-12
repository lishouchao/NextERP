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
  Progress,
  Timeline,
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
  ToolOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import { productionOrderApi, operationRecordApi } from '@/lib/api/production';
import type { ProProductionOrderDTO, ProOperationRecordDTO } from '@/lib/api/production';

const { RangePicker } = DatePicker;

// Default tenant ID
const DEFAULT_TENANT_ID = 1;

// 产品数据
const mockProducts = [
  { code: 'PROD-001', name: '产品A', unit: 'PCS', bomCode: 'BOM-001' },
  { code: 'PROD-002', name: '产品B', unit: 'PCS', bomCode: 'BOM-002' },
  { code: 'PROD-003', name: '配件C', unit: 'SET', bomCode: 'BOM-003' },
  { code: 'PROD-004', name: '产品D', unit: 'PCS', bomCode: 'BOM-004' },
];

export default function ProductionOrdersPage() {
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState<ProProductionOrderDTO[]>([]);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [activeTab, setActiveTab] = useState('all');
  const [orderModalVisible, setOrderModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [confirmModalVisible, setConfirmModalVisible] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<ProProductionOrderDTO | null>(null);
  const [orderOperations, setOrderOperations] = useState<ProOperationRecordDTO[]>([]);
  const [form] = Form.useForm();

  // 状态配置
  const statusConfig: Record<number, { color: string; text: string; step: number }> = {
    0: { color: 'default', text: '创建', step: 0 },
    1: { color: 'processing', text: '已下达', step: 1 },
    2: { color: 'orange', text: '准备中', step: 2 },
    3: { color: 'blue', text: '生产中', step: 3 },
    4: { color: 'green', text: '已完成', step: 4 },
    5: { color: 'cyan', text: '已确认', step: 5 },
    6: { color: 'red', text: '已关闭', step: -1 },
  };

  // 优先级配置
  const priorityConfig: Record<number, { color: string; text: string }> = {
    1: { color: 'red', text: '高' },
    2: { color: 'orange', text: '中' },
    3: { color: 'default', text: '低' },
  };

  const fetchOrders = useCallback(async (page = currentPage, size = pageSize, status?: number) => {
    try {
      setLoading(true);
      const res = await productionOrderApi.getPage({
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
      console.error('Failed to fetch production orders:', error);
      message.error('获取生产工单失败');
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize]);

  useEffect(() => {
    fetchOrders();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // 按状态筛选
  const filteredOrders = activeTab === 'all' ? orders : orders.filter(o => {
    if (activeTab === 'released') return o.status === 1;
    if (activeTab === 'in_progress') return o.status === 3;
    if (activeTab === 'completed') return o.status >= 4;
    return true;
  });

  // 生产工单列
  const columns = [
    { title: '工单号', dataIndex: 'orderNo', key: 'orderNo', width: 120, fixed: 'left' as const,
      render: (text: string, record: any) => (
        <a onClick={() => { setSelectedOrder(record); setDetailModalVisible(true); }}>{text}</a>
      ),
    },
    { title: '产品编码', dataIndex: 'productCode', key: 'productCode', width: 100 },
    { title: '产品名称', dataIndex: 'productName', key: 'productName', width: 100 },
    { title: '计划数量', dataIndex: 'quantity', key: 'quantity', width: 100, align: 'right' as const,
      render: (v: number, r: any) => `${v} ${r.unit}`,
    },
    { title: '完成数量', dataIndex: 'completedQty', key: 'completedQty', width: 100, align: 'right' as const,
      render: (v: number, r: any) => {
        const percent = (v / r.quantity) * 100;
        return (
          <Tooltip title={`${v}/${r.quantity} (${percent.toFixed(1)}%)`}>
            <Progress percent={Math.round(percent)} size="small" style={{ width: 80 }} />
          </Tooltip>
        );
      },
    },
    { title: '计划开始', dataIndex: 'plannedStart', key: 'plannedStart', width: 100 },
    { title: '计划结束', dataIndex: 'plannedEnd', key: 'plannedEnd', width: 100,
      render: (date: string, record: any) => {
        const isOverdue = dayjs(date).isBefore(dayjs(), 'day') && record.status < 4;
        return <span style={{ color: isOverdue ? '#ff4d4f' : 'inherit' }}>{date}</span>;
      },
    },
    { title: '车间', dataIndex: 'workshop', key: 'workshop', width: 80 },
    { title: '工作中心', dataIndex: 'workCenter', key: 'workCenter', width: 90 },
    { title: '优先级', dataIndex: 'priority', key: 'priority', width: 70,
      render: (priority: number) => {
        const config = priorityConfig[priority];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80,
      render: (status: number) => {
        const config = statusConfig[status];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    { title: '操作', key: 'action', width: 200, fixed: 'right' as const,
      render: (_: unknown, record: any) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => { setSelectedOrder(record); setDetailModalVisible(true); }}>详情</Button>
          {record.status === 0 && (
            <>
              <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
              <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
            </>
          )}
          {record.status === 1 && (
            <Button type="link" size="small" icon={<PlayCircleOutlined />} style={{ color: '#52c41a' }} onClick={async () => {
              try {
                const res = await productionOrderApi.start(record.id);
                if (res.success) {
                  message.success('工单已开工');
                  fetchOrders();
                } else {
                  message.error(res.message || '开工失败');
                }
              } catch {
                message.error('开工失败');
              }
            }}>开工</Button>
          )}
          {record.status === 3 && (
            <Button type="link" size="small" icon={<CheckOutlined />} style={{ color: '#1890ff' }}>报工</Button>
          )}
        </Space>
      ),
    },
  ];

  // 统计
  const stats = {
    totalOrders: orders.length,
    inProgress: orders.filter(o => o.status === 3).length,
    completed: orders.filter(o => o.status >= 4).length,
    pending: orders.filter(o => o.status <= 1).length,
    totalQty: orders.reduce((s, o) => s + o.quantity, 0),
    completedQty: orders.reduce((s, o) => s + o.completedQty, 0),
  };

  const handleTabChange = (key: string) => {
    setActiveTab(key);
    setCurrentPage(1);
    const status = key === 'all' ? undefined : key === 'released' ? 1 : key === 'in_progress' ? 3 : key === 'completed' ? 4 : undefined;
    fetchOrders(1, pageSize, status);
  };

  const handleViewDetail = async (record: ProProductionOrderDTO) => {
    setSelectedOrder(record);
    setDetailModalVisible(true);
    // Fetch operations for this order
    try {
      const res = await operationRecordApi.getByOrderId(record.id);
      if (res.success && res.data) {
        setOrderOperations(res.data);
      }
    } catch {
      setOrderOperations([]);
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="生产工单管理 (对标 SAP CO01/CO02/CO03)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => fetchOrders()}>刷新</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setOrderModalVisible(true)}>
              新建工单
            </Button>
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={handleTabChange}
          items={[
            { key: 'all', label: <><ToolOutlined /> 全部工单</> },
            { key: 'released', label: <>已下达</> },
            { key: 'in_progress', label: <>生产中</> },
            { key: 'completed', label: <>已完成</> },
          ]}
        />

        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={4}>
            <Card size="small">
              <Statistic title="工单总数" value={stats.totalOrders} suffix="单" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="生产中" value={stats.inProgress} suffix="单" valueStyle={{ fontSize: 18, color: '#1890ff' }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="已完成" value={stats.completed} suffix="单" valueStyle={{ fontSize: 18, color: '#52c41a' }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="待处理" value={stats.pending} suffix="单" valueStyle={{ fontSize: 18, color: '#faad14' }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="计划产量" value={stats.totalQty} suffix="件" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="完成率" value={(stats.completedQty / stats.totalQty * 100).toFixed(1)} suffix="%" valueStyle={{ fontSize: 18, color: '#13c2c2' }} />
            </Card>
          </Col>
        </Row>

        {/* 搜索表单 */}
        <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="orderNo" label="工单号">
            <Input placeholder="工单号" style={{ width: 120 }} />
          </Form.Item>
          <Form.Item name="productCode" label="产品">
            <Input placeholder="产品编码" style={{ width: 120 }} />
          </Form.Item>
          <Form.Item name="dateRange" label="计划日期">
            <RangePicker style={{ width: 240 }} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select placeholder="全部" allowClear style={{ width: 100 }}
              options={Object.entries(statusConfig).map(([k, v]) => ({ value: Number(k), label: v.text }))} />
          </Form.Item>
          <Form.Item name="workshop" label="车间">
            <Select placeholder="全部" allowClear style={{ width: 100 }}
              options={[
                { value: '车间A', label: '车间A' },
                { value: '车间B', label: '车间B' },
              ]} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SearchOutlined />}>查询</Button>
          </Form.Item>
        </Form>

        {/* 工单表格 */}
        <Table
          columns={columns}
          dataSource={filteredOrders}
          rowKey="id"
          loading={loading}
          size="small"
          scroll={{ x: 1400 }}
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

      {/* 新建工单弹窗 */}
      <Modal
        title="新建生产工单"
        open={orderModalVisible}
        onCancel={() => setOrderModalVisible(false)}
        onOk={() => { message.success('工单创建成功'); setOrderModalVisible(false); fetchOrders(); }}
        width={800}
      >
        <Form layout="vertical">
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="产品" required>
                <Select
                  placeholder="选择产品"
                  showSearch
                  optionFilterProp="label"
                  options={mockProducts.map(p => ({ value: p.code, label: `${p.code} - ${p.name}` }))}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="计划数量" required>
                <InputNumber style={{ width: '100%' }} min={1} placeholder="计划数量" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="单位">
                <Input placeholder="自动带出" disabled />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="计划开始日期" required>
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="计划结束日期" required>
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="优先级">
                <Select placeholder="选择优先级" options={[
                  { value: 1, label: '高' },
                  { value: 2, label: '中' },
                  { value: 3, label: '低' },
                ]} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="车间">
                <Select placeholder="选择车间" options={[
                  { value: '车间A', label: '车间A' },
                  { value: '车间B', label: '车间B' },
                ]} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="工作中心">
                <Select placeholder="选择工作中心" options={[
                  { value: 'WC-01', label: 'WC-01' },
                  { value: 'WC-02', label: 'WC-02' },
                  { value: 'WC-03', label: 'WC-03' },
                  { value: 'WC-04', label: 'WC-04' },
                ]} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item label="备注">
            <Input.TextArea rows={2} placeholder="输入备注信息" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 工单详情弹窗 */}
      <Modal
        title={`工单详情 - ${selectedOrder?.orderNo}`}
        open={detailModalVisible}
        onCancel={() => { setDetailModalVisible(false); setSelectedOrder(null); setOrderOperations([]); }}
        footer={[
          <Button key="print">打印</Button>,
          selectedOrder?.status === 3 && <Button key="confirm" type="primary" icon={<CheckOutlined />}>报工确认</Button>,
          <Button key="close" onClick={() => { setDetailModalVisible(false); setSelectedOrder(null); setOrderOperations([]); }}>关闭</Button>,
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
                  { title: '下达', status: selectedOrder.status >= 1 ? 'finish' : 'wait' },
                  { title: '生产', status: selectedOrder.status >= 3 ? 'finish' : 'wait' },
                  { title: '完成', status: selectedOrder.status >= 4 ? 'finish' : 'wait' },
                  { title: '确认', status: selectedOrder.status >= 5 ? 'finish' : 'wait' },
                ]}
              />
            </Card>

            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={16}>
                <Descriptions bordered size="small" column={2}>
                  <Descriptions.Item label="工单号">{selectedOrder.orderNo}</Descriptions.Item>
                  <Descriptions.Item label="状态">
                    <Tag color={statusConfig[selectedOrder.status]?.color}>{statusConfig[selectedOrder.status]?.text}</Tag>
                  </Descriptions.Item>
                  <Descriptions.Item label="产品编码">{selectedOrder.productCode}</Descriptions.Item>
                  <Descriptions.Item label="产品名称">{selectedOrder.productName}</Descriptions.Item>
                  <Descriptions.Item label="计划数量">{selectedOrder.quantity} {selectedOrder.unit}</Descriptions.Item>
                  <Descriptions.Item label="完成数量">{selectedOrder.completedQty} {selectedOrder.unit}</Descriptions.Item>
                  <Descriptions.Item label="计划开始">{selectedOrder.plannedStart}</Descriptions.Item>
                  <Descriptions.Item label="计划结束">{selectedOrder.plannedEnd}</Descriptions.Item>
                  <Descriptions.Item label="实际开始">{selectedOrder.actualStart || '-'}</Descriptions.Item>
                  <Descriptions.Item label="实际结束">{selectedOrder.actualEnd || '-'}</Descriptions.Item>
                  <Descriptions.Item label="车间">{selectedOrder.workshop}</Descriptions.Item>
                  <Descriptions.Item label="工作中心">{selectedOrder.workCenter}</Descriptions.Item>
                </Descriptions>
              </Col>
              <Col span={8}>
                <Card size="small" style={{ background: '#f6f8fa' }}>
                  <Statistic title="完成进度" value={selectedOrder.completedQty} suffix={`/ ${selectedOrder.quantity} ${selectedOrder.unit}`} />
                  <Progress
                    percent={Math.round((selectedOrder.completedQty / selectedOrder.quantity) * 100)}
                    style={{ marginTop: 16 }}
                    status={selectedOrder.completedQty >= selectedOrder.quantity ? 'success' : 'active'}
                  />
                </Card>
              </Col>
            </Row>

            <Card title="工序明细" size="small">
              <Table
                columns={[
                  { title: '工序号', dataIndex: 'sequenceNo', width: 80 },
                  { title: '工序名称', dataIndex: 'operationName', width: 100 },
                  { title: '工作中心', dataIndex: 'workCenter', width: 100 },
                  { title: '准备时间', dataIndex: 'actualManHours', width: 100, render: (v) => v ? `${v} 分钟` : '-' },
                  { title: '加工时间', dataIndex: 'actualMachineHours', width: 100, render: (v) => v ? `${v} 分钟` : '-' },
                  { title: '完成数量', dataIndex: 'completedQty', width: 100 },
                  {
                    title: '状态', dataIndex: 'status', width: 80,
                    render: (status: number) => {
                      const map: Record<number, { color: string; text: string }> = {
                        0: { color: 'default', text: '待加工' },
                        1: { color: 'processing', text: '加工中' },
                        2: { color: 'green', text: '已完成' },
                      };
                      const s = map[status] || { color: 'default', text: '未知' };
                      return <Tag color={s.color}>{s.text}</Tag>;
                    },
                  },
                  { title: '开始时间', dataIndex: 'startTime', width: 140, render: (v) => v || '-' },
                  { title: '结束时间', dataIndex: 'endTime', width: 140, render: (v) => v || '-' },
                ]}
                dataSource={orderOperations}
                rowKey="id"
                size="small"
                pagination={false}
                locale={{ emptyText: '暂无工序记录' }}
              />
            </Card>
          </>
        )}
      </Modal>
    </div>
  );
}
