import request from '../utils/request'

export function listUser(params) {
  return request({ url: '/system/user/list', method: 'get', params })
}
export function getUser(userId) {
  return request({ url: '/system/user/' + userId, method: 'get' })
}
export function addUser(data) {
  return request({ url: '/system/user', method: 'post', data })
}
export function updateUser(data) {
  return request({ url: '/system/user', method: 'put', data })
}
export function delUser(ids) {
  return request({ url: '/system/user/' + ids, method: 'delete' })
}
export function deptTree() {
  return request({ url: '/system/user/deptTree', method: 'get' })
}
export function changeUserStatus(userId, status) {
  return request({ url: '/system/user/changeStatus', method: 'put', data: { userId, status } })
}
