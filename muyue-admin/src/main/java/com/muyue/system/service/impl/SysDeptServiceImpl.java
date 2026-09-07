package com.muyue.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyue.common.config.DbTypeHolder;
import com.muyue.common.core.domain.entity.SysDept;
import com.muyue.common.core.domain.vo.TreeSelect;
import com.muyue.common.exception.ServiceException;
import com.muyue.common.utils.TreeUtils;
import com.muyue.system.mapper.SysDeptMapper;
import com.muyue.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门服务实现
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements ISysDeptService {

    private final DbTypeHolder dbTypeHolder;

    @Override
    public List<SysDept> selectDeptList(SysDept dept) {
        return baseMapper.selectDeptList(dept);
    }

    @Override
    public List<SysDept> buildDeptTree(List<SysDept> depts) {
        List<SysDept> list = depts.stream()
                .peek(d -> d.setChildren(new ArrayList<>()))
                .toList();
        return TreeUtils.build(list, SysDept::getDeptId, SysDept::getParentId, SysDept::setChildren);
    }

    @Override
    public List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts) {
        return buildDeptTree(depts).stream().map(this::toTreeSelect).toList();
    }

    @Override
    public SysDept selectDeptById(Long deptId) {
        return baseMapper.selectById(deptId);
    }

    @Override
    public boolean hasChildByDeptId(Long deptId) {
        return baseMapper.selectNormalChildrenDeptById(deptId) > 0;
    }

    @Override
    public boolean checkDeptExistUser(Long deptId) {
        return baseMapper.countUserByDeptId(deptId) > 0;
    }

    @Override
    public boolean checkDeptNameUnique(SysDept dept) {
        Long deptId = dept.getDeptId() == null ? -1L : dept.getDeptId();
        SysDept info = baseMapper.selectOne(new LambdaQueryWrapper<SysDept>()
                .eq(SysDept::getDeptName, dept.getDeptName())
                .eq(SysDept::getParentId, dept.getParentId())
                .last(dbTypeHolder.limitOne()));
        return info == null || info.getDeptId().equals(deptId);
    }

    @Override
    public boolean insertDept(SysDept dept) {
        if (!checkDeptNameUnique(dept)) {
            throw new ServiceException("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        SysDept parent = dept.getParentId() != null ? baseMapper.selectById(dept.getParentId()) : null;
        if (parent != null && "1".equals(parent.getStatus())) {
            throw new ServiceException("上级部门已停用，不允许新增");
        }
        dept.setAncestors(parent == null ? "0" : parent.getAncestors() + "," + dept.getParentId());
        return baseMapper.insert(dept) > 0;
    }

    @Override
    public boolean updateDept(SysDept dept) {
        if (!checkDeptNameUnique(dept)) {
            throw new ServiceException("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        if (dept.getParentId() != null && dept.getParentId().equals(dept.getDeptId())) {
            throw new ServiceException("上级部门不能是自己");
        }
        SysDept parent = dept.getParentId() != null ? baseMapper.selectById(dept.getParentId()) : null;
        dept.setAncestors(parent == null ? "0" : parent.getAncestors() + "," + dept.getParentId());
        return baseMapper.updateById(dept) > 0;
    }

    @Override
    public boolean deleteDeptById(Long deptId) {
        if (hasChildByDeptId(deptId)) {
            throw new ServiceException("存在下级部门，不允许删除");
        }
        if (checkDeptExistUser(deptId)) {
            throw new ServiceException("部门下存在用户，不允许删除");
        }
        return baseMapper.deleteById(deptId) > 0;
    }

    @Override
    public List<Long> selectDeptIdsByRoleId(Long roleId) {
        // 预留：按角色数据范围返回部门ID集合（1 全部数据时返回空）
        return List.of();
    }

    private TreeSelect toTreeSelect(SysDept dept) {
        if (dept.getChildren() == null || dept.getChildren().isEmpty()) {
            return new TreeSelect(dept.getDeptId(), dept.getDeptName());
        }
        return new TreeSelect(dept.getDeptId(), dept.getDeptName(),
                dept.getChildren().stream().map(this::toTreeSelect).toList());
    }
}
