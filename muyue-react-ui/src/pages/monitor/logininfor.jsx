import React from 'react'
import { PageContainer, ProTable } from '@ant-design/pro-components'
import { Tag } from 'antd'
import { listLogininfor } from '../../api/logininfor'

export default function MonitorLogininfor() {
  return (
    <PageContainer>
      <ProTable
        rowKey="infoId"
        cardBordered
        search={{ labelWidth: 'auto' }}
        scroll={{ x: 1000 }}
        pagination={{ defaultPageSize: 10 }}
        request={async (params) => {
          const { current, pageSize, ...rest } = params
          const res = await listLogininfor({ ...rest, pageNum: current, pageSize })
          return { data: res.rows, total: res.total, success: true }
        }}
        columns={[
          { title: '序号', dataIndex: 'index', valueType: 'index', width: 70 },
          { title: '账号', dataIndex: 'userName', width: 120 },
          { title: 'IP地址', dataIndex: 'ipaddr', width: 140 },
          { title: '登录地点', dataIndex: 'loginLocation', width: 140, ellipsis: true },
          { title: '浏览器', dataIndex: 'browser', width: 120, ellipsis: true },
          { title: '操作系统', dataIndex: 'os', width: 130, ellipsis: true },
          {
            title: '状态',
            dataIndex: 'status',
            width: 80,
            align: 'center',
            render: (v) => <Tag color={v === '0' ? 'success' : 'error'}>{v === '0' ? '成功' : '失败'}</Tag>
          },
          { title: '消息', dataIndex: 'msg', ellipsis: true },
          { title: '登录时间', dataIndex: 'loginTime', width: 170 }
        ]}
      />
    </PageContainer>
  )
}
