package com.muyue.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyue.common.core.domain.entity.SysPost;
import com.muyue.common.exception.ServiceException;
import com.muyue.system.mapper.SysPostMapper;
import com.muyue.system.service.ISysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 岗位服务实现
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements ISysPostService {

    @Override
    public Page<SysPost> selectPage(Page<SysPost> page, SysPost post) {
        List<SysPost> list = baseMapper.selectPostList(page, post);
        if (list != null && !list.isEmpty()) {
            page.setRecords(list);
        }
        return page;
    }

    @Override
    public boolean checkPostCodeUnique(SysPost post) {
        Long postId = post.getPostId() == null ? -1L : post.getPostId();
        SysPost one = getOne(new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getPostCode, post.getPostCode())
                .last("LIMIT 1"));
        return one == null || one.getPostId().equals(postId);
    }

    @Override
    public boolean checkPostNameUnique(SysPost post) {
        Long postId = post.getPostId() == null ? -1L : post.getPostId();
        SysPost one = getOne(new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getPostName, post.getPostName())
                .last("LIMIT 1"));
        return one == null || one.getPostId().equals(postId);
    }

    @Override
    public List<SysPost> selectPostAll() {
        return list(new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getStatus, "0")
                .orderByAsc(SysPost::getPostSort));
    }
}
