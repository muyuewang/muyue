/**
 * 页面缓存刷新总线：
 * 页签/用户下拉「刷新当前」→ 通知 PageCache 只重建对应路由的组件实例
 * （对标 Vue 版 TagsView 的 redirect 刷新：仅重载当前页，其余 keep-alive 页保留）。
 */
const listeners = new Set()

export function subscribePageRefresh(fn) {
  listeners.add(fn)
  return () => listeners.delete(fn)
}

/** 通知按 path 重建页面缓存（若已缓存） */
export function emitPageRefresh(path) {
  if (!path) return
  listeners.forEach((fn) => fn(path))
}
