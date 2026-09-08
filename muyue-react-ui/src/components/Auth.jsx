import { authStore, hasPermi } from '../store/auth'
import { getToken } from '../utils/request'

/**
 * 按钮级权限容器（对标 Vue 版的 v-hasPermi 指令）：
 *   <Auth permi="system:user:add"><Button>新增</Button></Auth>
 *   <Auth permi={['system:user:edit', 'system:user:remove']}>...</Auth>
 * 支持 fallback：无权限时渲染占位（如禁用态按钮）
 */
export default function Auth({ permi, fallback = null, children }) {
  const { user, permissions } = authStore.use()
  // 已登录但用户信息/权限尚未加载完成时，先放行按钮，避免表格操作列空白闪烁
  if (getToken() && (user === null || permissions.length === 0)) return children
  return hasPermi(permi) ? children : fallback
}

/** 无权限时返回 true 的便捷钩子（用于表格列显隐、disabled 等） */
export function usePermi(permi) {
  const { user, permissions } = authStore.use()
  if (getToken() && (user === null || permissions.length === 0)) return true
  return hasPermi(permi)
}
