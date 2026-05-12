'use client';

import { useState, useEffect, useCallback } from 'react';
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
import { bomApi } from '@/lib/api/production';
import type { ProBomDTO } from '@/lib/api/production';

// Default tenant ID
const DEFAULT_TENANT_ID = 1;

// BOM组件明细 (kept locally for detail view since backend may embed in DTO)
interface BomComponent {
  id: number;
  bomId: number;
  seq: number;
  materialCode: string;
  materialName: string;
  quantity: number;
  unit: string;
  scrapRate: number;
  effectiveFrom: string;
  effectiveTo: string | null;
  status: number;
}

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
  const [boms, setBOMs] = useState<ProBomDTO[]>([]);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [activeTab, setActiveTab] = useState('list');
  const [bomModalVisible, setBOMModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [componentModalVisible, setComponentModalVisible] = useState(false);
  const [selectedBOM, setSelectedBOM] = useState<ProBomDTO | null>(null);
  const [form] = Form.useForm();

  // 状态配置
  const statusConfig: Record<number, { color: string; text: string }> = {
    0: { color: 'orange', text: '待生效' },
    1: { color: 'green', text: '生效中' },
    2: { color: 'default', text: '已失效' },
  };

  const fetchBOMs = useCallback(async (page = currentPage, size = pageSize, status?: number) => {
    try {
      setLoading(true);
      const res = await bomApi.getPage({
        tenantId: DEFAULT_TENANT_ID,
        status,
        current: page,
        size,
      });
      if (res.success && res.data) {
        setBOMs(res.data.records);
        setTotal(res.data.total);
      }
    } catch (error) {
      console.error('Failed to fetch BOMs:', error);
      message.error('获取BOM列表失败');
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize]);

  useEffect(() => {
    fetchBOMs();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // BOM列表列
  const columns = [
    { title: 'BOM编码', dataIndex: 'bomCode', key: 'bomCode', width: 120, fixed: 'left' as const,
      render: (text: string, record: any) => (
        <a onClick={() => { setSelectedBOM(record); setDetailModalVisible(true); }}>{text}</a>
      ),
    },
    { title: '产品编码', dataIndex: 'productCode', key: 'productCode', width: 100 },
    { title: '产品名称', dataIndex: 'productName', key: 'productName', width: 100 },
    { title: '版本', dataIndex: 'version', key: 'version', width: 80,
      render: (version: string) => <Tag color="blue">{version}</Tag>,
    },
    { title: '基准数量', dataIndex: 'baseQty', key: 'baseQty', width: 100,
      render: (v: number, r: any) => `${v} ${r.unit}`,
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
      render: (_: unknown, record: any) => (
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

  const handleCreateBOM = async () => {
    try {
      const values = await form.validateFields();
      const res = await bomApi.create({
        ...values,
        tenantId: DEFAULT_TENANT_ID,
      });
      if (res.success) {
        message.success('BOM创建成功');
        setBOMModalVisible(false);
        form.resetFields();
        fetchBOMs();
      } else {
        message.error(res.message || '创建失败');
      }
    } catch (error: unknown) {
      if (error && typeof error === 'object' && 'errorFields' in error) {
        return;
      }
      console.error('Failed to create BOM:', error);
      message.error('创建BOM失败');
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="BOM 管理 (对标 SAP CS01/CS02/CS03)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => fetchBOMs()}>刷新</Button>
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
                <Button type="primary" icon={<SearchOutlined />} onClick={() => fetchBOMs(1)}>查询</Button>
              </Form.Item>
            </Form>

            <Table
              columns={columns}
              dataSource={boms}
              rowKey="id"
              loading={loading}
              size="small"
              scroll={{ x: 1400 }}
              pagination={{
                current: currentPage,
                pageSize,
                total,
                showSizeChanger: true,
                onChange: (page, size) => {
                  setCurrentPage(page);
                  setPageSize(size);
                  fetchBOMs(page, size);
                },
              }}
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
        onOk={handleCreateBOM}
        width={700}
      >
        <Form form={form} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="BOM编码" name="bomCode" rules={[{ required: true, message: '请输入BOM编码' }]}>
                <Input placeholder="输入BOM编码" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="版本号" name="version" rules={[{ required: true, message: '请输入版本号' }]}>
                <Input placeholder="如: V1.0" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="产品编码" name="productCode" rules={[{ required: true, message: '请输入产品编码' }]}>
                <Input placeholder="选择产品" />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item label="基准数量" name="baseQty" rules={[{ required: true, message: '请输入数量' }]}>
                <InputNumber style={{ width: '100%' }} min={1} placeholder="数量" />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item label="单位" name="unit">
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
              <Form.Item label="生效日期" name="effectiveFrom">
                <Input type="date" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="失效日期" name="effectiveTo">
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
                dataSource={[]}
                rowKey="id"
                size="small"
                pagination={false}
                locale={{ emptyText: '暂无组件数据' }}
              />
            </Card>
          </>
        )}
      </Modal>
    </div>
  );
}
