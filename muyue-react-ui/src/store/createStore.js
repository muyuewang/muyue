import { useSyncExternalStore } from 'react'

/**
 * 极简状态容器（约 20 行，替代引入 zustand）：
 * 与 Vue 版的 Pinia store 用法一致，订阅 + 选择器即触发组件更新。
 */
export function createStore(initial) {
  let state = initial
  const listeners = new Set()
  const emit = () => listeners.forEach((l) => l())
  return {
    get: () => state,
    set(patch) {
      state = typeof patch === 'function' ? patch(state) : { ...state, ...patch }
      emit()
    },
    subscribe(listener) {
      listeners.add(listener)
      return () => listeners.delete(listener)
    },
    /** 在组件中订阅整个 state */
    use() {
      return useSyncExternalStore(
        (cb) => {
          listeners.add(cb)
          return () => listeners.delete(cb)
        },
        () => state
      )
    }
  }
}
