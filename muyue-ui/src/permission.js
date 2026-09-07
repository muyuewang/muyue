import router from '@/router'
import { getToken } from '@/utils/auth'
import useUserStore from '@/store/modules/user'
import usePermissionStore from '@/store/modules/permission'

const whiteList = ['/login', '/404', '/401']

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()

  if (getToken()) {
    if (to.path === '/login') {
      next({ path: '/' })
      return
    }
    if (!userStore.name) {
      try {
        await userStore.getInfo()
        const accessRoutes = await permissionStore.generateRoutes()
        accessRoutes.forEach((route) => router.addRoute(route))
        // 兜底路由，需在动态路由之后添加
        router.addRoute({ path: '/:pathMatch(.*)*', redirect: '/404', hidden: true })
        next({ ...to, replace: true })
      } catch (error) {
        await userStore.logOut()
        next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      }
    } else {
      next()
    }
  } else {
    if (whiteList.indexOf(to.path) !== -1) {
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
  }
})
