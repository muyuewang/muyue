package com.muyue.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.annotation.Log;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.TableDataInfo;
import com.muyue.common.core.domain.entity.SysNotice;
import com.muyue.common.enums.BusinessType;
import com.muyue.common.utils.SecurityUtils;
import com.muyue.system.service.ISysNoticeService;
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
 * 通知公告控制器
 *
 * @author muyue
 */
@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
public class SysNoticeController extends BaseController {

    private final ISysNoticeService noticeService;

    /** 公告列表（分页，不含正文，携带当前用户已读状态） */
    @PreAuthorize("@ps.hasPermi('system:notice:list')")
    @GetMapping("/list")
    public TableDataInfo<SysNotice> list(SysNotice notice) {
        notice.setUserId(SecurityUtils.getUserId());
        Page<SysNotice> page = noticeService.selectPage(getPage(), notice);
        return new TableDataInfo<>(page.getRecords(), page.getTotal());
    }

    /** 公告详情（含正文，查看即标记已读） */
    @PreAuthorize("@ps.hasPermi('system:notice:query')")
    @GetMapping("/{noticeId}")
    public R<SysNotice> getInfo(@PathVariable Long noticeId) {
        SysNotice notice = noticeService.getById(noticeId);
        if (notice != null) {
            noticeService.markRead(SecurityUtils.getUserId(), noticeId);
        }
        return R.ok(notice);
    }

    /** 当前用户未读公告数 */
    @PreAuthorize("@ps.hasPermi('system:notice:list')")
    @GetMapping("/unreadCount")
    public R<Integer> unreadCount() {
        return R.ok(noticeService.countUnread(SecurityUtils.getUserId()));
    }

    /** 新增公告 */
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PreAuthorize("@ps.hasPermi('system:notice:add')")
    @PostMapping
    public R<Void> add(@RequestBody SysNotice notice) {
        return toAjax(noticeService.save(notice));
    }

    /** 修改公告 */
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ps.hasPermi('system:notice:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody SysNotice notice) {
        return toAjax(noticeService.updateById(notice));
    }

    /** 删除公告 */
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @PreAuthorize("@ps.hasPermi('system:notice:remove')")
    @DeleteMapping("/{noticeIds}")
    public R<Void> remove(@PathVariable String noticeIds) {
        List<Long> ids = Arrays.stream(noticeIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return toAjax(noticeService.removeByIds(ids));
    }
}
