'use client';

import { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { Layout, Menu } from 'antd';
import {
  DashboardOutlined,
  AppstoreOutlined,
  TeamOutlined,
  DollarOutlined,
  ShoppingCartOutlined,
  ShopOutlined,
  ToolOutlined,
  SettingOutlined,
} from '@ant-design/icons';

const { Sider } = Layout;

const menuItems = [
  {
    key: '/dashboard',
    icon: <DashboardOutlined />,
    label: '仪表盘',
    href: '/dashboard',
  },
  {
    key: '/sitemap',
    icon: <AppstoreOutlined />,
    label: '应用地图',
    href: '/sitemap',
  },
  {
    key: '/hrm',
    icon: <TeamOutlined />,
    label: '人力资源',
    children: [
      { key: '/hrm/organization', label: '组织管理', href: '/hrm/organization' },
      { key: '/hrm/employees', label: '员工管理', href: '/hrm/employees' },
      { key: '/hrm/actions', label: '人事措施', href: '/hrm/actions' },
      { key: '/hrm/attendance', label: '考勤管理', href: '/hrm/attendance' },
      { key: '/hrm/leave', label: '请假管理', href: '/hrm/leave' },
      { key: '/hrm/payroll', label: '薪酬管理', href: '/hrm/payroll' },
    ],
  },
  {
    key: '/finance',
    icon: <DollarOutlined />,
    label: '财务管理',
    children: [
      { key: '/finance/accounts', label: '会计科目', href: '/finance/accounts' },
      { key: '/finance/vouchers', label: '凭证管理', href: '/finance/vouchers' },
      { key: '/finance/ledger', label: '总账查询', href: '/finance/ledger' },
      { key: '/finance/receivables', label: '应收管理', href: '/finance/receivables' },
      { key: '/finance/payables', label: '应付管理', href: '/finance/payables' },
      { key: '/finance/assets', label: '固定资产', href: '/finance/assets' },
      { key: '/finance/periods', label: '期末结账', href: '/finance/periods' },
      { key: '/finance/reports', label: '财务报表', href: '/finance/reports' },
    ],
  },
  {
    key: '/supply',
    icon: <ShopOutlined />,
    label: '供应链',
    children: [
      { key: '/supply/inventory', label: '库存管理', href: '/supply/inventory' },
      { key: '/supply/purchase', label: '采购管理', href: '/supply/purchase' },
    ],
  },
  {
    key: '/sales',
    icon: <ShoppingCartOutlined />,
    label: '销售管理',
    children: [
      { key: '/sales/orders', label: '订单管理', href: '/sales/orders' },
      { key: '/sales/customers', label: '客户管理', href: '/sales/customers' },
    ],
  },
  {
    key: '/production',
    icon: <ToolOutlined />,
    label: '生产管理',
    children: [
      { key: '/production/orders', label: '生产工单', href: '/production/orders' },
      { key: '/production/bom', label: 'BOM 管理', href: '/production/bom' },
    ],
  },
  {
    key: '/system',
    icon: <SettingOutlined />,
    label: '系统设置',
    children: [
      { key: '/system/users', label: '用户管理', href: '/system/users' },
      { key: '/system/roles', label: '角色管理', href: '/system/roles' },
      { key: '/system/permissions', label: '权限管理', href: '/system/permissions' },
    ],
  },
];

export default function Sidebar() {
  const [collapsed, setCollapsed] = useState(false);
  const pathname = usePathname();

  // 获取当前选中的菜单项
  const getSelectedKeys = () => {
    const match = pathname?.split('/').slice(0, 3).join('/');
    return match || '/dashboard';
  };

  // 获取当前展开的菜单
  const getOpenKeys = () => {
    const firstSegment = pathname?.split('/')[1];
    return firstSegment ? [`/${firstSegment}`] : [];
  };

  return (
    <Sider
      collapsible
      collapsed={collapsed}
      onCollapse={setCollapsed}
      theme="light"
      style={{
        overflow: 'auto',
        height: '100vh',
        position: 'fixed',
        left: 0,
        top: 0,
        bottom: 0,
        borderRight: '1px solid #f0f0f0',
      }}
    >
      <div
        style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          borderBottom: '1px solid #f0f0f0',
        }}
      >
        <Link href="/dashboard" style={{ textDecoration: 'none' }}>
          <span style={{ fontSize: collapsed ? 16 : 20, fontWeight: 'bold', color: '#1890ff' }}>
            {collapsed ? 'ERP' : 'NextERP'}
          </span>
        </Link>
      </div>
      <Menu
        mode="inline"
        selectedKeys={[getSelectedKeys()]}
        defaultOpenKeys={getOpenKeys()}
        items={menuItems.map((item) =>
          item.children
            ? {
                ...item,
                children: item.children.map((child) => ({
                  key: child.key,
                  label: <Link href={child.href}>{child.label}</Link>,
                })),
              }
            : {
                key: item.key,
                icon: item.icon,
                label: <Link href={item.href!}>{item.label}</Link>,
              }
        )}
      />
    </Sider>
  );
}
