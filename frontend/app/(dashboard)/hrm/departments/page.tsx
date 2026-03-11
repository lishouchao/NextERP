'use client';

import { useState } from 'react';
import {
  Card, Form, Input, Select, Button, Space, Row, Col, Statistic,
  Tag, Modal, Tree, Descriptions, message, Tabs, List, Avatar, Progress,
  Dropdown, Tooltip,
} from 'antd';
import type { MenuProps, TreeProps } from 'antd';
import {
  SearchOutlined, PlusOutlined, EditOutlined, DeleteOutlined, ReloadOutlined,
  TeamOutlined, UserOutlined, ApartmentOutlined, SettingOutlined,
  PlusCircleOutlined, DownOutlined, FolderOutlined, FolderOpenOutlined,
} from '@ant-design/icons';

const mockDepartments = [
  { id: 1, code: 'DEPT-001', name: '技术部', parentId: null, manager: '张伟', managerEmpNo: 'EMP001', headcount: 15, maxHeadcount: 20, level: 1, status: 1, description: '负责产品技术研发', createdAt: '2018-01-01' },
  { id: 2, code: 'DEPT-002', name: '人力资源部', parentId: null, manager: '李娜', managerEmpNo: 'EMP002', headcount: 5, maxHeadcount: 8, level: 1, status: 1, description: '负责人力资源管理工作', createdAt: '2018-01-01' },
  { id: 3, code: 'DEPT-003', name: '财务部', parentId: null, manager: '赵敏', managerEmpNo: 'EMP004', headcount: 8, maxHeadcount: 10, level: 1, status: 1, description: '负责财务管理与核算', createdAt: '2018-01-01' },
  { id: 4, code: 'DEPT-004', name: '销售部', parentId: null, manager: '刘强', managerEmpNo: 'EMP005', headcount: 12, maxHeadcount: 15, level: 1, status: 1, description: '负责产品销售与客户关系', createdAt: '2018-01-01' },
  { id: 5, code: 'DEPT-005', name: '前端开发组', parentId: 1, manager: '王磊', managerEmpNo: 'EMP003', headcount: 8, maxHeadcount: 10, level: 2, status: 1, description: '前端技术团队', createdAt: '2019-06-01' },
  { id: 6, code: 'DEPT-006', name: '后端开发组', parentId: 1, manager: '周杰', managerEmpNo: 'EMP007', headcount: 7, maxHeadcount: 10, level: 2, status: 1, description: '后端技术团队', createdAt: '2019-06-01' },
  { id: 7, code: 'DEPT-007', name: '行政部', parentId: null, manager: '孙丽', managerEmpNo: 'EMP010', headcount: 4, maxHeadcount: 6, level: 1, status: 1, description: '负责行政后勤管理', createdAt: '2018-01-01' },
  { id: 8, code: 'DEPT-008', name: '测试组', parentId: 1, manager: '李明', managerEmpNo: 'EMP011', headcount: 5, maxHeadcount: 8, level: 2, status: 1, description: '质量保障团队', createdAt: '2020-01-15' },
  { id: 9, code: 'DEPT-009', name: '华东销售组', parentId: 4, manager: '陈芳', managerEmpNo: 'EMP006', headcount: 6, maxHeadcount: 8, level: 2, status: 1, description: '华东区销售团队', createdAt: '2021-03-08' },
  { id: 10, code: 'DEPT-010', name: '华南销售组', parentId: 4, manager: '黄强', managerEmpNo: 'EMP012', headcount: 6, maxHeadcount: 8, level: 2, status: 1, description: '华南区销售团队', createdAt: '2021-03-08' },
];

const mockEmployees = [
  { empNo: 'EMP001', name: '张伟', department: '技术部' },
  { empNo: 'EMP002', name: '李娜', department: '人力资源部' },
  { empNo: 'EMP003', name: '王磊', department: '技术部' },
  { empNo: 'EMP004', name: '赵敏', department: '财务部' },
  { empNo: 'EMP005', name: '刘强', department: '销售部' },
];

const statusConfig: Record<number, { color: string; text: string }> = {
  1: { color: 'green', text: '正常' },
  2: { color: 'orange', text: '调整中' },
  3: { color: 'red', text: '已撤销' },
};

export default function DepartmentsPage() {
  const [loading, setLoading] = useState(false);
  const [departments] = useState(mockDepartments);
  const [expandedKeys, setExpandedKeys] = useState<string[]>(['DEPT-001', 'DEPT-004']);
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);
  const [searchValue, setSearchValue] = useState('');
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [selected, setSelected] = useState<typeof mockDepartments[0] | null>(null);
  const [form] = Form.useForm();

  // 构建树形数据
  const buildTreeData = (depts: typeof mockDepartments, parentId: number | null = null): TreeProps['treeData'] => {
    return depts
      .filter(d => d.parentId === parentId)
      .sort((a, b) => a.code.localeCompare(b.code))
      .map(d => {
        const children = buildTreeData(depts, d.id);
        const percent = Math.round((d.headcount / d.maxHeadcount) * 100);
        return {
          title: (
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%', paddingRight: 8 }}>
              <Space>
                <span style={{ fontWeight: d.level === 1 ? 600 : 400 }}>{d.name}</span>
                <Tag color={d.level === 1 ? 'blue' : 'green'} style={{ marginLeft: 4 }}>L{d.level}</Tag>
                <Tag color={statusConfig[d.status]?.color}>{statusConfig[d.status]?.text}</Tag>
              </Space>
              <Space size="small">
                <Tooltip title={`${d.headcount}/${d.maxHeadcount} 人`}>
                  <Progress percent={percent} size="small" style={{ width: 60 }} showInfo={false} />
                  <span style={{ fontSize: 12, color: '#8c8c8c', marginLeft: 4 }}>{d.headcount}人</span>
                </Tooltip>
                <span style={{ fontSize: 12, color: '#8c8c8c' }}>{d.manager}</span>
                <Dropdown menu={{ items: getActionItems(d) }} trigger={['click']}>
                  <Button type="text" size="small" icon={<SettingOutlined />} onClick={e => e.stopPropagation()} />
                </Dropdown>
              </Space>
            </div>
          ),
          key: d.code,
          icon: (props: any) => props?.expanded ? <FolderOpenOutlined /> : <FolderOutlined />,
          children: children && children.length > 0 ? children : undefined,
        };
      });
  };

  // 操作菜单
  const getActionItems = (dept: typeof mockDepartments[0]): MenuProps['items'] => [
    { key: 'edit', icon: <EditOutlined />, label: '编辑部门', onClick: () => { setSelected(dept); setModalVisible(true); } },
    { key: 'addChild', icon: <PlusCircleOutlined />, label: '添加子部门', onClick: () => { setSelected(dept); setModalVisible(true); } },
    { key: 'detail', icon: <TeamOutlined />, label: '查看详情', onClick: () => { setSelected(dept); setDetailVisible(true); } },
    { type: 'divider' },
    { key: 'delete', icon: <DeleteOutlined />, label: '删除部门', danger: true },
  ];

  // 搜索过滤
  const filterTree = (depts: typeof mockDepartments, keyword: string) => {
    if (!keyword) return depts;
    return depts.filter(d =>
      d.name.toLowerCase().includes(keyword.toLowerCase()) ||
      d.code.toLowerCase().includes(keyword.toLowerCase()) ||
      d.manager.toLowerCase().includes(keyword.toLowerCase())
    );
  };

  // 选择节点
  const onSelect: TreeProps['onSelect'] = (keys) => {
    setSelectedKeys(keys as string[]);
    if (keys.length > 0) {
      const dept = departments.find(d => d.code === keys[0]);
      if (dept) {
        setSelected(dept);
        setDetailVisible(true);
      }
    }
  };

  // 展开/收起
  const onExpand: TreeProps['onExpand'] = (keys) => {
    setExpandedKeys(keys as string[]);
  };

  // 统计
  const stats = {
    total: departments.length,
    level1: departments.filter(d => d.level === 1).length,
    level2: departments.filter(d => d.level === 2).length,
    level3: departments.filter(d => d.level === 3).length,
    totalHeadcount: departments.reduce((s, d) => s + d.headcount, 0),
    totalMaxHeadcount: departments.reduce((s, d) => s + d.maxHeadcount, 0),
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="部门管理 (对标 SAP PPOME)"
        extra={<Space>
          <Button icon={<ReloadOutlined />} onClick={() => setLoading(!loading)}>刷新</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => { setSelected(null); setModalVisible(true); }}>新增部门</Button>
        </Space>}>
        <Tabs
          defaultActiveKey="tree"
          items={[
            { key: 'tree', label: <><ApartmentOutlined /> 组织架构</> },
            { key: 'stats', label: <><TeamOutlined /> 统计分析</> },
          ]}
        />

        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={4}>
            <Card size="small">
              <Statistic title="部门总数" value={stats.total} suffix="个" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="一级部门" value={stats.level1} suffix="个" valueStyle={{ fontSize: 18, color: '#1890ff' }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="二级部门" value={stats.level2} suffix="个" valueStyle={{ fontSize: 18, color: '#52c41a' }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="在职人数" value={stats.totalHeadcount} suffix="人" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Statistic title="编制总数" value={stats.totalMaxHeadcount} suffix="人" valueStyle={{ fontSize: 18, color: '#faad14' }} />
            </Card>
          </Col>
          <Col span={4}>
            <Card size="small">
              <Progress
                type="circle"
                percent={Math.round((stats.totalHeadcount / stats.totalMaxHeadcount) * 100)}
                size={60}
                format={percent => `${percent}%`}
              />
              <div style={{ textAlign: 'center', fontSize: 12, color: '#8c8c8c', marginTop: 4 }}>编制利用率</div>
            </Card>
          </Col>
        </Row>

        {/* 组织架构树 */}
        <Row gutter={24}>
          <Col span={16}>
            <Card
              title={
                <Space>
                  <ApartmentOutlined />
                  <span>组织架构</span>
                </Space>
              }
              size="small"
              extra={
                <Space>
                  <Input
                    placeholder="搜索部门/负责人"
                    prefix={<SearchOutlined />}
                    value={searchValue}
                    onChange={e => setSearchValue(e.target.value)}
                    style={{ width: 180 }}
                    allowClear
                  />
                  <Button size="small" onClick={() => setExpandedKeys(departments.map(d => d.code))}>展开全部</Button>
                  <Button size="small" onClick={() => setExpandedKeys([])}>收起全部</Button>
                </Space>
              }
            >
              <Tree
                showLine={{ showLeafIcon: false }}
                showIcon
                blockNode
                expandedKeys={expandedKeys}
                selectedKeys={selectedKeys}
                onExpand={onExpand}
                onSelect={onSelect}
                treeData={buildTreeData(searchValue ? filterTree(departments, searchValue) : departments)}
                style={{ fontSize: 14 }}
                switcherIcon={<DownOutlined />}
              />
            </Card>
          </Col>
          <Col span={8}>
            <Card title="快速操作" size="small" style={{ marginBottom: 16 }}>
              <Space direction="vertical" style={{ width: '100%' }}>
                <Button type="dashed" block icon={<PlusOutlined />}>新增一级部门</Button>
                <Button type="dashed" block icon={<UserOutlined />}>人员调动</Button>
                <Button type="dashed" block icon={<TeamOutlined />}>批量导入</Button>
              </Space>
            </Card>

            <Card title="部门层级分布" size="small">
              <List
                size="small"
                dataSource={[
                  { level: '一级部门', count: stats.level1, color: '#1890ff' },
                  { level: '二级部门', count: stats.level2, color: '#52c41a' },
                ]}
                renderItem={item => (
                  <List.Item>
                    <List.Item.Meta
                      avatar={<Tag color={item.color}>{item.level}</Tag>}
                      title={item.level}
                    />
                    <span style={{ fontWeight: 'bold' }}>{item.count} 个</span>
                  </List.Item>
                )}
              />
            </Card>

            {selected && (
              <Card title={`选中: ${selected.name}`} size="small" style={{ marginTop: 16 }}>
                <Descriptions size="small" column={1}>
                  <Descriptions.Item label="编码">{selected.code}</Descriptions.Item>
                  <Descriptions.Item label="负责人">
                    <Space><Avatar size="small" icon={<UserOutlined />} />{selected.manager}</Space>
                  </Descriptions.Item>
                  <Descriptions.Item label="编制">
                    <Progress percent={Math.round((selected.headcount / selected.maxHeadcount) * 100)} size="small" />
                  </Descriptions.Item>
                </Descriptions>
              </Card>
            )}
          </Col>
        </Row>
      </Card>

      {/* 新增/编辑部门弹窗 */}
      <Modal
        title={selected ? `编辑部门 - ${selected.name}` : '新增部门'}
        open={modalVisible}
        onCancel={() => { setModalVisible(false); setSelected(null); form.resetFields(); }}
        onOk={() => { message.success('保存成功'); setModalVisible(false); form.resetFields(); }}
        width={600}
      >
        <Form form={form} layout="vertical" initialValues={selected || {}}>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="部门编码" name="code" required>
                <Input placeholder="部门编码" disabled={!!selected} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="部门名称" name="name" required>
                <Input placeholder="部门名称" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="上级部门" name="parentId">
                <Select
                  placeholder="无（顶级部门）"
                  allowClear
                  showSearch
                  optionFilterProp="label"
                  options={departments.filter(d => d.level === 1).map(d => ({ value: d.id, label: d.name }))}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="部门负责人" name="managerEmpNo">
                <Select
                  placeholder="选择负责人"
                  showSearch
                  optionFilterProp="label"
                  options={mockEmployees.map(e => ({ value: e.empNo, label: `${e.name} (${e.department})` }))}
                />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="当前人数" name="headcount">
                <Input type="number" placeholder="人数" min={0} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="编制上限" name="maxHeadcount">
                <Input type="number" placeholder="编制" min={1} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="状态" name="status">
                <Select
                  placeholder="选择状态"
                  options={[
                    { value: 1, label: '正常' },
                    { value: 2, label: '调整中' },
                    { value: 3, label: '已撤销' },
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item label="部门描述" name="description">
            <Input.TextArea rows={3} placeholder="输入部门职责描述" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 部门详情弹窗 */}
      <Modal
        title={`部门详情 - ${selected?.name}`}
        open={detailVisible}
        onCancel={() => { setDetailVisible(false); setSelected(null); }}
        footer={[
          <Button key="edit" icon={<EditOutlined />} onClick={() => { setDetailVisible(false); setModalVisible(true); }}>编辑</Button>,
          <Button key="close" onClick={() => { setDetailVisible(false); setSelected(null); }}>关闭</Button>,
        ]}
        width={700}
      >
        {selected && (
          <Descriptions bordered size="small" column={2}>
            <Descriptions.Item label="部门编码">{selected.code}</Descriptions.Item>
            <Descriptions.Item label="部门名称">{selected.name}</Descriptions.Item>
            <Descriptions.Item label="部门负责人">
              <Space><Avatar size="small" icon={<UserOutlined />} />{selected.manager}</Space>
            </Descriptions.Item>
            <Descriptions.Item label="状态">
              <Tag color={statusConfig[selected.status]?.color}>{statusConfig[selected.status]?.text}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="人员编制">
              <Progress
                percent={Math.round((selected.headcount / selected.maxHeadcount) * 100)}
                format={() => `${selected.headcount} / ${selected.maxHeadcount}`}
              />
            </Descriptions.Item>
            <Descriptions.Item label="层级">
              <Tag color={selected.level === 1 ? 'blue' : 'green'}>L{selected.level}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="上级部门">
              {selected.parentId ? departments.find(d => d.id === selected.parentId)?.name : '无'}
            </Descriptions.Item>
            <Descriptions.Item label="创建日期">{selected.createdAt}</Descriptions.Item>
            <Descriptions.Item label="部门描述" span={2}>{selected.description}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
}
