package com.xm.game9.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.game9.model.domain.Wallet;
import com.xm.game9.model.domain.WalletTransaction;
import com.xm.game9.model.request.wallet.WalletRechargeRequest;
import com.xm.game9.model.request.wallet.WalletConsumeRequest;
import com.xm.game9.model.vo.wallet.WalletVO;
import com.xm.game9.model.vo.wallet.WalletTransactionVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包服务
 *
 * @描述 钱包相关业务逻辑
 */
public interface WalletService extends IService<Wallet> {

    /**
     * 根据用户ID获取钱包信息
     *
     * @param userId 用户ID
     * @return 钱包信息
     */
    WalletVO getWalletByUserId(Long userId);

    /**
     * 创建用户钱包
     *
     * @param userId 用户ID
     * @return 钱包信息
     */
    Wallet createWallet(Long userId);

    /**
     * 钱包充值
     *
     * @param request 充值请求
     * @return 充值后的钱包信息
     */
    WalletVO recharge(WalletRechargeRequest request);

    /**
     * 钱包消费
     *
     * @param request 消费请求
     * @return 消费后的钱包信息
     */
    WalletVO consume(WalletConsumeRequest request);

    /**
     * 内部消费方法（用于订单支付）
     *
     * @param userId       用户ID
     * @param amount       消费金额
     * @param description  消费描述
     * @param orderId      关联订单ID
     * @return 是否成功
     */
    boolean internalConsume(Long userId, BigDecimal amount, String description, Long orderId);

    /**
     * 获取钱包余额
     *
     * @param userId 用户ID
     * @return 钱包余额
     */
    BigDecimal getBalance(Long userId);

    /**
     * 获取用户交易记录
     *
     * @param userId 用户ID
     * @return 交易记录列表
     */
    List<WalletTransactionVO> getTransactionHistory(Long userId);

    /**
     * 获取用户交易记录（分页）
     *
     * @param userId 用户ID
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 交易记录列表
     */
    List<WalletTransactionVO> getTransactionHistoryWithPage(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 冻结钱包
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean freezeWallet(Long userId);

    /**
     * 解冻钱包
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean unfreezeWallet(Long userId);

    /**
     * 检查钱包状态
     *
     * @param userId 用户ID
     * @return 钱包状态是否正常
     */
    boolean checkWalletStatus(Long userId);

    /**
     * 转账（用户间转账）
     *
     * @param fromUserId    转出用户ID
     * @param toUserId      转入用户ID
     * @param amount        转账金额
     * @param description   转账描述
     * @return 是否成功
     */
    boolean transfer(Long fromUserId, Long toUserId, BigDecimal amount, String description);
}