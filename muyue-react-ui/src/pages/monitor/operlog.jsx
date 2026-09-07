import React, { useRef } from 'react'
import { PageContainer, ProTable } from '@ant-design/pro-components'
import { App, Descriptions, Modal, Popconfirm, Space, Tag } from 'antd'
import { listOperlog, delOperlog, cleanOperlog } from '../../api/operlog'

const bizMap = ['其它', '新增', '修改', '删除', '授权', '导出', '导入', '强退', '生成代码', '清空数据']

export default function MonitorOperlog() {
  const { message, modal } = App.useApp()
  const actionRef = useRef()

  const showDetail = (row) => {
    modal.info({
      title: '操作详情',
      width: 720,
      content: (
        <Descriptions column={2} size="small" bordered style={{ marginTop: 16 }}>
          <Descriptions.Item label="编号">{row.operId}</Descriptions.Item>
          <Descriptions.Item label="模块">{row.title}</Descriptions.Item>
          <Descriptions.Item label="类型">{bizMap[row.businessType] || '其它'}</Descriptions.Item>
          <Descriptions.Item label="请求方式">{row.requestMethod}</Descriptions.Item>
          <Descriptions.Item label="操作人员">{row.operName}</Descriptions.Item>
          <Descriptions.Item label="地址">{row.operIp}</Descriptions.Item>
          <Descriptions.Item label="URL" span={2}>{row.operUrl}</Descriptions.Item>
          <Descriptions.Item label="方法" span={2}>{row.method}</Descriptions.Item>
          <Descriptions.Item label="请求参数" span={2}>
            <div style={{ whiteSpace: 'pre-wrap', maxHeight: 200, overflow: 'auto' }}>{row.operParam}</div>
          </Descriptions.Item>
          <Descriptions.Item label="返回结果" span={2}>
            <div style={{ whiteSpace: 'pre-wrap', maxHeight: 200, overflow: 'auto' }}>{row.jsonResult}</div>
          </Descriptions.Item>
          <Descriptions.Item label="耗时">{row.costTime} ms</Descriptions.Item>
          <Descriptions.Item label="时间">{row.operTime}</Descriptions.Item>
          {row.errorMsg && <Descriptions.Item label="错误" span={2}>{row.errorMsg}</Descriptions.Item>}
        </Descriptions>
      )
    })
  }

  return (
    <PageContainer>
      <ProTable
        rowKey="operId"
        actionRef={actionRef}
        cardBordered
        scroll={{ x: 1200 }}
        search={{ labelWidth: 'auto' }}
        pagination={{ defaultPageSize: 10 }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params
          const res = await listOperlog({ ...rest, pageNum: current, pageSize })
          return { data: res.rows, total: res.total, success: true }
        }}
        columns={[
          { title: '序号', dataIndex: 'index', valueType: 'index', width: 70 },
          { title: '系统模块', dataIndex: 'title', width: 110, ellipsis: true },
          {
            title: '操作类型',
            dataIndex: 'businessType',
            width: 90,
            align: 'center',
            render: (v) => <Tag>{bizMap[v] || '其它'}</Tag>
          },
          { title: '请求方式', dataIndex: 'requestMethod', width: 90, align: 'center' },
          { title: '操作人员', dataIndex: 'operName', width: 100, ellipsis: true },
          { title: '部门', dataIndex: 'deptName', width: 120, ellipsis: true },
          { title: '操作地址', dataIndex: 'operIp', width: 130 },
          {
            title: '状态',
            dataIndex: 'status',
            width: 80,
            align: 'center',
            render: (v) => <Tag color={v === 0 ? 'success' : 'error'}>{v === 0 ? '正常' : '异常'}</Tag>
          },
          { title: '耗时', dataIndex: 'costTime', width: 90, render: (v) => `${v} ms` },
          { title: '操作时间', dataIndex: 'operTime', width: 170 },
          {
            title: '操作',
            width: 120,
            fixed: 'right',
            render: (_, row) => (
              <Space>
                <a onClick={() => showDetail(row)}>详细</a>
                <Popconfirm title="确认删除？" onConfirm={() =>
                  delOperlog(row.operId).then(() => { message.success('删除成功'); actionRef.current?.reload() })
                }>
                  <a style={{ color: 'red' }}>删除</a>
                </Popconfirm>
              </Space>
            )
          }
        ]}
        toolBarRender={() => [
          <Popconfirm key="clean" title="确认清空所有操作日志？" onConfirm={() =>
            cleanOperlog().then(() => { message.success('清空成功'); actionRef.current?.reload() })
          }>
            <a style={{ color: 'red' }}>清空</a>
          </Popconfirm>
        ]}
      />
    </PageContainer>
  )
}
