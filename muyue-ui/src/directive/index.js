import useUserStore from '@/store/modules/user'

/**
 * v-hasPermi="['system:user:add']"
 */
function checkPermission(el, binding) {
  const { value } = binding
  const allPermission = '*:*:*'
  const permissions = useUserStore().permissions || []

  if (value && value instanceof Array && value.length > 0) {
    const hasPermission = permissions.some((permission) => {
      return allPermission === permission || value.includes(permission)
    })
    if (!hasPermission) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  } else {
    throw new Error(`请设置操作权限标签值，如 v-hasPermi="['system:user:add']"`)
  }
}

/**
 * v-hasRole="['admin']"
 */
function checkRole(el, binding) {
  const { value } = binding
  const superAdmin = 'admin'
  const roles = useUserStore().roles || []

  if (value && value instanceof Array && value.length > 0) {
    const hasRole = roles.some((role) => {
      return superAdmin === role || value.includes(role)
    })
    if (!hasRole) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  } else {
    throw new Error(`请设置角色权限标签值，如 v-hasRole="['admin']"`)
  }
}

export default (app) => {
  app.directive('hasPermi', {
    mounted: checkPermission,
    updated: checkPermission
  })
  app.directive('hasRole', {
    mounted: checkRole,
    updated: checkRole
  })
}
