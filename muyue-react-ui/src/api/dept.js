import request from '../utils/request'

export function listDept(params) {
  return request({ url: '/system/dept/list', method: 'get', params })
}
export function getDept(deptId) {
  return request({ url: '/system/dept/' + deptId, method: 'get' })
}
export function deptTreeSelect() {
  return request({ url: '/system/dept/treeselect', method: 'get' })
}
export function addDept(data) {
  return request({ url: '/system/dept', method: 'post', data })
}
export function updateDept(data) {
  return request({ url: '/system/dept', method: 'put', data })
}
export function delDept(deptId) {
  return request({ url: '/system/dept/' + deptId, method: 'delete' })
}
