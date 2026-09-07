import React, { useEffect, useState } from 'react'
import { Card, Col, Row, Statistic, Tag, Typography, Button, Progress } from 'antd'
import {
  UserOutlined,
  TeamOutlined,
  ShoppingCartOutlined,
  BellOutlined,
  BankOutlined,
  FileTextOutlined,
  FundOutlined,
  RightOutlined
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getScreenStats } from '../api/tool'
import { listNotice } from '../api/auth'

const { Title, Text } = Typography

const quickList = [
  { title: '用户管理', path: '/system/user', icon: <UserOutlined />, color: '#1677ff' },
  { title: '角色管理', path: '/system/role', icon: <TeamOutlined />, color: '#722ed1' },
  { title: '订单管理', path: '/order', icon: <ShoppingCartOutlined />, color: '#fa8c16' },
  { title: '通知公告', path: '/system/notice', icon: <BellOutlined />, color: '#52c41a' },
  { title: '代码生成', path: '/tool/gen', icon: <FileTextOutlined />, color: '#13c2c2' },
  { title: '数据大屏', path: '/screen', icon: <FundOutlined />, color: '#eb2f96' }
]

const statusMap = {
  0: { text: '待付款', color: 'orange' },
  1: { text: '已付款', color: 'blue' },
  2: { text: '已发货', color: 'cyan' },
  3: { text: '已完成', color: 'green' },
  4: { text: '已取消', color: 'default' }
}
const payMap = { 0: '支付宝', 1: '微信', 2: '货到付款' }

export default function Home() {
  const navigate = useNavigate()
  const [stats, setStats] = useState(null)
  const [notices, setNotices] = useState([])

  useEffect(() => {
    getScreenStats().then((res) => setStats(res.data || {})).catch(() => setStats({}))
    listNotice({ pageNum: 1, pageSize: 5 })
      .then((res) => setNotices(res.rows || []))
      .catch(() => {})
  }, [])

  const summary = stats?.summary || {}
  const trend = (stats?.trend || []).slice(-6)
  const maxCnt = Math.max(...trend.map((t) => Number(t.cnt) || 0), 1)

  return (
    <div style={{ padding: 24 }}>
      <Title level={4} style={{ marginBottom: 4 }}>欢迎使用沐月管理系统</Title>
      <Text type="secondary">React + Ant Design Pro 版</Text>

      {/* 统计卡 */}
      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col xs={12} md={6}>
          <Card loading={!stats}>
            <Statistic title="用户总数" value={Number(summary.userCount) || 0} prefix={<UserOutlined />} />
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card loading={!stats}>
            <Statistic title="分公司数" value={Number(summary.branchCount) || 0} prefix={<BankOutlined />} />
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card loading={!stats}>
            <Statistic title="订单总数" value={Number(summary.orderCount) || 0} prefix={<ShoppingCartOutlined />} />
          </Card>
        </Col>
        <Col xs={12} md={6}>
          <Card loading={!stats}>
            <Statistic
              title="订单总额"
              value={Number(summary.totalAmount) || 0}
              precision={2}
              prefix="￥"
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginTop: 16 }}>
        {/* 订单趋势 */}
        <Col xs={24} md={14}>
          <Card
            size="small"
            title="近几月订单趋势"
            extra={<a onClick={() => navigate('/order')}>查看订单 <RightOutlined /></a>}
          >
            {trend.length === 0 && <div style={{ color: '#999', padding: 24, textAlign: 'center' }}>暂无数据</div>}
            <div style={{ display: 'flex', gap: 18, alignItems: 'flex-end', height: 180, padding: '12px 8px 0' }}>
              {trend.map((t) => {
                const h = Math.max(8, ((Number(t.cnt) || 0) / maxCnt) * 140)
                return (
                  <div key={t.ym} style={{ flex: 1, textAlign: 'center' }}>
                    <div style={{ fontSize: 12, marginBottom: 4 }}>{t.cnt}</div>
                    <div
                      title={`${t.ym}：${t.cnt} 单 / ￥${Number(t.amt).toFixed(2)}`}
                      style={{
                        height: h,
                        background: 'linear-gradient(180deg, #69b1ff, #1677ff)',
                        borderRadius: 4,
                        margin: '0 6px'
                      }}
                    />
                    <div style={{ fontSize: 12, marginTop: 6, color: '#888' }}>{t.ym}</div>
                  </div>
                )
              })}
            </div>
          </Card>
        </Col>

        {/* 最近公告 */}
        <Col xs={24} md={10}>
          <Card
            size="small"
            title="最近公告"
            extra={<a onClick={() => navigate('/system/notice')}>全部 <RightOutlined /></a>}
          >
            {notices.length === 0 && <div style={{ color: '#999', padding: 24, textAlign: 'center' }}>暂无公告</div>}
            {notices.map((n) => (
              <div
                key={n.noticeId}
                onClick={() => navigate('/system/notice')}
                style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 0', cursor: 'pointer', borderBottom: '1px solid #f0f0f0' }}
              >
                <Tag color={n.noticeType === '1' ? 'warning' : 'success'} style={{ marginRight: 0 }}>
                  {n.noticeType === '1' ? '通知' : '公告'}
                </Tag>
                <Text ellipsis style={{ flex: 1 }}>{n.noticeTitle}</Text>
                <Text type="secondary" style={{ fontSize: 12 }}>{(n.createTime || '').slice(0, 10)}</Text>
              </div>
            ))}
          </Card>
        </Col>
      </Row>

      {/* 快捷入口 */}
      <Card size="small" title="快捷入口" style={{ marginTop: 16 }}>
        <Row gutter={16}>
          {quickList.map((q) => (
            <Col xs={12} sm={8} md={4} key={q.path}>
              <Card
                hoverable
                size="small"
                onClick={() => navigate(q.path)}
                style={{ textAlign: 'center' }}
                bodyStyle={{ padding: 16 }}
              >
                <div style={{ fontSize: 28, color: q.color, marginBottom: 8 }}>{q.icon}</div>
                {q.title}
              </Card>
            </Col>
          ))}
        </Row>
      </Card>
    </div>
  )
}
