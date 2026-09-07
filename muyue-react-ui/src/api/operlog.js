import request from '../utils/request'

export function listOperlog(params) {
  return request({ url: '/monitor/operlog/list', method: 'get', params })
}
export function delOperlog(ids) {
  return request({ url: '/monitor/operlog/' + ids, method: 'delete' })
}
export function cleanOperlog() {
  return request({ url: '/monitor/operlog/clean', method: 'delete' })
}
