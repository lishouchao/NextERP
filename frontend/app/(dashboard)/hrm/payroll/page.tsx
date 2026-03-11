'use client';

import { useState } from 'react';
import {
  Card, Table, Button, Space, Modal, Form, Input, Select, DatePicker,
  Tag, message, Row, Col, Statistic, Tabs, Descriptions, List, Avatar,
  Timeline, Progress, Badge, Steps, Divider, Collapse, Spin,
} from 'antd';
import {
  DollarOutlined, CheckCircleOutlined, CloseCircleOutlined,
  ClockCircleOutlined, PlusOutlined, FileTextOutlined,
  CalculatorOutlined, BankOutlined, DownloadOutlined, EyeOutlined,
  RocketOutlined,
} from '@ant-design/icons';

// 薪资项
const payrollItems = [
  { code: '/101', name: '基本工资', type: 'EARNING', category: 'FIXED', amount: 18000 },
  { code: '/102', name: '岗位工资', type: 'EARNING', category: 'FIXED', amount: 3000 },
  { code: '/103', name: '绩效工资', type: 'EARNING', category: 'VARIABLE', amount: 4500 },
  { code: '/111', name: '交通补贴', type: 'EARNING', category: 'ALLOWANCE', amount: 500 },
  { code: '/112', name: '通讯补贴', type: 'EARNING', category: 'ALLOWANCE', amount: 200 },
  { code: '/201', name: '养老保险(个人)', type: 'DEDUCTION', category: 'SOCIAL', amount: 1440 },
  { code: '/202', name: '医疗保险(个人)', type: 'DEDUCTION', category: 'SOCIAL', amount: 360 },
  { code: '/203', name: '失业保险(个人)', type: 'DEDUCTION', category: 'SOCIAL', amount: 90 },
  { code: '/204', name: '住房公积金(个人)', type: 'DEDUCTION', category: 'HOUSING', amount: 2160 },
  { code: '/301', name: '个人所得税', type: 'DEDUCTION', category: 'TAX', amount: 790 },
  { code: '/401', name: '考勤扣款', type: 'DEDUCTION', category: 'ATTENDANCE', amount: 0 },
];

// 模拟薪资结果
const mockPayrollResults = [
  { pernr: 'EMP001', empName: '张伟', period: '2024-03', grossPay: 26200, deductions: 4840, netPay: 21360, status: 'CONFIRMED', bankAccount: '****1234', bankName: '工商银行' },
  { pernr: 'EMP002', empName: '李娜', period: '2024-03', grossPay: 18500, deductions: 3420, netPay: 15080, status: 'CONFIRMED', bankAccount: '****5678', bankName: '建设银行' },
  { pernr: 'EMP003', empName: '王磊', period: '2024-03', grossPay: 22000, deductions: 4080, netPay: 17920, status: 'CONFIRMED', bankAccount: '****9012', bankName: '招商银行' },
  { pernr: 'EMP004', empName: '赵敏', period: '2024-03', grossPay: 16000, deductions: 2960, netPay: 13040, status: 'CONFIRMED', bankAccount: '****3456', bankName: '农业银行' },
  { pernr: 'EMP005', empName: '刘强', period: '2024-03', grossPay: 32000, deductions: 5940, netPay: 26060, status: 'CONFIRMED', bankAccount: '****7890', bankName: '中国银行' },
  { pernr: 'EMP006', empName: '陈芳', period: '2024-03', grossPay: 12500, deductions: 2310, netPay: 10190, status: 'CONFIRMED', bankAccount: '****2345', bankName: '工商银行' },
  { pernr: 'EMP007', empName: '周杰', period: '2024-03', grossPay: 21000, deductions: 3890, netPay: 17110, status: 'DRAFT', bankAccount: '****6789', bankName: '建设银行' },
];

// 社保配置
const socialInsuranceRates = [
  { type: '养老保险', companyRate: '16%', employeeRate: '8%', base: '18000' },
  { type: '医疗保险', companyRate: '9.5%', employeeRate: '2%', base: '18000' },
  { type: '失业保险', companyRate: '0.5%', employeeRate: '0.5%', base: '18000' },
  { type: '工伤保险', companyRate: '0.2%', employeeRate: '-', base: '18000' },
  { type: '生育保险', companyRate: '0.8%', employeeRate: '-', base: '18000' },
  { type: '住房公积金', companyRate: '12%', employeeRate: '12%', base: '18000' },
];

export default function PayrollPage() {
  const [activeTab, setActiveTab] = useState('results');
  const [loading, setLoading] = useState(false);
  const [runModalVisible, setRunModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedEmployee, setSelectedEmployee] = useState<typeof mockPayrollResults[0] | null>(null);
  const [form] = Form.useForm();

  // 统计
  const stats = {
    totalEmployees: mockPayrollResults.length,
    totalGrossPay: mockPayrollResults.reduce((s, r) => s + r.grossPay, 0),
    totalDeductions: mockPayrollResults.reduce((s, r) => s + r.deductions, 0),
    totalNetPay: mockPayrollResults.reduce((s, r) => s + r.netPay, 0),
    confirmed: mockPayrollResults.filter(r => r.status === 'CONFIRMED').length,
    pending: mockPayrollResults.filter(r => r.status === 'DRAFT').length,
  };

  // 薪资结果列
  const columns = [
    { title: '员工号', dataIndex: 'pernr', width: 90 },
    { title: '姓名', dataIndex: 'empName', width: 100, render: (v: string) => <Space><Avatar size="small">{v[0]}</Avatar>{v}</Space> },
    { title: '薪资期间', dataIndex: 'period', width: 90 },
    { title: '应发合计', dataIndex: 'grossPay', width: 100, align: 'right' as const, render: (v: number) => <span style={{ color: '#52c41a', fontWeight: 'bold' }}>¥{v.toLocaleString()}</span> },
    { title: '扣款合计', dataIndex: 'deductions', width: 100, align: 'right' as const, render: (v: number) => <span style={{ color: '#ff4d4f' }}>¥{v.toLocaleString()}</span> },
    { title: '实发工资', dataIndex: 'netPay', width: 100, align: 'right' as const, render: (v: number) => <span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{v.toLocaleString()}</span> },
    { title: '银行账户', dataIndex: 'bankAccount', width: 100 },
    { title: '状态', dataIndex: 'status', width: 80, render: (s: string) => <Tag color={s === 'CONFIRMED' ? 'green' : 'orange'}>{s === 'CONFIRMED' ? '已确认' : '草稿'}</Tag> },
    { title: '操作', width: 120, render: (_: any, r: any) => (
      <Space>
        <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => { setSelectedEmployee(r); setDetailModalVisible(true); }}>明细</Button>
        <Button type="link" size="small" icon={<DownloadOutlined />}>薪资单</Button>
      </Space>
    )},
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card title="薪酬管理 (对标 SAP PC00_M00)"
        extra={<Space>
          <Button icon={<BankOutlined />}>社保配置</Button>
          <Button icon={<CalculatorOutlined />}>个税设置</Button>
          <Button type="primary" icon={<RocketOutlined />} onClick={() => setRunModalVisible(true)}>薪资核算</Button>
        </Space>}
      >
        <Tabs activeKey={activeTab} onChange={setActiveTab}
          items={[
            { key: 'results', label: <><DollarOutlined /> 薪资结果</> },
            { key: 'structure', label: <><FileTextOutlined /> 薪资结构</> },
            { key: 'social', label: <><BankOutlined /> 社保公积金</> },
            { key: 'tax', label: <><CalculatorOutlined /> 个税计算</> },
          ]} />

        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={4}><Card size="small"><Statistic title="核算人数" value={stats.totalEmployees} suffix="人" valueStyle={{ fontSize: 16 }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="应发总额" value={stats.totalGrossPay} prefix="¥" valueStyle={{ fontSize: 16, color: '#52c41a' }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="扣款总额" value={stats.totalDeductions} prefix="¥" valueStyle={{ fontSize: 16, color: '#ff4d4f' }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="实发总额" value={stats.totalNetPay} prefix="¥" valueStyle={{ fontSize: 16, color: '#1890ff' }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="已确认" value={stats.confirmed} suffix="人" valueStyle={{ fontSize: 16, color: '#52c41a' }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="待确认" value={stats.pending} suffix="人" valueStyle={{ fontSize: 16, color: '#faad14' }} /></Card></Col>
        </Row>

        {/* 薪资结果 */}
        {activeTab === 'results' && (
          <>
            <Form layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item><Select placeholder="薪资期间" defaultValue="2024-03" style={{ width: 120 }} options={[{ value: '2024-03', label: '2024年3月' }, { value: '2024-02', label: '2024年2月' }]} /></Form.Item>
              <Form.Item><Input placeholder="员工号/姓名" style={{ width: 120 }} /></Form.Item>
              <Form.Item><Select placeholder="状态" allowClear style={{ width: 100 }} options={[{ value: 'CONFIRMED', label: '已确认' }, { value: 'DRAFT', label: '草稿' }]} /></Form.Item>
              <Form.Item><Button type="primary">查询</Button></Form.Item>
              <Form.Item><Button icon={<DownloadOutlined />}>导出Excel</Button></Form.Item>
              <Form.Item><Button icon={<DownloadOutlined />}>银行文件</Button></Form.Item>
            </Form>
            <Table columns={columns} dataSource={mockPayrollResults} rowKey="pernr" size="small" loading={loading} pagination={{ defaultPageSize: 20 }} scroll={{ x: 1100 }} />
          </>
        )}

        {/* 薪资结构 */}
        {activeTab === 'structure' && (
          <Row gutter={24}>
            <Col span={12}>
              <Card title="收入项目" size="small">
                <Table
                  columns={[
                    { title: '编码', dataIndex: 'code', width: 70 },
                    { title: '名称', dataIndex: 'name' },
                    { title: '类型', dataIndex: 'category', width: 80, render: (v: string) => <Tag color="green">{v}</Tag> },
                    { title: '金额', dataIndex: 'amount', width: 100, align: 'right' as const, render: (v: number) => `¥${v.toLocaleString()}` },
                  ]}
                  dataSource={payrollItems.filter(i => i.type === 'EARNING')}
                  rowKey="code"
                  size="small"
                  pagination={false}
                />
              </Card>
            </Col>
            <Col span={12}>
              <Card title="扣款项目" size="small">
                <Table
                  columns={[
                    { title: '编码', dataIndex: 'code', width: 70 },
                    { title: '名称', dataIndex: 'name' },
                    { title: '类型', dataIndex: 'category', width: 80, render: (v: string) => <Tag color="red">{v}</Tag> },
                    { title: '金额', dataIndex: 'amount', width: 100, align: 'right' as const, render: (v: number) => `¥${v.toLocaleString()}` },
                  ]}
                  dataSource={payrollItems.filter(i => i.type === 'DEDUCTION')}
                  rowKey="code"
                  size="small"
                  pagination={false}
                />
              </Card>
            </Col>
          </Row>
        )}

        {/* 社保公积金 */}
        {activeTab === 'social' && (
          <>
            <Form layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item label="城市"><Select defaultValue="beijing" style={{ width: 120 }} options={[{ value: 'beijing', label: '北京' }, { value: 'shanghai', label: '上海' }]} /></Form.Item>
              <Form.Item label="生效日期"><DatePicker defaultValue={null} style={{ width: 120 }} /></Form.Item>
            </Form>
            <Table
              columns={[
                { title: '险种', dataIndex: 'type', width: 100 },
                { title: '单位比例', dataIndex: 'companyRate', width: 100 },
                { title: '个人比例', dataIndex: 'employeeRate', width: 100 },
                { title: '缴费基数', dataIndex: 'base', width: 100 },
                { title: '单位金额', width: 100, render: () => '¥2,880' },
                { title: '个人金额', width: 100, render: () => '¥1,440' },
              ]}
              dataSource={socialInsuranceRates}
              rowKey="type"
              size="small"
              pagination={false}
              bordered
            />
            <Divider>五险一金计算说明</Divider>
            <Card size="small">
              <Descriptions column={2} size="small">
                <Descriptions.Item label="社保基数下限">6,326 元</Descriptions.Item>
                <Descriptions.Item label="社保基数上限">33,891 元</Descriptions.Item>
                <Descriptions.Item label="公积金下限">2,420 元</Descriptions.Item>
                <Descriptions.Item label="公积金上限">33,891 元</Descriptions.Item>
              </Descriptions>
            </Card>
          </>
        )}

        {/* 个税计算 */}
        {activeTab === 'tax' && (
          <Row gutter={24}>
            <Col span={12}>
              <Card title="个税计算器" size="small">
                <Form layout="vertical">
                  <Row gutter={16}>
                    <Col span={12}><Form.Item label="税前工资"><Input type="number" defaultValue={26200} /></Form.Item></Col>
                    <Col span={12}><Form.Item label="社保公积金"><Input type="number" defaultValue={4050} /></Form.Item></Col>
                  </Row>
                  <Row gutter={16}>
                    <Col span={12}><Form.Item label="专项附加扣除"><Input type="number" defaultValue={3000} /></Form.Item></Col>
                    <Col span={12}><Form.Item label="累计收入"><Input type="number" defaultValue={78600} /></Form.Item></Col>
                  </Row>
                  <Form.Item><Button type="primary">计算个税</Button></Form.Item>
                </Form>
                <Divider />
                <Descriptions column={1} bordered size="small">
                  <Descriptions.Item label="累计应纳税所得额">¥45,750.00</Descriptions.Item>
                  <Descriptions.Item label="适用税率">10%</Descriptions.Item>
                  <Descriptions.Item label="速算扣除数">¥2,520.00</Descriptions.Item>
                  <Descriptions.Item label="累计应扣税额">¥2,055.00</Descriptions.Item>
                  <Descriptions.Item label="已扣税额">¥1,265.00</Descriptions.Item>
                  <Descriptions.Item label="本期应扣税额"><span style={{ fontSize: 18, color: '#ff4d4f', fontWeight: 'bold' }}>¥790.00</span></Descriptions.Item>
                </Descriptions>
              </Card>
            </Col>
            <Col span={12}>
              <Card title="税率表 (累计预扣法)" size="small">
                <Table
                  columns={[
                    { title: '级数', dataIndex: 'level', width: 50 },
                    { title: '累计应纳税所得额', dataIndex: 'range' },
                    { title: '税率', dataIndex: 'rate', width: 60 },
                    { title: '速算扣除数', dataIndex: 'deduction', width: 100 },
                  ]}
                  dataSource={[
                    { level: 1, range: '不超过36,000元', rate: '3%', deduction: '0' },
                    { level: 2, range: '超过36,000至144,000元', rate: '10%', deduction: '2,520' },
                    { level: 3, range: '超过144,000至300,000元', rate: '20%', deduction: '16,920' },
                    { level: 4, range: '超过300,000至420,000元', rate: '25%', deduction: '31,920' },
                    { level: 5, range: '超过420,000至660,000元', rate: '30%', deduction: '52,920' },
                    { level: 6, range: '超过660,000至960,000元', rate: '35%', deduction: '85,920' },
                    { level: 7, range: '超过960,000元', rate: '45%', deduction: '181,920' },
                  ]}
                  rowKey="level"
                  size="small"
                  pagination={false}
                />
              </Card>
            </Col>
          </Row>
        )}
      </Card>

      {/* 薪资核算弹窗 */}
      <Modal title="薪资核算" open={runModalVisible} onCancel={() => setRunModalVisible(false)} onOk={() => { message.success('核算完成'); setRunModalVisible(false); }} width={600}>
        <Form form={form} layout="vertical">
          <Row gutter={16}>
            <Col span={12}><Form.Item label="薪资期间" required><DatePicker picker="month" style={{ width: '100%' }} /></Form.Item></Col>
            <Col span={12}><Form.Item label="薪资范围"><Select placeholder="选择范围" style={{ width: '100%' }} options={[{ value: 'ALL', label: '全部员工' }, { value: 'TECH', label: '技术部' }]} /></Form.Item></Col>
          </Row>
          <Form.Item label="核算选项">
            <Checkbox.Group options={[
              { label: '重新计算', value: 'recalc' },
              { label: '包含离职员工', value: 'term' },
              { label: '生成薪资单', value: 'payslip' },
              { label: '生成银行文件', value: 'bank' },
            ]} defaultValue={['payslip']} />
          </Form.Item>
        </Form>
        <Divider>核算流程</Divider>
        <Steps direction="vertical" size="small" current={-1}
          items={[
            { title: '数据准备', description: '检查主数据、考勤数据、薪资变更' },
            { title: '基础计算', description: '计算应发工资、加班费、津贴' },
            { title: '扣款计算', description: '社保、公积金、个税、其他扣款' },
            { title: '生成结果', description: '薪资结果、薪资单、银行文件' },
          ]} />
      </Modal>

      {/* 薪资明细弹窗 */}
      <Modal title={`薪资明细 - ${selectedEmployee?.empName} (${selectedEmployee?.period})`} open={detailModalVisible} onCancel={() => { setDetailModalVisible(false); setSelectedEmployee(null); }} footer={null} width={800}>
        {selectedEmployee && (
          <>
            <Row gutter={16}>
              <Col span={8}><Card size="small"><Statistic title="应发工资" value={selectedEmployee.grossPay} prefix="¥" valueStyle={{ color: '#52c41a' }} /></Card></Col>
              <Col span={8}><Card size="small"><Statistic title="扣款合计" value={selectedEmployee.deductions} prefix="¥" valueStyle={{ color: '#ff4d4f' }} /></Card></Col>
              <Col span={8}><Card size="small"><Statistic title="实发工资" value={selectedEmployee.netPay} prefix="¥" valueStyle={{ color: '#1890ff' }} /></Card></Col>
            </Row>
            <Divider>收入明细</Divider>
            <Table columns={[
              { title: '项目', dataIndex: 'name' },
              { title: '金额', dataIndex: 'amount', align: 'right' as const, render: (v: number) => `¥${v.toLocaleString()}` },
            ]} dataSource={payrollItems.filter(i => i.type === 'EARNING')} rowKey="code" size="small" pagination={false} />
            <Divider>扣款明细</Divider>
            <Table columns={[
              { title: '项目', dataIndex: 'name' },
              { title: '金额', dataIndex: 'amount', align: 'right' as const, render: (v: number) => `¥${v.toLocaleString()}` },
            ]} dataSource={payrollItems.filter(i => i.type === 'DEDUCTION')} rowKey="code" size="small" pagination={false} />
          </>
        )}
      </Modal>
    </div>
  );
}

// 添加 Checkbox 组件
import { Checkbox } from 'antd';
