import request from '../utils/request'

export function listNotice(params) {
  return request({ url: '/system/notice/list', method: 'get', params })
}
export function getNotice(noticeId) {
  return request({ url: '/system/notice/' + noticeId, method: 'get' })
}
export function addNotice(data) {
  return request({ url: '/system/notice', method: 'post', data })
}
export function updateNotice(data) {
  return request({ url: '/system/notice', method: 'put', data })
}
export function delNotice(ids) {
  return request({ url: '/system/notice/' + ids, method: 'delete' })
}
export function unreadCount() {
  return request({ url: '/system/notice/unreadCount', method: 'get' })
}
