package com.muyue.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyue.common.core.domain.entity.SysDictData;

import java.util.List;

/**
 * 字典数据服务接口
 *
 * @author muyue
 */
public interface ISysDictDataService extends IService<SysDictData> {

    /**
     * 分页查询字典数据
     */
    Page<SysDictData> selectPage(Page<SysDictData> page, SysDictData dictData);

    /**
     * 根据字典类型查询字典数据列表（前端下拉/回显）
     */
    List<SysDictData> selectDictDataByType(String dictType);

    /**
     * 校验字典标签是否唯一
     */
    boolean checkDictLabelUnique(SysDictData dictData);

    /**
     * 根据类型删除字典数据
     */
    int deleteDictDataByType(String dictType);
}
