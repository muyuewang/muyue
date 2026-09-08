import React, { useEffect, useRef, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { Spin } from 'antd'
import { componentMap, Placeholder } from '../routes/pages'

/**
 * 页面缓存（对标 Vue 版 keep-alive）：
 * 已访问过的页面保留在 DOM 中，切走时仅隐藏（display:none），
 * 切回来保留查询条件、分页位置、表单状态。
 *
 * 不缓存的页面（菜单「是否缓存 = 不缓存」，后端下发 meta.noCache=true）
 * 切走时直接卸载，下次进入重新加载。
 */
export default function PageCache({ routes }) {
  const location = useLocation()
  const cacheRef = useRef(new Map())
  const [, setTick] = useState(0)

  const matched = (routes || []).find((r) => r.path === location.pathname)

  useEffect(() => {
    if (!matched) return
    if (cacheRef.current.has(matched.path)) return
    const Comp = componentMap[matched.component] || Placeholder
    cacheRef.current.set(matched.path, { node: <Comp />, noCache: !!matched.noCache })
    setTick((t) => t + 1)
  }, [matched])

  if (!matched) {
    return <Placeholder />
  }

  return (
    <>
      {[...cacheRef.current.entries()].map(([path, item]) => {
        const active = path === location.pathname
        // 不缓存：离开即销毁（渲染 null 触发卸载）
        if (!active && item.noCache) return null
        return (
          <div key={path} style={{ display: active ? 'block' : 'none' }}>
            <React.Suspense
              fallback={
                <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}>
                  <Spin size="large" />
                </div>
              }
            >
              {item.node}
            </React.Suspense>
          </div>
        )
      })}
    </>
  )
}
