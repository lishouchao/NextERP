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
  Tabs,
  Badge,
  Descriptions,
  message,
  Tooltip,
  Progress,
  Timeline,
  Alert,
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  CheckOutlined,
  SafetyCertificateOutlined,
  DollarOutlined,
  AuditOutlined,
  UnlockOutlined,
  WarningOutlined,
  ExclamationCircleOutlined,
  LockOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';

// 风险等级配置
const riskClassConfig: Record<string, { text: string; color: string }> = {
  '1': { text: '低风险', color: 'green' },
  '2': { text: '中风险', color: 'orange' },
  '3': { text: '高风险', color: 'red' },
};

// 信用状态配置
const creditStatusConfig: Record<string, { text: string; color: string }> = {
  '01': { text: '正常', color: 'green' },
  '02': { text: '预警', color: 'orange' },
  '03': { text: '冻结', color: 'red' },
};

// 模拟信用主数据
const mockCreditMasters = [
  {
    id: 1,
    customerCode: 'CUST-001',
    customerName: '北京科技有限公司',
    contactPerson: '李总',
    creditLimit: 500000.00,
    usedLimit: 266000.00,
    openOrders: 125000.00,
    openDeliveries: 80000.00,
    openInvoices: 61000.00,
    overdueReceivables: 0.00,
    riskClass: '1',
    creditStatus: '01',
    paymentTerms: 'ZT01 - 30天净额',
    lastCreditCheck: '2024-01-25 09:00:00',
    createdAt: '2023-06-01',
    checkHistory: [
      { id: 1, checkDate: '2024-01-25 09:00:00', orderNo: 'SO-2023-005', checkResult: '通过', usedRate: 53.2, checkedBy: '信用管理员A' },
      { id: 2, checkDate: '2024-01-20 14:30:00', orderNo: 'SO-2023-004', checkResult: '通过', usedRate: 45.0, checkedBy: '信用管理员A' },
      { id: 3, checkDate: '2024-01-15 10:00:00', orderNo: 'SO-2023-001', checkResult: '通过', usedRate: 38.5, checkedBy: '信用管理员A' },
    ],
  },
  {
    id: 2,
    customerCode: 'CUST-002',
    customerName: '上海贸易集团',
    contactPerson: '王总',
    creditLimit: 800000.00,
    usedLimit: 720000.00,
    openOrders: 256000.00,
    openDeliveries: 289280.00,
    openInvoices: 174720.00,
    overdueReceivables: 120000.00,
    riskClass: '2',
    creditStatus: '02',
    paymentTerms: 'ZT02 - 60天净额',
    lastCreditCheck: '2024-01-26 11:30:00',
    createdAt: '2023-03-15',
    checkHistory: [
      { id: 1, checkDate: '2024-01-26 11:30:00', orderNo: 'SO-2023-007', checkResult: '预警', usedRate: 90.0, checkedBy: '信用管理员B' },
      { id: 2, checkDate: '2024-01-22 09:15:00', orderNo: 'SO-2023-006', checkResult: '通过', usedRate: 82.5, checkedBy: '信用管理员B' },
      { id: 3, checkDate: '2024-01-18 16:45:00', orderNo: 'SO-2023-002', checkResult: '预警', usedRate: 75.0, checkedBy: '信用管理员A' },
    ],
  },
  {
    id: 3,
    customerCode: 'CUST-003',
    customerName: '广州制造企业',
    contactPerson: '张总',
    creditLimit: 300000.00,
    usedLimit: 285000.00,
    openOrders: 88000.00,
    openDeliveries: 56000.00,
    openInvoices: 99440.00,
    overdueReceivables: 41560.00,
    riskClass: '3',
    creditStatus: '03',
    paymentTerms: 'ZT03 - 45天净额',
    lastCreditCheck: '2024-01-27 08:00:00',
    createdAt: '2023-09-01',
    checkHistory: [
      { id: 1, checkDate: '2024-01-27 08:00:00', orderNo: 'SO-2023-008', checkResult: '冻结', usedRate: 95.0, checkedBy: '信用管理员A' },
      { id: 2, checkDate: '2024-01-25 10:30:00', orderNo: 'SO-2023-003', checkResult: '预警', usedRate: 88.0, checkedBy: '信用管理员A' },
      { id: 3, checkDate: '2024-01-20 14:00:00', orderNo: 'SO-2023-003', checkResult: '预警', usedRate: 80.0, checkedBy: '信用管理员B' },
      { id: 4, checkDate: '2024-01-15 09:00:00', orderNo: 'SO-2023-003', checkResult: '通过', usedRate: 65.0, checkedBy: '信用管理员A' },
    ],
  },
  {
    id: 4,
    customerCode: 'CUST-004',
    customerName: '深圳电子公司',
    contactPerson: '陈总',
    creditLimit: 600000.00,
    usedLimit: 395200.00,
    openOrders: 380000.00,
    openDeliveries: 0.00,
    openInvoices: 429400.00,
    overdueReceivables: 0.00,
    riskClass: '1',
    creditStatus: '01',
    paymentTerms: 'ZT02 - 60天净额',
    lastCreditCheck: '2024-01-24 15:00:00',
    createdAt: '2023-07-20',
    checkHistory: [
      { id: 1, checkDate: '2024-01-24 15:00:00', orderNo: 'SO-2023-005', checkResult: '通过', usedRate: 65.9, checkedBy: '信用管理员B' },
      { id: 2, checkDate: '2024-01-20 09:00:00', orderNo: 'SO-2023-005', checkResult: '通过', usedRate: 58.0, checkedBy: '信用管理员B' },
    ],
  },
  {
    id: 5,
    customerCode: 'CUST-005',
    customerName: '杭州网络科技',
    contactPerson: '赵总',
    creditLimit: 200000.00,
    usedLimit: 186000.00,
    openOrders: 0.00,
    openDeliveries: 21018.00,
    openInvoices: 21018.00,
    overdueReceivables: 144964.00,
    riskClass: '2',
    creditStatus: '02',
    paymentTerms: 'ZT01 - 30天净额',
    lastCreditCheck: '2024-01-26 10:00:00',
    createdAt: '2023-11-01',
    checkHistory: [
      { id: 1, checkDate: '2024-01-26 10:00:00', orderNo: 'DN-2024-003', checkResult: '预警', usedRate: 93.0, checkedBy: '信用管理员A' },
      { id: 2, checkDate: '2024-01-22 14:30:00', orderNo: 'SO-2023-006', checkResult: '通过', usedRate: 85.0, checkedBy: '信用管理员A' },
    ],
  },
  {
    id: 6,
    customerCode: 'CUST-006',
    customerName: '成都智能制造有限公司',
    contactPerson: '刘总',
    creditLimit: 450000.00,
    usedLimit: 67500.00,
    openOrders: 67500.00,
    openDeliveries: 0.00,
    openInvoices: 0.00,
    overdueReceivables: 0.00,
    riskClass: '1',
    creditStatus: '01',
    paymentTerms: 'ZT01 - 30天净额',
    lastCreditCheck: '2024-01-20 09:30:00',
    createdAt: '2024-01-10',
    checkHistory: [
      { id: 1, checkDate: '2024-01-20 09:30:00', orderNo: 'SO-2024-001', checkResult: '通过', usedRate: 15.0, checkedBy: '信用管理员B' },
    ],
  },
];

export default function CreditPage() {
  const [loading, setLoading] = useState(false);
  const [creditMasters, setCreditMasters] = useState(mockCreditMasters);
  const [activeTab, setActiveTab] = useState('overview');
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [updateLimitModalVisible, setUpdateLimitModalVisible] = useState(false);
  const [selectedCredit, setSelectedCredit] = useState<typeof mockCreditMasters[0] | null>(null);
  const [form] = Form.useForm();

  // 统计
  const stats = {
    totalCustomers: creditMasters.length,
    totalCreditLimit: creditMasters.reduce((s, c) => s + c.creditLimit, 0),
    totalUsed: creditMasters.reduce((s, c) => s + c.usedLimit, 0),
    totalOverdue: creditMasters.reduce((s, c) => s + c.overdueReceivables, 0),
    frozenCount: creditMasters.filter(c => c.creditStatus === '03').length,
    warningCount: creditMasters.filter(c => c.creditStatus === '02').length,
  };

  // 计算使用率
  const getUsageRate = (used: number, limit: number) => limit > 0 ? ((used / limit) * 100).toFixed(1) : '0.0';

  // 获取进度条颜色
  const getProgressColor = (rate: number) => {
    if (rate >= 90) return '#ff4d4f';
    if (rate >= 75) return '#faad14';
    return '#52c41a';
  };

  // 信用主数据列
  const creditColumns = [
    {
      title: '客户编码',
      dataIndex: 'customerCode',
      key: 'customerCode',
      width: 110,
      fixed: 'left' as const,
    },
    {
      title: '客户名称',
      dataIndex: 'customerName',
      key: 'customerName',
      width: 170,
      render: (text: string, record: typeof mockCreditMasters[0]) => (
        <a onClick={() => { setSelectedCredit(record); setDetailModalVisible(true); }}>{text}</a>
      ),
    },
    {
      title: '信用额度',
      dataIndex: 'creditLimit',
      key: 'creditLimit',
      width: 130,
      align: 'right' as const,
      render: (v: number) => <span style={{ fontWeight: 'bold' }}>¥{v.toLocaleString()}</span>,
    },
    {
      title: '已用额度',
      dataIndex: 'usedLimit',
      key: 'usedLimit',
      width: 130,
      align: 'right' as const,
      render: (v: number) => {
        const rate = parseFloat(getUsageRate(v, 1));
        return <span style={{ color: '#1890ff' }}>¥{v.toLocaleString()}</span>;
      },
    },
    {
      title: '可用额度',
      key: 'availableLimit',
      width: 130,
      align: 'right' as const,
      render: (_: unknown, record: typeof mockCreditMasters[0]) => {
        const available = record.creditLimit - record.usedLimit;
        return <span style={{ color: available > 0 ? '#52c41a' : '#ff4d4f' }}>¥{available.toLocaleString()}</span>;
      },
    },
    {
      title: '使用率',
      key: 'usageRate',
      width: 160,
      render: (_: unknown, record: typeof mockCreditMasters[0]) => {
        const rate = parseFloat(getUsageRate(record.usedLimit, record.creditLimit));
        return <Progress percent={rate} size="small" strokeColor={getProgressColor(rate)} format={(p) => `${p}%`} />;
      },
    },
    {
      title: '逾期应收',
      dataIndex: 'overdueReceivables',
      key: 'overdueReceivables',
      width: 120,
      align: 'right' as const,
      render: (v: number) => {
        if (v > 0) return <span style={{ color: '#ff4d4f', fontWeight: 'bold' }}>¥{v.toLocaleString()}</span>;
        return <span style={{ color: '#52c41a' }}>¥0</span>;
      },
    },
    {
      title: '风险等级',
      dataIndex: 'riskClass',
      key: 'riskClass',
      width: 90,
      render: (cls: string) => {
        const config = riskClassConfig[cls];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    {
      title: '信用状态',
      dataIndex: 'creditStatus',
      key: 'creditStatus',
      width: 90,
      render: (status: string) => {
        const config = creditStatusConfig[status];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      fixed: 'right' as const,
      render: (_: unknown, record: typeof mockCreditMasters[0]) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => { setSelectedCredit(record); setDetailModalVisible(true); }}>
            详情
          </Button>
          <Tooltip title="调整信用额度">
            <Button type="link" size="small" icon={<EditOutlined />}
              onClick={() => { setSelectedCredit(record); setUpdateLimitModalVisible(true); }}>
              额度
            </Button>
          </Tooltip>
          <Tooltip title="执行信用检查">
            <Button type="link" size="small" icon={<AuditOutlined />} style={{ color: '#1890ff' }}>
              检查
            </Button>
          </Tooltip>
          {record.creditStatus === '03' && (
            <Tooltip title="释放冻结订单">
              <Button type="link" size="small" icon={<UnlockOutlined />} style={{ color: '#faad14' }}>
                释放
              </Button>
            </Tooltip>
          )}
        </Space>
      ),
    },
  ];

  // 信用检查历史列
  const checkHistoryColumns = [
    { title: '检查时间', dataIndex: 'checkDate', key: 'checkDate', width: 160 },
    { title: '关联单据', dataIndex: 'orderNo', key: 'orderNo', width: 130 },
    {
      title: '检查结果',
      dataIndex: 'checkResult',
      key: 'checkResult',
      width: 100,
      render: (result: string) => {
        const colorMap: Record<string, string> = { '通过': 'green', '预警': 'orange', '冻结': 'red' };
        return <Tag color={colorMap[result]}>{result}</Tag>;
      },
    },
    { title: '使用率', dataIndex: 'usedRate', key: 'usedRate', width: 100, render: (v: number) => `${v}%` },
    { title: '检查人', dataIndex: 'checkedBy', key: 'checkedBy', width: 120 },
  ];

  // 所有检查历史
  const allCheckHistory = creditMasters.flatMap(cm =>
    cm.checkHistory.map(ch => ({ ...ch, customerName: cm.customerName, customerCode: cm.customerCode }))
  ).sort((a, b) => dayjs(b.checkDate).unix() - dayjs(a.checkDate).unix());

  return (
    <div style={{ padding: 24 }}>
      <Card
        title="信用管理 (对标 SAP FD32/FD33)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => setLoading(!loading)}>刷新</Button>
            <Button icon={<AuditOutlined />} onClick={() => message.info('批量信用检查已触发')}>
              批量信用检查
            </Button>
          </Space>
        }
      >
        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={4}>
            <Card size="small">
              <Statistic title="客户总数" value={stats.totalCustomers} suffix="户" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={5}>
            <Card size="small">
              <Statistic title="信用额度总额" value={stats.totalCreditLimit} prefix="¥" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={5}>
            <Card size="small">
              <Statistic title="已用额度" value={stats.totalUsed} prefix="¥" valueStyle={{ fontSize: 18, color: '#1890ff' }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="逾期应收" value={stats.totalOverdue} prefix="¥" valueStyle={{ fontSize: 18, color: '#ff4d4f' }} />
            </Card>
          </Col>
          <Col span={3}>
            <Card size="small">
              <Statistic title="预警" value={stats.warningCount} suffix="户" valueStyle={{ fontSize: 18, color: '#faad14' }} />
            </Card>
          </Col>
          <Col span={3}>
            <Card size="small">
              <Statistic title="冻结" value={stats.frozenCount} suffix="户" valueStyle={{ fontSize: 18, color: '#ff4d4f' }} />
            </Card>
          </Col>
        </Row>

        {/* 冻结预警 */}
        {(stats.frozenCount > 0 || stats.warningCount > 0) && (
          <Alert
            message={`当前有 ${stats.warningCount} 户客户信用预警，${stats.frozenCount} 户客户信用冻结，请及时处理。`}
            type="warning"
            showIcon
            closable
            style={{ marginBottom: 16 }}
            icon={<WarningOutlined />}
          />
        )}

        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            { key: 'overview', label: <><SafetyCertificateOutlined /> 信用总览</> },
            { key: 'history', label: <><AuditOutlined /> 信用检查日志</> },
          ]}
        />

        {activeTab === 'overview' && (
          <>
            {/* 信用概览卡片 */}
            <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
              {creditMasters.map(cm => {
                const rate = parseFloat(getUsageRate(cm.usedLimit, cm.creditLimit));
                const available = cm.creditLimit - cm.usedLimit;
                return (
                  <Col span={8} key={cm.id}>
                    <Card
                      size="small"
                      title={
                        <Space>
                          <span>{cm.customerName}</span>
                          <Tag color={riskClassConfig[cm.riskClass]?.color}>{riskClassConfig[cm.riskClass]?.text}</Tag>
                          <Tag color={creditStatusConfig[cm.creditStatus]?.color}>{creditStatusConfig[cm.creditStatus]?.text}</Tag>
                        </Space>
                      }
                      extra={
                        <Button type="link" size="small" onClick={() => { setSelectedCredit(cm); setDetailModalVisible(true); }}>
                          详情
                        </Button>
                      }
                    >
                      <Row gutter={8}>
                        <Col span={12}>
                          <div style={{ fontSize: 12, color: '#8c8c8c' }}>信用额度</div>
                          <div style={{ fontWeight: 'bold', fontSize: 16 }}>¥{cm.creditLimit.toLocaleString()}</div>
                        </Col>
                        <Col span={12}>
                          <div style={{ fontSize: 12, color: '#8c8c8c' }}>可用额度</div>
                          <div style={{ fontWeight: 'bold', fontSize: 16, color: available > 0 ? '#52c41a' : '#ff4d4f' }}>
                            ¥{available.toLocaleString()}
                          </div>
                        </Col>
                      </Row>
                      <Progress
                        percent={rate}
                        size="small"
                        strokeColor={getProgressColor(rate)}
                        format={(p) => `使用 ${p}%`}
                        style={{ marginTop: 8 }}
                      />
                      {cm.overdueReceivables > 0 && (
                        <div style={{ marginTop: 4, color: '#ff4d4f', fontSize: 12 }}>
                          <WarningOutlined /> 逾期应收: ¥{cm.overdueReceivables.toLocaleString()}
                        </div>
                      )}
                    </Card>
                  </Col>
                );
              })}
            </Row>

            {/* 搜索表单 */}
            <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item name="customerName" label="客户">
                <Input placeholder="客户名称" style={{ width: 150 }} />
              </Form.Item>
              <Form.Item name="riskClass" label="风险等级">
                <Select placeholder="全部" allowClear style={{ width: 100 }}
                  options={Object.entries(riskClassConfig).map(([k, v]) => ({ value: k, label: v.text }))} />
              </Form.Item>
              <Form.Item name="creditStatus" label="信用状态">
                <Select placeholder="全部" allowClear style={{ width: 100 }}
                  options={Object.entries(creditStatusConfig).map(([k, v]) => ({ value: k, label: v.text }))} />
              </Form.Item>
              <Form.Item>
                <Button type="primary" icon={<SearchOutlined />}>查询</Button>
              </Form.Item>
            </Form>

            {/* 信用主数据表格 */}
            <Table
              columns={creditColumns}
              dataSource={creditMasters}
              rowKey="id"
              loading={loading}
              size="small"
              scroll={{ x: 1500 }}
              pagination={{ defaultPageSize: 20, showSizeChanger: true }}
            />
          </>
        )}

        {activeTab === 'history' && (
          <Table
            columns={[
              { title: '客户编码', dataIndex: 'customerCode', key: 'customerCode', width: 110 },
              { title: '客户名称', dataIndex: 'customerName', key: 'customerName', width: 160 },
              ...checkHistoryColumns,
            ]}
            dataSource={allCheckHistory}
            rowKey="id"
            loading={loading}
            size="small"
            pagination={{ defaultPageSize: 20, showSizeChanger: true }}
          />
        )}
      </Card>

      {/* 调整信用额度弹窗 */}
      <Modal
        title={`调整信用额度 - ${selectedCredit?.customerName}`}
        open={updateLimitModalVisible}
        onCancel={() => { setUpdateLimitModalVisible(false); setSelectedCredit(null); }}
        onOk={() => { message.success('信用额度已更新'); setUpdateLimitModalVisible(false); setSelectedCredit(null); }}
        width={500}
      >
        {selectedCredit && (
          <Form layout="vertical">
            <Descriptions bordered size="small" column={1} style={{ marginBottom: 16 }}>
              <Descriptions.Item label="客户编码">{selectedCredit.customerCode}</Descriptions.Item>
              <Descriptions.Item label="客户名称">{selectedCredit.customerName}</Descriptions.Item>
              <Descriptions.Item label="当前信用额度">
                <span style={{ fontWeight: 'bold' }}>¥{selectedCredit.creditLimit.toLocaleString()}</span>
              </Descriptions.Item>
              <Descriptions.Item label="已用额度">
                <span style={{ color: '#1890ff' }}>¥{selectedCredit.usedLimit.toLocaleString()}</span>
              </Descriptions.Item>
              <Descriptions.Item label="当前使用率">
                {getUsageRate(selectedCredit.usedLimit, selectedCredit.creditLimit)}%
              </Descriptions.Item>
            </Descriptions>
            <Form.Item label="新信用额度" required>
              <InputNumber
                style={{ width: '100%' }}
                min={0}
                precision={2}
                defaultValue={selectedCredit.creditLimit}
                addonAfter="CNY"
              />
            </Form.Item>
            <Form.Item label="风险等级">
              <Select defaultValue={selectedCredit.riskClass}
                options={Object.entries(riskClassConfig).map(([k, v]) => ({ value: k, label: `${v.text}` }))} />
            </Form.Item>
            <Form.Item label="调整原因" required>
              <Input.TextArea rows={3} placeholder="请输入调整原因" />
            </Form.Item>
          </Form>
        )}
      </Modal>

      {/* 信用详情弹窗 */}
      <Modal
        title={`信用详情 - ${selectedCredit?.customerName}`}
        open={detailModalVisible}
        onCancel={() => { setDetailModalVisible(false); setSelectedCredit(null); }}
        footer={[
          <Button key="update" icon={<EditOutlined />}
            onClick={() => { setDetailModalVisible(false); setUpdateLimitModalVisible(true); }}>
            调整额度
          </Button>,
          <Button key="check" icon={<AuditOutlined />} type="primary"
            onClick={() => { message.success('信用检查已执行'); }}>
            执行信用检查
          </Button>,
          selectedCredit?.creditStatus === '03' ? (
            <Button key="release" icon={<UnlockOutlined />} style={{ background: '#faad14', borderColor: '#faad14', color: '#fff' }}
              onClick={() => { message.success('冻结订单已释放'); }}>
              释放冻结订单
            </Button>
          ) : null,
          <Button key="close" onClick={() => { setDetailModalVisible(false); setSelectedCredit(null); }}>关闭</Button>,
        ]}
        width={900}
      >
        {selectedCredit && (
          <>
            {/* 信用概览 */}
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={12}>
                <Card size="small">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontSize: 12, color: '#8c8c8c' }}>信用额度</div>
                      <div style={{ fontSize: 24, fontWeight: 'bold' }}>¥{selectedCredit.creditLimit.toLocaleString()}</div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <div style={{ fontSize: 12, color: '#8c8c8c' }}>使用率</div>
                      <div style={{ fontSize: 24, fontWeight: 'bold', color: getProgressColor(parseFloat(getUsageRate(selectedCredit.usedLimit, selectedCredit.creditLimit))) }}>
                        {getUsageRate(selectedCredit.usedLimit, selectedCredit.creditLimit)}%
                      </div>
                    </div>
                  </div>
                  <Progress
                    percent={parseFloat(getUsageRate(selectedCredit.usedLimit, selectedCredit.creditLimit))}
                    strokeColor={getProgressColor(parseFloat(getUsageRate(selectedCredit.usedLimit, selectedCredit.creditLimit)))}
                    showInfo={false}
                    style={{ marginTop: 12 }}
                  />
                </Card>
              </Col>
              <Col span={12}>
                <Card size="small">
                  <Space direction="vertical" style={{ width: '100%' }} size="small">
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>风险等级</span>
                      <Tag color={riskClassConfig[selectedCredit.riskClass]?.color}>{riskClassConfig[selectedCredit.riskClass]?.text}</Tag>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>信用状态</span>
                      <Tag color={creditStatusConfig[selectedCredit.creditStatus]?.color}>{creditStatusConfig[selectedCredit.creditStatus]?.text}</Tag>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>付款条件</span>
                      <span>{selectedCredit.paymentTerms}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>联系人</span>
                      <span>{selectedCredit.contactPerson}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                      <span>最后检查</span>
                      <span>{selectedCredit.lastCreditCheck}</span>
                    </div>
                  </Space>
                </Card>
              </Col>
            </Row>

            {/* 信用占用明细 */}
            <Card title="信用占用明细" size="small" style={{ marginBottom: 16 }}>
              <Table
                columns={[
                  {
                    title: '占用类型',
                    dataIndex: 'type',
                    width: 120,
                    render: (type: string) => {
                      const typeMap: Record<string, { color: string; icon: React.ReactNode }> = {
                        'openOrders': { color: 'blue', icon: null },
                        'openDeliveries': { color: 'processing', icon: null },
                        'openInvoices': { color: 'orange', icon: null },
                        'overdueReceivables': { color: 'red', icon: null },
                      };
                      const typeTextMap: Record<string, string> = {
                        'openOrders': '未清订单',
                        'openDeliveries': '未清交货',
                        'openInvoices': '未清发票',
                        'overdueReceivables': '逾期应收',
                      };
                      return <Tag color={typeMap[type]?.color}>{typeTextMap[type]}</Tag>;
                    },
                  },
                  {
                    title: '金额',
                    dataIndex: 'amount',
                    width: 150,
                    align: 'right' as const,
                    render: (v: number) => <span style={{ fontWeight: 'bold' }}>¥{v.toLocaleString()}</span>,
                  },
                  {
                    title: '占比',
                    dataIndex: 'percentage',
                    width: 100,
                    render: (v: number) => `${v.toFixed(1)}%`,
                  },
                ]}
                dataSource={[
                  { key: '1', type: 'openOrders', amount: selectedCredit.openOrders, percentage: selectedCredit.usedLimit > 0 ? (selectedCredit.openOrders / selectedCredit.usedLimit * 100) : 0 },
                  { key: '2', type: 'openDeliveries', amount: selectedCredit.openDeliveries, percentage: selectedCredit.usedLimit > 0 ? (selectedCredit.openDeliveries / selectedCredit.usedLimit * 100) : 0 },
                  { key: '3', type: 'openInvoices', amount: selectedCredit.openInvoices, percentage: selectedCredit.usedLimit > 0 ? (selectedCredit.openInvoices / selectedCredit.usedLimit * 100) : 0 },
                  { key: '4', type: 'overdueReceivables', amount: selectedCredit.overdueReceivables, percentage: selectedCredit.usedLimit > 0 ? (selectedCredit.overdueReceivables / selectedCredit.usedLimit * 100) : 0 },
                ]}
                size="small"
                pagination={false}
                summary={(data) => {
                  const total = data.reduce((sum, r) => sum + r.amount, 0);
                  return (
                    <Table.Summary.Row>
                      <Table.Summary.Cell index={0}><strong>已用合计</strong></Table.Summary.Cell>
                      <Table.Summary.Cell index={1} align="right">
                        <strong style={{ color: '#1890ff' }}>¥{total.toLocaleString()}</strong>
                      </Table.Summary.Cell>
                      <Table.Summary.Cell index={2}>100%</Table.Summary.Cell>
                    </Table.Summary.Row>
                  );
                }}
              />
            </Card>

            {/* 信用检查历史 */}
            <Card title="信用检查历史" size="small">
              <Table
                columns={checkHistoryColumns}
                dataSource={selectedCredit.checkHistory}
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
