package com.xm.game9.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.mapper.OrdersMapper;
import com.xm.game9.mapper.GameMapper;
import com.xm.game9.mapper.WalletMapper;
import com.xm.game9.mapper.WalletTransactionMapper;
import com.xm.game9.model.domain.Orders;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.domain.Game;
import com.xm.game9.model.domain.Wallet;
import com.xm.game9.model.domain.WalletTransaction;
import com.xm.game9.model.request.order.CreateOrderRequest;
import com.xm.game9.model.vo.order.OrderVO;
import com.xm.game9.service.OrderService;
import com.xm.game9.service.WalletService;
import com.xm.game9.service.UserService;
import com.xm.game9.service.UserLibraryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * @author Game9
 * @描述 订单服务实现类
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserLibraryService userLibraryService;

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(CreateOrderRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建订单请求不能为空");
        }

        Long userId = request.getUserId();
        Long gameId = request.getGameId();
        BigDecimal orderAmount = request.getOrderAmount();

        // 参数校验
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }
        if (gameId == null || gameId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "游戏ID不合法");
        }
        if (orderAmount == null || orderAmount.compareTo(ZERO) < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单金额不能为负数");
        }

        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 检查游戏是否存在
        Game game = gameMapper.selectById(gameId);
        if (game == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "游戏不存在");
        }

        // 创建订单
        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setGameId(gameId);
        
        // 设置价格信息：originalPrice为游戏原价，finalPrice为实际支付价格
        BigDecimal originalPrice = game.getGamePrice();
        BigDecimal finalPrice = orderAmount;
        BigDecimal discountAmount = originalPrice.subtract(finalPrice);
        
        order.setOriginalPrice(originalPrice);
        order.setFinalPrice(finalPrice);
        order.setDiscountAmount(discountAmount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setOrderStatus(1); // 待支付状态
        order.setCreatedTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        boolean saved = save(order);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建订单失败");
        }

        log.info("创建订单成功 - userId: {}, gameId: {}, orderNo: {}, amount: {}", 
                userId, gameId, order.getOrderNo(), orderAmount);

        return convertToOrderVO(order);
    }

    @Override
    public OrderVO getOrderById(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID不合法");
        }

        Orders order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        return convertToOrderVO(order);
    }

    @Override
    public OrderVO getOrderByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单编号不能为空");
        }

        Orders order = lambdaQuery()
                .eq(Orders::getOrderNo, orderNo)
                .one();
        
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        return convertToOrderVO(order);
    }

    @Override
    public List<OrderVO> getOrdersByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不合法");
        }

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, userId)
               .orderByDesc(Orders::getCreatedTime);

        List<Orders> orders = list(wrapper);
        return orders.stream()
                .map(this::convertToOrderVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Long orderId, String paymentMethod) {
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID不合法");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "支付方式不能为空");
        }

        // 使用悲观锁锁定订单记录，防止重复支付
        Orders order = lambdaQuery()
                .eq(Orders::getOrderId, orderId)
                .one();
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        // 检查订单状态
        if (order.getOrderStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单状态不正确，无法支付");
        }
        
        // 双重检查：使用数据库锁确保订单状态没有被其他线程修改
        Orders lockedOrder = lambdaQuery()
                .eq(Orders::getOrderId, orderId)
                .eq(Orders::getOrderStatus, 1) // 确保仍然是待支付状态
                .last("FOR UPDATE") // 添加悲观锁
                .one();
        if (lockedOrder == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单已支付或状态已改变，请刷新页面");
        }

        // 锁定游戏记录，确保库存信息的一致性
        Game lockedGame = gameMapper.selectByIdForUpdate(lockedOrder.getGameId());
        if (lockedGame == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "游戏不存在");
        }

        // 如果是免费支付，直接跳过钱包扣款流程
        if ("FREE".equalsIgnoreCase(paymentMethod)) {
            log.info("免费游戏支付成功 - orderId: {}, orderNo: {}", orderId, lockedOrder.getOrderNo());
        }
        // 如果是钱包支付，直接在当前事务中处理钱包扣款
        else if ("wallet".equalsIgnoreCase(paymentMethod)) {
            // 使用悲观锁锁定钱包记录
            Wallet wallet = walletMapper.selectByUserIdForUpdate(lockedOrder.getUserId());
            if (wallet == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户钱包不存在");
            }
            
            // 检查钱包状态
            if (wallet.getWalletStatus() != 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "钱包已冻结，无法支付");
            }
            
            // 检查余额是否为0
            if (wallet.getWalletBalance().compareTo(ZERO) <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "余额为0，支付失败");
            }
            
            // 检查余额是否充足
            if (wallet.getWalletBalance().compareTo(lockedOrder.getFinalPrice()) < 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, 
                    String.format("余额不足，支付失败。当前余额：%.2f元，需要支付：%.2f元", 
                        wallet.getWalletBalance(), lockedOrder.getFinalPrice()));
            }
            
            // 更新钱包余额
            BigDecimal newBalance = wallet.getWalletBalance().subtract(lockedOrder.getFinalPrice());
            wallet.setWalletBalance(newBalance);
            wallet.setUpdateTime(LocalDateTime.now());
            
            boolean walletUpdated = walletService.updateById(wallet);
            if (!walletUpdated) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "钱包扣款失败");
            }
            
            // 创建钱包交易记录
            WalletTransaction transaction = new WalletTransaction();
            transaction.setUserId(lockedOrder.getUserId());
            transaction.setTransactionType(2); // 消费类型
            transaction.setTransactionAmount(lockedOrder.getFinalPrice());
            transaction.setBalanceAfter(newBalance);
            transaction.setTransactionDescription("购买游戏 - 订单号:" + lockedOrder.getOrderNo());
            transaction.setOrderId(orderId);
            transaction.setTransactionStatus(1); // 成功状态
            transaction.setPaymentMethod("WALLET");
            transaction.setCreatedTime(LocalDateTime.now());
            transaction.setUpdateTime(LocalDateTime.now());
            
            boolean transactionSaved = walletTransactionMapper.insert(transaction) > 0;
            if (!transactionSaved) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建交易记录失败");
            }
            
            log.info("钱包支付成功 - userId: {}, orderId: {}, amount: {}, newBalance: {}", 
                    lockedOrder.getUserId(), orderId, lockedOrder.getFinalPrice(), newBalance);
        }

        // 更新订单状态
        lockedOrder.setOrderStatus(2); // 已支付
        lockedOrder.setPaymentMethod(paymentMethod);
        lockedOrder.setPaymentTime(LocalDateTime.now());
        lockedOrder.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(lockedOrder);
        if (!updated) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "订单状态更新失败");
        }

        // 支付成功后，将游戏添加到用户游戏库
        try {
            boolean libraryResult = userLibraryService.addUserGame(lockedOrder.getUserId(), lockedOrder.getGameId());
            if (!libraryResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加游戏到游戏库失败");
            }
            log.info("游戏已添加到用户游戏库 - userId: {}, gameId: {}, orderId: {}", 
                    lockedOrder.getUserId(), lockedOrder.getGameId(), orderId);
        } catch (Exception e) {
            log.error("添加游戏到游戏库失败 - userId: {}, gameId: {}, orderId: {}", 
                    lockedOrder.getUserId(), lockedOrder.getGameId(), orderId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "添加游戏到游戏库失败");
        }

        log.info("订单支付成功 - orderId: {}, orderNo: {}, paymentMethod: {}", 
                orderId, lockedOrder.getOrderNo(), paymentMethod);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID不合法");
        }

        Orders order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        // 检查订单状态
        if (order.getOrderStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单状态不正确，无法取消");
        }

        // 更新订单状态
        order.setOrderStatus(3); // 已取消
        order.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(order);
        
        // 释放预占的库存
        if (updated) {
            boolean stockReleased = gameMapper.releaseStock(order.getGameId());
            if (stockReleased) {
                log.info("库存释放成功 - orderId: {}, gameId: {}", orderId, order.getGameId());
            }
            log.info("订单取消成功 - orderId: {}, orderNo: {}", orderId, order.getOrderNo());
        }
        
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean refundOrder(Long orderId, String reason) {
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID不合法");
        }

        Orders order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        // 检查订单状态
        if (order.getOrderStatus() != 2) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单状态不正确，无法退款");
        }

        // 如果是钱包支付，需要退款到钱包
        if ("wallet".equalsIgnoreCase(order.getPaymentMethod())) {
            // 使用悲观锁锁定钱包记录
            Wallet wallet = walletMapper.selectByUserIdForUpdate(order.getUserId());
            if (wallet == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户钱包不存在");
            }
            
            // 检查钱包状态
            if (wallet.getWalletStatus() != 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "钱包已冻结，无法退款");
            }
            
            // 更新钱包余额
            BigDecimal refundAmount = order.getFinalPrice();
            BigDecimal newBalance = wallet.getWalletBalance().add(refundAmount);
            wallet.setWalletBalance(newBalance);
            wallet.setUpdateTime(LocalDateTime.now());
            
            boolean walletUpdated = walletService.updateById(wallet);
            if (!walletUpdated) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "钱包退款失败");
            }
            
            // 创建退款交易记录
            WalletTransaction transaction = new WalletTransaction();
            transaction.setUserId(order.getUserId());
            transaction.setTransactionType(3);
            transaction.setTransactionAmount(refundAmount);
            transaction.setBalanceAfter(newBalance);
            transaction.setTransactionDescription("订单退款 - 订单号:" + order.getOrderNo() + " - 退款原因: " + reason);
            transaction.setOrderId(orderId);
            transaction.setTransactionStatus(1);
            transaction.setPaymentMethod("REFUND");
            transaction.setCreatedTime(LocalDateTime.now());
            transaction.setUpdateTime(LocalDateTime.now());
            
            boolean transactionSaved = walletTransactionMapper.insert(transaction) > 0;
            if (!transactionSaved) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建退款交易记录失败");
            }
            
            log.info("钱包退款成功 - userId: {}, orderId: {}, refundAmount: {}, newBalance: {}", 
                    order.getUserId(), orderId, refundAmount, newBalance);
        }

        // 更新订单状态
        order.setOrderStatus(4); // 已退款
        order.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(order);
        if (updated) {
            // 从用户游戏库中移除游戏
            try {
                boolean libraryRemoved = userLibraryService.removeUserGame(order.getUserId(), order.getGameId());
                if (libraryRemoved) {
                    log.info("游戏已从用户游戏库移除 - userId: {}, gameId: {}, orderId: {}", 
                            order.getUserId(), order.getGameId(), orderId);
                } else {
                    log.warn("从用户游戏库移除游戏失败 - userId: {}, gameId: {}, orderId: {}", 
                            order.getUserId(), order.getGameId(), orderId);
                }
            } catch (Exception e) {
                log.error("从用户游戏库移除游戏时发生错误 - userId: {}, gameId: {}, orderId: {}", 
                        order.getUserId(), order.getGameId(), orderId, e);
            }
            
            log.info("订单退款成功 - orderId: {}, orderNo: {}, reason: {}", 
                    orderId, order.getOrderNo(), reason);
        }
        return updated;
    }

    @Override
    public Integer checkOrderStatus(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单ID不合法");
        }

        Orders order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        return order.getOrderStatus();
    }

    @Override
    public String generateOrderNo() {
        // 生成格式：ORDER + 时间戳 + 随机数
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORDER" + timestamp + random;
    }

    /**
     * 转换为订单视图对象
     *
     * @param order 订单实体
     * @return 订单视图对象
     */
    private OrderVO convertToOrderVO(Orders order) {
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        
        // 设置用户名
        User user = userService.getById(order.getUserId());
        if (user != null) {
            orderVO.setUserName(user.getUserName());
        }
        
        // 设置游戏名称
        try {
            Game game = gameMapper.selectById(order.getGameId());
            if (game != null) {
                orderVO.setGameName(game.getGameName());
            }
        } catch (Exception e) {
            log.warn("获取游戏名称失败 - gameId: {}", order.getGameId(), e);
        }
        
        // 设置订单状态描述
        if (order.getOrderStatus() != null) {
            String statusDesc = switch (order.getOrderStatus()) {
                case 1 -> "待支付";
                case 2 -> "已支付";
                case 3 -> "已取消";
                case 4 -> "已退款";
                default -> "未知";
            };
            orderVO.setOrderStatusDesc(statusDesc);
        }
        
        return orderVO;
    }
}