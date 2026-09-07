package com.muyue.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyue.common.core.domain.entity.SysNotice;
import com.muyue.common.utils.StringUtils;
import com.muyue.system.mapper.SysNoticeMapper;
import com.muyue.system.service.ISysNoticeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知公告服务实现
 *
 * @author muyue
 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

    @Override
    public Page<SysNotice> selectPage(Page<SysNotice> page, SysNotice notice) {
        List<SysNotice> list = baseMapper.selectNoticeList(page, notice);
        page.setRecords(list);
        return page;
    }

    @Override
    public void markRead(Long userId, Long noticeId) {
        if (userId == null || noticeId == null) {
            return;
        }
        if (baseMapper.selectNoticeReadCount(userId, noticeId) == 0) {
            baseMapper.insertNoticeRead(userId, noticeId);
        }
    }

    @Override
    public int countUnread(Long userId) {
        return baseMapper.selectUnreadCount(userId);
    }

    @Override
    public boolean save(SysNotice notice) {
        if (StringUtils.isBlank(notice.getStatus())) {
            notice.setStatus("0");
        }
        return baseMapper.insert(notice) > 0;
    }
}
