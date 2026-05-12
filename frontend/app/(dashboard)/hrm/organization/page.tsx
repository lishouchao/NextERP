'use client';

import { useState } from 'react';
import {
  Card, Table, Form, Input, Select, Button, Space, Row, Col, Statistic,
  Tag, Modal, Tree, Descriptions, message, Tabs, Avatar, Progress,
  Dropdown, Tooltip, Badge, Collapse, List, Divider, Empty, Spin,
} from 'antd';
import type { MenuProps, TreeProps } from 'antd';
import {
  SearchOutlined, PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined,
  TeamOutlined, UserOutlined, ApartmentOutlined, SettingOutlined,
  PlusCircleOutlined, DownOutlined, FolderOutlined, FolderOpenOutlined,
  BankOutlined, ClusterOutlined, CrownOutlined, SolutionOutlined,
  EnvironmentOutlined, PhoneOutlined, MailOutlined,
} from '@ant-design/icons';

// ==================== 模拟数据 ====================

const mockOrgUnits = [
  { id: '1', code: 'OU001', name: '总公司', shortName: '总部', parentId: null, type: 'COMPANY', level: 1, managerId: 'EMP001', managerName: '张伟', effectiveDate: '2018-01-01', endDate: null, status: 'ACTIVE', sortOrder: 1, headcount: 45, maxHeadcount: 50, costCenterId: 'CC001', location: '北京市海淀区中关村大街1号' },
  { id: '2', code: 'OU002', name: '技术部', shortName: '技术', parentId: '1', type: 'DEPARTMENT', level: 2, managerId: 'EMP001', managerName: '张伟', effectiveDate: '2018-01-01', endDate: null, status: 'ACTIVE', sortOrder: 1, headcount: 20, maxHeadcount: 28, costCenterId: 'CC002' },
  { id: '3', code: 'OU003', name: '人力资源部', shortName: 'HR', parentId: '1', type: 'DEPARTMENT', level: 2, managerId: 'EMP002', managerName: '李娜', effectiveDate: '2018-01-01', endDate: null, status: 'ACTIVE', sortOrder: 2, headcount: 5, maxHeadcount: 8, costCenterId: 'CC003' },
  { id: '4', code: 'OU004', name: '财务部', shortName: '财务', parentId: '1', type: 'DEPARTMENT', level: 2, managerId: 'EMP004', managerName: '赵敏', effectiveDate: '2018-01-01', endDate: null, status: 'ACTIVE', sortOrder: 3, headcount: 8, maxHeadcount: 10, costCenterId: 'CC004' },
  { id: '5', code: 'OU005', name: '销售部', shortName: '销售', parentId: '1', type: 'DEPARTMENT', level: 2, managerId: 'EMP005', managerName: '刘强', effectiveDate: '2018-01-01', endDate: null, status: 'ACTIVE', sortOrder: 4, headcount: 12, maxHeadcount: 16, costCenterId: 'CC005' },
  { id: '6', code: 'OU006', name: '前端开发组', shortName: '前端', parentId: '2', type: 'TEAM', level: 3, managerId: 'EMP003', managerName: '王磊', effectiveDate: '2019-06-01', endDate: null, status: 'ACTIVE', sortOrder: 1, headcount: 8, maxHeadcount: 10 },
  { id: '7', code: 'OU007', name: '后端开发组', shortName: '后端', parentId: '2', type: 'TEAM', level: 3, managerId: 'EMP007', managerName: '周杰', effectiveDate: '2019-06-01', endDate: null, status: 'ACTIVE', sortOrder: 2, headcount: 7, maxHeadcount: 10 },
  { id: '8', code: 'OU008', name: '测试组', shortName: '测试', parentId: '2', type: 'TEAM', level: 3, managerId: 'EMP011', managerName: '李明', effectiveDate: '2020-01-15', endDate: null, status: 'ACTIVE', sortOrder: 3, headcount: 5, maxHeadcount: 8 },
  { id: '9', code: 'OU009', name: '华东销售组', shortName: '华东', parentId: '5', type: 'TEAM', level: 3, managerId: 'EMP006', managerName: '陈芳', effectiveDate: '2021-03-08', endDate: null, status: 'ACTIVE', sortOrder: 1, headcount: 6, maxHeadcount: 8 },
  { id: '10', code: 'OU010', name: '华南销售组', shortName: '华南', parentId: '5', type: 'TEAM', level: 3, managerId: 'EMP012', managerName: '黄强', effectiveDate: '2021-03-08', endDate: null, status: 'ACTIVE', sortOrder: 2, headcount: 6, maxHeadcount: 8 },
  { id: '11', code: 'OU011', name: '行政部', shortName: '行政', parentId: '1', type: 'DEPARTMENT', level: 2, managerId: 'EMP010', managerName: '孙丽', effectiveDate: '2018-01-01', endDate: null, status: 'ACTIVE', sortOrder: 5, headcount: 4, maxHeadcount: 6 },
];

const mockJobs = [
  { id: '1', code: 'JOB001', name: '技术总监', category: 'M', grade: 'M4', description: '负责技术团队管理和架构设计', requirements: '10年以上开发经验', status: 'ACTIVE' },
  { id: '2', code: 'JOB002', name: '技术经理', category: 'M', grade: 'M3', description: '负责技术团队日常管理', requirements: '5年以上开发经验', status: 'ACTIVE' },
  { id: '3', code: 'JOB003', name: '高级工程师', category: 'P', grade: 'P6', description: '负责核心模块开发', requirements: '3年以上开发经验', status: 'ACTIVE' },
  { id: '4', code: 'JOB004', name: '工程师', category: 'P', grade: 'P5', description: '负责功能模块开发', requirements: '1年以上开发经验', status: 'ACTIVE' },
  { id: '5', code: 'JOB005', name: 'HR经理', category: 'M', grade: 'M3', description: '负责人力资源管理工作', requirements: '5年以上HR经验', status: 'ACTIVE' },
  { id: '6', code: 'JOB006', name: 'HR专员', category: 'S', grade: 'S2', description: '负责招聘/薪酬等模块', requirements: '1年以上HR经验', status: 'ACTIVE' },
  { id: '7', code: 'JOB007', name: '财务主管', category: 'M', grade: 'M2', description: '负责财务核算管理', requirements: '3年以上财务经验', status: 'ACTIVE' },
  { id: '8', code: 'JOB008', name: '销售总监', category: 'M', grade: 'M4', description: '负责销售团队管理', requirements: '8年以上销售经验', status: 'ACTIVE' },
  { id: '9', code: 'JOB009', name: '销售经理', category: 'M', grade: 'M2', description: '负责销售业务拓展', requirements: '3年以上销售经验', status: 'ACTIVE' },
  { id: '10', code: 'JOB010', name: '测试工程师', category: 'P', grade: 'P5', description: '负责软件测试工作', requirements: '2年以上测试经验', status: 'ACTIVE' },
];

const mockPositions = [
  { id: '1', code: 'POS001', name: '技术总监', jobId: '1', jobName: '技术总监', orgUnitId: '2', orgUnitName: '技术部', holderId: 'EMP001', holderName: '张伟', status: 'FILLED', headcount: 1, currentCount: 1 },
  { id: '2', code: 'POS002', name: '前端负责人', jobId: '2', jobName: '技术经理', orgUnitId: '6', orgUnitName: '前端开发组', holderId: 'EMP003', holderName: '王磊', status: 'FILLED', headcount: 1, currentCount: 1 },
  { id: '3', code: 'POS003', name: '后端负责人', jobId: '2', jobName: '技术经理', orgUnitId: '7', orgUnitName: '后端开发组', holderId: 'EMP007', holderName: '周杰', status: 'FILLED', headcount: 1, currentCount: 1 },
  { id: '4', code: 'POS004', name: '高级前端工程师', jobId: '3', jobName: '高级工程师', orgUnitId: '6', orgUnitName: '前端开发组', holderId: 'EMP008', holderName: '吴静', status: 'FILLED', headcount: 2, currentCount: 2 },
  { id: '5', code: 'POS005', name: '前端工程师', jobId: '4', jobName: '工程师', orgUnitId: '6', orgUnitName: '前端开发组', status: 'VACANT', headcount: 5, currentCount: 4 },
  { id: '6', code: 'POS006', name: 'HR经理', jobId: '5', jobName: 'HR经理', orgUnitId: '3', orgUnitName: '人力资源部', holderId: 'EMP002', holderName: '李娜', status: 'FILLED', headcount: 1, currentCount: 1 },
  { id: '7', code: 'POS007', name: '销售总监', jobId: '8', jobName: '销售总监', orgUnitId: '5', orgUnitName: '销售部', holderId: 'EMP005', holderName: '刘强', status: 'FILLED', headcount: 1, currentCount: 1 },
  { id: '8', code: 'POS008', name: '测试负责人', jobId: '2', jobName: '技术经理', orgUnitId: '8', orgUnitName: '测试组', holderId: 'EMP011', holderName: '李明', status: 'FILLED', headcount: 1, currentCount: 1 },
  { id: '9', code: 'POS009', name: '财务主管', jobId: '7', jobName: '财务主管', orgUnitId: '4', orgUnitName: '财务部', holderId: 'EMP004', holderName: '赵敏', status: 'FILLED', headcount: 1, currentCount: 1 },
  { id: '10', code: 'POS010', name: '华东销售经理', jobId: '9', jobName: '销售经理', orgUnitId: '9', orgUnitName: '华东销售组', holderId: 'EMP006', holderName: '陈芳', status: 'FILLED', headcount: 1, currentCount: 1 },
  { id: '11', code: 'POS011', name: '测试工程师', jobId: '10', jobName: '测试工程师', orgUnitId: '8', orgUnitName: '测试组', status: 'VACANT', headcount: 4, currentCount: 3 },
  { id: '12', code: 'POS012', name: '行政主管', jobId: '7', jobName: '财务主管', orgUnitId: '11', orgUnitName: '行政部', holderId: 'EMP010', holderName: '孙丽', status: 'FILLED', headcount: 1, currentCount: 1 },
];

const mockEmployees = [
  { empNo: 'EMP001', name: '张伟', department: '技术部' },
  { empNo: 'EMP002', name: '李娜', department: '人力资源部' },
  { empNo: 'EMP003', name: '王磊', department: '技术部' },
  { empNo: 'EMP004', name: '赵敏', department: '财务部' },
  { empNo: 'EMP005', name: '刘强', department: '销售部' },
];

// ==================== 配置 ====================

const orgTypeConfig: Record<string, { color: string; text: string; icon: any }> = {
  'COMPANY': { color: 'gold', text: '公司', icon: <BankOutlined /> },
  'BRANCH': { color: 'purple', text: '分公司', icon: <BankOutlined /> },
  'DEPARTMENT': { color: 'blue', text: '部门', icon: <ApartmentOutlined /> },
  'CENTER': { color: 'cyan', text: '中心', icon: <ClusterOutlined /> },
  'TEAM': { color: 'green', text: '小组', icon: <TeamOutlined /> },
  'PROJECT': { color: 'orange', text: '项目组', icon: <SolutionOutlined /> },
};

const statusConfig: Record<string, { color: string; text: string }> = {
  'ACTIVE': { color: 'green', text: '生效中' },
  'INACTIVE': { color: 'default', text: '已失效' },
  'PLANNED': { color: 'orange', text: '规划中' },
};

const positionStatusConfig: Record<string, { color: string; text: string }> = {
  'FILLED': { color: 'green', text: '已填' },
  'VACANT': { color: 'orange', text: '空缺' },
  'FROZEN': { color: 'blue', text: '冻结' },
  'ABOLISHED': { color: 'default', text: '撤销' },
};

const jobCategoryConfig: Record<string, { color: string; text: string }> = {
  'M': { color: 'gold', text: '管理类' },
  'P': { color: 'blue', text: '专业类' },
  'S': { color: 'green', text: '支持类' },
  'O': { color: 'default', text: '操作类' },
};

export default function OrganizationPage() {
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('structure');
  const [expandedKeys, setExpandedKeys] = useState<string[]>(['OU001', 'OU002', 'OU005']);
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);
  const [selectedOrgUnit, setSelectedOrgUnit] = useState<typeof mockOrgUnits[0] | null>(null);
  const [searchValue, setSearchValue] = useState('');
  const [modalVisible, setModalVisible] = useState(false);
  const [modalType, setModalType] = useState<'orgUnit' | 'position' | 'job'>('orgUnit');
  const [selectedPosition, setSelectedPosition] = useState<typeof mockPositions[0] | null>(null);
  const [form] = Form.useForm();

  // 构建组织树
  const buildOrgTree = (parentId: string | null = null): TreeProps['treeData'] => {
    return mockOrgUnits
      .filter(ou => ou.parentId === parentId)
      .sort((a, b) => a.sortOrder - b.sortOrder)
      .map(ou => {
        const children = buildOrgTree(ou.id);
        const positions = mockPositions.filter(p => p.orgUnitId === ou.id);
        const percent = Math.round((ou.headcount / ou.maxHeadcount) * 100);
        return {
          title: (
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%', paddingRight: 8 }}>
              <Space>
                {orgTypeConfig[ou.type]?.icon}
                <span style={{ fontWeight: ou.level === 1 ? 600 : 400 }}>{ou.name}</span>
                <Tag color={orgTypeConfig[ou.type]?.color} style={{ marginLeft: 4 }}>{orgTypeConfig[ou.type]?.text}</Tag>
                <Tag color={statusConfig[ou.status]?.color}>{statusConfig[ou.status]?.text}</Tag>
              </Space>
              <Space size="small">
                <Progress percent={percent} size="small" style={{ width: 50 }} showInfo={false} />
                <span style={{ fontSize: 12, color: '#8c8c8c' }}>{ou.headcount}人</span>
                {ou.managerName && (
                  <Tooltip title={`负责人: ${ou.managerName}`}>
                    <Avatar size="small" style={{ backgroundColor: '#1890ff' }}>{ou.managerName[0]}</Avatar>
                  </Tooltip>
                )}
                <Dropdown menu={{ items: getOrgActionItems(ou) }} trigger={['click']}>
                  <Button type="text" size="small" icon={<SettingOutlined />} onClick={e => e.stopPropagation()} />
                </Dropdown>
              </Space>
            </div>
          ),
          key: ou.code,
          icon: (props: any) => props?.expanded ? <FolderOpenOutlined /> : <FolderOutlined />,
          children: children && children.length > 0 ? children : undefined,
        };
      });
  };

  // 操作菜单
  const getOrgActionItems = (orgUnit: typeof mockOrgUnits[0]): MenuProps['items'] => [
    { key: 'edit', icon: <EditOutlined />, label: '编辑组织', onClick: () => { setSelectedOrgUnit(orgUnit); setModalType('orgUnit'); setModalVisible(true); } },
    { key: 'addChild', icon: <PlusCircleOutlined />, label: '添加子组织', onClick: () => { setSelectedOrgUnit(orgUnit); setModalType('orgUnit'); setModalVisible(true); } },
    { key: 'addPosition', icon: <UserOutlined />, label: '添加职位', onClick: () => { setSelectedOrgUnit(orgUnit); setModalType('position'); setModalVisible(true); } },
    { type: 'divider' },
    { key: 'delete', icon: <DeleteOutlined />, label: '撤销组织', danger: true },
  ];

  // 选择组织节点
  const onSelect: TreeProps['onSelect'] = (keys) => {
    setSelectedKeys(keys as string[]);
    if (keys.length > 0) {
      const orgUnit = mockOrgUnits.find(ou => ou.code === keys[0]);
      if (orgUnit) {
        setSelectedOrgUnit(orgUnit);
      }
    }
  };

  // 统计数据
  const stats = {
    totalOrgUnits: mockOrgUnits.length,
    activeOrgUnits: mockOrgUnits.filter(ou => ou.status === 'ACTIVE').length,
    totalPositions: mockPositions.length,
    filledPositions: mockPositions.filter(p => p.status === 'FILLED').length,
    vacantPositions: mockPositions.filter(p => p.status === 'VACANT').length,
    totalJobs: mockJobs.length,
    totalHeadcount: mockOrgUnits.reduce((s, ou) => s + ou.headcount, 0),
    totalMaxHeadcount: mockOrgUnits.reduce((s, ou) => s + ou.maxHeadcount, 0),
  };

  // 职务表格列
  const jobColumns = [
    { title: '职务编码', dataIndex: 'code', width: 100 },
    { title: '职务名称', dataIndex: 'name', width: 120 },
    { title: '分类', dataIndex: 'category', width: 80, render: (cat: string) => <Tag color={jobCategoryConfig[cat]?.color}>{jobCategoryConfig[cat]?.text}</Tag> },
    { title: '职级', dataIndex: 'grade', width: 80, render: (g: string) => <Tag color="blue">{g}</Tag> },
    { title: '描述', dataIndex: 'description', ellipsis: true },
    { title: '状态', dataIndex: 'status', width: 80, render: (s: string) => <Tag color={statusConfig[s]?.color}>{statusConfig[s]?.text}</Tag> },
    { title: '操作', width: 100, render: () => <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button> },
  ];

  // 职位表格列
  const positionColumns = [
    { title: '职位编码', dataIndex: 'code', width: 100 },
    { title: '职位名称', dataIndex: 'name', width: 120 },
    { title: '所属组织', dataIndex: 'orgUnitName', width: 120 },
    { title: '职务', dataIndex: 'jobName', width: 100 },
    { title: '持有人', dataIndex: 'holderName', width: 100, render: (v: string) => v ? <Space><Avatar size="small">{v[0]}</Avatar>{v}</Space> : <Tag color="orange">空缺</Tag> },
    { title: '编制', key: 'headcount', width: 100, render: (_: any, r: any) => `${r.currentCount}/${r.headcount}` },
    { title: '状态', dataIndex: 'status', width: 80, render: (s: string) => <Tag color={positionStatusConfig[s]?.color}>{positionStatusConfig[s]?.text}</Tag> },
    { title: '操作', width: 120, render: () => <Space><Button type="link" size="small">详情</Button><Button type="link" size="small" icon={<EditOutlined />}>编辑</Button></Space> },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card title="组织管理 (对标 SAP PPOME)"
        extra={<Space>
          <Button icon={<ReloadOutlined />} onClick={() => setLoading(!loading)}>刷新</Button>
          <Dropdown menu={{
            items: [
              { key: 'orgUnit', label: '新增组织单位', icon: <ApartmentOutlined /> },
              { key: 'position', label: '新增职位', icon: <UserOutlined /> },
              { key: 'job', label: '新增职务', icon: <SolutionOutlined /> },
            ],
            onClick: (e) => { setModalType(e.key as any); setModalVisible(true); },
          }}>
            <Button type="primary" icon={<PlusOutlined />}>新增</Button>
          </Dropdown>
        </Space>}
      >
        <Tabs activeKey={activeTab} onChange={setActiveTab}
          items={[
            { key: 'structure', label: <><ApartmentOutlined /> 组织架构</> },
            { key: 'positions', label: <><UserOutlined /> 职位管理</> },
            { key: 'jobs', label: <><SolutionOutlined /> 职务管理</> },
          ]} />

        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={3}><Card size="small"><Statistic title="组织单位" value={stats.totalOrgUnits} suffix="个" valueStyle={{ fontSize: 16 }} /></Card></Col>
          <Col span={3}><Card size="small"><Statistic title="职位总数" value={stats.totalPositions} suffix="个" valueStyle={{ fontSize: 16, color: '#1890ff' }} /></Card></Col>
          <Col span={3}><Card size="small"><Statistic title="空缺职位" value={stats.vacantPositions} suffix="个" valueStyle={{ fontSize: 16, color: '#faad14' }} /></Card></Col>
          <Col span={3}><Card size="small"><Statistic title="职务数量" value={stats.totalJobs} suffix="个" valueStyle={{ fontSize: 16, color: '#52c41a' }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="在职人数" value={stats.totalHeadcount} suffix="人" valueStyle={{ fontSize: 16 }} /></Card></Col>
          <Col span={4}><Card size="small"><Statistic title="编制总数" value={stats.totalMaxHeadcount} suffix="人" valueStyle={{ fontSize: 16, color: '#722ed1' }} /></Card></Col>
          <Col span={4}>
            <Card size="small">
              <Progress type="circle" percent={Math.round((stats.totalHeadcount / stats.totalMaxHeadcount) * 100)} size={50} />
              <div style={{ textAlign: 'center', fontSize: 12, color: '#8c8c8c', marginTop: 4 }}>编制利用率</div>
            </Card>
          </Col>
        </Row>

        {/* 组织架构 */}
        {activeTab === 'structure' && (
          <Row gutter={24}>
            <Col span={16}>
              <Card title="组织架构图" size="small" extra={
                <Space>
                  <Input placeholder="搜索组织" prefix={<SearchOutlined />} value={searchValue} onChange={e => setSearchValue(e.target.value)} style={{ width: 150 }} allowClear />
                  <Button size="small" onClick={() => setExpandedKeys(mockOrgUnits.map(ou => ou.code))}>展开全部</Button>
                  <Button size="small" onClick={() => setExpandedKeys([])}>收起全部</Button>
                </Space>
              }>
                <Tree
                  showLine={{ showLeafIcon: false }}
                  showIcon
                  blockNode
                  expandedKeys={expandedKeys}
                  selectedKeys={selectedKeys}
                  onExpand={(keys: any) => setExpandedKeys(keys)}
                  onSelect={onSelect}
                  treeData={buildOrgTree()}
                  style={{ fontSize: 14 }}
                  switcherIcon={<DownOutlined />}
                />
              </Card>
            </Col>
            <Col span={8}>
              {selectedOrgUnit ? (
                <Card title={`${selectedOrgUnit.name} 详情`} size="small" extra={<Button size="small" icon={<EditOutlined />}>编辑</Button>}>
                  <Descriptions size="small" column={1} bordered>
                    <Descriptions.Item label="编码">{selectedOrgUnit.code}</Descriptions.Item>
                    <Descriptions.Item label="类型"><Tag color={orgTypeConfig[selectedOrgUnit.type]?.color}>{orgTypeConfig[selectedOrgUnit.type]?.text}</Tag></Descriptions.Item>
                    <Descriptions.Item label="负责人">
                      {selectedOrgUnit.managerName ? <Space><Avatar size="small">{selectedOrgUnit.managerName[0]}</Avatar>{selectedOrgUnit.managerName}</Space> : '-'}
                    </Descriptions.Item>
                    <Descriptions.Item label="编制">
                      <Progress percent={Math.round((selectedOrgUnit.headcount / selectedOrgUnit.maxHeadcount) * 100)} size="small" format={() => `${selectedOrgUnit.headcount}/${selectedOrgUnit.maxHeadcount}`} />
                    </Descriptions.Item>
                    <Descriptions.Item label="成本中心">{selectedOrgUnit.costCenterId || '-'}</Descriptions.Item>
                    <Descriptions.Item label="状态"><Tag color={statusConfig[selectedOrgUnit.status]?.color}>{statusConfig[selectedOrgUnit.status]?.text}</Tag></Descriptions.Item>
                  </Descriptions>
                  <Divider orientation="left">下属职位</Divider>
                  <List
                    size="small"
                    dataSource={mockPositions.filter(p => p.orgUnitId === selectedOrgUnit.id)}
                    renderItem={item => (
                      <List.Item>
                        <List.Item.Meta avatar={<Avatar size="small" style={{ backgroundColor: item.holderName ? '#52c41a' : '#faad14' }}>{item.holderName ? item.holderName[0] : '?'}</Avatar>}
                          title={item.name} description={item.jobName} />
                        <Tag color={positionStatusConfig[item.status]?.color}>{positionStatusConfig[item.status]?.text}</Tag>
                      </List.Item>
                    )}
                    locale={{ emptyText: '暂无职位' }}
                  />
                </Card>
              ) : (
                <Card size="small"><Empty description="请选择组织单位" /></Card>
              )}
            </Col>
          </Row>
        )}

        {/* 职位管理 */}
        {activeTab === 'positions' && (
          <>
            <Form layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item><Input placeholder="职位编码/名称" style={{ width: 150 }} /></Form.Item>
              <Form.Item><Select placeholder="所属组织" allowClear style={{ width: 150 }} options={mockOrgUnits.map(ou => ({ value: ou.id, label: ou.name }))} /></Form.Item>
              <Form.Item><Select placeholder="状态" allowClear style={{ width: 100 }} options={Object.entries(positionStatusConfig).map(([k, v]) => ({ value: k, label: v.text }))} /></Form.Item>
              <Form.Item><Button type="primary" icon={<SearchOutlined />}>查询</Button></Form.Item>
            </Form>
            <Table columns={positionColumns} dataSource={mockPositions} rowKey="id" size="small" loading={loading} scroll={{ x: 1000 }} pagination={{ defaultPageSize: 20 }} />
          </>
        )}

        {/* 职务管理 */}
        {activeTab === 'jobs' && (
          <>
            <Form layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item><Input placeholder="职务编码/名称" style={{ width: 150 }} /></Form.Item>
              <Form.Item><Select placeholder="分类" allowClear style={{ width: 100 }} options={Object.entries(jobCategoryConfig).map(([k, v]) => ({ value: k, label: v.text }))} /></Form.Item>
              <Form.Item><Button type="primary" icon={<SearchOutlined />}>查询</Button></Form.Item>
            </Form>
            <Table columns={jobColumns} dataSource={mockJobs} rowKey="id" size="small" loading={loading} pagination={{ defaultPageSize: 20 }} />
          </>
        )}
      </Card>

      {/* 通用弹窗 */}
      <Modal
        title={modalType === 'orgUnit' ? '组织单位' : modalType === 'position' ? '职位' : '职务'}
        open={modalVisible}
        onCancel={() => { setModalVisible(false); form.resetFields(); }}
        onOk={() => { message.success('保存成功'); setModalVisible(false); }}
        width={700}
      >
        {modalType === 'orgUnit' && (
          <Form form={form} layout="vertical" initialValues={selectedOrgUnit as any}>
            <Row gutter={16}>
              <Col span={12}><Form.Item label="组织编码" name="code" required><Input /></Form.Item></Col>
              <Col span={12}><Form.Item label="组织名称" name="name" required><Input /></Form.Item></Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}><Form.Item label="组织类型" name="type" required><Select options={Object.entries(orgTypeConfig).map(([k, v]) => ({ value: k, label: v.text }))} /></Form.Item></Col>
              <Col span={12}><Form.Item label="上级组织" name="parentId"><Select allowClear options={mockOrgUnits.filter(ou => ou.level < 3).map(ou => ({ value: ou.id, label: ou.name }))} /></Form.Item></Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}><Form.Item label="负责人" name="managerId"><Select showSearch optionFilterProp="label" options={mockEmployees.map(e => ({ value: e.empNo, label: e.name }))} /></Form.Item></Col>
              <Col span={12}><Form.Item label="成本中心" name="costCenterId"><Input /></Form.Item></Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}><Form.Item label="编制人数" name="maxHeadcount"><Input type="number" /></Form.Item></Col>
              <Col span={12}><Form.Item label="状态" name="status"><Select options={Object.entries(statusConfig).map(([k, v]) => ({ value: k, label: v.text }))} /></Form.Item></Col>
            </Row>
          </Form>
        )}
        {modalType === 'position' && (
          <Form form={form} layout="vertical">
            <Row gutter={16}>
              <Col span={12}><Form.Item label="职位编码" required><Input /></Form.Item></Col>
              <Col span={12}><Form.Item label="职位名称" required><Input /></Form.Item></Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}><Form.Item label="所属组织" required><Select options={mockOrgUnits.map(ou => ({ value: ou.id, label: ou.name }))} /></Form.Item></Col>
              <Col span={12}><Form.Item label="职务" required><Select options={mockJobs.map(j => ({ value: j.id, label: j.name }))} /></Form.Item></Col>
            </Row>
            <Row gutter={16}>
              <Col span={8}><Form.Item label="编制数"><Input type="number" /></Form.Item></Col>
              <Col span={8}><Form.Item label="持有人"><Select allowClear options={mockEmployees.map(e => ({ value: e.empNo, label: e.name }))} /></Form.Item></Col>
              <Col span={8}><Form.Item label="状态"><Select options={Object.entries(positionStatusConfig).map(([k, v]) => ({ value: k, label: v.text }))} /></Form.Item></Col>
            </Row>
          </Form>
        )}
        {modalType === 'job' && (
          <Form form={form} layout="vertical">
            <Row gutter={16}>
              <Col span={12}><Form.Item label="职务编码" required><Input /></Form.Item></Col>
              <Col span={12}><Form.Item label="职务名称" required><Input /></Form.Item></Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}><Form.Item label="职务分类"><Select options={Object.entries(jobCategoryConfig).map(([k, v]) => ({ value: k, label: v.text }))} /></Form.Item></Col>
              <Col span={12}><Form.Item label="职级"><Input /></Form.Item></Col>
            </Row>
            <Form.Item label="职务描述"><Input.TextArea rows={3} /></Form.Item>
            <Form.Item label="任职要求"><Input.TextArea rows={3} /></Form.Item>
          </Form>
        )}
      </Modal>
    </div>
  );
}
