import axios from 'axios'
import { Modal } from 'antd'

export const BASE_API = import.meta.env.VITE_APP_BASE_API || '/dev-api'

/** token 存取（与后端约定：请求头 Authorization: Bearer xxx） */
export function getToken() {
  return localStorage.getItem('muyue_react_token')
}
export function setToken(token) {
  localStorage.setItem('muyue_react_token', 'Bearer ' + token)
}
export function removeToken() {
  localStorage.removeItem('muyue_react_token')
}

const request = axios.create({
  baseURL: BASE_API,
  timeout: 30000
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

let loggingOut = false

request.interceptors.response.use(
  (response) => {
    // blob 直接透传
    if (response.request && response.request.responseType === 'blob') {
      return response.data
    }
    const body = response.data
    const code = body.code || 200
    if (code === 401) {
      if (!loggingOut) {
        loggingOut = true
        Modal.confirm({
          title: '登录状态已过期',
          content: '您可以继续留在该页面，或重新登录',
          okText: '重新登录',
          cancelText: '取消',
          onOk: () => {
            removeToken()
            loggingOut = false
            window.location.href = '/login'
          },
          onCancel: () => {
            loggingOut = false
          }
        })
      }
      return Promise.reject(new Error(body.msg || '登录状态已过期'))
    }
    if (code !== 200) {
      return Promise.reject(new Error(body.msg || '请求失败'))
    }
    return body
  },
  (error) => {
    return Promise.reject(new Error(error.message || '网络请求异常'))
  }
)

export default request
