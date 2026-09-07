import { createApp } from 'vue'

import store from './store'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './styles/index.css'

import App from './App.vue'
import router from './router'
import directive from './directive'
import Pagination from '@/components/Pagination/index.vue'
import './permission'

const app = createApp(App)

// 全局分页组件
app.component('pagination', Pagination)

// 注册全部 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(store)
app.use(router)
app.use(ElementPlus, { locale: zhCn, size: 'default' })
app.use(directive)

app.mount('#app')
