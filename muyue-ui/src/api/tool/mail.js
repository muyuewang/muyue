import request from '@/utils/request'

// 获取邮箱配置
export function getMailConfig() {
  return request({ url: '/tool/mail/config', method: 'get' })
}
// 保存邮箱配置
export function saveMailConfig(data) {
  return request({ url: '/tool/mail/config', method: 'put', data })
}
// 发送测试邮件（富文本）
export function sendMail(data) {
  return request({ url: '/tool/mail/send', method: 'post', data })
}
