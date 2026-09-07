import request from '../utils/request'

/** 以 blob 方式下载（走 axios 拦截器带 token），保存为文件 */
export function downloadBlob(url, filename) {
  return request({ url, method: 'get', responseType: 'blob' }).then((blob) => {
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(link.href)
  })
}

/** 以 blob 方式打开（新窗口预览） */
export function openBlobUrl(url) {
  return request({ url, method: 'get', responseType: 'blob' }).then((blob) => {
    const u = window.URL.createObjectURL(blob)
    window.open(u, '_blank')
    setTimeout(() => window.URL.revokeObjectURL(u), 60000)
  })
}

export function listOnline(params) {
  return request({ url: '/monitor/online/list', method: 'get', params })
}
export function forceLogout(token) {
  return request({ url: '/monitor/online/' + token, method: 'delete' })
}

export function getServerInfo() {
  return request({ url: '/monitor/server', method: 'get' })
}

export function listGenTables(params) {
  return request({ url: '/tool/gen/db/list', method: 'get', params })
}
export function previewGen(tableName) {
  return request({ url: '/tool/gen/preview/' + tableName, method: 'get' })
}
export const genDownload = (tableName, filename) => downloadBlob('/tool/gen/download/' + tableName, filename)

export function listFiles(params) {
  return request({ url: '/tool/file/list', method: 'get', params })
}
export const filePreview = (fileId) => openBlobUrl('/tool/file/preview/' + fileId)
export const fileDownload = (fileId, fileName) => downloadBlob('/tool/file/download/' + fileId, fileName)

export function getMailConfig() {
  return request({ url: '/tool/mail/config', method: 'get' })
}
export function saveMailConfig(data) {
  return request({ url: '/tool/mail/config', method: 'put', data })
}
export function sendMail(data) {
  return request({ url: '/tool/mail/send', method: 'post', data })
}

export function getScreenStats() {
  return request({ url: '/screen/stats', method: 'get' })
}

export function getProfile() {
  return request({ url: '/system/user/profile', method: 'get' })
}
export function updateProfile(data) {
  return request({ url: '/system/user/profile', method: 'put', data })
}
export function updatePwd(data) {
  return request({ url: '/system/user/profile/updatePwd', method: 'put', data })
}
