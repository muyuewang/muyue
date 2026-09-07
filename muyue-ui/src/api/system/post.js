import request from '@/utils/request'

// 列表
export function listPost(query) {
  return request({ url: '/system/post/list', method: 'get', params: query })
}
// 详情
export function getPost(postId) {
  return request({ url: '/system/post/' + postId, method: 'get' })
}
// 新增
export function addPost(data) {
  return request({ url: '/system/post', method: 'post', data })
}
// 修改
export function updatePost(data) {
  return request({ url: '/system/post', method: 'put', data })
}
// 删除
export function delPost(postId) {
  return request({ url: '/system/post/' + postId, method: 'delete' })
}
