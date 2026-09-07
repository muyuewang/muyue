import request from '../utils/request'

export function listPost(params) {
  return request({ url: '/system/post/list', method: 'get', params })
}
export function getPost(postId) {
  return request({ url: '/system/post/' + postId, method: 'get' })
}
export function addPost(data) {
  return request({ url: '/system/post', method: 'post', data })
}
export function updatePost(data) {
  return request({ url: '/system/post', method: 'put', data })
}
export function delPost(ids) {
  return request({ url: '/system/post/' + ids, method: 'delete' })
}
