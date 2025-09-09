package com.xm.game9.model.vo.wallet;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易记录视图对象
 *
 * @author Game9
 * @描述 钱包交易记录视图对象
 */
@Data
public class WalletTransactionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long transactionId;

    private Long userId;

    private String userName;

    private Integer transactionType;

    private String transactionTypeDesc;

    private BigDecimal transactionAmount;

    private BigDecimal balanceAfter;

    private String transactionDescription;

    private Long orderId;

    private String thirdPartyTransactionId;

    private LocalDateTime createdTime;
}