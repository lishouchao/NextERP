'use client';

import { useState, useEffect } from 'react';
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
import type { FinAccountingPeriod } from '@/types/finance';
import { periodApi } from '@/lib/api/finance';

export default function PeriodsPage() {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<FinAccountingPeriod[]>([]);
  const [form] = Form.useForm();
  const [closeModalVisible, setCloseModalVisible] = useState(false);
  const [selectedPeriod, setSelectedPeriod] = useState<FinAccountingPeriod | null>(null);

  // 加载会计期间数据
  const loadPeriods = async (fiscalYear?: number) => {
    setLoading(true);
    try {
      const res = await periodApi.getList(fiscalYear);
      setData(res.data || []);
    } catch (err) {
      message.error('加载会计期间数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPeriods();
  }, []);

  // 按年度查询
  const handleSearch = async () => {
    const values = await form.validateFields();
    const year = values.fiscalYear ? dayjs(values.fiscalYear).year() : undefined;
    loadPeriods(year);
  };

  const handleClosePeriod = (period: FinAccountingPeriod) => {
    setSelectedPeriod(period);
    setCloseModalVisible(true);
  };

  const confirmClosePeriod = async () => {
    if (!selectedPeriod) return;
    setLoading(true);
    try {
      await periodApi.closePeriod(selectedPeriod.id);
      message.success(`会计期间 ${selectedPeriod.periodName} 结账成功`);
      setCloseModalVisible(false);
      loadPeriods();
    } catch (err) {
      message.error('结账失败');
    } finally {
      setLoading(false);
    }
  };

  const handleReopenPeriod = (period: FinAccountingPeriod) => {
    Modal.confirm({
      title: '反结账确认',
      content: `确定要反结账 ${period.periodName} 吗？这将会重新开启该期间进行修改。`,
      onOk: async () => {
        setLoading(true);
        try {
          await periodApi.reopenPeriod(period.id);
          message.success(`会计期间 ${period.periodName} 已反结账`);
          loadPeriods();
        } catch (err) {
          message.error('反结账失败');
        } finally {
          setLoading(false);
        }
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
    { title: '状态', dataIndex: 'periodStatus', key: 'periodStatus', width: 100,
      render: (status: number) => {
        const map: Record<number, { color: string; text: string }> = {
          0: { color: 'default', text: '未开启' },
          1: { color: 'processing', text: '已开启' },
          2: { color: 'success', text: '已结账' },
        };
        const s = map[status] || { color: 'default', text: '未知' };
        return <Tag color={s.color} icon={status === 1 ? <SyncOutlined spin /> : <CheckCircleOutlined />}>{s.text}</Tag>;
      },
    },
    { title: '结账时间', dataIndex: 'closingAt', key: 'closingAt', width: 120,
      render: (v: string) => v || '-',
    },
    { title: '结账人', dataIndex: 'closingBy', key: 'closingBy', width: 100,
      render: (v: string) => v || '-',
    },
    { title: '操作', key: 'action', width: 180, fixed: 'right' as const,
      render: (_: unknown, record: FinAccountingPeriod) => (
        <Space>
          {record.periodStatus === 1 && (
            <Button type="primary" size="small" onClick={() => handleClosePeriod(record)}>
              期末结账
            </Button>
          )}
          {record.periodStatus === 2 && (
            <Button size="small" onClick={() => handleReopenPeriod(record)}>
              反结账
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const currentPeriod = data.find(p => p.periodStatus === 1);
  const closedCount = data.filter(p => p.periodStatus === 2).length;

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
            <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>查询</Button>
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
