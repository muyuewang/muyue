package com.muyue.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.annotation.Log;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.TableDataInfo;
import com.muyue.common.core.domain.entity.SysPost;
import com.muyue.common.enums.BusinessType;
import com.muyue.common.exception.ServiceException;
import com.muyue.system.service.ISysPostService;
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
 * 岗位管理控制器
 *
 * @author muyue
 */
@RestController
@RequestMapping("/system/post")
@RequiredArgsConstructor
public class SysPostController extends BaseController {

    private final ISysPostService postService;

    @PreAuthorize("@ps.hasPermi('system:post:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysPost post) {
        Page<SysPost> page = postService.selectPage(getPage(), post);
        return new TableDataInfo<>(page.getRecords(), page.getTotal());
    }

    @PreAuthorize("@ps.hasPermi('system:post:query')")
    @GetMapping("/{postId}")
    public R<SysPost> getInfo(@PathVariable Long postId) {
        return R.ok(postService.getById(postId));
    }

    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ps.hasPermi('system:post:add')")
    @PostMapping
    public R<?> add(@RequestBody SysPost post) {
        if (!postService.checkPostCodeUnique(post)) {
            throw new ServiceException("新增岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        }
        if (!postService.checkPostNameUnique(post)) {
            throw new ServiceException("新增岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        }
        return R.ok(postService.save(post));
    }

    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ps.hasPermi('system:post:edit')")
    @PutMapping
    public R<?> edit(@RequestBody SysPost post) {
        if (!postService.checkPostCodeUnique(post)) {
            throw new ServiceException("修改岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        }
        if (!postService.checkPostNameUnique(post)) {
            throw new ServiceException("修改岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        }
        return R.ok(postService.updateById(post));
    }

    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ps.hasPermi('system:post:remove')")
    @DeleteMapping("/{postIds}")
    public R<?> remove(@PathVariable String postIds) {
        List<Long> ids = Arrays.stream(postIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return R.ok(postService.removeByIds(ids));
    }
}
