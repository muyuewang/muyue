import React, { useEffect, useState } from 'react'
import { PageContainer, ProCard } from '@ant-design/pro-components'
import { Col, Descriptions, Progress, Row, Card, Table } from 'antd'
import { getServerInfo } from '../../api/tool'

const colorOf = (v) => (v >= 80 ? '#f5222d' : v >= 60 ? '#faad14' : '#52c41a')
const num = (v) => Math.min(Math.max(Number(v) || 0, 0), 100)

function Ring({ title, percent, children }) {
  return (
    <Card size="small" title={title}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
        <Progress type="dashboard" percent={num(percent)} strokeColor={colorOf(num(percent))} />
        <div style={{ flex: 1 }}>{children}</div>
      </div>
    </Card>
  )
}

export default function MonitorServer() {
  const [server, setServer] = useState(null)

  const load = () => getServerInfo().then((res) => setServer(res.data))
  useEffect(load, [])

  if (!server) return <PageContainer><Card loading /></PageContainer>

  return (
    <PageContainer>
      <Row gutter={16}>
        <Col span={8}>
          <Ring title="CPU 使用率" percent={server.cpu?.total}>
            <Descriptions column={1} size="small">
              <Descriptions.Item label="核心数">{server.cpu?.cpuNum}</Descriptions.Item>
              <Descriptions.Item label="用户 / 系统">{server.cpu?.used}% / {server.cpu?.sys}%</Descriptions.Item>
              <Descriptions.Item label="空闲">{server.cpu?.free}%</Descriptions.Item>
            </Descriptions>
          </Ring>
        </Col>
        <Col span={8}>
          <Ring title="内存使用率" percent={server.mem?.usage}>
            <Descriptions column={1} size="small">
              <Descriptions.Item label="总内存">{server.mem?.total} GB</Descriptions.Item>
              <Descriptions.Item label="已用 / 剩余">{server.mem?.used} / {server.mem?.free} GB</Descriptions.Item>
            </Descriptions>
          </Ring>
        </Col>
        <Col span={8}>
          <Ring title="JVM 使用率" percent={server.jvm?.usage}>
            <Descriptions column={1} size="small">
              <Descriptions.Item label="版本">{server.jvm?.version}</Descriptions.Item>
              <Descriptions.Item label="已用 / 最大">{server.jvm?.used} / {server.jvm?.max} MB</Descriptions.Item>
              <Descriptions.Item label="运行时长">{server.jvm?.runTime}</Descriptions.Item>
            </Descriptions>
          </Ring>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col span={12}>
          <Card size="small" title="服务器信息">
            <Descriptions column={1} size="small">
              <Descriptions.Item label="服务器名称">{server.sys?.computerName}</Descriptions.Item>
              <Descriptions.Item label="操作系统">{server.sys?.osName}</Descriptions.Item>
              <Descriptions.Item label="服务器IP">{server.sys?.computerIp}</Descriptions.Item>
              <Descriptions.Item label="系统架构">{server.sys?.osArch}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
        <Col span={12}>
          <Card size="small" title="Java 虚拟机信息">
            <Descriptions column={1} size="small">
              <Descriptions.Item label="JVM 名称">{server.jvm?.name}</Descriptions.Item>
              <Descriptions.Item label="启动时间">{server.jvm?.startTime}</Descriptions.Item>
              <Descriptions.Item label="Java 版本">{server.jvm?.version}</Descriptions.Item>
              <Descriptions.Item label="Java 路径">{server.jvm?.home}</Descriptions.Item>
            </Descriptions>
          </Card>
        </Col>
      </Row>

      <Card size="small" title="磁盘状态" style={{ marginTop: 16 }}>
        <Table
          rowKey="dirName"
          size="small"
          pagination={false}
          dataSource={server.sysFiles || []}
          columns={[
            { title: '盘符路径', dataIndex: 'dirName' },
            { title: '文件系统', dataIndex: 'sysTypeName' },
            { title: '类型', dataIndex: 'typeName' },
            { title: '总大小', dataIndex: 'total' },
            { title: '可用大小', dataIndex: 'free' },
            { title: '已用大小', dataIndex: 'used' },
            {
              title: '已用百分比', dataIndex: 'usage', width: 180,
              render: (v) => <Progress percent={num(v)} strokeColor={colorOf(num(v))} size="small" />
            }
          ]}
        />
      </Card>
    </PageContainer>
  )
}
