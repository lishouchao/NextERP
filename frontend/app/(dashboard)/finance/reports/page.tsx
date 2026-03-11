'use client';

import { useState } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Select,
  DatePicker,
  Button,
  Space,
  Divider,
} from 'antd';
import {
  DownloadOutlined,
  PrinterOutlined,
} from '@ant-design/icons';

const { RangePicker } = DatePicker;

// 模拟报表数据
const balanceSheetData = [
  { item: '流动资产', amount: null, isHeader: true },
  { item: '  货币资金', amount: 620000 },
  { item: '  应收账款', amount: 250000 },
  { item: '  存货', amount: 120000 },
  { item: '流动资产合计', amount: 990000, isTotal: true },
  { item: '非流动资产', amount: null, isHeader: true },
  { item: '  固定资产', amount: 1100000 },
  { item: '非流动资产合计', amount: 1100000, isTotal: true },
  { item: '资产总计', amount: 2090000, isGrandTotal: true },
  { item: '', amount: null, divider: true },
  { item: '流动负债', amount: null, isHeader: true },
  { item: '  应付账款', amount: 120000 },
  { item: '  应付职工薪酬', amount: 50000 },
  { item: '流动负债合计', amount: 170000, isTotal: true },
  { item: '非流动负债', amount: null, isHeader: true },
  { item: '非流动负债合计', amount: 0, isTotal: true },
  { item: '负债合计', amount: 170000, isGrandTotal: true },
  { item: '', amount: null, divider: true },
  { item: '所有者权益', amount: null, isHeader: true },
  { item: '  实收资本', amount: 2000000 },
  { item: '  未分配利润', amount: -80000 },
  { item: '所有者权益合计', amount: 1920000, isTotal: true },
  { item: '负债和所有者权益总计', amount: 2090000, isGrandTotal: true },
];

const incomeStatementData = [
  { item: '一、营业收入', amount: 500000, isHeader: true },
  { item: '  主营业务收入', amount: 500000 },
  { item: '二、营业成本', amount: null, isHeader: true },
  { item: '  主营业务成本', amount: 350000 },
  { item: '三、营业利润', amount: 150000, isTotal: true },
  { item: '四、期间费用', amount: null, isHeader: true },
  { item: '  销售费用', amount: 30000 },
  { item: '  管理费用', amount: 50000 },
  { item: '  财务费用', amount: 5000 },
  { item: '五、利润总额', amount: 65000, isTotal: true },
  { item: '六、所得税费用', amount: 16250 },
  { item: '七、净利润', amount: 48750, isGrandTotal: true },
];

export default function ReportsPage() {
  const [activeReport, setActiveReport] = useState('balance');
  const [period, setPeriod] = useState('2023-12');

  const balanceColumns = [
    { title: '资产', dataIndex: 'item', key: 'item',
      render: (text: string, record: any) => {
        if (record.divider) return <Divider style={{ margin: '8px 0' }} />;
        if (record.isGrandTotal) return <strong>{text}</strong>;
        if (record.isTotal) return <strong style={{ color: '#1890ff' }}>{text}</strong>;
        if (record.isHeader) return <strong style={{ color: '#262626' }}>{text}</strong>;
        return text;
      },
    },
    { title: '金额', dataIndex: 'amount', key: 'amount', width: 150, align: 'right' as const,
      render: (v: number, record: any) => {
        if (record.divider || record.isHeader) return null;
        if (record.isGrandTotal) return <strong style={{ color: '#1890ff' }}>{v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</strong>;
        return v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 }) || '-';
      },
    },
    { title: '负债和所有者权益', dataIndex: 'item2', key: 'item2', render: () => '' },
    { title: '金额', dataIndex: 'amount2', key: 'amount2', width: 150, align: 'right' as const, render: () => '' },
  ];

  const incomeColumns = [
    { title: '项目', dataIndex: 'item', key: 'item',
      render: (text: string, record: any) => {
        if (record.isGrandTotal) return <strong>{text}</strong>;
        if (record.isTotal) return <strong style={{ color: '#52c41a' }}>{text}</strong>;
        if (record.isHeader) return <strong style={{ color: '#262626' }}>{text}</strong>;
        return text;
      },
    },
    { title: '本期金额', dataIndex: 'amount', key: 'amount', width: 200, align: 'right' as const,
      render: (v: number, record: any) => {
        if (record.isHeader) return null;
        if (record.isGrandTotal) return <strong style={{ color: '#52c41a' }}>{v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</strong>;
        return v?.toLocaleString('zh-CN', { minimumFractionDigits: 2 });
      },
    },
    { title: '上期金额', dataIndex: 'lastAmount', key: 'lastAmount', width: 200, align: 'right' as const,
      render: () => '-',
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card title="财务报表 (对标 SAP S_PL0_86000028)"
        extra={
          <Space>
            <Select value={activeReport} onChange={setActiveReport} style={{ width: 200 }}
              options={[
                { value: 'balance', label: '资产负债表 (F.01)' },
                { value: 'income', label: '利润表 (F.02)' },
                { value: 'cashflow', label: '现金流量表' },
                { value: 'trial', label: '试算平衡表' },
              ]} />
            <DatePicker picker="month" value={null} placeholder="选择期间" style={{ width: 120 }} />
            <Button icon={<DownloadOutlined />}>导出</Button>
            <Button icon={<PrinterOutlined />}>打印</Button>
          </Space>
        }
      >
        {/* 报表摘要 */}
        <Row gutter={16} style={{ marginBottom: 24 }}>
          <Col span={8}>
            <Card size="small">
              <Statistic title="报表期间" value={period} valueStyle={{ fontSize: 16 }} />
            </Card>
          </Col>
          <Col span={8}>
            <Card size="small">
              <Statistic title="报表类型"
                value={activeReport === 'balance' ? '资产负债表' : activeReport === 'income' ? '利润表' : '其他报表'}
                valueStyle={{ fontSize: 16 }} />
            </Card>
          </Col>
          <Col span={8}>
            <Card size="small">
              <Statistic title="编制日期" value={new Date().toLocaleDateString('zh-CN')} valueStyle={{ fontSize: 16 }} />
            </Card>
          </Col>
        </Row>

        {/* 报表内容 */}
        {activeReport === 'balance' && (
          <>
            <h2 style={{ textAlign: 'center', marginBottom: 24 }}>资产负债表</h2>
            <Table
              columns={balanceColumns}
              dataSource={balanceSheetData}
              pagination={false}
              size="middle"
              showHeader={true}
            />
          </>
        )}

        {activeReport === 'income' && (
          <>
            <h2 style={{ textAlign: 'center', marginBottom: 24 }}>利润表</h2>
            <Table
              columns={incomeColumns}
              dataSource={incomeStatementData}
              pagination={false}
              size="middle"
            />
          </>
        )}

        {activeReport === 'cashflow' && (
          <div style={{ textAlign: 'center', padding: 50, color: '#999' }}>
            现金流量表 - 待开发
          </div>
        )}

        {activeReport === 'trial' && (
          <div style={{ textAlign: 'center', padding: 50, color: '#999' }}>
            试算平衡表 - 待开发
          </div>
        )}
      </Card>
    </div>
  );
}
