package com.xm.game9.controller;

import com.xm.game9.common.BaseResponse;
import com.xm.game9.common.ErrorCode;
import com.xm.game9.common.ResultUtils;
import com.xm.game9.constant.UserConstant;
import com.xm.game9.exception.BusinessException;
import com.xm.game9.model.domain.Orders;
import com.xm.game9.model.domain.User;
import com.xm.game9.model.request.order.CreateOrderRequest;
import com.xm.game9.model.vo.order.OrderVO;
import com.xm.game9.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 订单接口
 *
 * @author Game9
 */
@Tag(name = "订单接口", description = "订单相关的所有接口")
@RestController
@RequestMapping("/order")
@Validated
@Slf4j
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 创建订单
     *
     * @param createOrderRequest 创建订单请求
     * @param request          HTTP请求
     * @return 订单信息
     */
    @Operation(summary = "创建订单", description = "创建新的订单")
    @PostMapping("/create")
    public BaseResponse<OrderVO> createOrder(@RequestBody @Valid CreateOrderRequest createOrderRequest,
                                           HttpServletRequest request) {
        // 设置当前用户ID
        createOrderRequest.setUserId(getCurrentUserId(request));
        OrderVO orderVO = orderService.createOrder(createOrderRequest);
        return ResultUtils.success(orderVO);
    }

    /**
     * 根据订单ID获取订单信息
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 订单信息
     */
    @Operation(summary = "根据订单ID获取订单信息", description = "根据订单ID获取订单详细信息")
    @GetMapping("/{orderId}")
    public BaseResponse<OrderVO> getOrderById(@PathVariable Long orderId, HttpServletRequest request) {
        // 验证订单是否属于当前用户
        OrderVO orderVO = orderService.getOrderById(orderId);
        if (!orderVO.getUserId().equals(getCurrentUserId(request))) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权访问该订单");
        }
        return ResultUtils.success(orderVO);
    }

    /**
     * 根据订单编号获取订单信息
     *
     * @param orderNo 订单编号
     * @param request HTTP请求
     * @return 订单信息
     */
    @Operation(summary = "根据订单编号获取订单信息", description = "根据订单编号获取订单详细信息")
    @GetMapping("/by-order-no/{orderNo}")
    public BaseResponse<OrderVO> getOrderByOrderNo(@PathVariable String orderNo, HttpServletRequest request) {
        OrderVO orderVO = orderService.getOrderByOrderNo(orderNo);
        if (!orderVO.getUserId().equals(getCurrentUserId(request))) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权访问该订单");
        }
        return ResultUtils.success(orderVO);
    }

    /**
     * 获取用户的订单列表
     *
     * @param request HTTP请求
     * @return 订单列表
     */
    @Operation(summary = "获取用户订单列表", description = "获取当前用户的所有订单")
    @GetMapping("/list")
    public BaseResponse<List<OrderVO>> getUserOrders(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<OrderVO> orders = orderService.getOrdersByUserId(userId);
        return ResultUtils.success(orders);
    }

    /**
     * 支付订单
     *
     * @param orderId       订单ID
     * @param paymentMethod 支付方式
     * @param request      HTTP请求
     * @return 是否成功
     */
    @Operation(summary = "支付订单", description = "支付订单")
    @PostMapping("/{orderId}/pay")
    public BaseResponse<Boolean> payOrder(@PathVariable Long orderId,
                                        @RequestParam String paymentMethod,
                                        HttpServletRequest request) {
        // 验证订单是否属于当前用户
        OrderVO orderVO = orderService.getOrderById(orderId);
        if (!orderVO.getUserId().equals(getCurrentUserId(request))) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权操作该订单");
        }
        
        boolean result = orderService.payOrder(orderId, paymentMethod);
        return ResultUtils.success(result);
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 是否成功
     */
    @Operation(summary = "取消订单", description = "取消订单")
    @PostMapping("/{orderId}/cancel")
    public BaseResponse<Boolean> cancelOrder(@PathVariable Long orderId, HttpServletRequest request) {
        // 验证订单是否属于当前用户
        OrderVO orderVO = orderService.getOrderById(orderId);
        if (!orderVO.getUserId().equals(getCurrentUserId(request))) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权操作该订单");
        }
        
        boolean result = orderService.cancelOrder(orderId);
        return ResultUtils.success(result);
    }

    /**
     * 退款订单
     *
     * @param orderId 订单ID
     * @param reason  退款原因
     * @param request HTTP请求
     * @return 是否成功
     */
    @Operation(summary = "退款订单", description = "申请退款")
    @PostMapping("/{orderId}/refund")
    public BaseResponse<Boolean> refundOrder(@PathVariable Long orderId,
                                           @RequestParam String reason,
                                           HttpServletRequest request) {
        // 验证订单是否属于当前用户
        OrderVO orderVO = orderService.getOrderById(orderId);
        if (!orderVO.getUserId().equals(getCurrentUserId(request))) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权操作该订单");
        }
        
        boolean result = orderService.refundOrder(orderId, reason);
        return ResultUtils.success(result);
    }

    /**
     * 检查订单状态
     *
     * @param orderId 订单ID
     * @param request HTTP请求
     * @return 订单状态
     */
    @Operation(summary = "检查订单状态", description = "检查订单状态")
    @GetMapping("/{orderId}/status")
    public BaseResponse<Integer> checkOrderStatus(@PathVariable Long orderId, HttpServletRequest request) {
        // 验证订单是否属于当前用户
        OrderVO orderVO = orderService.getOrderById(orderId);
        if (!orderVO.getUserId().equals(getCurrentUserId(request))) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无权访问该订单");
        }
        
        Integer status = orderService.checkOrderStatus(orderId);
        return ResultUtils.success(status);
    }

    /**
     * 获取当前登录用户ID
     *
     * @param request HTTP请求
     * @return 用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        User user = (User) userObj;
        if (user == null || user.getUserId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "用户未登录");
        }
        return user.getUserId();
    }
}