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
import type { FinVoucher, FinVoucherEntry, FinVoucherFormData } from '@/types/finance';
import {
  VOUCHER_TYPE_OPTIONS,
  VOUCHER_STATUS_OPTIONS,
  VOUCHER_WORD_OPTIONS,
} from '@/types/finance';
import { voucherApi, accountApi } from '@/lib/api/finance';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;
const { TextArea } = Input;

export default function VouchersPage() {
  const [loading, setLoading] = useState(false);
  const [vouchers, setVouchers] = useState<FinVoucher[]>([]);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailVisible, setDetailVisible] = useState(false);
  const [editingVoucher, setEditingVoucher] = useState<FinVoucher | null>(null);
  const [viewingVoucher, setViewingVoucher] = useState<FinVoucher | null>(null);
  const [form] = Form.useForm();
  const [activeTab, setActiveTab] = useState('all');
  const [accountOptions, setAccountOptions] = useState<{ value: number; label: string; code: string; name: string }[]>([]);
  const currentUser = { id: 1, name: '张三' };

  // 加载科目选项
  const loadAccountOptions = async () => {
    try {
      const res = await accountApi.getList();
      const accounts = res.data || [];
      const options = accounts.map((a: any) => ({
        value: a.id,
        label: `${a.accountCode} ${a.accountName}`,
        code: a.accountCode,
        name: a.accountName,
      }));
      setAccountOptions(options);
    } catch (err) {
      // silently ignore
    }
  };

  useEffect(() => {
    loadAccountOptions();
  }, []);

  // 加载凭证数据
  const loadVouchers = async () => {
    setLoading(true);
    try {
      const params: any = {
        current: currentPage,
        size: pageSize,
      };
      // If filtering by tab, add status filter
      if (activeTab !== 'all') {
        const statusMap: Record<string, number> = {
          draft: 0,
          pending: 1,
          approved: 2,
          posted: 3,
        };
        if (statusMap[activeTab] !== undefined) {
          params.voucherStatus = statusMap[activeTab];
        }
      }
      const res = await voucherApi.getList(params);
      const pageData = res.data;
      if (pageData) {
        setVouchers(pageData.records || []);
        setTotal(pageData.total || 0);
      }
    } catch (err) {
      message.error('加载凭证数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadVouchers();
  }, [currentPage, pageSize, activeTab]);

  // View voucher detail by fetching full data
  const handleViewDetail = async (voucher: FinVoucher) => {
    try {
      const res = await voucherApi.getById(voucher.id);
      setViewingVoucher(res.data || voucher);
      setDetailVisible(true);
    } catch (err) {
      // Fallback to the list item data
      setViewingVoucher(voucher);
      setDetailVisible(true);
    }
  };

  const filteredVouchers = useMemo(() => {
    return vouchers;
  }, [vouchers]);

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

      const formData: FinVoucherFormData = {
        voucherWord: values.voucherWord,
        voucherDate: values.voucherDate.format('YYYY-MM-DD'),
        voucherType: values.voucherType,
        attachmentCount: values.attachmentCount,
        summary: values.summary,
        entries: entries.map((e: any) => ({
          accountId: e.accountId,
          summary: e.summary,
          debitAmount: e.debitAmount || 0,
          creditAmount: e.creditAmount || 0,
        })),
      };

      if (editingVoucher) {
        await voucherApi.update(editingVoucher.id, formData);
        message.success('更新凭证成功');
      } else {
        await voucherApi.create(formData);
        message.success('创建凭证成功');
      }
      handleCloseModal();
      loadVouchers();
    } catch (error: any) {
      if (error?.response?.data?.message) {
        message.error(error.response.data.message);
      } else if (error?.errorFields) {
        // form validation error, ignore
      } else {
        message.error('操作失败');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitForApproval = async (id: number) => {
    setLoading(true);
    try {
      await voucherApi.submitForApproval(id);
      message.success('已提交审核');
      loadVouchers();
    } catch (err) {
      message.error('提交审核失败');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (id: number) => {
    setLoading(true);
    try {
      await voucherApi.approve(id);
      message.success('审核通过');
      loadVouchers();
    } catch (err) {
      message.error('审核失败');
    } finally {
      setLoading(false);
    }
  };

  const handlePost = async (id: number) => {
    setLoading(true);
    try {
      await voucherApi.post(id);
      message.success('记账成功');
      loadVouchers();
    } catch (err) {
      message.error('记账失败');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    setLoading(true);
    try {
      await voucherApi.delete(id);
      message.success('删除凭证成功');
      loadVouchers();
    } catch (err) {
      message.error('删除凭证失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = (voucher: FinVoucher) => {
    // Copy creates a new voucher pre-filled from the existing one
    handleOpenModal();
    setTimeout(() => {
      form.setFieldsValue({
        voucherWord: voucher.voucherWord,
        voucherDate: dayjs(),
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
    }, 0);
  };

  const columns = [
    { title: '凭证号', dataIndex: 'voucherNo', key: 'voucherNo', width: 140, fixed: 'left' as const,
      render: (text: string, record: FinVoucher) => (
        <a onClick={() => handleViewDetail(record)}>{text}</a>
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
              <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => handleViewDetail(record)} />
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

  // Tab counts - derive from status-based counts
  const tabCounts = useMemo(() => {
    const counts = { all: vouchers.length, draft: 0, pending: 0, approved: 0, posted: 0 };
    // Since we filter on server side, counts are only accurate for the active tab
    // For tab labels, we show server-side total
    return counts;
  }, [vouchers]);

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
        <Tabs activeKey={activeTab} onChange={(key) => { setActiveTab(key); setCurrentPage(1); }} items={[
          { key: 'all', label: `全部 (${total})` },
          { key: 'draft', label: '草稿' },
          { key: 'pending', label: '待审核' },
          { key: 'approved', label: '已审核' },
          { key: 'posted', label: '已记账' },
        ]} />

        <Table
          columns={columns}
          dataSource={filteredVouchers}
          rowKey="id"
          loading={loading}
          size="small"
          scroll={{ x: 1400 }}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: total,
            showSizeChanger: true,
            showTotal: (t) => `共 ${t} 条凭证`,
            onChange: (page, size) => {
              setCurrentPage(page);
              setPageSize(size);
            },
          }}
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
