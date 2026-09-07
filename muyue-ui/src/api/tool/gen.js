import request from '@/utils/request'

// 库表列表
export function listGenTables(query) {
  return request({ url: '/tool/gen/db/list', method: 'get', params: query })
}
// 预览生成代码
export function previewGenTable(tableName) {
  return request({ url: '/tool/gen/preview/' + tableName, method: 'get' })
}
// 下载生成代码（zip）
export function downloadGenCode(tableName) {
  return request({ url: '/tool/gen/download/' + tableName, method: 'get', responseType: 'blob' })
}
