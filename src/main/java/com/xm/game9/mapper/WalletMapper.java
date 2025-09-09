package com.xm.game9.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.game9.model.domain.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 钱包Mapper
 *
 * @描述 针对表【wallet(钱包表)】的数据库操作Mapper
 */
@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {

    /**
     * 根据用户ID查询钱包（悲观锁）
     *
     * @param userId 用户ID
     * @return 钱包信息
     */
    @Select("SELECT * FROM wallet WHERE userId = #{userId} FOR UPDATE")
    Wallet selectByUserIdForUpdate(@Param("userId") Long userId);

    /**
     * 根据用户ID查询钱包
     *
     * @param userId 用户ID
     * @return 钱包信息
     */
    @Select("SELECT * FROM wallet WHERE userId = #{userId}")
    Wallet selectByUserId(@Param("userId") Long userId);

    /**
     * 增加钱包余额（悲观锁）
     *
     * @param userId 用户ID
     * @param amount 增加金额
     * @return 影响行数
     */
    @Update("UPDATE wallet SET walletBalance = walletBalance + #{amount}, updateTime = NOW() " +
            "WHERE userId = #{userId} AND walletStatus = 1")
    int increaseBalance(@Param("userId") Long userId, @Param("amount") java.math.BigDecimal amount);

    /**
     * 减少钱包余额（悲观锁）
     *
     * @param userId 用户ID
     * @param amount 减少金额
     * @return 影响行数
     */
    @Update("UPDATE wallet SET walletBalance = walletBalance - #{amount}, updateTime = NOW() " +
            "WHERE userId = #{userId} AND walletStatus = 1 AND walletBalance >= #{amount}")
    int decreaseBalance(@Param("userId") Long userId, @Param("amount") java.math.BigDecimal amount);

    /**
     * 冻结钱包
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE wallet SET walletStatus = 0, updateTime = NOW() WHERE userId = #{userId}")
    int freezeWallet(@Param("userId") Long userId);

    /**
     * 解冻钱包
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE wallet SET walletStatus = 1, updateTime = NOW() WHERE userId = #{userId}")
    int unfreezeWallet(@Param("userId") Long userId);
}