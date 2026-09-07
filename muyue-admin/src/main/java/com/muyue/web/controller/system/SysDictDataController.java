package com.muyue.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.annotation.Log;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.TableDataInfo;
import com.muyue.common.core.domain.entity.SysDictData;
import com.muyue.common.enums.BusinessType;
import com.muyue.common.exception.ServiceException;
import com.muyue.system.service.ISysDictDataService;
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
 * 字典数据控制器
 *
 * @author muyue
 */
@RestController
@RequestMapping("/system/dict/data")
@RequiredArgsConstructor
public class SysDictDataController extends BaseController {

    private final ISysDictDataService dictDataService;

    @PreAuthorize("@ps.hasPermi('system:dict:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysDictData dictData) {
        Page<SysDictData> page = dictDataService.selectPage(getPage(), dictData);
        return new TableDataInfo<>(page.getRecords(), page.getTotal());
    }

    @PreAuthorize("@ps.hasPermi('system:dict:query')")
    @GetMapping("/{dictCode}")
    public R<SysDictData> getInfo(@PathVariable Long dictCode) {
        return R.ok(dictDataService.getById(dictCode));
    }

    /**
     * 根据字典类型查询字典数据（前端下拉 / 回显）
     */
    @PreAuthorize("@ps.hasPermi('system:dict:list')")
    @GetMapping("/type/{dictType}")
    public R<List<SysDictData>> dictType(@PathVariable String dictType) {
        return R.ok(dictDataService.selectDictDataByType(dictType));
    }

    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    @PreAuthorize("@ps.hasPermi('system:dict:add')")
    @PostMapping
    public R<?> add(@RequestBody SysDictData dictData) {
        if (!dictDataService.checkDictLabelUnique(dictData)) {
            throw new ServiceException("新增字典数据'" + dictData.getDictLabel() + "'失败，该字典类型下标签已存在");
        }
        return R.ok(dictDataService.save(dictData));
    }

    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ps.hasPermi('system:dict:edit')")
    @PutMapping
    public R<?> edit(@RequestBody SysDictData dictData) {
        if (!dictDataService.checkDictLabelUnique(dictData)) {
            throw new ServiceException("修改字典数据'" + dictData.getDictLabel() + "'失败，该字典类型下标签已存在");
        }
        return R.ok(dictDataService.updateById(dictData));
    }

    @Log(title = "字典数据", businessType = BusinessType.DELETE)
    @PreAuthorize("@ps.hasPermi('system:dict:remove')")
    @DeleteMapping("/{dictCodes}")
    public R<?> remove(@PathVariable String dictCodes) {
        List<Long> codes = Arrays.stream(dictCodes.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return R.ok(dictDataService.removeByIds(codes));
    }
}
