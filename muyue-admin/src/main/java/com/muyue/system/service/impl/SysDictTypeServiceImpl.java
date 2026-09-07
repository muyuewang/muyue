package com.muyue.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyue.common.core.domain.entity.SysDictData;
import com.muyue.common.core.domain.entity.SysDictType;
import com.muyue.common.exception.ServiceException;
import com.muyue.system.mapper.SysDictTypeMapper;
import com.muyue.system.service.ISysDictDataService;
import com.muyue.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典类型服务实现
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements ISysDictTypeService {

    private final ISysDictDataService dictDataService;

    @Override
    public Page<SysDictType> selectPage(Page<SysDictType> page, SysDictType dictType) {
        List<SysDictType> list = baseMapper.selectDictTypeList(page, dictType);
        if (list != null && !list.isEmpty()) {
            page.setRecords(list);
        }
        return page;
    }

    @Override
    public boolean checkDictTypeUnique(SysDictType dictType) {
        Long dictId = dictType.getDictId() == null ? -1L : dictType.getDictId();
        SysDictType one = getOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictType.getDictType())
                .last("LIMIT 1"));
        return one == null || one.getDictId().equals(dictId);
    }

    @Override
    public boolean removeDictTypeByIds(List<Long> dictIds) {
        for (Long dictId : dictIds) {
            SysDictType type = getById(dictId);
            if (type == null) {
                continue;
            }
            long count = dictDataService.count(new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDictType, type.getDictType()));
            if (count > 0) {
                throw new ServiceException("字典'" + type.getDictName() + "'已关联数据，不能删除");
            }
            removeById(dictId);
        }
        return true;
    }

    @Override
    public List<SysDictType> optionselect() {
        return list(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getStatus, "0")
                .orderByAsc(SysDictType::getDictId));
    }
}
