import request from '@/utils/request'

// 订单列表
export function listOrder(query) {
  return request({ url: '/order/list', method: 'get', params: query })
}
// 订单详情（含明细）
export function getOrder(orderId) {
  return request({ url: '/order/' + orderId, method: 'get' })
}
// 新增订单（含明细）
export function addOrder(data) {
  return request({ url: '/order', method: 'post', data })
}
// 修改订单（含明细）
export function updateOrder(data) {
  return request({ url: '/order', method: 'put', data })
}
// 删除订单
export function delOrder(orderId) {
  return request({ url: '/order/' + orderId, method: 'delete' })
}
