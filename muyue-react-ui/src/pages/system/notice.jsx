import React, { useEffect, useRef, useState } from 'react'
import {
  PageContainer,
  ProTable,
  ProForm,
  ModalForm,
  ProFormText,
  ProFormTextArea,
  ProFormRadio
} from '@ant-design/pro-components'
import { App, Button, Descriptions, Modal, Popconfirm, Space, Tag } from 'antd'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons'
import {
  listNotice,
  getNotice,
  addNotice,
  updateNotice,
  delNotice
} from '../../api/notice'

const typeMap = { 1: { label: '通知', color: 'warning' }, 2: { label: '公告', color: 'success' } }

export default function SystemNotice() {
  const { message, modal } = App.useApp()
  const actionRef = useRef()
  const [formOpen, setFormOpen] = useState(false)
  const [editRow, setEditRow] = useState(null)

  const columns = [
    { title: '编号', dataIndex: 'noticeId', width: 80 },
    {
      title: '类型',
      dataIndex: 'noticeType',
      width: 90,
      align: 'center',
      render: (v) => <Tag color={typeMap[v]?.color}>{typeMap[v]?.label || v}</Tag>
    },
    { title: '标题', dataIndex: 'noticeTitle', ellipsis: true },
    { title: '发布人', dataIndex: 'createBy', width: 110 },
    { title: '发布时间', dataIndex: 'createTime', width: 170 },
    {
      title: '操作',
      width: 160,
      render: (_, row) => (
        <Space>
          <a onClick={() => handleView(row)}>详情</a>
          <a
            onClick={() => {
              setFormOpen(true)
              setEditRow(row)
            }}
          >
            修改
          </a>
          <Popconfirm title="确认删除该公告？" onConfirm={() => handleDelete([row.noticeId])}>
            <a style={{ color: 'red' }}>删除</a>
          </Popconfirm>
        </Space>
      )
    }
  ]

  const handleView = (row) => {
    getNotice(row.noticeId).then((res) => {
      setView(res.data)
      modal.info({
        title: res.data.noticeTitle,
        width: 640,
        content: (
          <div>
            <Descriptions column={2} size="small" style={{ marginTop: 16 }}>
              <Descriptions.Item label="类型">
                <Tag color={typeMap[res.data.noticeType]?.color}>
                  {typeMap[res.data.noticeType]?.label}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="发布时间">{res.data.createTime}</Descriptions.Item>
            </Descriptions>
            <div style={{ marginTop: 16, whiteSpace: 'pre-wrap', lineHeight: 1.8 }}>
              {res.data.noticeContent}
            </div>
          </div>
        )
      })
    })
  }

  const handleDelete = (ids) => {
    delNotice(ids.join(',')).then(() => {
      message.success('删除成功')
      actionRef.current?.reload()
    })
  }

  return (
    <PageContainer>
      <ProTable
        rowKey="noticeId"
        actionRef={actionRef}
        columns={columns}
        cardBordered
        search={false}
        pagination={{ defaultPageSize: 10 }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params
          const res = await listNotice({ ...rest, pageNum: current, pageSize })
          return { data: res.rows, total: res.total, success: true }
        }}
        toolBarRender={() => [
          <Button
            key="add"
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => {
              setEditRow(null)
              setFormOpen(true)
            }}
          >
            发布公告
          </Button>
        ]}
      />

      <NoticeForm
        editRow={editRow}
        open={formOpen}
        setOpen={setFormOpen}
        onDone={() => actionRef.current?.reload()}
      />

    </PageContainer>
  )
}

function NoticeForm({ open, setOpen, editRow, onDone }) {
  const { message } = App.useApp()
  const [form] = ProForm.useForm()

  useEffect(() => {
    if (open) {
      if (editRow) {
        getNotice(editRow.noticeId).then((res) => {
          form.setFieldsValue(res.data)
        })
      } else {
        form.resetFields()
        form.setFieldsValue({ noticeType: '1', status: '0' })
      }
    }
  }, [open])

  return (
    <ModalForm
      title={editRow ? '修改公告' : '发布公告'}
      open={open}
      form={form}
      width={640}
      modalProps={{ destroyOnClose: true }}
      onOpenChange={setOpen}
      onFinish={async (values) => {
        const action = editRow ? updateNotice({ ...editRow, ...values }) : addNotice(values)
        await action
        message.success(editRow ? '修改成功' : '发布成功')
        onDone()
        return true
      }}
    >
      <ProFormRadio.Group
        name="noticeType"
        label="公告类型"
        options={[
          { label: '通知', value: '1' },
          { label: '公告', value: '2' }
        ]}
        rules={[{ required: true }]}
      />
      <ProFormText
        name="noticeTitle"
        label="标题"
        rules={[{ required: true, message: '标题不能为空' }]}
      />
      <ProFormRadio.Group
        name="status"
        label="状态"
        options={[
          { label: '正常', value: '0' },
          { label: '关闭', value: '1' }
        ]}
      />
      <ProFormTextArea
        name="noticeContent"
        label="内容"
        fieldProps={{ rows: 8 }}
        rules={[{ required: true, message: '内容不能为空' }]}
      />
    </ModalForm>
  )
}
