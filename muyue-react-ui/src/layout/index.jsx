import React, { useEffect, useRef, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { ProLayout } from '@ant-design/pro-components'
import { Badge, Dropdown, List, Popover, Tag, Typography, Avatar, message } from 'antd'
import {
  BellOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
  ReloadOutlined,
  ShoppingCartOutlined,
  BankOutlined,
  FileTextOutlined,
  LinkOutlined,
  HddOutlined,
  MenuOutlined,
  AppstoreOutlined,
  BarsOutlined,
  ToolOutlined,
  DesktopOutlined,
  FormOutlined,
  MessageOutlined,
  MailOutlined,
  BarChartOutlined,
  DashboardOutlined,
  FundOutlined,
  FolderOutlined,
  HomeOutlined,
  IdcardOutlined,
  FolderOpenOutlined,
  TeamOutlined
} from '@ant-design/icons'
import { getInfo, listNotice, logout, unreadCount } from '../api/auth'
import { subscribeUnread } from '../utils/noticeBus'
import ErrorBoundary from '../components/ErrorBoundary'
import { removeToken } from '../utils/request'

const { Text } = Typography

/** 后端 Element 图标名 → antd 图标（显式按名导入，避免全量引入 ~800 个图标） */
const iconAlias = {
  User: UserOutlined, UserFilled: TeamOutlined, ShoppingCart: ShoppingCartOutlined,
  Bell: BellOutlined, OfficeBuilding: BankOutlined, Document: FileTextOutlined,
  Connection: LinkOutlined, Cpu: HddOutlined, Setting: SettingOutlined,
  Menu: MenuOutlined, Collection: AppstoreOutlined, List: BarsOutlined,
  MagicStick: ToolOutlined, Monitor: DesktopOutlined, Edit: FormOutlined,
  EditPen: FormOutlined, Message: MessageOutlined, Email: MailOutlined,
  Download: MailOutlined, Upload: MailOutlined, Chart: BarChartOutlined,
  Dashboard: DashboardOutlined, Tree: FundOutlined, DataBoard: FundOutlined,
  Postcard: IdcardOutlined, FolderOpened: FolderOpenOutlined
}
function menuIcon(name) {
  const Comp = iconAlias[name] || FolderOutlined
  return Comp ? <Comp /> : <FolderOutlined />
}

/** 后端路由树 → ProLayout 菜单数据（子路径拼父前缀，ProLayout 认 path 字段） */
function toMenuData(routers, parentPath = '') {
  const result = []
  for (const r of (routers || []).filter((x) => !x.hidden)) {
    // 一级 C 菜单被包在无名 Layout 壳（path:'/' 且无标题）里：拆壳，子项直接提升为一级菜单
    if (r.path === '/' && r.children && r.children.length && !(r.meta && r.meta.title)) {
      result.push(...toMenuData(r.children, ''))
      continue
    }
    const path = r.path.startsWith('/')
      ? r.path
      : (parentPath ? parentPath + '/' + r.path : '/' + r.path)
    result.push({
      path,
      name: (r.meta && r.meta.title) || r.name,
      icon: r.meta && r.meta.icon ? menuIcon(r.meta.icon) : null,
      routes: r.children && r.children.length ? toMenuData(r.children, path) : undefined
    })
  }
  return result
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
    // 公告页读完/发布/删除时即时联动
    const unsub = subscribeUnread(load)
    return () => {
      clearInterval(timer.current)
      unsub()
    }
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
      menu={{
        request: async () => [
          // 首页是静态路由（与 Vue 版一致），固定在菜单首位
          { path: '/index', name: '首页', icon: <Icons.HomeOutlined /> },
          ...toMenuData(menus)
        ],
        autoClose: false
      }}
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
      {/* 页面级错误边界：切路由自动重置，避免单页崩溃拖垮整个框架 */}
      <ErrorBoundary resetKey={location.pathname}>
        <Outlet />
      </ErrorBoundary>
    </ProLayout>
  )
}
