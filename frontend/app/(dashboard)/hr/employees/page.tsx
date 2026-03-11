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
import type { Employee } from '@/types/hr';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;

// 模拟员工数据
const mockEmployees: Employee[] = [
  {
    id: 1,
    employeeNo: 'EMP001',
    employeeName: '张三',
    englishName: 'Zhang San',
    gender: 1,
    birthDate: '1990-05-15',
    nation: '汉',
    idCard: '110101199005150011',
    nativePlace: '北京市',
    politicalStatus: '党员',
    maritalStatus: 2,
    education: 3,
    phone: '13800138001',
    email: 'zhangsan@nexterp.com',
    deptId: 2,
    deptName: '技术部',
    position: '高级工程师',
    hireDate: '2018-03-01',
    workStatus: 1,
    status: 1,
  },
  {
    id: 2,
    employeeNo: 'EMP002',
    employeeName: '李四',
    englishName: 'Li Si',
    gender: 1,
    birthDate: '1992-08-20',
    nation: '汉',
    idCard: '110101199208200022',
    nativePlace: '上海市',
    politicalStatus: '团员',
    maritalStatus: 1,
    education: 3,
    phone: '13800138002',
    email: 'lisi@nexterp.com',
    deptId: 2,
    deptName: '技术部',
    position: '中级工程师',
    hireDate: '2019-06-15',
    workStatus: 1,
    status: 1,
  },
  {
    id: 3,
    employeeNo: 'EMP003',
    employeeName: '王五',
    englishName: 'Wang Wu',
    gender: 2,
    birthDate: '1995-03-10',
    nation: '汉',
    idCard: '110101199503100033',
    nativePlace: '广东省',
    politicalStatus: '群众',
    maritalStatus: 1,
    education: 4,
    phone: '13800138003',
    email: 'wangwu@nexterp.com',
    deptId: 3,
    deptName: '产品部',
    position: '产品经理',
    hireDate: '2020-01-10',
    workStatus: 1,
    status: 1,
  },
];

export default function EmployeesPage() {
  const [loading, setLoading] = useState(false);
  const [employees, setEmployees] = useState<Employee[]>(mockEmployees);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState<Employee | null>(null);
  const [form] = Form.useForm();
  const [searchParams, setSearchParams] = useState({
    employeeName: '',
    deptId: undefined as number | undefined,
    workStatus: undefined as number | undefined,
  });

  // 加载员工数据
  const loadEmployees = async () => {
    setLoading(true);
    // 模拟 API 调用
    setTimeout(() => {
      setEmployees(mockEmployees);
      setLoading(false);
    }, 500);
  };

  useEffect(() => {
    loadEmployees();
  }, []);

  // 搜索
  const handleSearch = () => {
    setLoading(true);
    setTimeout(() => {
      let filtered = mockEmployees;
      if (searchParams.employeeName) {
        filtered = filtered.filter(e =>
          e.employeeName.includes(searchParams.employeeName) ||
          e.employeeNo.includes(searchParams.employeeName)
        );
      }
      if (searchParams.deptId) {
        filtered = filtered.filter(e => e.deptId === searchParams.deptId);
      }
      if (searchParams.workStatus !== undefined) {
        filtered = filtered.filter(e => e.workStatus === searchParams.workStatus);
      }
      setEmployees(filtered);
      setLoading(false);
    }, 300);
  };

  // 重置搜索
  const handleReset = () => {
    setSearchParams({
      employeeName: '',
      deptId: undefined,
      workStatus: undefined,
    });
    loadEmployees();
  };

  // 打开新增/编辑弹窗
  const handleOpenModal = (employee?: Employee) => {
    setEditingEmployee(employee || null);
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
    setEditingEmployee(null);
    form.resetFields();
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      // 模拟 API 调用
      setTimeout(() => {
        const formattedValues = {
          ...values,
          birthDate: values.birthDate?.format('YYYY-MM-DD'),
          hireDate: values.hireDate?.format('YYYY-MM-DD'),
        };

        if (editingEmployee) {
          setEmployees(prev =>
            prev.map(e => (e.id === editingEmployee.id ? { ...e, ...formattedValues } : e))
          );
          message.success('更新员工成功');
        } else {
          const newEmployee: Employee = {
            id: Math.max(...employees.map(e => e.id)) + 1,
            ...formattedValues,
            status: 1,
          };
          setEmployees(prev => [...prev, newEmployee]);
          message.success('创建员工成功');
        }
        handleCloseModal();
        setLoading(false);
      }, 500);
    } catch (error) {
      console.error(error);
    }
  };

  // 删除员工
  const handleDelete = (id: number) => {
    setLoading(true);
    setTimeout(() => {
      setEmployees(prev => prev.filter(e => e.id !== id));
      message.success('删除员工成功');
      setLoading(false);
    }, 300);
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
      render: (_: unknown, record: Employee) => (
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
            <Button icon={<ReloadOutlined />} onClick={loadEmployees}>
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
                options={[
                  { value: 2, label: '技术部' },
                  { value: 3, label: '产品部' },
                  { value: 4, label: '人力资源部' },
                  { value: 5, label: '财务部' },
                ]}
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
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>

      {/* 新增/编辑弹窗 */}
      <Modal
        title={editingEmployee ? '编辑员工' : '新增员工'}
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
                <Input placeholder="请输入工号" disabled={!!editingEmployee} />
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
                  options={[
                    { value: 2, label: '技术部' },
                    { value: 3, label: '产品部' },
                    { value: 4, label: '人力资源部' },
                    { value: 5, label: '财务部' },
                  ]}
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
