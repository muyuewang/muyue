package com.muyue.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyue.common.core.domain.entity.SysOperLog;
import com.muyue.system.mapper.SysOperLogMapper;
import com.muyue.system.service.ISysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志服务实现
 *
 * @author muyue
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogService {

    @Override
    public Page<SysOperLog> selectOperLogPage(Page<SysOperLog> page, SysOperLog operLog) {
        List<SysOperLog> list = baseMapper.selectOperLogList(page, operLog);
        if (list != null && !list.isEmpty()) {
            page.setRecords(list);
        }
        return page;
    }

    @Override
    public void insertOperlog(SysOperLog operLog) {
        if (operLog.getOperTime() == null) {
            operLog.setOperTime(LocalDateTime.now());
        }
        if (operLog.getStatus() == null) {
            operLog.setStatus(0);
        }
        if (operLog.getBusinessType() == null) {
            operLog.setBusinessType(0);
        }
        if (operLog.getOperatorType() == null) {
            operLog.setOperatorType(1);
        }
        if (operLog.getCostTime() == null) {
            operLog.setCostTime(0L);
        }
        try {
            baseMapper.insert(operLog);
        } catch (Exception e) {
            log.warn("操作日志入库失败：{}", e.getMessage());
        }
    }

    @Override
    public SysOperLog selectOperLogById(Long operId) {
        return baseMapper.selectById(operId);
    }

    @Override
    public boolean deleteOperLogByIds(List<Long> operIds) {
        if (operIds == null || operIds.isEmpty()) {
            return false;
        }
        return baseMapper.delete(new LambdaQueryWrapper<SysOperLog>().in(SysOperLog::getOperId, operIds)) > 0;
    }

    @Override
    public void cleanOperLog() {
        baseMapper.cleanOperLog();
    }
}
