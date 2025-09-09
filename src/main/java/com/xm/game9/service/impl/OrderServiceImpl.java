package com.xm.game9.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.mapper.OrdersMapper;
import com.xm.game9.model.domain.Orders;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.order.CreateOrderRequest;
import com.xm.game9.model.vo.order.OrderVO;
import com.xm.game9.service.OrderService;
import com.xm.game9.service.WalletService;
import com.xm.game9.service.GameService;
import com.xm.game9.service.UserService;
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
    private GameService gameService;

    @Autowired
    private UserService userService;

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
        if (orderAmount == null || orderAmount.compareTo(ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单金额必须大于0");
        }

        // 检查用户是否存在
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 检查游戏是否存在
        Object game = gameService.getById(gameId);
        if (game == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "游戏不存在");
        }

        // 创建订单
        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setGameId(gameId);
        order.setOriginalPrice(orderAmount);
        order.setFinalPrice(orderAmount);
        order.setDiscountAmount(ZERO); // 暂时没有折扣
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

        // 获取订单信息
        Orders order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }

        // 检查订单状态
        if (order.getOrderStatus() != 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单状态不正确，无法支付");
        }

        // 如果是钱包支付，调用钱包服务
        if ("wallet".equals(paymentMethod)) {
            boolean consumeResult = walletService.internalConsume(
                    order.getUserId(), 
                    order.getFinalPrice(), 
                    "购买游戏 - 订单号:" + order.getOrderNo(), 
                    orderId
            );
            
            if (!consumeResult) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "钱包支付失败");
            }
        }

        // 更新订单状态
        order.setOrderStatus(2); // 已支付
        order.setPaymentMethod(paymentMethod);
        order.setPaymentTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(order);
        if (updated) {
            log.info("订单支付成功 - orderId: {}, orderNo: {}, paymentMethod: {}", 
                    orderId, order.getOrderNo(), paymentMethod);
        }
        return updated;
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
        if (updated) {
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
        if ("wallet".equals(order.getPaymentMethod())) {
            // 这里应该调用钱包服务的退款方法，但需要先实现
            // walletService.refund(order.getUserId(), order.getFinalPrice(), "退款 - " + reason);
            // 暂时记录日志
            log.warn("钱包退款功能待实现 - orderId: {}, amount: {}", orderId, order.getFinalPrice());
        }

        // 更新订单状态
        order.setOrderStatus(4); // 已退款
        order.setUpdateTime(LocalDateTime.now());

        boolean updated = updateById(order);
        if (updated) {
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
            Object game = gameService.getById(order.getGameId());
            if (game != null) {
                // 假设Game实体有getGameName方法
                orderVO.setGameName((String) game.getClass().getMethod("getGameName").invoke(game));
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