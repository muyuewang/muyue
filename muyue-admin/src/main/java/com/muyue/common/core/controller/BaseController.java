package com.muyue.common.core.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.core.domain.R;
import com.muyue.common.utils.ServletUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 基础控制器（分页参数、通用返回）
 *
 * @author muyue
 */
public class BaseController {

    protected static final int DEFAULT_PAGE_NUM = 1;
    protected static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 从请求中获取分页参数
     */
    protected <T> Page<T> getPage() {
        HttpServletRequest request = ServletUtils.getRequest();
        int pageNum = DEFAULT_PAGE_NUM;
        int pageSize = DEFAULT_PAGE_SIZE;
        if (request != null) {
            pageNum = toInt(request.getParameter("pageNum"), DEFAULT_PAGE_NUM);
            pageSize = toInt(request.getParameter("pageSize"), DEFAULT_PAGE_SIZE);
        }
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 响应操作结果
     */
    protected R<Void> toAjax(int rows) {
        return rows > 0 ? R.ok() : R.fail();
    }

    protected R<Void> toAjax(boolean result) {
        return result ? R.ok() : R.fail();
    }

    private int toInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
