package com.muyue.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyue.common.core.domain.entity.SysUser;

import java.util.List;
import java.util.Set;

/**
 * 用户服务接口
 *
 * @author muyue
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 分页查询用户列表
     */
    Page<SysUser> selectUserPage(Page<SysUser> page, SysUser user);

    /**
     * 根据用户名查询用户
     */
    SysUser selectUserByUserName(String userName);

    /**
     * 根据用户ID查询用户
     */
    SysUser selectUserById(Long userId);

    /**
     * 查询用户所属角色标识
     */
    Set<String> selectRoleKeysByUserId(Long userId);

    /**
     * 校验用户名是否唯一
     */
    boolean checkUserNameUnique(SysUser user);

    /**
     * 校验手机号是否唯一
     */
    boolean checkPhoneUnique(SysUser user);

    /**
     * 校验邮箱是否唯一
     */
    boolean checkEmailUnique(SysUser user);

    /**
     * 新增用户
     */
    boolean insertUser(SysUser user);

    /**
     * 修改用户
     */
    boolean updateUser(SysUser user);

    /**
     * 修改用户状态
     */
    boolean updateUserStatus(SysUser user);

    /**
     * 重置密码
     */
    boolean resetUserPassword(Long userId, String password);

    /**
     * 批量删除用户
     */
    boolean deleteUserByIds(List<Long> userIds);
}
