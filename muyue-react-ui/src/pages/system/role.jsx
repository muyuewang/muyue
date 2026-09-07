import React, { useEffect, useRef, useState } from 'react'
import {
  PageContainer, ProTable, ModalForm, ProFormText, ProFormRadio,
  ProFormTextArea, ProFormTreeSelect
} from '@ant-design/pro-components'
import { App, Popconfirm, Space, Switch, Tag, Tree } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { listRole, getRole, addRole, updateRole, changeRoleStatus, delRole, roleMenuTree } from '../../api/role'

function toTreeData(nodes) {
  return (nodes || []).map((n) => ({ title: n.label, value: n.id, key: n.id, children: toTreeData(n.children) }))
}

export default function SystemRole() {
  const { message } = App.useApp()
  const actionRef = useRef()
  const [formOpen, setFormOpen] = useState(false)
  const [editRow, setEditRow] = useState(null)
  const [menuTree, setMenuTree] = useState([])
  const [checkedKeys, setCheckedKeys] = useState([])

  const openForm = async (row) => {
    setEditRow(row || null)
    setFormOpen(true)
    if (row) {
      const [detail, tree] = await Promise.all([getRole(row.roleId), roleMenuTree(row.roleId)])
      setMenuTree(toTreeData(tree.data.menus))
      setCheckedKeys((tree.data.checkedKeys || []).filter((k) => k < 90000000))
    } else {
      const tree = await roleMenuTree(0)
      setMenuTree(toTreeData(tree.data.menus))
      setCheckedKeys([])
    }
  }

  const columns = [
    { title: '编号', dataIndex: 'roleId', width: 70 },
    { title: '角色名称', dataIndex: 'roleName', width: 140 },
    {
      title: '权限字符', dataIndex: 'roleKey', width: 140,
      render: (v) => <Tag color="blue">{v}</Tag>
    },
    { title: '显示顺序', dataIndex: 'roleSort', width: 90, align: 'center' },
    {
      title: '状态', dataIndex: 'status', width: 90, align: 'center',
      render: (_, row) => (
        <Switch
          checked={row.status === '0'}
          onChange={(checked) =>
            changeRoleStatus(row.roleId, checked ? '0' : '1').then(() => {
              message.success('状态已更新')
              actionRef.current?.reload()
            })
          }
        />
      )
    },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    {
      title: '操作', width: 130,
      render: (_, row) => (
        <Space>
          <a onClick={() => openForm(row)}>修改</a>
          {row.roleId !== 1 && (
            <Popconfirm title="确认删除该角色？" onConfirm={() =>
              delRole(row.roleId).then(() => { message.success('删除成功'); actionRef.current?.reload() })
            }>
              <a style={{ color: 'red' }}>删除</a>
            </Popconfirm>
          )}
        </Space>
      )
    }
  ]

  return (
    <PageContainer>
      <ProTable
        rowKey="roleId"
        actionRef={actionRef}
        columns={columns}
        cardBordered
        search={{ labelWidth: 'auto' }}
        pagination={{ defaultPageSize: 10 }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params
          const res = await listRole({ ...rest, pageNum: current, pageSize })
          return { data: res.rows, total: res.total, success: true }
        }}
        toolBarRender={() => [
          <a key="add" onClick={() => openForm(null)}><PlusOutlined /> 新增角色</a>
        ]}
      />

      <ModalForm
        title={editRow ? '修改角色' : '新增角色'}
        open={formOpen}
        onOpenChange={setFormOpen}
        width={640}
        modalProps={{ destroyOnClose: true }}
        onFinish={async (values) => {
          const payload = { ...values, menuIds: checkedKeys }
          if (editRow) {
            await updateRole({ roleId: editRow.roleId, ...payload })
            message.success('修改成功')
          } else {
            await addRole(payload)
            message.success('新增成功')
          }
          actionRef.current?.reload()
          return true
        }}
      >
        <ProFormText name="roleName" label="角色名称" rules={[{ required: true }]} />
        <ProFormText
          name="roleKey" label="权限字符"
          rules={[{ required: true }, { pattern: /^[a-zA-Z0-9_:]+$/, message: '仅限字母数字与下划线' }]}
        />
        <ProFormText name="roleSort" label="显示顺序" placeholder="数字越小越靠前" rules={[{ required: true }]} />
        <ProFormRadio.Group
          name="status" label="状态"
          options={[{ label: '正常', value: '0' }, { label: '停用', value: '1' }]}
        />
        <ProFormTextArea name="remark" label="备注" />
        <div style={{ marginBottom: 8, fontWeight: 600 }}>菜单权限</div>
        <Tree
          checkable
          defaultExpandAll
          treeData={menuTree}
          checkedKeys={checkedKeys}
          onCheck={(keys) => setCheckedKeys(keys)}
        />
      </ModalForm>
    </PageContainer>
  )
}
