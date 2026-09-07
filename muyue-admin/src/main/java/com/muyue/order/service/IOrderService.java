package com.muyue.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.muyue.common.core.domain.entity.Order;

import java.util.List;

/**
 * 订单服务接口
 *
 * @author muyue
 */
public interface IOrderService extends IService<Order> {

    /** 分页查询订单 */
    Page<Order> selectPage(Page<Order> page, Order order);

    /** 查询订单详情（含明细） */
    Order selectOrderById(Long orderId);

    /** 新增订单（含明细） */
    boolean insertOrder(Order order);

    /** 修改订单（含明细，全量替换明细） */
    boolean updateOrder(Order order);

    /** 批量删除订单（级联删除明细） */
    boolean deleteOrderByIds(List<Long> orderIds);
}
