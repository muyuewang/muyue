package com.muyue.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyue.common.core.domain.entity.SysDictType;

import java.util.List;

/**
 * 字典类型服务接口
 *
 * @author muyue
 */
public interface ISysDictTypeService extends IService<SysDictType> {

    /**
     * 分页查询字典类型
     */
    Page<SysDictType> selectPage(Page<SysDictType> page, SysDictType dictType);

    /**
     * 校验字典类型是否唯一
     */
    boolean checkDictTypeUnique(SysDictType dictType);

    /**
     * 批量删除字典类型（删除前校验是否已关联字典数据）
     */
    boolean removeDictTypeByIds(List<Long> dictIds);

    /**
     * 查询所有字典类型（下拉用）
     */
    List<SysDictType> optionselect();
}
