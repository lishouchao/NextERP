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
  DatePicker,
  Tabs,
  Tree,
  Progress,
  Descriptions,
  message,
} from 'antd';
import {
  SearchOutlined,
  PlusOutlined,
  ReloadOutlined,
  BarcodeOutlined,
} from '@ant-design/icons';
import { inventoryApi } from '@/lib/api/supply';
import type { StockDTO, MaterialDocDTO } from '@/lib/api/supply';

const { RangePicker } = DatePicker;

// Default tenant ID
const DEFAULT_TENANT_ID = 1;

export default function InventoryPage() {
  const [loading, setLoading] = useState(false);
  const [inventory, setInventory] = useState<StockDTO[]>([]);
  const [movements, setMovements] = useState<MaterialDocDTO[]>([]);
  const [stockTotal, setStockTotal] = useState(0);
  const [stockPage, setStockPage] = useState(1);
  const [stockPageSize, setStockPageSize] = useState(20);
  const [docTotal, setDocTotal] = useState(0);
  const [docPage, setDocPage] = useState(1);
  const [docPageSize, setDocPageSize] = useState(20);
  const [activeTab, setActiveTab] = useState('stock');
  const [movementModalVisible, setMovementModalVisible] = useState(false);
  const [form] = Form.useForm();

  // 仓库树形结构
  const warehouseTree = [
    {
      title: '所有仓库',
      key: 'all',
      children: [
        {
          title: 'WH-01 原材料仓',
          key: 'WH-01',
          children: [
            { title: 'A区 - 货架1', key: 'WH-01-A-1' },
            { title: 'A区 - 货架2', key: 'WH-01-A-2' },
          ],
        },
        {
          title: 'WH-02 辅材仓',
          key: 'WH-02',
          children: [
            { title: 'B区 - 货架1', key: 'WH-02-B-1' },
            { title: 'B区 - 货架2', key: 'WH-02-B-2' },
          ],
        },
        { title: 'WH-03 成品仓', key: 'WH-03' },
      ],
    },
  ];

  const fetchStock = useCallback(async (page = stockPage, size = stockPageSize) => {
    try {
      setLoading(true);
      const res = await inventoryApi.getStock({
        tenantId: DEFAULT_TENANT_ID,
        current: page,
        size,
      });
      if (res.success && res.data) {
        setInventory(res.data.records);
        setStockTotal(res.data.total);
      }
    } catch (error) {
      console.error('Failed to fetch stock:', error);
      message.error('获取库存数据失败');
    } finally {
      setLoading(false);
    }
  }, [stockPage, stockPageSize]);

  const fetchMovements = useCallback(async (page = docPage, size = docPageSize) => {
    try {
      setLoading(true);
      const res = await inventoryApi.getMaterialDocs({
        tenantId: DEFAULT_TENANT_ID,
        current: page,
        size,
      });
      if (res.success && res.data) {
        setMovements(res.data.records);
        setDocTotal(res.data.total);
      }
    } catch (error) {
      console.error('Failed to fetch material docs:', error);
      message.error('获取物料凭证失败');
    } finally {
      setLoading(false);
    }
  }, [docPage, docPageSize]);

  useEffect(() => {
    if (activeTab === 'stock') {
      fetchStock();
    } else if (activeTab === 'movement') {
      fetchMovements();
    }
  }, [activeTab]); // eslint-disable-line react-hooks/exhaustive-deps

  // 库存表格列
  const stockColumns = [
    { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode', width: 110, fixed: 'left' as const },
    { title: '物料名称', dataIndex: 'materialName', key: 'materialName', width: 120 },
    { title: '物料分类', dataIndex: 'category', key: 'category', width: 100,
      render: (category: string) => <Tag color="blue">{category}</Tag>,
    },
    { title: '仓库', dataIndex: 'warehouse', key: 'warehouse', width: 80 },
    { title: '库位', dataIndex: 'location', key: 'location', width: 80 },
    { title: '库存数量', dataIndex: 'quantity', key: 'quantity', width: 100, align: 'right' as const,
      render: (v: number, record: any) => `${v} ${record.unit}`,
    },
    { title: '库存状态', key: 'stockStatus', width: 120,
      render: (_: unknown, record: any) => {
        const percent = (record.quantity / record.maxStock) * 100;
        let status: 'success' | 'normal' | 'exception' = 'normal';
        if (record.quantity <= record.safetyStock) status = 'exception';
        else if (record.quantity >= record.maxStock * 0.8) status = 'success';
        return <Progress percent={Math.round(percent)} size="small" status={status} showInfo={false} />;
      },
    },
    { title: '单价', dataIndex: 'unitPrice', key: 'unitPrice', width: 100, align: 'right' as const,
      render: (v: number) => `¥${v.toFixed(2)}`,
    },
    { title: '库存金额', dataIndex: 'totalValue', key: 'totalValue', width: 120, align: 'right' as const,
      render: (v: number) => <span style={{ color: '#1890ff', fontWeight: 'bold' }}>¥{v.toLocaleString()}</span>,
    },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80,
      render: (status: number) => {
        const map: Record<number, { color: string; text: string }> = {
          1: { color: 'green', text: '正常' },
          2: { color: 'orange', text: '呆滞' },
          3: { color: 'red', text: '告警' },
        };
        const s = map[status] || { color: 'default', text: '未知' };
        return <Tag color={s.color}>{s.text}</Tag>;
      },
    },
  ];

  // 移动记录列
  const movementColumns = [
    { title: '移动单号', dataIndex: 'movementNo', key: 'movementNo', width: 140 },
    { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode', width: 110 },
    { title: '物料名称', dataIndex: 'materialName', key: 'materialName', width: 120 },
    { title: '移动类型', dataIndex: 'movementTypeName', key: 'movementTypeName', width: 80,
      render: (text: string, record: any) => (
        <Tag color={record.quantity > 0 ? 'green' : 'red'}>{text}</Tag>
      ),
    },
    { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 80, align: 'right' as const,
      render: (v: number) => <span style={{ color: v > 0 ? '#52c41a' : '#ff4d4f' }}>{v > 0 ? '+' : ''}{v}</span>,
    },
    { title: '仓库', dataIndex: 'warehouse', key: 'warehouse', width: 80 },
    { title: '参考单号', dataIndex: 'referenceNo', key: 'referenceNo', width: 120 },
    { title: '操作时间', dataIndex: 'createdAt', key: 'createdAt', width: 140 },
    { title: '操作人', dataIndex: 'createdBy', key: 'createdBy', width: 80 },
  ];

  // 统计
  const totalStats = {
    totalValue: inventory.reduce((s, d) => s + d.totalValue, 0),
    totalItems: inventory.length,
    normalCount: inventory.filter(d => d.status === 1).length,
    warningCount: inventory.filter(d => d.status === 3).length,
  };

  const handleRefresh = () => {
    if (activeTab === 'stock') {
      fetchStock(1);
      setStockPage(1);
    } else if (activeTab === 'movement') {
      fetchMovements(1);
      setDocPage(1);
    }
  };

  return (
    <div style={{ padding: 24 }}>
      <Card title="库存管理 (对标 SAP MMBE)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={handleRefresh}>刷新</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setMovementModalVisible(true)}>
              库存移动
            </Button>
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            { key: 'stock', label: '库存查询' },
            { key: 'movement', label: '移动记录' },
            { key: 'warehouse', label: '仓库结构' },
          ]}
        />

        {/* 统计卡片 */}
        <Row gutter={16} style={{ marginBottom: 16 }}>
          <Col span={6}>
            <Card size="small">
              <Statistic title="库存总额" value={totalStats.totalValue} precision={2} prefix="¥" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="物料种类" value={totalStats.totalItems} suffix="种" valueStyle={{ fontSize: 18 }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="正常库存" value={totalStats.normalCount} suffix="项" valueStyle={{ fontSize: 18, color: '#52c41a' }} />
            </Card>
          </Col>
          <Col span={6}>
            <Card size="small">
              <Statistic title="库存告警" value={totalStats.warningCount} suffix="项" valueStyle={{ fontSize: 18, color: '#ff4d4f' }} />
            </Card>
          </Col>
        </Row>

        {/* 库存查询 */}
        {activeTab === 'stock' && (
          <>
            <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item name="keyword" label="搜索">
                <Input placeholder="物料编码/名称" style={{ width: 150 }} prefix={<BarcodeOutlined />} />
              </Form.Item>
              <Form.Item name="category" label="分类">
                <Select placeholder="全部" allowClear style={{ width: 100 }}
                  options={[
                    { value: '原材料', label: '原材料' },
                    { value: '包材', label: '包材' },
                    { value: '半成品', label: '半成品' },
                    { value: '产成品', label: '产成品' },
                  ]} />
              </Form.Item>
              <Form.Item name="warehouse" label="仓库">
                <Select placeholder="全部" allowClear style={{ width: 120 }}
                  options={[
                    { value: 'WH-01', label: 'WH-01 原材料仓' },
                    { value: 'WH-02', label: 'WH-02 辅材仓' },
                    { value: 'WH-03', label: 'WH-03 成品仓' },
                  ]} />
              </Form.Item>
              <Form.Item name="status" label="状态">
                <Select placeholder="全部" allowClear style={{ width: 80 }}
                  options={[
                    { value: 1, label: '正常' },
                    { value: 2, label: '呆滞' },
                    { value: 3, label: '告警' },
                  ]} />
              </Form.Item>
              <Form.Item>
                <Button type="primary" icon={<SearchOutlined />} onClick={() => fetchStock(1)}>查询</Button>
              </Form.Item>
            </Form>

            <Table
              columns={stockColumns}
              dataSource={inventory}
              rowKey="id"
              loading={loading}
              size="small"
              scroll={{ x: 1200 }}
              pagination={{
                current: stockPage,
                pageSize: stockPageSize,
                total: stockTotal,
                showSizeChanger: true,
                onChange: (page, size) => {
                  setStockPage(page);
                  setStockPageSize(size);
                  fetchStock(page, size);
                },
              }}
            />
          </>
        )}

        {/* 移动记录 */}
        {activeTab === 'movement' && (
          <>
            <Form form={form} layout="inline" style={{ marginBottom: 16 }}>
              <Form.Item name="dateRange" label="日期">
                <RangePicker />
              </Form.Item>
              <Form.Item name="movementType" label="类型">
                <Select placeholder="全部" allowClear style={{ width: 100 }}
                  options={[
                    { value: 'GR', label: '收货' },
                    { value: 'GI', label: '发货' },
                    { value: 'TR', label: '调拨' },
                  ]} />
              </Form.Item>
              <Form.Item>
                <Button type="primary" icon={<SearchOutlined />} onClick={() => fetchMovements(1)}>查询</Button>
              </Form.Item>
            </Form>

            <Table
              columns={movementColumns}
              dataSource={movements}
              rowKey="id"
              loading={loading}
              size="small"
              pagination={{
                current: docPage,
                pageSize: docPageSize,
                total: docTotal,
                showSizeChanger: true,
                onChange: (page, size) => {
                  setDocPage(page);
                  setDocPageSize(size);
                  fetchMovements(page, size);
                },
              }}
            />
          </>
        )}

        {/* 仓库结构 */}
        {activeTab === 'warehouse' && (
          <Row gutter={24}>
            <Col span={8}>
              <Card title="仓库结构" size="small">
                <Tree
                  defaultExpandedKeys={['all']}
                  defaultSelectedKeys={['all']}
                  treeData={warehouseTree}
                />
              </Card>
            </Col>
            <Col span={16}>
              <Card title="仓库详情" size="small">
                <Descriptions bordered column={2}>
                  <Descriptions.Item label="仓库编码">WH-01</Descriptions.Item>
                  <Descriptions.Item label="仓库名称">原材料仓</Descriptions.Item>
                  <Descriptions.Item label="仓库类型">原材料</Descriptions.Item>
                  <Descriptions.Item label="负责人">张三</Descriptions.Item>
                  <Descriptions.Item label="库位数">15</Descriptions.Item>
                  <Descriptions.Item label="已用库位">12</Descriptions.Item>
                  <Descriptions.Item label="库存金额">¥{totalStats.totalValue.toLocaleString()}</Descriptions.Item>
                  <Descriptions.Item label="物料种类">{inventory.length}</Descriptions.Item>
                </Descriptions>
              </Card>
            </Col>
          </Row>
        )}
      </Card>

      {/* 库存移动弹窗 */}
      <Modal
        title="库存移动"
        open={movementModalVisible}
        onCancel={() => setMovementModalVisible(false)}
        onOk={() => setMovementModalVisible(false)}
        width={600}
      >
        <Form layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="移动类型" required>
                <Select options={[
                  { value: 'GR', label: '收货 (GR)' },
                  { value: 'GI', label: '发货 (GI)' },
                  { value: 'TR', label: '调拨 (TR)' },
                ]} placeholder="选择类型" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="物料编码" required>
                <Input placeholder="输入物料编码" prefix={<BarcodeOutlined />} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="仓库" required>
                <Select options={[
                  { value: 'WH-01', label: 'WH-01 原材料仓' },
                  { value: 'WH-02', label: 'WH-02 辅材仓' },
                  { value: 'WH-03', label: 'WH-03 成品仓' },
                ]} placeholder="选择仓库" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="库位">
                <Input placeholder="如: A-1-01" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item label="数量" required>
                <InputNumber style={{ width: '100%' }} min={1} placeholder="输入数量" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="参考单号">
                <Input placeholder="采购订单/生产工单等" />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item label="备注">
            <Input.TextArea rows={2} placeholder="输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
