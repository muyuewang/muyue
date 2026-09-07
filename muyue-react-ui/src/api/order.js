import request from '../utils/request'

export function listOrder(params) {
  return request({ url: '/order/list', method: 'get', params })
}
export function getOrder(orderId) {
  return request({ url: '/order/' + orderId, method: 'get' })
}
export function addOrder(data) {
  return request({ url: '/order', method: 'post', data })
}
export function updateOrder(data) {
  return request({ url: '/order', method: 'put', data })
}
export function delOrder(ids) {
  return request({ url: '/order/' + ids, method: 'delete' })
}
