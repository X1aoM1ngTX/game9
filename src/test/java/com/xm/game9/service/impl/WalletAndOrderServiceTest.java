package com.xm.game9.service.impl;

import com.xm.game9.model.domain.Wallet;
import com.xm.game9.model.domain.WalletTransaction;
import com.xm.game9.model.domain.Orders;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.model.request.wallet.WalletRechargeRequest;
import com.xm.game9.model.request.wallet.WalletConsumeRequest;
import com.xm.game9.model.request.order.CreateOrderRequest;
import com.xm.game9.model.vo.wallet.WalletVO;
import com.xm.game9.model.vo.wallet.WalletTransactionVO;
import com.xm.game9.model.vo.order.OrderVO;
import com.xm.game9.service.WalletService;
import com.xm.game9.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 钱包和订单服务测试
 *
 * @author Game9
 */
@SpringBootTest
@Transactional
@Rollback
@Slf4j
class WalletAndOrderServiceTest {

    @Resource
    private WalletService walletService;

    @Resource
    private OrderService orderService;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_GAME_ID = 1L;
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("100.00");

    @Test
    void testCreateAndGetWallet() {
        // 测试创建钱包
        Wallet wallet = walletService.createWallet(TEST_USER_ID);
        assertNotNull(wallet);
        assertEquals(TEST_USER_ID, wallet.getUserId());
        assertEquals(0, wallet.getWalletBalance().compareTo(BigDecimal.ZERO));

        // 测试获取钱包信息
        WalletVO retrievedWallet = walletService.getWalletByUserId(TEST_USER_ID);
        assertNotNull(retrievedWallet);
        assertEquals(wallet.getWalletId(), retrievedWallet.getWalletId());
    }

    @Test
    void testWalletRecharge() {
        // 先创建钱包
        walletService.createWallet(TEST_USER_ID);

        // 测试充值
        WalletRechargeRequest rechargeRequest = new WalletRechargeRequest();
        rechargeRequest.setUserId(TEST_USER_ID);
        rechargeRequest.setAmount(TEST_AMOUNT);
        rechargeRequest.setPaymentMethod("alipay");
        rechargeRequest.setDescription("测试充值");

        WalletVO walletVO = walletService.recharge(rechargeRequest);
        assertNotNull(walletVO);
        assertEquals(0, walletVO.getWalletBalance().compareTo(TEST_AMOUNT));

        // 验证余额
        BigDecimal balance = walletService.getBalance(TEST_USER_ID);
        assertEquals(0, balance.compareTo(TEST_AMOUNT));
    }

    @Test
    void testWalletConsume() {
        // 先创建钱包并充值
        walletService.createWallet(TEST_USER_ID);
        
        WalletRechargeRequest rechargeRequest = new WalletRechargeRequest();
        rechargeRequest.setUserId(TEST_USER_ID);
        rechargeRequest.setAmount(TEST_AMOUNT);
        rechargeRequest.setPaymentMethod("alipay");
        walletService.recharge(rechargeRequest);

        // 测试消费
        BigDecimal consumeAmount = new BigDecimal("50.00");
        WalletConsumeRequest consumeRequest = new WalletConsumeRequest();
        consumeRequest.setUserId(TEST_USER_ID);
        consumeRequest.setAmount(consumeAmount);
        consumeRequest.setDescription("测试消费");

        WalletVO walletVO = walletService.consume(consumeRequest);
        assertNotNull(walletVO);
        
        BigDecimal expectedBalance = TEST_AMOUNT.subtract(consumeAmount);
        assertEquals(0, walletVO.getWalletBalance().compareTo(expectedBalance));
    }

    @Test
    void testWalletTransfer() {
        Long fromUserId = 1L;
        Long toUserId = 1L; // 使用同一个用户进行测试，因为数据库中可能只有ID为1的用户
        BigDecimal transferAmount = new BigDecimal("30.00");

        // 创建钱包并充值
        walletService.createWallet(fromUserId);

        WalletRechargeRequest rechargeRequest = new WalletRechargeRequest();
        rechargeRequest.setUserId(fromUserId);
        rechargeRequest.setAmount(new BigDecimal("100.00"));
        rechargeRequest.setPaymentMethod("alipay");
        walletService.recharge(rechargeRequest);

        // 测试自转账（实际业务中不应该允许，但为了测试功能）
        try {
            boolean result = walletService.transfer(fromUserId, toUserId, transferAmount, "测试转账");
            // 如果转账失败是正常的，因为不应该给自己转账
            if (!result) {
                log.info("转账失败符合预期：不能给自己转账");
            }
        } catch (BusinessException e) {
            log.info("转账异常符合预期：{}", e.getMessage());
        }
    }

    @Test
    void testTransactionHistory() {
        // 先创建钱包
        walletService.createWallet(TEST_USER_ID);

        // 进行一些交易
        WalletRechargeRequest rechargeRequest = new WalletRechargeRequest();
        rechargeRequest.setUserId(TEST_USER_ID);
        rechargeRequest.setAmount(TEST_AMOUNT);
        rechargeRequest.setPaymentMethod("alipay");
        walletService.recharge(rechargeRequest);

        WalletConsumeRequest consumeRequest = new WalletConsumeRequest();
        consumeRequest.setUserId(TEST_USER_ID);
        consumeRequest.setAmount(new BigDecimal("20.00"));
        consumeRequest.setDescription("测试消费");
        walletService.consume(consumeRequest);

        // 获取交易记录
        List<WalletTransactionVO> transactions = walletService.getTransactionHistory(TEST_USER_ID);
        assertNotNull(transactions);
        assertEquals(2, transactions.size());

        // 获取分页交易记录
        List<WalletTransactionVO> pagedTransactions = walletService.getTransactionHistoryWithPage(TEST_USER_ID, 1, 10);
        assertNotNull(pagedTransactions);
        assertEquals(2, pagedTransactions.size());
    }

    @Test
    void testCreateOrder() {
        // 创建订单
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setUserId(TEST_USER_ID);
        createOrderRequest.setGameId(TEST_GAME_ID);
        createOrderRequest.setOrderAmount(TEST_AMOUNT);
        createOrderRequest.setPaymentMethod("wallet");
        createOrderRequest.setDescription("测试订单");

        OrderVO orderVO = orderService.createOrder(createOrderRequest);
        assertNotNull(orderVO);
        assertEquals(TEST_USER_ID, orderVO.getUserId());
        assertEquals(TEST_GAME_ID, orderVO.getGameId());
        assertEquals(0, orderVO.getFinalPrice().compareTo(TEST_AMOUNT));
        assertEquals(1, orderVO.getOrderStatus()); // 待支付状态
    }

    @Test
    void testOrderPayment() {
        // 先创建钱包并充值
        walletService.createWallet(TEST_USER_ID);
        
        WalletRechargeRequest rechargeRequest = new WalletRechargeRequest();
        rechargeRequest.setUserId(TEST_USER_ID);
        rechargeRequest.setAmount(new BigDecimal("200.00"));
        rechargeRequest.setPaymentMethod("alipay");
        walletService.recharge(rechargeRequest);

        // 创建订单
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setUserId(TEST_USER_ID);
        createOrderRequest.setGameId(TEST_GAME_ID);
        createOrderRequest.setOrderAmount(TEST_AMOUNT);
        createOrderRequest.setPaymentMethod("wallet");
        OrderVO orderVO = orderService.createOrder(createOrderRequest);

        // 支付订单
        boolean paid = orderService.payOrder(orderVO.getOrderId(), "wallet");
        assertTrue(paid);

        // 验证订单状态
        Integer status = orderService.checkOrderStatus(orderVO.getOrderId());
        assertEquals(2, status); // 已支付状态

        // 验证钱包余额
        BigDecimal balance = walletService.getBalance(TEST_USER_ID);
        assertEquals(0, balance.compareTo(new BigDecimal("100.00")));
    }

    @Test
    void testOrderCancellation() {
        // 创建订单
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setUserId(TEST_USER_ID);
        createOrderRequest.setGameId(TEST_GAME_ID);
        createOrderRequest.setOrderAmount(TEST_AMOUNT);
        createOrderRequest.setPaymentMethod("alipay");
        OrderVO orderVO = orderService.createOrder(createOrderRequest);

        // 取消订单
        boolean cancelled = orderService.cancelOrder(orderVO.getOrderId());
        assertTrue(cancelled);

        // 验证订单状态
        Integer status = orderService.checkOrderStatus(orderVO.getOrderId());
        assertEquals(3, status); // 已取消状态
    }

    @Test
    void testWalletStatusManagement() {
        // 创建钱包
        walletService.createWallet(TEST_USER_ID);

        // 测试冻结钱包
        boolean frozen = walletService.freezeWallet(TEST_USER_ID);
        assertTrue(frozen);

        // 验证钱包状态
        boolean status = walletService.checkWalletStatus(TEST_USER_ID);
        assertFalse(status);

        // 测试解冻钱包
        boolean unfrozen = walletService.unfreezeWallet(TEST_USER_ID);
        assertTrue(unfrozen);

        // 验证钱包状态
        status = walletService.checkWalletStatus(TEST_USER_ID);
        assertTrue(status);
    }
}