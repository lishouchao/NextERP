'use client';

import { useState, useEffect, useMemo } from 'react';
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
  DatePicker,
  message,
  Popconfirm,
  Tag,
  Row,
  Col,
  Divider,
  Descriptions,
  Tooltip,
  Badge,
  Tabs,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  SearchOutlined,
  CheckOutlined,
  CloseOutlined,
  SendOutlined,
  BookOutlined,
  CopyOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import type { FinVoucher, FinVoucherEntry } from '@/types/finance';
import {
  VOUCHER_TYPE_OPTIONS,
  VOUCHER_STATUS_OPTIONS,
  VOUCHER_WORD_OPTIONS,
} from '@/types/finance';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;
const { TextArea } = Input;

// 模拟凭证数据
const mockVouchers: FinVoucher[] = [
  {
    id: 1,
    voucherNo: '记-2023-0001',
    voucherWord: '记',
    voucherDate: '2023-12-01',
    accountingPeriod: '2023-12',
    voucherType: 3,
    attachmentCount: 2,
    debitAmount: 50000,
    creditAmount: 50000,
    createdById: 1,
    createdByName: '张三',
    approvedById: 2,
    approvedByName: '李四',
    approvedAt: '2023-12-01 10:30:00',
    postedById: 3,
    postedByName: '王五',
    postedAt: '2023-12-01 14:00:00',
    voucherStatus: 3,
    summary: '销售产品收款',
    sourceType: 'sales_order',
    sourceId: 1001,
    entries: [
      { id: 1, voucherId: 1, lineNo: 1, accountId: 2, accountCode: '1002', accountName: '银行存款', summary: '收到销售款', debitAmount: 50000, creditAmount: 0 },
      { id: 2, voucherId: 1, lineNo: 2, accountId: 25, accountCode: '6001', accountName: '主营业务收入', summary: '确认收入', debitAmount: 0, creditAmount: 44247.79 },
      { id: 3, voucherId: 1, lineNo: 3, accountId: 16, accountCode: '222101', accountName: '应交增值税', summary: '销项税额', debitAmount: 0, creditAmount: 5752.21 },
    ],
  },
  {
    id: 2,
    voucherNo: '记-2023-0002',
    voucherWord: '记',
    voucherDate: '2023-12-02',
    accountingPeriod: '2023-12',
    voucherType: 2,
    attachmentCount: 3,
    debitAmount: 30000,
    creditAmount: 30000,
    createdById: 1,
    createdByName: '张三',
    voucherStatus: 2,
    summary: '采购原材料付款',
    sourceType: 'purchase_order',
    sourceId: 2001,
    entries: [
      { id: 4, voucherId: 2, lineNo: 1, accountId: 8, accountCode: '1405', accountName: '原材料', summary: '采购原材料', debitAmount: 25641.03, creditAmount: 0 },
      { id: 5, voucherId: 2, lineNo: 2, accountId: 16, accountCode: '222101', accountName: '应交增值税', summary: '进项税额', debitAmount: 4358.97, creditAmount: 0 },
      { id: 6, voucherId: 2, lineNo: 3, accountId: 2, accountCode: '1002', accountName: '银行存款', summary: '支付采购款', debitAmount: 0, creditAmount: 30000 },
    ],
  },
  {
    id: 3,
    voucherNo: '记-2023-0003',
    voucherWord: '记',
    voucherDate: '2023-12-05',
    accountingPeriod: '2023-12',
    voucherType: 1,
    attachmentCount: 1,
    debitAmount: 100000,
    creditAmount: 100000,
    createdById: 1,
    createdByName: '张三',
    voucherStatus: 1,
    summary: '股东投资款',
    entries: [
      { id: 7, voucherId: 3, lineNo: 1, accountId: 2, accountCode: '1002', accountName: '银行存款', summary: '收到投资款', debitAmount: 100000, creditAmount: 0 },
      { id: 8, voucherId: 3, lineNo: 2, accountId: 17, accountCode: '4001', accountName: '实收资本', summary: '股东投资', debitAmount: 0, creditAmount: 100000 },
    ],
  },
  {
    id: 4,
    voucherNo: '记-2023-0004',
    voucherWord: '记',
    voucherDate: '2023-12-08',
    accountingPeriod: '2023-12',
    voucherType: 3,
    attachmentCount: 0,
    debitAmount: 5000,
    creditAmount: 5000,
    createdById: 2,
    createdByName: '李四',
    voucherStatus: 0,
    summary: '计提折旧',
    remark: '待完善附件',
    entries: [
      { id: 9, voucherId: 4, lineNo: 1, accountId: 24, accountCode: '5101', accountName: '制造费用', summary: '生产设备折旧', debitAmount: 3000, creditAmount: 0 },
      { id: 10, voucherId: 4, lineNo: 2, accountId: 29, accountCode: '6602', accountName: '管理费用', summary: '办公设备折旧', debitAmount: 2000, creditAmount: 0 },
      { id: 11, voucherId: 4, lineNo: 3, accountId: 10, accountCode: '1601', accountName: '固定资产', summary: '累计折旧', debitAmount: 0, creditAmount: 5000 },
    ],
  },
];

// 模拟科目数据
const accountOptions = [
  { value: 1, label: '1001 库存现金', code: '1001', name: '库存现金' },
  { value: 2, label: '1002 银行存款', code: '1002', name: '银行存款' },
  { value: 8, label: '1405 原材料', code: '1405', name: '原材料' },
  { value: 10, label: '1601 固定资产', code: '1601', name: '固定资产' },
  { value: 16, label: '222101 应交增值税', code: '222101', name: '应交增值税' },
  { value: 17, label: '4001 实收资本', code: '4001', name: '实收资本' },
  { value: 24, label: '5101 制造费用', code: '5101', name: '制造费用' },
  { value: 25, label: '6001 主营业务收入', code: '6001', name: '主营业务收入' },
  { value: 29, label: '6602 管理费用', code: '6602', name: '管理费用' },
];

export default function VouchersPage() {
  const [loading, setLoading] = useState(false);
  const [vouchers, setVouchers] = useState<FinVoucher[]>(mockVouchers);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [editingVoucher, setEditingVoucher] = useState<FinVoucher | null>(null);
  const [viewingVoucher, setViewingVoucher] = useState<FinVoucher | null>(null);
  const [form] = Form.useForm();
  const [activeTab, setActiveTab] = useState('all');
  const currentUser = { id: 1, name: '张三' };

  useEffect(() => {
    loadVouchers();
  }, []);

  const filteredVouchers = useMemo(() => {
    switch (activeTab) {
      case 'draft': return vouchers.filter((v) => v.voucherStatus === 0);
      case 'pending': return vouchers.filter((v) => v.voucherStatus === 1);
      case 'approved': return vouchers.filter((v) => v.voucherStatus === 2);
      case 'posted': return vouchers.filter((v) => v.voucherStatus === 3);
      default: return vouchers;
    }
  }, [vouchers, activeTab]);

  const loadVouchers = () => {
    setLoading(true);
    setTimeout(() => {
      setVouchers(mockVouchers);
      setLoading(false);
    }, 300);
  };

  const getStatusTag = (status: number) => {
    const option = VOUCHER_STATUS_OPTIONS.find((o) => o.value === status);
    return <Tag color={option?.color || 'default'}>{option?.label || '未知'}</Tag>;
  };

  const handleOpenModal = (voucher?: FinVoucher) => {
    setEditingVoucher(voucher || null);
    if (voucher) {
      form.setFieldsValue({
        voucherWord: voucher.voucherWord,
        voucherDate: dayjs(voucher.voucherDate),
        voucherType: voucher.voucherType,
        attachmentCount: voucher.attachmentCount,
        summary: voucher.summary,
        entries: voucher.entries.map((e) => ({
          accountId: e.accountId,
          summary: e.summary,
          debitAmount: e.debitAmount,
          creditAmount: e.creditAmount,
        })),
      });
    } else {
      form.resetFields();
      form.setFieldsValue({
        voucherWord: '记',
        voucherDate: dayjs(),
        voucherType: 3,
        attachmentCount: 0,
        entries: [{ accountId: undefined, summary: '', debitAmount: 0, creditAmount: 0 }],
      });
    }
    setModalVisible(true);
  };

  const handleCloseModal = () => {
    setModalVisible(false);
    setEditingVoucher(null);
    form.resetFields();
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const entries = values.entries || [];
      const debitAmount = entries.reduce((sum: number, e: any) => sum + (e.debitAmount || 0), 0);
      const creditAmount = entries.reduce((sum: number, e: any) => sum + (e.creditAmount || 0), 0);

      if (Math.abs(debitAmount - creditAmount) > 0.01) {
        message.error('借贷不平衡，请检查');
        return;
      }

      setLoading(true);
      setTimeout(() => {
        if (editingVoucher) {
          setVouchers((prev) =>
            prev.map((v) =>
              v.id === editingVoucher.id
                ? {
                    ...v,
                    ...values,
                    voucherDate: values.voucherDate.format('YYYY-MM-DD'),
                    accountingPeriod: values.voucherDate.format('YYYY-MM'),
                    debitAmount,
                    creditAmount,
                    entries: entries.map((e: any, idx: number) => {
                      const acc = accountOptions.find((a) => a.value === e.accountId);
                      return {
                        id: Date.now() + idx,
                        voucherId: v.id,
                        lineNo: idx + 1,
                        accountId: e.accountId,
                        accountCode: acc?.code || '',
                        accountName: acc?.name || '',
                        summary: e.summary,
                        debitAmount: e.debitAmount || 0,
                        creditAmount: e.creditAmount || 0,
                      };
                    }),
                  }
                : v
            )
          );
          message.success('更新凭证成功');
        } else {
          const newVoucher: FinVoucher = {
            id: Math.max(...vouchers.map((v) => v.id)) + 1,
            voucherNo: `记-2023-${String(vouchers.length + 1).padStart(4, '0')}`,
            ...values,
            voucherDate: values.voucherDate.format('YYYY-MM-DD'),
            accountingPeriod: values.voucherDate.format('YYYY-MM'),
            debitAmount,
            creditAmount,
            createdById: currentUser.id,
            createdByName: currentUser.name,
            voucherStatus: 0,
            entries: entries.map((e: any, idx: number) => {
              const acc = accountOptions.find((a) => a.value === e.accountId);
              return {
                id: Date.now() + idx,
                voucherId: Math.max(...vouchers.map((v) => v.id)) + 1,
                lineNo: idx + 1,
                accountId: e.accountId,
                accountCode: acc?.code || '',
                accountName: acc?.name || '',
                summary: e.summary,
                debitAmount: e.debitAmount || 0,
                creditAmount: e.creditAmount || 0,
              };
            }),
          };
          setVouchers((prev) => [...prev, newVoucher]);
          message.success('创建凭证成功');
        }
        handleCloseModal();
        setLoading(false);
      }, 500);
    } catch (error) {
      console.error(error);
    }
  };

  const handleSubmitForApproval = (id: number) => {
    setLoading(true);
    setTimeout(() => {
      setVouchers((prev) => prev.map((v) => (v.id === id ? { ...v, voucherStatus: 1 } : v)));
      message.success('已提交审核');
      setLoading(false);
    }, 300);
  };

  const handleApprove = (id: number) => {
    setLoading(true);
    setTimeout(() => {
      setVouchers((prev) =>
        prev.map((v) =>
          v.id === id
            ? { ...v, voucherStatus: 2, approvedById: currentUser.id, approvedByName: currentUser.name, approvedAt: dayjs().format('YYYY-MM-DD HH:mm:ss') }
            : v
        )
      );
      message.success('审核通过');
      setLoading(false);
    }, 300);
  };

  const handlePost = (id: number) => {
    setLoading(true);
    setTimeout(() => {
      setVouchers((prev) =>
        prev.map((v) =>
          v.id === id
            ? { ...v, voucherStatus: 3, postedById: currentUser.id, postedByName: currentUser.name, postedAt: dayjs().format('YYYY-MM-DD HH:mm:ss') }
            : v
        )
      );
      message.success('记账成功');
      setLoading(false);
    }, 300);
  };

  const handleDelete = (id: number) => {
    setLoading(true);
    setTimeout(() => {
      setVouchers((prev) => prev.filter((v) => v.id !== id));
      message.success('删除凭证成功');
      setLoading(false);
    }, 300);
  };

  const handleCopy = (voucher: FinVoucher) => {
    const newVoucher: FinVoucher = {
      ...voucher,
      id: Math.max(...vouchers.map((v) => v.id)) + 1,
      voucherNo: `记-2023-${String(vouchers.length + 1).padStart(4, '0')}`,
      voucherDate: dayjs().format('YYYY-MM-DD'),
      voucherStatus: 0,
      createdById: currentUser.id,
      createdByName: currentUser.name,
      entries: voucher.entries.map((e) => ({ ...e, id: Date.now() + e.lineNo })),
    };
    setVouchers((prev) => [...prev, newVoucher]);
    message.success('复制凭证成功');
  };

  const columns = [
    { title: '凭证号', dataIndex: 'voucherNo', key: 'voucherNo', width: 140, fixed: 'left' as const,
      render: (text: string, record: FinVoucher) => (
        <a onClick={() => { setViewingVoucher(record); setDetailVisible(true); }}>{text}</a>
      ),
    },
    { title: '凭证日期', dataIndex: 'voucherDate', key: 'voucherDate', width: 110 },
    { title: '凭证字', dataIndex: 'voucherWord', key: 'voucherWord', width: 80 },
    { title: '凭证类型', dataIndex: 'voucherType', key: 'voucherType', width: 100,
      render: (type: number) => VOUCHER_TYPE_OPTIONS.find((o) => o.value === type)?.label || '-',
    },
    { title: '摘要', dataIndex: 'summary', key: 'summary', ellipsis: true, width: 180 },
    { title: '借方金额', dataIndex: 'debitAmount', key: 'debitAmount', width: 120, align: 'right' as const,
      render: (amount: number) => amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2 }),
    },
    { title: '贷方金额', dataIndex: 'creditAmount', key: 'creditAmount', width: 120, align: 'right' as const,
      render: (amount: number) => amount?.toLocaleString('zh-CN', { minimumFractionDigits: 2 }),
    },
    { title: '附件', dataIndex: 'attachmentCount', key: 'attachmentCount', width: 60, align: 'center' as const,
      render: (count: number) => <Badge count={count} showZero size="small" />,
    },
    { title: '状态', dataIndex: 'voucherStatus', key: 'voucherStatus', width: 80, render: (status: number) => getStatusTag(status) },
    { title: '制单人', dataIndex: 'createdByName', key: 'createdByName', width: 80 },
    { title: '操作', key: 'action', width: 220, fixed: 'right' as const,
      render: (_: unknown, record: FinVoucher) => {
        const status = record.voucherStatus;
        return (
          <Space size="small">
            <Tooltip title="查看">
              <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => { setViewingVoucher(record); setDetailVisible(true); }} />
            </Tooltip>
            {status === 0 && (
              <>
                <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleOpenModal(record)}>编辑</Button>
                <Button type="link" size="small" icon={<SendOutlined />} onClick={() => handleSubmitForApproval(record.id)}>提交</Button>
                <Popconfirm title="确定要删除此凭证吗？" onConfirm={() => handleDelete(record.id)}>
                  <Button type="link" size="small" danger icon={<DeleteOutlined />} />
                </Popconfirm>
              </>
            )}
            {status === 1 && (
              <>
                <Button type="link" size="small" icon={<CheckOutlined />} onClick={() => handleApprove(record.id)}>审核</Button>
              </>
            )}
            {status === 2 && (
              <Button type="link" size="small" icon={<BookOutlined />} onClick={() => handlePost(record.id)}>记账</Button>
            )}
            <Tooltip title="复制">
              <Button type="link" size="small" icon={<CopyOutlined />} onClick={() => handleCopy(record)} />
            </Tooltip>
          </Space>
        );
      },
    },
  ];

  const entryColumns = [
    { title: '行号', dataIndex: 'lineNo', key: 'lineNo', width: 60 },
    { title: '科目编码', dataIndex: 'accountCode', key: 'accountCode', width: 100 },
    { title: '科目名称', dataIndex: 'accountName', key: 'accountName' },
    { title: '摘要', dataIndex: 'summary', key: 'summary', ellipsis: true },
    { title: '借方金额', dataIndex: 'debitAmount', key: 'debitAmount', width: 120, align: 'right' as const,
      render: (amount: number) => amount > 0 ? amount.toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '',
    },
    { title: '贷方金额', dataIndex: 'creditAmount', key: 'creditAmount', width: 120, align: 'right' as const,
      render: (amount: number) => amount > 0 ? amount.toLocaleString('zh-CN', { minimumFractionDigits: 2 }) : '',
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card
        title="凭证管理 (对标 SAP FB50)"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadVouchers}>刷新</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => handleOpenModal()}>新增凭证</Button>
          </Space>
        }
      >
        <Tabs activeKey={activeTab} onChange={setActiveTab} items={[
          { key: 'all', label: `全部 (${vouchers.length})` },
          { key: 'draft', label: `草稿 (${vouchers.filter((v) => v.voucherStatus === 0).length})` },
          { key: 'pending', label: `待审核 (${vouchers.filter((v) => v.voucherStatus === 1).length})` },
          { key: 'approved', label: `已审核 (${vouchers.filter((v) => v.voucherStatus === 2).length})` },
          { key: 'posted', label: `已记账 (${vouchers.filter((v) => v.voucherStatus === 3).length})` },
        ]} />

        <Table
          columns={columns}
          dataSource={filteredVouchers}
          rowKey="id"
          loading={loading}
          size="small"
          scroll={{ x: 1400 }}
          pagination={{ defaultPageSize: 20, showSizeChanger: true, showTotal: (total) => `共 ${total} 条凭证` }}
        />
      </Card>

      {/* 新增/编辑弹窗 */}
      <Modal
        title={editingVoucher ? '编辑凭证' : '新增凭证'}
        open={modalVisible}
        onCancel={handleCloseModal}
        onOk={handleSubmit}
        confirmLoading={loading}
        destroyOnClose
        width={900}
      >
        <Form form={form} layout="vertical">
          <Row gutter={16}>
            <Col span={4}>
              <Form.Item name="voucherWord" label="凭证字" rules={[{ required: true }]}>
                <Select options={VOUCHER_WORD_OPTIONS} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="voucherDate" label="凭证日期" rules={[{ required: true }]}>
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="voucherType" label="凭证类型" rules={[{ required: true }]}>
                <Select options={VOUCHER_TYPE_OPTIONS} />
              </Form.Item>
            </Col>
            <Col span={4}>
              <Form.Item name="attachmentCount" label="附件数">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="summary" label="摘要">
            <Input placeholder="凭证摘要" />
          </Form.Item>

          <Divider>分录信息</Divider>

          <Form.List name="entries">
            {(fields, { add, remove }) => (
              <>
                {fields.map((field) => (
                  <Row key={field.key} gutter={16} style={{ marginBottom: 8 }}>
                    <Col span={8}>
                      <Form.Item {...field} name={[field.name, 'accountId']} rules={[{ required: true, message: '请选择科目' }]}>
                        <Select showSearch placeholder="选择科目" options={accountOptions}
                          filterOption={(input, option) => (option?.label ?? '').toLowerCase().includes(input.toLowerCase())} />
                      </Form.Item>
                    </Col>
                    <Col span={6}>
                      <Form.Item {...field} name={[field.name, 'summary']}>
                        <Input placeholder="摘要" />
                      </Form.Item>
                    </Col>
                    <Col span={4}>
                      <Form.Item {...field} name={[field.name, 'debitAmount']}>
                        <InputNumber placeholder="借方金额" min={0} precision={2} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                    <Col span={4}>
                      <Form.Item {...field} name={[field.name, 'creditAmount']}>
                        <InputNumber placeholder="贷方金额" min={0} precision={2} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                    <Col span={2}>
                      {fields.length > 2 && <Button type="link" danger onClick={() => remove(field.name)}>删除</Button>}
                    </Col>
                  </Row>
                ))}
                <Button type="dashed" onClick={() => add({ accountId: undefined, summary: '', debitAmount: 0, creditAmount: 0 })} block>
                  + 添加分录
                </Button>
              </>
            )}
          </Form.List>
        </Form>
      </Modal>

      {/* 查看详情弹窗 */}
      <Modal
        title={`凭证详情 - ${viewingVoucher?.voucherNo}`}
        open={detailVisible}
        onCancel={() => { setDetailVisible(false); setViewingVoucher(null); }}
        footer={[<Button key="close" onClick={() => { setDetailVisible(false); setViewingVoucher(null); }}>关闭</Button>]}
        width={800}
      >
        {viewingVoucher && (
          <>
            <Descriptions bordered size="small" column={4}>
              <Descriptions.Item label="凭证号">{viewingVoucher.voucherNo}</Descriptions.Item>
              <Descriptions.Item label="凭证日期">{viewingVoucher.voucherDate}</Descriptions.Item>
              <Descriptions.Item label="会计期间">{viewingVoucher.accountingPeriod}</Descriptions.Item>
              <Descriptions.Item label="状态">{getStatusTag(viewingVoucher.voucherStatus)}</Descriptions.Item>
            </Descriptions>

            <Divider>分录明细</Divider>

            <Table columns={entryColumns} dataSource={viewingVoucher.entries} rowKey="id" size="small" pagination={false}
              summary={() => (
                <Table.Summary fixed>
                  <Table.Summary.Row>
                    <Table.Summary.Cell index={0} colSpan={4} align="right"><strong>合计</strong></Table.Summary.Cell>
                    <Table.Summary.Cell index={1} align="right">
                      <strong style={{ color: '#1890ff' }}>{viewingVoucher.debitAmount?.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</strong>
                    </Table.Summary.Cell>
                    <Table.Summary.Cell index={2} align="right">
                      <strong style={{ color: '#52c41a' }}>{viewingVoucher.creditAmount?.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</strong>
                    </Table.Summary.Cell>
                  </Table.Summary.Row>
                </Table.Summary>
              )}
            />
          </>
        )}
      </Modal>
    </div>
  );
}
