import request from '@/utils/request'

/**
 * 查询角色列表
 */
export function listRole(query) {
  return request({
    url: '/system/role/list',
    method: 'get',
    params: query
  })
}

/**
 * 查询角色详情
 */
export function getRole(roleId) {
  return request({
    url: '/system/role/' + roleId,
    method: 'get'
  })
}

/**
 * 查询全部角色（下拉）
 */
export function optionselect() {
  return request({
    url: '/system/role/optionselect',
    method: 'get'
  })
}

/**
 * 新增角色
 */
export function addRole(data) {
  return request({
    url: '/system/role',
    method: 'post',
    data
  })
}

/**
 * 修改角色
 */
export function updateRole(data) {
  return request({
    url: '/system/role',
    method: 'put',
    data
  })
}

/**
 * 删除角色
 */
export function delRole(roleIds) {
  return request({
    url: '/system/role/' + roleIds,
    method: 'delete'
  })
}

/**
 * 修改角色状态
 */
export function changeRoleStatus(roleId, status) {
  return request({
    url: '/system/role/changeStatus',
    method: 'put',
    data: { roleId, status }
  })
}
