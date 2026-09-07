import React, { useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { getToken } from './utils/request'
import { getRouters } from './api/auth'
import Layout from './layout'
import Login from './pages/login'
import Home from './pages/home'
import SystemUser from './pages/system/user'
import SystemRole from './pages/system/role'
import SystemMenu from './pages/system/menu'
import SystemDept from './pages/system/dept'
import SystemPost from './pages/system/post'
import SystemDict from './pages/system/dict'
import SystemNotice from './pages/system/notice'
import OrderOrder from './pages/order/order'
import MonitorOperlog from './pages/monitor/operlog'
import MonitorLogininfor from './pages/monitor/logininfor'
import MonitorOnline from './pages/monitor/online'
import MonitorServer from './pages/monitor/server'
import ToolGen from './pages/tool/gen'
import ToolMail from './pages/tool/mail'
import ToolFile from './pages/tool/file'
import Profile from './pages/profile'
import Screen from './pages/screen'
import Placeholder from './pages/placeholder'

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

/** 把后端路由树打平成 { path, component, title } 列表 */
export function flattenRouters(routers, parentPath = '') {
  const list = []
  for (const r of routers || []) {
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
    </BrowserRouter>
  )
}
