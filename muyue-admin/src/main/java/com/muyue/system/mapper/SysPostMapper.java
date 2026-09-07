package com.muyue.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.core.domain.entity.SysPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 岗位数据访问层
 *
 * @author muyue
 */
public interface SysPostMapper extends BaseMapper<SysPost> {

    List<SysPost> selectPostList(Page<SysPost> page, @Param("post") SysPost post);

    /**
     * 根据用户ID查询岗位
     */
    List<SysPost> selectPostsByUserId(@Param("userId") Long userId);

    /**
     * 新增用户岗位关系
     */
    int insertUserPost(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 删除用户岗位关系
     */
    int deleteUserPostByUserId(@Param("userId") Long userId);
}
