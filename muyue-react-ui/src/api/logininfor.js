import request from '../utils/request'

export function listLogininfor(params) {
  return request({ url: '/monitor/logininfor/list', method: 'get', params })
}
export function delLogininfor(ids) {
  return request({ url: '/monitor/logininfor/' + ids, method: 'delete' })
}
export function cleanLogininfor() {
  return request({ url: '/monitor/logininfor/clean', method: 'delete' })
}
