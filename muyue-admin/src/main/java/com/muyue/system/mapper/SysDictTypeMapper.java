package com.muyue.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.core.domain.entity.SysDictType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典类型数据访问层
 *
 * @author muyue
 */
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {

    List<SysDictType> selectDictTypeList(Page<SysDictType> page, @Param("dictType") SysDictType dictType);
}
