package com.muyue.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.core.domain.entity.SysUser;
import com.muyue.common.core.domain.entity.SysUserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 用户数据访问层
 *
 * @author muyue
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 分页查询用户列表
     */
    List<SysUser> selectUserList(Page<SysUser> page, @Param("user") SysUser user);

    /**
     * 通过用户名查询用户
     */
    SysUser selectUserByUserName(@Param("userName") String userName);

    /**
     * 通过用户ID查询用户
     */
    SysUser selectUserById(@Param("userId") Long userId);

    /**
     * 查询用户拥有的角色标识
     */
    Set<String> selectRoleKeysByUserId(@Param("userId") Long userId);

    /**
     * 新增用户角色关系（单条，跨库通用）
     */
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 通过用户ID删除用户角色关系
     */
    int deleteUserRoleByUserId(@Param("userId") Long userId);

    /**
     * 批量删除用户角色关系
     */
    int deleteUserRoles(@Param("userIds") List<Long> userIds);
}
