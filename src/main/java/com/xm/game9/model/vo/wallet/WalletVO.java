package com.xm.game9.model.vo.wallet;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包视图对象
 *
 * @author Game9
 * @描述 钱包信息视图对象
 */
@Data
public class WalletVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long walletId;

    private Long userId;

    private String userName;

    private BigDecimal walletBalance;

    private Integer walletStatus;

    private String walletStatusDesc;

    private LocalDateTime createdTime;

    private LocalDateTime updateTime;
}