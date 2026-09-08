import React, { useEffect, useRef, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { Spin } from 'antd'
import { componentMap, Placeholder } from '../routes/pages'
import { subscribePageRefresh } from '../utils/cacheBus'

/**
 * 页面缓存（对标 Vue 版 keep-alive）：
 * 已访问过的页面保留在 DOM 中，切走时仅隐藏（display:none），
 * 切回来保留查询条件、分页位置、表单状态。
 *
 * 不缓存的页面（菜单「是否缓存 = 不缓存」，后端下发 meta.noCache=true）
 * 切走时直接卸载，下次进入重新加载。
 *
 * 「刷新当前」：页签/下拉点击刷新时，通过 cacheBus 把对应 path 的 epoch +1，
 * React key（path#epoch）变化 → 该页面组件被卸载重挂载（状态清空重载），
 * 其余已缓存页面保持不动 —— 对标 Vue 版 TagsView 的 redirect 刷新语义。
 */
export default function PageCache({ routes }) {
  const location = useLocation()
  const cacheRef = useRef(new Map())
  const [, setTick] = useState(0)

  const matched = (routes || []).find((r) => r.path === location.pathname)

  useEffect(() => {
    if (!matched) return
    if (cacheRef.current.has(matched.path)) return
    cacheRef.current.set(matched.path, {
      Comp: componentMap[matched.component] || Placeholder,
      noCache: !!matched.noCache,
      epoch: 0
    })
    setTick((t) => t + 1)
  }, [matched])

  // 接收「刷新某页」通知：仅该页 epoch+1（若尚未访问过则无需处理，进入时会新建）
  useEffect(() => {
    return subscribePageRefresh((path) => {
      const item = cacheRef.current.get(path)
      if (!item) return
      item.epoch += 1
      setTick((t) => t + 1)
    })
  }, [])

  if (!matched) {
    return <Placeholder />
  }

  return (
    <>
      {[...cacheRef.current.entries()].map(([path, item]) => {
        const active = path === location.pathname
        // 不缓存：离开即销毁（渲染 null 触发卸载）
        if (!active && item.noCache) return null
        const Comp = item.Comp
        return (
          <div key={`${path}#${item.epoch}`} style={{ display: active ? 'block' : 'none' }}>
            <React.Suspense
              fallback={
                <div style={{ display: 'flex', justifyContent: 'center', padding: 80 }}>
                  <Spin size="large" />
                </div>
              }
            >
              <Comp />
            </React.Suspense>
          </div>
        )
      })}
    </>
  )
}
