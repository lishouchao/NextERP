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
  DatePicker,
  InputNumber,
  message,
  Popconfirm,
  Tag,
  Row,
  Col,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import type { LegacyEmployee } from '@/types/hr';
import { employeeApi, departmentApi } from '@/lib/api/hr';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;

export default function LegacyEmployeesPage() {
  const [loading, setLoading] = useState(false);
  const [employees, setLegacyEmployees] = useState<LegacyEmployee[]>([]);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingLegacyEmployee, setEditingLegacyEmployee] = useState<LegacyEmployee | null>(null);
  const [form] = Form.useForm();
  const [searchParams, setSearchParams] = useState({
    employeeName: '',
    deptId: undefined as number | undefined,
    workStatus: undefined as number | undefined,
  });
  const [deptOptions, setDeptOptions] = useState<{ value: number; label: string }[]>([]);

  // 加载部门选项
  const loadDeptOptions = async () => {
    try {
      const res = await departmentApi.getList();
      const pageData = res.data;
      const depts = pageData?.records || (Array.isArray(res.data) ? res.data : []);
      setDeptOptions(depts.map((d: any) => ({ value: d.id, label: d.deptName })));
    } catch (err) {
      // silently ignore
    }
  };

  useEffect(() => {
    loadDeptOptions();
  }, []);

  // 加载员工数据
  const loadLegacyEmployees = async () => {
    setLoading(true);
    try {
      const params: any = {
        current: currentPage,
        size: pageSize,
      };
      if (searchParams.employeeName) {
        params.employeeName = searchParams.employeeName;
      }
      if (searchParams.deptId) {
        params.deptId = searchParams.deptId;
      }
      if (searchParams.workStatus !== undefined) {
        params.workStatus = searchParams.workStatus;
      }
      const res = await employeeApi.getList(params);
      const pageData = res.data;
      if (pageData) {
        setLegacyEmployees((pageData.records || []) as any);
        setTotal(pageData.total || 0);
      }
    } catch (err) {
      message.error('加载员工数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadLegacyEmployees();
  }, [currentPage, pageSize]);

  // 搜索
  const handleSearch = () => {
    setCurrentPage(1);
    loadLegacyEmployees();
  };

  // 重置搜索
  const handleReset = () => {
    setSearchParams({
      employeeName: '',
      deptId: undefined,
      workStatus: undefined,
    });
    setCurrentPage(1);
    // Re-fetch with no filters
    setTimeout(() => loadLegacyEmployees(), 0);
  };

  // 打开新增/编辑弹窗
  const handleOpenModal = (employee?: LegacyEmployee) => {
    setEditingLegacyEmployee(employee || null);
    if (employee) {
      form.setFieldsValue({
        ...employee,
        birthDate: employee.birthDate ? dayjs(employee.birthDate) : null,
        hireDate: employee.hireDate ? dayjs(employee.hireDate) : null,
      });
    } else {
      form.resetFields();
    }
    setModalVisible(true);
  };

  // 关闭弹窗
  const handleCloseModal = () => {
    setModalVisible(false);
    setEditingLegacyEmployee(null);
    form.resetFields();
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      const formattedValues = {
        ...values,
        birthDate: values.birthDate?.format('YYYY-MM-DD'),
        hireDate: values.hireDate?.format('YYYY-MM-DD'),
      };

      if (editingLegacyEmployee) {
        await employeeApi.update(editingLegacyEmployee.id, formattedValues);
        message.success('更新员工成功');
      } else {
        await employeeApi.create({ ...formattedValues, status: 1 });
        message.success('创建员工成功');
      }
      handleCloseModal();
      loadLegacyEmployees();
    } catch (error: any) {
      if (error?.response?.data?.message) {
        message.error(error.response.data.message);
      } else if (error?.message) {
        // form validation error, ignore
      } else {
        message.error('操作失败');
      }
    } finally {
      setLoading(false);
    }
  };

  // 删除员工
  const handleDelete = async (id: number) => {
    setLoading(true);
    try {
      await employeeApi.delete(id);
      message.success('删除员工成功');
      loadLegacyEmployees();
    } catch (err) {
      message.error('删除员工失败');
    } finally {
      setLoading(false);
    }
  };

  // 获取性别显示文本
  const getGenderText = (gender: number) => (gender === 1 ? '男' : '女');
  const getGenderColor = (gender: number) => (gender === 1 ? 'blue' : 'pink');

  // 获取工作状态显示文本
  const getWorkStatusText = (status: number) => {
    const map: Record<number, string> = { 1: '在职', 2: '试用', 3: '离职', 4: '停薪留职' };
    return map[status] || '未知';
  };
  const getWorkStatusColor = (status: number) => {
    const map: Record<number, string> = { 1: 'green', 2: 'orange', 3: 'red', 4: 'default' };
    return map[status] || 'default';
  };

  // 表格列定义
  const columns = [
    {
      title: '工号',
      dataIndex: 'employeeNo',
      key: 'employeeNo',
      width: 100,
    },
    {
      title: '姓名',
      dataIndex: 'employeeName',
      key: 'employeeName',
      width: 100,
    },
    {
      title: '性别',
      dataIndex: 'gender',
      key: 'gender',
      width: 60,
      render: (gender: number) => (
        <Tag color={getGenderColor(gender)}>{getGenderText(gender)}</Tag>
      ),
    },
    {
      title: '部门',
      dataIndex: 'deptName',
      key: 'deptName',
      width: 100,
    },
    {
      title: '职位',
      dataIndex: 'position',
      key: 'position',
      width: 120,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone',
      width: 120,
    },
    {
      title: '入职日期',
      dataIndex: 'hireDate',
      key: 'hireDate',
      width: 110,
    },
    {
      title: '工作状态',
      dataIndex: 'workStatus',
      key: 'workStatus',
      width: 90,
      render: (status: number) => (
        <Tag color={getWorkStatusColor(status)}>{getWorkStatusText(status)}</Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      fixed: 'right' as const,
      render: (_: unknown, record: LegacyEmployee) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleOpenModal(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除此员工吗？"
            onConfirm={() => handleDelete(record.id)}
          >
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
        title="员工管理"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadLegacyEmployees}>
              刷新
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => handleOpenModal()}>
              新增员工
            </Button>
          </Space>
        }
      >
        {/* 搜索区域 */}
        <div style={{ marginBottom: 16 }}>
          <Row gutter={16}>
            <Col span={6}>
              <Input
                placeholder="姓名/工号"
                value={searchParams.employeeName}
                onChange={(e) => setSearchParams(prev => ({ ...prev, employeeName: e.target.value }))}
                onPressEnter={handleSearch}
              />
            </Col>
            <Col span={4}>
              <Select
                placeholder="部门"
                allowClear
                style={{ width: '100%' }}
                value={searchParams.deptId}
                onChange={(value) => setSearchParams(prev => ({ ...prev, deptId: value }))}
                options={deptOptions}
              />
            </Col>
            <Col span={4}>
              <Select
                placeholder="工作状态"
                allowClear
                style={{ width: '100%' }}
                value={searchParams.workStatus}
                onChange={(value) => setSearchParams(prev => ({ ...prev, workStatus: value }))}
                options={[
                  { value: 1, label: '在职' },
                  { value: 2, label: '试用' },
                  { value: 3, label: '离职' },
                  { value: 4, label: '停薪留职' },
                ]}
              />
            </Col>
            <Col span={4}>
              <Space>
                <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                  搜索
                </Button>
                <Button onClick={handleReset}>重置</Button>
              </Space>
            </Col>
          </Row>
        </div>

        {/* 表格 */}
        <Table
          columns={columns}
          dataSource={employees}
          rowKey="id"
          loading={loading}
          scroll={{ x: 1200 }}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (t) => `共 ${t} 条`,
            onChange: (page, size) => {
              setCurrentPage(page);
              setPageSize(size);
            },
          }}
        />
      </Card>

      {/* 新增/编辑弹窗 */}
      <Modal
        title={editingLegacyEmployee ? '编辑员工' : '新增员工'}
        open={modalVisible}
        onCancel={handleCloseModal}
        onOk={handleSubmit}
        confirmLoading={loading}
        destroyOnClose
        width={720}
      >
        <Form form={form} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="employeeNo"
                label="工号"
                rules={[{ required: true, message: '请输入工号' }]}
              >
                <Input placeholder="请输入工号" disabled={!!editingLegacyEmployee} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="employeeName"
                label="姓名"
                rules={[{ required: true, message: '请输入姓名' }]}
              >
                <Input placeholder="请输入姓名" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="englishName" label="英文名">
                <Input placeholder="请输入英文名" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="gender"
                label="性别"
                rules={[{ required: true, message: '请选择性别' }]}
              >
                <Select
                  placeholder="请选择性别"
                  options={[
                    { value: 1, label: '男' },
                    { value: 2, label: '女' },
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="birthDate" label="出生日期">
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="idCard" label="身份证号">
                <Input placeholder="请输入身份证号" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="phone" label="手机号">
                <Input placeholder="请输入手机号" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="email" label="邮箱">
                <Input placeholder="请输入邮箱" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="deptId"
                label="部门"
                rules={[{ required: true, message: '请选择部门' }]}
              >
                <Select
                  placeholder="请选择部门"
                  options={deptOptions}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="position" label="职位">
                <Input placeholder="请输入职位" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="hireDate" label="入职日期">
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="workStatus"
                label="工作状态"
                initialValue={2}
              >
                <Select
                  placeholder="请选择工作状态"
                  options={[
                    { value: 1, label: '在职' },
                    { value: 2, label: '试用' },
                    { value: 3, label: '离职' },
                    { value: 4, label: '停薪留职' },
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
