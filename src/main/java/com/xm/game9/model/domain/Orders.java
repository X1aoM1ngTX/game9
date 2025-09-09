package com.xm.game9.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表
 *
 * @表名 orders
 */
@TableName(value = "orders")
@Data
public class Orders implements Serializable {
    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 游戏ID
     */
    private Long gameId;

    /**
     * 游戏名称
     */
    private String gameName;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 最终价格
     */
    private BigDecimal finalPrice;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 订单状态：1-待支付 2-已支付 3-已取消 4-已退款 5-已发货
     */
    private Integer orderStatus;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}