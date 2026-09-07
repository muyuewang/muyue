import request from '@/utils/request'

// 大屏统计数据
export function getScreenStats() {
  return request({ url: '/screen/stats', method: 'get' })
}
