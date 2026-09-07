package com.muyue.web.controller.order;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.annotation.Log;
import com.muyue.common.core.controller.BaseController;
import com.muyue.common.core.domain.R;
import com.muyue.common.core.domain.TableDataInfo;
import com.muyue.common.core.domain.entity.Order;
import com.muyue.common.enums.BusinessType;
import com.muyue.order.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单管理控制器（主从表：订单 + 订单明细）
 *
 * @author muyue
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController extends BaseController {

    private final IOrderService orderService;

    /** 订单列表（分页） */
    @PreAuthorize("@ps.hasPermi('order:order:list')")
    @GetMapping("/list")
    public TableDataInfo<Order> list(Order order) {
        Page<Order> page = orderService.selectPage(getPage(), order);
        return new TableDataInfo<>(page.getRecords(), page.getTotal());
    }

    /** 订单详情（含明细列表） */
    @PreAuthorize("@ps.hasPermi('order:order:query')")
    @GetMapping("/{orderId}")
    public R<Order> getInfo(@PathVariable Long orderId) {
        return R.ok(orderService.selectOrderById(orderId));
    }

    /** 新增订单（含明细） */
    @Log(title = "订单管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ps.hasPermi('order:order:add')")
    @PostMapping
    public R<Void> add(@RequestBody Order order) {
        return toAjax(orderService.insertOrder(order));
    }

    /** 修改订单（含明细） */
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ps.hasPermi('order:order:edit')")
    @PutMapping
    public R<Void> edit(@RequestBody Order order) {
        return toAjax(orderService.updateOrder(order));
    }

    /** 删除订单（级联删除明细） */
    @Log(title = "订单管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ps.hasPermi('order:order:remove')")
    @DeleteMapping("/{orderIds}")
    public R<Void> remove(@PathVariable String orderIds) {
        List<Long> ids = Arrays.stream(orderIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return toAjax(orderService.deleteOrderByIds(ids));
    }
}
