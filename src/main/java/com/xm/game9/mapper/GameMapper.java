package com.xm.game9.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.game9.model.domain.Game;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author X1aoM1ngTX
 * @描述 针对表【game(游戏表)】的数据库操作Mapper
 */
@Mapper
public interface GameMapper extends BaseMapper<Game> {

    /**
     * 原子扣减库存（带悲观锁，防止超卖）
     *
     * @param gameId 游戏ID
     * @return 影响行数（1=成功，0=库存不足）
     */
    @Update("UPDATE game SET gameStock = gameStock - 1 WHERE gameId = #{gameId} AND gameStock > 0")
    int decreaseStockForUpdate(@Param("gameId") Long gameId);

    /**
     * 查询游戏信息（带悲观锁）
     *
     * @param gameId 游戏ID
     * @return 游戏信息
     */
    @Select("SELECT * FROM game WHERE gameId = #{gameId} FOR UPDATE")
    Game selectByIdForUpdate(@Param("gameId") Long gameId);

    /**
     * 释放预占的库存
     *
     * @param gameId 游戏ID
     * @return 是否成功
     */
    @Update("UPDATE game SET gameStock = gameStock + 1 WHERE gameId = #{gameId}")
    boolean releaseStock(@Param("gameId") Long gameId);
}