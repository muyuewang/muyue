import { createStore } from './createStore'

const HOME = { path: '/index', title: '首页', closable: false }

/**
 * 已打开页面页签（对标 Vue 版 TagsView）：
 * 首页固定不可关闭，关闭当前页自动回退到最后一个页签
 */
export const tabsStore = createStore({ tabs: [HOME] })

export function openTab(tab) {
  if (!tab || !tab.path || tab.path === '/login') return
  const { tabs } = tabsStore.get()
  if (tabs.some((t) => t.path === tab.path)) return
  tabsStore.set({ tabs: [...tabs, { closable: true, ...tab }] })
}

/** 关闭页签，返回关闭后应跳转的路径 */
export function closeTab(path) {
  const { tabs } = tabsStore.get()
  const index = tabs.findIndex((t) => t.path === path)
  if (index < 0) return undefined
  const next = tabs.filter((t) => t.path !== path)
  tabsStore.set({ tabs: next })
  if (path === HOME.path) return undefined
  return next[Math.min(index, next.length - 1)]?.path
}

export function closeOthers(path) {
  const { tabs } = tabsStore.get()
  tabsStore.set({ tabs: tabs.filter((t) => t.path === path || t.path === HOME.path) })
  return path
}

export function closeAll() {
  tabsStore.set({ tabs: [HOME] })
  return HOME.path
}
