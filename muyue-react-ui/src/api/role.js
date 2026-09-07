import request from '../utils/request'

export function listRole(params) {
  return request({ url: '/system/role/list', method: 'get', params })
}
export function getRole(roleId) {
  return request({ url: '/system/role/' + roleId, method: 'get' })
}
export function addRole(data) {
  return request({ url: '/system/role', method: 'post', data })
}
export function updateRole(data) {
  return request({ url: '/system/role', method: 'put', data })
}
export function changeRoleStatus(roleId, status) {
  return request({ url: '/system/role/changeStatus', method: 'put', data: { roleId, status } })
}
export function delRole(ids) {
  return request({ url: '/system/role/' + ids, method: 'delete' })
}
export function roleMenuTree(roleId) {
  return request({ url: '/system/menu/roleMenuTreeselect/' + roleId, method: 'get' })
}
