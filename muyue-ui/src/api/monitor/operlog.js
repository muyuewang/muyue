import request from '@/utils/request'

/**
 * 查询操作日志
 */
export function listOperlog(query) {
  return request({
    url: '/monitor/operlog/list',
    method: 'get',
    params: query
  })
}

/**
 * 删除操作日志
 */
export function delOperlog(operIds) {
  return request({
    url: '/monitor/operlog/' + operIds,
    method: 'delete'
  })
}

/**
 * 清空操作日志
 */
export function cleanOperlog() {
  return request({
    url: '/monitor/operlog/clean',
    method: 'delete'
  })
}
