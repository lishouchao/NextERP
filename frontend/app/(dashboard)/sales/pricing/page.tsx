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
  Descriptions,
  message,
  Tooltip,
  Popconfirm,
  Alert,
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  CheckOutlined,
  DollarOutlined,
  TagsOutlined,
  HistoryOutlined,
  CalendarOutlined,
  CopyOutlined,
  ExclamationCircleOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;

// 条件类型配置
const conditionTypeConfig: Record<string, { text: string; color: string; category: string }> = {
  PR00: { text: '基础价格', color: 'blue', category: '价格' },
  K004: { text: '客户折扣', color: 'green', category: '折扣' },
  K005: { text: '物料折扣', color: 'cyan', category: '折扣' },
  K007: { text: '附加费', color: 'purple', category: '附加' },
  MWST: { text: '增值税', color: 'orange', category: '税' },
  SKTO: { text: '现金折扣', color: 'gold', category: '折扣' },
};

// 计算类型配置
const calculationTypeConfig: Record<string, { text: string; color: string }> = {
  A: { text: '百分比', color: 'blue' },
  B: { text: '固定金额', color: 'green' },
  C: { text: '数量依赖', color: 'purple' },
};

// 模拟条件记录数据
const mockConditions = [
  {
    id: 1,
    conditionType: 'PR00',
    conditionDesc: '基础价格',
    customerCode: 'CUST-001',
    customerName: '北京科技有限公司',
    materialCode: 'MAT-001',
    materialName: '精密轴承 A-100',
    amount: 450.00,
    rate: null,
    currency: 'CNY',
    calculationType: 'B',
    validFrom: '2024-01-01',
    validTo: '2024-12-31',
    pricingScale: [{ fromQty: 0, toQty: 99, value: 450.00 }, { fromQty: 100, toQty: 499, value: 420.00 }, { fromQty: 500, toQty: 9999, value: 380.00 }],
    createdBy: '价格管理员A',
    createdAt: '2024-01-01 09:00:00',
    updatedBy: null,
    updatedAt: null,
  },
  {
    id: 2,
    conditionType: 'PR00',
    conditionDesc: '基础价格',
    customerCode: 'CUST-002',
    customerName: '上海贸易集团',
    materialCode: 'MAT-004',
    materialName: '工业电机 M-500',
    amount: 3200.00,
    rate: null,
    currency: 'CNY',
    calculationType: 'B',
    validFrom: '2024-01-01',
    validTo: '2024-06-30',
    pricingScale: [{ fromQty: 0, toQty: 19, value: 3200.00 }, { fromQty: 20, toQty: 99, value: 3050.00 }],
    createdBy: '价格管理员A',
    createdAt: '2024-01-01 10:00:00',
    updatedBy: null,
    updatedAt: null,
  },
  {
    id: 3,
    conditionType: 'K004',
    conditionDesc: '客户折扣',
    customerCode: 'CUST-001',
    customerName: '北京科技有限公司',
    materialCode: '*',
    materialName: '全部物料',
    amount: null,
    rate: 5.0,
    currency: 'CNY',
    calculationType: 'A',
    validFrom: '2024-01-01',
    validTo: '2024-12-31',
    pricingScale: [],
    createdBy: '价格管理员B',
    createdAt: '2024-01-02 08:30:00',
    updatedBy: null,
    updatedAt: null,
  },
  {
    id: 4,
    conditionType: 'K005',
    conditionDesc: '物料折扣',
    customerCode: '*',
    customerName: '全部客户',
    materialCode: 'MAT-001',
    materialName: '精密轴承 A-100',
    amount: null,
    rate: 3.0,
    currency: 'CNY',
    calculationType: 'A',
    validFrom: '2024-03-01',
    validTo: '2024-05-31',
    pricingScale: [],
    createdBy: '价格管理员A',
    createdAt: '2024-02-25 14:00:00',
    updatedBy: null,
    updatedAt: null,
  },
  {
    id: 5,
    conditionType: 'MWST',
    conditionDesc: '增值税',
    customerCode: '*',
    customerName: '全部客户',
    materialCode: '*',
    materialName: '全部物料',
    amount: null,
    rate: 13.0,
    currency: 'CNY',
    calculationType: 'A',
    validFrom: '2024-01-01',
    validTo: '9999-12-31',
    pricingScale: [],
    createdBy: '系统管理员',
    createdAt: '2024-01-01 00:00:00',
    updatedBy: null,
    updatedAt: null,
  },
  {
    id: 6,
    conditionType: 'K007',
    conditionDesc: '附加费',
    customerCode: 'CUST-003',
    customerName: '广州制造企业',
    materialCode: 'MAT-006',
    materialName: '液压阀门 H-800',
    amount: 150.00,
    rate: null,
    currency: 'CNY',
    calculationType: 'B',
    validFrom: '2024-02-01',
    validTo: '2024-07-31',
    pricingScale: [],
    createdBy: '价格管理员B',
    createdAt: '2024-01-28 11:00:00',
    updatedBy: null,
    updatedAt: null,
  },
  {
    id: 7,
    conditionType: 'SKTO',
    conditionDesc: '现金折扣',
    customerCode: 'CUST-002',
    customerName: '上海贸易集团',
    materialCode: '*',
    materialName: '全部物料',
    amount: null,
    rate: 2.0,
    currency: 'CNY',
    calculationType: 'A',
    validFrom: '2024-01-01',
    validTo: '2024-12-31',
    pricingScale: [],
    createdBy: '价格管理员A',
    createdAt: '2024-01-01 10:00:00',
    updatedBy: '价格管理员B',
    updatedAt: '2024-02-15 16:00:00',
  },
  {
    id: 8,
    conditionType: 'PR00',
    conditionDesc: '基础价格',
    customerCode: 'CUST-004',
    customerName: '深圳电子公司',
    materialCode: 'MAT-008',
    materialName: '电源模块 PM-600',
    amount: 1900.00,
    rate: null,
    currency: 'CNY',
    calculationType: 'B',
    validFrom: '2024-01-15',
    validTo: '2024-03-14',
    pricingScale: [],
    createdBy: '价格管理员A',
    createdAt: '2024-01-15 09:30:00',
    updatedBy: null,
    updatedAt: null,
  },
];

export default function PricingPage() {
  const [loading, setLoading] = useState(false);
  const [conditions, setConditions] = useState(mockConditions);
  const [activeTab, setActiveTab] = useState('all');
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedCondition, setSelectedCondition] = useState<typeof mockConditions[0] | null>(null);
  const [form] = Form.useForm();

  // 判断是否过期
  const isExpired = (validTo: string) => dayjs(validTo).isBefore(dayjs(), 'day');
  const isExpiringSoon = (validTo: string) => {
    const diff = dayjs(validTo).diff(dayjs(), 'day');
    return diff > 0 && diff <= 30;
  };

  // 按类型筛选
  const filteredConditions = activeTab === 'all' ? conditions : conditions.filter(c => {
    if (activeTab === 'PR00') return c.conditionType === 'PR00';
    if (activeTab === 'K004') return c.conditionType === 'K004' || c.conditionType === 'K005' || c.conditionType === 'SKTO';
    if (activeTab === 'MWST') return c.conditionType === 'MWST';
    if (activeTab === 'K007') return c.conditionType === 'K007';
    return true;
  });

  // 统计
  const stats = {
    total: conditions.length,
    active: conditions.filter(c => !isExpired(c.validTo)).length,
    expiringSoon: conditions.filter(c => isExpiringSoon(c.validTo)).length,
    expired: conditions.filter(c => isExpired(c.validTo)).length,
  };

  // 条件列表列
  const columns = [
    {
      title: '条件类型',
      dataIndex: 'conditionType',
      key: 'conditionType',
      width: 110,
      fixed: 'left' as const,
      render: (type: string) => {
        const config = conditionTypeConfig[type];
        return <Tag color={config?.color}>{type} - {config?.text}</Tag>;
      },
    },
    {
      title: '客户',
      dataIndex: 'customerName',
      key: 'customerName',
      width: 140,
      render: (name: string) => name === '全部客户' ? <span style={{ color: '#8c8c8c' }}>{name}</span> : name,
    },
    {
      title: '物料',
      dataIndex: 'materialName',
      key: 'materialName',
      width: 140,
      render: (name: string) => name === '全部物料' ? <span style={{ color: '#8c8c8c' }}>{name}</span> : name,
    },
    {
      title: '金额/费率',
      key: 'value',
      width: 120,
      align: 'right' as const,
      render: (_: unknown, record: typeof mockConditions[0]) => {
        if (record.calculationType === 'A') {
          return <span style={{ color: '#1890ff', fontWeight: 'bold' }}>{record.rate}%</span>;
        }
        return <span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{record.amount?.toFixed(2)}</span>;
      },
    },
    {
      title: '计算类型',
      dataIndex: 'calculationType',
      key: 'calculationType',
      width: 90,
      render: (type: string) => {
        const config = calculationTypeConfig[type];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    {
      title: '生效日期',
      dataIndex: 'validFrom',
      key: 'validFrom',
      width: 110,
    },
    {
      title: '失效日期',
      dataIndex: 'validTo',
      key: 'validTo',
      width: 110,
      render: (date: string) => {
        if (isExpired(date)) return <span style={{ color: '#ff4d4f' }}>{date}</span>;
        if (isExpiringSoon(date)) return <span style={{ color: '#faad14' }}>{date}</span>;
        return date;
      },
    },
    {
      title: '状态',
      key: 'status',
      width: 80,
      render: (_: unknown, record: typeof mockConditions[0]) => {
        if (isExpired(record.validTo)) return <Tag color="red">已失效</Tag>;
        if (isExpiringSoon(record.validTo)) return <Tag color="orange">即将到期</Tag>;
        return <Tag color="green">有效</Tag>;
      },
    },
    {
      title: '创建人',
      dataIndex: 'createdBy',
      key: 'createdBy',
      width: 110,
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      fixed: 'right' as const,
      render: (_: unknown, record: typeof mockConditions[0]) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => { setSelectedCondition(record); setDetailModalVisible(true); }}>
            详情
          </Button>
          <Tooltip title="编辑条件记录">
            <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
          </Tooltip>
          {isExpiringSoon(record.validTo) && (
            <Tooltip title="延长有效期">
              <Button type="link" size="small" icon={<CalendarOutlined />} style={{ color: '#faad14' }}>
                延期
              </Button>
            </Tooltip>
          )}
          <Popconfirm title="确定删除此条件记录？" icon={<ExclamationCircleOutlined />} onConfirm={() => message.success('条件记录已删除')}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card
        title="定价管理 (对标 SAP VK11/VK12)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => setLoading(!loading)}>刷新</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateModalVisible(true)}>
              新建条件记录
            </Button>
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            { key: 'all', label: <><TagsOutlined /> 全部条件</> },
            { key: 'PR00', label: <>PR00 基础价格</> },
            { key: 'K004', label: <>折扣条件</> },
            { key: 'MWST', label: <>MWST 税率</> },
            { key: 'K007', label: <>附加费</> },
          ]}
        />

        {/* 提示 */}
        {stats.expiringSoon > 0 && (
          <Alert
            message={`有 ${stats.expiringSoon} 条条件记录将在30天内到期，请及时处理。`}
            type="warning"
            showIcon
            closable
            style={{ marginBottom: 16 }}
          />
        )}

        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card size="small">
              <Statistic title="条件记录总数" value={stats.total} suffix="条" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="有效记录" value={stats.active} suffix="条" valueStyle={{ fontSize: 18, color: '#52c41a' }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="即将到期" value={stats.expiringSoon} suffix="条" valueStyle={{ fontSize: 18, color: '#faad14' }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="已失效" value={stats.expired} suffix="条" valueStyle={{ fontSize: 18, color: '#ff4d4f' }} />
            </Card>
          </Col>
        </Row>

        {/* 搜索表单 */}
        <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="conditionType" label="条件类型">
            <Select placeholder="全部" allowClear style={{ width: 160 }}
              options={Object.entries(conditionTypeConfig).map(([k, v]) => ({ value: k, label: `${k} - ${v.text}` }))} />
          </Form.Item>
          <Form.Item name="customerName" label="客户">
            <Input placeholder="客户名称" style={{ width: 130 }} />
          </Form.Item>
          <Form.Item name="materialCode" label="物料">
            <Input placeholder="物料编码" style={{ width: 130 }} />
          </Form.Item>
          <Form.Item name="dateRange" label="有效期">
            <RangePicker style={{ width: 240 }} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select placeholder="全部" allowClear style={{ width: 100 }}
              options={[
                { value: 'active', label: '有效' },
                { value: 'expiring', label: '即将到期' },
                { value: 'expired', label: '已失效' },
              ]} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SearchOutlined />}>查询</Button>
          </Form.Item>
        </Form>

        {/* 条件记录表格 */}
        <Table
          columns={columns}
          dataSource={filteredConditions}
          rowKey="id"
          loading={loading}
          size="small"
          scroll={{ x: 1400 }}
          pagination={{ defaultPageSize: 20, showSizeChanger: true }}
        />
      </Card>

      {/* 新建条件记录弹窗 */}
      <Modal
        title="新建条件记录"
        open={createModalVisible}
        onCancel={() => setCreateModalVisible(false)}
        onOk={() => { message.success('条件记录创建成功'); setCreateModalVisible(false); }}
        width={700}
      >
        <Form layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="条件类型" required>
                <Select placeholder="选择条件类型"
                  options={Object.entries(conditionTypeConfig).map(([k, v]) => ({ value: k, label: `${k} - ${v.text}` }))} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="计算类型" required>
                <Select placeholder="选择计算类型"
                  options={Object.entries(calculationTypeConfig).map(([k, v]) => ({ value: k, label: `${k} - ${v.text}` }))} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="客户编码">
                <Select placeholder="选择客户（留空表示全部客户）" showSearch allowClear optionFilterProp="label"
                  options={[
                    { value: 'CUST-001', label: 'CUST-001 - 北京科技有限公司' },
                    { value: 'CUST-002', label: 'CUST-002 - 上海贸易集团' },
                    { value: 'CUST-003', label: 'CUST-003 - 广州制造企业' },
                    { value: 'CUST-004', label: 'CUST-004 - 深圳电子公司' },
                    { value: 'CUST-005', label: 'CUST-005 - 杭州网络科技' },
                  ]} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="物料编码">
                <Select placeholder="选择物料（留空表示全部物料）" showSearch allowClear optionFilterProp="label"
                  options={[
                    { value: 'MAT-001', label: 'MAT-001 - 精密轴承 A-100' },
                    { value: 'MAT-002', label: 'MAT-002 - 密封组件 S-200' },
                    { value: 'MAT-004', label: 'MAT-004 - 工业电机 M-500' },
                    { value: 'MAT-006', label: 'MAT-006 - 液压阀门 H-800' },
                    { value: 'MAT-008', label: 'MAT-008 - 电源模块 PM-600' },
                  ]} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="金额 (计算类型为固定金额时)" name="amount">
                <InputNumber placeholder="输入金额" min={0} precision={2} style={{ width: '100%' }} addonAfter="CNY" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="费率 (计算类型为百分比时)" name="rate">
                <InputNumber placeholder="输入费率" min={0} max={100} precision={2} style={{ width: '100%' }} addonAfter="%" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="生效日期" required>
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="失效日期" required>
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item label="币种">
            <Select defaultValue="CNY" style={{ width: 200 }}
              options={[
                { value: 'CNY', label: 'CNY - 人民币' },
                { value: 'USD', label: 'USD - 美元' },
                { value: 'EUR', label: 'EUR - 欧元' },
              ]} />
          </Form.Item>
          <Card title="价格等级（可选，用于数量依赖定价）" size="small">
            <Table
              columns={[
                { title: '起订数量', dataIndex: 'fromQty', width: 120, render: () => <InputNumber size="small" min={0} style={{ width: '100%' }} /> },
                { title: '截止数量', dataIndex: 'toQty', width: 120, render: () => <InputNumber size="small" min={0} style={{ width: '100%' }} /> },
                { title: '价格/费率', dataIndex: 'value', width: 120, render: () => <InputNumber size="small" min={0} precision={2} style={{ width: '100%' }} /> },
                { title: '操作', width: 60, render: () => <Button type="link" size="small" danger>删除</Button> },
              ]}
              dataSource={[]}
              size="small"
              pagination={false}
              locale={{ emptyText: '点击下方添加价格等级' }}
            />
            <Button type="dashed" block style={{ marginTop: 16 }} icon={<PlusOutlined />}>
              添加价格等级
            </Button>
          </Card>
        </Form>
      </Modal>

      {/* 条件记录详情弹窗 */}
      <Modal
        title={`条件记录详情 - ${selectedCondition?.conditionType} ${selectedCondition?.conditionDesc}`}
        open={detailModalVisible}
        onCancel={() => { setDetailModalVisible(false); setSelectedCondition(null); }}
        footer={[
          <Button key="edit" icon={<EditOutlined />}>编辑</Button>,
          <Button key="copy" icon={<CopyOutlined />}>复制</Button>,
          <Button key="close" type="primary" onClick={() => { setDetailModalVisible(false); setSelectedCondition(null); }}>关闭</Button>,
        ]}
        width={800}
      >
        {selectedCondition && (
          <>
            <Descriptions bordered size="small" column={2} style={{ marginBottom: 16 }}>
              <Descriptions.Item label="条件类型">
                <Tag color={conditionTypeConfig[selectedCondition.conditionType]?.color}>
                  {selectedCondition.conditionType} - {conditionTypeConfig[selectedCondition.conditionType]?.text}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="分类">{conditionTypeConfig[selectedCondition.conditionType]?.category}</Descriptions.Item>
              <Descriptions.Item label="客户编码">{selectedCondition.customerCode}</Descriptions.Item>
              <Descriptions.Item label="客户名称">{selectedCondition.customerName}</Descriptions.Item>
              <Descriptions.Item label="物料编码">{selectedCondition.materialCode}</Descriptions.Item>
              <Descriptions.Item label="物料名称">{selectedCondition.materialName}</Descriptions.Item>
              <Descriptions.Item label="计算类型">
                <Tag color={calculationTypeConfig[selectedCondition.calculationType]?.color}>
                  {calculationTypeConfig[selectedCondition.calculationType]?.text}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="金额/费率">
                {selectedCondition.calculationType === 'A'
                  ? <span style={{ color: '#1890ff', fontWeight: 'bold' }}>{selectedCondition.rate}%</span>
                  : <span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{selectedCondition.amount?.toFixed(2)}</span>
                }
              </Descriptions.Item>
              <Descriptions.Item label="币种">{selectedCondition.currency}</Descriptions.Item>
              <Descriptions.Item label="状态">
                {isExpired(selectedCondition.validTo) && <Tag color="red">已失效</Tag>}
                {isExpiringSoon(selectedCondition.validTo) && !isExpired(selectedCondition.validTo) && <Tag color="orange">即将到期</Tag>}
                {!isExpired(selectedCondition.validTo) && !isExpiringSoon(selectedCondition.validTo) && <Tag color="green">有效</Tag>}
              </Descriptions.Item>
              <Descriptions.Item label="生效日期">{selectedCondition.validFrom}</Descriptions.Item>
              <Descriptions.Item label="失效日期">{selectedCondition.validTo}</Descriptions.Item>
              <Descriptions.Item label="创建人">{selectedCondition.createdBy}</Descriptions.Item>
              <Descriptions.Item label="创建时间">{selectedCondition.createdAt}</Descriptions.Item>
              {selectedCondition.updatedBy && (
                <>
                  <Descriptions.Item label="最后修改人">{selectedCondition.updatedBy}</Descriptions.Item>
                  <Descriptions.Item label="最后修改时间">{selectedCondition.updatedAt}</Descriptions.Item>
                </>
              )}
            </Descriptions>

            {/* 价格等级 */}
            {selectedCondition.pricingScale.length > 0 && (
              <Card title="价格等级" size="small">
                <Table
                  columns={[
                    { title: '起订数量', dataIndex: 'fromQty', width: 120, render: (v: number) => `${v}` },
                    { title: '截止数量', dataIndex: 'toQty', width: 120, render: (v: number) => `${v}` },
                    {
                      title: '价格',
                      dataIndex: 'value',
                      width: 120,
                      align: 'right' as const,
                      render: (v: number) => <span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{v.toFixed(2)}</span>,
                    },
                  ]}
                  dataSource={selectedCondition.pricingScale}
                  rowKey="fromQty"
                  size="small"
                  pagination={false}
                />
              </Card>
            )}
          </>
        )}
      </Modal>
    </div>
  );
}
