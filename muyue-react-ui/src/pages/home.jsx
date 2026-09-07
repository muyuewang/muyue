import React from 'react'
import { Card, Col, Row, Statistic, Typography } from 'antd'
import {
  UserOutlined,
  ShoppingCartOutlined,
  BellOutlined,
  FileTextOutlined
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'

const { Title } = Typography

const quickList = [
  { title: '用户管理', path: '/system/user', icon: <UserOutlined /> },
  { title: '订单管理', path: '/order', icon: <ShoppingCartOutlined /> },
  { title: '通知公告', path: '/notice', icon: <BellOutlined /> },
  { title: '操作记录', path: '/monitor/operlog', icon: <FileTextOutlined /> }
]

export default function Home() {
  const navigate = useNavigate()
  return (
    <div style={{ padding: 24 }}>
      <Title level={4}>欢迎使用沐月管理系统</Title>
      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col span={6}><Card><Statistic title="用户总数" value="-" prefix={<UserOutlined />} /></Card></Col>
        <Col span={6}><Card><Statistic title="订单总数" value="-" prefix={<ShoppingCartOutlined />} /></Card></Col>
        <Col span={6}><Card><Statistic title="公告数" value="-" prefix={<BellOutlined />} /></Card></Col>
        <Col span={6}><Card><Statistic title="日志数" value="-" prefix={<FileTextOutlined />} /></Card></Col>
      </Row>
      <Row gutter={16} style={{ marginTop: 24 }}>
        {quickList.map((q) => (
          <Col span={6} key={q.path}>
            <Card hoverable onClick={() => navigate(q.path)}>
              <Card.Meta avatar={q.icon} title={q.title} description="点击进入" />
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  )
}
