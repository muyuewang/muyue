package com.muyue.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyue.common.core.domain.entity.SysLogininfor;
import com.muyue.system.mapper.SysLogininforMapper;
import java.util.List;
import com.muyue.system.service.ISysLogininforService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 登录日志服务实现
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class SysLogininforServiceImpl extends ServiceImpl<SysLogininforMapper, SysLogininfor> implements ISysLogininforService {

    @Override
    public Page<SysLogininfor> selectPage(Page<SysLogininfor> page, SysLogininfor logininfor) {
        List<SysLogininfor> list = baseMapper.selectLogininforList(page, logininfor);
        if (list != null && !list.isEmpty()) {
            page.setRecords(list);
        }
        return page;
    }

    @Override
    public void cleanLogininfor() {
        baseMapper.cleanLogininfor();
    }
}
