import React, { useEffect, useState } from 'react'
import { Card, Col, Progress, Row, Table, Tag } from 'antd'
import {
  UserOutlined, ShoppingCartOutlined, DollarOutlined, BankOutlined,
  ClockCircleOutlined
} from '@ant-design/icons'
import { getScreenStats } from '../api/tool'

const statusMap = {
  0: { text: '待付款', color: '#faad14' },
  1: { text: '已付款', color: '#1677ff' },
  2: { text: '已发货', color: '#13c2c2' },
  3: { text: '已完成', color: '#52c41a' },
  4: { text: '已取消', color: '#666' }
}
const payMap = { 0: '支付宝', 1: '微信', 2: '货到付款' }

const darkCard = {
  background: 'rgba(255,255,255,0.05)',
  border: '1px solid rgba(255,255,255,0.12)',
  borderRadius: 10
}

/** 分布行取数：兼容 COUNT(*) 未加别名的键名 */
const rowCnt = (row) => Number(row.cnt ?? row['count(*)'] ?? Object.values(row)[1]) || 0

function BarRow({ label, value, max, color, suffix }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '7px 0' }}>
      <div style={{ width: 88, textAlign: 'right', color: '#8fa3bf', fontSize: 13 }}>{label}</div>
      <div style={{ flex: 1 }}>
        <Progress
          percent={max > 0 ? Math.round((value / max) * 100) : 0}
          showText={false}
          strokeColor={color || '#1677ff'}
          trailColor="rgba(255,255,255,0.08)"
          size="small"
        />
      </div>
      <div style={{ width: 70, color: '#e6f0ff', fontSize: 13 }}>{suffix ?? value}</div>
    </div>
  )
}

export default function Screen() {
  const [stats, setStats] = useState(null)
  const [clock, setClock] = useState('')

  useEffect(() => {
    const load = () => getScreenStats().then((res) => setStats(res.data || {})).catch(() => setStats({}))
    load()
    const timer = setInterval(load, 30000)
    const tick = setInterval(
      () => setClock(new Date().toLocaleString('zh-CN', { hour12: false })),
      1000
    )
    return () => {
      clearInterval(timer)
      clearInterval(tick)
    }
  }, [])

  const summary = stats?.summary || {}
  const trend = stats?.trend || []
  const statusDist = stats?.statusDist || []
  const payDist = stats?.payDist || []
  const branchUsers = (stats?.branchUsers || []).slice().sort((a, b) => (b.cnt || 0) - (a.cnt || 0))
  const recentOrders = stats?.recentOrders || []
  const maxTrend = Math.max(...trend.map((t) => Number(t.cnt) || 0), 1)
  const maxBranch = Math.max(...branchUsers.map((b) => Number(b.cnt) || 0), 1)

  return (
    <div
      style={{
        margin: -24,
        minHeight: 'calc(100vh - 56px)',
        background: 'linear-gradient(160deg, #0b1c33 0%, #0d2440 55%, #0a2f4a 100%)',
        padding: 24,
        color: '#e6f0ff'
      }}
    >
      {/* 头部 */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div style={{ fontSize: 24, fontWeight: 700, letterSpacing: 4, color: '#fff' }}>
          沐月电商运营数据大屏
        </div>
        <div style={{ color: '#8fa3bf' }}>
          <ClockCircleOutlined style={{ marginRight: 8 }} />
          {clock}
          <Tag color="green" style={{ marginLeft: 16 }}>30s 自动刷新</Tag>
        </div>
      </div>

      {/* 汇总指标 */}
      <Row gutter={16}>
        {[
          { title: '用户总数', value: Number(summary.userCount) || 0, icon: <UserOutlined />, color: '#1677ff' },
          { title: '分公司数', value: Number(summary.branchCount) || 0, icon: <BankOutlined />, color: '#722ed1' },
          { title: '订单总数', value: Number(summary.orderCount) || 0, icon: <ShoppingCartOutlined />, color: '#fa8c16' },
          { title: '订单总额', value: (Number(summary.totalAmount) || 0).toFixed(2), icon: <DollarOutlined />, color: '#52c41a', prefix: '￥' }
        ].map((s) => (
          <Col xs={12} md={6} key={s.title}>
            <Card size="small" style={darkCard} styles={{ body: { padding: 18 } }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                <div
                  style={{
                    width: 48, height: 48, borderRadius: 12, fontSize: 22,
                    background: s.color + '22', color: s.color,
                    display: 'flex', alignItems: 'center', justifyContent: 'center'
                  }}
                >
                  {s.icon}
                </div>
                <div>
                  <div style={{ color: '#8fa3bf', fontSize: 13 }}>{s.title}</div>
                  <div style={{ fontSize: 26, fontWeight: 700, color: '#fff' }}>
                    {s.prefix}
                    {Number(s.value).toLocaleString()}
                  </div>
                </div>
              </div>
            </Card>
          </Col>
        ))}
      </Row>

      <Row gutter={16} style={{ marginTop: 16 }}>
        {/* 订单趋势 */}
        <Col xs={24} lg={12}>
          <Card size="small" title="月度订单趋势" style={{ ...darkCard, height: '100%' }}
            styles={{ header: { color: '#e6f0ff', borderBottom: '1px solid rgba(255,255,255,0.1)' } }}>
            <div style={{ display: 'flex', gap: 16, alignItems: 'flex-end', height: 200, padding: '16px 8px 0' }}>
              {trend.length === 0 && <div style={{ color: '#8fa3bf' }}>暂无数据</div>}
              {trend.map((t) => {
                const h = Math.max(10, ((Number(t.cnt) || 0) / maxTrend) * 150)
                return (
                  <div key={t.ym} style={{ flex: 1, textAlign: 'center' }}>
                    <div style={{ fontSize: 12, color: '#e6f0ff', marginBottom: 4 }}>{t.cnt}</div>
                    <div
                      title={`${t.ym}：${t.cnt} 单 / ￥${Number(t.amt).toFixed(2)}`}
                      style={{
                        height: h,
                        background: 'linear-gradient(180deg, #4096ff, #1668dc)',
                        borderRadius: 4,
                        boxShadow: '0 0 12px #1668dc66'
                      }}
                    />
                    <div style={{ fontSize: 12, marginTop: 6, color: '#8fa3bf' }}>{t.ym}</div>
                  </div>
                )
              })}
            </div>
          </Card>
        </Col>

        {/* 状态 / 支付分布 */}
        <Col xs={24} lg={12}>
          <Card size="small" title="订单状态 / 支付方式分布" style={{ ...darkCard, height: '100%' }}
            styles={{ header: { color: '#e6f0ff', borderBottom: '1px solid rgba(255,255,255,0.1)' } }}>
            {statusDist.map((row) => {
              const st = statusMap[row.status] || { text: '状态' + row.status, color: '#1677ff' }
              return (
                <BarRow key={'s' + row.status} label={st.text} value={rowCnt(row)}
                  max={Math.max(...statusDist.map(rowCnt), 1)} color={st.color} />
              )
            })}
            <div style={{ borderTop: '1px dashed rgba(255,255,255,0.12)', margin: '10px 0' }} />
            {payDist.map((row) => (
              <BarRow key={'p' + row.pay_type} label={payMap[row.pay_type] || row.pay_type} value={rowCnt(row)}
                max={Math.max(...payDist.map(rowCnt), 1)} color="#722ed1" />
            ))}
          </Card>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginTop: 16 }}>
        {/* 分公司用户 */}
        <Col xs={24} lg={10}>
          <Card size="small" title="分公司人数排行" style={{ ...darkCard, height: '100%' }}
            styles={{ header: { color: '#e6f0ff', borderBottom: '1px solid rgba(255,255,255,0.1)' } }}>
            {branchUsers.map((b, i) => (
              <BarRow
                key={b.name}
                label={b.name}
                value={Number(b.cnt) || 0}
                max={maxBranch}
                color={['#faad14', '#1677ff', '#13c2c2', '#52c41a', '#eb2f96'][i % 5]}
              />
            ))}
          </Card>
        </Col>

        {/* 最近订单 */}
        <Col xs={24} lg={14}>
          <Card size="small" title="最近订单" style={{ ...darkCard, height: '100%' }}
            styles={{ header: { color: '#e6f0ff', borderBottom: '1px solid rgba(255,255,255,0.1)' }, body: { padding: 0 } }}>
            <Table
              size="small"
              rowKey="order_no"
              pagination={false}
              dataSource={recentOrders}
              style={{ background: 'transparent' }}
              columns={[
                { title: '订单号', dataIndex: 'order_no', width: 170, render: (v) => <span style={{ color: '#4096ff' }}>{v}</span> },
                { title: '买家', dataIndex: 'user_name', width: 90 },
                {
                  title: '金额', dataIndex: 'total_amount', width: 110, align: 'right',
                  render: (v) => <span style={{ color: '#52c41a' }}>￥{Number(v).toFixed(2)}</span>
                },
                {
                  title: '状态', dataIndex: 'status', width: 90, align: 'center',
                  render: (v) => <Tag color={statusMap[v]?.color}>{statusMap[v]?.text}</Tag>
                },
                { title: '时间', dataIndex: 'create_time', render: (v) => <span style={{ color: '#8fa3bf' }}>{String(v || '').slice(0, 19)}</span> }
              ]}
            />
          </Card>
        </Col>
      </Row>
    </div>
  )
}
