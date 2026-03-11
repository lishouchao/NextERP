'use client';

import { Card, Row, Col, Typography, Tag, Input, Space, Empty } from 'antd';
import { SearchOutlined, AppstoreOutlined } from '@ant-design/icons';
import Link from 'next/link';
import { useState, useMemo } from 'react';
import { TCODES, type TCode } from '@/config/tcodes';

const { Title, Text, Paragraph } = Typography;

// 模块配置
const moduleConfig: Record<string, { name: string; color: string; icon: string }> = {
  finance: { name: '财务会计', color: '#1890ff', icon: '💰' },
  hrm: { name: '人力资源', color: '#52c41a', icon: '👥' },
  supply: { name: '供应链', color: '#fa8c16', icon: '📦' },
  sales: { name: '销售管理', color: '#722ed1', icon: '🛒' },
  production: { name: '生产管理', color: '#13c2c2', icon: '🏭' },
  settings: { name: '系统管理', color: '#eb2f96', icon: '⚙️' },
  common: { name: '通用功能', color: '#8c8c8c', icon: '🏠' },
};

// 按模块分组事务码
function groupByModule(tcodes: TCode[]): Record<string, TCode[]> {
  const groups: Record<string, TCode[]> = {};
  tcodes.forEach((t) => {
    if (!groups[t.module]) {
      groups[t.module] = [];
    }
    groups[t.module].push(t);
  });
  return groups;
}

// 按功能细分分组
function groupByCategory(tcodes: TCode[], module: string): Record<string, TCode[]> {
  const groups: Record<string, TCode[]> = {};

  // 定义各模块的细分分组规则
  const categoryRules: Record<string, Record<string, string[]>> = {
    finance: {
      '科目管理': ['FS00', 'FSP0', 'FSS0'],
      '凭证处理': ['FB50', 'FB01', 'FB60', 'FB65', 'FB70', 'FB75'],
      '总账查询': ['FBL3N', 'FS10N', 'FAGLL03'],
      '应收账款': ['FBL5N', 'FD10N', 'FD03'],
      '应付账款': ['FBL1N', 'FK10N', 'FK03'],
      '固定资产': ['AS01', 'AS02', 'AS03', 'AW01N', 'AFAB'],
      '期间管理': ['OB52', 'S_ALR_87003642'],
      '财务报表': ['F.01', 'F.02', 'S_PL0_86000028', 'F.08', 'FAGLB03'],
    },
    hrm: {
      '员工管理': ['PA20', 'PA30', 'PA40', 'PP01', 'PP02'],
      '组织管理': ['PPOME', 'PPOCE', 'PPOSE'],
      '考勤管理': ['PT60', 'PT50'],
      '薪资管理': ['PC00_M00_CALC', 'PC_PAYRESULT'],
    },
    supply: {
      '物料管理': ['MM01', 'MM02', 'MM03'],
      '采购管理': ['ME21N', 'ME22N', 'ME23N', 'ME51N', 'ME2N'],
      '库存管理': ['MMBE', 'MB52', 'MB51', 'MIGO', 'MI01'],
      '供应商管理': ['XK01', 'XK02', 'XK03', 'MK01'],
    },
    sales: {
      '销售订单': ['VA01', 'VA02', 'VA03', 'VA05'],
      '客户管理': ['VD01', 'VD02', 'VD03', 'XD01', 'XD02', 'XD03'],
      '交货管理': ['VL01N', 'VL02N', 'VL03N', 'VL10B'],
      '开票管理': ['VF01', 'VF02', 'VF03', 'VF05N'],
    },
    production: {
      '物料清单': ['CS01', 'CS02', 'CS03'],
      '工艺路线': ['CA01', 'CA02', 'CA03'],
      '生产订单': ['CO01', 'CO02', 'CO03', 'CO11N'],
      '计划管理': ['MD01', 'MD04'],
    },
    settings: {
      '用户管理': ['SU01', 'SU01D', 'SUIM'],
      '权限管理': ['PFCG', 'SM01'],
    },
    common: {
      '导航': ['HOME', 'DASHBOARD'],
    },
  };

  const rules = categoryRules[module] || {};
  const categorized: Record<string, TCode[]> = {};
  const usedCodes = new Set<string>();

  // 按规则分组
  Object.entries(rules).forEach(([category, codes]) => {
    categorized[category] = [];
    codes.forEach((code) => {
      const tcode = tcodes.find((t) => t.code === code);
      if (tcode) {
        categorized[category].push(tcode);
        usedCodes.add(code);
      }
    });
  });

  // 未分类的放入其他
  const uncategorized = tcodes.filter((t) => !usedCodes.has(t.code));
  if (uncategorized.length > 0) {
    categorized['其他'] = uncategorized;
  }

  return categorized;
}

export default function SiteMapPage() {
  const [searchValue, setSearchValue] = useState('');

  // 过滤事务码
  const filteredTCodes = useMemo(() => {
    if (!searchValue.trim()) {
      return TCODES;
    }
    const keyword = searchValue.toLowerCase();
    return TCODES.filter(
      (t) =>
        t.code.toLowerCase().includes(keyword) ||
        t.name.toLowerCase().includes(keyword) ||
        t.description?.toLowerCase().includes(keyword)
    );
  }, [searchValue]);

  // 按模块分组
  const groupedByModule = useMemo(() => groupByModule(filteredTCodes), [filteredTCodes]);

  // 统计信息
  const stats = useMemo(() => {
    const total = TCODES.length;
    const filtered = filteredTCodes.length;
    const modules = Object.keys(groupedByModule).length;
    return { total, filtered, modules };
  }, [filteredTCodes, groupedByModule]);

  return (
    <div style={{ padding: 24 }}>
      <Card>
        {/* 标题和搜索 */}
        <div style={{ marginBottom: 24 }}>
          <Row gutter={24} align="middle">
            <Col flex="auto">
              <Title level={3} style={{ margin: 0 }}>
                <AppstoreOutlined style={{ marginRight: 12 }} />
                应用地图
              </Title>
              <Paragraph type="secondary" style={{ margin: '8px 0 0' }}>
                共 {stats.total} 个事务码，按模块分组展示
              </Paragraph>
            </Col>
            <Col>
              <Input
                placeholder="搜索事务码或功能名称..."
                prefix={<SearchOutlined />}
                value={searchValue}
                onChange={(e) => setSearchValue(e.target.value)}
                allowClear
                style={{ width: 280 }}
                size="large"
              />
            </Col>
          </Row>
        </div>

        {/* 模块分组展示 */}
        {Object.keys(groupedByModule).length === 0 ? (
          <Empty description="未找到匹配的事务码" />
        ) : (
          <Row gutter={[24, 24]}>
            {Object.entries(groupedByModule).map(([module, tcodes]) => {
              const config = moduleConfig[module] || { name: module, color: '#8c8c8c', icon: '📁' };
              const categories = groupByCategory(tcodes, module);

              return (
                <Col key={module} xs={24} lg={12} xl={8}>
                  <Card
                    title={
                      <Space>
                        <span style={{ fontSize: 18 }}>{config.icon}</span>
                        <span>{config.name}</span>
                        <Tag color={config.color}>{tcodes.length}</Tag>
                      </Space>
                    }
                    size="small"
                    style={{ height: '100%' }}
                    headStyle={{ borderBottom: `2px solid ${config.color}` }}
                  >
                    {Object.entries(categories).map(([category, categoryTcodes]) => (
                      <div key={category} style={{ marginBottom: 16 }}>
                        <Text type="secondary" style={{ fontSize: 12, fontWeight: 500 }}>
                          {category}
                        </Text>
                        <div style={{ marginTop: 8 }}>
                          {categoryTcodes.map((tcode) => (
                            <Link key={tcode.code} href={tcode.path}>
                              <Tag
                                style={{
                                  margin: '4px',
                                  cursor: 'pointer',
                                  fontSize: 12,
                                }}
                                color={config.color}
                              >
                                <Text keyboard style={{ fontSize: 11, color: 'inherit' }}>
                                  {tcode.code}
                                </Text>
                                <span style={{ marginLeft: 4 }}>{tcode.name}</span>
                              </Tag>
                            </Link>
                          ))}
                        </div>
                      </div>
                    ))}
                  </Card>
                </Col>
              );
            })}
          </Row>
        )}

        {/* 使用提示 */}
        <Card
          size="small"
          style={{
            marginTop: 24,
            background: '#f6f8fa',
            border: '1px dashed #d9d9d9',
          }}
        >
          <Space split={<Text type="secondary">|</Text>}>
            <Text type="secondary">
              <Text keyboard>Ctrl</Text> + <Text keyboard>T</Text> 快速打开事务码
            </Text>
            <Text type="secondary">点击事务码跳转到对应功能</Text>
            <Text type="secondary">输入关键词搜索事务码</Text>
          </Space>
        </Card>
      </Card>
    </div>
  );
}
