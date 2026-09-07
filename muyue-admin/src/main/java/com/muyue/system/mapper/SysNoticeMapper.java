package com.muyue.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.core.domain.entity.SysNotice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知公告数据访问层
 *
 * @author muyue
 */
public interface SysNoticeMapper extends BaseMapper<SysNotice> {

    List<SysNotice> selectNoticeList(Page<SysNotice> page, @Param("notice") SysNotice notice);

    /** 标记公告已读 */
    int insertNoticeRead(@Param("userId") Long userId, @Param("noticeId") Long noticeId);

    /** 查询已读记录数 */
    int selectNoticeReadCount(@Param("userId") Long userId, @Param("noticeId") Long noticeId);

    /** 统计当前用户未读公告数 */
    int selectUnreadCount(@Param("userId") Long userId);
}
