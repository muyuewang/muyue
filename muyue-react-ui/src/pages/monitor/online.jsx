import React, { useRef } from 'react'
import { PageContainer, ProTable } from '@ant-design/pro-components'
import { App, Popconfirm, Tag } from 'antd'
import { listOnline, forceLogout } from '../../api/tool'
import Auth from '../../components/Auth'

export default function MonitorOnline() {
  const { message } = App.useApp()
  const actionRef = useRef()

  return (
    <PageContainer>
      <ProTable
        rowKey="tokenId"
        actionRef={actionRef}
        cardBordered
        search={{ labelWidth: 'auto', defaultCollapsed: false }}
        pagination={false}
        request={async (params) => {
          const { current, pageSize, ...rest } = params
          const res = await listOnline(rest)
          return { data: res.rows, total: res.total, success: true }
        }}
        columns={[
          { title: '会话编号', dataIndex: 'tokenId', ellipsis: true, width: 260 },
          { title: '账号', dataIndex: 'userName', width: 120 },
          { title: '部门', dataIndex: 'deptName', width: 140, ellipsis: true },
          { title: 'IP地址', dataIndex: 'ipaddr', width: 140 },
          { title: '登录地点', dataIndex: 'loginLocation', width: 140, ellipsis: true },
          { title: '浏览器', dataIndex: 'browser', width: 120 },
          {
            title: '状态', dataIndex: 'status', width: 80, align: 'center',
            render: () => <Tag color="success">在线</Tag>
          },
          { title: '登录时间', dataIndex: 'loginTime', width: 170 },
          {
            title: '操作', width: 100,
            render: (_, row) => (
              <Auth permi="monitor:online:forceLogout">
                <Popconfirm title="确认强制该用户下线？" onConfirm={() =>
                  forceLogout(row.tokenId).then(() => { message.success('已强制下线'); actionRef.current?.reload() })
                }>
                  <a style={{ color: 'red' }}>强退</a>
                </Popconfirm>
              </Auth>
            )
          }
        ]}
      />
    </PageContainer>
  )
}
