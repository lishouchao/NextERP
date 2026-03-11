'use client';

import { useState } from 'react';
import {
  Card,
  Table,
  Form,
  Select,
  DatePicker,
  Button,
  Space,
  Row,
  Col,
  Statistic,
  InputNumber,
  Tag,
} from 'antd';
import {
  SearchOutlined,
  ExportOutlined,
  PrinterOutlined,
} from '@ant-design/icons';
import type { FinAccount } from '@/types/finance';
import { ACCOUNT_TYPE_OPTIONS } from '@/types/finance';

const { RangePicker } = DatePicker;

// 模拟总账数据
const mockLedgerData = [
  { id: 1, accountCode: '1001', accountName: '库存现金', accountType: 1, direction: 1, period: '2023-12', openingBalance: 50000, debitAmount: 30000, creditAmount: 20000, endingBalance: 60000 },
  { id: 2, accountCode: '1002', accountName: '银行存款', accountType: 1, direction: 1, period: '2023-12', openingBalance: 500000, debitAmount: 150000, creditAmount: 80000, endingBalance: 570000 },
  { id: 3, accountCode: '1122', accountName: '应收账款', accountType: 1, direction: 1, period: '2023-12', openingBalance: 200000, debitAmount: 100000, creditAmount: 50000, endingBalance: 250000 },
  { id: 4, accountCode: '1405', accountName: '原材料', accountType: 1, direction: 1, period: '2023-12', openingBalance: 150000, debitAmount: 50000, creditAmount: 80000, endingBalance: 120000 },
  { id: 5, accountCode: '1601', accountName: '固定资产', accountType: 1, direction: 1, period: '2023-12', openingBalance: 1000000, debitAmount: 0, creditAmount: 0, endingBalance: 1000000 },
  { id: 6, accountCode: '2202', accountName: '应付账款', accountType: 2, direction: 2, period: '2023-12', openingBalance: 100000, debitAmount: 30000, creditAmount: 50000, endingBalance: 120000 },
  { id: 7, accountCode: '2221', accountName: '应交税费', accountType: 2, direction: 2, period: '2023-12', openingBalance: 15000, debitAmount: 20000, creditAmount: 25000, endingBalance: 20000 },
  { id: 8, accountCode: '4001', accountName: '实收资本', accountType: 3, direction: 2, period: '2023-12', openingBalance: 2000000, debitAmount: 0, creditAmount: 0, endingBalance: 2000000 },
  { id: 9, accountCode: '6001', accountName: '主营业务收入', accountType: 5, direction: 2, period: '2023-12', openingBalance: 0, debitAmount: 0, creditAmount: 500000, endingBalance: 500000 },
  { id: 10, accountCode: '6401', accountName: '主营业务成本', accountType: 5, direction: 1, period: '2023-12', openingBalance: 0, debitAmount: 350000, creditAmount: 0, endingBalance: 350000 },
  { id: 11, accountCode: '6602', accountName: '管理费用', accountType: 5, direction: 1, period: '2023-12', openingBalance: 0, debitAmount: 50000, creditAmount: 0, endingBalance: 50000 },
  { id: 12, accountCode: '6603', accountName: '财务费用', accountType: 5, direction: 1, period: '2023-12', openingBalance: 0, debitAmount: 5000, creditAmount: 0, endingBalance: 5000 },
];

export default function LedgerPage() {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState(mockLedgerData);
  const [form] = Form.useForm();

  const columns = [
    { title: '科目编码', dataIndex: 'accountCode', key: 'accountCode', width: 100, fixed: 'left' as const },
    { title: '科目名称', dataIndex: 'accountName', key: 'accountName', width: 150 },
    { title: '科目类型', dataIndex: 'accountType', key: 'accountType', width: 100,
      render: (type: number) => {
        const option = ACCOUNT_TYPE_OPTIONS.find(o => o.value === type);
        const colors: Record<number, string> = { 1: 'blue', 2: 'red', 3: 'green', 4: 'orange', 5: 'purple' };
        return <Tag color={colors[type]}>{option?.label}</Tag>;
      },
    },
    { title: '方向', dataIndex: 'direction', key: 'direction', width: 60,
      render: (d: number) => <Tag color={d === 1 ? 'blue' : 'red'}>{d === 1 ? '借' : '贷'}</Tag>,
    },
    { title: '期初余额', dataIndex: 'openingBalance', key: 'openingBalance', width: 130, align: 'right' as const,
      render: (v: number) => v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 }),
    },
    { title: '本期借方', dataIndex: 'debitAmount', key: 'debitAmount', width: 130, align: 'right' as const,
      render: (v: number) => v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 }),
    },
    { title: '本期贷方', dataIndex: 'creditAmount', key: 'creditAmount', width: 130, align: 'right' as const,
      render: (v: number) => v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 }),
    },
    { title: '期末余额', dataIndex: 'endingBalance', key: 'endingBalance', width: 130, align: 'right' as const,
      render: (v: number) => <strong style={{ color: '#1890ff' }}>{v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</strong>,
    },
  ];

  // 计算汇总
  const totalStats = {
    assets: data.filter(d => d.accountType === 1).reduce((sum, d) => sum + d.endingBalance, 0),
    liabilities: data.filter(d => d.accountType === 2).reduce((sum, d) => sum + d.endingBalance, 0),
    equity: data.filter(d => d.accountType === 3).reduce((sum, d) => sum + d.endingBalance, 0),
    income: data.filter(d => d.accountType === 5 && d.direction === 2).reduce((sum, d) => sum + d.creditAmount, 0),
    expense: data.filter(d => d.accountType === 5 && d.direction === 1).reduce((sum, d) => sum + d.debitAmount, 0),
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="总账查询 (对标 SAP FBL3N)" style={{ marginBottom: 16 }}
        extra={
          <Space>
            <Button icon={<ExportOutlined />}>导出 Excel</Button>
            <Button icon={<PrinterOutlined />}>打印</Button>
          </Space>
        }
      >
        <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="period" label="会计期间">
            <DatePicker picker="month" placeholder="选择期间" />
          </Form.Item>
          <Form.Item name="accountType" label="科目类型">
            <Select placeholder="全部" allowClear style={{ width: 120 }} options={ACCOUNT_TYPE_OPTIONS} />
          </Form.Item>
          <Form.Item name="accountCode" label="科目编码">
            <InputNumber placeholder="起" style={{ width: 100 }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" icon={<SearchOutlined />}>查询</Button>
              <Button onClick={() => form.resetFields()}>重置</Button>
            </Space>
          </Form.Item>
        </Form>

        {/* 汇总统计 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={4}>
            <Statistic title="资产总额" value={totalStats.assets} precision={2} prefix="¥"
              valueStyle={{ color: '#1890ff', fontSize: 18 }} />
          </Col>
          <Col span={4}>
            <Statistic title="负债总额" value={totalStats.liabilities} precision={2} prefix="¥"
              valueStyle={{ color: '#ff4d4f', fontSize: 18 }} />
          </Col>
          <Col span={4}>
            <Statistic title="所有者权益" value={totalStats.equity} precision={2} prefix="¥"
              valueStyle={{ color: '#52c41a', fontSize: 18 }} />
          </Col>
          <Col span={4}>
            <Statistic title="本期收入" value={totalStats.income} precision={2} prefix="¥"
              valueStyle={{ color: '#722ed1', fontSize: 18 }} />
          </Col>
          <Col span={4}>
            <Statistic title="本期费用" value={totalStats.expense} precision={2} prefix="¥"
              valueStyle={{ color: '#fa8c16', fontSize: 18 }} />
          </Col>
          <Col span={4}>
            <Statistic title="本期利润" value={totalStats.income - totalStats.expense} precision={2} prefix="¥"
              valueStyle={{ color: totalStats.income > totalStats.expense ? '#52c41a' : '#ff4d4f', fontSize: 18 }} />
          </Col>
        </Row>

        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          size="small"
          scroll={{ x: 1100 }}
          pagination={{ defaultPageSize: 20, showSizeChanger: true, showTotal: (total) => `共 ${total} 条` }}
          summary={() => (
            <Table.Summary fixed>
              <Table.Summary.Row>
                <Table.Summary.Cell index={0} colSpan={3}><strong>合计</strong></Table.Summary.Cell>
                <Table.Summary.Cell index={1} />
                <Table.Summary.Cell index={2} align="right">
                  <strong>{data.reduce((s, d) => s + d.openingBalance, 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</strong>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={3} align="right">
                  <strong style={{ color: '#1890ff' }}>{data.reduce((s, d) => s + d.debitAmount, 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</strong>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={4} align="right">
                  <strong style={{ color: '#52c41a' }}>{data.reduce((s, d) => s + d.creditAmount, 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</strong>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={5} align="right">
                  <strong style={{ color: '#1890ff' }}>{data.reduce((s, d) => s + d.endingBalance, 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</strong>
                </Table.Summary.Cell>
              </Table.Summary.Row>
            </Table.Summary>
          )}
        />
      </Card>
    </div>
  );
}
