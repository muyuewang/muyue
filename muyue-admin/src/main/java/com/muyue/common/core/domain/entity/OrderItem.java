package com.muyue.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细表 biz_order_item
 *
 * @author muyue
 */
@Data
@TableName("biz_order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 明细ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long itemId;

    /** 所属订单ID */
    private Long orderId;

    /** 商品名称 */
    private String productName;

    /** 商品单价 */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;

    /** 小计金额 */
    private BigDecimal totalPrice;
}
