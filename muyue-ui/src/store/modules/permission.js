import { defineStore } from 'pinia'
import { constantRoutes } from '@/router'
import { getRouters } from '@/api/login'
import Layout from '@/layout/index.vue'
import ParentView from '@/components/ParentView.vue'

/**
 * 动态加载 views 下的组件
 */
const modules = import.meta.glob('../../views/**/*.vue')

function loadView(view) {
  for (const path in modules) {
    const dir = path.split('views/')[1]?.split('.vue')[0]
    if (dir === view) {
      return modules[path]
    }
  }
  return null
}

/**
 * 将后端返回的路由转换为前端路由对象
 */
function filterAsyncRouter(asyncRouterMap) {
  return asyncRouterMap.filter((route) => {
    if (route.component) {
      if (route.component === 'Layout') {
        route.component = Layout
      } else if (route.component === 'ParentView') {
        route.component = ParentView
      } else {
        route.component = loadView(route.component) || Layout
      }
    }
    // noRedirect 表示目录本身不跳转
    if (route.redirect === 'noRedirect') {
      route.redirect = undefined
    }
    if (route.children && route.children.length > 0) {
      route.children = filterAsyncRouter(route.children)
    }
    return true
  })
}

const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [],
    addRoutes: [],
    sidebarRouters: []
  }),

  actions: {
    /** 生成动态路由 */
    generateRoutes() {
      return new Promise((resolve, reject) => {
        getRouters()
          .then((res) => {
            // 一份用于路由，一份用于侧边栏，避免互相影响
            const rdata = JSON.parse(JSON.stringify(res.data))
            const sdata = JSON.parse(JSON.stringify(res.data))
            const rewriteRoutes = filterAsyncRouter(rdata)
            const sidebarRoutes = filterAsyncRouter(sdata)
            this.routes = constantRoutes.concat(rewriteRoutes)
            this.addRoutes = rewriteRoutes
            this.sidebarRouters = constantRoutes.concat(sidebarRoutes)
            resolve(rewriteRoutes)
          })
          .catch((error) => {
            reject(error)
          })
      })
    }
  }
})

export default usePermissionStore
