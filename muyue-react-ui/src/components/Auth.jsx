import { authStore, hasPermi } from '../store/auth'

/**
 * 按钮级权限容器（对标 Vue 版的 v-hasPermi 指令）：
 *   <Auth permi="system:user:add"><Button>新增</Button></Auth>
 *   <Auth permi={['system:user:edit', 'system:user:remove']}>...</Auth>
 * 支持 fallback：无权限时渲染占位（如禁用态按钮）
 */
export default function Auth({ permi, fallback = null, children }) {
  const { permissions } = authStore.use()
  const allowed = hasPermi(permi)
  if (allowed) return children
  return fallback
}

/** 无权限时返回 true 的便捷钩子（用于表格列显隐、disabled 等） */
export function usePermi(permi) {
  const { permissions } = authStore.use()
  return hasPermi(permi)
}
