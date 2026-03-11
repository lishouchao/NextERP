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
  Descriptions,
  Progress,
  List,
  Rate,
  message,
  Tooltip,
  Badge,
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  ReloadOutlined,
  UserOutlined,
  PhoneOutlined,
  MailOutlined,
  EnvironmentOutlined,
  TeamOutlined,
  CreditCardOutlined,
} from '@ant-design/icons';

// 模拟客户数据
const mockCustomers = [
  {
    id: 1,
    code: 'CUST-001',
    name: '北京科技有限公司',
    shortName: '北京科技',
    type: 'enterprise',
    level: 'A',
    status: 1,
    contact: '李总',
    phone: '13800138001',
    email: 'lijd@bjtech.com',
    address: '北京市海淀区中关村大街1号',
    creditLimit: 500000,
    creditUsed: 125000,
    paymentTerm: '30',
    totalOrders: 28,
    totalAmount: 1850000,
    lastOrderDate: '2023-12-15',
    createdAt: '2022-03-10',
    salesRep: '销售员A',
    region: '华北',
    industry: '信息技术',
    remark: '重点客户，优质付款记录',
  },
  {
    id: 2,
    code: 'CUST-002',
    name: '上海贸易集团',
    shortName: '上海贸易',
    type: 'enterprise',
    level: 'A',
    status: 1,
    contact: '王总',
    phone: '13900139002',
    email: 'wangz@shtrade.com',
    address: '上海市浦东新区陆家嘴环路100号',
    creditLimit: 800000,
    creditUsed: 560000,
    paymentTerm: '45',
    totalOrders: 45,
    totalAmount: 3280000,
    lastOrderDate: '2023-12-12',
    createdAt: '2021-08-20',
    salesRep: '销售员A',
    region: '华东',
    industry: '贸易',
    remark: '大客户，月均采购50万+',
  },
  {
    id: 3,
    code: 'CUST-003',
    name: '广州制造企业',
    shortName: '广州制造',
    type: 'enterprise',
    level: 'B',
    status: 1,
    contact: '张总',
    phone: '13700137003',
    email: 'zhangz@gzmfg.com',
    address: '广州市天河区珠江新城',
    creditLimit: 300000,
    creditUsed: 88000,
    paymentTerm: '30',
    totalOrders: 15,
    totalAmount: 680000,
    lastOrderDate: '2023-12-14',
    createdAt: '2022-11-05',
    salesRep: '销售员B',
    region: '华南',
    industry: '制造业',
    remark: '',
  },
  {
    id: 4,
    code: 'CUST-004',
    name: '深圳电子公司',
    shortName: '深圳电子',
    type: 'enterprise',
    level: 'A',
    status: 1,
    contact: '陈总',
    phone: '13600136004',
    email: 'chenz@szelec.com',
    address: '深圳市南山区科技园',
    creditLimit: 600000,
    creditUsed: 380000,
    paymentTerm: '30',
    totalOrders: 32,
    totalAmount: 2560000,
    lastOrderDate: '2023-12-16',
    createdAt: '2021-05-18',
    salesRep: '销售员A',
    region: '华南',
    industry: '电子',
    remark: '高增长客户',
  },
  {
    id: 5,
    code: 'CUST-005',
    name: '杭州网络科技',
    shortName: '杭州网络',
    type: 'enterprise',
    level: 'C',
    status: 2,
    contact: '赵总',
    phone: '13500135005',
    email: 'zhaoz@hznet.com',
    address: '杭州市西湖区文三路',
    creditLimit: 200000,
    creditUsed: 200000,
    paymentTerm: '15',
    totalOrders: 8,
    totalAmount: 320000,
    lastOrderDate: '2023-11-20',
    createdAt: '2023-02-28',
    salesRep: '销售员B',
    region: '华东',
    industry: '互联网',
    remark: '付款有延期，需关注',
  },
  {
    id: 6,
    code: 'CUST-006',
    name: '成都零售连锁',
    shortName: '成都零售',
    type: 'retail',
    level: 'B',
    status: 1,
    contact: '刘总',
    phone: '13400134006',
    email: 'liuz@cdretail.com',
    address: '成都市武侯区天府大道',
    creditLimit: 400000,
    creditUsed: 150000,
    paymentTerm: '30',
    totalOrders: 22,
    totalAmount: 980000,
    lastOrderDate: '2023-12-13',
    createdAt: '2022-06-15',
    salesRep: '销售员C',
    region: '西南',
    industry: '零售',
    remark: '',
  },
];

// 客户订单历史
const mockOrderHistory = [
  { orderNo: 'SO-2023-001', date: '2023-12-10', amount: 125000, status: '已完成' },
  { orderNo: 'SO-2023-015', date: '2023-11-28', amount: 85000, status: '已完成' },
  { orderNo: 'SO-2023-022', date: '2023-11-15', amount: 156000, status: '已完成' },
  { orderNo: 'SO-2023-030', date: '2023-10-30', amount: 92000, status: '已完成' },
];

// 客户付款记录
const mockPaymentHistory = [
  { paymentNo: 'PAY-2023-012', date: '2023-12-08', amount: 125000, method: '银行转账', status: '已到账' },
  { paymentNo: 'PAY-2023-008', date: '2023-11-25', amount: 85000, method: '银行转账', status: '已到账' },
  { paymentNo: 'PAY-2023-005', date: '2023-11-10', amount: 100000, method: '银行转账', status: '已到账' },
];

export default function CustomersPage() {
  const [loading, setLoading] = useState(false);
  const [customers, setCustomers] = useState(mockCustomers);
  const [activeTab, setActiveTab] = useState('list');
  const [customerModalVisible, setCustomerModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState<typeof mockCustomers[0] | null>(null);
  const [form] = Form.useForm();

  // 客户类型
  const customerTypeMap: Record<string, { color: string; text: string }> = {
    enterprise: { color: 'blue', text: '企业' },
    retail: { color: 'green', text: '零售' },
    government: { color: 'gold', text: '政府' },
  };

  // 客户等级
  const levelMap: Record<string, { color: string; stars: number }> = {
    A: { color: 'gold', stars: 5 },
    B: { color: 'blue', stars: 3 },
    C: { color: 'default', stars: 1 },
  };

  // 客户状态
  const statusMap: Record<number, { color: string; text: string }> = {
    1: { color: 'green', text: '正常' },
    2: { color: 'orange', text: '冻结' },
    3: { color: 'red', text: '黑名单' },
  };

  // 客户表格列
  const columns = [
    { title: '客户编码', dataIndex: 'code', key: 'code', width: 100, fixed: 'left' as const },
    { title: '客户名称', dataIndex: 'name', key: 'name', width: 160,
      render: (text: string, record) => (
        <a onClick={() => { setSelectedCustomer(record); setDetailModalVisible(true); }}>{text}</a>
      ),
    },
    { title: '简称', dataIndex: 'shortName', key: 'shortName', width: 80 },
    { title: '类型', dataIndex: 'type', key: 'type', width: 70,
      render: (type: string) => {
        const config = customerTypeMap[type];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    { title: '等级', dataIndex: 'level', key: 'level', width: 100,
      render: (level: string) => {
        const config = levelMap[level];
        return <Rate disabled defaultValue={config?.stars || 1} count={5} style={{ fontSize: 12 }} />;
      },
    },
    { title: '联系人', dataIndex: 'contact', key: 'contact', width: 80 },
    { title: '联系电话', dataIndex: 'phone', key: 'phone', width: 120 },
    { title: '信用额度', key: 'credit', width: 140,
      render: (_: unknown, record) => {
        const percent = (record.creditUsed / record.creditLimit) * 100;
        const status = percent >= 90 ? 'exception' : percent >= 70 ? 'normal' : 'success';
        return (
          <Tooltip title={`已用: ¥${record.creditUsed.toLocaleString()} / 额度: ¥${record.creditLimit.toLocaleString()}`}>
            <Progress percent={Math.round(percent)} size="small" status={status} format={() => `${percent.toFixed(0)}%`} />
          </Tooltip>
        );
      },
    },
    { title: '订单数', dataIndex: 'totalOrders', key: 'totalOrders', width: 80, align: 'center' as const,
      render: (v: number) => <Badge count={v} showZero style={{ backgroundColor: '#52c41a' }} />,
    },
    { title: '累计金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120, align: 'right' as const,
      render: (v: number) => <span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{v.toLocaleString()}</span>,
    },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80,
      render: (status: number) => {
        const config = statusMap[status];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    { title: '销售员', dataIndex: 'salesRep', key: 'salesRep', width: 90 },
    { title: '操作', key: 'action', width: 180, fixed: 'right' as const,
      render: (_: unknown, record) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => { setSelectedCustomer(record); setDetailModalVisible(true); }}>详情</Button>
          <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
          <Button type="link" size="small">下单</Button>
        </Space>
      ),
    },
  ];

  // 统计
  const stats = {
    totalCustomers: customers.length,
    activeCustomers: customers.filter(c => c.status === 1).length,
    levelACustomers: customers.filter(c => c.level === 'A').length,
    totalCreditLimit: customers.reduce((s, c) => s + c.creditLimit, 0),
    totalCreditUsed: customers.reduce((s, c) => s + c.creditUsed, 0),
    totalAmount: customers.reduce((s, c) => s + c.totalAmount, 0),
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="客户管理 (对标 SAP VD01/XD01)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => setLoading(!loading)}>刷新</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setCustomerModalVisible(true)}>
              新增客户
            </Button>
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            { key: 'list', label: <><TeamOutlined /> 客户列表</> },
            { key: 'credit', label: <><CreditCardOutlined /> 信用管理</> },
            { key: 'analysis', label: '客户分析' },
          ]}
        />

        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={4}>
            <Card size="small">
              <Statistic title="客户总数" value={stats.totalCustomers} suffix="家" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="活跃客户" value={stats.activeCustomers} suffix="家" valueStyle={{ fontSize: 18, color: '#52c41a' }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="A类客户" value={stats.levelACustomers} suffix="家" valueStyle={{ fontSize: 18, color: '#faad14' }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="授信总额" value={stats.totalCreditLimit} prefix="¥" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="已用额度" value={stats.totalCreditUsed} prefix="¥" valueStyle={{ fontSize: 18, color: '#ff4d4f' }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="累计销售" value={stats.totalAmount} prefix="¥" valueStyle={{ fontSize: 18, color: '#1890ff' }} />
            </Card>
          </Col>
        </Row>

        {/* 客户列表 */}
        {activeTab === 'list' && (
          <>
            <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item name="keyword" label="搜索">
                <Input placeholder="编码/名称/联系人" style={{ width: 160 }} />
              </Form.Item>
              <Form.Item name="level" label="等级">
                <Select placeholder="全部" allowClear style={{ width: 80 }}
                  options={[
                    { value: 'A', label: 'A类' },
                    { value: 'B', label: 'B类' },
                    { value: 'C', label: 'C类' },
                  ]} />
              </Form.Item>
              <Form.Item name="status" label="状态">
                <Select placeholder="全部" allowClear style={{ width: 80 }}
                  options={Object.entries(statusMap).map(([k, v]) => ({ value: Number(k), label: v.text }))} />
              </Form.Item>
              <Form.Item name="region" label="区域">
                <Select placeholder="全部" allowClear style={{ width: 100 }}
                  options={[
                    { value: '华北', label: '华北' },
                    { value: '华东', label: '华东' },
                    { value: '华南', label: '华南' },
                    { value: '西南', label: '西南' },
                  ]} />
              </Form.Item>
              <Form.Item>
                <Button type="primary" icon={<SearchOutlined />}>查询</Button>
              </Form.Item>
            </Form>

            <Table
              columns={columns}
              dataSource={customers}
              rowKey="id"
              loading={loading}
              size="small"
              scroll={{ x: 1600 }}
              pagination={{ defaultPageSize: 20, showSizeChanger: true }}
            />
          </>
        )}

        {/* 信用管理 */}
        {activeTab === 'credit' && (
          <Row gutter={24}>
            <Col span={16}>
              <Card title="信用预警" size="small">
                <Table
                  columns={[
                    { title: '客户', dataIndex: 'name', width: 150 },
                    { title: '信用额度', dataIndex: 'creditLimit', width: 120, render: (v) => `¥${v.toLocaleString()}` },
                    { title: '已用额度', dataIndex: 'creditUsed', width: 120, render: (v) => `¥${v.toLocaleString()}` },
                    {
                      title: '使用率', key: 'usage', width: 150,
                      render: (_: unknown, r) => {
                        const percent = (r.creditUsed / r.creditLimit) * 100;
                        return <Progress percent={Math.round(percent)} size="small" status={percent >= 90 ? 'exception' : 'normal'} />;
                      },
                    },
                    { title: '销售员', dataIndex: 'salesRep', width: 100 },
                    { title: '操作', key: 'action', width: 100, render: () => <Button type="link" size="small">调整额度</Button> },
                  ]}
                  dataSource={customers.filter(c => (c.creditUsed / c.creditLimit) >= 0.7)}
                  rowKey="id"
                  size="small"
                  pagination={false}
                />
              </Card>
            </Col>
            <Col span={8}>
              <Card title="信用分布" size="small">
                <div style={{ padding: 20, textAlign: 'center' }}>
                  <div style={{ marginBottom: 16 }}>
                    <Progress type="circle" percent={Math.round((stats.totalCreditUsed / stats.totalCreditLimit) * 100)} />
                  </div>
                  <div style={{ color: '#8c8c8c' }}>总体信用使用率</div>
                </div>
              </Card>
            </Col>
          </Row>
        )}

        {/* 客户分析 */}
        {activeTab === 'analysis' && (
          <Row gutter={24}>
            <Col span={12}>
              <Card title="客户销售排名 TOP 5" size="small">
                <List
                  dataSource={customers.sort((a, b) => b.totalAmount - a.totalAmount).slice(0, 5)}
                  renderItem={(item, index) => (
                    <List.Item>
                      <List.Item.Meta
                        avatar={<Tag color={index < 3 ? 'gold' : 'default'}>{index + 1}</Tag>}
                        title={item.name}
                        description={`订单数: ${item.totalOrders} | 等级: ${item.level}`}
                      />
                      <span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{item.totalAmount.toLocaleString()}</span>
                    </List.Item>
                  )}
                />
              </Card>
            </Col>
            <Col span={12}>
              <Card title="区域销售分布" size="small">
                {['华东', '华南', '华北', '西南'].map(region => {
                  const regionTotal = customers.filter(c => c.region === region).reduce((s, c) => s + c.totalAmount, 0);
                  const percent = (regionTotal / stats.totalAmount) * 100;
                  return (
                    <div key={region} style={{ marginBottom: 12 }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                        <span>{region}</span>
                        <span>¥{regionTotal.toLocaleString()}</span>
                      </div>
                      <Progress percent={Math.round(percent)} size="small" showInfo={false} />
                    </div>
                  );
                })}
              </Card>
            </Col>
          </Row>
        )}
      </Card>

      {/* 新增客户弹窗 */}
      <Modal
        title="新增客户"
        open={customerModalVisible}
        onCancel={() => setCustomerModalVisible(false)}
        onOk={() => { message.success('客户创建成功'); setCustomerModalVisible(false); }}
        width={800}
      >
        <Form layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="客户编码" required>
                <Input placeholder="系统自动生成" disabled />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="客户名称" required>
                <Input placeholder="输入客户全称" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="简称">
                <Input placeholder="客户简称" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="客户类型" required>
                <Select placeholder="选择类型" options={[
                  { value: 'enterprise', label: '企业客户' },
                  { value: 'retail', label: '零售客户' },
                  { value: 'government', label: '政府机构' },
                ]} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="客户等级">
                <Select placeholder="选择等级" options={[
                  { value: 'A', label: 'A类 (优质客户)' },
                  { value: 'B', label: 'B类 (普通客户)' },
                  { value: 'C', label: 'C类 (一般客户)' },
                ]} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="联系人">
                <Input placeholder="联系人姓名" prefix={<UserOutlined />} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="联系电话">
                <Input placeholder="联系电话" prefix={<PhoneOutlined />} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="邮箱">
                <Input placeholder="电子邮箱" prefix={<MailOutlined />} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item label="地址">
            <Input placeholder="详细地址" prefix={<EnvironmentOutlined />} />
          </Form.Item>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="信用额度">
                <InputNumber style={{ width: '100%' }} min={0} placeholder="信用额度" prefix="¥" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="付款账期">
                <InputNumber style={{ width: '100%' }} min={0} placeholder="天数" suffix="天" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="销售员">
                <Select placeholder="选择销售员" options={[
                  { value: 'sales_a', label: '销售员A' },
                  { value: 'sales_b', label: '销售员B' },
                  { value: 'sales_c', label: '销售员C' },
                ]} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item label="备注">
            <Input.TextArea rows={2} placeholder="输入备注信息" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 客户详情弹窗 */}
      <Modal
        title={`客户详情 - ${selectedCustomer?.name}`}
        open={detailModalVisible}
        onCancel={() => { setDetailModalVisible(false); setSelectedCustomer(null); }}
        footer={[
          <Button key="order" type="primary">创建订单</Button>,
          <Button key="close" onClick={() => { setDetailModalVisible(false); setSelectedCustomer(null); }}>关闭</Button>,
        ]}
        width={900}
      >
        {selectedCustomer && (
          <Tabs
            items={[
              {
                key: 'basic',
                label: '基本信息',
                children: (
                  <Descriptions bordered size="small" column={2}>
                    <Descriptions.Item label="客户编码">{selectedCustomer.code}</Descriptions.Item>
                    <Descriptions.Item label="客户名称">{selectedCustomer.name}</Descriptions.Item>
                    <Descriptions.Item label="简称">{selectedCustomer.shortName}</Descriptions.Item>
                    <Descriptions.Item label="类型">
                      <Tag color={customerTypeMap[selectedCustomer.type]?.color}>
                        {customerTypeMap[selectedCustomer.type]?.text}
                      </Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="等级">
                      <Rate disabled defaultValue={levelMap[selectedCustomer.level]?.stars || 1} count={5} style={{ fontSize: 12 }} />
                    </Descriptions.Item>
                    <Descriptions.Item label="状态">
                      <Tag color={statusMap[selectedCustomer.status]?.color}>
                        {statusMap[selectedCustomer.status]?.text}
                      </Tag>
                    </Descriptions.Item>
                    <Descriptions.Item label="联系人">{selectedCustomer.contact}</Descriptions.Item>
                    <Descriptions.Item label="联系电话">{selectedCustomer.phone}</Descriptions.Item>
                    <Descriptions.Item label="邮箱" span={2}>{selectedCustomer.email}</Descriptions.Item>
                    <Descriptions.Item label="地址" span={2}>{selectedCustomer.address}</Descriptions.Item>
                    <Descriptions.Item label="区域">{selectedCustomer.region}</Descriptions.Item>
                    <Descriptions.Item label="行业">{selectedCustomer.industry}</Descriptions.Item>
                    <Descriptions.Item label="销售员">{selectedCustomer.salesRep}</Descriptions.Item>
                    <Descriptions.Item label="创建日期">{selectedCustomer.createdAt}</Descriptions.Item>
                    <Descriptions.Item label="备注" span={2}>{selectedCustomer.remark || '-'}</Descriptions.Item>
                  </Descriptions>
                ),
              },
              {
                key: 'credit',
                label: '信用信息',
                children: (
                  <Row gutter={24}>
                    <Col span={12}>
                      <Card size="small">
                        <Statistic title="信用额度" value={selectedCustomer.creditLimit} prefix="¥" />
                        <Progress
                          percent={Math.round((selectedCustomer.creditUsed / selectedCustomer.creditLimit) * 100)}
                          style={{ marginTop: 16 }}
                        />
                        <div style={{ marginTop: 8, color: '#8c8c8c' }}>
                          已使用: ¥{selectedCustomer.creditUsed.toLocaleString()}
                        </div>
                      </Card>
                    </Col>
                    <Col span={12}>
                      <Card size="small">
                        <Statistic title="付款账期" value={selectedCustomer.paymentTerm} suffix="天" />
                        <div style={{ marginTop: 16 }}>
                          <div>累计订单: {selectedCustomer.totalOrders} 单</div>
                          <div>累计金额: ¥{selectedCustomer.totalAmount.toLocaleString()}</div>
                          <div>最近下单: {selectedCustomer.lastOrderDate}</div>
                        </div>
                      </Card>
                    </Col>
                  </Row>
                ),
              },
              {
                key: 'orders',
                label: '订单历史',
                children: (
                  <Table
                    columns={[
                      { title: '订单号', dataIndex: 'orderNo', width: 120 },
                      { title: '日期', dataIndex: 'date', width: 100 },
                      { title: '金额', dataIndex: 'amount', width: 120, render: (v) => `¥${v.toLocaleString()}` },
                      { title: '状态', dataIndex: 'status', width: 80, render: (v) => <Tag color="green">{v}</Tag> },
                    ]}
                    dataSource={mockOrderHistory}
                    rowKey="orderNo"
                    size="small"
                    pagination={false}
                  />
                ),
              },
              {
                key: 'payments',
                label: '付款记录',
                children: (
                  <Table
                    columns={[
                      { title: '付款单号', dataIndex: 'paymentNo', width: 120 },
                      { title: '日期', dataIndex: 'date', width: 100 },
                      { title: '金额', dataIndex: 'amount', width: 120, render: (v) => `¥${v.toLocaleString()}` },
                      { title: '方式', dataIndex: 'method', width: 100 },
                      { title: '状态', dataIndex: 'status', width: 80, render: (v) => <Tag color="green">{v}</Tag> },
                    ]}
                    dataSource={mockPaymentHistory}
                    rowKey="paymentNo"
                    size="small"
                    pagination={false}
                  />
                ),
              },
            ]}
          />
        )}
      </Modal>
    </div>
  );
}
