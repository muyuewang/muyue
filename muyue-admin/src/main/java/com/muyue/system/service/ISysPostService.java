package com.muyue.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyue.common.core.domain.entity.SysPost;

import java.util.List;

/**
 * 岗位服务接口
 *
 * @author muyue
 */
public interface ISysPostService extends IService<SysPost> {

    /**
     * 分页查询岗位
     */
    Page<SysPost> selectPage(Page<SysPost> page, SysPost post);

    /**
     * 校验岗位编码是否唯一
     */
    boolean checkPostCodeUnique(SysPost post);

    /**
     * 校验岗位名称是否唯一
     */
    boolean checkPostNameUnique(SysPost post);

    /**
     * 查询所有正常岗位（下拉用）
     */
    List<SysPost> selectPostAll();
}
