import request from '@/utils/request'

// 服务器监控信息
export function getServer() {
  return request({ url: '/monitor/server', method: 'get' })
}
