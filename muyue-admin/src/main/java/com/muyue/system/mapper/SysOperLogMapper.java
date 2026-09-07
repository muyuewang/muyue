package com.muyue.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.core.domain.entity.SysOperLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志数据访问层
 *
 * @author muyue
 */
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

    /**
     * 分页查询操作日志
     */
    List<SysOperLog> selectOperLogList(Page<SysOperLog> page, @Param("operLog") SysOperLog operLog);

    /**
     * 清空操作日志
     */
    int cleanOperLog();
}
