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

/** 后端 Element 图标名 → antd 图标 */
import * as Icons from '@ant-design/icons'
const iconAlias = {
  User: 'UserOutlined', UserFilled: 'UserOutlined', ShoppingCart: 'ShoppingCartOutlined',
  Bell: 'BellOutlined', OfficeBuilding: 'BankOutlined', Document: 'FileTextOutlined',
  Connection: 'LinkOutlined', Cpu: 'HddOutlined', Setting: 'SettingOutlined',
  Menu: 'MenuOutlined', Collection: 'AppstoreOutlined', List: 'BarsOutlined',
  MagicStick: 'ToolOutlined', Monitor: 'DesktopOutlined', Edit: 'EditOutlined',
  EditPen: 'FormOutlined', Message: 'MessageOutlined', Email: 'MailOutlined',
  Download: 'DownloadOutlined', Upload: 'UploadOutlined', Chart: 'BarChartOutlined',
  Dashboard: 'DashboardOutlined', Tree: 'ClusterOutlined', DataBoard: 'FundOutlined'
}
function menuIcon(name) {
  const compName = iconAlias[name] || 'FolderOutlined'
  const Comp = Icons[compName]
  return Comp ? <Comp /> : <Icons.FolderOutlined />
}

/** 后端路由树 → ProLayout 菜单数据（子路径拼父前缀，ProLayout 认 path 字段） */
function toMenuData(routers, parentPath = '') {
  return (routers || [])
    .filter((r) => !r.hidden)
    .map((r) => {
      const path = r.path.startsWith('/')
        ? r.path
        : (parentPath ? parentPath + '/' + r.path : '/' + r.path)
      return {
        path,
        name: (r.meta && r.meta.title) || r.name,
        icon: r.meta && r.meta.icon ? menuIcon(r.meta.icon) : null,
        routes: r.children && r.children.length ? toMenuData(r.children, path) : undefined
      }
    })
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
      {notices.length === 0 && <div style={{ padding: 24, textAlign: 'center', color: '#999' }}>暂无消息通知</div>}
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
      menu={{ request: async () => toMenuData(menus), autoClose: false }}
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
                { key: 'profile', icon: <SettingOutlined />, label: '个人中心' },
                { key: 'reload', icon: <ReloadOutlined />, label: '刷新页面' },
                { type: 'divider' },
                { key: 'logout', icon: <LogoutOutlined />, danger: true, label: '退出登录' }
              ],
              onClick: ({ key }) => {
                if (key === 'profile') navigate('/user/profile')
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
