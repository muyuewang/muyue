package com.muyue.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyue.common.config.DbTypeHolder;
import com.muyue.common.core.domain.entity.SysUser;
import com.muyue.common.exception.ServiceException;
import com.muyue.common.utils.StringUtils;
import com.muyue.system.mapper.SysUserMapper;
import com.muyue.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 用户服务实现
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final PasswordEncoder passwordEncoder;
    private final DbTypeHolder dbTypeHolder;

    @Override
    public Page<SysUser> selectUserPage(Page<SysUser> page, SysUser user) {
        List<SysUser> list = baseMapper.selectUserList(page, user);
        if (list != null && !list.isEmpty()) {
            page.setRecords(list);
        }
        for (SysUser record : page.getRecords()) {
            record.setRoles(null);
        }
        return page;
    }

    @Override
    public SysUser selectUserByUserName(String userName) {
        return baseMapper.selectUserByUserName(userName);
    }

    @Override
    public SysUser selectUserById(Long userId) {
        return baseMapper.selectUserById(userId);
    }

    @Override
    public Set<String> selectRoleKeysByUserId(Long userId) {
        return baseMapper.selectRoleKeysByUserId(userId);
    }

    @Override
    public boolean checkUserNameUnique(SysUser user) {
        Long userId = user.getUserId() == null ? -1L : user.getUserId();
        SysUser info = baseMapper.selectUserByUserName(user.getUserName());
        return info == null || info.getUserId().equals(userId);
    }

    @Override
    public boolean checkPhoneUnique(SysUser user) {
        if (StringUtils.isBlank(user.getPhonenumber())) {
            return true;
        }
        Long userId = user.getUserId() == null ? -1L : user.getUserId();
        SysUser info = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhonenumber, user.getPhonenumber())
                .last(dbTypeHolder.limitOne()));
        return info == null || info.getUserId().equals(userId);
    }

    @Override
    public boolean checkEmailUnique(SysUser user) {
        if (StringUtils.isBlank(user.getEmail())) {
            return true;
        }
        Long userId = user.getUserId() == null ? -1L : user.getUserId();
        SysUser info = baseMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, user.getEmail())
                .last(dbTypeHolder.limitOne()));
        return info == null || info.getUserId().equals(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertUser(SysUser user) {
        if (!checkUserNameUnique(user)) {
            throw new ServiceException("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        if (!checkPhoneUnique(user)) {
            throw new ServiceException("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        if (!checkEmailUnique(user)) {
            throw new ServiceException("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        if (StringUtils.isBlank(user.getPassword())) {
            throw new ServiceException("密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        int rows = baseMapper.insert(user);
        insertUserRole(user);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        Long userId = user.getUserId();
        if (!checkUserNameUnique(user)) {
            throw new ServiceException("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        if (!checkPhoneUnique(user)) {
            throw new ServiceException("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        if (!checkEmailUnique(user)) {
            throw new ServiceException("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        // 密码不在修改界面处理
        user.setPassword(null);
        int rows = baseMapper.updateById(user);
        if (rows > 0 && user.getRoleIds() != null) {
            baseMapper.deleteUserRoleByUserId(userId);
            insertUserRole(user);
        }
        return rows > 0;
    }

    @Override
    public boolean updateUserStatus(SysUser user) {
        SysUser update = new SysUser();
        update.setUserId(user.getUserId());
        update.setStatus(user.getStatus());
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public boolean resetUserPassword(Long userId, String password) {
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setPassword(passwordEncoder.encode(password));
        return baseMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }
        if (userIds.contains(com.muyue.common.utils.SecurityUtils.SUPER_ADMIN_ID)) {
            throw new ServiceException("超级管理员不允许删除");
        }
        baseMapper.deleteUserRoles(userIds);
        return baseMapper.delete(new LambdaQueryWrapper<SysUser>().in(SysUser::getUserId, userIds)) > 0;
    }

    /**
     * 新增用户角色关系
     */
    private void insertUserRole(SysUser user) {
        Long[] roles = user.getRoleIds();
        if (roles == null || roles.length == 0) {
            return;
        }
        for (Long roleId : roles) {
            if (roleId != null) {
                baseMapper.insertUserRole(user.getUserId(), roleId);
            }
        }
    }
}
