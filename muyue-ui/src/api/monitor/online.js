import request from '@/utils/request'

// 在线用户列表
export function listOnline(query) {
  return request({ url: '/monitor/online/list', method: 'get', params: query })
}
// 强退用户
export function forceLogout(token) {
  return request({ url: '/monitor/online/' + token, method: 'delete' })
}
