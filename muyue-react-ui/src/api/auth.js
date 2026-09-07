import request from '../utils/request'

/** 登录（验证码开关由后端返回） */
export function login(data) {
  return request({ url: '/login', method: 'post', data })
}

/** 获取验证码 */
export function getCaptcha() {
  return request({ url: '/captchaImage', method: 'get' })
}

/** 用户信息 + 权限 */
export function getInfo() {
  return request({ url: '/getInfo', method: 'get' })
}

/** 动态菜单路由 */
export function getRouters() {
  return request({ url: '/getRouters', method: 'get' })
}

/** 退出登录 */
export function logout() {
  return request({ url: '/logout', method: 'post' })
}

/** 公告（铃铛用） */
export function listNotice(params) {
  return request({ url: '/system/notice/list', method: 'get', params })
}
export function unreadCount() {
  return request({ url: '/system/notice/unreadCount', method: 'get' })
}
