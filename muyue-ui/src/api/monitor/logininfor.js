import request from '@/utils/request'

// 登录日志列表
export function listLogininfor(query) {
  return request({ url: '/monitor/logininfor/list', method: 'get', params: query })
}
// 删除
export function delLogininfor(infoIds) {
  return request({ url: '/monitor/logininfor/' + infoIds, method: 'delete' })
}
// 清空
export function cleanLogininfor() {
  return request({ url: '/monitor/logininfor/clean', method: 'delete' })
}
