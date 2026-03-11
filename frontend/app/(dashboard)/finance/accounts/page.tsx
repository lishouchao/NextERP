'use client';

import { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  InputNumber,
  Switch,
  message,
  Popconfirm,
  Tree,
  Tag,
  Row,
  Col,
  Tabs,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  BankOutlined,
  DollarOutlined,
} from '@ant-design/icons';
import type { FinAccount } from '@/types/finance';
import {
  ACCOUNT_TYPE_OPTIONS,
  ACCOUNT_DIRECTION_OPTIONS,
} from '@/types/finance';

// 模拟数据 - 会计科目
const mockAccounts: FinAccount[] = [
  // 资产类
  { id: 1, accountCode: '1001', accountName: '库存现金', accountType: 1, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: true, isCash: true, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: false, status: 1 },
  { id: 2, accountCode: '1002', accountName: '银行存款', accountType: 1, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: false, isCash: false, isBank: true, isQuantity: false, isForeignCurrency: true, isAuxiliary: false, status: 1 },
  { id: 3, accountCode: '100201', accountName: '工商银行', accountType: 1, accountDirection: 1, parentId: 2, accountLevel: 2, isLeaf: true, isCash: false, isBank: true, isQuantity: false, isForeignCurrency: true, isAuxiliary: true, auxiliaryType: '["project"]', status: 1 },
  { id: 4, accountCode: '100202', accountName: '建设银行', accountType: 1, accountDirection: 1, parentId: 2, accountLevel: 2, isLeaf: true, isCash: false, isBank: true, isQuantity: false, isForeignCurrency: true, isAuxiliary: true, auxiliaryType: '["project"]', status: 1 },
  { id: 5, accountCode: '1012', accountName: '其他货币资金', accountType: 1, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: false, status: 1 },
  { id: 6, accountCode: '1122', accountName: '应收账款', accountType: 1, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["customer"]', status: 1 },
  { id: 7, accountCode: '1123', accountName: '预付账款', accountType: 1, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["supplier"]', status: 1 },
  { id: 8, accountCode: '1405', accountName: '原材料', accountType: 1, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: true, quantityUnit: '千克', isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["warehouse","material"]', status: 1 },
  { id: 9, accountCode: '1406', accountName: '库存商品', accountType: 1, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: true, quantityUnit: '件', isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["warehouse","product"]', status: 1 },
  { id: 10, accountCode: '1601', accountName: '固定资产', accountType: 1, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: true, quantityUnit: '台', isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["asset"]', status: 1 },
  // 负债类
  { id: 11, accountCode: '2001', accountName: '短期借款', accountType: 2, accountDirection: 2, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: true, isAuxiliary: false, status: 1 },
  { id: 12, accountCode: '2202', accountName: '应付账款', accountType: 2, accountDirection: 2, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["supplier"]', status: 1 },
  { id: 13, accountCode: '2203', accountName: '预收账款', accountType: 2, accountDirection: 2, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["customer"]', status: 1 },
  { id: 14, accountCode: '2211', accountName: '应付职工薪酬', accountType: 2, accountDirection: 2, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["department"]', status: 1 },
  { id: 15, accountCode: '2221', accountName: '应交税费', accountType: 2, accountDirection: 2, parentId: null, accountLevel: 1, isLeaf: false, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: false, status: 1 },
  { id: 16, accountCode: '222101', accountName: '应交增值税', accountType: 2, accountDirection: 2, parentId: 15, accountLevel: 2, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: false, status: 1 },
  // 所有者权益
  { id: 17, accountCode: '4001', accountName: '实收资本', accountType: 3, accountDirection: 2, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: false, status: 1 },
  { id: 18, accountCode: '4002', accountName: '资本公积', accountType: 3, accountDirection: 2, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: false, status: 1 },
  { id: 19, accountCode: '4101', accountName: '盈余公积', accountType: 3, accountDirection: 2, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: false, status: 1 },
  { id: 20, accountCode: '4103', accountName: '本年利润', accountType: 3, accountDirection: 2, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: false, status: 1 },
  // 成本类
  { id: 21, accountCode: '5001', accountName: '生产成本', accountType: 4, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: false, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: false, status: 1 },
  { id: 22, accountCode: '500101', accountName: '直接材料', accountType: 4, accountDirection: 1, parentId: 21, accountLevel: 2, isLeaf: true, isCash: false, isBank: false, isQuantity: true, quantityUnit: '千克', isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["product","material"]', status: 1 },
  { id: 23, accountCode: '500102', accountName: '直接人工', accountType: 4, accountDirection: 1, parentId: 21, accountLevel: 2, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["department"]', status: 1 },
  { id: 24, accountCode: '5101', accountName: '制造费用', accountType: 4, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["department"]', status: 1 },
  // 损益类
  { id: 25, accountCode: '6001', accountName: '主营业务收入', accountType: 5, accountDirection: 2, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["department","product"]', status: 1 },
  { id: 26, accountCode: '6051', accountName: '其他业务收入', accountType: 5, accountDirection: 2, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: false, status: 1 },
  { id: 27, accountCode: '6401', accountName: '主营业务成本', accountType: 5, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: true, quantityUnit: '件', isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["product"]', status: 1 },
  { id: 28, accountCode: '6601', accountName: '销售费用', accountType: 5, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["department"]', status: 1 },
  { id: 29, accountCode: '6602', accountName: '管理费用', accountType: 5, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: false, isAuxiliary: true, auxiliaryType: '["department"]', status: 1 },
  { id: 30, accountCode: '6603', accountName: '财务费用', accountType: 5, accountDirection: 1, parentId: null, accountLevel: 1, isLeaf: true, isCash: false, isBank: false, isQuantity: false, isForeignCurrency: true, isAuxiliary: false, status: 1 },
];

export default function AccountsPage() {
  const [loading, setLoading] = useState(false);
  const [accounts, setAccounts] = useState<FinAccount[]>(mockAccounts);
  const [selectedAccount, setSelectedAccount] = useState<FinAccount | null>(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingAccount, setEditingAccount] = useState<FinAccount | null>(null);
  const [form] = Form.useForm();
  const [activeTab, setActiveTab] = useState('list');

  // 加载科目数据
  const loadAccounts = async () => {
    setLoading(true);
    // 模拟 API 调用
    setTimeout(() => {
      setAccounts(mockAccounts);
      setLoading(false);
    }, 500);
  };

  useEffect(() => {
    loadAccounts();
  }, []);

  // 将扁平数据转换为树形结构
  const buildTreeData = (items: FinAccount[]): any[] => {
    const map = new Map<number, any>();
    const roots: any[] = [];

    items.forEach((item) => {
      const node = {
        key: item.id,
        title: `${item.accountCode} ${item.accountName}`,
        value: item,
        children: [],
      };
      map.set(item.id, node);
    });

    items.forEach((item) => {
      const node = map.get(item.id);
      if (node && item.parentId && map.has(item.parentId)) {
        const parent = map.get(item.parentId);
        if (parent) {
          parent.children.push(node);
        }
      } else if (node) {
        roots.push(node);
      }
    });

    return roots;
  };

  // 按科目类型分组
  const groupByType = (items: FinAccount[]) => {
    const groups: Record<number, FinAccount[]> = { 1: [], 2: [], 3: [], 4: [], 5: [] };
    items.forEach((item) => {
      if (groups[item.accountType]) {
        groups[item.accountType].push(item);
      }
    });
    return groups;
  };

  const treeData = buildTreeData(accounts);
  const groupedAccounts = groupByType(accounts);

  // 获取科目类型颜色
  const getAccountTypeColor = (type: number) => {
    const colors: Record<number, string> = { 1: 'blue', 2: 'red', 3: 'green', 4: 'orange', 5: 'purple' };
    return colors[type] || 'default';
  };

  // 打开新增/编辑弹窗
  const handleOpenModal = (account?: FinAccount) => {
    setEditingAccount(account || null);
    if (account) {
      form.setFieldsValue({
        ...account,
        isCash: account.isCash,
        isBank: account.isBank,
        isQuantity: account.isQuantity,
        isForeignCurrency: account.isForeignCurrency,
        isAuxiliary: account.isAuxiliary,
      });
    } else {
      form.resetFields();
      form.setFieldsValue({
        accountDirection: 1,
        accountLevel: 1,
        isCash: false,
        isBank: false,
        isQuantity: false,
        isForeignCurrency: false,
        isAuxiliary: false,
        status: 1,
      });
    }
    setModalVisible(true);
  };

  // 关闭弹窗
  const handleCloseModal = () => {
    setModalVisible(false);
    setEditingAccount(null);
    form.resetFields();
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      setTimeout(() => {
        if (editingAccount) {
          setAccounts((prev) =>
            prev.map((a) => (a.id === editingAccount.id ? { ...a, ...values } : a))
          );
          message.success('更新科目成功');
        } else {
          const newAccount: FinAccount = {
            id: Math.max(...accounts.map((a) => a.id)) + 1,
            ...values,
            isLeaf: true,
          };
          setAccounts((prev) => [...prev, newAccount]);
          message.success('创建科目成功');
        }
        handleCloseModal();
        setLoading(false);
      }, 500);
    } catch (error) {
      console.error(error);
    }
  };

  // 删除科目
  const handleDelete = (id: number) => {
    const hasChildren = accounts.some((a) => a.parentId === id);
    if (hasChildren) {
      message.error('该科目下有子科目，无法删除');
      return;
    }

    setLoading(true);
    setTimeout(() => {
      setAccounts((prev) => prev.filter((a) => a.id !== id));
      message.success('删除科目成功');
      setLoading(false);
    }, 300);
  };

  // 表格列定义
  const columns = [
    {
      title: '科目编码',
      dataIndex: 'accountCode',
      key: 'accountCode',
      width: 120,
    },
    {
      title: '科目名称',
      dataIndex: 'accountName',
      key: 'accountName',
    },
    {
      title: '科目类型',
      dataIndex: 'accountType',
      key: 'accountType',
      width: 120,
      render: (type: number) => (
        <Tag color={getAccountTypeColor(type)}>
          {ACCOUNT_TYPE_OPTIONS.find((o) => o.value === type)?.label}
        </Tag>
      ),
    },
    {
      title: '方向',
      dataIndex: 'accountDirection',
      key: 'accountDirection',
      width: 80,
      render: (direction: number) => (
        <Tag color={direction === 1 ? 'blue' : 'red'}>
          {direction === 1 ? '借' : '贷'}
        </Tag>
      ),
    },
    {
      title: '属性',
      key: 'attributes',
      width: 150,
      render: (_: unknown, record: FinAccount) => (
        <Space size="small">
          {record.isCash && <Tag icon={<DollarOutlined />} color="gold">现金</Tag>}
          {record.isBank && <Tag icon={<BankOutlined />} color="cyan">银行</Tag>}
          {record.isQuantity && <Tag color="blue">数量</Tag>}
          {record.isForeignCurrency && <Tag color="green">外币</Tag>}
          {record.isAuxiliary && <Tag color="purple">辅助</Tag>}
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: number) => (
        <Tag color={status === 1 ? 'green' : 'default'}>{status === 1 ? '启用' : '禁用'}</Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_: unknown, record: FinAccount) => (
        <Space>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleOpenModal(record)}>
            编辑
          </Button>
          <Popconfirm title="确定要删除此科目吗？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card
        title="会计科目管理 (对标 SAP FS00)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadAccounts}>
              刷新
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => handleOpenModal()}>
              新增科目
            </Button>
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            { key: 'list', label: '列表视图' },
            { key: 'tree', label: '树形视图' },
            { key: 'group', label: '分类视图' },
          ]}
        />

        {activeTab === 'list' && (
          <Table
            columns={columns}
            dataSource={accounts}
            rowKey="id"
            loading={loading}
            size="small"
            scroll={{ x: 1000 }}
            pagination={{ defaultPageSize: 20, showSizeChanger: true, showTotal: (total) => `共 ${total} 个科目` }}
          />
        )}

        {activeTab === 'tree' && (
          <div style={{ display: 'flex', gap: 24 }}>
            <div style={{ width: 350, borderRight: '1px solid #f0f0f0', paddingRight: 24, minHeight: 500 }}>
              <div style={{ marginBottom: 16, fontWeight: 'bold' }}>科目树</div>
              <Tree
                showLine
                defaultExpandAll
                treeData={treeData}
                onSelect={(selectedKeys, info) => {
                  if (info.selected && info.node.value) {
                    setSelectedAccount(info.node.value);
                  }
                }}
              />
            </div>
            <div style={{ flex: 1 }}>
              {selectedAccount ? (
                <div>
                  <h3 style={{ marginBottom: 16 }}>
                    {selectedAccount.accountCode} - {selectedAccount.accountName}
                  </h3>
                  <Row gutter={[16, 8]}>
                    <Col span={12}><b>科目类型:</b> {ACCOUNT_TYPE_OPTIONS.find(o => o.value === selectedAccount.accountType)?.label}</Col>
                    <Col span={12}><b>科目方向:</b> {selectedAccount.accountDirection === 1 ? '借方' : '贷方'}</Col>
                    <Col span={12}><b>现金科目:</b> {selectedAccount.isCash ? '是' : '否'}</Col>
                    <Col span={12}><b>银行科目:</b> {selectedAccount.isBank ? '是' : '否'}</Col>
                    <Col span={12}><b>数量核算:</b> {selectedAccount.isQuantity ? `是 (${selectedAccount.quantityUnit || ''})` : '否'}</Col>
                    <Col span={12}><b>外币核算:</b> {selectedAccount.isForeignCurrency ? `是 (${selectedAccount.currency || ''})` : '否'}</Col>
                    <Col span={12}><b>辅助核算:</b> {selectedAccount.isAuxiliary ? '是' : '否'}</Col>
                    <Col span={24}><b>备注:</b> {selectedAccount.remark || '-'}</Col>
                  </Row>
                  <div style={{ marginTop: 16 }}>
                    <Button type="primary" onClick={() => handleOpenModal(selectedAccount)}>编辑</Button>
                  </div>
                </div>
              ) : (
                <div style={{ color: '#999', textAlign: 'center', padding: 50 }}>请从左侧选择科目查看详情</div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'group' && (
          <div>
            {Object.entries(groupedAccounts).map(([type, items]) => (
              <Card
                key={type}
                title={
                  <Tag color={getAccountTypeColor(Number(type))} style={{ fontSize: 14 }}>
                    {ACCOUNT_TYPE_OPTIONS.find((o) => o.value === Number(type))?.label} ({items.length})
                  </Tag>
                }
                style={{ marginBottom: 16 }}
                size="small"
              >
                <Table
                  columns={columns.filter((c) => c.key !== 'accountType')}
                  dataSource={items}
                  rowKey="id"
                  loading={loading}
                  size="small"
                  pagination={false}
                />
              </Card>
            ))}
          </div>
        )}
      </Card>

      {/* 新增/编辑弹窗 */}
      <Modal
        title={editingAccount ? '编辑科目' : '新增科目'}
        open={modalVisible}
        onCancel={handleCloseModal}
        onOk={handleSubmit}
        confirmLoading={loading}
        destroyOnClose
        width={700}
      >
        <Form form={form} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="accountCode" label="科目编码" rules={[{ required: true, message: '请输入科目编码' }]}>
                <Input placeholder="如: 1001" disabled={!!editingAccount} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="accountName" label="科目名称" rules={[{ required: true, message: '请输入科目名称' }]}>
                <Input placeholder="如: 库存现金" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="accountType" label="科目类型" rules={[{ required: true, message: '请选择科目类型' }]}>
                <Select options={ACCOUNT_TYPE_OPTIONS} placeholder="请选择" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="accountDirection" label="科目方向" rules={[{ required: true, message: '请选择科目方向' }]}>
                <Select options={ACCOUNT_DIRECTION_OPTIONS} placeholder="请选择" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="parentId" label="上级科目">
                <InputNumber placeholder="上级科目ID" style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="accountLevel" label="科目层级" rules={[{ required: true }]}>
                <InputNumber min={1} max={5} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Card title="核算属性" size="small" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={6}>
                <Form.Item name="isCash" label="现金科目" valuePropName="checked">
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item name="isBank" label="银行科目" valuePropName="checked">
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item name="isQuantity" label="数量核算" valuePropName="checked">
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={6}>
                <Form.Item name="isForeignCurrency" label="外币核算" valuePropName="checked">
                  <Switch />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item name="quantityUnit" label="数量单位">
                  <Input placeholder="千克、件、台等" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="currency" label="币种">
                  <Select placeholder="请选择" options={[
                    { value: 'CNY', label: '人民币' },
                    { value: 'USD', label: '美元' },
                    { value: 'EUR', label: '欧元' },
                    { value: 'JPY', label: '日元' },
                  ]} />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item name="isAuxiliary" label="辅助核算" valuePropName="checked">
                  <Switch />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="auxiliaryType" label="辅助核算类型">
                  <Select mode="multiple" placeholder="请选择" options={[
                    { value: 'customer', label: '客户' },
                    { value: 'supplier', label: '供应商' },
                    { value: 'department', label: '部门' },
                    { value: 'employee', label: '员工' },
                    { value: 'project', label: '项目' },
                    { value: 'warehouse', label: '仓库' },
                  ]} />
                </Form.Item>
              </Col>
            </Row>
          </Card>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
