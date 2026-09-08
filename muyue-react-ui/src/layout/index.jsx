import React, { useEffect, useRef, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { ProLayout } from '@ant-design/pro-components'
import { App as AntdApp, Badge, Dropdown, List, Popover, Tag, Typography, Avatar } from 'antd'
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
  BulbOutlined,
  MoonOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined,
  IdcardOutlined,
  FolderOpenOutlined,
  TeamOutlined
} from '@ant-design/icons'
import { listNotice, logout, unreadCount } from '../api/auth'
import { subscribeUnread } from '../utils/noticeBus'
import ErrorBoundary from '../components/ErrorBoundary'
import TabsView from '../components/TabsView'
import PageCache from '../components/PageCache'
import { flattenRouters, staticRoutes } from '../routes/pages'
import { authStore, loadAuth } from '../store/auth'
import { tabsStore, openTab } from '../store/tabs'
import { themeStore, toggleTheme } from '../store/theme'
import { emitPageRefresh } from '../utils/cacheBus'
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

/** 菜单树 → { path: title }，用于页签标题 */
function collectTitles(routers, parentPath = '') {
  const map = {}
  for (const r of routers || []) {
    const isWrap = r.path === '/' && r.children && r.children.length && !(r.meta && r.meta.title)
    const path = isWrap
      ? ''
      : (r.path.startsWith('/') ? r.path : (parentPath ? parentPath + '/' + r.path : '/' + r.path))
    if (isWrap) {
      Object.assign(map, collectTitles(r.children, ''))
      continue
    }
    if (r.meta && r.meta.title) map[path] = r.meta.title
    if (r.children && r.children.length) Object.assign(map, collectTitles(r.children, path))
  }
  return map
}

export default function Layout({ menus }) {
  const location = useLocation()
  const navigate = useNavigate()
  const { modal } = AntdApp.useApp()
  const { user } = authStore.use()
  const { dark } = themeStore.use()
  const [fullscreen, setFullscreen] = useState(false)

  // 静态路由 + 后端动态菜单（携带 noCache，决定是否参与页面缓存）
  const pageRoutes = React.useMemo(
    () => [...staticRoutes, ...flattenRouters(menus)],
    [menus]
  )

  useEffect(() => {
    if (!localStorage.getItem('muyue_react_token')) {
      navigate('/login', { replace: true })
      return
    }
    loadAuth().catch(() => {})
  }, [])

  // 路由变化 → 登记页签（首页固定不可关闭）
  useEffect(() => {
    if (menus === null) return
    const titles = collectTitles(menus)
    staticRoutes.forEach((r) => { titles[r.path] = r.title })
    const path = location.pathname
    if (path === '/login') return
    openTab({ path, title: titles[path] || path })
  }, [location.pathname, menus])

  // 全屏状态同步（对齐 Vue Navbar 的全屏按钮）
  useEffect(() => {
    const onFsChange = () => setFullscreen(Boolean(document.fullscreenElement))
    document.addEventListener('fullscreenchange', onFsChange)
    return () => document.removeEventListener('fullscreenchange', onFsChange)
  }, [])

  const toggleFullscreen = () => {
    if (document.fullscreenElement) {
      document.exitFullscreen?.()
    } else {
      document.documentElement.requestFullscreen?.()
    }
  }

  // 退出登录二次确认（对齐 Vue 版 ElMessageBox）
  const onLogout = () => {
    modal.confirm({
      title: '提示',
      content: '确定注销并退出系统吗？',
      okText: '确定',
      cancelText: '取消',
      okButtonProps: { danger: true },
      onOk: () => {
        logout().catch(() => {})
        removeToken()
        window.location.href = '/login'
      }
    })
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
          { path: '/index', name: '首页', icon: <HomeOutlined /> },
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
                { key: 'reload', icon: <ReloadOutlined />, label: '刷新当前页' },
                { type: 'divider' },
                { key: 'logout', icon: <LogoutOutlined />, danger: true, label: '退出登录' }
              ],
              onClick: ({ key }) => {
                if (key === 'profile') navigate('/user/profile')
                if (key === 'logout') onLogout()
                // 仅重载当前页签组件，不整页刷新（见 PageCache）
                if (key === 'reload') emitPageRefresh(location.pathname)
              }
            }}
          >
            {dom}
          </Dropdown>
        )
      }}
      actionsRender={() => [
        <NoticeBell key="bell" navigate={navigate} />,
        // 数据大屏：独立全屏页新标签打开（对齐 Vue 版 /bigscreen）
        <span
          key="screen"
          title="数据大屏"
          onClick={() => window.open('/screen', '_blank')}
          style={{ cursor: 'pointer', display: 'inline-flex', alignItems: 'center', fontSize: 16 }}
        >
          <FundOutlined />
        </span>,
        // 整页刷新（对齐 Vue Navbar 刷新）
        <span
          key="refresh"
          title="刷新"
          onClick={() => window.location.reload()}
          style={{ cursor: 'pointer', display: 'inline-flex', alignItems: 'center', fontSize: 16 }}
        >
          <ReloadOutlined />
        </span>,
        <span
          key="theme"
          style={{ cursor: 'pointer', display: 'inline-flex', alignItems: 'center', fontSize: 16 }}
          title={dark ? '切换浅色' : '切换深色'}
          onClick={() => toggleTheme()}
        >
          {dark ? <BulbOutlined /> : <MoonOutlined />}
        </span>,
        // 全屏切换（对齐 Vue Navbar 全屏按钮）
        <span
          key="fullscreen"
          style={{ cursor: 'pointer', display: 'inline-flex', alignItems: 'center', fontSize: 16 }}
          title={fullscreen ? '退出全屏' : '全屏'}
          onClick={toggleFullscreen}
        >
          {fullscreen ? <FullscreenExitOutlined /> : <FullscreenOutlined />}
        </span>
      ]}
    >
      {/* 已打开页面页签栏（对标 Vue 版 TagsView） */}
      <TabsView />
      {/* 页面级错误边界：切路由自动重置，避免单页崩溃拖垮整个框架 */}
      <ErrorBoundary resetKey={location.pathname}>
        <PageCache routes={pageRoutes} />
      </ErrorBoundary>
    </ProLayout>
  )
}
