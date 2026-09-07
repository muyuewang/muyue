package com.muyue.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.muyue.common.core.domain.entity.Order;
import com.muyue.common.core.domain.entity.OrderItem;
import com.muyue.common.utils.StringUtils;
import com.muyue.order.mapper.OrderItemMapper;
import com.muyue.order.mapper.OrderMapper;
import com.muyue.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单服务实现
 *
 * @author muyue
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final OrderItemMapper itemMapper;

    @Override
    public Page<Order> selectPage(Page<Order> page, Order order) {
        List<Order> list = baseMapper.selectOrderList(page, order);
        page.setRecords(list);
        return page;
    }

    @Override
    public Order selectOrderById(Long orderId) {
        Order order = getById(orderId);
        if (order != null) {
            order.setItems(listItems(orderId));
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean insertOrder(Order order) {
        if (StringUtils.isBlank(order.getOrderNo())) {
            order.setOrderNo("ORD" + System.currentTimeMillis());
        }
        if (StringUtils.isBlank(order.getStatus())) {
            order.setStatus("0");
        }
        if (StringUtils.isBlank(order.getPayType())) {
            order.setPayType("0");
        }
        calcAmount(order);
        boolean saved = save(order);
        if (saved) {
            saveItems(order);
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrder(Order order) {
        calcAmount(order);
        boolean updated = updateById(order);
        if (updated) {
            itemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getOrderId()));
            saveItems(order);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrderByIds(List<Long> orderIds) {
        itemMapper.delete(new LambdaQueryWrapper<OrderItem>().in(OrderItem::getOrderId, orderIds));
        return removeByIds(orderIds);
    }

    /** 根据明细行小计汇总订单总额 */
    private void calcAmount(Order order) {
        List<OrderItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            return;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
            int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
            BigDecimal line = price.multiply(BigDecimal.valueOf(quantity));
            item.setTotalPrice(line);
            total = total.add(line);
        }
        order.setTotalAmount(total);
    }

    private void saveItems(Order order) {
        List<OrderItem> items = order.getItems();
        if (items == null || items.isEmpty()) {
            return;
        }
        for (OrderItem item : items) {
            item.setItemId(null);
            item.setOrderId(order.getOrderId());
            itemMapper.insert(item);
        }
    }

    private List<OrderItem> listItems(Long orderId) {
        return itemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
    }
}
