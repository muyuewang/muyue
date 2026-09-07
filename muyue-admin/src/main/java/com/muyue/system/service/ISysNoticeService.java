package com.muyue.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyue.common.core.domain.entity.SysNotice;

/**
 * 通知公告服务接口
 *
 * @author muyue
 */
public interface ISysNoticeService extends IService<SysNotice> {

    /** 分页查询公告 */
    Page<SysNotice> selectPage(Page<SysNotice> page, SysNotice notice);

    /** 标记公告已读（幂等） */
    void markRead(Long userId, Long noticeId);

    /** 当前用户未读公告数 */
    int countUnread(Long userId);
}
