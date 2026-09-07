package com.muyue.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyue.common.core.domain.entity.SysLogininfor;

/**
 * 登录日志服务接口
 *
 * @author muyue
 */
public interface ISysLogininforService extends IService<SysLogininfor> {

    /**
     * 分页查询登录日志
     */
    Page<SysLogininfor> selectPage(Page<SysLogininfor> page, SysLogininfor logininfor);

    /**
     * 清空登录日志
     */
    void cleanLogininfor();
}
