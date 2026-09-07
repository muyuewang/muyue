package com.muyue.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.core.domain.entity.SysDictData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据访问层
 *
 * @author muyue
 */
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

    List<SysDictData> selectDictDataList(Page<SysDictData> page, @Param("dictData") SysDictData dictData);
}
