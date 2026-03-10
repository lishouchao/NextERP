'use client';

import { Button, Card, Typography } from 'antd';

const { Title, Paragraph } = Typography;

export default function Home() {
  return (
    <main style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: '#f0f2f5'
    }}>
      <Card style={{ width: 400, textAlign: 'center' }}>
        <Title level={2}>NextERP</Title>
        <Paragraph>下一代 ERP 系统</Paragraph>
        <Paragraph type="secondary">
          基于 Spring Modulith + Next.js 构建
        </Paragraph>
        <Button type="primary" href="/dashboard">
          进入系统
        </Button>
      </Card>
    </main>
  );
}
