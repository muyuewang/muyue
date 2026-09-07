package com.muyue.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.core.domain.entity.SysLogininfor;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录日志数据访问层
 *
 * @author muyue
 */
public interface SysLogininforMapper extends BaseMapper<SysLogininfor> {

    List<SysLogininfor> selectLogininforList(Page<SysLogininfor> page, @Param("logininfor") SysLogininfor logininfor);

    /**
     * 清空登录日志
     */
    int cleanLogininfor();
}
