package com.xm.game9.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包表
 *
 * @表名 wallet
 */
@TableName(value = "wallet")
@Data
public class Wallet implements Serializable {
    @TableField(exist = false)
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 钱包ID
     */
    @TableId(type = IdType.AUTO)
    private Long walletId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 钱包余额
     */
    private BigDecimal walletBalance;

    /**
     * 钱包状态：1-正常 0-冻结
     */
    private Integer walletStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}