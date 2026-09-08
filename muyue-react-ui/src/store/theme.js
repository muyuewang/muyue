import { createStore } from './createStore'

const KEY = 'muyue_theme'

/** 浅色 / 暗色主题（与 Vue 版共用同一个 localStorage 键） */
export const themeStore = createStore({
  dark: localStorage.getItem(KEY) === 'dark'
})

export function toggleTheme() {
  const next = !themeStore.get().dark
  localStorage.setItem(KEY, next ? 'dark' : 'light')
  document.documentElement.classList.toggle('dark', next)
  themeStore.set({ dark: next })
}

export function applyTheme() {
  document.documentElement.classList.toggle('dark', themeStore.get().dark)
}
