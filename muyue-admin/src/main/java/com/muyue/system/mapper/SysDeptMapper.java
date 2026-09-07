package com.muyue.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.muyue.common.core.domain.entity.SysDept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门数据访问层
 *
 * @author muyue
 */
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 查询部门列表
     */
    List<SysDept> selectDeptList(@Param("dept") SysDept dept);

    /**
     * 查询部门下的子部门数量（正常状态）
     */
    int selectNormalChildrenDeptById(@Param("deptId") Long deptId);

    /**
     * 查询部门下的用户数量
     */
    int countUserByDeptId(@Param("deptId") Long deptId);
}
