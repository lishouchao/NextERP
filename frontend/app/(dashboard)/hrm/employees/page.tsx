'use client';

import { useState } from 'react';
import {
  Card, Table, Form, Input, Select, Button, Space, Row, Col, Statistic,
  Tag, Modal, DatePicker, Tabs, Avatar, Descriptions, Timeline, message, Tree, Progress,
} from 'antd';
import {
  SearchOutlined, PlusOutlined, EditOutlined, ReloadOutlined, UserOutlined,
  PhoneOutlined, MailOutlined, IdcardOutlined, TeamOutlined, ManOutlined, WomanOutlined,
} from '@ant-design/icons';

const mockEmployees = [
  { id: 1, empNo: 'EMP001', name: '张伟', gender: 'M', birthDate: '1985-03-15', department: '技术部', position: '技术总监', level: 'P8', status: 1, phone: '13800138001', email: 'zhangwei@company.com', hireDate: '2018-06-01', workYears: 5, education: '硕士', salary: 35000 },
  { id: 2, empNo: 'EMP002', name: '李娜', gender: 'F', birthDate: '1990-07-22', department: '人力资源部', position: 'HR经理', level: 'P7', status: 1, phone: '13900139002', email: 'lina@company.com', hireDate: '2019-03-15', workYears: 4, education: '本科', salary: 25000 },
  { id: 3, empNo: 'EMP003', name: '王磊', gender: 'M', birthDate: '1992-11-08', department: '技术部', position: '高级工程师', level: 'P6', status: 1, phone: '13700137003', email: 'wanglei@company.com', hireDate: '2020-01-10', workYears: 3, education: '硕士', salary: 28000 },
  { id: 4, empNo: 'EMP004', name: '赵敏', gender: 'F', birthDate: '1995-05-30', department: '财务部', position: '财务主管', level: 'P6', status: 1, phone: '13600136004', email: 'zhaomin@company.com', hireDate: '2020-07-01', workYears: 3, education: '本科', salary: 22000 },
  { id: 5, empNo: 'EMP005', name: '刘强', gender: 'M', birthDate: '1988-09-12', department: '销售部', position: '销售总监', level: 'P8', status: 1, phone: '13500135005', email: 'liuqiang@company.com', hireDate: '2017-09-01', workYears: 6, education: '本科', salary: 32000 },
  { id: 6, empNo: 'EMP006', name: '陈芳', gender: 'F', birthDate: '1993-02-28', department: '销售部', position: '销售经理', level: 'P5', status: 1, phone: '13400134006', email: 'chenfang@company.com', hireDate: '2021-03-08', workYears: 2, education: '本科', salary: 18000 },
  { id: 7, empNo: 'EMP007', name: '周杰', gender: 'M', birthDate: '1994-12-05', department: '技术部', position: '工程师', level: 'P5', status: 1, phone: '13300133007', email: 'zhoujie@company.com', hireDate: '2021-06-15', workYears: 2, education: '本科', salary: 16000 },
  { id: 8, empNo: 'EMP008', name: '吴静', gender: 'F', birthDate: '1996-08-18', department: '人力资源部', position: 'HR专员', level: 'P4', status: 1, phone: '13200132008', email: 'wujing@company.com', hireDate: '2022-02-14', workYears: 1, education: '本科', salary: 12000 },
];

const mockDepartments = [
  { code: 'DEPT-001', name: '技术部', manager: '张伟', headcount: 15 },
  { code: 'DEPT-002', name: '人力资源部', manager: '李娜', headcount: 5 },
  { code: 'DEPT-003', name: '财务部', manager: '赵敏', headcount: 8 },
  { code: 'DEPT-004', name: '销售部', manager: '刘强', headcount: 12 },
];

const levelConfig: Record<string, { color: string }> = {
  'P3': { color: 'default' }, 'P4': { color: 'green' }, 'P5': { color: 'blue' },
  'P6': { color: 'orange' }, 'P7': { color: 'purple' }, 'P8': { color: 'red' },
};

const statusConfig: Record<number, { color: string; text: string }> = {
  1: { color: 'green', text: '在职' }, 2: { color: 'orange', text: '休假' },
  3: { color: 'red', text: '离职' }, 4: { color: 'default', text: '试用期' },
};

export default function EmployeesPage() {
  const [loading, setLoading] = useState(false);
  const [employees] = useState(mockEmployees);
  const [activeTab, setActiveTab] = useState('list');
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [selected, setSelected] = useState<typeof mockEmployees[0] | null>(null);
  const [form] = Form.useForm();

  const columns = [
    { title: '工号', dataIndex: 'empNo', width: 90, fixed: 'left' as const },
    { title: '姓名', dataIndex: 'name', width: 100, render: (text: string, r: any) => (
      <Space><Avatar size="small" icon={<UserOutlined />} style={{ backgroundColor: r.gender === 'M' ? '#1890ff' : '#eb2f96' }} />
        <a onClick={() => { setSelected(r); setDetailVisible(true); }}>{text}</a></Space>
    )},
    { title: '性别', dataIndex: 'gender', width: 60, render: (g: string) => g === 'M' ? <ManOutlined style={{ color: '#1890ff' }} /> : <WomanOutlined style={{ color: '#eb2f96' }} /> },
    { title: '部门', dataIndex: 'department', width: 110 },
    { title: '职位', dataIndex: 'position', width: 100 },
    { title: '职级', dataIndex: 'level', width: 70, render: (l: string) => <Tag color={levelConfig[l]?.color}>{l}</Tag> },
    { title: '联系电话', dataIndex: 'phone', width: 120 },
    { title: '入职日期', dataIndex: 'hireDate', width: 100 },
    { title: '工龄', dataIndex: 'workYears', width: 70, render: (v: number) => `${v}年` },
    { title: '状态', dataIndex: 'status', width: 80, render: (s: number) => <Tag color={statusConfig[s]?.color}>{statusConfig[s]?.text}</Tag> },
    { title: '操作', key: 'action', width: 150, fixed: 'right' as const, render: (_: unknown, r: any) => (
      <Space size="small">
        <Button type="link" size="small" onClick={() => { setSelected(r); setDetailVisible(true); }}>详情</Button>
        <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
      </Space>
    )},
  ];

  const stats = {
    total: employees.length, active: employees.filter(e => e.status === 1).length,
    male: employees.filter(e => e.gender === 'M').length, female: employees.filter(e => e.gender === 'F').length,
    avgYears: (employees.reduce((s, e) => s + e.workYears, 0) / employees.length).toFixed(1),
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="员工管理 (对标 SAP PA20/PA30)"
        extra={<Space>
          <Button icon={<ReloadOutlined />} onClick={() => setLoading(!loading)}>刷新</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalVisible(true)}>新增员工</Button>
        </Space>}>
        <Tabs activeKey={activeTab} onChange={setActiveTab}
          items={[{ key: 'list', label: <><UserOutlined /> 员工列表</> }, { key: 'org', label: <><TeamOutlined /> 组织架构</> }]} />

        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={4}><Card size="small"><Statistic title="员工总数" value={stats.total} suffix="人" valueStyle={{ fontSize: 18 }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="在职" value={stats.active} suffix="人" valueStyle={{ fontSize: 18, color: '#52c41a' }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="男性" value={stats.male} suffix="人" valueStyle={{ fontSize: 18, color: '#1890ff' }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="女性" value={stats.female} suffix="人" valueStyle={{ fontSize: 18, color: '#eb2f96' }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="平均工龄" value={stats.avgYears} suffix="年" valueStyle={{ fontSize: 18 }} /></Card></Col>
          <Col span={4}><Card size="small"><Progress type="circle" percent={Math.round((stats.male / stats.total) * 100)} size={50} format={() => '男占比'} /></Card></Col>
        </Row>

        {activeTab === 'list' && (
          <>
            <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item name="keyword" label="搜索"><Input placeholder="工号/姓名" style={{ width: 120 }} /></Form.Item>
              <Form.Item name="department" label="部门">
                <Select placeholder="全部" allowClear style={{ width: 120 }} options={mockDepartments.map(d => ({ value: d.name, label: d.name }))} /></Form.Item>
              <Form.Item name="status" label="状态">
                <Select placeholder="全部" allowClear style={{ width: 100 }} options={Object.entries(statusConfig).map(([k, v]) => ({ value: Number(k), label: v.text }))} /></Form.Item>
              <Form.Item><Button type="primary" icon={<SearchOutlined />}>查询</Button></Form.Item>
            </Form>
            <Table columns={columns} dataSource={employees} rowKey="id" loading={loading} size="small" scroll={{ x: 1200 }} pagination={{ defaultPageSize: 20 }} />
          </>
        )}

        {activeTab === 'org' && (
          <Row gutter={24}>
            <Col span={8}>
              <Card title="部门结构" size="small">
                <Tree defaultExpandedKeys={['all']} treeData={[{ title: '总公司', key: 'all', children: mockDepartments.map(d => ({ title: `${d.name} (${d.headcount}人)`, key: d.code })) }]} />
              </Card>
            </Col>
            <Col span={16}>
              <Card title="部门概览" size="small">
                <Table columns={[
                  { title: '部门', dataIndex: 'name', width: 120 }, { title: '负责人', dataIndex: 'manager', width: 100 }, { title: '人数', dataIndex: 'headcount', width: 80 },
                ]} dataSource={mockDepartments} rowKey="code" size="small" pagination={false} />
              </Card>
            </Col>
          </Row>
        )}
      </Card>

      <Modal title="新增员工" open={modalVisible} onCancel={() => setModalVisible(false)} onOk={() => { message.success('创建成功'); setModalVisible(false); }} width={700}>
        <Form layout="vertical">
          <Row gutter={16}>
            <Col span={12}><Form.Item label="姓名" required><Input placeholder="员工姓名" /></Form.Item></Col>
            <Col span={12}><Form.Item label="性别" required><Select placeholder="选择" options={[{ value: 'M', label: '男' }, { value: 'F', label: '女' }]} /></Form.Item></Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}><Form.Item label="部门" required><Select placeholder="选择部门" options={mockDepartments.map(d => ({ value: d.name, label: d.name }))} /></Form.Item></Col>
            <Col span={12}><Form.Item label="职位" required><Input placeholder="职位名称" /></Form.Item></Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}><Form.Item label="联系电话"><Input placeholder="手机号" prefix={<PhoneOutlined />} /></Form.Item></Col>
            <Col span={12}><Form.Item label="邮箱"><Input placeholder="邮箱" prefix={<MailOutlined />} /></Form.Item></Col>
          </Row>
        </Form>
      </Modal>

      <Modal title={`员工详情 - ${selected?.name}`} open={detailVisible} onCancel={() => { setDetailVisible(false); setSelected(null); }} footer={null} width={800}>
        {selected && (
          <Tabs items={[
            { key: 'basic', label: '基本信息', children: <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="工号">{selected.empNo}</Descriptions.Item>
              <Descriptions.Item label="姓名">{selected.name}</Descriptions.Item>
              <Descriptions.Item label="部门">{selected.department}</Descriptions.Item>
              <Descriptions.Item label="职位">{selected.position}</Descriptions.Item>
              <Descriptions.Item label="职级"><Tag color={levelConfig[selected.level]?.color}>{selected.level}</Tag></Descriptions.Item>
              <Descriptions.Item label="状态"><Tag color={statusConfig[selected.status]?.color}>{statusConfig[selected.status]?.text}</Tag></Descriptions.Item>
              <Descriptions.Item label="联系电话">{selected.phone}</Descriptions.Item>
              <Descriptions.Item label="邮箱">{selected.email}</Descriptions.Item>
              <Descriptions.Item label="入职日期">{selected.hireDate}</Descriptions.Item>
              <Descriptions.Item label="工龄">{selected.workYears}年</Descriptions.Item>
            </Descriptions> },
            { key: 'history', label: '变动记录', children: <Timeline items={[
              { color: 'green', children: `入职 - ${selected.hireDate}` },
              { color: 'blue', children: '转正 - 试用期结束' },
            ]} /> },
          ]} />
        )}
      </Modal>
    </div>
  );
}
