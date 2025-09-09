package com.xm.game9.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.game9.model.domain.WalletTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 钱包交易记录Mapper
 *
 * @描述 针对表【walletTransaction(钱包交易记录表)】的数据库操作Mapper
 */
@Mapper
public interface WalletTransactionMapper extends BaseMapper<WalletTransaction> {

    /**
     * 根据用户ID查询交易记录
     *
     * @param userId 用户ID
     * @return 交易记录列表
     */
    @Select("SELECT * FROM walletTransaction WHERE userId = #{userId} ORDER BY createdTime DESC")
    List<WalletTransaction> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询交易记录（分页）
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制条数
     * @return 交易记录列表
     */
    @Select("SELECT * FROM walletTransaction WHERE userId = #{userId} " +
            "ORDER BY createdTime DESC LIMIT #{offset}, #{limit}")
    List<WalletTransaction> selectByUserIdWithPage(@Param("userId") Long userId, 
                                                   @Param("offset") Integer offset, 
                                                   @Param("limit") Integer limit);

    /**
     * 根据用户ID统计交易记录数量
     *
     * @param userId 用户ID
     * @return 交易记录数量
     */
    @Select("SELECT COUNT(*) FROM walletTransaction WHERE userId = #{userId}")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 根据订单ID查询交易记录
     *
     * @param orderId 订单ID
     * @return 交易记录
     */
    @Select("SELECT * FROM walletTransaction WHERE orderId = #{orderId}")
    WalletTransaction selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据用户ID和交易类型查询交易记录
     *
     * @param userId 用户ID
     * @param transactionType 交易类型
     * @return 交易记录列表
     */
    @Select("SELECT * FROM walletTransaction WHERE userId = #{userId} AND transactionType = #{transactionType} " +
            "ORDER BY createdTime DESC")
    List<WalletTransaction> selectByUserIdAndType(@Param("userId") Long userId, 
                                                  @Param("transactionType") Integer transactionType);
}