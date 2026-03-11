'use client';

import { useState } from 'react';
import {
  Card, Table, Button, Space, Modal, Form, Input, Select, DatePicker,
  Tag, message, Row, Col, Statistic, Tabs, Descriptions, List, Avatar,
  Timeline, Progress, Badge, Steps, Upload, Divider,
} from 'antd';
import {
  CalendarOutlined, CheckCircleOutlined, CloseCircleOutlined,
  ClockCircleOutlined, PlusOutlined, FileTextOutlined,
  UserOutlined, AuditOutlined,
} from '@ant-design/icons';

const { RangePicker } = DatePicker;

// 假期类型配置
const leaveTypes: Record<string, { name: string; paid: boolean; color: string }> = {
  ANNUAL: { name: '年假', paid: true, color: 'green' },
  SICK: { name: '病假', paid: true, color: 'red' },
  PERSONAL: { name: '事假', paid: false, color: 'orange' },
  MARRIAGE: { name: '婚假', paid: true, color: 'magenta' },
  MATERNITY: { name: '产假', paid: true, color: 'purple' },
  PATERNITY: { name: '陪产假', paid: true, color: 'cyan' },
  BEREAVEMENT: { name: '丧假', paid: true, color: 'volcano' },
  COMPENSATORY: { name: '调休', paid: true, color: 'blue' },
};

const statusConfig: Record<string, { text: string; color: string }> = {
  DRAFT: { text: '草稿', color: 'default' },
  PENDING: { text: '待审批', color: 'processing' },
  APPROVED: { text: '已批准', color: 'success' },
  REJECTED: { text: '已拒绝', color: 'error' },
  CANCELLED: { text: '已撤销', color: 'default' },
};

// 模拟请假申请
const mockLeaveRequests = [
  { id: 'LR001', pernr: 'EMP003', empName: '王磊', leaveType: 'ANNUAL', startDate: '2024-03-25', endDate: '2024-03-27', days: 3, reason: '个人事务', status: 'PENDING', createdAt: '2024-03-20', approver: '张伟' },
  { id: 'LR002', pernr: 'EMP006', empName: '陈芳', leaveType: 'SICK', startDate: '2024-03-21', endDate: '2024-03-21', days: 1, reason: '身体不适', status: 'APPROVED', createdAt: '2024-03-20', approver: '刘强' },
  { id: 'LR003', pernr: 'EMP008', empName: '吴静', leaveType: 'PERSONAL', startDate: '2024-03-28', endDate: '2024-03-29', days: 2, reason: '家中有事', status: 'REJECTED', createdAt: '2024-03-19', approver: '王磊' },
  { id: 'LR004', pernr: 'EMP007', empName: '周杰', leaveType: 'COMPENSATORY', startDate: '2024-03-22', endDate: '2024-03-22', days: 1, reason: '调休', status: 'APPROVED', createdAt: '2024-03-18', approver: '张伟' },
  { id: 'LR005', pernr: 'EMP011', empName: '李明', leaveType: 'ANNUAL', startDate: '2024-04-01', endDate: '2024-04-05', days: 5, reason: '清明节回乡', status: 'PENDING', createdAt: '2024-03-20', approver: '张伟' },
];

// 假期余额
const mockLeaveBalance = [
  { pernr: 'EMP001', empName: '张伟', year: 2024, annualTotal: 15, annualUsed: 5, annualBal: 10, sickTotal: 12, sickUsed: 2, sickBal: 10, compTotal: 3, compUsed: 0, compBal: 3 },
  { pernr: 'EMP003', empName: '王磊', year: 2024, annualTotal: 10, annualUsed: 6, annualBal: 4, sickTotal: 12, sickUsed: 1, sickBal: 11, compTotal: 2, compUsed: 1, compBal: 1 },
  { pernr: 'EMP006', empName: '陈芳', year: 2024, annualTotal: 10, annualUsed: 3, annualBal: 7, sickTotal: 12, sickUsed: 2, sickBal: 10, compTotal: 1, compUsed: 0, compBal: 1 },
  { pernr: 'EMP007', empName: '周杰', year: 2024, annualTotal: 10, annualUsed: 2, annualBal: 8, sickTotal: 12, sickUsed: 0, sickBal: 12, compTotal: 5, compUsed: 1, compBal: 4 },
  { pernr: 'EMP011', empName: '李明', year: 2024, annualTotal: 10, annualUsed: 4, annualBal: 6, sickTotal: 12, sickUsed: 1, sickBal: 11, compTotal: 2, compUsed: 0, compBal: 2 },
];

export default function LeavePage() {
  const [activeTab, setActiveTab] = useState('request');
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [balanceModalVisible, setBalanceModalVisible] = useState(false);
  const [selectedRequest, setSelectedRequest] = useState<typeof mockLeaveRequests[0] | null>(null);
  const [form] = Form.useForm();

  const columns = [
    { title: '申请单号', dataIndex: 'id', width: 90 },
    { title: '员工', dataIndex: 'empName', width: 100, render: (v: string, r: any) => <Space><Avatar size="small">{v[0]}</Avatar><span>{v}</span></Space> },
    { title: '假期类型', dataIndex: 'leaveType', width: 100, render: (t: string) => <Tag color={leaveTypes[t]?.color}>{leaveTypes[t]?.name}</Tag> },
    { title: '开始日期', dataIndex: 'startDate', width: 100 },
    { title: '结束日期', dataIndex: 'endDate', width: 100 },
    { title: '天数', dataIndex: 'days', width: 60, render: (v: number) => <Badge count={v} style={{ backgroundColor: '#52c41a' }} /> },
    { title: '原因', dataIndex: 'reason', ellipsis: true },
    { title: '状态', dataIndex: 'status', width: 80, render: (s: string) => <Tag color={statusConfig[s]?.color}>{statusConfig[s]?.text}</Tag> },
    { title: '申请时间', dataIndex: 'createdAt', width: 100 },
    { title: '操作', width: 180, render: (_: any, r: any) => (
      <Space size="small">
        <Button type="link" size="small" onClick={() => { setSelectedRequest(r); setDetailVisible(true); }}>详情</Button>
        {r.status === 'PENDING' && <>
          <Button type="link" size="small" style={{ color: '#52c41a' }}>审批</Button>
          <Button type="link" size="small" danger>撤销</Button>
        </>}
      </Space>
    )},
  ];

  const balanceColumns = [
    { title: '员工', dataIndex: 'empName', width: 100, render: (v: string) => <Space><Avatar size="small">{v[0]}</Avatar>{v}</Space> },
    { title: '年度', dataIndex: 'year', width: 80 },
    { title: '年假余额', children: [
      { title: '总额', dataIndex: 'annualTotal', width: 60 },
      { title: '已用', dataIndex: 'annualUsed', width: 60 },
      { title: '余额', dataIndex: 'annualBal', width: 60, render: (v: number) => <span style={{ color: v > 3 ? '#52c41a' : '#faad14' }}>{v}</span> },
    ]},
    { title: '病假余额', children: [
      { title: '总额', dataIndex: 'sickTotal', width: 60 },
      { title: '已用', dataIndex: 'sickUsed', width: 60 },
      { title: '余额', dataIndex: 'sickBal', width: 60 },
    ]},
    { title: '调休余额', children: [
      { title: '总额', dataIndex: 'compTotal', width: 60 },
      { title: '已用', dataIndex: 'compUsed', width: 60 },
      { title: '余额', dataIndex: 'compBal', width: 60 },
    ]},
  ];

  const stats = {
    total: mockLeaveRequests.length,
    pending: mockLeaveRequests.filter(r => r.status === 'PENDING').length,
    approved: mockLeaveRequests.filter(r => r.status === 'APPROVED').length,
    rejected: mockLeaveRequests.filter(r => r.status === 'REJECTED').length,
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="请假管理 (对标 SAP PT60/PT50)"
        extra={<Space>
          <Button onClick={() => setBalanceModalVisible(true)}>假期余额</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalVisible(true)}>新建申请</Button>
        </Space>}
      >
        <Tabs activeKey={activeTab} onChange={setActiveTab}
          items={[
            { key: 'request', label: <><FileTextOutlined /> 请假申请</> },
            { key: 'approval', label: <><AuditOutlined /> 待我审批</> },
            { key: 'balance', label: <><CalendarOutlined /> 假期余额</> },
          ]} />

        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={4}><Card size="small"><Statistic title="本月申请" value={stats.total} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="待审批" value={stats.pending} valueStyle={{ color: '#1890ff' }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="已批准" value={stats.approved} valueStyle={{ color: '#52c41a' }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="已拒绝" value={stats.rejected} valueStyle={{ color: '#ff4d4f' }} /></Card></Col>
        </Row>

        {activeTab === 'request' && (
          <>
            <Form layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item><Input placeholder="员工号/姓名" style={{ width: 120 }} /></Form.Item>
              <Form.Item><Select placeholder="假期类型" allowClear style={{ width: 100 }} options={Object.entries(leaveTypes).map(([k, v]) => ({ value: k, label: v.name }))} /></Form.Item>
              <Form.Item><Select placeholder="状态" allowClear style={{ width: 100 }} options={Object.entries(statusConfig).map(([k, v]) => ({ value: k, label: v.text }))} /></Form.Item>
              <Form.Item><Button type="primary">查询</Button></Form.Item>
            </Form>
            <Table columns={columns} dataSource={mockLeaveRequests} rowKey="id" size="small" loading={loading} pagination={{ defaultPageSize: 20 }} scroll={{ x: 1200 }} />
          </>
        )}

        {activeTab === 'approval' && (
          <List
            itemLayout="horizontal"
            dataSource={mockLeaveRequests.filter(r => r.status === 'PENDING')}
            renderItem={item => (
              <List.Item actions={[
                <Button type="primary" size="small" key="approve">批准</Button>,
                <Button danger size="small" key="reject">拒绝</Button>,
              ]}>
                <List.Item.Meta
                  avatar={<Avatar>{item.empName[0]}</Avatar>}
                  title={<>{item.empName} 申请 <Tag color={leaveTypes[item.leaveType]?.color}>{leaveTypes[item.leaveType]?.name}</Tag></>}
                  description={`${item.startDate} ~ ${item.endDate} (${item.days}天) - ${item.reason}`}
                />
                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: 12, color: '#8c8c8c' }}>申请于 {item.createdAt}</div>
                </div>
              </List.Item>
            )}
          />
        )}

        {activeTab === 'balance' && (
          <Table columns={balanceColumns} dataSource={mockLeaveBalance} rowKey="pernr" size="small" loading={loading} bordered pagination={{ defaultPageSize: 20 }} />
        )}
      </Card>

      {/* 新建请假弹窗 */}
      <Modal title="新建请假申请" open={modalVisible} onCancel={() => setModalVisible(false)} onOk={() => { message.success('申请已提交'); setModalVisible(false); }} width={600}>
        <Form form={form} layout="vertical">
          <Row gutter={16}>
            <Col span={12}><Form.Item label="假期类型" required><Select options={Object.entries(leaveTypes).map(([k, v]) => ({ value: k, label: `${v.name} (${v.paid ? '带薪' : '无薪'})` }))} /></Form.Item></Col>
            <Col span={12}><Form.Item label="请假天数"><Input type="number" disabled style={{ width: '100%' }} /></Form.Item></Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}><Form.Item label="开始日期" required><DatePicker style={{ width: '100%' }} /></Form.Item></Col>
            <Col span={12}><Form.Item label="结束日期" required><DatePicker style={{ width: '100%' }} /></Form.Item></Col>
          </Row>
          <Form.Item label="请假原因" required><Input.TextArea rows={3} placeholder="请输入请假原因" /></Form.Item>
          <Form.Item label="附件"><Upload><Button>上传附件</Button></Upload></Form.Item>
        </Form>
      </Modal>

      {/* 详情弹窗 */}
      <Modal title="请假详情" open={detailVisible} onCancel={() => { setDetailVisible(false); setSelectedRequest(null); }} footer={null} width={700}>
        {selectedRequest && (
          <>
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="申请单号">{selectedRequest.id}</Descriptions.Item>
              <Descriptions.Item label="申请人"><Space><Avatar>{selectedRequest.empName[0]}</Avatar>{selectedRequest.empName}</Space></Descriptions.Item>
              <Descriptions.Item label="假期类型"><Tag color={leaveTypes[selectedRequest.leaveType]?.color}>{leaveTypes[selectedRequest.leaveType]?.name}</Tag></Descriptions.Item>
              <Descriptions.Item label="状态"><Tag color={statusConfig[selectedRequest.status]?.color}>{statusConfig[selectedRequest.status]?.text}</Tag></Descriptions.Item>
              <Descriptions.Item label="开始日期">{selectedRequest.startDate}</Descriptions.Item>
              <Descriptions.Item label="结束日期">{selectedRequest.endDate}</Descriptions.Item>
              <Descriptions.Item label="请假天数">{selectedRequest.days}天</Descriptions.Item>
              <Descriptions.Item label="申请时间">{selectedRequest.createdAt}</Descriptions.Item>
              <Descriptions.Item label="请假原因" span={2}>{selectedRequest.reason}</Descriptions.Item>
            </Descriptions>
            <Divider>审批流程</Divider>
            <Steps current={selectedRequest.status === 'APPROVED' ? 2 : selectedRequest.status === 'PENDING' ? 1 : 0} size="small"
              items={[
                { title: '提交申请', status: 'finish', icon: <UserOutlined /> },
                { title: '经理审批', status: selectedRequest.status === 'PENDING' ? 'process' : 'finish', icon: <AuditOutlined /> },
                { title: '审批完成', status: selectedRequest.status === 'APPROVED' ? 'finish' : 'wait', icon: <CheckCircleOutlined /> },
              ]} />
          </>
        )}
      </Modal>

      {/* 假期余额弹窗 */}
      <Modal title="假期余额查询" open={balanceModalVisible} onCancel={() => setBalanceModalVisible(false)} footer={null} width={600}>
        <Form layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item><Input placeholder="员工号" style={{ width: 120 }} /></Form.Item>
          <Form.Item><Button type="primary">查询</Button></Form.Item>
        </Form>
        <Card size="small">
          <Row gutter={16}>
            <Col span={8}>
              <Progress type="circle" percent={67} format={() => '10/15天'} />
              <div style={{ textAlign: 'center', marginTop: 8 }}>年假</div>
            </Col>
            <Col span={8}>
              <Progress type="circle" percent={83} format={() => '10/12天'} strokeColor="#52c41a" />
              <div style={{ textAlign: 'center', marginTop: 8 }}>病假</div>
            </Col>
            <Col span={8}>
              <Progress type="circle" percent={100} format={() => '3/3天'} strokeColor="#1890ff" />
              <div style={{ textAlign: 'center', marginTop: 8 }}>调休</div>
            </Col>
          </Row>
        </Card>
      </Modal>
    </div>
  );
}
