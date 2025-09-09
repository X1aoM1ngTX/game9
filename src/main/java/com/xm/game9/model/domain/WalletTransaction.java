package com.xm.game9.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易记录表
 *
 * @表名 walletTransaction
 */
@TableName(value = "walletTransaction")
@Data
public class WalletTransaction implements Serializable {
    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 交易ID
     */
    @TableId(type = IdType.AUTO)
    private Long transactionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 交易类型：1-充值 2-消费 3-退款 4-赠送
     */
    private Integer transactionType;

    /**
     * 交易金额（正数表示收入，负数表示支出）
     */
    private BigDecimal transactionAmount;

    /**
     * 交易后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 交易描述
     */
    private String transactionDescription;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 交易状态：0-处理中 1-成功 2-失败
     */
    private Integer transactionStatus;

    /**
     * 支付方式：模拟支付-支付宝-微信-银行卡
     */
    private String paymentMethod;

    /**
     * 第三方交易号（模拟）
     */
    private String thirdPartyTransactionId;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}