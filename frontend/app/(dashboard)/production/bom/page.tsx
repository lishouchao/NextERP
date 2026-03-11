'use client';

import { useState } from 'react';
import {
  Card,
  Table,
  Form,
  Input,
  Select,
  Button,
  Space,
  Row,
  Col,
  Statistic,
  Tag,
  Modal,
  InputNumber,
  Tree,
  Descriptions,
  message,
  Tooltip,
  Tabs,
  Dropdown,
} from 'antd';
import type { MenuProps } from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  ApartmentOutlined,
  CopyOutlined,
  FileTextOutlined,
  DownloadOutlined,
} from '@ant-design/icons';

// 模拟BOM数据
const mockBOMs = [
  { id: 1, bomCode: 'BOM-001', productCode: 'PROD-001', productName: '产品A', version: 'V1.0', status: 1, baseQty: 1, unit: 'PCS', effectiveFrom: '2023-01-01', effectiveTo: null, components: 5, createdBy: '工程师A', createdAt: '2023-01-15', updatedAt: '2023-06-20' },
  { id: 2, bomCode: 'BOM-002', productCode: 'PROD-002', productName: '产品B', version: 'V1.0', status: 1, baseQty: 1, unit: 'PCS', effectiveFrom: '2023-02-01', effectiveTo: null, components: 4, createdBy: '工程师A', createdAt: '2023-02-10', updatedAt: '2023-05-15' },
  { id: 3, bomCode: 'BOM-003', productCode: 'PROD-003', productName: '配件C', version: 'V1.0', status: 1, baseQty: 10, unit: 'SET', effectiveFrom: '2023-03-01', effectiveTo: null, components: 3, createdBy: '工程师B', createdAt: '2023-03-05', updatedAt: '2023-03-05' },
  { id: 4, bomCode: 'BOM-004', productCode: 'PROD-004', productName: '产品D', version: 'V2.0', status: 1, baseQty: 1, unit: 'PCS', effectiveFrom: '2023-06-01', effectiveTo: null, components: 8, createdBy: '工程师B', createdAt: '2023-04-20', updatedAt: '2023-08-10' },
  { id: 5, bomCode: 'BOM-001-V2', productCode: 'PROD-001', productName: '产品A', version: 'V2.0', status: 0, baseQty: 1, unit: 'PCS', effectiveFrom: '2024-01-01', effectiveTo: null, components: 6, createdBy: '工程师A', createdAt: '2023-11-01', updatedAt: '2023-11-01' },
  { id: 6, bomCode: 'BOM-005', productCode: 'PROD-005', productName: '产品E', version: 'V1.0', status: 2, baseQty: 1, unit: 'PCS', effectiveFrom: '2023-04-01', effectiveTo: '2023-09-30', components: 4, createdBy: '工程师A', createdAt: '2023-03-20', updatedAt: '2023-03-20' },
];

// BOM组件明细
const mockComponents = [
  { id: 1, bomId: 1, seq: 10, materialCode: 'MAT-001', materialName: '原材料A', quantity: 2, unit: 'KG', scrapRate: 5, effectiveFrom: '2023-01-01', effectiveTo: null, status: 1 },
  { id: 2, bomId: 1, seq: 20, materialCode: 'MAT-002', materialName: '原材料B', quantity: 1.5, unit: 'KG', scrapRate: 3, effectiveFrom: '2023-01-01', effectiveTo: null, status: 1 },
  { id: 3, bomId: 1, seq: 30, materialCode: 'MAT-003', materialName: '包装材料', quantity: 1, unit: 'PCS', scrapRate: 0, effectiveFrom: '2023-01-01', effectiveTo: null, status: 1 },
  { id: 4, bomId: 1, seq: 40, materialCode: 'MAT-006', materialName: '辅助材料E', quantity: 0.5, unit: 'L', scrapRate: 10, effectiveFrom: '2023-01-01', effectiveTo: null, status: 1 },
  { id: 5, bomId: 1, seq: 50, materialCode: 'COMP-001', materialName: '零件甲', quantity: 4, unit: 'PCS', scrapRate: 2, effectiveFrom: '2023-01-01', effectiveTo: null, status: 1 },
  { id: 6, bomId: 2, seq: 10, materialCode: 'MAT-001', materialName: '原材料A', quantity: 3, unit: 'KG', scrapRate: 5, effectiveFrom: '2023-02-01', effectiveTo: null, status: 1 },
  { id: 7, bomId: 2, seq: 20, materialCode: 'MAT-004', materialName: '半成品C', quantity: 2, unit: 'PCS', scrapRate: 0, effectiveFrom: '2023-02-01', effectiveTo: null, status: 1 },
  { id: 8, bomId: 2, seq: 30, materialCode: 'MAT-003', materialName: '包装材料', quantity: 1, unit: 'PCS', scrapRate: 0, effectiveFrom: '2023-02-01', effectiveTo: null, status: 1 },
  { id: 9, bomId: 2, seq: 40, materialCode: 'COMP-002', materialName: '零件乙', quantity: 2, unit: 'PCS', scrapRate: 1, effectiveFrom: '2023-02-01', effectiveTo: null, status: 1 },
];

// BOM树形结构数据
const bomTreeData = [
  {
    title: '产品A (PROD-001)',
    key: 'PROD-001',
    children: [
      {
        title: '原材料A (MAT-001) - 2 KG',
        key: 'MAT-001',
      },
      {
        title: '原材料B (MAT-002) - 1.5 KG',
        key: 'MAT-002',
      },
      {
        title: '包装材料 (MAT-003) - 1 PCS',
        key: 'MAT-003',
      },
      {
        title: '辅助材料E (MAT-006) - 0.5 L',
        key: 'MAT-006',
      },
      {
        title: '零件甲 (COMP-001) - 4 PCS',
        key: 'COMP-001',
      },
    ],
  },
];

export default function BOMPage() {
  const [loading, setLoading] = useState(false);
  const [boms, setBOMs] = useState(mockBOMs);
  const [activeTab, setActiveTab] = useState('list');
  const [bomModalVisible, setBOMModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [componentModalVisible, setComponentModalVisible] = useState(false);
  const [selectedBOM, setSelectedBOM] = useState<typeof mockBOMs[0] | null>(null);
  const [form] = Form.useForm();

  // 状态配置
  const statusConfig: Record<number, { color: string; text: string }> = {
    0: { color: 'orange', text: '待生效' },
    1: { color: 'green', text: '生效中' },
    2: { color: 'default', text: '已失效' },
  };

  // BOM列表列
  const columns = [
    { title: 'BOM编码', dataIndex: 'bomCode', key: 'bomCode', width: 120, fixed: 'left' as const,
      render: (text: string, record) => (
        <a onClick={() => { setSelectedBOM(record); setDetailModalVisible(true); }}>{text}</a>
      ),
    },
    { title: '产品编码', dataIndex: 'productCode', key: 'productCode', width: 100 },
    { title: '产品名称', dataIndex: 'productName', key: 'productName', width: 100 },
    { title: '版本', dataIndex: 'version', key: 'version', width: 80,
      render: (version: string) => <Tag color="blue">{version}</Tag>,
    },
    { title: '基准数量', dataIndex: 'baseQty', key: 'baseQty', width: 100,
      render: (v: number, r) => `${v} ${r.unit}`,
    },
    { title: '组件数', dataIndex: 'components', key: 'components', width: 80, align: 'center' as const,
      render: (v: number) => <Tag color="purple">{v}</Tag>,
    },
    { title: '生效日期', dataIndex: 'effectiveFrom', key: 'effectiveFrom', width: 100 },
    { title: '失效日期', dataIndex: 'effectiveTo', key: 'effectiveTo', width: 100,
      render: (v: string) => v || '-',
    },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80,
      render: (status: number) => {
        const config = statusConfig[status];
        return <Tag color={config?.color}>{config?.text}</Tag>;
      },
    },
    { title: '创建人', dataIndex: 'createdBy', key: 'createdBy', width: 90 },
    { title: '更新日期', dataIndex: 'updatedAt', key: 'updatedAt', width: 100 },
    { title: '操作', key: 'action', width: 200, fixed: 'right' as const,
      render: (_: unknown, record) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => { setSelectedBOM(record); setDetailModalVisible(true); }}>详情</Button>
          {record.status !== 2 && (
            <>
              <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
              <Button type="link" size="small" icon={<CopyOutlined />}>复制</Button>
            </>
          )}
          <Dropdown menu={{
            items: [
              { key: 'export', label: '导出BOM', icon: <DownloadOutlined /> },
              { key: 'where', label: '查找使用处', icon: <SearchOutlined /> },
            ],
          }}>
            <Button type="link" size="small">更多</Button>
          </Dropdown>
        </Space>
      ),
    },
  ];

  // 组件明细列
  const componentColumns = [
    { title: '项目号', dataIndex: 'seq', key: 'seq', width: 80 },
    { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode', width: 110 },
    { title: '物料名称', dataIndex: 'materialName', key: 'materialName', width: 120 },
    { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 80, align: 'right' as const },
    { title: '单位', dataIndex: 'unit', key: 'unit', width: 60 },
    { title: '损耗率(%)', dataIndex: 'scrapRate', key: 'scrapRate', width: 90, align: 'right' as const,
      render: (v: number) => v > 0 ? `${v}%` : '-',
    },
    { title: '生效日期', dataIndex: 'effectiveFrom', key: 'effectiveFrom', width: 100 },
    { title: '失效日期', dataIndex: 'effectiveTo', key: 'effectiveTo', width: 100,
      render: (v: string) => v || '-',
    },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80,
      render: (status: number) => <Tag color={status === 1 ? 'green' : 'default'}>{status === 1 ? '生效' : '失效'}</Tag>,
    },
  ];

  // 统计
  const stats = {
    totalBOMs: boms.length,
    activeBOMs: boms.filter(b => b.status === 1).length,
    pendingBOMs: boms.filter(b => b.status === 0).length,
    totalComponents: boms.reduce((s, b) => s + b.components, 0),
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="BOM 管理 (对标 SAP CS01/CS02/CS03)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => setLoading(!loading)}>刷新</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setBOMModalVisible(true)}>
              新建BOM
            </Button>
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            { key: 'list', label: <><FileTextOutlined /> BOM清单</> },
            { key: 'tree', label: <><ApartmentOutlined /> BOM结构</> },
          ]}
        />

        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card size="small">
              <Statistic title="BOM总数" value={stats.totalBOMs} suffix="个" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="生效中" value={stats.activeBOMs} suffix="个" valueStyle={{ fontSize: 18, color: '#52c41a' }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="待生效" value={stats.pendingBOMs} suffix="个" valueStyle={{ fontSize: 18, color: '#faad14' }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="组件总数" value={stats.totalComponents} suffix="项" valueStyle={{ fontSize: 18, color: '#1890ff' }} />
            </Card>
          </Col>
        </Row>

        {/* BOM清单 */}
        {activeTab === 'list' && (
          <>
            <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item name="bomCode" label="BOM编码">
                <Input placeholder="BOM编码" style={{ width: 120 }} />
              </Form.Item>
              <Form.Item name="productCode" label="产品编码">
                <Input placeholder="产品编码" style={{ width: 120 }} />
              </Form.Item>
              <Form.Item name="status" label="状态">
                <Select placeholder="全部" allowClear style={{ width: 100 }}
                  options={Object.entries(statusConfig).map(([k, v]) => ({ value: Number(k), label: v.text }))} />
              </Form.Item>
              <Form.Item>
                <Button type="primary" icon={<SearchOutlined />}>查询</Button>
              </Form.Item>
            </Form>

            <Table
              columns={columns}
              dataSource={boms}
              rowKey="id"
              loading={loading}
              size="small"
              scroll={{ x: 1400 }}
              pagination={{ defaultPageSize: 20, showSizeChanger: true }}
            />
          </>
        )}

        {/* BOM结构树 */}
        {activeTab === 'tree' && (
          <Row gutter={24}>
            <Col span={8}>
              <Card title="产品列表" size="small">
                <Tree
                  defaultExpandedKeys={['PROD-001']}
                  defaultSelectedKeys={['PROD-001']}
                  treeData={bomTreeData}
                  style={{ fontSize: 14 }}
                />
              </Card>
            </Col>
            <Col span={16}>
              <Card title="BOM详情" size="small">
                <Descriptions bordered size="small" column={2}>
                  <Descriptions.Item label="BOM编码">BOM-001</Descriptions.Item>
                  <Descriptions.Item label="版本">V1.0</Descriptions.Item>
                  <Descriptions.Item label="产品编码">PROD-001</Descriptions.Item>
                  <Descriptions.Item label="产品名称">产品A</Descriptions.Item>
                  <Descriptions.Item label="基准数量">1 PCS</Descriptions.Item>
                  <Descriptions.Item label="组件数">5</Descriptions.Item>
                  <Descriptions.Item label="生效日期">2023-01-01</Descriptions.Item>
                  <Descriptions.Item label="状态"><Tag color="green">生效中</Tag></Descriptions.Item>
                </Descriptions>
                <Table
                  columns={componentColumns}
                  dataSource={mockComponents.filter(c => c.bomId === 1)}
                  rowKey="id"
                  size="small"
                  style={{ marginTop: 16 }}
                  pagination={false}
                  title={() => (
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span>组件明细</span>
                      <Button type="primary" size="small" icon={<PlusOutlined />}>添加组件</Button>
                    </div>
                  )}
                />
              </Card>
            </Col>
          </Row>
        )}
      </Card>

      {/* 新建BOM弹窗 */}
      <Modal
        title="新建BOM"
        open={bomModalVisible}
        onCancel={() => setBOMModalVisible(false)}
        onOk={() => { message.success('BOM创建成功'); setBOMModalVisible(false); }}
        width={700}
      >
        <Form layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="BOM编码" required>
                <Input placeholder="输入BOM编码" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="版本号" required>
                <Input placeholder="如: V1.0" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="产品" required>
                <Input placeholder="选择产品" />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item label="基准数量" required>
                <InputNumber style={{ width: '100%' }} min={1} placeholder="数量" />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item label="单位">
                <Select placeholder="单位" options={[
                  { value: 'PCS', label: 'PCS' },
                  { value: 'KG', label: 'KG' },
                  { value: 'SET', label: 'SET' },
                ]} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="生效日期">
                <Input type="date" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="失效日期">
                <Input type="date" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item label="备注">
            <Input.TextArea rows={2} placeholder="输入备注" />
          </Form.Item>
        </Form>
      </Modal>

      {/* BOM详情弹窗 */}
      <Modal
        title={`BOM详情 - ${selectedBOM?.bomCode}`}
        open={detailModalVisible}
        onCancel={() => { setDetailModalVisible(false); setSelectedBOM(null); }}
        footer={[
          <Button key="export" icon={<DownloadOutlined />}>导出</Button>,
          selectedBOM?.status === 1 && <Button key="edit" type="primary" icon={<EditOutlined />}>编辑组件</Button>,
          <Button key="close" onClick={() => { setDetailModalVisible(false); setSelectedBOM(null); }}>关闭</Button>,
        ]}
        width={1000}
      >
        {selectedBOM && (
          <>
            <Descriptions bordered size="small" column={3} style={{ marginBottom: 16 }}>
              <Descriptions.Item label="BOM编码">{selectedBOM.bomCode}</Descriptions.Item>
              <Descriptions.Item label="版本"><Tag color="blue">{selectedBOM.version}</Tag></Descriptions.Item>
              <Descriptions.Item label="状态">
                <Tag color={statusConfig[selectedBOM.status]?.color}>{statusConfig[selectedBOM.status]?.text}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="产品编码">{selectedBOM.productCode}</Descriptions.Item>
              <Descriptions.Item label="产品名称">{selectedBOM.productName}</Descriptions.Item>
              <Descriptions.Item label="基准数量">{selectedBOM.baseQty} {selectedBOM.unit}</Descriptions.Item>
              <Descriptions.Item label="生效日期">{selectedBOM.effectiveFrom}</Descriptions.Item>
              <Descriptions.Item label="失效日期">{selectedBOM.effectiveTo || '-'}</Descriptions.Item>
              <Descriptions.Item label="组件数">{selectedBOM.components}</Descriptions.Item>
              <Descriptions.Item label="创建人">{selectedBOM.createdBy}</Descriptions.Item>
              <Descriptions.Item label="创建日期">{selectedBOM.createdAt}</Descriptions.Item>
              <Descriptions.Item label="更新日期">{selectedBOM.updatedAt}</Descriptions.Item>
            </Descriptions>

            <Card title="组件明细" size="small">
              <Table
                columns={componentColumns}
                dataSource={mockComponents.filter(c => c.bomId === selectedBOM.id)}
                rowKey="id"
                size="small"
                pagination={false}
              />
            </Card>
          </>
        )}
      </Modal>
    </div>
  );
}
