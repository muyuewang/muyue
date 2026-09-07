import React, { lazy, Suspense, useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Spin } from 'antd'
import { getToken } from './utils/request'
import { getRouters } from './api/auth'
import Layout from './layout'

// 路由级代码分割：每个页面独立 chunk，首屏只加载当前页
const Login = lazy(() => import('./pages/login'))
const Home = lazy(() => import('./pages/home'))
const SystemUser = lazy(() => import('./pages/system/user'))
const SystemRole = lazy(() => import('./pages/system/role'))
const SystemMenu = lazy(() => import('./pages/system/menu'))
const SystemDept = lazy(() => import('./pages/system/dept'))
const SystemPost = lazy(() => import('./pages/system/post'))
const SystemDict = lazy(() => import('./pages/system/dict'))
const SystemNotice = lazy(() => import('./pages/system/notice'))
const OrderOrder = lazy(() => import('./pages/order/order'))
const MonitorOperlog = lazy(() => import('./pages/monitor/operlog'))
const MonitorLogininfor = lazy(() => import('./pages/monitor/logininfor'))
const MonitorOnline = lazy(() => import('./pages/monitor/online'))
const MonitorServer = lazy(() => import('./pages/monitor/server'))
const ToolGen = lazy(() => import('./pages/tool/gen'))
const ToolMail = lazy(() => import('./pages/tool/mail'))
const ToolFile = lazy(() => import('./pages/tool/file'))
const Profile = lazy(() => import('./pages/profile'))
const Screen = lazy(() => import('./pages/screen'))
const Placeholder = lazy(() => import('./pages/placeholder'))

function PageLoading() {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 300 }}>
      <Spin size="large" tip="加载中..." />
    </div>
  )
}

/**
 * 组件注册表：key 为后端菜单的 component 字段
 * 新增页面时在这里登记即可，未登记的菜单自动落到 Placeholder
 */
const componentMap = {
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

/** 把后端路由树打平成 { path, component, title } 列表（无名 Layout 壳直接拆开） */
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
      list.push({ path, component: r.component, title: r.meta && r.meta.title })
    }
  }
  return list
}

export default function App() {
  const [menus, setMenus] = useState(null)

  useEffect(() => {
    if (!getToken()) {
      setMenus([])
      return
    }
    getRouters()
      .then((res) => setMenus(res.data || []))
      .catch(() => setMenus([]))
  }, [])

  return (
    <BrowserRouter>
      <Suspense fallback={<PageLoading />}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/" element={<Layout menus={menus} />}>
            {/* 首页是静态路由（后端不下发），固定为 /index */}
            <Route path="index" element={<Home />} />
            <Route path="user/profile" element={<Profile />} />
            <Route index element={<Navigate to="/index" replace />} />
            {(menus || []).length > 0 &&
              flattenRouters(menus).map((r) => {
                const Comp = componentMap[r.component] || Placeholder
                return <Route key={r.path} path={r.path} element={<Comp />} />
              })}
            <Route path="*" element={<Placeholder />} />
          </Route>
        </Routes>
      </Suspense>
    </BrowserRouter>
  )
}
