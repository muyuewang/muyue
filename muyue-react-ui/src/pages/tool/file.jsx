import React, { useRef } from 'react'
import { PageContainer, ProTable } from '@ant-design/pro-components'
import { App, Popconfirm, Space, Tag, Upload } from 'antd'
import { UploadOutlined } from '@ant-design/icons'
import request from '../../utils/request'
import { listFiles, filePreview, fileDownload } from '../../api/tool'
import Auth from '../../components/Auth'

const fmtSize = (v) => {
  const n = Number(v) || 0
  if (n > 1024 * 1024) return (n / 1024 / 1024).toFixed(1) + ' MB'
  if (n > 1024) return (n / 1024).toFixed(1) + ' KB'
  return n + ' B'
}

export default function ToolFile() {
  const { message } = App.useApp()
  const actionRef = useRef()

  const doUpload = async ({ file, onSuccess, onError }) => {
    const form = new FormData()
    form.append('file', file)
    try {
      await request({ url: '/tool/file/upload', method: 'post', data: form })
      onSuccess()
      message.success('上传成功')
      actionRef.current?.reload()
    } catch (e) {
      onError(e)
      message.error(e.message || '上传失败')
    }
  }

  return (
    <PageContainer>
      <ProTable
        rowKey="fileId"
        actionRef={actionRef}
        cardBordered
        search={{ labelWidth: 'auto' }}
        scroll={{ x: 1000 }}
        pagination={{ defaultPageSize: 10 }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params
          const res = await listFiles({ ...rest, pageNum: current, pageSize })
          return { data: res.rows, total: res.total, success: true }
        }}
        columns={[
          { title: '编号', dataIndex: 'fileId', width: 70 },
          { title: '文件名', dataIndex: 'fileName', ellipsis: true },
          {
            title: '类型', dataIndex: 'fileType', width: 90, align: 'center',
            render: (v) => <Tag>{v}</Tag>
          },
          { title: '大小', dataIndex: 'fileSize', width: 100, render: fmtSize },
          { title: '上传人', dataIndex: 'uploadBy', width: 110 },
          { title: '上传时间', dataIndex: 'createTime', width: 170 },
          {
            title: '操作', width: 130,
            render: (_, row) => (
              <Space>
                <a onClick={() => filePreview(row.fileId)}>预览</a>
                <a onClick={() => fileDownload(row.fileId, row.fileName)}>下载</a>
                <Auth permi="tool:file:remove">
                  <Popconfirm title="确认删除该附件？" onConfirm={() =>
                    request({ url: '/tool/file/' + row.fileId, method: 'delete' })
                      .then(() => { message.success('删除成功'); actionRef.current?.reload() })
                  }>
                    <a style={{ color: 'red' }}>删除</a>
                  </Popconfirm>
                </Auth>
              </Space>
            )
          }
        ]}
        toolBarRender={() => [
          <Auth key="upload" permi="tool:file:upload">
            <Upload customRequest={doUpload} showUploadList={false}>
              <a><UploadOutlined /> 上传附件</a>
            </Upload>
          </Auth>
        ]}
      />
    </PageContainer>
  )
}
