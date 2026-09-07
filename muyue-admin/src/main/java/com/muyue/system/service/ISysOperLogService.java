package com.muyue.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyue.common.core.domain.entity.SysOperLog;

import java.util.List;

/**
 * 操作日志服务接口
 *
 * @author muyue
 */
public interface ISysOperLogService extends IService<SysOperLog> {

    /**
     * 分页查询操作日志
     */
    Page<SysOperLog> selectOperLogPage(Page<SysOperLog> page, SysOperLog operLog);

    /**
     * 新增操作日志
     */
    void insertOperlog(SysOperLog operLog);

    /**
     * 查询操作日志
     */
    SysOperLog selectOperLogById(Long operId);

    /**
     * 批量删除操作日志
     */
    boolean deleteOperLogByIds(List<Long> operIds);

    /**
     * 清空操作日志
     */
    void cleanOperLog();
}
