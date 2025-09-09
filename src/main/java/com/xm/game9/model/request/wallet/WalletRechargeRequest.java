package com.xm.game9.model.request.wallet;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 钱包充值请求
 *
 * @author Game9
 * @描述 钱包充值请求参数
 */
@Data
public class WalletRechargeRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long userId;

    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal amount;

    @NotNull(message = "支付方式不能为空")
    private String paymentMethod;

    private String description;

    private String thirdPartyTransactionId;
}