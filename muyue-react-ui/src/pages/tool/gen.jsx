import React, { useRef, useState } from 'react'
import { PageContainer, ProTable } from '@ant-design/pro-components'
import { App, Modal, Space, Tabs, Typography } from 'antd'
import { listGenTables, previewGen, genDownload } from '../../api/tool'
import Auth from '../../components/Auth'

const { Text, Paragraph } = Typography

export default function ToolGen() {
  const { message } = App.useApp()
  const actionRef = useRef()
  const [preview, setPreview] = useState(null) // { tableName, files: Map }

  const showPreview = (tableName) => {
    previewGen(tableName).then((res) => setPreview({ tableName, files: res.data }))
  }

  return (
    <PageContainer>
      <ProTable
        rowKey="tableName"
        actionRef={actionRef}
        cardBordered
        search={{ labelWidth: 'auto' }}
        pagination={{ defaultPageSize: 10 }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params
          const res = await listGenTables({ ...rest, pageNum: current, pageSize })
          return { data: res.rows, total: res.total, success: true }
        }}
        columns={[
          { title: '表名', dataIndex: 'tableName', width: 200 },
          { title: '实体类', dataIndex: 'className', width: 160 },
          { title: '业务名', dataIndex: 'businessName', width: 140 },
          { title: '主键', dataIndex: 'pkColumn', render: (v) => (v ? v.columnName : '-') },
          { title: '字段数', dataIndex: 'columnCount', width: 90, align: 'center' },
          {
            title: '操作', width: 140,
            render: (_, row) => (
              <Space>
                <Auth permi="tool:gen:query">
                  <a onClick={() => showPreview(row.tableName)}>预览</a>
                </Auth>
                <Auth permi="tool:gen:code">
                  <a onClick={() =>
                    genDownload(row.tableName, row.tableName + '-code.zip')
                      .then(() => message.success('下载成功'))
                      .catch((e) => message.error(e.message))
                  }>
                    下载
                  </a>
                </Auth>
              </Space>
            )
          }
        ]}
      />

      <Modal
        open={!!preview}
        title={`代码预览 - ${preview?.tableName}`}
        width={900}
        footer={null}
        onCancel={() => setPreview(null)}
      >
        {preview && (
          <Tabs
            items={Object.entries(preview.files).map(([name, content]) => ({
              key: name,
              label: <Text style={{ fontSize: 12 }}>{name}</Text>,
              children: (
                <pre
                  style={{
                    maxHeight: 480, overflow: 'auto', fontSize: 12, lineHeight: 1.6,
                    background: '#0d1117', color: '#c9d1d9', padding: 16, borderRadius: 8
                  }}
                >
                  {content}
                </pre>
              )
            }))}
          />
        )}
      </Modal>
    </PageContainer>
  )
}
