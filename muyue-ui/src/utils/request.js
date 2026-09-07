import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
  (res) => {
    const data = res.data
    const code = data.code || 200
    const msg = data.msg || '系统错误'

    if (code === 200) {
      return data
    }
    if (code === 401) {
      removeToken()
      ElMessage.error('登录状态已过期，请重新登录')
      setTimeout(() => {
        window.location.href = '/login'
      }, 800)
      return Promise.reject(new Error('无效的会话，或者会话已过期，请重新登录'))
    }
    if (code === 403) {
      ElMessage.error('没有权限访问该资源')
      return Promise.reject(new Error(msg))
    }
    ElMessage.error(msg)
    return Promise.reject(new Error(msg))
  },
  (error) => {
    let message = error.message
    if (message === 'Network Error') {
      message = '后端接口连接异常'
    } else if (message.includes('timeout')) {
      message = '系统接口请求超时'
    } else if (message.includes('Request failed with status code')) {
      message = '系统接口 ' + message.substr(message.length - 3) + ' 异常'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
