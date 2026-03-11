'use client';

import { useState } from 'react';
import {
  Card, Table, Button, Space, Modal, Form, Input, Select, DatePicker, TimePicker,
  Tag, message, Row, Col, Statistic, Tabs, Calendar, Badge, Descriptions,
  Timeline, List, Avatar, Progress, Tooltip, Popconfirm,
} from 'antd';
import {
  ClockCircleOutlined, CheckCircleOutlined, CloseCircleOutlined,
  ExclamationCircleOutlined, CalendarOutlined, TeamOutlined,
  ExportOutlined, ImportOutlined, FilterOutlined,
} from '@ant-design/icons';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';

// 模拟考勤记录
const mockAttendance = [
  { id: '1', pernr: 'EMP001', empName: '张伟', date: '2024-03-20', shiftName: '标准班', clockIn: '08:55', clockOut: '18:05', workHours: 8.5, overtime: 0.5, status: 'NORMAL', remark: '' },
  { id: '2', pernr: 'EMP003', empName: '王磊', date: '2024-03-20', shiftName: '标准班', clockIn: '09:15', clockOut: '18:30', workHours: 8.0, overtime: 0.0, status: 'LATE', remark: '迟到15分钟' },
  { id: '3', pernr: 'EMP004', empName: '赵敏', date: '2024-03-20', shiftName: '标准班', clockIn: '-', clockOut: '-', workHours: 0, overtime: 0, status: 'LEAVE', remark: '年假' },
  { id: '4', pernr: 'EMP006', empName: '陈芳', date: '2024-03-20', shiftName: '标准班', clockIn: '08:50', clockOut: '17:30', workHours: 7.5, overtime: 0, status: 'EARLY', remark: '早退30分钟' },
  { id: '5', pernr: 'EMP007', empName: '周杰', date: '2024-03-20', shiftName: '标准班', clockIn: '08:58', clockOut: '20:00', workHours: 10.0, overtime: 2.0, status: 'NORMAL', remark: '加班' },
  { id: '6', pernr: 'EMP008', empName: '吴静', date: '2024-03-20', shiftName: '标准班', clockIn: '09:30', clockOut: '18:00', workHours: 7.5, overtime: 0, status: 'LATE', remark: '迟到30分钟' },
  { id: '7', pernr: 'EMP011', empName: '李明', date: '2024-03-20', shiftName: '标准班', clockIn: '08:45', clockOut: '18:10', workHours: 8.5, overtime: 0.5, status: 'NORMAL', remark: '' },
  { id: '8', pernr: 'EMP002', empName: '李娜', date: '2024-03-20', shiftName: '标准班', clockIn: '-', clockOut: '-', workHours: 0, overtime: 0, status: 'ABSENT', remark: '未打卡无请假' },
];

const statusConfig: Record<string, { color: string; text: string; icon: any }> = {
  NORMAL: { color: 'green', text: '正常', icon: <CheckCircleOutlined /> },
  LATE: { color: 'orange', text: '迟到', icon: <ExclamationCircleOutlined /> },
  EARLY: { color: 'gold', text: '早退', icon: <ExclamationCircleOutlined /> },
  LEAVE: { color: 'blue', text: '请假', icon: <CalendarOutlined /> },
  ABSENT: { color: 'red', text: '缺勤', icon: <CloseCircleOutlined /> },
  HOLIDAY: { color: 'purple', text: '节假日', icon: <CalendarOutlined /> },
};

export default function AttendancePage() {
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('daily');
  const [selectedDate, setSelectedDate] = useState<Dayjs>(dayjs());
  const [modalVisible, setModalVisible] = useState(false);
  const [adjustModalVisible, setAdjustModalVisible] = useState(false);
  const [form] = Form.useForm();

  // 统计
  const stats = {
    total: mockAttendance.length,
    normal: mockAttendance.filter(a => a.status === 'NORMAL').length,
    late: mockAttendance.filter(a => a.status === 'LATE').length,
    early: mockAttendance.filter(a => a.status === 'EARLY').length,
    leave: mockAttendance.filter(a => a.status === 'LEAVE').length,
    absent: mockAttendance.filter(a => a.status === 'ABSENT').length,
    attendanceRate: Math.round((mockAttendance.filter(a => a.status === 'NORMAL').length / mockAttendance.length) * 100),
  };

  // 考勤列表列
  const columns = [
    { title: '员工号', dataIndex: 'pernr', width: 100 },
    { title: '姓名', dataIndex: 'empName', width: 100, render: (v: string) => <Space><Avatar size="small">{v[0]}</Avatar>{v}</Space> },
    { title: '日期', dataIndex: 'date', width: 100 },
    { title: '班次', dataIndex: 'shiftName', width: 80 },
    { title: '上班打卡', dataIndex: 'clockIn', width: 90, render: (v: string) => v === '-' ? <Tag color="default">未打卡</Tag> : v },
    { title: '下班打卡', dataIndex: 'clockOut', width: 90, render: (v: string) => v === '-' ? <Tag color="default">未打卡</Tag> : v },
    { title: '工时', dataIndex: 'workHours', width: 70, render: (v: number) => `${v}h` },
    { title: '加班', dataIndex: 'overtime', width: 70, render: (v: number) => v > 0 ? <Tag color="blue">{v}h</Tag> : '-' },
    { title: '状态', dataIndex: 'status', width: 80, render: (s: string) => {
      const config = statusConfig[s];
      return <Tag color={config?.color} icon={config?.icon}>{config?.text}</Tag>;
    }},
    { title: '备注', dataIndex: 'remark', ellipsis: true },
    { title: '操作', width: 100, render: () => (
      <Space>
        <Button type="link" size="small" onClick={() => setAdjustModalVisible(true)}>调整</Button>
      </Space>
    )},
  ];

  // 日历单元格渲染
  const dateCellRender = (date: Dayjs) => {
    const dateStr = date.format('YYYY-MM-DD');
    const records = mockAttendance.filter(a => a.date === dateStr);
    if (records.length === 0) return null;
    return (
      <ul style={{ padding: 0, margin: 0, listStyle: 'none' }}>
        {records.slice(0, 2).map(r => (
          <li key={r.id} style={{ marginBottom: 2 }}>
            <Badge status={r.status === 'NORMAL' ? 'success' : r.status === 'LATE' ? 'warning' : 'error'} text={r.empName} />
          </li>
        ))}
        {records.length > 2 && <li style={{ color: '#8c8c8c', fontSize: 12 }}>+{records.length - 2} 更多</li>}
      </ul>
    );
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="考勤管理 (对标 SAP PT60/CAT2)"
        extra={<Space>
          <Button icon={<ImportOutlined />}>导入打卡</Button>
          <Button icon={<ExportOutlined />}>导出报表</Button>
          <Button type="primary" icon={<CalendarOutlined />}>排班管理</Button>
        </Space>}
      >
        <Tabs activeKey={activeTab} onChange={setActiveTab}
          items={[
            { key: 'daily', label: <><ClockCircleOutlined /> 日常考勤</> },
            { key: 'calendar', label: <><CalendarOutlined /> 日历视图</> },
            { key: 'exception', label: <><ExclamationCircleOutlined /> 异常处理</> },
            { key: 'statistics', label: <><TeamOutlined /> 考勤统计</> },
          ]} />

        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={3}><Card size="small"><Statistic title="今日应出勤" value={stats.total} suffix="人" valueStyle={{ fontSize: 16 }} /></Card></Col>
          <Col span={3}><Card size="small"><Statistic title="正常出勤" value={stats.normal} suffix="人" valueStyle={{ fontSize: 16, color: '#52c41a' }} /></Card></Col>
          <Col span={3}><Card size="small"><Statistic title="迟到" value={stats.late} suffix="人" valueStyle={{ fontSize: 16, color: '#faad14' }} /></Card></Col>
          <Col span={3}><Card size="small"><Statistic title="早退" value={stats.early} suffix="人" valueStyle={{ fontSize: 16, color: '#fa8c16' }} /></Card></Col>
          <Col span={3}><Card size="small"><Statistic title="请假" value={stats.leave} suffix="人" valueStyle={{ fontSize: 16, color: '#1890ff' }} /></Card></Col>
          <Col span={3}><Card size="small"><Statistic title="缺勤" value={stats.absent} suffix="人" valueStyle={{ fontSize: 16, color: '#ff4d4f' }} /></Card></Col>
          <Col span={3}>
            <Card size="small">
              <Progress type="circle" percent={stats.attendanceRate} size={50} format={p => `${p}%`} />
              <div style={{ textAlign: 'center', fontSize: 12, color: '#8c8c8c', marginTop: 4 }}>出勤率</div>
            </Card>
          </Col>
          <Col span={3}>
            <Card size="small">
              <DatePicker value={selectedDate} onChange={(d) => d && setSelectedDate(d)} style={{ width: '100%' }} />
            </Card>
          </Col>
        </Row>

        {/* 日常考勤 */}
        {activeTab === 'daily' && (
          <>
            <Form layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item><Input placeholder="员工号/姓名" style={{ width: 120 }} /></Form.Item>
              <Form.Item><Select placeholder="状态" allowClear style={{ width: 100 }} options={Object.entries(statusConfig).map(([k, v]) => ({ value: k, label: v.text }))} /></Form.Item>
              <Form.Item><Button type="primary" icon={<FilterOutlined />}>筛选</Button></Form.Item>
            </Form>
            <Table columns={columns} dataSource={mockAttendance} rowKey="id" size="small" loading={loading} pagination={{ defaultPageSize: 20 }} scroll={{ x: 1200 }} />
          </>
        )}

        {/* 日历视图 */}
        {activeTab === 'calendar' && (
          <Card>
            <Calendar dateCellRender={dateCellRender} />
          </Card>
        )}

        {/* 异常处理 */}
        {activeTab === 'exception' && (
          <Card title="待处理异常">
            <List
              itemLayout="horizontal"
              dataSource={mockAttendance.filter(a => a.status !== 'NORMAL' && a.status !== 'LEAVE')}
              renderItem={item => (
                <List.Item actions={[
                  <Button type="link" key="approve">确认</Button>,
                  <Button type="link" key="adjust">调整</Button>,
                  <Button type="link" key="leave">转请假</Button>,
                ]}>
                  <List.Item.Meta
                    avatar={<Avatar>{item.empName[0]}</Avatar>}
                    title={`${item.empName} - ${item.date}`}
                    description={<Space>
                      <Tag color={statusConfig[item.status]?.color}>{statusConfig[item.status]?.text}</Tag>
                      <span>{item.remark}</span>
                    </Space>}
                  />
                </List.Item>
              )}
            />
          </Card>
        )}

        {/* 考勤统计 */}
        {activeTab === 'statistics' && (
          <Row gutter={16}>
            <Col span={12}>
              <Card title="本月出勤统计" size="small">
                <Descriptions column={2} bordered size="small">
                  <Descriptions.Item label="应出勤天数">22天</Descriptions.Item>
                  <Descriptions.Item label="实际出勤">20天</Descriptions.Item>
                  <Descriptions.Item label="迟到次数">2次</Descriptions.Item>
                  <Descriptions.Item label="早退次数">0次</Descriptions.Item>
                  <Descriptions.Item label="请假天数">2天</Descriptions.Item>
                  <Descriptions.Item label="加班时长">8小时</Descriptions.Item>
                </Descriptions>
              </Card>
            </Col>
            <Col span={12}>
              <Card title="异常趋势" size="small">
                <Timeline items={[
                  { color: 'green', children: '03月20日: 出勤率 87.5%' },
                  { color: 'orange', children: '03月19日: 出勤率 82.3% (有3人迟到)' },
                  { color: 'green', children: '03月18日: 出勤率 95.0%' },
                  { color: 'blue', children: '03月15日: 出勤率 90.0%' },
                ]} />
              </Card>
            </Col>
          </Row>
        )}
      </Card>

      {/* 调整弹窗 */}
      <Modal title="考勤调整" open={adjustModalVisible} onCancel={() => setAdjustModalVisible(false)} onOk={() => { message.success('调整成功'); setAdjustModalVisible(false); }}>
        <Form form={form} layout="vertical">
          <Row gutter={16}>
            <Col span={12}><Form.Item label="上班时间"><TimePicker style={{ width: '100%' }} format="HH:mm" /></Form.Item></Col>
            <Col span={12}><Form.Item label="下班时间"><TimePicker style={{ width: '100%' }} format="HH:mm" /></Form.Item></Col>
          </Row>
          <Form.Item label="调整原因"><Input.TextArea rows={2} placeholder="请输入调整原因" /></Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
