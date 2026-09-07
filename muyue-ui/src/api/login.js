import request from '@/utils/request'

/**
 * 获取验证码
 */
export function getCodeImg() {
  return request({
    url: '/captchaImage',
    method: 'get'
  })
}

/**
 * 登录
 */
export function login(data) {
  return request({
    url: '/login',
    method: 'post',
    data
  })
}

/**
 * 获取用户信息（角色、权限）
 */
export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

/**
 * 获取动态路由（根据角色菜单）
 */
export function getRouters() {
  return request({
    url: '/getRouters',
    method: 'get'
  })
}

/**
 * 退出登录
 */
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}
