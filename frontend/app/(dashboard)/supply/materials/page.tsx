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
  Tabs,
  Descriptions,
  InputNumber,
  DatePicker,
  Divider,
  Tooltip,
  message,
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  EyeOutlined,
  ShopOutlined,
  ShoppingOutlined,
  AppstoreOutlined,
  TagOutlined,
  DatabaseOutlined,
} from '@ant-design/icons';
import { materialApi } from '@/lib/api/supply';
import type { MaterialDTO } from '@/lib/api/supply';

// ============================================================
// Types
// ============================================================
interface Material extends MaterialDTO {}

// ============================================================
// Lookup maps
// ============================================================
const materialTypeMap: Record<string, { label: string; color: string }> = {
  ROH: { label: 'ROH 原材料', color: 'blue' },
  HALB: { label: 'HALB 半成品', color: 'orange' },
  FERT: { label: 'FERT 成品', color: 'green' },
  VERP: { label: 'VERP 包装', color: 'purple' },
  DIEN: { label: 'DIEN 服务', color: 'cyan' },
  NLAG: { label: 'NLAG 非库存', color: 'default' },
};

const mrpTypeMap: Record<string, string> = {
  PD: 'MRP 按需计划',
  VB: '自动重订货点',
  ND: '无计划',
};

const procurementTypeMap: Record<string, string> = {
  E: '自制',
  F: '外购',
};

// Default tenant ID (should be from user context in production)
const DEFAULT_TENANT_ID = 1;

// ============================================================
// Component
// ============================================================
export default function MaterialsPage() {
  const [loading, setLoading] = useState(false);
  const [materials, setMaterials] = useState<Material[]>([]);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [activeTab, setActiveTab] = useState('all');
  const [createModalVisible, setCreateModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedMaterial, setSelectedMaterial] = useState<Material | null>(null);
  const [detailTab, setDetailTab] = useState('basic');
  const [extendPlantModalVisible, setExtendPlantModalVisible] = useState(false);
  const [extendSalesModalVisible, setExtendSalesModalVisible] = useState(false);
  const [createForm] = Form.useForm();
  const [searchForm] = Form.useForm();
  const [plantForm] = Form.useForm();
  const [salesForm] = Form.useForm();

  // ---------- Fetch data ----------
  const fetchMaterials = useCallback(async (page = currentPage, size = pageSize, materialType?: string) => {
    try {
      setLoading(true);
      const res = await materialApi.getPage({
        tenantId: DEFAULT_TENANT_ID,
        materialType: materialType || undefined,
        current: page,
        size,
      });
      if (res.success && res.data) {
        setMaterials(res.data.records as Material[]);
        setTotal(res.data.total);
      }
    } catch (error) {
      console.error('Failed to fetch materials:', error);
      message.error('获取物料列表失败');
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize]);

  useEffect(() => {
    fetchMaterials();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // ---------- Filtering ----------
  const filteredData =
    activeTab === 'all'
      ? materials
      : materials.filter((m) => m.materialType === activeTab);

  // ---------- Stats ----------
  const stats = {
    total: materials.length,
    fertCount: materials.filter((m) => m.materialType === 'FERT').length,
    rohCount: materials.filter((m) => m.materialType === 'ROH').length,
    activeCount: materials.filter((m) => m.crossPlantStatus === '活跃').length,
  };

  // ---------- Handlers ----------
  const handleViewDetail = async (record: Material) => {
    try {
      const res = await materialApi.getById(record.id);
      if (res.success && res.data) {
        setSelectedMaterial(res.data as Material);
      } else {
        setSelectedMaterial(record);
      }
    } catch {
      setSelectedMaterial(record);
    }
    setDetailTab('basic');
    setDetailModalVisible(true);
  };

  const handleCreate = async () => {
    try {
      const values = await createForm.validateFields();
      const data = {
        ...values,
        tenantId: DEFAULT_TENANT_ID,
        validFrom: values.validFrom?.format('YYYY-MM-DD'),
        validTo: values.validTo?.format('YYYY-MM-DD'),
      };
      const res = await materialApi.create(data);
      if (res.success) {
        message.success('物料主数据创建成功');
        setCreateModalVisible(false);
        createForm.resetFields();
        fetchMaterials();
      } else {
        message.error(res.message || '创建失败');
      }
    } catch (error: unknown) {
      if (error && typeof error === 'object' && 'errorFields' in error) {
        return; // form validation error
      }
      console.error('Failed to create material:', error);
      message.error('创建物料失败');
    }
  };

  const handleExtendPlant = () => {
    message.success('工厂扩展成功');
    setExtendPlantModalVisible(false);
    plantForm.resetFields();
  };

  const handleExtendSales = () => {
    message.success('销售视图扩展成功');
    setExtendSalesModalVisible(false);
    salesForm.resetFields();
  };

  const handleDelete = (record: Material) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除物料 ${record.materialNumber}（${record.description}）吗？`,
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          const res = await materialApi.delete(record.id);
          if (res.success) {
            message.success(`物料 ${record.materialNumber} 已标记删除 (MM06)`);
            fetchMaterials();
          } else {
            message.error(res.message || '删除失败');
          }
        } catch (error) {
          console.error('Failed to delete material:', error);
          message.error('删除物料失败');
        }
      },
    });
  };

  const handleSearch = async () => {
    const values = searchForm.getFieldsValue();
    const keyword = values.materialNumber || values.description;
    if (keyword) {
      try {
        setLoading(true);
        const res = await materialApi.search({
          keyword,
          tenantId: DEFAULT_TENANT_ID,
          current: 1,
          size: pageSize,
        });
        if (res.success && res.data) {
          setMaterials(res.data.records as Material[]);
          setTotal(res.data.total);
          setCurrentPage(1);
        }
      } catch (error) {
        console.error('Failed to search materials:', error);
        message.error('搜索物料失败');
      } finally {
        setLoading(false);
      }
    } else {
      fetchMaterials(1);
    }
  };

  const handleRefresh = () => {
    searchForm.resetFields();
    setActiveTab('all');
    setCurrentPage(1);
    fetchMaterials(1);
  };

  const handleTabChange = (key: string) => {
    setActiveTab(key);
    setCurrentPage(1);
    fetchMaterials(1, pageSize, key === 'all' ? undefined : key);
  };

  // ---------- Table columns ----------
  const columns = [
    {
      title: '物料编码',
      dataIndex: 'materialNumber',
      key: 'materialNumber',
      width: 120,
      fixed: 'left' as const,
      render: (text: string, record: Material) => (
        <a onClick={() => handleViewDetail(record)}>{text}</a>
      ),
    },
    {
      title: '物料类型',
      dataIndex: 'materialType',
      key: 'materialType',
      width: 130,
      render: (type: string) => {
        const cfg = materialTypeMap[type];
        return cfg ? <Tag color={cfg.color}>{cfg.label}</Tag> : type;
      },
    },
    {
      title: '物料组',
      dataIndex: 'materialGroup',
      key: 'materialGroup',
      width: 110,
    },
    {
      title: '物料描述',
      dataIndex: 'description',
      key: 'description',
      width: 220,
      ellipsis: true,
    },
    {
      title: '基本单位',
      dataIndex: 'baseUom',
      key: 'baseUom',
      width: 90,
      align: 'center' as const,
    },
    {
      title: '跨工厂状态',
      dataIndex: 'crossPlantStatus',
      key: 'crossPlantStatus',
      width: 110,
      render: (status: string) => (
        <Tag color={status === '活跃' ? 'green' : 'volcano'}>{status}</Tag>
      ),
    },
    {
      title: '行业领域',
      dataIndex: 'industrySector',
      key: 'industrySector',
      width: 100,
    },
    {
      title: '有效期',
      key: 'validity',
      width: 200,
      render: (_: unknown, record: Material) =>
        `${record.validFrom} ~ ${record.validTo}`,
    },
    {
      title: '操作',
      key: 'action',
      width: 260,
      fixed: 'right' as const,
      render: (_: unknown, record: Material) => (
        <Space size="small">
          <Tooltip title="查看详情 (MM03)">
            <Button
              type="link"
              size="small"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetail(record)}
            >
              详情
            </Button>
          </Tooltip>
          <Tooltip title="编辑物料 (MM02)">
            <Button type="link" size="small" icon={<EditOutlined />}>
              编辑
            </Button>
          </Tooltip>
          <Tooltip title="扩展到工厂">
            <Button
              type="link"
              size="small"
              icon={<ShopOutlined />}
              onClick={() => setExtendPlantModalVisible(true)}
            >
              工厂
            </Button>
          </Tooltip>
          <Tooltip title="扩展销售视图">
            <Button
              type="link"
              size="small"
              icon={<ShoppingOutlined />}
              onClick={() => setExtendSalesModalVisible(true)}
            >
              销售
            </Button>
          </Tooltip>
          <Tooltip title="删除物料 (MM06)">
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
              onClick={() => handleDelete(record)}
            >
              删除
            </Button>
          </Tooltip>
        </Space>
      ),
    },
  ];

  // ---------- Detail modal content ----------
  const renderDetailContent = () => {
    if (!selectedMaterial) return null;

    switch (detailTab) {
      case 'basic':
        return (
          <Descriptions bordered size="small" column={2}>
            <Descriptions.Item label="物料编码">
              {selectedMaterial.materialNumber}
            </Descriptions.Item>
            <Descriptions.Item label="物料类型">
              <Tag color={materialTypeMap[selectedMaterial.materialType]?.color}>
                {materialTypeMap[selectedMaterial.materialType]?.label}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="物料组">
              {selectedMaterial.materialGroup}
            </Descriptions.Item>
            <Descriptions.Item label="物料描述">
              {selectedMaterial.description}
            </Descriptions.Item>
            <Descriptions.Item label="基本计量单位">
              {selectedMaterial.baseUom}
            </Descriptions.Item>
            <Descriptions.Item label="跨工厂状态">
              <Tag
                color={
                  selectedMaterial.crossPlantStatus === '活跃' ? 'green' : 'volcano'
                }
              >
                {selectedMaterial.crossPlantStatus}
              </Tag>
            </Descriptions.Item>
            <Descriptions.Item label="行业领域">
              {selectedMaterial.industrySector}
            </Descriptions.Item>
            <Descriptions.Item label="有效期从">
              {selectedMaterial.validFrom}
            </Descriptions.Item>
            <Descriptions.Item label="有效期至">
              {selectedMaterial.validTo}
            </Descriptions.Item>
          </Descriptions>
        );

      case 'plant':
        return (
          <Descriptions bordered size="small" column={2}>
            <Descriptions.Item label="工厂">
              {selectedMaterial.plant || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="MRP 类型">
              {selectedMaterial.mrpType
                ? mrpTypeMap[selectedMaterial.mrpType] || selectedMaterial.mrpType
                : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="采购类型">
              {selectedMaterial.procurementType
                ? procurementTypeMap[selectedMaterial.procurementType] ||
                  selectedMaterial.procurementType
                : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="批量大小">
              {selectedMaterial.lotSize || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="重订货点">
              {selectedMaterial.reorderPoint ?? '-'}
            </Descriptions.Item>
          </Descriptions>
        );

      case 'sales':
        return (
          <Descriptions bordered size="small" column={2}>
            <Descriptions.Item label="销售组织">
              {selectedMaterial.salesOrg || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="分销渠道">
              {selectedMaterial.distrChannel || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="产品组">
              {selectedMaterial.division || '-'}
            </Descriptions.Item>
          </Descriptions>
        );

      case 'valuation':
        return (
          <Descriptions bordered size="small" column={2}>
            <Descriptions.Item label="评估类">
              {selectedMaterial.valuationClass || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="价格单位">
              {selectedMaterial.priceUnit ?? '-'}
            </Descriptions.Item>
            <Descriptions.Item label="标准价格">
              {selectedMaterial.standardPrice != null
                ? `¥${selectedMaterial.standardPrice.toFixed(2)}`
                : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="移动平均价">
              {selectedMaterial.movingPrice != null ? (
                <span style={{ color: '#1890ff', fontWeight: 'bold' }}>
                  ¥{selectedMaterial.movingPrice.toFixed(2)}
                </span>
              ) : (
                '-'
              )}
            </Descriptions.Item>
          </Descriptions>
        );

      default:
        return null;
    }
  };

  // ---------- Render ----------
  return (
    <div style={{ padding: 24 }}>
      <Card
        title={
          <Space>
            <DatabaseOutlined />
            <span>物料主数据管理 (对标 SAP MM01/MM02/MM03)</span>
          </Space>
        }
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={handleRefresh}>
              刷新
            </Button>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setCreateModalVisible(true)}
            >
              创建物料 (MM01)
            </Button>
          </Space>
        }
      >
        {/* Statistics */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card size="small">
              <Statistic
                title="物料总数"
                value={stats.total}
                suffix="项"
                prefix={<AppstoreOutlined />}
                valueStyle={{ fontSize: 18 }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic
                title="成品 (FERT)"
                value={stats.fertCount}
                suffix="项"
                valueStyle={{ fontSize: 18, color: '#52c41a' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic
                title="原材料 (ROH)"
                value={stats.rohCount}
                suffix="项"
                valueStyle={{ fontSize: 18, color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic
                title="活跃状态"
                value={stats.activeCount}
                suffix="项"
                valueStyle={{ fontSize: 18, color: '#52c41a' }}
              />
            </Card>
          </Col>
        </Row>

        {/* Tabs - filter by material type */}
        <Tabs
          activeKey={activeTab}
          onChange={handleTabChange}
          items={[
            { key: 'all', label: '全部' },
            { key: 'FERT', label: 'FERT 成品' },
            { key: 'ROH', label: 'ROH 原材料' },
            { key: 'HALB', label: 'HALB 半成品' },
            { key: 'VERP', label: 'VERP 包装' },
            { key: 'DIEN', label: 'DIEN 服务' },
          ]}
        />

        {/* Search / Filter Form */}
        <Form form={searchForm} layout="inline" style={{ marginBottom: 16 }}>
          <Form.Item name="materialNumber" label="物料编码">
            <Input placeholder="MAT-xxx" style={{ width: 130 }} />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input placeholder="物料描述" style={{ width: 160 }} />
          </Form.Item>
          <Form.Item name="materialGroup" label="物料组">
            <Select
              placeholder="全部"
              allowClear
              style={{ width: 130 }}
              options={[
                { value: '01-原材料', label: '01-原材料' },
                { value: '02-半成品', label: '02-半成品' },
                { value: '03-成品', label: '03-成品' },
                { value: '04-包装', label: '04-包装' },
                { value: '05-服务', label: '05-服务' },
                { value: '06-非库存', label: '06-非库存' },
              ]}
            />
          </Form.Item>
          <Form.Item name="crossPlantStatus" label="状态">
            <Select
              placeholder="全部"
              allowClear
              style={{ width: 100 }}
              options={[
                { value: '活跃', label: '活跃' },
                { value: '受限', label: '受限' },
              ]}
            />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
              查询
            </Button>
          </Form.Item>
        </Form>

        {/* Data Table */}
        <Table
          columns={columns}
          dataSource={filteredData}
          rowKey="id"
          loading={loading}
          size="small"
          scroll={{ x: 1400 }}
          pagination={{
            current: currentPage,
            pageSize,
            total,
            showSizeChanger: true,
            showTotal: (t) => `共 ${t} 条`,
            onChange: (page, size) => {
              setCurrentPage(page);
              setPageSize(size);
              fetchMaterials(page, size, activeTab === 'all' ? undefined : activeTab);
            },
          }}
        />
      </Card>

      {/* ========== Create Material Modal (MM01) ========== */}
      <Modal
        title="创建物料主数据 (MM01)"
        open={createModalVisible}
        onCancel={() => {
          setCreateModalVisible(false);
          createForm.resetFields();
        }}
        onOk={handleCreate}
        width={820}
        okText="创建"
        destroyOnClose
      >
        <Form form={createForm} layout="vertical">
          {/* --- Basic Data --- */}
          <Divider orientation="left">基本数据</Divider>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                label="物料类型"
                name="materialType"
                rules={[{ required: true, message: '请选择物料类型' }]}
              >
                <Select
                  placeholder="选择物料类型"
                  options={Object.entries(materialTypeMap).map(([k, v]) => ({
                    value: k,
                    label: v.label,
                  }))}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                label="行业领域"
                name="industrySector"
                rules={[{ required: true, message: '请选择行业领域' }]}
              >
                <Select
                  placeholder="选择行业领域"
                  options={[
                    { value: '机械工程', label: '机械工程' },
                    { value: '电子', label: '电子' },
                    { value: '化工', label: '化工' },
                    { value: '服务', label: '服务' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                label="物料组"
                name="materialGroup"
                rules={[{ required: true, message: '请选择物料组' }]}
              >
                <Select
                  placeholder="选择物料组"
                  options={[
                    { value: '01-原材料', label: '01-原材料' },
                    { value: '02-半成品', label: '02-半成品' },
                    { value: '03-成品', label: '03-成品' },
                    { value: '04-包装', label: '04-包装' },
                    { value: '05-服务', label: '05-服务' },
                    { value: '06-非库存', label: '06-非库存' },
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={16}>
              <Form.Item
                label="物料描述"
                name="description"
                rules={[{ required: true, message: '请输入物料描述' }]}
              >
                <Input placeholder="物料描述 (中英文)" />
              </Form.Item>
            </Col>
            <Col span={4}>
              <Form.Item
                label="基本单位"
                name="baseUom"
                rules={[{ required: true, message: '请选择单位' }]}
              >
                <Select
                  placeholder="单位"
                  options={[
                    { value: 'KG', label: 'KG' },
                    { value: 'PCS', label: 'PCS' },
                    { value: 'M', label: 'M' },
                    { value: 'L', label: 'L' },
                    { value: 'SET', label: 'SET' },
                    { value: 'AU', label: 'AU' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col span={4}>
              <Form.Item label="跨工厂状态" name="crossPlantStatus">
                <Select
                  placeholder="状态"
                  defaultValue="活跃"
                  options={[
                    { value: '活跃', label: '活跃' },
                    { value: '受限', label: '受限' },
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="有效期从" name="validFrom">
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="有效期至" name="validTo">
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          {/* --- Plant Data --- */}
          <Divider orientation="left">工厂数据</Divider>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="工厂" name="plant">
                <Select
                  placeholder="选择工厂"
                  options={[
                    { value: '1000', label: '1000 北京工厂' },
                    { value: '1100', label: '1100 上海工厂' },
                    { value: '1200', label: '1200 广州工厂' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="MRP 类型" name="mrpType">
                <Select
                  placeholder="选择MRP类型"
                  options={Object.entries(mrpTypeMap).map(([k, v]) => ({
                    value: k,
                    label: v,
                  }))}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="采购类型" name="procurementType">
                <Select
                  placeholder="选择采购类型"
                  options={Object.entries(procurementTypeMap).map(([k, v]) => ({
                    value: k,
                    label: `${v} (${k})`,
                  }))}
                />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="批量大小" name="lotSize">
                <Select
                  placeholder="选择批量大小"
                  options={[
                    { value: 'EX', label: 'EX 精确批量' },
                    { value: 'FX', label: 'FX 固定批量' },
                    { value: 'WB', label: 'WB 按周' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="重订货点" name="reorderPoint">
                <InputNumber
                  style={{ width: '100%' }}
                  min={0}
                  placeholder="安全库存水平"
                />
              </Form.Item>
            </Col>
          </Row>

          {/* --- Sales Data --- */}
          <Divider orientation="left">销售数据</Divider>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item label="销售组织" name="salesOrg">
                <Select
                  placeholder="选择销售组织"
                  options={[
                    { value: '1000', label: '1000 华北销售部' },
                    { value: '2000', label: '2000 华东销售部' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="分销渠道" name="distrChannel">
                <Select
                  placeholder="选择分销渠道"
                  options={[
                    { value: '10', label: '10 直销' },
                    { value: '20', label: '20 经销' },
                    { value: '30', label: '30 电商' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="产品组" name="division">
                <Select
                  placeholder="选择产品组"
                  options={[
                    { value: '00', label: '00 全部产品' },
                    { value: '01', label: '01 工业产品' },
                    { value: '02', label: '02 消费产品' },
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>

      {/* ========== Detail Modal (MM03) ========== */}
      <Modal
        title={
          <Space>
            <TagOutlined />
            <span>物料详情 (MM03) - {selectedMaterial?.materialNumber}</span>
          </Space>
        }
        open={detailModalVisible}
        onCancel={() => {
          setDetailModalVisible(false);
          setSelectedMaterial(null);
        }}
        footer={[
          <Button
            key="close"
            onClick={() => {
              setDetailModalVisible(false);
              setSelectedMaterial(null);
            }}
          >
            关闭
          </Button>,
        ]}
        width={900}
      >
        {selectedMaterial && (
          <>
            <Descriptions
              bordered
              size="small"
              column={3}
              style={{ marginBottom: 16 }}
            >
              <Descriptions.Item label="物料编码">
                <strong>{selectedMaterial.materialNumber}</strong>
              </Descriptions.Item>
              <Descriptions.Item label="物料描述" span={2}>
                {selectedMaterial.description}
              </Descriptions.Item>
              <Descriptions.Item label="物料类型">
                <Tag color={materialTypeMap[selectedMaterial.materialType]?.color}>
                  {materialTypeMap[selectedMaterial.materialType]?.label}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="物料组">
                {selectedMaterial.materialGroup}
              </Descriptions.Item>
              <Descriptions.Item label="跨工厂状态">
                <Tag
                  color={
                    selectedMaterial.crossPlantStatus === '活跃' ? 'green' : 'volcano'
                  }
                >
                  {selectedMaterial.crossPlantStatus}
                </Tag>
              </Descriptions.Item>
            </Descriptions>

            <Tabs
              activeKey={detailTab}
              onChange={setDetailTab}
              items={[
                { key: 'basic', label: '基本数据' },
                { key: 'plant', label: '工厂数据' },
                { key: 'sales', label: '销售数据' },
                { key: 'valuation', label: '评估数据' },
              ]}
            />

            {renderDetailContent()}
          </>
        )}
      </Modal>

      {/* ========== Extend to Plant Modal ========== */}
      <Modal
        title="扩展物料到工厂"
        open={extendPlantModalVisible}
        onCancel={() => {
          setExtendPlantModalVisible(false);
          plantForm.resetFields();
        }}
        onOk={handleExtendPlant}
        width={600}
        okText="扩展"
        destroyOnClose
      >
        <Form form={plantForm} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                label="目标工厂"
                name="plant"
                rules={[{ required: true, message: '请选择工厂' }]}
              >
                <Select
                  placeholder="选择工厂"
                  options={[
                    { value: '1000', label: '1000 北京工厂' },
                    { value: '1100', label: '1100 上海工厂' },
                    { value: '1200', label: '1200 广州工厂' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                label="MRP 类型"
                name="mrpType"
                rules={[{ required: true, message: '请选择MRP类型' }]}
              >
                <Select
                  placeholder="选择MRP类型"
                  options={Object.entries(mrpTypeMap).map(([k, v]) => ({
                    value: k,
                    label: v,
                  }))}
                />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="采购类型" name="procurementType">
                <Select
                  placeholder="选择采购类型"
                  options={Object.entries(procurementTypeMap).map(([k, v]) => ({
                    value: k,
                    label: `${v} (${k})`,
                  }))}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="重订货点" name="reorderPoint">
                <InputNumber
                  style={{ width: '100%' }}
                  min={0}
                  placeholder="安全库存水平"
                />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>

      {/* ========== Extend to Sales Modal ========== */}
      <Modal
        title="扩展物料销售视图"
        open={extendSalesModalVisible}
        onCancel={() => {
          setExtendSalesModalVisible(false);
          salesForm.resetFields();
        }}
        onOk={handleExtendSales}
        width={600}
        okText="扩展"
        destroyOnClose
      >
        <Form form={salesForm} layout="vertical">
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                label="销售组织"
                name="salesOrg"
                rules={[{ required: true, message: '请选择销售组织' }]}
              >
                <Select
                  placeholder="选择"
                  options={[
                    { value: '1000', label: '1000 华北销售部' },
                    { value: '2000', label: '2000 华东销售部' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                label="分销渠道"
                name="distrChannel"
                rules={[{ required: true, message: '请选择分销渠道' }]}
              >
                <Select
                  placeholder="选择"
                  options={[
                    { value: '10', label: '10 直销' },
                    { value: '20', label: '20 经销' },
                    { value: '30', label: '30 电商' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item label="产品组" name="division">
                <Select
                  placeholder="选择"
                  options={[
                    { value: '00', label: '00 全部产品' },
                    { value: '01', label: '01 工业产品' },
                    { value: '02', label: '02 消费产品' },
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  );
}
