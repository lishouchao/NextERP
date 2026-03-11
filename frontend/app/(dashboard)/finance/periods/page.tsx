'use client';

import { useState } from 'react';
import {
  Card,
  Table,
  Form,
  DatePicker,
  Button,
  Space,
  Row,
  Col,
  Statistic,
  Tag,
  Steps,
  Modal,
  message,
  Popconfirm,
} from 'antd';
import {
  SearchOutlined,
  CheckCircleOutlined,
  SyncOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';

// 模拟会计期间数据
const mockPeriods = [
  { id: 1, periodCode: '2023-01', periodName: '2023年1月', fiscalYear: 2023, periodNumber: 1, startDate: '2023-01-01', endDate: '2023-01-31', status: 2, closedAt: '2023-02-05', closedBy: '李四' },
  { id: 2, periodCode: '2023-02', periodName: '2023年2月', fiscalYear: 2023, periodNumber: 2, startDate: '2023-02-01', endDate: '2023-02-28', status: 2, closedAt: '2023-03-05', closedBy: '李四' },
  { id: 3, periodCode: '2023-03', periodName: '2023年3月', fiscalYear: 2023, periodNumber: 3, startDate: '2023-03-01', endDate: '2023-03-31', status: 2, closedAt: '2023-04-05', closedBy: '李四' },
  { id: 4, periodCode: '2023-04', periodName: '2023年4月', fiscalYear: 2023, periodNumber: 4, startDate: '2023-04-01', endDate: '2023-04-30', status: 2, closedAt: '2023-05-05', closedBy: '李四' },
  { id: 5, periodCode: '2023-05', periodName: '2023年5月', fiscalYear: 2023, periodNumber: 5, startDate: '2023-05-01', endDate: '2023-05-31', status: 2, closedAt: '2023-06-05', closedBy: '李四' },
  { id: 6, periodCode: '2023-06', periodName: '2023年6月', fiscalYear: 2023, periodNumber: 6, startDate: '2023-06-01', endDate: '2023-06-30', status: 2, closedAt: '2023-07-05', closedBy: '李四' },
  { id: 7, periodCode: '2023-07', periodName: '2023年7月', fiscalYear: 2023, periodNumber: 7, startDate: '2023-07-01', endDate: '2023-07-31', status: 2, closedAt: '2023-08-05', closedBy: '李四' },
  { id: 8, periodCode: '2023-08', periodName: '2023年8月', fiscalYear: 2023, periodNumber: 8, startDate: '2023-08-01', endDate: '2023-08-31', status: 2, closedAt: '2023-09-05', closedBy: '李四' },
  { id: 9, periodCode: '2023-09', periodName: '2023年9月', fiscalYear: 2023, periodNumber: 9, startDate: '2023-09-01', endDate: '2023-09-30', status: 2, closedAt: '2023-10-05', closedBy: '李四' },
  { id: 10, periodCode: '2023-10', periodName: '2023年10月', fiscalYear: 2023, periodNumber: 10, startDate: '2023-10-01', endDate: '2023-10-31', status: 2, closedAt: '2023-11-05', closedBy: '李四' },
  { id: 11, periodCode: '2023-11', periodName: '2023年11月', fiscalYear: 2023, periodNumber: 11, startDate: '2023-11-01', endDate: '2023-11-30', status: 2, closedAt: '2023-12-05', closedBy: '李四' },
  { id: 12, periodCode: '2023-12', periodName: '2023年12月', fiscalYear: 2023, periodNumber: 12, startDate: '2023-12-01', endDate: '2023-12-31', status: 1, closedAt: null, closedBy: null },
];

export default function PeriodsPage() {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState(mockPeriods);
  const [form] = Form.useForm();
  const [closeModalVisible, setCloseModalVisible] = useState(false);
  const [selectedPeriod, setSelectedPeriod] = useState<any>(null);

  const handleClosePeriod = (period: any) => {
    setSelectedPeriod(period);
    setCloseModalVisible(true);
  };

  const confirmClosePeriod = () => {
    setLoading(true);
    setTimeout(() => {
      setData(prev => prev.map(p =>
        p.id === selectedPeriod.id
          ? { ...p, status: 2, closedAt: dayjs().format('YYYY-MM-DD'), closedBy: '张三' }
          : p
      ));
      message.success(`会计期间 ${selectedPeriod.periodName} 结账成功`);
      setCloseModalVisible(false);
      setLoading(false);
    }, 500);
  };

  const handleReopenPeriod = (period: any) => {
    Modal.confirm({
      title: '反结账确认',
      content: `确定要反结账 ${period.periodName} 吗？这将会重新开启该期间进行修改。`,
      onOk: () => {
        setLoading(true);
        setTimeout(() => {
          setData(prev => prev.map(p =>
            p.id === period.id
              ? { ...p, status: 1, closedAt: null, closedBy: null }
              : p
          ));
          message.success(`会计期间 ${period.periodName} 已反结账`);
          setLoading(false);
        }, 300);
      },
    });
  };

  const columns = [
    { title: '期间编码', dataIndex: 'periodCode', key: 'periodCode', width: 120 },
    { title: '期间名称', dataIndex: 'periodName', key: 'periodName', width: 120 },
    { title: '会计年度', dataIndex: 'fiscalYear', key: 'fiscalYear', width: 100, align: 'center' as const },
    { title: '期间号', dataIndex: 'periodNumber', key: 'periodNumber', width: 80, align: 'center' as const },
    { title: '开始日期', dataIndex: 'startDate', key: 'startDate', width: 110 },
    { title: '结束日期', dataIndex: 'endDate', key: 'endDate', width: 110 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 100,
      render: (status: number) => {
        const map: Record<number, { color: string; text: string }> = {
          1: { color: 'processing', text: '已开启' },
          2: { color: 'success', text: '已结账' },
        };
        const s = map[status] || { color: 'default', text: '未知' };
        return <Tag color={s.color} icon={status === 1 ? <SyncOutlined spin /> : <CheckCircleOutlined />}>{s.text}</Tag>;
      },
    },
    { title: '结账时间', dataIndex: 'closedAt', key: 'closedAt', width: 120,
      render: (v: string) => v || '-',
    },
    { title: '结账人', dataIndex: 'closedBy', key: 'closedBy', width: 100,
      render: (v: string) => v || '-',
    },
    { title: '操作', key: 'action', width: 180, fixed: 'right' as const,
      render: (_: unknown, record: any) => (
        <Space>
          {record.status === 1 && (
            <Button type="primary" size="small" onClick={() => handleClosePeriod(record)}>
              期末结账
            </Button>
          )}
          {record.status === 2 && (
            <Button size="small" onClick={() => handleReopenPeriod(record)}>
              反结账
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const currentPeriod = data.find(p => p.status === 1);
  const closedCount = data.filter(p => p.status === 2).length;

  return (
    <div style={{ padding: 24 }}>
      <Card title="期末结账 (对标 SAP OB52)"
        extra={
          <Space>
            <Button>年终结转</Button>
            <Button type="primary">自动结账</Button>
          </Space>
        }
      >
        {/* 当前状态 */}
        <Card size="small" style={{ marginBottom: 16, background: '#f5f5f5' }}>
          <Row gutter={24} align="middle">
            <Col span={6}>
              <Statistic title="当前期间" value={currentPeriod?.periodName || '-'} valueStyle={{ fontSize: 18, color: '#1890ff' }} />
            </Col>
            <Col span={6}>
              <Statistic title="已结账期间" value={closedCount} suffix={`/ ${data.length}`} valueStyle={{ fontSize: 18, color: '#52c41a' }} />
            </Col>
            <Col span={12}>
              <div style={{ marginBottom: 8 }}>结账进度</div>
              <Steps
                size="small"
                current={closedCount}
                items={[
                  { title: '1-3月', status: closedCount >= 3 ? 'finish' : 'process' },
                  { title: '4-6月', status: closedCount >= 6 ? 'finish' : 'process' },
                  { title: '7-9月', status: closedCount >= 9 ? 'finish' : 'process' },
                  { title: '10-12月', status: closedCount >= 12 ? 'finish' : 'wait' },
                ]}
              />
            </Col>
          </Row>
        </Card>

        <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="fiscalYear" label="会计年度">
            <DatePicker picker="year" placeholder="选择年度" style={{ width: 120 }} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SearchOutlined />}>查询</Button>
          </Form.Item>
        </Form>

        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          size="small"
          pagination={false}
        />
      </Card>

      {/* 结账确认弹窗 */}
      <Modal
        title="期末结账确认"
        open={closeModalVisible}
        onCancel={() => setCloseModalVisible(false)}
        onOk={confirmClosePeriod}
        confirmLoading={loading}
      >
        <p>确定要对 <strong>{selectedPeriod?.periodName}</strong> 进行期末结账吗？</p>
        <p style={{ color: '#ff4d4f' }}>结账后将无法修改该期间的凭证。</p>
        <Card size="small" title="结账前检查项" style={{ marginTop: 16 }}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Tag color="green">✓ 所有凭证已审核</Tag>
            <Tag color="green">✓ 所有凭证已记账</Tag>
            <Tag color="green">✓ 借贷平衡检查通过</Tag>
            <Tag color="green">✓ 试算平衡检查通过</Tag>
          </Space>
        </Card>
      </Modal>
    </div>
  );
}
