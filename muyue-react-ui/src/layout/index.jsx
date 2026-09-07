import React, { useEffect, useRef, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { ProLayout } from '@ant-design/pro-components'
import { Badge, Dropdown, List, Popover, Tag, Typography, Avatar, message } from 'antd'
import {
  BellOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
  ReloadOutlined
} from '@ant-design/icons'
import { getInfo, listNotice, logout, unreadCount } from '../api/auth'
import { removeToken } from '../utils/request'

const { Text } = Typography

/** antd 图标名 → 组件（按需映射常用图标） */
import * as Icons from '@ant-design/icons'
function menuIcon(name) {
  const Comp = Icons[name]
  return Comp ? <Comp /> : null
}

/** 后端路由树 → ProLayout 菜单数据 */
function toMenuData(routers) {
  return (routers || [])
    .filter((r) => !r.hidden)
    .map((r) => ({
      key: r.path.startsWith('/') ? r.path : '/' + r.path,
      name: (r.meta && r.meta.title) || r.name,
      icon: r.meta && r.meta.icon ? menuIcon(r.meta.icon) : null,
      routes: r.children && r.children.length ? toMenuData(r.children) : undefined
    }))
}

function NoticeBell({ navigate }) {
  const [unread, setUnread] = useState(0)
  const [notices, setNotices] = useState([])
  const timer = useRef(null)

  const load = () => {
    unreadCount()
      .then((res) => setUnread(res.data || 0))
      .catch(() => {})
    listNotice({ pageNum: 1, pageSize: 5 })
      .then((res) => setNotices(res.rows || []))
      .catch(() => {})
  }

  useEffect(() => {
    load()
    timer.current = setInterval(load, 60000)
    return () => clearInterval(timer.current)
  }, [])

  const content = (
    <div style={{ width: 320 }}>
      {notices.length === 0 && <List emptyText="暂无消息通知" />}
      <List
        size="small"
        dataSource={notices}
        renderItem={(item) => (
          <List.Item
            style={{ cursor: 'pointer' }}
            onClick={() => navigate('/system/notice')}
          >
            <Tag color={item.noticeType === '1' ? 'warning' : 'success'}>
              {item.noticeType === '1' ? '通知' : '公告'}
            </Tag>
            <Text ellipsis style={{ maxWidth: 220 }}>
              {item.noticeTitle}
            </Text>
          </List.Item>
        )}
      />
    </div>
  )

  return (
    <Popover content={content} title="消息通知" trigger="click" placement="bottomRight">
      <Badge count={unread} size="small" offset={[-2, 2]}>
        <BellOutlined style={{ fontSize: 18 }} />
      </Badge>
    </Popover>
  )
}

export default function Layout({ menus }) {
  const location = useLocation()
  const navigate = useNavigate()
  const [user, setUser] = useState({ nickName: '', avatar: '' })

  useEffect(() => {
    if (!localStorage.getItem('muyue_react_token')) {
      navigate('/login', { replace: true })
      return
    }
    getInfo()
      .then((res) => setUser(res.user || {}))
      .catch(() => {})
  }, [])

  const onLogout = () => {
    logout().catch(() => {})
    removeToken()
    message.success('已退出登录')
    window.location.href = '/login'
  }

  if (menus === null) {
    return <div style={{ padding: 100, textAlign: 'center' }}>加载中...</div>
  }

  return (
    <ProLayout
      title="沐月管理系统"
      layout="mix"
      fixSiderbar
      fixedHeader
      location={{ pathname: location.pathname }}
      menu={{ request: () => toMenuData(menus), autoClose: false }}
      menuItemRender={(item, dom) => (
        <div onClick={() => item.path && navigate(item.path)}>{dom}</div>
      )}
      breadcrumbRender={(routers = []) => [
        { path: '/', breadcrumbName: '首页' },
        ...routers
      ]}
      avatarProps={{
        src: user.avatar || undefined,
        icon: <UserOutlined />,
        size: 'small',
        title: user.nickName || '用户',
        render: (_props, dom) => (
          <Dropdown
            menu={{
              items: [
                { key: 'reload', icon: <ReloadOutlined />, label: '刷新页面' },
                { type: 'divider' },
                { key: 'logout', icon: <LogoutOutlined />, danger: true, label: '退出登录' }
              ],
              onClick: ({ key }) => {
                if (key === 'logout') onLogout()
                if (key === 'reload') window.location.reload()
              }
            }}
          >
            {dom}
          </Dropdown>
        )
      }}
      actionsRender={() => [
        <NoticeBell key="bell" navigate={navigate} />
      ]}
    >
      <Outlet />
    </ProLayout>
  )
}
