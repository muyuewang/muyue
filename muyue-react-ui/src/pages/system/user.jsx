import React, { useRef, useState } from 'react'
import {
  PageContainer,
  ProTable,
  ProForm,
  ModalForm,
  ProFormText,
  ProFormSelect,
  ProFormRadio,
  ProFormTreeSelect,
  ProFormTextArea
} from '@ant-design/pro-components'
import { App, Card, Col, Popconfirm, Row, Space, Tag, Tree } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { listUser, getUser, addUser, updateUser, delUser, deptTree } from '../../api/user'

/** 后端 deptTree（{code,data:[{id,label,children}]}）→ antd TreeData */
function toTreeData(nodes) {
  return (nodes || []).map((n) => ({ title: n.label, value: n.id, children: toTreeData(n.children) }))
}

const statusTag = (v) => <Tag color={v === '0' ? 'success' : 'default'}>{v === '0' ? '正常' : '停用'}</Tag>

export default function SystemUser() {
  const { message } = App.useApp()
  const actionRef = useRef()
  const [deptId, setDeptId] = useState(undefined)
  const [treeData, setTreeData] = React.useState([])
  const [formOpen, setFormOpen] = useState(false)
  const [editRow, setEditRow] = useState(null)

  React.useEffect(() => {
    deptTree().then((res) => setTreeData(toTreeData(res.data)))
  }, [])

  const columns = [
    { title: '编号', dataIndex: 'userId', width: 70 },
    { title: '账号', dataIndex: 'userName', width: 110, ellipsis: true },
    { title: '昵称', dataIndex: 'nickName', width: 110, ellipsis: true },
    { title: '部门', dataIndex: ['dept', 'deptName'], width: 130, ellipsis: true },
    { title: '手机号', dataIndex: 'phonenumber', width: 130 },
    { title: '状态', dataIndex: 'status', width: 80, align: 'center', render: statusTag },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    {
      title: '操作',
      width: 150,
      fixed: 'right',
      render: (_, row) => (
        <Space>
          <a onClick={async () => {
            const res = await getUser(row.userId)
            setEditRow(res.data)
            setFormOpen(true)
          }}>修改</a>
          <Popconfirm title="确认删除该用户？" onConfirm={() =>
            delUser(row.userId).then(() => { message.success('删除成功'); actionRef.current?.reload() })
          }>
            <a style={{ color: 'red' }}>删除</a>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <PageContainer>
      <Row gutter={16}>
        <Col xs={24} md={5}>
          <Card size="small" title="部门列表" style={{ minHeight: 480 }}>
            <Tree
              key={treeData.length}
              treeData={treeData}
              defaultExpandAll
              selectedKeys={deptId ? [deptId] : []}
              onSelect={(keys) => {
                setDeptId(keys[0])
                actionRef.current?.reload()
              }}
            />
          </Card>
        </Col>
        <Col xs={24} md={19}>
          <ProTable
            rowKey="userId"
            actionRef={actionRef}
            columns={columns}
            cardBordered
            scroll={{ x: 1000 }}
            pagination={{ defaultPageSize: 10 }}
            params={{ deptId }}
            request={async (params) => {
              const { current, pageSize, ...rest } = params
              const res = await listUser({ ...rest, pageNum: current, pageSize })
              return { data: res.rows, total: res.total, success: true }
            }}
            toolBarRender={() => [
              <a key="add" onClick={() => { setEditRow(null); setFormOpen(true) }}>
                <PlusOutlined /> 新增用户
              </a>
            ]}
          />
        </Col>
      </Row>

      <UserForm
        open={formOpen}
        setOpen={setFormOpen}
        editRow={editRow}
        treeData={treeData}
        onDone={() => actionRef.current?.reload()}
      />
    </PageContainer>
  )
}

function UserForm({ open, setOpen, editRow, treeData, onDone }) {
  const { message } = App.useApp()
  const [form] = ProForm.useForm()

  React.useEffect(() => {
    if (open) {
      if (editRow) {
        form.setFieldsValue(editRow)
      } else {
        form.resetFields()
        form.setFieldsValue({ status: '0', sex: '0' })
      }
    }
  }, [open])

  return (
    <ModalForm
      title={editRow ? '修改用户' : '新增用户'}
      open={open}
      form={form}
      width={560}
      modalProps={{ destroyOnClose: true }}
      onOpenChange={setOpen}
      onFinish={async (values) => {
        if (editRow) {
          await updateUser({ userId: editRow.userId, ...values })
          message.success('修改成功')
        } else {
          await addUser(values)
          message.success('新增成功')
        }
        onDone()
        return true
      }}
    >
      <ProFormText name="userName" label="账号" rules={[{ required: true }]} />
      <ProFormText name="nickName" label="昵称" rules={[{ required: true }]} />
      {!editRow && (
        <ProFormText.Password name="password" label="密码" rules={[{ required: true }]} />
      )}
      <ProFormText name="phonenumber" label="手机号" />
      <ProFormText name="email" label="邮箱" />
      <ProFormRadio.Group
        name="sex"
        label="性别"
        options={[{ label: '男', value: '0' }, { label: '女', value: '1' }]}
      />
      <ProFormTreeSelect
        name="deptId"
        label="部门"
        fieldProps={{ treeData, fieldNames: { label: 'title', value: 'value', children: 'children' } }}
        rules={[{ required: true, message: '请选择部门' }]}
      />
      <ProFormRadio.Group
        name="status"
        label="状态"
        options={[{ label: '正常', value: '0' }, { label: '停用', value: '1' }]}
      />
      <ProFormTextArea name="remark" label="备注" />
    </ModalForm>
  )
}
