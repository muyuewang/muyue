import React, { useRef, useState } from 'react'
import {
  PageContainer, ProTable, ModalForm,
  ProFormText, ProFormDigit, ProFormRadio, ProFormTextArea
} from '@ant-design/pro-components'
import { App, Popconfirm, Space, Tag } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { listPost, getPost, addPost, updatePost, delPost } from '../../api/post'
import Auth from '../../components/Auth'

export default function SystemPost() {
  const { message } = App.useApp()
  const actionRef = useRef()
  const [formOpen, setFormOpen] = useState(false)
  const [editRow, setEditRow] = useState(null)

  const columns = [
    { title: '编号', dataIndex: 'postId', width: 70 },
    { title: '岗位名称', dataIndex: 'postName', width: 140 },
    { title: '岗位编码', dataIndex: 'postCode', width: 140, render: (v) => <Tag color="blue">{v}</Tag> },
    { title: '排序', dataIndex: 'postSort', width: 80, align: 'center' },
    {
      title: '状态', dataIndex: 'status', width: 90, align: 'center',
      render: (v) => <Tag color={v === '0' ? 'success' : 'default'}>{v === '0' ? '正常' : '停用'}</Tag>
    },
    { title: '创建时间', dataIndex: 'createTime', width: 170 },
    {
      title: '操作', width: 130,
      render: (_, row) => (
        <Space>
          <Auth permi="system:post:edit">
            <a onClick={async () => { const res = await getPost(row.postId); setEditRow(res.data); setFormOpen(true) }}>修改</a>
          </Auth>
          <Auth permi="system:post:remove">
            <Popconfirm title="确认删除该岗位？" onConfirm={() =>
              delPost(row.postId).then(() => { message.success('删除成功'); actionRef.current?.reload() })
            }>
              <a style={{ color: 'red' }}>删除</a>
            </Popconfirm>
          </Auth>
        </Space>
      )
    }
  ]

  return (
    <PageContainer>
      <ProTable
        rowKey="postId"
        actionRef={actionRef}
        columns={columns}
        cardBordered
        search={{ labelWidth: 'auto' }}
        pagination={{ defaultPageSize: 10 }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params
          const res = await listPost({ ...rest, pageNum: current, pageSize })
          return { data: res.rows, total: res.total, success: true }
        }}
        toolBarRender={() => [
          <Auth key="add" permi="system:post:add">
            <a onClick={() => { setEditRow(null); setFormOpen(true) }}><PlusOutlined /> 新增岗位</a>
          </Auth>
        ]}
      />

      <ModalForm
        title={editRow ? '修改岗位' : '新增岗位'}
        open={formOpen}
        onOpenChange={setFormOpen}
        width={520}
        modalProps={{ destroyOnClose: true }}
        key={formOpen ? (editRow ? 'e' + editRow.postId : 'a') : 'closed'}
        initialValues={editRow || { status: '0', postSort: 0 }}
        onFinish={async (values) => {
          if (editRow) {
            await updatePost({ postId: editRow.postId, ...values })
            message.success('修改成功')
          } else {
            await addPost(values)
            message.success('新增成功')
          }
          actionRef.current?.reload()
          return true
        }}
      >
        <ProFormText name="postName" label="岗位名称" rules={[{ required: true }]} />
        <ProFormText name="postCode" label="岗位编码" rules={[{ required: true }]} />
        <ProFormDigit name="postSort" label="显示顺序" min={0} rules={[{ required: true }]} />
        <ProFormRadio.Group
          name="status" label="状态"
          options={[{ label: '正常', value: '0' }, { label: '停用', value: '1' }]}
        />
        <ProFormTextArea name="remark" label="备注" />
      </ModalForm>
    </PageContainer>
  )
}
