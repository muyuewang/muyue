import request from '@/utils/request'

// 附件列表
export function listFile(query) {
  return request({ url: '/tool/file/list', method: 'get', params: query })
}
// 上传附件
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({ url: '/tool/file/upload', method: 'post', data: formData })
}
// 在线预览（blob）
export function previewFile(fileId) {
  return request({ url: '/tool/file/preview/' + fileId, method: 'get', responseType: 'blob' })
}
// 下载附件（blob）
export function downloadFile(fileId) {
  return request({ url: '/tool/file/download/' + fileId, method: 'get', responseType: 'blob' })
}
// 删除附件
export function delFile(fileIds) {
  return request({ url: '/tool/file/' + fileIds, method: 'delete' })
}
