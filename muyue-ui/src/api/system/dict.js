import request from '@/utils/request'

// 字典类型
export function listType(query) {
  return request({ url: '/system/dict/type/list', method: 'get', params: query })
}
export function getType(dictId) {
  return request({ url: '/system/dict/type/' + dictId, method: 'get' })
}
export function addType(data) {
  return request({ url: '/system/dict/type', method: 'post', data })
}
export function updateType(data) {
  return request({ url: '/system/dict/type', method: 'put', data })
}
export function delType(dictIds) {
  return request({ url: '/system/dict/type/' + dictIds, method: 'delete' })
}
export function optionselect() {
  return request({ url: '/system/dict/type/optionselect', method: 'get' })
}

// 字典数据
export function listData(query) {
  return request({ url: '/system/dict/data/list', method: 'get', params: query })
}
export function getData(dictCode) {
  return request({ url: '/system/dict/data/' + dictCode, method: 'get' })
}
export function addData(data) {
  return request({ url: '/system/dict/data', method: 'post', data })
}
export function updateData(data) {
  return request({ url: '/system/dict/data', method: 'put', data })
}
export function delData(dictCodes) {
  return request({ url: '/system/dict/data/' + dictCodes, method: 'delete' })
}
