package com.xm.gamehub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xm.gamehub.model.domain.Game;
import com.xm.gamehub.model.domain.UserLibrary;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author X1aoM1ngTX
 * @描述 针对表【userLibrary(用户游戏库)】的数据库操作Mapper
 * @创建时间 2024-11-13 15:11:22
 * @实体 model.domain.UserLibrary
 */
public interface UserLibraryMapper extends BaseMapper<UserLibrary> {

    @Delete("DELETE FROM UserLibrary WHERE userId = #{userId} AND gameId = #{gameId}")
    int deleteByUserIdAndGameId(Long userId, Long gameId);

    @Select("SELECT g.* FROM Game g JOIN UserLibrary ul ON g.gameId = ul.gameId WHERE ul.userId = #{userId}")
    List<Game> selectGamesByUserId(Long userId);
}




