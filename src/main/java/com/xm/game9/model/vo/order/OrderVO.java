package com.xm.game9.model.vo.order;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单视图对象
 *
 * @author Game9
 * @描述 订单视图对象
 */
@Data
public class OrderVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long orderId;

    private String orderNo;

    private Long userId;

    private String userName;

    private Long gameId;

    private String gameName;

    private BigDecimal originalPrice;

    private BigDecimal finalPrice;

    private BigDecimal discountAmount;

    private String paymentMethod;

    private Integer orderStatus;

    private String orderStatusDesc;

    private LocalDateTime paymentTime;

    private LocalDateTime createdTime;

    private LocalDateTime updateTime;

    private String cancelReason;

    private String refundReason;

    private LocalDateTime refundTime;

    private String remark;
}