package com.xm.game9.model.request.order;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建订单请求
 *
 * @author Game9
 * @描述 创建订单请求参数
 */
@Data
public class CreateOrderRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    @NotNull(message = "游戏ID不能为空")
    private Long gameId;

    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.01", message = "订单金额必须大于0")
    private BigDecimal orderAmount;

    @NotNull(message = "支付方式不能为空")
    private String paymentMethod;

    private String description;
}