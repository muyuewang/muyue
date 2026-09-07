package com.muyue.common.core.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.muyue.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单主表 biz_order
 *
 * @author muyue
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_order")
public class Order extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    @TableId(type = IdType.ASSIGN_ID)
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 买家名称 */
    private String userName;

    /** 订单总额 */
    private BigDecimal totalAmount;

    /** 订单状态（0待付款 1已付款 2已发货 3已完成 4已取消） */
    private String status;

    /** 支付方式（0支付宝 1微信 2货到付款） */
    private String payType;

    /** 收货人 */
    private String receiver;

    /** 联系电话 */
    private String phone;

    /** 收货地址 */
    private String address;

    /** 订单明细（非表字段） */
    @TableField(exist = false)
    private List<OrderItem> items;
}
