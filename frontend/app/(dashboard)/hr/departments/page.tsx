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
  InputNumber,
  message,
  Popconfirm,
  Tree,
  Empty,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { Department } from '@/types/hr';

// 模拟数据
const mockDepartments: Department[] = [
  { id: 1, deptName: '总公司', deptCode: 'HQ', parentId: null, sort: 1, status: 1, remark: '总部' },
  { id: 2, deptName: '技术部', deptCode: 'TECH', parentId: 1, sort: 1, status: 1, remark: '技术研发' },
  { id: 3, deptName: '产品部', deptCode: 'PROD', parentId: 1, sort: 2, status: 1, remark: '产品管理' },
  { id: 4, deptName: '人力资源部', deptCode: 'HR', parentId: 1, sort: 3, status: 1, remark: '人力资源管理' },
  { id: 5, deptName: '财务部', deptCode: 'FIN', parentId: 1, sort: 4, status: 1, remark: '财务管理' },
  { id: 6, deptName: '前端组', deptCode: 'FE', parentId: 2, sort: 1, status: 1, remark: '前端开发' },
  { id: 7, deptName: '后端组', deptCode: 'BE', parentId: 2, sort: 2, status: 1, remark: '后端开发' },
];

export default function DepartmentsPage() {
  const [loading, setLoading] = useState(false);
  const [departments, setDepartments] = useState<Department[]>(mockDepartments);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingDept, setEditingDept] = useState<Department | null>(null);
  const [form] = Form.useForm();

  // 加载部门数据
  const loadDepartments = async () => {
    setLoading(true);
    // 模拟 API 调用
    setTimeout(() => {
      setDepartments(mockDepartments);
      setLoading(false);
    }, 500);
  };

  useEffect(() => {
    loadDepartments();
  }, []);

  // 打开新增/编辑弹窗
  const handleOpenModal = (dept?: Department) => {
    setEditingDept(dept || null);
    if (dept) {
      form.setFieldsValue({
        deptName: dept.deptName,
        deptCode: dept.deptCode,
        parentId: dept.parentId,
        sort: dept.sort,
        remark: dept.remark,
      });
    } else {
      form.resetFields();
    }
    setModalVisible(true);
  };

  // 关闭弹窗
  const handleCloseModal = () => {
    setModalVisible(false);
    setEditingDept(null);
    form.resetFields();
  };

  // 提交表单
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setLoading(true);

      // 模拟 API 调用
      setTimeout(() => {
        if (editingDept) {
          // 更新
          setDepartments(prev =>
            prev.map(d => (d.id === editingDept.id ? { ...d, ...values } : d))
          );
          message.success('更新部门成功');
        } else {
          // 新增
          const newDept: Department = {
            id: Math.max(...departments.map(d => d.id)) + 1,
            ...values,
            status: 1,
          };
          setDepartments(prev => [...prev, newDept]);
          message.success('创建部门成功');
        }
        handleCloseModal();
        setLoading(false);
      }, 500);
    } catch (error) {
      console.error(error);
    }
  };

  // 删除部门
  const handleDelete = (id: number) => {
    setLoading(true);
    // 检查是否有子部门
    const hasChildren = departments.some(d => d.parentId === id);
    if (hasChildren) {
      message.error('该部门下有子部门，无法删除');
      setLoading(false);
      return;
    }

    setTimeout(() => {
      setDepartments(prev => prev.filter(d => d.id !== id));
      message.success('删除部门成功');
      setLoading(false);
    }, 300);
  };

  // 将扁平数据转换为树形结构
  const buildTreeData = (items: Department[]): any[] => {
    const map = new Map<number, any[]>();
    const roots: any[] = [];

    items.forEach((item) => {
      const node = {
        key: item.id,
        title: item.deptName,
        value: item,
        children: [],
      };
      map.set(item.id, node as unknown as any[]);
    });

    items.forEach((item) => {
      const node = map.get(item.id);
      if (item.parentId && map.has(item.parentId)) {
        map.get(item.parentId)![0].children.push(node);
      } else {
        roots.push(node);
      }
    });

    return roots;
  };

  const treeData = buildTreeData(departments);

  // 表格列定义
  const columns = [
    {
      title: '部门编码',
      dataIndex: 'deptCode',
      key: 'deptCode',
      width: 120,
    },
    {
      title: '部门名称',
      dataIndex: 'deptName',
      key: 'deptName',
    },
    {
      title: '排序',
      dataIndex: 'sort',
      key: 'sort',
      width: 80,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (status: number) => (status === 1 ? '启用' : '禁用'),
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      render: (_: unknown, record: Department) => (
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
            title="确定要删除此部门吗？"
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
        title="部门管理"
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={loadDepartments}>
              刷新
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => handleOpenModal()}>
              新增部门
            </Button>
          </Space>
        }
      >
        <div style={{ display: 'flex', gap: 24 }}>
          {/* 左侧树形结构 */}
          <div style={{ width: 280, borderRight: '1px solid #f0f0f0', paddingRight: 24, minHeight: 400 }}>
            <div style={{ marginBottom: 16, fontWeight: 'bold' }}>组织架构</div>
            {treeData.length > 0 ? (
              <Tree
                showLine
                defaultExpandAll
                treeData={treeData}
                onSelect={(selectedKeys, info) => {
                  if (info.selected && info.node.value) {
                    handleOpenModal(info.node.value);
                  }
                }}
              />
            ) : (
              <Empty description="暂无部门数据" />
            )}
          </div>

          {/* 右侧表格 */}
          <div style={{ flex: 1 }}>
            <Table
              columns={columns}
              dataSource={departments}
              rowKey="id"
              loading={loading}
              pagination={false}
              size="middle"
            />
          </div>
        </div>
      </Card>

      {/* 新增/编辑弹窗 */}
      <Modal
        title={editingDept ? '编辑部门' : '新增部门'}
        open={modalVisible}
        onCancel={handleCloseModal}
        onOk={handleSubmit}
        confirmLoading={loading}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="deptName"
            label="部门名称"
            rules={[{ required: true, message: '请输入部门名称' }]}
          >
            <Input placeholder="请输入部门名称" />
          </Form.Item>
          <Form.Item
            name="deptCode"
            label="部门编码"
            rules={[{ required: true, message: '请输入部门编码' }]}
          >
            <Input placeholder="请输入部门编码" disabled={!!editingDept} />
          </Form.Item>
          <Form.Item name="parentId" label="上级部门">
            <InputNumber placeholder="上级部门ID" style={{ width: '100%' }} min={1} />
          </Form.Item>
          <Form.Item name="sort" label="排序" initialValue={1}>
            <InputNumber placeholder="排序号" style={{ width: '100%' }} min={1} />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea placeholder="请输入备注" rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
