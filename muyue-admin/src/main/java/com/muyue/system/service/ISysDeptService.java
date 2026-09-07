package com.muyue.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.muyue.common.core.domain.entity.SysDept;
import com.muyue.common.core.domain.vo.TreeSelect;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author muyue
 */
public interface ISysDeptService extends IService<SysDept> {

    /**
     * 查询部门列表（平铺）
     */
    List<SysDept> selectDeptList(SysDept dept);

    /**
     * 构建部门树
     */
    List<SysDept> buildDeptTree(List<SysDept> depts);

    /**
     * 构建部门下拉树
     */
    List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts);

    /**
     * 根据部门ID查询
     */
    SysDept selectDeptById(Long deptId);

    /**
     * 是否存在子部门
     */
    boolean hasChildByDeptId(Long deptId);

    /**
     * 部门下是否存在用户
     */
    boolean checkDeptExistUser(Long deptId);

    /**
     * 校验部门名称是否唯一
     */
    boolean checkDeptNameUnique(SysDept dept);

    /**
     * 新增部门
     */
    boolean insertDept(SysDept dept);

    /**
     * 修改部门
     */
    boolean updateDept(SysDept dept);

    /**
     * 删除部门
     */
    boolean deleteDeptById(Long deptId);

    /**
     * 根据角色ID查询部门ID集合（预留数据权限）
     */
    List<Long> selectDeptIdsByRoleId(Long roleId);
}
