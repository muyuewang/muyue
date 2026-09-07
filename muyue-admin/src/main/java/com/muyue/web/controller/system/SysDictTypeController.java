package com.muyue.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.annotation.Log;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.TableDataInfo;
import com.muyue.common.core.domain.entity.SysDictType;
import com.muyue.common.enums.BusinessType;
import com.muyue.common.exception.ServiceException;
import com.muyue.common.utils.StringUtils;
import com.muyue.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典类型控制器
 *
 * @author muyue
 */
@RestController
@RequestMapping("/system/dict/type")
@RequiredArgsConstructor
public class SysDictTypeController extends BaseController {

    private final ISysDictTypeService dictTypeService;

    @PreAuthorize("@ps.hasPermi('system:dict:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysDictType dictType) {
        Page<SysDictType> page = dictTypeService.selectPage(getPage(), dictType);
        return new TableDataInfo<>(page.getRecords(), page.getTotal());
    }

    @PreAuthorize("@ps.hasPermi('system:dict:query')")
    @GetMapping("/{dictId}")
    public R<SysDictType> getInfo(@PathVariable Long dictId) {
        return R.ok(dictTypeService.getById(dictId));
    }

    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    @PreAuthorize("@ps.hasPermi('system:dict:add')")
    @PostMapping
    public R<?> add(@RequestBody SysDictType dictType) {
        if (!dictTypeService.checkDictTypeUnique(dictType)) {
            throw new ServiceException("新增字典'" + dictType.getDictName() + "'失败，字典类型已存在");
        }
        return R.ok(dictTypeService.save(dictType));
    }

    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ps.hasPermi('system:dict:edit')")
    @PutMapping
    public R<?> edit(@RequestBody SysDictType dictType) {
        if (!dictTypeService.checkDictTypeUnique(dictType)) {
            throw new ServiceException("修改字典'" + dictType.getDictName() + "'失败，字典类型已存在");
        }
        return R.ok(dictTypeService.updateById(dictType));
    }

    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @PreAuthorize("@ps.hasPermi('system:dict:remove')")
    @DeleteMapping("/{dictIds}")
    public R<?> remove(@PathVariable String dictIds) {
        List<Long> ids = Arrays.stream(dictIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return R.ok(dictTypeService.removeDictTypeByIds(ids));
    }

    @PreAuthorize("@ps.hasPermi('system:dict:list')")
    @GetMapping("/optionselect")
    public R<List<SysDictType>> optionselect() {
        return R.ok(dictTypeService.optionselect());
    }
}
