import { lazy } from 'react'

/** 路由级代码分割：每个页面独立 chunk */
export const Login = lazy(() => import('../pages/login'))
export const Home = lazy(() => import('../pages/home'))
const SystemUser = lazy(() => import('../pages/system/user'))
const SystemRole = lazy(() => import('../pages/system/role'))
const SystemMenu = lazy(() => import('../pages/system/menu'))
const SystemDept = lazy(() => import('../pages/system/dept'))
const SystemPost = lazy(() => import('../pages/system/post'))
const SystemDict = lazy(() => import('../pages/system/dict'))
const SystemNotice = lazy(() => import('../pages/system/notice'))
const OrderOrder = lazy(() => import('../pages/order/order'))
const MonitorOperlog = lazy(() => import('../pages/monitor/operlog'))
const MonitorLogininfor = lazy(() => import('../pages/monitor/logininfor'))
const MonitorOnline = lazy(() => import('../pages/monitor/online'))
const MonitorServer = lazy(() => import('../pages/monitor/server'))
const ToolGen = lazy(() => import('../pages/tool/gen'))
const ToolMail = lazy(() => import('../pages/tool/mail'))
const ToolFile = lazy(() => import('../pages/tool/file'))
export const Profile = lazy(() => import('../pages/profile'))
const Screen = lazy(() => import('../pages/screen'))
/** 独立全屏数据大屏（Layout 外，对标 Vue 版 /bigscreen） */
export const BigScreen = Screen
export const Placeholder = lazy(() => import('../pages/placeholder'))

/**
 * 组件注册表：key 为后端菜单的 component 字段
 * 新增页面时在这里登记即可，未登记的菜单自动落到 Placeholder
 */
export const componentMap = {
  'static:index': Home,
  'static:profile': Profile,
  'system/user/index': SystemUser,
  'system/role/index': SystemRole,
  'system/menu/index': SystemMenu,
  'system/dept/index': SystemDept,
  'system/post/index': SystemPost,
  'system/dict/index': SystemDict,
  'system/notice/index': SystemNotice,
  'order/order/index': OrderOrder,
  'monitor/operlog/index': MonitorOperlog,
  'monitor/logininfor/index': MonitorLogininfor,
  'monitor/online/index': MonitorOnline,
  'monitor/server/index': MonitorServer,
  'tool/gen/index': ToolGen,
  'tool/mail/index': ToolMail,
  'tool/file/index': ToolFile,
  'screen/jump': Screen
}

/** 静态路由（后端菜单不下发）：首页、个人中心 */
export const staticRoutes = [
  { path: '/index', component: 'static:index', title: '首页' },
  { path: '/user/profile', component: 'static:profile', title: '个人中心' }
]

/**
 * 把后端路由树打平成 { path, component, title, noCache } 列表（无名 Layout 壳直接拆开）
 * noCache 来自菜单的 is_cache 设置（后端 meta.noCache），决定是否参与 keep-alive
 */
export function flattenRouters(routers, parentPath = '') {
  const list = []
  for (const r of routers || []) {
    if (r.path === '/' && r.children && r.children.length && !(r.meta && r.meta.title)) {
      list.push(...flattenRouters(r.children, ''))
      continue
    }
    const path = r.path.startsWith('/')
      ? r.path
      : (parentPath ? parentPath + '/' + r.path : '/' + r.path)
    if (r.children && r.children.length) {
      list.push(...flattenRouters(r.children, path))
    } else if (r.component) {
      list.push({
        path,
        component: r.component,
        title: (r.meta && r.meta.title) || r.name,
        noCache: !!(r.meta && r.meta.noCache)
      })
    }
  }
  return list
}
