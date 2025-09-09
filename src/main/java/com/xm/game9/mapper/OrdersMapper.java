package com.xm.game9.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.game9.model.domain.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 订单Mapper
 *
 * @描述 针对表【orders(订单表)】的数据库操作Mapper
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 根据用户ID查询订单
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    @Select("SELECT * FROM orders WHERE userId = #{userId} ORDER BY createdTime DESC")
    List<Orders> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询订单（分页）
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制条数
     * @return 订单列表
     */
    @Select("SELECT * FROM orders WHERE userId = #{userId} " +
            "ORDER BY createdTime DESC LIMIT #{offset}, #{limit}")
    List<Orders> selectByUserIdWithPage(@Param("userId") Long userId, 
                                        @Param("offset") Integer offset, 
                                        @Param("limit") Integer limit);

    /**
     * 根据用户ID统计订单数量
     *
     * @param userId 用户ID
     * @return 订单数量
     */
    @Select("SELECT COUNT(*) FROM orders WHERE userId = #{userId}")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单
     */
    @Select("SELECT * FROM orders WHERE orderNo = #{orderNo}")
    Orders selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据订单ID查询订单
     *
     * @param orderId 订单ID
     * @return 订单
     */
    @Select("SELECT * FROM orders WHERE orderId = #{orderId}")
    Orders selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 更新订单状态
     *
     * @param orderId 订单ID
     * @param orderStatus 订单状态
     * @return 影响行数
     */
    @Update("UPDATE orders SET orderStatus = #{orderStatus}, updateTime = NOW() WHERE orderId = #{orderId}")
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("orderStatus") Integer orderStatus);

    /**
     * 更新订单状态和支付信息
     *
     * @param orderId 订单ID
     * @param orderStatus 订单状态
     * @param paymentMethod 支付方式
     * @return 影响行数
     */
    @Update("UPDATE orders SET orderStatus = #{orderStatus}, paymentMethod = #{paymentMethod}, " +
            "paymentTime = NOW(), updateTime = NOW() WHERE orderId = #{orderId}")
    int updateOrderStatusAndPayment(@Param("orderId") Long orderId, 
                                     @Param("orderStatus") Integer orderStatus,
                                     @Param("paymentMethod") String paymentMethod);

    /**
     * 根据用户ID和订单状态查询订单
     *
     * @param userId 用户ID
     * @param orderStatus 订单状态
     * @return 订单列表
     */
    @Select("SELECT * FROM orders WHERE userId = #{userId} AND orderStatus = #{orderStatus} " +
            "ORDER BY createdTime DESC")
    List<Orders> selectByUserIdAndStatus(@Param("userId") Long userId, 
                                         @Param("orderStatus") Integer orderStatus);

    /**
     * 根据游戏ID查询待支付订单数量
     *
     * @param gameId 游戏ID
     * @return 待支付订单数量
     */
    @Select("SELECT COUNT(*) FROM orders WHERE gameId = #{gameId} AND orderStatus = 1")
    Long countPendingOrdersByGameId(@Param("gameId") Long gameId);
}