import React, { useEffect, useRef, useState } from 'react'
import {
  PageContainer,
  ProTable,
  ProForm,
  ModalForm,
  ProFormText,
  ProFormTextArea,
  ProFormRadio,
  ProFormSelect
} from '@ant-design/pro-components'
import { App, Button, Descriptions, Modal, Popconfirm, Space, Tag, Typography } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import {
  listNotice,
  getNotice,
  addNotice,
  updateNotice,
  delNotice
} from '../../api/notice'
import { notifyUnread } from '../../utils/noticeBus'

const { Text } = Typography
const typeMap = { 1: { label: '通知', color: 'warning' }, 2: { label: '公告', color: 'success' } }

export default function SystemNotice() {
  const { message, modal } = App.useApp()
  const actionRef = useRef()
  const [formOpen, setFormOpen] = useState(false)
  const [editRow, setEditRow] = useState(null)

  const openDetail = (row) => {
    getNotice(row.noticeId).then((res) => {
      const notice = res.data
      modal.info({
        title: notice.noticeTitle,
        width: 680,
        content: (
          <div>
            <Descriptions column={2} size="small" style={{ marginTop: 16 }}>
              <Descriptions.Item label="类型">
                <Tag color={typeMap[notice.noticeType]?.color}>
                  {typeMap[notice.noticeType]?.label}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="发布时间">{notice.createTime}</Descriptions.Item>
              <Descriptions.Item label="发布人">{notice.createBy}</Descriptions.Item>
              <Descriptions.Item label="状态">
                {notice.status === '0' ? '正常' : '已关闭'}
              </Descriptions.Item>
            </Descriptions>
            <div
              style={{
                marginTop: 16,
                padding: 16,
                background: '#f7f8fa',
                borderRadius: 8,
                whiteSpace: 'pre-wrap',
                lineHeight: 1.8,
                minHeight: 120
              }}
            >
              {notice.noticeContent}
            </div>
          </div>
        )
      })
      // 后端"查看即标记已读"：刷新列表中的已读状态 + 立即联动铃铛角标
      actionRef.current?.reload()
      notifyUnread()
    })
  }

  const handleDelete = (ids) => {
    delNotice(ids.join(',')).then(() => {
      message.success('删除成功')
      actionRef.current?.reload()
      notifyUnread()
    })
  }

  const columns = [
    {
      title: '类型',
      dataIndex: 'noticeType',
      width: 90,
      align: 'center',
      render: (v) => <Tag color={typeMap[v]?.color}>{typeMap[v]?.label || v}</Tag>
    },
    {
      title: '标题',
      dataIndex: 'noticeTitle',
      ellipsis: true,
      render: (v, row) => (
        <Space>
          {row.is_read === 0 && <Badge />}
          <Text strong={row.is_read === 0}>{v}</Text>
        </Space>
      )
    },
    {
      title: '已读状态',
      dataIndex: 'is_read',
      width: 100,
      align: 'center',
      render: (v) =>
        v === 0 ? <Tag color="error">未读</Tag> : <Tag color="default">已读</Tag>
    },
    { title: '发布人', dataIndex: 'createBy', width: 110 },
    { title: '发布时间', dataIndex: 'createTime', width: 170 },
    {
      title: '操作',
      width: 150,
      render: (_, row) => (
        <Space>
          <a onClick={() => openDetail(row)}>{row.is_read === 0 ? '阅读' : '详情'}</a>
          <a
            onClick={() => {
              setEditRow(row)
              setFormOpen(true)
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

  return (
    <PageContainer>
      <ProTable
        rowKey="noticeId"
        actionRef={actionRef}
        columns={columns}
        cardBordered
        search={{ labelWidth: 'auto' }}
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
        onDone={() => {
          actionRef.current?.reload()
          notifyUnread()
        }}
      />
    </PageContainer>
  )
}

function Badge() {
  return <span style={{ width: 8, height: 8, background: '#ff4d4f', borderRadius: '50%', display: 'inline-block' }} />
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
