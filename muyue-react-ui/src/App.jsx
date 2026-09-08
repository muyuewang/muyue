import React, { Suspense, useEffect, useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { Spin } from 'antd'
import { getToken } from './utils/request'
import { getRouters } from './api/auth'
import Layout from './layout'
import { Login, BigScreen } from './routes/pages'

function PageLoading() {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 300 }}>
      <Spin size="large" tip="加载中..." />
    </div>
  )
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
          {/* 独立全屏数据大屏（Layout 外，对标 Vue 版 /bigscreen），供新标签直接打开 */}
          <Route path="/screen" element={<BigScreen standalone />} />
          {/* 其余路径交给 Layout 按菜单自行匹配（含页面缓存） */}
          <Route path="/*" element={<Layout menus={menus} />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  )
}
