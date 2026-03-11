'use client';

import { useState } from 'react';
import {
  Card, Table, Button, Space, Modal, Form, Input, Select, DatePicker,
  Steps, Descriptions, Tag, message, Row, Col, Statistic, Timeline,
  Avatar, Tabs, Divider, List, Result,
} from 'antd';
import {
  UserAddOutlined, UserDeleteOutlined, SwapOutlined,
  RiseOutlined, FallOutlined, CheckCircleOutlined,
  ClockCircleOutlined, CloseCircleOutlined, ExclamationCircleOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;

// 模拟措施类型
const actionTypes = [
  { code: 'HIRE', name: '录用', icon: <UserAddOutlined />, color: 'green', infotypes: ['IT0000', 'IT0001', 'IT0002', 'IT0006', 'IT0008', 'IT0016'] },
  { code: 'TERM', name: '离职', icon: <UserDeleteOutlined />, color: 'red', infotypes: ['IT0000', 'IT0001'] },
  { code: 'TRANS', name: '调动', icon: <SwapOutlined />, color: 'blue', infotypes: ['IT0000', 'IT0001'] },
  { code: 'PROM', name: '晋升', icon: <RiseOutlined />, color: 'gold', infotypes: ['IT0000', 'IT0001', 'IT0008'] },
  { code: 'DEMO', name: '降职', icon: <FallOutlined />, color: 'orange', infotypes: ['IT0000', 'IT0001', 'IT0008'] },
  { code: 'CONF', name: '转正', icon: <CheckCircleOutlined />, color: 'cyan', infotypes: ['IT0000'] },
  { code: 'RENEW', name: '合同续签', icon: <CheckCircleOutlined />, color: 'purple', infotypes: ['IT0016'] },
];

// 模拟措施记录
const mockActions = [
  { id: '1', pernr: 'EMP015', empName: '新员工A', actionType: 'HIRE', actionName: '录用', effectiveDate: '2024-04-01', status: 'PENDING', reason: '新入职', createdAt: '2024-03-20', currentStep: 1 },
  { id: '2', pernr: 'EMP003', empName: '王磊', actionType: 'PROM', actionName: '晋升', effectiveDate: '2024-04-01', status: 'APPROVED', reason: '表现优秀', createdAt: '2024-03-18', currentStep: 3 },
  { id: '3', pernr: 'EMP008', empName: '吴静', actionType: 'TRANS', actionName: '调动', effectiveDate: '2024-03-15', status: 'COMPLETED', reason: '业务需要', createdAt: '2024-03-10', currentStep: 4 },
  { id: '4', pernr: 'EMP016', empName: '离职员工B', actionType: 'TERM', actionName: '离职', effectiveDate: '2024-03-31', status: 'PENDING', reason: '个人原因', createdAt: '2024-03-15', currentStep: 2 },
  { id: '5', pernr: 'EMP006', empName: '陈芳', actionType: 'CONF', actionName: '转正', effectiveDate: '2024-04-08', status: 'DRAFT', reason: '试用期结束', createdAt: '2024-03-20', currentStep: 0 },
];

const statusConfig: Record<string, { color: string; text: string; icon: any }> = {
  DRAFT: { color: 'default', text: '草稿', icon: <ExclamationCircleOutlined /> },
  PENDING: { color: 'processing', text: '审批中', icon: <ClockCircleOutlined /> },
  APPROVED: { color: 'success', text: '已批准', icon: <CheckCircleOutlined /> },
  REJECTED: { color: 'error', text: '已拒绝', icon: <CloseCircleOutlined /> },
  COMPLETED: { color: 'cyan', text: '已完成', icon: <CheckCircleOutlined /> },
};

export default function PersonnelActionsPage() {
  const [loading, setLoading] = useState(false);
  const [selectedActionType, setSelectedActionType] = useState<string | null>(null);
  const [wizardVisible, setWizardVisible] = useState(false);
  const [wizardStep, setWizardStep] = useState(0);
  const [detailVisible, setDetailVisible] = useState(false);
  const [selectedAction, setSelectedAction] = useState<typeof mockActions[0] | null>(null);
  const [form] = Form.useForm();

  // 措施列表列
  const columns = [
    { title: '措施编号', dataIndex: 'id', width: 80 },
    { title: '员工号', dataIndex: 'pernr', width: 100 },
    { title: '姓名', dataIndex: 'empName', width: 100, render: (v: string) => <Space><Avatar size="small">{v[0]}</Avatar>{v}</Space> },
    { title: '措施类型', dataIndex: 'actionName', width: 100, render: (v: string, r: any) => {
      const type = actionTypes.find(t => t.code === r.actionType);
      return <Tag color={type?.color} icon={type?.icon}>{v}</Tag>;
    }},
    { title: '生效日期', dataIndex: 'effectiveDate', width: 100 },
    { title: '原因', dataIndex: 'reason', ellipsis: true },
    { title: '状态', dataIndex: 'status', width: 100, render: (s: string) => <Tag color={statusConfig[s]?.color} icon={statusConfig[s]?.icon}>{statusConfig[s]?.text}</Tag> },
    { title: '创建时间', dataIndex: 'createdAt', width: 100 },
    { title: '操作', width: 150, render: (_: any, r: any) => (
      <Space size="small">
        <Button type="link" size="small" onClick={() => { setSelectedAction(r); setDetailVisible(true); }}>详情</Button>
        {r.status === 'DRAFT' && <Button type="link" size="small">提交</Button>}
        {r.status === 'PENDING' && <Button type="link" size="small">审批</Button>}
      </Space>
    )},
  ];

  // 统计
  const stats = {
    total: mockActions.length,
    pending: mockActions.filter(a => a.status === 'PENDING').length,
    draft: mockActions.filter(a => a.status === 'DRAFT').length,
    completed: mockActions.filter(a => a.status === 'COMPLETED').length,
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="人事措施 (对标 SAP PA40)"
        extra={<Button type="primary" icon={<UserAddOutlined />} onClick={() => setWizardVisible(true)}>执行措施</Button>}
      >
        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={4}><Card size="small"><Statistic title="总措施" value={stats.total} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="待审批" value={stats.pending} valueStyle={{ color: '#1890ff' }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="草稿" value={stats.draft} valueStyle={{ color: '#8c8c8c' }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="已完成" value={stats.completed} valueStyle={{ color: '#52c41a' }} /></Card></Col>
        </Row>

        {/* 快捷措施按钮 */}
        <Card title="快捷措施" size="small" style={{ marginBottom: 16 }}>
          <Space wrap>
            {actionTypes.map(type => (
              <Button key={type.code} icon={type.icon} onClick={() => { setSelectedActionType(type.code); setWizardVisible(true); }}>
                {type.name}
              </Button>
            ))}
          </Space>
        </Card>

        {/* 措施列表 */}
        <Form layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item><Input placeholder="员工号/姓名" style={{ width: 120 }} /></Form.Item>
          <Form.Item><Select placeholder="措施类型" allowClear style={{ width: 100 }} options={actionTypes.map(t => ({ value: t.code, label: t.name }))} /></Form.Item>
          <Form.Item><Select placeholder="状态" allowClear style={{ width: 100 }} options={Object.entries(statusConfig).map(([k, v]) => ({ value: k, label: v.text }))} /></Form.Item>
          <Form.Item><Button type="primary">查询</Button></Form.Item>
        </Form>

        <Table columns={columns} dataSource={mockActions} rowKey="id" size="small" loading={loading} pagination={{ defaultPageSize: 20 }} />
      </Card>

      {/* 措施向导弹窗 */}
      <Modal
        title={`执行人事措施 - ${actionTypes.find(t => t.code === selectedActionType)?.name || ''}`}
        open={wizardVisible}
        onCancel={() => { setWizardVisible(false); setWizardStep(0); }}
        onOk={() => { if (wizardStep < 3) setWizardStep(wizardStep + 1); else { message.success('措施已提交'); setWizardVisible(false); setWizardStep(0); } }}
        okText={wizardStep === 3 ? '提交' : '下一步'}
        cancelText={wizardStep === 0 ? '取消' : '上一步'}
        width={800}
      >
        <Steps current={wizardStep} size="small" style={{ marginBottom: 24 }}
          items={[
            { title: '选择员工' },
            { title: '填写信息' },
            { title: '确认变更' },
            { title: '提交审批' },
          ]} />

        {wizardStep === 0 && (
          <Form form={form} layout="vertical">
            <Form.Item label="员工号" required><Input placeholder="输入员工号" /></Form.Item>
            <Form.Item label="措施类型" required>
              <Select value={selectedActionType} onChange={setSelectedActionType} options={actionTypes.map(t => ({ value: t.code, label: t.name }))} />
            </Form.Item>
            <Form.Item label="生效日期" required><DatePicker style={{ width: '100%' }} /></Form.Item>
          </Form>
        )}
        {wizardStep === 1 && (
          <Card size="small" title="信息类型变更">
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="组织单位">技术部 → 前端开发组</Descriptions.Item>
              <Descriptions.Item label="职位">工程师 → 高级工程师</Descriptions.Item>
              <Descriptions.Item label="职务">工程师 → 高级工程师</Descriptions.Item>
              <Descriptions.Item label="基本工资">¥16,000 → ¥22,000</Descriptions.Item>
            </Descriptions>
          </Card>
        )}
        {wizardStep === 2 && (
          <div>
            <Result status="info" title="确认变更信息" subTitle="请确认以下变更将更新信息类型记录" />
            <List size="small" bordered
              header="涉及信息类型"
              dataSource={actionTypes.find(t => t.code === selectedActionType)?.infotypes || []}
              renderItem={item => <List.Item>{item}</List.Item>} />
          </div>
        )}
        {wizardStep === 3 && (
          <div>
            <Card size="small" title="审批流程">
              <Steps direction="vertical" size="small"
                items={[
                  { title: '直线经理审批', status: 'process', description: '待审批' },
                  { title: 'HR 复核', status: 'wait', description: '等待上一节点' },
                  { title: '生效', status: 'wait', description: '系统自动执行' },
                ]} />
            </Card>
          </div>
        )}
      </Modal>

      {/* 详情弹窗 */}
      <Modal
        title={`措施详情 - ${selectedAction?.id}`}
        open={detailVisible}
        onCancel={() => { setDetailVisible(false); setSelectedAction(null); }}
        footer={[
          <Button key="close" onClick={() => { setDetailVisible(false); setSelectedAction(null); }}>关闭</Button>,
        ]}
        width={700}
      >
        {selectedAction && (
          <>
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="员工">{selectedAction.empName} ({selectedAction.pernr})</Descriptions.Item>
              <Descriptions.Item label="措施类型"><Tag color={actionTypes.find(t => t.code === selectedAction.actionType)?.color}>{selectedAction.actionName}</Tag></Descriptions.Item>
              <Descriptions.Item label="生效日期">{selectedAction.effectiveDate}</Descriptions.Item>
              <Descriptions.Item label="状态"><Tag color={statusConfig[selectedAction.status]?.color}>{statusConfig[selectedAction.status]?.text}</Tag></Descriptions.Item>
              <Descriptions.Item label="原因" span={2}>{selectedAction.reason}</Descriptions.Item>
            </Descriptions>
            <Divider>审批进度</Divider>
            <Steps current={selectedAction.currentStep} size="small"
              items={[
                { title: '提交', status: 'finish' },
                { title: '经理审批', status: selectedAction.currentStep >= 1 ? 'finish' : 'wait' },
                { title: 'HR复核', status: selectedAction.currentStep >= 2 ? 'finish' : 'wait' },
                { title: '生效', status: selectedAction.currentStep >= 3 ? 'finish' : 'wait' },
              ]} />
          </>
        )}
      </Modal>
    </div>
  );
}
