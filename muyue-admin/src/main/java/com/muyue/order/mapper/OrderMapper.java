package com.muyue.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.muyue.common.core.domain.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单数据访问层
 *
 * @author muyue
 */
public interface OrderMapper extends BaseMapper<Order> {

    List<Order> selectOrderList(Page<Order> page, @Param("order") Order order);
}
