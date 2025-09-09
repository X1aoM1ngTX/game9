package com.xm.game9.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xm.game9.model.domain.Orders;
import com.xm.game9.model.request.order.CreateOrderRequest;
import com.xm.game9.model.vo.order.OrderVO;

import java.util.List;

/**
 * 订单服务
 *
 * @author Game9
 * @描述 订单相关业务逻辑
 */
public interface OrderService extends IService<Orders> {

    /**
     * 创建订单
     *
     * @param request 创建订单请求
     * @return 订单信息
     */
    OrderVO createOrder(CreateOrderRequest request);

    /**
     * 根据订单ID获取订单信息
     *
     * @param orderId 订单ID
     * @return 订单信息
     */
    OrderVO getOrderById(Long orderId);

    /**
     * 根据订单编号获取订单信息
     *
     * @param orderNo 订单编号
     * @return 订单信息
     */
    OrderVO getOrderByOrderNo(String orderNo);

    /**
     * 根据用户ID获取订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<OrderVO> getOrdersByUserId(Long userId);

    /**
     * 支付订单
     *
     * @param orderId 订单ID
     * @param paymentMethod 支付方式
     * @return 是否成功
     */
    boolean payOrder(Long orderId, String paymentMethod);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean cancelOrder(Long orderId);

    /**
     * 退款订单
     *
     * @param orderId 订单ID
     * @param reason 退款原因
     * @return 是否成功
     */
    boolean refundOrder(Long orderId, String reason);

    /**
     * 检查订单状态
     *
     * @param orderId 订单ID
     * @return 订单状态
     */
    Integer checkOrderStatus(Long orderId);

    /**
     * 生成订单编号
     *
     * @return 订单编号
     */
    String generateOrderNo();
}