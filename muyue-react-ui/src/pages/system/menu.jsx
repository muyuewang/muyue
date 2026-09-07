import React, { useEffect, useState } from 'react'
import {
  PageContainer, ModalForm, ProFormText, ProFormRadio,
  ProFormTreeSelect, ProFormSelect, ProFormDigit
} from '@ant-design/pro-components'
import { App, Popconfirm, Space, Tag } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { listMenu, getMenu, menuTreeSelect, addMenu, updateMenu, delMenu } from '../../api/menu'

function toTreeData(nodes) {
  return (nodes || []).map((n) => ({
    title: n.label, value: n.id, key: n.id,
    children: toTreeData(n.children)
  }))
}

function toTableTree(nodes) {
  return (nodes || []).map((n) => ({
    key: n.menuId,
    menuId: n.menuId,
    menuName: n.menuName,
    icon: n.icon,
    orderNum: n.orderNum,
    path: n.path,
    component: n.component,
    perms: n.perms,
    menuType: n.menuType,
    status: n.status,
    parentId: n.parentId,
    children: n.children && n.children.length ? toTableTree(n.children) : undefined
  }))
}

const typeTag = (v) => (
  <Tag color={v === 'M' ? 'geekblue' : v === 'C' ? 'cyan' : 'default'}>
    {v === 'M' ? '目录' : v === 'C' ? '菜单' : '按钮'}
  </Tag>
)

export default function SystemMenu() {
  const { message } = App.useApp()
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [editRow, setEditRow] = useState(null)
  const [treeData, setTreeData] = useState([])

  const load = () => {
    setLoading(true)
    listMenu().then((res) => {
      setRows(toTableTree(res.data))
      setLoading(false)
    })
  }

  useEffect(load, [])

  const openForm = async (row, parentId) => {
    setEditRow(row ? { ...row } : { parentId: parentId ?? 0, menuType: 'M', isFrame: '1', isCache: '0', visible: '0', status: '0' })
    setFormOpen(true)
    const tree = await menuTreeSelect()
    setTreeData(toTreeData(tree.data))
  }

  const columns = [
    { title: '菜单名称', dataIndex: 'menuName', width: 220 },
    { title: '图标', dataIndex: 'icon', width: 90, align: 'center', render: (v) => v && v !== '#' ? v : '-' },
    { title: '排序', dataIndex: 'orderNum', width: 70, align: 'center' },
    { title: '类型', dataIndex: 'menuType', width: 80, align: 'center', render: typeTag },
    { title: '权限字符', dataIndex: 'perms', width: 180, ellipsis: true },
    { title: '组件路径', dataIndex: 'component', width: 200, ellipsis: true },
    {
      title: '状态', dataIndex: 'status', width: 80, align: 'center',
      render: (v) => <Tag color={v === '0' ? 'success' : 'default'}>{v === '0' ? '正常' : '停用'}</Tag>
    },
    {
      title: '操作', width: 180,
      render: (_, row) => (
        <Space>
          {row.menuType !== 'F' && <a onClick={() => openForm(null, row.menuId)}><PlusOutlined /> 新增</a>}
          <a onClick={() => openForm(row)}>修改</a>
          <Popconfirm title="确认删除该菜单？" onConfirm={() =>
            delMenu(row.menuId).then(() => { message.success('删除成功'); load() })
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
        <a onClick={() => openForm(null, 0)}><PlusOutlined /> 新增顶级菜单</a>
      </div>
      <a-table
        rowKey="menuId"
        columns={columns}
        dataSource={rows}
        loading={loading}
        pagination={false}
      />

      <ModalForm
        title={editRow && editRow.menuId ? '修改菜单' : '新增菜单'}
        open={formOpen}
        onOpenChange={setFormOpen}
        width={620}
        modalProps={{ destroyOnClose: true }}
        initialValues={editRow}
        key={formOpen ? (editRow && editRow.menuId ? 'e' + editRow.menuId : 'a' + (editRow && editRow.parentId)) : 'closed'}
        onFinish={async (values) => {
          if (editRow && editRow.menuId) {
            await updateMenu({ menuId: editRow.menuId, parentId: editRow.parentId, ...values })
            message.success('修改成功')
          } else {
            await addMenu({ parentId: editRow.parentId, ...values })
            message.success('新增成功')
          }
          load()
          return true
        }}
      >
        <ProFormTreeSelect
          name="parentId" label="上级菜单"
          fieldProps={{ treeData, fieldNames: { label: 'title', value: 'value', children: 'children' } }}
          rules={[{ required: true }]}
        />
        <ProFormRadio.Group
          name="menuType" label="菜单类型"
          options={[{ label: '目录', value: 'M' }, { label: '菜单', value: 'C' }, { label: '按钮', value: 'F' }]}
          rules={[{ required: true }]}
        />
        <ProFormText name="menuName" label="菜单名称" rules={[{ required: true }]} />
        <ProFormText name="icon" label="图标" placeholder="Element 图标名，如 User" />
        <ProFormDigit name="orderNum" label="显示排序" min={0} rules={[{ required: true }]} />
        <ProFormText name="perms" label="权限字符" placeholder="如 system:user:list" />
        <ProFormText name="path" label="路由地址" placeholder="如 user" />
        <ProFormText name="component" label="组件路径" placeholder="如 system/user/index" />
        <ProFormRadio.Group
          name="status" label="状态"
          options={[{ label: '正常', value: '0' }, { label: '停用', value: '1' }]}
        />
      </ModalForm>
    </PageContainer>
  )
}
