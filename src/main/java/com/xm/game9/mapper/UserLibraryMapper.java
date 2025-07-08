package com.xm.game9.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.game9.model.domain.Game;
import com.xm.game9.model.domain.UserLibrary;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author X1aoM1ngTX
 * @描述 针对表【userLibrary(用户游戏库)】的数据库操作Mapper
 */
public interface UserLibraryMapper extends BaseMapper<UserLibrary> {

    /**
     * 根据用户ID和游戏ID删除用户游戏库
     *
     * @param userId 用户ID
     * @param gameId 游戏ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM UserLibrary WHERE userId = #{userId} AND gameId = #{gameId}")
    int deleteByUserIdAndGameId(Long userId, Long gameId);

    /**
     * 根据用户ID查询用户游戏库
     *
     * @param userId 用户ID
     * @return 游戏列表
     */
    @Select("SELECT g.* FROM Game g JOIN UserLibrary ul ON g.gameId = ul.gameId WHERE ul.userId = #{userId}")
    List<Game> selectGamesByUserId(Long userId);
}




