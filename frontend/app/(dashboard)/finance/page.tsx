'use client';

import { Card, Row, Col, Statistic, List, Typography, Avatar, Tag, Space } from 'antd';
import {
  DollarOutlined,
  BankOutlined,
  FileTextOutlined,
  ReconciliationOutlined,
  CalculatorOutlined,
  BarChartOutlined,
  ClockCircleOutlined,
  WarningOutlined,
} from '@ant-design/icons';
import Link from 'next/link';
import { TCODES } from '@/config/tcodes';

const { Title, Text } = Typography;

// 财务模块快捷入口
const financeModules = [
  { title: '会计科目', icon: <BankOutlined />, path: '/finance/accounts', tcode: 'FS00', color: '#1890ff', desc: '科目主数据管理' },
  { title: '凭证管理', icon: <FileTextOutlined />, path: '/finance/vouchers', tcode: 'FB50', color: '#52c41a', desc: '凭证录入与审核' },
  { title: '总账查询', icon: <ReconciliationOutlined />, path: '/finance/ledger', tcode: 'FBL3N', color: '#722ed1', desc: '总账余额与明细' },
  { title: '应收账款', icon: <DollarOutlined />, path: '/finance/receivables', tcode: 'FBL5N', color: '#fa8c16', desc: '客户应收管理' },
  { title: '应付账款', icon: <DollarOutlined />, path: '/finance/payables', tcode: 'FBL1N', color: '#eb2f96', desc: '供应商应付管理' },
  { title: '固定资产', icon: <CalculatorOutlined />, path: '/finance/assets', tcode: 'AS01', color: '#13c2c2', desc: '资产与折旧管理' },
  { title: '期末结账', icon: <ClockCircleOutlined />, path: '/finance/periods', tcode: 'OB52', color: '#2f54eb', desc: '会计期间管理' },
  { title: '财务报表', icon: <BarChartOutlined />, path: '/finance/reports', tcode: 'F.01', color: '#f5222d', desc: '资产负债表/利润表' },
];

// 财务相关事务码
const financeTCodes = TCODES.filter(t => t.module === 'finance').slice(0, 12);

export default function FinanceHomePage() {
  return (
    <div style={{ padding: 24 }}>
      <Title level={4} style={{ marginBottom: 24 }}>财务管理中心</Title>

      {/* 统计概览 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="本月凭证"
              value={128}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="待审核"
              value={12}
              prefix={<WarningOutlined />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="应收余额"
              value={365000}
              precision={2}
              prefix="¥"
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="应付余额"
              value={198000}
              precision={2}
              prefix="¥"
              valueStyle={{ color: '#eb2f96' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={24}>
        {/* 功能模块入口 */}
        <Col span={16}>
          <Card title="功能模块" bordered={false}>
            <Row gutter={[16, 16]}>
              {financeModules.map((item) => (
                <Col span={6} key={item.path}>
                  <Link href={item.path} style={{ textDecoration: 'none' }}>
                    <Card
                      hoverable
                      style={{ textAlign: 'center', height: '100%' }}
                      bodyStyle={{ padding: 20 }}
                    >
                      <Avatar
                        size={48}
                        style={{ backgroundColor: item.color, marginBottom: 12 }}
                        icon={item.icon}
                      />
                      <div style={{ fontWeight: 500, marginBottom: 4 }}>{item.title}</div>
                      <Text type="secondary" style={{ fontSize: 12 }}>{item.desc}</Text>
                      <Tag style={{ marginTop: 8, fontSize: 11 }}>{item.tcode}</Tag>
                    </Card>
                  </Link>
                </Col>
              ))}
            </Row>
          </Card>
        </Col>

        {/* 事务码快捷入口 */}
        <Col span={8}>
          <Card
            title="事务码快捷入口"
            bordered={false}
            extra={<Text type="secondary" style={{ fontSize: 12 }}>Ctrl+T 打开</Text>}
          >
            <List
              size="small"
              dataSource={financeTCodes}
              renderItem={(item) => (
                <List.Item style={{ padding: '8px 0' }}>
                  <Link href={item.path} style={{ width: '100%', textDecoration: 'none' }}>
                    <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                      <Space>
                        <Tag color="blue">{item.code}</Tag>
                        <Text>{item.name}</Text>
                      </Space>
                    </Space>
                  </Link>
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>
    </div>
  );
}
