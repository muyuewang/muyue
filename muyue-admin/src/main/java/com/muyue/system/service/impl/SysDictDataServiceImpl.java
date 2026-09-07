package com.muyue.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyue.common.core.domain.entity.SysDictData;
import com.muyue.common.exception.ServiceException;
import com.muyue.system.mapper.SysDictDataMapper;
import com.muyue.system.service.ISysDictDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典数据服务实现
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements ISysDictDataService {

    @Override
    public Page<SysDictData> selectPage(Page<SysDictData> page, SysDictData dictData) {
        List<SysDictData> list = baseMapper.selectDictDataList(page, dictData);
        if (list != null && !list.isEmpty()) {
            page.setRecords(list);
        }
        return page;
    }

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        return list(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, "0")
                .orderByAsc(SysDictData::getDictSort));
    }

    @Override
    public boolean checkDictLabelUnique(SysDictData dictData) {
        Long dictCode = dictData.getDictCode() == null ? -1L : dictData.getDictCode();
        SysDictData one = getOne(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictLabel, dictData.getDictLabel())
                .eq(SysDictData::getDictType, dictData.getDictType())
                .last("LIMIT 1"));
        return one == null || one.getDictCode().equals(dictCode);
    }

    @Override
    public int deleteDictDataByType(String dictType) {
        return baseMapper.delete(new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getDictType, dictType));
    }
}
