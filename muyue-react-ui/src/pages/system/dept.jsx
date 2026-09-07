import React, { useRef, useState } from 'react'
import {
  PageContainer, ModalForm, ProFormText, ProFormRadio,
  ProFormTreeSelect, ProFormTextArea
} from '@ant-design/pro-components'
import { App, Popconfirm, Space, Tag } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { listDept, getDept, addDept, updateDept, delDept, deptTreeSelect } from '../../api/dept'

function toTreeData(nodes) {
  return (nodes || []).map((n) => ({
    title: n.label, value: n.id, key: n.id,
    children: toTreeData(n.children)
  }))
}

/** 列表 → 树表数据 */
function toTableTree(nodes) {
  return (nodes || []).map((n) => ({
    key: n.deptId,
    deptId: n.deptId,
    deptName: n.deptName,
    orderNum: n.orderNum,
    leader: n.leader,
    phone: n.phone,
    email: n.email,
    status: n.status,
    createTime: n.createTime,
    children: n.children && n.children.length ? toTableTree(n.children) : undefined
  }))
}

export default function SystemDept() {
  const { message } = App.useApp()
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [editRow, setEditRow] = useState(null)
  const [treeData, setTreeData] = useState([])

  const load = () => {
    setLoading(true)
    listDept().then((res) => {
      setRows(toTableTree(res.data))
      setLoading(false)
    })
  }

  React.useEffect(load, [])

  const openForm = async (row, parentId) => {
    setEditRow(row ? { ...row, parentId: row.parentId } : { parentId: parentId ?? 0 })
    setFormOpen(true)
    const tree = await deptTreeSelect()
    setTreeData(toTreeData(tree.data))
    if (row) {
      const detail = await getDept(row.deptId)
      setEditRow(detail.data)
    }
  }

  const columns = [
    { title: '部门名称', dataIndex: 'deptName', width: 260 },
    { title: '排序', dataIndex: 'orderNum', width: 80, align: 'center' },
    { title: '负责人', dataIndex: 'leader', width: 110 },
    { title: '联系电话', dataIndex: 'phone', width: 140 },
    { title: '邮箱', dataIndex: 'email', ellipsis: true },
    {
      title: '状态', dataIndex: 'status', width: 90, align: 'center',
      render: (v) => <Tag color={v === '0' ? 'success' : 'default'}>{v === '0' ? '正常' : '停用'}</Tag>
    },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    {
      title: '操作', width: 180,
      render: (_, row) => (
        <Space>
          <a onClick={() => openForm(null, row.deptId)}><PlusOutlined /> 新增</a>
          <a onClick={() => openForm(row)}>修改</a>
          <Popconfirm title="确认删除该部门？" onConfirm={() =>
            delDept(row.deptId).then(() => { message.success('删除成功'); load() })
          }>
            <a style={{ color: 'red' }}>删除</a>
          </Popconfirm>
        </Space>
      )
    }
  ]

  return (
    <PageContainer>
      <div style={{ marginBottom: 12 }}>
        <a onClick={() => openForm(null, 0)}><PlusOutlined /> 新增顶级部门</a>
      </div>
      <a-table
        rowKey="deptId"
        columns={columns}
        dataSource={rows}
        loading={loading}
        pagination={false}
        defaultExpandAllRows
      />

      <ModalForm
        title={editRow && editRow.deptId ? '修改部门' : '新增部门'}
        open={formOpen}
        onOpenChange={setFormOpen}
        width={560}
        modalProps={{ destroyOnClose: true }}
        initialValues={editRow}
        key={formOpen ? (editRow && editRow.deptId ? 'e' + editRow.deptId : 'a' + (editRow && editRow.parentId)) : 'closed'}
        onFinish={async (values) => {
          if (editRow && editRow.deptId) {
            await updateDept({ deptId: editRow.deptId, parentId: editRow.parentId, ...values })
            message.success('修改成功')
          } else {
            await addDept({ parentId: editRow.parentId, ...values })
            message.success('新增成功')
          }
          load()
          return true
        }}
      >
        <ProFormTreeSelect
          name="parentId" label="上级部门"
          fieldProps={{ treeData, fieldNames: { label: 'title', value: 'value', children: 'children' } }}
          rules={[{ required: true, message: '请选择上级部门' }]}
        />
        <ProFormText name="deptName" label="部门名称" rules={[{ required: true }]} />
        <ProFormText name="orderNum" label="显示排序" rules={[{ required: true }]} />
        <ProFormText name="leader" label="负责人" />
        <ProFormText name="phone" label="联系电话" />
        <ProFormText name="email" label="邮箱" />
        <ProFormRadio.Group
          name="status" label="状态"
          options={[{ label: '正常', value: '0' }, { label: '停用', value: '1' }]}
        />
      </ModalForm>
    </PageContainer>
  )
}
