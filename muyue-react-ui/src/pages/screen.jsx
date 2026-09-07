import React, { useEffect, useState } from 'react'
import { PageContainer, ProCard } from '@ant-design/pro-components'
import { Col, Progress, Row, Card, Statistic } from 'antd'
import {
  UserOutlined, ShoppingCartOutlined, BellOutlined, FileTextOutlined,
  DollarOutlined, TeamOutlined
} from '@ant-design/icons'
import { getScreenStats } from '../api/tool'

/**
 * 数据大屏（/screen/stats 登录即可访问）
 * 后端返回 Map<String,Object>，这里按常见键位渲染，缺省项自动隐藏
 */
export default function Screen() {
  const [stats, setStats] = useState(null)

  useEffect(() => {
    const load = () => getScreenStats().then((res) => setStats(res.data || {})).catch(() => setStats({}))
    load()
    const timer = setInterval(load, 30000)
    return () => clearInterval(timer)
  }, [])

  const num = (k) => Number(stats?.[k]) || 0

  const cards = [
    { key: 'userCount', title: '用户总数', icon: <UserOutlined /> },
    { key: 'orderCount', title: '订单总数', icon: <ShoppingCartOutlined /> },
    { key: 'orderAmount', title: '订单总额', icon: <DollarOutlined /> },
    { key: 'noticeCount', title: '公告数', icon: <BellOutlined /> },
    { key: 'logCount', title: '操作日志', icon: <FileTextOutlined /> },
    { key: 'onlineCount', title: '在线用户', icon: <TeamOutlined /> }
  ].filter((c) => stats && stats[c.key] !== undefined)

  return (
    <PageContainer title="数据大屏" ghost>
      {cards.length > 0 && (
        <Row gutter={[16, 16]}>
          {cards.map((c) => (
            <Col xs={12} md={8} lg={4} key={c.key}>
              <Card>
                <Statistic
                  title={c.title}
                  value={c.key === 'orderAmount' ? num(c.key).toFixed(2) : num(c.key)}
                  prefix={c.icon}
                  valueStyle={{ fontSize: 22 }}
                />
              </Card>
            </Col>
          ))}
        </Row>
      )}

      {cards.length === 0 && (
        <ProCard style={{ marginTop: 16 }}>
          <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>
            后端 /screen/stats 暂无可展示的统计项
          </div>
        </ProCard>
      )}

      {stats && Object.keys(stats).length > 0 && (
        <Card size="small" title="原始统计字段" style={{ marginTop: 16 }}>
          <pre style={{ fontSize: 12, maxHeight: 240, overflow: 'auto' }}>
            {JSON.stringify(stats, null, 2)}
          </pre>
        </Card>
      )}
    </PageContainer>
  )
}
