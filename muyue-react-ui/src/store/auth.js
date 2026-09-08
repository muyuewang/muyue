import { createStore } from './createStore'
import { getInfo } from '../api/auth'

/** 当前用户与权限（与 Vue 版 user store 对齐） */
export const authStore = createStore({ user: null, permissions: [], roles: [] })

export function loadAuth() {
  return getInfo().then((res) => {
    authStore.set({
      user: res.user || null,
      permissions: res.permissions || [],
      roles: res.roles || []
    })
  })
}

/** 是否拥有指定权限（超级管理员 *:*:* 一律放行，与后端 PermissionService 规则一致） */
export function hasPermi(perm) {
  if (!perm) return true
  const { permissions } = authStore.get()
  if (permissions.includes('*:*:*')) return true
  const list = Array.isArray(perm) ? perm : [perm]
  return list.some((p) => permissions.includes(p))
}
